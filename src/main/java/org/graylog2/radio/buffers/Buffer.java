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
package org.graylog2.radio.buffers;

import org.graylog2.radio.messages.RawMessage;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.MultiThreadedClaimStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.graylog2.radio.Radio;
import org.graylog2.radio.buffers.processors.BufferProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lennart Koopmann <lennart@torch.sh>
 */
public class Buffer {
    
    private static final Logger LOG = LoggerFactory.getLogger(Buffer.class);
    
    private final Radio radio;

    protected static RingBuffer<MessageEvent> ringBuffer;

    protected ExecutorService executor = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder()
                .setNameFormat("bufferprocessor-%d")
                .build()
    );
    
    private final Meter incomingMessages = Metrics.newMeter(Buffer.class, "InsertedMessages", "messages", TimeUnit.SECONDS);
    private final Meter rejectedMessages = Metrics.newMeter(Buffer.class, "RejectedMessages", "messages", TimeUnit.SECONDS);

    public Buffer(Radio radio) {
        this.radio = radio;
    }

    public void initialize() {
        Disruptor<MessageEvent> disruptor = new Disruptor<MessageEvent>(
                MessageEvent.EVENT_FACTORY,
                executor,
                new MultiThreadedClaimStrategy(radio.getConfiguration().getRingSize()),
                radio.getConfiguration().getProcessorWaitStrategy()
        );
        
        LOG.info("Initialized buffer with ring size <{}> "
                + "and wait strategy <{}>.", radio.getConfiguration().getRingSize(),
                radio.getConfiguration().getProcessorWaitStrategy().getClass().getSimpleName());

        BufferProcessor[] processors = new BufferProcessor[radio.getConfiguration().getProcessBufferProcessors()];
        
        for (int i = 0; i < radio.getConfiguration().getProcessBufferProcessors(); i++) {
            processors[i] = new BufferProcessor(radio, i, radio.getConfiguration().getProcessBufferProcessors());
        }
        
        disruptor.handleEventsWith(processors);
        
        ringBuffer = disruptor.start();
    }
    
    public void insert(RawMessage message) throws BufferOutOfCapacityException {
        if (!hasCapacity()) {
            rejectedMessages.mark();
            LOG.debug("Rejecting message, because I am full. Raise my size or add more processors.");
            throw new BufferOutOfCapacityException();
        }
        
        long sequence = ringBuffer.next();
        MessageEvent event = ringBuffer.get(sequence);
        event.setMessage(message);
        ringBuffer.publish(sequence);

        incomingMessages.mark();
        radio.bufferWatermark().incrementAndGet();
    }

    public boolean hasCapacity() {
        return ringBuffer.remainingCapacity() > 0;
    }
    
}
