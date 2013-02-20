/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graylog2.radio;

import java.net.InetSocketAddress;
import java.util.List;
import org.graylog2.radio.inputs.InputConfiguration;
import org.graylog2.radio.inputs.InputType;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author lennart.koopmann
 */
public class InputConfigurationParserTest {

    @Test
    public void testFromString() {
        String source = "udp logs1 0.0.0.0 22501\nudp logs2 127.0.0.1 22502\ntcp logs3 localhost 22503";
        List<InputConfiguration> inputs = InputConfigurationParser.fromString(source);

        assertEquals(3, inputs.size());
        
        assertEquals(InputType.UDP, inputs.get(0).getType());
        assertEquals("/0.0.0.0:22501", inputs.get(0).getAddress().toString());
        assertEquals("logs1", inputs.get(0).getRoutingKey());
        
        assertEquals(InputType.UDP, inputs.get(1).getType());
        assertEquals("/127.0.0.1:22502", inputs.get(1).getAddress().toString());
        assertEquals("logs2", inputs.get(1).getRoutingKey());
        
        assertEquals(InputType.TCP, inputs.get(2).getType());
        assertEquals("localhost/127.0.0.1:22503", inputs.get(2).getAddress().toString());
        assertEquals("logs3", inputs.get(2).getRoutingKey());
    }
    
    @Test
    public void testFromStringWithMultipleNewlines() {
        String source = "udp logs1 0.0.0.0 22501\n\n\nudp logs2 127.0.0.1 22502";
        List<InputConfiguration> inputs = InputConfigurationParser.fromString(source);

        assertEquals(2, inputs.size());
        
        assertEquals(InputType.UDP, inputs.get(0).getType());
        assertEquals("/0.0.0.0:22501", inputs.get(0).getAddress().toString());
        assertEquals("logs1", inputs.get(0).getRoutingKey());
        
        assertEquals(InputType.UDP, inputs.get(1).getType());
        assertEquals("/127.0.0.1:22502", inputs.get(1).getAddress().toString());
        assertEquals("logs2", inputs.get(1).getRoutingKey());
    }
    
    @Test
    public void testFromStringWithMultipleSpaces() {
        String source = "udp      logs1 0.0.0.0 22501\nudp logs2 127.0.0.1 22502";
        List<InputConfiguration> inputs = InputConfigurationParser.fromString(source);

        assertEquals(2, inputs.size());
        
        assertEquals(InputType.UDP, inputs.get(0).getType());
        assertEquals("/0.0.0.0:22501", inputs.get(0).getAddress().toString());
        assertEquals("logs1", inputs.get(0).getRoutingKey());
        
        assertEquals(InputType.UDP, inputs.get(1).getType());
        assertEquals("/127.0.0.1:22502", inputs.get(1).getAddress().toString());
        assertEquals("logs2", inputs.get(1).getRoutingKey());
    }
    
    @Test
    public void testFromStringWithInvalidInItSpaces() {
        String source = "udp logs1 0.0.0.0 22501\nudp logs2 127.0.0.1 wat\ntcp logs3 localhost 22503";
        List<InputConfiguration> inputs = InputConfigurationParser.fromString(source);

        assertEquals(2, inputs.size());
        
        assertEquals(InputType.UDP, inputs.get(0).getType());
        assertEquals("/0.0.0.0:22501", inputs.get(0).getAddress().toString());
        assertEquals("logs1", inputs.get(0).getRoutingKey());
        
        assertEquals(InputType.TCP, inputs.get(1).getType());
        assertEquals("localhost/127.0.0.1:22503", inputs.get(1).getAddress().toString());
        assertEquals("logs3", inputs.get(1).getRoutingKey());
    }
    
    @Test
    public void testFromStringWithInvalidGarbageInit() {
        String source = "udp logs1 0.0.0.0 22501\nZOMG SOMETHING ELSE\nudp logs2 127.0.0.1 22502";
        List<InputConfiguration> inputs = InputConfigurationParser.fromString(source);

        assertEquals(2, inputs.size());
        
        assertEquals(InputType.UDP, inputs.get(0).getType());
        assertEquals("/0.0.0.0:22501", inputs.get(0).getAddress().toString());
        assertEquals("logs1", inputs.get(0).getRoutingKey());
        
        assertEquals(InputType.UDP, inputs.get(1).getType());
        assertEquals("/127.0.0.1:22502", inputs.get(1).getAddress().toString());
        assertEquals("logs2", inputs.get(1).getRoutingKey());
    }
    
    @Test
    public void testFromStringWithGarbageOnly() {
        String source = "ohai";
        List<InputConfiguration> inputs = InputConfigurationParser.fromString(source);

        assertEquals(0, inputs.size());
    }
}
