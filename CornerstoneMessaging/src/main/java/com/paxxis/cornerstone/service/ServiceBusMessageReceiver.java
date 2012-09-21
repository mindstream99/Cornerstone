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

package com.paxxis.cornerstone.service;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.activemq.command.ActiveMQQueue;

import com.paxxis.cornerstone.base.MessageGroup;
import com.paxxis.cornerstone.common.CornerstoneConfigurable;


/**
 * Manages a message consumer on a service bus message destination.
 *
 * @author Robert Englander
 */
public class ServiceBusMessageReceiver extends CornerstoneConfigurable implements IServiceBusConnectorClient
{
    // JMS objects
    private MessageConsumer _consumer = null;
    private Destination _destination = null;

    // the name of the destination
    private String _destinationName = "";
    
    // the message handler
    private ServiceBusMessageHandler _messageHandler = null;
    
    // the message selector
    private String _selector = null;

    // exclusive flag
    private boolean exclusive = false;

    // the request queue connector
    private ServiceBusConnector _connector = null;

    // teardown pending flag
    boolean _teardownPending = false;
    
    /**
     * Creates a new instance of ServiceBusMessageReceiver
     */
    public ServiceBusMessageReceiver()
    {
    }

    public void initialize() {
    	if (_messageHandler instanceof ServiceBusMessageRouter) {
    		ServiceBusMessageRouter router = (ServiceBusMessageRouter)_messageHandler;
        	MessageGroup group = router.getMessageGroup();
        	setMessageSelector(group.getMessageSelector());
    	}
    }
    
    public void setExclusive(boolean val) {
        exclusive = val;
    }
    
    /**
     * Set the name of the destination
     *
     * @param name the name of the destination
     */
    public void setDestinationName(String name)
    {
        _destinationName = name;
    }
    
    /**
     * Get the name of the destination
     *
     * @return the name of the destination
     */
    protected String getDestinationName()
    {
        return _destinationName;
    }
    
    /**
     * Set the request queue connector.
     *
     * @param connector the connector
     */
    public void setServiceBusConnector(ServiceBusConnector connector)
    {
        _connector = connector;
    }
    
    /**
     * Set the message handler
     *
     * @param handler the message handler
     */
    public void setMessageHandler(ServiceBusMessageHandler handler)
    {
        _messageHandler = handler;
    }
    
    /**
     * Set the message selector.  See the JMS spec for selector syntax.
     *
     * @param selector the message selector.
     */
    public void setMessageSelector(String selector)
    {
        _selector = selector;
    }
    
    /**
     * Is a teardown pending?
     *
     * @return true if a teardown has been initiated and is not
     * yet complete, false otherwise.
     */
    public boolean isTeardownPending()
    {
        return _teardownPending;
    }

    /**
     * Tear down the receiver.
     */
    public void tearDown(final ShutdownListener listener)
    {
        // don't do this if we're already in the process of tearing down
        if (!isTeardownPending())
        {
            _teardownPending = true;
            
            try
            {
                // stop the message consumer so that we don't
                // get any more messages from the broker
                closeDown();

                // shut down the message handler, which will work off all remaining
                // requests before fully terminating.  the listener
                // will be notified when the shutdown is complete
                _messageHandler.shutdown(
                    new ShutdownListener()
                    {
                        public void onShutdownComplete()
                        {
                            _teardownPending = false;
                            listener.onShutdownComplete();
                        }
                    }
                );
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    public void halt()
    {
        if (_messageHandler != null)
        {
            _messageHandler.halt();
        }
    }
    
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
            _messageHandler.init(
                    _destinationName,
                    _connector.getSession(), 
                    _connector.getAcknowledgeMode() == Session.CLIENT_ACKNOWLEDGE);

            // create a message consumer and start the connection so that messages
            // can be delivered
            if (exclusive) {
                _destination = new ActiveMQQueue(_destinationName + "?consumer.exclusive=true");
            } else {
                // lookup the destination
                _destination = (Destination)_connector.getInitialContextFactory().createInitialContext().lookup(_destinationName);
            }

            _consumer = _connector.createConsumer(_destination, _selector);
            _consumer.setMessageListener(_messageHandler);
        }
        catch(Throwable t)
        {
            try
            {
                closeDown();
            }
            catch (JMSException e)
            {
                // TODO is there any need to include this in the runtime exception below?
            }

            throw new RuntimeException(t);
        }
    }

    /**
     * Close down the JMS objects.
     */
    private void closeDown() throws JMSException
    {
        if (_consumer != null)
        {
            _consumer.close();
            _consumer = null;
            
            _destination = null;
        }
    }
    
}
