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
import java.util.Set;
import org.graylog2.radio.inputs.InputConfiguration;

/**
 * @author Lennart Koopmann <lennart@torch.sh>
 */
public class InputConfigurationParser {
 
    public static Set<InputConfiguration> fromString(String source) {
        Set<InputConfiguration> configs = Sets.newHashSet();
        
        for(String line : source.split("\n")) {
            System.out.println("LINE: " + line);
        }
        
        return configs;
    }
    
}
