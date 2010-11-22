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

package com.paxxis.chime.service;

import java.io.Serializable;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Topic;

import com.paxxis.chime.client.common.MessagingConstants;
import com.paxxis.chime.common.MessagePayload;

/**
 * Manages a connection to a service notification topic as a message
 * sender (producer).
 *
 * @author Robert Englander
 */
public class NotificationTopicSender extends ChimeConfigurable implements IServiceBusConnectorClient
{
    // the notification topic
    private Topic _topic = null;

    // the message sender for sending notifications
    MessageProducer _notificationSender = null;

    // the name of the notification topic
    private String _notificationTopicName = null;

    // the service bus connector
    ServiceBusConnector _connector = null;

    // teardown pending flag
    boolean _teardownPending = false;

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
    protected void closeDown() throws JMSException
    {
        _notificationSender.close();
        _notificationSender = null;

        _topic = null;
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
        try
        {
            // lookup the notification topic
            _topic = (Topic)_connector.getInitialContextFactory().createInitialContext().lookup(_notificationTopicName);

            _notificationSender = _connector.createMessageProducer(_topic);
            _notificationSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        }
        catch (Exception je)
        {
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

    public synchronized void send(ServiceBusMessageProducer notifier, MessagePayload payloadType) throws JMSException {
        send(notifier, payloadType, 0);
    }

    /**
     * Send a notification message
     *
     * @param notifier the service notifier
     *
     */
    public synchronized void send(ServiceBusMessageProducer notifier, MessagePayload payloadType, long ttl) throws JMSException {
        javax.jms.Message message = payloadType.createMessage(_connector.getSession());

        com.paxxis.chime.client.common.Message msg = notifier.getMessage();
        message.setIntProperty(MessagingConstants.HeaderConstant.MessageType.name(), msg.getMessageType());
        message.setIntProperty(MessagingConstants.HeaderConstant.MessageVersion.name(), msg.getMessageVersion());
        message.setIntProperty(MessagingConstants.HeaderConstant.PayloadType.name(), payloadType.getType().getValue());

        // only supporting java objects right now
        Object payload = msg.getAsPayload(payloadType.getType());
        ((ObjectMessage)message).setObject((Serializable)payload);

        long cid = notifier.getNewCorrelationId();
        message.setJMSCorrelationID(String.valueOf(cid));

        // send the message to the service notification topic
        _notificationSender.send(message, Message.DEFAULT_DELIVERY_MODE, Message.DEFAULT_PRIORITY, ttl);
    }

}
