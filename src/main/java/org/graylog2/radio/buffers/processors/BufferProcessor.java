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
package org.graylog2.radio.buffers.processors;

import com.lmax.disruptor.EventHandler;
import org.graylog2.radio.Radio;
import org.graylog2.radio.buffers.MessageEvent;
import org.graylog2.radio.messages.RawMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lennart Koopmann <lennart@torch.sh>
 */
public class BufferProcessor implements EventHandler<MessageEvent> {
    
    private static final Logger LOG = LoggerFactory.getLogger(BufferProcessor.class);

    private final Radio radio;
    private final long ordinal;
    private final long numberOfConsumers;
    
    //private final AMQPEmitter emitter;

    public BufferProcessor(Radio radio, final long ordinal, final long numberOfConsumers) {
        this.ordinal = ordinal;
        this.numberOfConsumers = numberOfConsumers;
        this.radio = radio;
        
        //this.emitter = new AMQPEmitter(acceptor, acceptor.getConfiguration().getClusterName());
    }
    
    @Override
    public void onEvent(MessageEvent event, long sequence, boolean endOfBatch) throws Exception {
        // Because Trisha said so. (http://code.google.com/p/disruptor/wiki/FrequentlyAskedQuestions)
        if ((sequence % numberOfConsumers) != ordinal) {
            return;
        }
        
        radio.bufferWatermark().decrementAndGet();

        RawMessage msg = event.getMessage();
        
        LOG.debug("Writing message from {} to bus.", msg.getSourceInput());

        // Write message to AMQP broker.
    }
    
}
