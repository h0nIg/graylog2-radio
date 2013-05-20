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
package org.graylog2.radio.inputs.udp;

import org.graylog2.radio.Radio;
import org.graylog2.radio.gelf.GELFChunkManager;
import org.graylog2.radio.inputs.InputConfiguration;
import org.graylog2.radio.inputs.InputType;
import org.graylog2.radio.inputs.MessageDispatcher;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;

/**
 * @author Lennart Koopmann <lennart@torch.sh>
 */
public class UDPPipelineFactory implements ChannelPipelineFactory {

    private final MessageDispatcher dispatcher;

    public UDPPipelineFactory(MessageDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        return Channels.pipeline(dispatcher);
    }
    
}
