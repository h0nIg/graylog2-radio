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
package org.graylog2.radio.rest.json.requests;

import com.google.gson.annotations.SerializedName;
import java.net.InetSocketAddress;
import org.graylog2.radio.inputs.InputType;

/**
 * @author Lennart Koopmann <lennart@torch.sh>
 */
public class CreateInputRequest {
    
    @SerializedName("listen_address") public String listenAddress;
    @SerializedName("listen_port") public int listenPort;
    @SerializedName("input_type") public String inputType;
    @SerializedName("routing_key") public String routingKey;
    
    public InetSocketAddress getAddress() {
        return new InetSocketAddress(listenAddress, listenPort);
    }
    
    public InputType getInputType() {
        return InputType.valueOf(inputType);
    }
    
}
