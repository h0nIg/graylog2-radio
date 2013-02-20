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
package org.graylog2.radio;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.graylog2.radio.buffers.Buffer;
import org.graylog2.radio.inputs.InputConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lennart Koopmann <lennart@torch.sh>
 */
public class Radio {
    
    private static final Logger LOG = LoggerFactory.getLogger(Radio.class);
    
    private Configuration configuration;
    
    private final Buffer buffer;
    
    private AtomicInteger bufferWatermark = new AtomicInteger();
    
    public Radio() {
        buffer = new Buffer(this);
    }
    
    public void initialize(List<InputConfiguration> initialInputs) throws IOException {
        // Initialize buffer.
        buffer.initialize();
        
        // Connect to AMQP broker.
        
        // Spawn inputs.
        for (InputConfiguration input : initialInputs) {
            LOG.info("Initializing input <{}>", input);
        }
    }
    
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
    
    public Configuration getConfiguration() {
        return configuration;
    }
    
    public Buffer getBuffer() {
        return buffer;
    }
    
    public AtomicInteger bufferWatermark() {
        return bufferWatermark;
    }
    
}
