/*
 * Copyright 2010 the original author or authors.
 * Copyright 2009 Paxxis Technology LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paxxis.cornerstone.messaging.service.amqp;

import java.util.HashMap;
import java.util.Map;

import com.paxxis.cornerstone.messaging.common.Session;
import com.paxxis.cornerstone.messaging.common.amqp.AMQPDestination;
import com.paxxis.cornerstone.messaging.common.amqp.AMQPSession;
import com.paxxis.cornerstone.messaging.service.ServiceBusMessageReceiver;


/**
 * Manages a message consumer on a service bus message destination.
 *
 * @author Matthew Pflueger
 */
public class AMQPServiceBusMessageReceiver extends ServiceBusMessageReceiver {

    /**
     * Setup the MS specific objects.<br><br>
     * This method is called by the ServiceBusConnector as part of its
     * connection process.  It should not be called directly by users
     * of ServiceBusMessageReceiver instances.
     *
     * @throws RuntimeException if the setup could not be completed
     */
    public void setup() {
        try {
            _messageHandler.setMaxMessagesInFlight(maxMessagesInFlight);
            _messageHandler.init(
                    _destinationName,
                    _connector.getSession(),
                    _connector.getAcknowledgeMode() == Session.CLIENT_ACKNOWLEDGE);

            Map<String, Object> args = new HashMap<String, Object>();
            args.put("exclusive", exclusive);

            _destination = new AMQPDestination(_destinationName, ((AMQPSession) _connector.getSession()).getChannel());
            _consumer = _connector.getSession().createConsumer(_destination, _selector, _messageHandler, args);
        } catch(Throwable t) {
            closeDown();
            throw new RuntimeException(t);
        }
    }


}
