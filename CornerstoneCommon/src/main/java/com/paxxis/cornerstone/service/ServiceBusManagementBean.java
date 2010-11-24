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

/**
 * Provides management services for an associated Service Bus Connector.
 * <br>
 * This class collaborates with another instance of IServiceBusController,
 * which is responsible for exposing the management interface to a
 * management container.  This technique isolates any dependencies on specific
 * frameworks or APIs that may be used for that purpose, and allows different
 * implementations of the collaborator to be used without impacting this class.
 * 
 * 
 * @author Robert Englander
 */
public class ServiceBusManagementBean implements IServiceBusController
{
    // the connection state strings
    private static final String CONNECTED = "Connected";
    private static final String DISCONNECTED = "Disconnected";
    private static final String DISCONNECTING = "Disconnect Pending";
    private static final String RECONNECTPENDING = "Auto Reconnect Pending";
    
    // the service bus connector being managed
    ServiceBusConnector _connector = null;
    
    // the connection status
    String _status = DISCONNECTED;
    
    // the collaborating management object
    IServiceBusController _collaborator = null;
    
    /** 
     * Constructor
     */
    public ServiceBusManagementBean()
    {
    }
    
    public void initialize()
    {
    }
    
    /**
     * Set the collaborating management object.
     *
     * @param collaborator the collaborating management object.
     */
    public void setCollaborator(IServiceBusController collaborator)
    {
        _collaborator = collaborator;
    }
    
    /**
     * Set the connector object to manage.
     *
     * @param connector the connector to manage
     */
    public void setConnector(ServiceBusConnector connector)
    {
        _connector = connector;
        
        // tell the connector about me
        _connector.setManagementBean(this);
    }
    
    /**
     * Connect to the service bus
     */
    public void connect()
    {
        _connector.connect();
    }
    
    /**
     * Disconnect from the service bus
     */
    public void disconnect()
    {
        _connector.disconnect();
    }
    
    /**
     * Updates the connection status property and notifies the
     * collaborator.
     */
    public void notifyConnectionStatusChange()
    {
        setConnectionStatus(getStatusValue());
        
        if (_collaborator != null)
        {
            _collaborator.notifyConnectionStatusChange();
        }
    }

    public void notifyConnectionFailed()
    {
        setConnectionStatus(getStatusValue());
        
        if (_collaborator != null)
        {
            _collaborator.notifyConnectionFailed();
        }
    }
    
    /**
     * Set the connection status property.
     *
     * @param status the connection status
     */
    public void setConnectionStatus(String status)
    {
        _status = status;
    }
    
    /**
     * Get the connection status.
     */
    public String getConnectionStatus()
    {
        return _status;
    }
    
    /**
     * Determine if the connector is connected to the bus
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected()
    {
        setConnectionStatus(getStatusValue());
        return _connector.isConnected();
    }
    
    /**
     * Derives the connection status text based on the
     * state of the connector being managed.
     */
    String getStatusValue()
    {
        if (_connector.isAutoReconnectPending())
        {
            return RECONNECTPENDING;
        }
        else if (!_connector.isConnected())
        {
            return DISCONNECTED;
        }
        else
        {
            if (_connector.isDisconnectPending())
            {
                return DISCONNECTING;
            }
        }
        
        return CONNECTED;
    }

}
