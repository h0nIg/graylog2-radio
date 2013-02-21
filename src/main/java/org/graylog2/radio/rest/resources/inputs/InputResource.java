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
package org.graylog2.radio.rest.resources.inputs;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.jersey.api.core.ResourceConfig;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.graylog2.radio.Radio;
import org.graylog2.radio.inputs.Input;
import org.graylog2.radio.inputs.InputConfiguration;
import org.graylog2.radio.inputs.InputFactory;
import org.graylog2.radio.inputs.NoInputFoundException;
import org.graylog2.radio.rest.json.requests.CreateInputRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lennart Koopmann <lennart@torch.sh>
 */
@Path("/inputs")
public class InputResource {
    
    private static final Logger LOG = LoggerFactory.getLogger(InputResource.class);
    
    @Context ResourceConfig rc;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String list(@QueryParam("pretty") boolean prettyPrint) {
        Radio radio = (Radio) rc.getProperty("radio");

        Map<String, Object> result = buildInputMapResult(radio.getActiveInputs());

        Gson gson = new Gson();
        
        if (prettyPrint) {
            gson = new GsonBuilder().setPrettyPrinting().create();
        }
        
        return gson.toJson(result);
    }
    
    @POST
    public Response create(String body) {
        Radio radio = (Radio) rc.getProperty("radio");

        CreateInputRequest ir;
        
        try {
            ir = new Gson().fromJson(body, CreateInputRequest.class);
        } catch (JsonSyntaxException e) {
            LOG.info("Failed API create input request.", e);
            return Response.status(Status.BAD_REQUEST)
                    .entity("Invalid JSON.").build();
        }
    
        InputConfiguration config;
        try {
            config = new InputConfiguration(
                    ir.getAddress(),
                    ir.getInputType(),
                    ir.routingKey
            );
        } catch (IllegalArgumentException e) {
            LOG.info("Failed API create input request.", e);
            return Response.status(Status.BAD_REQUEST)
                    .entity("Missing or invalid parameters.").build();
        }
        
        try {
            radio.spawnInput(InputFactory.get(radio, config));
        } catch (NoInputFoundException e) {
            LOG.info("Failed API create input request.", e);
            return Response.status(Status.BAD_REQUEST)
                    .entity("Invalid input type given.").build();
        }
        
        return Response.status(Response.Status.CREATED).build();
    }
    
    private Map<String, Object> buildInputMapResult(Set<Input> inputList) {        
        Map<String, Object> result = Maps.newHashMap();
        Set<Map<String, Object>> inputResult = Sets.newHashSet();

        for (Input i : inputList) {
            Map<String, Object> input = Maps.newHashMap();
            
            InputConfiguration config = i.getConfiguration();
            
            input.put("type", config.getType());
            input.put("address", config.getAddress());
            input.put("routing_key", config.getRoutingKey());
            input.put("started_at", i.getStartedAt());
            
            inputResult.add(input);
        }
        
        result.put("total", inputList.size());
        result.put("inputs", inputResult);
        
        return result;
    }
    
}
