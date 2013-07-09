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
package org.graylog2.radio.inputs.tcp;

import org.graylog2.radio.Radio;
import org.graylog2.radio.inputs.InputConfiguration;
import org.graylog2.radio.inputs.MessageDispatcher;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;

/**
 * @author Lennart Koopmann <lennart@torch.sh>
 */
public class TCPPipelineFactory implements ChannelPipelineFactory {

    private final Radio radio;
    private final InputConfiguration config;
    private final boolean useNulSeparator;

    public TCPPipelineFactory(Radio radio, InputConfiguration config, boolean useNulSeparator) {
        this.radio = radio;
        this.config = config;
        this.useNulSeparator = useNulSeparator;
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelBuffer[] delimiter;
        if (useNulSeparator) {
            delimiter = Delimiters.nulDelimiter();
        } else {
            delimiter = Delimiters.lineDelimiter();
        }

        ChannelPipeline p = Channels.pipeline();
        p.addLast("framer", new DelimiterBasedFrameDecoder(2 * 1024 * 1024, delimiter));
        p.addLast("handler", new MessageDispatcher(radio, config));
        return p;
    }
    
}
