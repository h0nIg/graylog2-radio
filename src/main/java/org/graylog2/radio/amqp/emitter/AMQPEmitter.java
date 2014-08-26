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
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
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
    
    private final Meter writtenMessages = Metrics.newMeter(AMQPEmitter.class, "WrittenMessages", "messages", TimeUnit.SECONDS);
    private final Meter amqpConnects = Metrics.newMeter(AMQPEmitter.class, "AMQPConnects", "connects", TimeUnit.SECONDS);
    private final Timer processTime = Metrics.newTimer(AMQPEmitter.class, "ProcessTimeMilliseconds", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
    
    public AMQPEmitter(Radio radio, String exchangeName) {
        this.radio = radio;
        this.exchangeName = exchangeName;
    }
    
    public void write(byte[] payload, String routingKey) throws Exception {
        TimerContext tcx = processTime.time();

        while (true) {
            try {
                if (channel == null || !channel.isOpen() || channel.getConnection() == null || !channel.getConnection().isOpen()) {
                    connect();
                    LOG.info("Connected to target AMQP broker {}:{}/{}",
                            radio.getConfiguration().getAMQPHost(),
                            radio.getConfiguration().getAMQPPort(),
                            exchangeName);
                }

                writtenMessages.mark();

                channel.basicPublish(exchangeName, routingKey, null, payload);
                channel.waitForConfirms();

                break;
            } catch (Exception e) {
                LOG.info("Error while sending message to AMQP broker", e);

                continue;
            }
        }

        tcx.stop();
    }
    
    private void connect() throws IOException {
        amqpConnects.mark();

        channel = radio.getBroker().createChannel();
        channel.confirmSelect();
    }
}
