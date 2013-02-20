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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.graylog2.radio.Radio;
import org.graylog2.radio.inputs.Input;
import org.graylog2.radio.inputs.InputConfiguration;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lennart Koopmann <lennart@torch.sh>
 */
public class TCPInput implements Runnable, Input {
    
    private static final Logger LOG = LoggerFactory.getLogger(TCPInput.class);

    private final Radio radio;
    private final InputConfiguration config;
    private int startedAt = 0;
    
    public TCPInput(Radio radio, InputConfiguration config) {
        this.config = config;
        this.radio = radio;
    }
    
    @Override
    public void run() {
        final ExecutorService bossThreadPool = Executors.newCachedThreadPool(
                new ThreadFactoryBuilder()
                .setNameFormat("input-tcp-boss-%d")
                .build());
        
        final ExecutorService workerThreadPool = Executors.newCachedThreadPool(
                new ThreadFactoryBuilder()
                .setNameFormat("input-tcp-worker-%d")
                .build());

        ServerBootstrap tcpBootstrap = new ServerBootstrap(
            new NioServerSocketChannelFactory(bossThreadPool, workerThreadPool)
        );

        tcpBootstrap.setPipelineFactory(new TCPPipelineFactory(radio, config));

        try {
            tcpBootstrap.bind(config.getAddress());
        } catch (ChannelException e) {
            LOG.error("Could not bind TCP input {}", config.getAddress(), e);
        }
    }

    @Override
    public InputConfiguration getConfiguration() {
        return config;
    }

    @Override
    public int getStartedAt() {
        return startedAt;
    }
    
}
