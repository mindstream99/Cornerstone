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

import java.util.concurrent.RejectedExecutionException;


import com.paxxis.cornerstone.messaging.common.Message;
import com.paxxis.cornerstone.messaging.common.MessageListener;
import com.paxxis.cornerstone.messaging.common.Session;
import org.apache.log4j.Logger;

import com.paxxis.cornerstone.base.MessagingConstants;
import com.paxxis.cornerstone.common.CornerstoneConfigurable;

/**
 *
 * @author Robert Englander
 */
public abstract class SimpleServiceBusMessageHandler extends CornerstoneConfigurable implements MessageListener {
	private static final Logger LOGGER = Logger.getLogger(SimpleServiceBusMessageHandler.class);
	
    // the session to use for publishing responses
    protected Session _session = null;
    private boolean clientAck = false;


    protected MessagingConstants.PayloadType payloadType;
    
    public SimpleServiceBusMessageHandler() {
    }

    public abstract SimpleMessageProcessor<?, ?> getProcessor(int type, int version, int payloadType);

    /**
     * Process a message.
     *
     * @param msg the message that contains the request.
     */
    public void onMessage(Message msg) {
        SimpleMessageProcessor<?, ?> processor = buildProcessor(msg);
        processor.setClientAck(isClientAck());
        processor.init(_session, msg);
        submit(processor);
    }

    public com.paxxis.cornerstone.base.Message processMessage(Message msg) {
        SimpleMessageProcessor<?, ?> processor = buildProcessor(msg);
        processor.init(_session, msg);
        com.paxxis.cornerstone.base.Message result = processor.execute(false);

        if (isClientAck()) {
        	try {
            	msg.acknowledge();
        	} catch (Exception e) {
        		LOGGER.error(e);
        	}
        }
        
        return result;
    }

    public boolean isClientAck() {
        return clientAck;
    }

    private SimpleMessageProcessor<?, ?> buildProcessor(Message msg) {
        int type = getMessageIntProperty(msg, MessagingConstants.HeaderConstant.MessageType.name());
        int version = getMessageIntProperty(msg, MessagingConstants.HeaderConstant.MessageVersion.name());
        int payloadType = getMessageIntProperty(msg, MessagingConstants.HeaderConstant.PayloadType.name());

        SimpleMessageProcessor<?, ?> processor = getProcessor(type, version, payloadType);
        return processor;
    }
    
    private int getMessageIntProperty(Message message, String propertyName) {
        Object val = null;
        int propertyValue = -1;
        try {
            val = message.getProperty(propertyName);
            if (val instanceof Integer) {
                propertyValue = (Integer) val;
            } else {
                propertyValue = Integer.valueOf(val.toString());
            }
        } catch (Exception ex) {
            LOGGER.error("Invalid or missing integer property " + propertyName, ex);
        }
        return propertyValue;
    }

    /**
     * Initialize the request processor.
     *
     * @param session the JMS session to use for publishing responses.
     *
     */
    public void init(Session session, boolean clientAck) {
        _session = session;
        this.clientAck = clientAck;
    }

    public void halt() {
    }

    /**
     * Get the Session instance.
     */
    protected Session getSession() {
        return _session;
    }

    /**
     * Submit a request for processing.  The request is queued up
     * and will be processed asynchronously.
     *
     * @param request the request to be processed
     *
     * @throws java.util.concurrent.RejectedExecutionException if the message handler
     * can not accept requests.
     *
     */
    protected void submit(Runnable request) throws RejectedExecutionException {
        request.run();
    }

//    public MessagingConstants.PayloadType getPayloadType() {
//        return payloadType;
//    }
//
//    public void setPayloadType(MessagingConstants.PayloadType payloadType) {
//        this.payloadType = payloadType;
//    }
}
