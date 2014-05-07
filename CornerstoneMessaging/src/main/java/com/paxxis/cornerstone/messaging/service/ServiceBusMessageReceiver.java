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

package com.paxxis.cornerstone.messaging.service;

import com.paxxis.cornerstone.messaging.common.Destination;
import com.paxxis.cornerstone.messaging.common.MessageConsumer;
import com.paxxis.cornerstone.messaging.common.Session;
import com.paxxis.cornerstone.service.ShutdownListener;

import com.paxxis.cornerstone.base.MessageGroup;
import com.paxxis.cornerstone.common.CornerstoneConfigurable;


/**
 * Manages a message consumer on a service bus message destination.
 *
 * @author Robert Englander
 */
public abstract class ServiceBusMessageReceiver extends CornerstoneConfigurable implements IServiceBusConnectorClient
{
    protected MessageConsumer _consumer = null;
    protected Destination _destination = null;

    // the name of the destination
    protected String _destinationName = "";
    
    // the message handler
    protected ServiceBusMessageHandler _messageHandler = null;
    
    // the message selector
    protected String _selector = null;

    // exclusive flag
    protected boolean exclusive = false;

    // the request queue connector
    protected ServiceBusConnector _connector = null;

    // teardown pending flag
    boolean _teardownPending = false;
    
    // max messages in flight
    protected int maxMessagesInFlight = 10000;
    
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
    
    public void setMaxMessagesInFlight(int value) {
        this.maxMessagesInFlight = value;
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
     * This method is called by the ServiceBusConnector as part of its
     * connection process.  It should not be called directly by users
     * of ServiceBusMessageReceiver instances.
     *
     * @throws RuntimeException if the setup could not be completed
     */
    public abstract void setup();

    /**
     * Close down the MS objects.
     */
    protected void closeDown()
    {
        if (_consumer != null)
        {
            _consumer.close();
            _consumer = null;
            _destination = null;
        }
    }
    
}
