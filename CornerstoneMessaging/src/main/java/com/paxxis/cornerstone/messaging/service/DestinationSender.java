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

import com.paxxis.cornerstone.base.ErrorMessage;
import com.paxxis.cornerstone.base.MessageGroup;
import com.paxxis.cornerstone.base.MessagingConstants;
import com.paxxis.cornerstone.base.RequestMessage;
import com.paxxis.cornerstone.common.CornerstoneConfigurable;
import com.paxxis.cornerstone.messaging.common.Message;
import com.paxxis.cornerstone.messaging.common.MessageProducer;
import com.paxxis.cornerstone.service.ShutdownListener;
import org.apache.log4j.Logger;


/**
 * 
 * @author Robert Englander
 */
public class DestinationSender extends CornerstoneConfigurable 
        implements IServiceBusConnectorClient, DestinationPublisher {
    
    private static final Logger logger = Logger.getLogger(DestinationSender.class);

    protected MessagingConstants.PayloadType payloadType;

    // the message sender
    private MessageProducer messageSender = null;

	// the name of the destination
    private String destinationName = null;

    // the optional replyto destination name
    private String replyToName = null;
    
    // the service bus connector
    private ServiceBusConnector connector = null;

    private boolean persistent = false;
    private MessageGroup messageGroup = null;
    private int timeToLive = 0;

    // teardown pending flag
    private boolean teardownPending = false;


    protected ServiceBusConnector getConnector() {
		return connector;
	}


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
        this.persistent = persistent;
    }

    public boolean isPersistentDelivery() {
        return this.persistent;
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

    protected MessageProducer getMessageSender() {
        return messageSender;
    }

    public void setPayloadType(MessagingConstants.PayloadType payloadType) {
        this.payloadType = payloadType;
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
    protected void closeDown() {
        messageSender.close();
        messageSender = null;
    }

    public void halt() {
    }

    /**
     * Setup the MQ specific objects.<br><br>
     * This method is called by the ServiceBusConnector as part of its
     * connection process.  It should not be called directly by users...
     *
     * @throws RuntimeException if the setup could not be completed
     */
    public void setup() {
        try {
            messageSender = connector.getSession().createProducer(destinationName);
            messageSender.setPersistent(this.persistent);
            messageSender.setTimeToLive(timeToLive);
        } catch(Throwable t) {
            logger.error(t);
            try {
                closeDown();
            } catch (Exception e) {
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
    public synchronized <REQ extends RequestMessage> void publish(REQ msg) {

    	try {
            Message message = prepareMessage(msg, payloadType);
            messageSender.setPersistent(persistent).setTimeToLive(timeToLive).setPriority(0).send(message);
        } catch (Exception je) {
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
    public synchronized <REQ extends RequestMessage> void publish(String destinationName, REQ msg) {

    	try {
            Message message = prepareMessage(msg, payloadType);
            if (destinationName != null) {
                MessageProducer sender = connector.getSession().createProducer(destinationName);
                sender.setPersistent(persistent).setTimeToLive(timeToLive).setPriority(0).send(message);
                sender.close();
            } else {
                messageSender.setPersistent(persistent).setTimeToLive(timeToLive).setPriority(0).send(message);
            }
        } catch (Exception je) {
            logger.error(je);
            ErrorMessage errorMsg = new ErrorMessage();
            errorMsg.setMessage("Unable to publish message. " + je.getMessage());
            throw new SendException(errorMsg);
        }
    }

    /**
     * Prepare a message for sending.
     */
    protected Message prepareMessage(
            RequestMessage requestMessage,
            MessagingConstants.PayloadType payloadType) {

    	if (this.messageSender == null) {
    		this.setup();
    	}
    	
        requestMessage.setRequestSentOn(System.currentTimeMillis());

        Message message = connector.getSession().createMessage(requestMessage, payloadType);
        message.setProperty(MessagingConstants.HeaderConstant.GroupId.name(), messageGroup.getId());
        message.setProperty(MessagingConstants.HeaderConstant.GroupVersion.name(), messageGroup.getVersion());
        if (replyToName != null) {
        	message.setProperty(MessagingConstants.HeaderConstant.ReplyToName.name(), replyToName);
        }
        
        String jmsxGroup = requestMessage.getJmsxGroupID();
        if (jmsxGroup != null) {
            message.setProperty("JMSXGroupID", jmsxGroup);
        }
		
		return message;
    }
}
