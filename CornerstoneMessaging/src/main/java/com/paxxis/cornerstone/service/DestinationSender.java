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

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;

import org.apache.log4j.Logger;

import com.paxxis.cornerstone.base.ErrorMessage;
import com.paxxis.cornerstone.base.MessageGroup;
import com.paxxis.cornerstone.base.MessagingConstants;
import com.paxxis.cornerstone.base.RequestMessage;
import com.paxxis.cornerstone.common.CornerstoneConfigurable;
import com.paxxis.cornerstone.common.MessagePayload;

/**
 * 
 * @author Robert Englander
 * 
 */
public class DestinationSender extends CornerstoneConfigurable 
        implements IServiceBusConnectorClient, DestinationPublisher {
    
    private static final Logger logger = Logger.getLogger(RequestQueueSender.class);

    // the message sender
    private MessageProducer messageSender = null;

    protected MessageProducer getMessageSender() {
		return messageSender;
	}

	// the name of the destination
    private String destinationName = null;

    // the optional replyto destination name
    private String replyToName = null;
    
    // the replyto destination
    //private Destination replyTo = null;
    
    // the service bus connector
    private ServiceBusConnector connector = null;

    private int deliveryMode = DeliveryMode.NON_PERSISTENT;
    private MessageGroup messageGroup = null;
    private int timeToLive = 0;
    
    protected ServiceBusConnector getConnector() {
		return connector;
	}

	// teardown pending flag
    private boolean teardownPending = false;

    public DestinationSender() {
    }

    public void setDestinationName(String name) {
        destinationName = name;
    }

    protected String getDestinationName() {
        return destinationName;
    }

    public void setReplyToName(String name) {
        replyToName = name;
    }

    protected String getReplyToName() {
        return replyToName;
    }

    public void setPersistentDelivery(boolean persistent) {
    	deliveryMode = (persistent ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT);
    }

    public boolean isPersistentDelivery() {
    	return deliveryMode == DeliveryMode.PERSISTENT;
    }

    public void setMessageGroup(MessageGroup group) {
    	messageGroup = group;
    }
    
    public MessageGroup getMessageGroup() {
    	return messageGroup;
    }
    
    public void setTimeToLive(int ttl) {
    	timeToLive = ttl;
    }
    
    public int getTimeToLive() {
    	return timeToLive;
    }
    
    public void setServiceBusConnector(ServiceBusConnector connector) {
        this.connector = connector;
    }

    /**
     * Is a teardown pending?
     *
     * @return true if a disconnect has been initiated and is not
     * yet complete, false otherwise.
     */
    public boolean isTeardownPending() {
        return teardownPending;
    }

    /**
     * Close the JMS session objects
     */
    protected void closeDown() throws JMSException {
        messageSender.close();
        messageSender = null;
    }

    public void halt() {
    }

    /**
     * Setup the JMS specific objects.<br><br>
     * This method is called by the ServiceBusConnector as part of its
     * connection process.  It should not be called directly by users...
     *
     * @throws RuntimeException if the setup could not be completed
     */
    public void setup() {
        try {
            messageSender = connector.createMessageProducer(destinationName);
            messageSender.setDeliveryMode(deliveryMode);
            messageSender.setTimeToLive(timeToLive);
        } catch(Throwable t) {
            logger.error(t);
            try {
                closeDown();
            } catch (JMSException e) {
                // is there any need to include this in the runtime exception below?
            }

            throw new RuntimeException(t);
        }
   }

    /**
     * Tear down the sender.
     */
    public void tearDown(final ShutdownListener listener) {
        // don't do this if we're already in the process of tearing down
        if (!isTeardownPending()) {
            teardownPending = true;

            try {
                closeDown();
                teardownPending = false;
                listener.onShutdownComplete();
            } catch (Exception e) {
                logger.error(e);
                throw new RuntimeException(e);
            }
        }
    }

    /* (non-Javadoc)
     * @see com.paxxis.cornerstone.service.DestinationPublisher#publish(REQ, com.paxxis.cornerstone.common.MessagePayload)
     */
    @Override
    public synchronized <REQ extends RequestMessage> void publish(
            REQ msg,
			MessagePayload payloadType) {

    	try {
            Message message = prepareMessage(msg, payloadType);
            messageSender.send(message, deliveryMode, 0, timeToLive);
        } catch (JMSException je) {
            logger.error(je);
            ErrorMessage errorMsg = new ErrorMessage();
            errorMsg.setMessage("Unable to publish message. " + je.getMessage());
            throw new SendException(errorMsg);
        }
    }

    /* (non-Javadoc)
     * @see com.paxxis.cornerstone.service.DestinationPublisher#publish(REQ, com.paxxis.cornerstone.common.MessagePayload)
     */
    @Override
    public synchronized <REQ extends RequestMessage> void publish(
    		String destinationName,
            REQ msg,
			MessagePayload payloadType) {

    	try {
            Message message = prepareMessage(msg, payloadType);
            if (destinationName != null) {
                MessageProducer sender = connector.createMessageProducer(destinationName);
                sender.setDeliveryMode(deliveryMode);
                sender.setTimeToLive(timeToLive);
                sender.send(message, deliveryMode, 0, timeToLive);
                sender.close();
            } else {
                messageSender.send(message, deliveryMode, 0, timeToLive);
            }
        } catch (JMSException je) {
            logger.error(je);
            ErrorMessage errorMsg = new ErrorMessage();
            errorMsg.setMessage("Unable to publish message. " + je.getMessage());
            throw new SendException(errorMsg);
        }
    }

    /**
     * Prepare a message for sending.
     *
     * @param requester the service requester
     */
    protected Message prepareMessage(
            RequestMessage requestMessage,
            MessagePayload payloadType) 
            throws JMSException {

        requestMessage.setRequestSentOn(System.currentTimeMillis());
        
		Message message = payloadType.createMessage(connector.getSession(), requestMessage);
		message.setIntProperty(MessagingConstants.HeaderConstant.GroupId.name(), messageGroup.getId());
		message.setIntProperty(MessagingConstants.HeaderConstant.GroupVersion.name(), messageGroup.getVersion());
        if (replyToName != null) {
        	message.setStringProperty(MessagingConstants.HeaderConstant.ReplyToName.name(), replyToName);
        }
        
        String jmsxGroup = requestMessage.getJmsxGroupID();
        if (jmsxGroup != null) {
            message.setStringProperty("JMSXGroupID", jmsxGroup);
        }
		
		return message;
    }
}
