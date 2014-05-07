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

package com.paxxis.cornerstone.messaging.service.jms;

import com.paxxis.cornerstone.messaging.common.Destination;
import com.paxxis.cornerstone.messaging.common.Session;
import com.paxxis.cornerstone.messaging.common.jms.JMSDestination;
import com.paxxis.cornerstone.messaging.service.ServiceBusMessageReceiver;
import org.apache.activemq.command.ActiveMQQueue;


/**
 * Manages a message consumer on a service bus message destination.
 *
 * @author Robert Englander
 */
public class JMSServiceBusMessageReceiver extends ServiceBusMessageReceiver {

    /**
     * Setup the JMS specific objects.<br><br>
     * This method is called by the ServiceBusConnector as part of its
     * connection process.  It should not be called directly by users
     * of ServiceBusMessageReceiver instances.
     * 
     * 
     * 
     * @throws RuntimeException if the setup could not be completed
     */
    public void setup()
    {
        try
        {
            // initialize the message handler
            _messageHandler.setMaxMessagesInFlight(maxMessagesInFlight);
            _messageHandler.init(
                    _destinationName,
                    _connector.getSession(), 
                    _connector.getAcknowledgeMode() == Session.CLIENT_ACKNOWLEDGE);

            // create a message consumer and start the connection so that messages
            // can be delivered
            if (exclusive) {
                _destination = new JMSDestination(new ActiveMQQueue(_destinationName + "?consumer.exclusive=true"));
            } else {
                // lookup the destination
                _destination = (Destination)_connector.getInitialContextFactory().createInitialContext().lookup(_destinationName);
            }

            _consumer = _connector.getSession().createConsumer(_destination, _selector, _messageHandler);
        } catch(Throwable t) {
            try {
                closeDown();
            }
            catch (RuntimeException e) { }

            throw new RuntimeException(t);
        }
    }


}
