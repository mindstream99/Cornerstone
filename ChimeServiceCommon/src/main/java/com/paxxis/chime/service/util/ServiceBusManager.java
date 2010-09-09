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


package com.paxxis.chime.service.util;

import com.paxxis.chime.service.IServiceBusController;
import javax.management.Notification;
import org.springframework.jmx.export.notification.NotificationPublisher;
import org.springframework.jmx.export.notification.NotificationPublisherAware;

/**
 * The mechanism for exposing management interfaces for service connectors is based on a pair
 * of collaborating objects that implement IServiceBusController.  The primary
 * management class is com.mindstream.cornerstone.service.common.ServiceBusManagementBean.  
 * <br>
 * This class collaborates with the primary management object in order to expose the
 * management interface to the JMX container using the Spring framework.  This collaboration
 * is done so that the dependency on Spring is isolated.  
 * 
 * 
 * @author Robert Englander
 */
public class ServiceBusManager implements IServiceBusController, NotificationPublisherAware
{
    // used for publishing JMX notifications
    private NotificationPublisher _publisher = null;
    
    // the sequence number for notification messages
    private int _seqNum = 0;

    // the collaborating management object
    private IServiceBusController _collaborator = null;
    
    /** 
     * Constructor
     */
    public ServiceBusManager()
    {
    }
    
    /**
     * Sets the collaborating management object.
     *
     * @param collaborator the collaborating management object.
     */
    public void setCollaborator(IServiceBusController collaborator)
    {
        _collaborator = collaborator;
        
        // tell the collaborator about me
        _collaborator.setCollaborator(this);
    }
    
    /**
     * Set the notification publisher.  This method is called by the Spring
     * framework because this class implements NotificationPublisherAware.
     *
     * @param publisher the notification publisher
     */
    public void setNotificationPublisher(NotificationPublisher publisher)
    {
        _publisher = publisher;
    }
    
    /**
     * Connect to the service bus.
     */
    public void connect()
    {
        _collaborator.connect();
    }
    
    /**
     * Disconnect from the service bus.
     */
    public void disconnect()
    {
        _collaborator.disconnect();
    }

    /**
     * Determine if the connector is connected to the bus
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected()
    {
        return _collaborator.isConnected();
    }

    /**
     * Get the current connection status.
     *
     * @return a textual representation of the current connection status.
     */
    public String getConnectionStatus()
    {
        return _collaborator.getConnectionStatus();
    }
    
    public void notifyConnectionFailed()
    {
        _publisher.sendNotification(new Notification("ServiceBusConnector.Connection", "ServiceBusConnector", ++_seqNum, 
                            "Bus Connection Failed"));
    }
    
    /**
     * Publishes a notification message containing the current connection status.
     */
    public void notifyConnectionStatusChange()
    {
        _publisher.sendNotification(new Notification("ServiceBusConnector.Status", "ServiceBusConnector", ++_seqNum, 
                            _collaborator.getConnectionStatus()));
    }
}
