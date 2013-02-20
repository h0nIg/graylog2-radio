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

import ch.qos.logback.classic.Level;
import com.beust.jcommander.JCommander;
import com.github.joschi.jadconfig.JadConfig;
import com.github.joschi.jadconfig.RepositoryException;
import com.github.joschi.jadconfig.ValidationException;
import com.github.joschi.jadconfig.repositories.PropertiesRepository;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Set;
import org.graylog2.radio.inputs.InputConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lennart Koopmann <lennart@torch.sh>
 */
public class Main {
    
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
        
        LOG.info("Starting up.");
        
        final CommandLineArguments cli = new CommandLineArguments();
        final JCommander jCommander = new JCommander(cli, args);
        jCommander.setProgramName("graylog2-radio");
        
        // Show help and exit.
        if (cli.isHelp()) {
            jCommander.usage();
            System.exit(0);
        }
        
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        if (cli.isDebug()) {
            root.setLevel(Level.ALL);
        } else {
            root.setLevel(Level.INFO);
        }        
        
        LOG.info("Loading configuration: " + cli.getConfigFile());
        
        Configuration configuration = new Configuration();
        JadConfig jadConfig = new JadConfig(new PropertiesRepository(cli.getConfigFile()), configuration);

        // Parse configuration.
        try {
            jadConfig.process();
        } catch (RepositoryException e) {
            LOG.error("Couldn't load configuration file " + cli.getConfigFile(), e);
            System.exit(1);
        } catch (ValidationException e) {
            LOG.error("Invalid configuration", e);
            System.exit(1);
        }
        
        // Parse input configuration;
        Set<InputConfiguration> inputs = Sets.newHashSet();
        try {
            String inputDefinition = Tools.readFile(configuration.getInputsFile());
            inputs = InputConfigurationParser.fromString(inputDefinition);
            LOG.info("Read {} initial inputs from {}", inputs.size(), configuration.getInputsFile());
        } catch (Exception e) {
            LOG.error("Could not read initial set of inputs. Terminating.", e);
            System.exit(1);
        }

        Radio radio = new Radio();
        radio.setConfiguration(configuration);
        
        try {
            radio.initialize(inputs);
        } catch(IOException e) {
            LOG.error("IOException on startup. Terminating.", e);
            System.exit(1);
        }
        
    }
    
}
