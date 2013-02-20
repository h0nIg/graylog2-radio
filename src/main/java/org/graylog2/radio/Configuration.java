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

import com.github.joschi.jadconfig.Parameter;
import com.github.joschi.jadconfig.validators.InetPortValidator;
import com.github.joschi.jadconfig.validators.PositiveIntegerValidator;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lennart Koopmann <lennart@torch.sh>
 */
public class Configuration {
    
    private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);

    @Parameter(value = "inputs_file", required = true)
    private String inputsFile = "/etc/graylog2-radio-inputs.conf";
    
    @Parameter(value = "amqp_user", required = true)
    private String amqpUser = "guest";
    
    @Parameter(value = "amqp_pass", required = true)
    private String amqpPass = "guest";
    
    @Parameter(value = "amqp_host", required = true)
    private String amqpHost = "127.0.0.1";
    
    @Parameter(value = "amqp_port", required = true, validator = InetPortValidator.class)
    private int amqpPort = 5672;
    
    @Parameter(value = "udp_recvbuffer_sizes", required = true, validator = PositiveIntegerValidator.class)
    private int udpRecvBufferSizes = 1048576;
    
    @Parameter(value = "processor_wait_strategy", required = true)
    private String processorWaitStrategy = "sleeping";
    
    @Parameter(value = "processbuffer_processors", required = true, validator = PositiveIntegerValidator.class)
    private int processBufferProcessors = 5;
    
    @Parameter(value = "ring_size", required = true, validator = PositiveIntegerValidator.class)
    private int ringSize = 1024;
    
    public String getInputsFile() {
        return inputsFile;
    }
    
    public String getAMQPUser() {
        return amqpUser;
    }
    
    public String getAMQPPassword() {
        return amqpPass;
    }    
    
    public String getAMQPHost() {
        return amqpHost;
    }
    
    public int getAMQPPort() {
        return amqpPort;
    }

    public int getUdpRecvBufferSizes() {
        return udpRecvBufferSizes;
    }
    
    public WaitStrategy getProcessorWaitStrategy() {
        if (processorWaitStrategy.equals("sleeping")) {
            return new SleepingWaitStrategy();
        }
        
        if (processorWaitStrategy.equals("yielding")) {
            return new YieldingWaitStrategy();
        }
        
        if (processorWaitStrategy.equals("blocking")) {
            return new BlockingWaitStrategy();
        }
        
        if (processorWaitStrategy.equals("busy_spinning")) {
            return new BusySpinWaitStrategy();
        }
        
        LOG.warn("Invalid setting for [processor_wait_strategy]:"
                + " Falling back to default: SleepingWaitStrategy.");
        return new SleepingWaitStrategy();
    }

    public int getRingSize() {
        return ringSize;
    }
    
    public int getProcessBufferProcessors() {
        return processBufferProcessors;
    }

}
