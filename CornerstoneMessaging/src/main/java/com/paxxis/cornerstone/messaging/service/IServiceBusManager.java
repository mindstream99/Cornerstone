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

/**
 * This interface defines a set of management operations that a
 * service bus management bean exposes to the management container.
 *
 * @author Robert Englander
 */
public interface IServiceBusManager
{
    /**
     * Connect the service to the service bus.
     */
    public void connect();
    
    /**
     * Disconnect the service from the service bus.  This will not simply
     * disconnect from the bus.  It will stop accepting messages
     * and will work off any unprocessed messages that it has already
     * received.  When all messages are processed the service will complete
     * the disconnect.
     */
    public void disconnect();
    
    /**
     * Get the current connection status.
     *
     * @return <b><i>Connected</i></b> if the service is currently accepting messages, <b><i>Disconnect Pending</i></b>
     * if the service has stopped accepting messages and is currently working off unprocessed messages,
     * and <b><i>Disconnected</i></b> if the service is not connected to the service bus.
     */
    public String getConnectionStatus();
    
    /**
     * Determine if the connector is connected to the bus
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected();    
}
