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


import com.paxxis.cornerstone.base.MessageGroup;
import com.paxxis.cornerstone.base.MessagingConstants;
import com.paxxis.cornerstone.common.CornerstoneConfigurable;

import com.paxxis.cornerstone.messaging.common.Message;
import com.paxxis.cornerstone.messaging.common.MessagePayload;
import com.paxxis.cornerstone.messaging.common.Destination;
import com.paxxis.cornerstone.messaging.common.MessageProducer;

import com.paxxis.cornerstone.service.ShutdownListener;

/**
 * Manages a connection to a service notification topic as a message
 * sender (producer).
 *
 * @author Robert Englander
 */
public class NotificationTopicSender extends CornerstoneConfigurable implements IServiceBusConnectorClient
{
    // the message sender for sending notifications
    MessageProducer _notificationSender = null;

    // the name of the notification topic
    private String _notificationTopicName = null;

    // the service bus connector
    ServiceBusConnector _connector = null;

    // teardown pending flag
    boolean _teardownPending = false;

    private MessageGroup messageGroup = null;

    /**
     * Constructor
     */
    public NotificationTopicSender()
    {
    }

    public ServiceBusConnector getConnector() {
        return _connector;
    }

    /**
     * This is an empty implementation.  Subclass implementations should override
     * this method in order to use a cornerstoneConfiguration object to retrieve and
     * set property values.
     */
    public void loadConfigurationPropertyValues()
    {
    }

    /**
     * Set the name of the notification topic
     *
     * @param name the name of the notification topic
     */
    public void setNotificationTopicName(String name)
    {
        _notificationTopicName = name;
    }

    /**
     * Get the name of the notification topic
     *
     * @return the name of the notification topic
     */
    protected String getNotificationTopicName()
    {
        return _notificationTopicName;
    }

    public void setMessageGroup(MessageGroup group) {
    	messageGroup = group;
    }

    public MessageGroup getMessageGroup() {
    	return messageGroup;
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
     * Is a teardown pending?
     *
     * @return true if a disconnect has been initiated and is not
     * yet complete, false otherwise.
     */
    public boolean isTeardownPending()
    {
        return _teardownPending;
    }

    /**
     * Close the JMS session objects
     */
    protected void closeDown()
    {
        _notificationSender.close();
        _notificationSender = null;
    }

    public void halt()
    {

    }

    /**
     * Setup the JMS specific objects.<br><br>
     * This method is called by the ServiceBusConnector as part of its
     * connection process.  It should not be called directly.
     *
     * @throws RuntimeException if the setup could not be completed
     */
    public void setup()
    {
        try {
            _notificationSender = _connector.getSession().createProducer(_notificationTopicName);
            _notificationSender.setPersistent(false);
        } catch (Exception je) {
            throw new RuntimeException(je);
        }
   }

    /**
     * Tear down the sender.
     */
    public void tearDown(final ShutdownListener listener)
    {
        // don't do this if we're already in the process of tearing down
        if (!isTeardownPending())
        {
            _teardownPending = true;

            try
            {
                closeDown();

                _teardownPending = false;
                listener.onShutdownComplete();
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Send a notification message
     *
     * @param msg the message
     * @param payloadType the payload
     */
    public synchronized void send(
            com.paxxis.cornerstone.base.Message msg,
            MessagePayload payloadType) {
        send(msg, payloadType, 0);
    }

    /**
     * Send a notification message
     *
     * @param msg the message
     * @param payloadType the payload
     * @param ttl time to live
     *
     */
    public synchronized void send(
            com.paxxis.cornerstone.base.Message msg,
            MessagePayload payloadType,
            long ttl) {

		Message message = payloadType.createMessage(msg);
		message.setProperty(MessagingConstants.HeaderConstant.GroupId.name(), messageGroup.getId());
		message.setProperty(MessagingConstants.HeaderConstant.GroupVersion.name(), messageGroup.getVersion());

        // send the message to the service notification topic
        _notificationSender.setTimeToLive(ttl).send(message);
    }

}
