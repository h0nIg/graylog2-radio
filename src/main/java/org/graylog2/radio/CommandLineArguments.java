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

import com.beust.jcommander.Parameter;

/**
 * @author Lennart Koopmann <lennart@torch.sh>
 */
public class CommandLineArguments {
    
    public static final String STANDARD_CONFIG_FILE = "/etc/graylog2-radio.conf";

    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    private static final String TMPDIR = System.getProperty("java.io.tmpdir", "/tmp");
    
    @Parameter(names = {"-f", "--config-file"}, description = "Path to a specific config file")
    private String configFile;
    
    @Parameter(names = {"-p", "--pidfile"}, description = "File containing the PID of graylog2")
    private String pidFile = TMPDIR + FILE_SEPARATOR + "graylog2-radio.pid";
    
    @Parameter(names = {"-d", "--debug"}, description = "Run in debug mode")
    private boolean debug = false;
    
    @Parameter(names = {"-h", "--help"}, description = "Show help", help = true)
    private boolean isHelp;
    
    public String getConfigFile() {
        if (configFile == null || configFile.isEmpty()) {
            return STANDARD_CONFIG_FILE;
        }

        return configFile;
    }
    
    public String getPidFile() {
        return pidFile;
    }

    public boolean isDebug() {
        return debug;
    }
    
    public boolean isHelp() {
        return isHelp;
    }

}
