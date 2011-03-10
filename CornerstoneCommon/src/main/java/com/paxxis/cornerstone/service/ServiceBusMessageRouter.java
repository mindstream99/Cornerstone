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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.paxxis.cornerstone.base.MessageGroup;
import com.paxxis.cornerstone.base.MessagingConstants.PayloadType;
import com.paxxis.cornerstone.common.JSonObjectPayload;
import com.paxxis.cornerstone.common.JavaObjectPayload;

/**
 * 
 * @author Matthew Pflueger
 */
public class ServiceBusMessageRouter extends ServiceBusMessageHandler {
    private static final Logger logger = Logger.getLogger(ServiceBusMessageRouter.class);

    private MessageProcessorFactory messageProcessorFactory;
    private MessageGroup messageGroup = null;
    private boolean failOnOverride = true;
    
    private Map<MessageGroup.TypeVersionTuple, String> messageProcessorMap;
    
    
    public void initialize() {
        messageProcessorMap = new HashMap<MessageGroup.TypeVersionTuple, String>();
        
        for (String name: messageGroup.getMessageProcessorNames()) {
            MessageProcessor<?,?> messageProcessor = 
                this.messageProcessorFactory.getMessageProcessor(name);
            MessageGroup.TypeVersionTuple tvt = new MessageGroup.TypeVersionTuple(
                    messageProcessor.getRequestMessageType(), messageProcessor.getRequestMessageVersion());
            String old = messageProcessorMap.put(tvt, name);
            if (old != null) {
	            String msg = name  + " overrides "  + old  + " for message "  + tvt; 
                if (this.failOnOverride) {
	                throw new IllegalStateException(msg);
                }
                logger.info(msg);
            }
        }

        messageGroup.validate(messageProcessorMap);
    }
    
    @Override
    public MessageProcessor<?, ?> getProcessor(int type, int version, int payloadType) {
        
        MessageGroup.TypeVersionTuple tvt = new MessageGroup.TypeVersionTuple(type, version);
        String messageProcessorName = this.messageProcessorMap.get(tvt);
        
        if (messageProcessorName == null) {
            logger.error("No processor found for message " + tvt);
            return null;
        }
        
        MessageProcessor<?, ?> processor = messageProcessorFactory.getMessageProcessor(messageProcessorName);
		if (payloadType == PayloadType.JavaObjectPayload.getValue()) {
			processor.setPayloadType(new JavaObjectPayload());
		} else if (payloadType == PayloadType.JsonObjectPayload.getValue()) {
			processor.setPayloadType(new JSonObjectPayload(new ObjectMapper(), processor.getRequestMessageClass()));
		} else {
			logger.error("Invalid Payload type detected: " + payloadType);
			return null;
		}

		if (processor instanceof MultiRequestProcessor) {
		    //it is expected that this type of request processor encapsulates multiple requests in
		    //one message and it will be calling back to the router to determine the processors to handle
		    //the inner requests - unfortunately this means we could have a nice infinite loop too...
		    ((MultiRequestProcessor) processor).setServiceBusMessageHandler(this);
		}
		return processor;
    }

    public void setMessageProcessorFactory(MessageProcessorFactory messageProcessorFactory) {
        this.messageProcessorFactory = messageProcessorFactory;
    }

    public void setMessageGroup(MessageGroup group) {
    	messageGroup = group;
    }
    
    public MessageGroup getMessageGroup() {
    	return messageGroup;
    }
    
}
