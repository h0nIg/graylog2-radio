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

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.librato.metrics.LibratoReporter;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.reporting.GraphiteReporter;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.graylog2.radio.buffers.Buffer;
import org.graylog2.radio.inputs.InputConfiguration;
import org.graylog2.radio.inputs.Input;
import org.graylog2.radio.inputs.InputFactory;
import org.graylog2.radio.inputs.NoInputFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lennart Koopmann <lennart@torch.sh>
 */
public class Radio {
    
    private static final Logger LOG = LoggerFactory.getLogger(Radio.class);
    
    private Configuration configuration;
    
    private Connection brokerConnection;
    
    private final Buffer buffer;
    
    private AtomicInteger bufferWatermark = new AtomicInteger();
    
    private final Meter inputSpawns = Metrics.newMeter(Radio.class, "SpawnedInputs", "messages", TimeUnit.SECONDS);
    
    final ExecutorService inputThreadPool;
    final Set<Input> activeInputs = Sets.newHashSet();
    
    public Radio() {
        buffer = new Buffer(this);
        
        inputThreadPool = Executors.newCachedThreadPool(
                new ThreadFactoryBuilder()
                .setNameFormat("inputs-%d")
                .build());
    }
    
    public void initialize(List<InputConfiguration> initialInputs) throws IOException {
        // Initialize metrics.
        if (configuration.isEnableGraphite()) { enableGraphiteReporter(); }
        if (configuration.isEnableLibratoMetrics()) { enableLibratoReporter(); }
        
        // Initialize buffer.
        buffer.initialize();
        
        // Initially connect to AMQP broker.
        while (true) {
            try {
                brokerConnection = getBroker();
                break;
            } catch(IOException e) {
                LOG.warn("Could not connect to AMQP broker. Retrying in 5 seconds.", e);
                
                try {
                    Thread.sleep(5*1000);
                } catch(InterruptedException ex) { /* */ }
            }
        }
        
        // Start REST API.
        URI restUri = UriBuilder.fromUri(configuration.getRestListenUri())
                                .port(configuration.getRestListenPort()).build();
        startRestServer(restUri);
        LOG.info("Started REST API at <{}>", restUri);
        
        // Spawn inputs.
        for (InputConfiguration config : initialInputs) {
            try {
                spawnInput(InputFactory.get(this, config));
            } catch(NoInputFoundException e) {
                LOG.warn("No input for type [{}] available. Skipping.", config.getType());
            }
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
    
    public synchronized void spawnInput(Input input) {
        LOG.info("Spawning input [{}]", input);
        inputSpawns.mark();
        
        activeInputs.add(input);
        inputThreadPool.submit((Runnable) input);
    }
    
    public Set<Input> getActiveInputs() {
        return activeInputs;
    }
    
    private HttpServer startRestServer(URI restUri) throws IOException {
        ResourceConfig rc = new PackagesResourceConfig("org.graylog2.radio.rest.resources");
        rc.getProperties().put("radio", this);
        return GrizzlyServerFactory.createHttpServer(restUri, rc);
    }
    
    public Connection getBroker() throws IOException {
        if (brokerConnection == null || !brokerConnection.isOpen()) {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(configuration.getAMQPHost());
            factory.setPort(configuration.getAMQPPort());
            factory.setUsername(configuration.getAMQPUser());
            factory.setPassword(configuration.getAMQPPassword());
            
            brokerConnection = factory.newConnection();
        }
        
        return brokerConnection;
    }
    
    private void enableGraphiteReporter() {
        GraphiteReporter.enable(
                5,
                TimeUnit.SECONDS,
                configuration.getGraphiteCarbonHost(),
                configuration.getGraphiteCarbonTcpPort(),
                configuration.getGraphitePrefix()
        );
    }
    
    private void enableLibratoReporter() {
        LibratoReporter.enable(
                LibratoReporter.builder(
                    configuration.getLibratoMetricsAPIUser(),
                    configuration.getLibratoMetricsAPIToken(),
                    configuration.getLibratoMetricsSource()
                ),
                configuration.getLibratoMetricsInterval(),
                TimeUnit.SECONDS
        );
    }
    
}
