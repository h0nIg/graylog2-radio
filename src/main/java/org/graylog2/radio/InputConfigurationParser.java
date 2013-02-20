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

import com.google.common.collect.Lists;
import java.net.InetSocketAddress;
import java.util.List;
import org.graylog2.radio.inputs.InputConfiguration;
import org.graylog2.radio.inputs.InputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lennart Koopmann <lennart@torch.sh>
 */
public class InputConfigurationParser {
 
    private static final Logger LOG = LoggerFactory.getLogger(InputConfigurationParser.class);
    
    /**
     * Parses an input configuration String (usually read from inputs.conf).
     * Does this without much checking the values for validity. Expects a
     * list of newline separated input definitions like this:
     * 
     *   udp logs1 0.0.0.0 22501
     *   tcp logs3 0.0.0.0 22503
     * 
     * @param source The configuration String to read
     * @return List of input configurations
     */
    public static List<InputConfiguration> fromString(String source) {
        List<InputConfiguration> configs = Lists.newArrayList();
        
        for(String line : source.split("\n")) {
            try {
                String[] parts = line.trim().replaceAll(" +", " ").split(" ");

                if (parts.length != 4) {
                    LOG.warn("Skipping invalid input config. Not consisting of 4 segments.");
                    continue;
                }

                InputConfiguration config = new InputConfiguration(
                        new InetSocketAddress(parts[2], Integer.parseInt(parts[3])),
                        InputType.valueOf(parts[0].toUpperCase()),
                        parts[1]
                );
                
                configs.add(config);
            } catch (Exception e) {
                LOG.warn("Exception when trying to parse input config. Skipping.", e);
                continue;
            }
        }
        
        return configs;
    }
    
}
