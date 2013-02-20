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
package org.graylog2.radio.amqp.emitter;

import com.rabbitmq.client.Channel;
import java.io.IOException;
import org.graylog2.radio.Radio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lennart Koopmann <lennart@torch.sh>
 */
public class AMQPEmitter {
    
    protected final static Logger LOG = LoggerFactory.getLogger(AMQPEmitter.class);
    
    private Channel channel;
    private final String exchangeName;
    private final Radio radio;
    
    public AMQPEmitter(Radio radio, String exchangeName) {
        this.radio = radio;
        this.exchangeName = exchangeName;
    }
    
    public void write(byte[] payload, String routingKey) throws Exception {
        if (channel == null || !channel.isOpen() || channel.getConnection() == null || !channel.getConnection().isOpen()) {
            connect();
            LOG.info("Connected to target AMQP broker {}:{}/{}",
                    radio.getConfiguration().getAMQPHost(),
                    radio.getConfiguration().getAMQPPort(),
                    exchangeName);
        }

        channel.basicPublish(exchangeName, routingKey, null, payload);
    }
    
    private void connect() throws IOException {
        channel = radio.getBroker().createChannel();
        channel.exchangeDeclare(exchangeName, "topic", true);
    }
    
}
