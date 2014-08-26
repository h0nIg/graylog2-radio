/**
 * Copyright 2013 Lennart Koopmann <lennart@socketfeed.com>
 *
 * This file is part of Graylog2.
 *
 * Graylog2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog2.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.graylog2.radio.inputs;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;
import java.util.concurrent.TimeUnit;
import org.graylog2.radio.Radio;
import org.graylog2.radio.gelf.GELFChunkManager;
import org.graylog2.radio.inputs.tcp.TCPInput;
import org.graylog2.radio.inputs.udp.UDPInput;
import org.graylog2.radio.messages.RawMessage;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lennart Koopmann <lennart@torch.sh>
 */
public class MessageDispatcher extends SimpleChannelHandler {
    
    private static final Logger LOG = LoggerFactory.getLogger(MessageDispatcher.class);

    public static final byte[] GELF_CHUNK_MAGIC_BYTES = { (byte) 0x1e, (byte) 0x0f };

    private final Radio radio;
    private final InputConfiguration config;
    private GELFChunkManager chunkManager = null;
    
    private final Meter dispatchedMessages = Metrics.newMeter(MessageDispatcher.class, "DispatchedMessages", "messages", TimeUnit.SECONDS);
    private final Meter incomingTcpMessages = Metrics.newMeter(TCPInput.class, "IncomingMessages", "messages", TimeUnit.SECONDS);
    private final Meter incomingUdpMessages = Metrics.newMeter(UDPInput.class, "IncomingMessages", "messages", TimeUnit.SECONDS);
    
    public MessageDispatcher(Radio radio, InputConfiguration config) {
        this.radio = radio;
        this.config = config;
    }

    public MessageDispatcher(Radio radio, InputConfiguration config, GELFChunkManager chunkManager) {
        this.radio = radio;
        this.config = config;
        this.chunkManager = chunkManager;
    }
    
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

        ChannelBuffer buffer = (ChannelBuffer) e.getMessage();

        byte[] readable = new byte[buffer.readableBytes()];
        buffer.toByteBuffer().get(readable, buffer.readerIndex(), buffer.readableBytes());
        
        messageReceived(readable);
    }

    public void messageReceived(byte[] message) throws Exception {
        if (message.length > 0) {
            dispatchedMessages.mark();

            // Source input metrics. (doing that here instead of an own Netty pipeline step)
            switch (config.getType()) {
                case TCP:
                    incomingTcpMessages.mark();
                case UDP:
                    incomingUdpMessages.mark();
            }

            // UDP input can receive GELF chunks.
            if (config.getType() == InputType.UDP && chunkManager != null && isGELFChunk(message)) {
                LOG.debug("Received a GELF chunk. Handing over to chunk manager.");
                chunkManager.insert(message);
                return;
            }

            // Write to AMQP.
            RawMessage msg = new RawMessage(message, config);
            radio.getBuffer().insert(msg);
        }
    }

    private boolean isGELFChunk(byte[] payload) {
        return payload.length > 2 && payload[0] == GELF_CHUNK_MAGIC_BYTES[0] && payload[1] == GELF_CHUNK_MAGIC_BYTES[1];
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        LOG.error("Could not handle message.", e.getCause());
    }
    
}
