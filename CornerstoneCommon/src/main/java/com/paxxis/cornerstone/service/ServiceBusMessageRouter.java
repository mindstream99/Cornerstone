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
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

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
    //private MessagePayloadFactory messagePayloadFactory;
    
    private List<String> messageProcessorNames;
    private boolean failOnOverride = true;
    
    private Map<TypeVersionTuple, String> messageProcessorMap;
    
    
    public void initialize() {
        messageProcessorMap = new HashMap<TypeVersionTuple, String>();
        
        for (String name: this.messageProcessorNames) {
            MessageProcessor<?,?> messageProcessor = 
                this.messageProcessorFactory.getMessageProcessor(name);
            TypeVersionTuple tvt = new TypeVersionTuple(
                    messageProcessor.getMessageType(), messageProcessor.getMessageVersion());
            String old = messageProcessorMap.put(tvt, name);
            if (old != null) {
	            String msg = name  + " overrides "  + old  + " for message "  + tvt; 
                if (this.failOnOverride) {
	                throw new IllegalStateException(msg);
                }
                logger.info(msg);
            }
        }
    }
    
    @Override
    protected MessageProcessor<?, ?> getProcessor(int type, int version, int payloadType) {
        
        TypeVersionTuple tvt = new TypeVersionTuple(type, version);
        String messageProcessorName = this.messageProcessorMap.get(tvt);
        
        if (messageProcessorName == null) {
            logger.error("No processor found for message " + tvt);
            return null;
        }
        
        MessageProcessor processor = messageProcessorFactory.getMessageProcessor(messageProcessorName);
		if (payloadType == PayloadType.JavaObjectPayload.getValue()) {
			processor.setPayloadType(new JavaObjectPayload());
		} else if (payloadType == PayloadType.JsonObjectPayload.getValue()) {
			processor.setPayloadType(new JSonObjectPayload(new ObjectMapper(), processor.getRequestMessageClass()));
		} else {
			logger.error("Invalid Payload type detected: " + payloadType);
			return null;
		}

		return processor;
    }

    public void setMessageProcessorNames(List<String> messageProcessorNames) {
        this.messageProcessorNames = messageProcessorNames;
    }

    public void setMessageProcessorFactory(MessageProcessorFactory messageProcessorFactory) {
        this.messageProcessorFactory = messageProcessorFactory;
    }

    private static class TypeVersionTuple {
        
        private Integer type;
        private Integer version;
        
        public TypeVersionTuple(Integer type, Integer version) {
            if (type == null || version == null) {
                throw new NullPointerException("type/version cannot be null");
            }
            this.type = type;
            this.version = version;
        }
        
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof TypeVersionTuple)) {
                return false;
            }
            
            TypeVersionTuple e = (TypeVersionTuple) o;
            
            Integer t1 = getType();
            Integer t2 = e.getType();
            if (t1 == null) {
                if (t2 != null) {
	                return false;
                }
            } else if (!t1.equals(t2)) {
                return false;
            }
            
            Integer v1 = getVersion();
            Integer v2 = e.getVersion();
            if (v1 == null) {
                if (v2 != null) {
	                return false;
                }
            } else if (!v1.equals(v2)) {
                return false;
            }
            
            return true;
        }

        public int hashCode() {
            return (this.type == null ? 0 : this.type.hashCode()) ^
                   (this.version == null ? 0 : this.version.hashCode());
        }

        public String toString() {
            return "type " + getType() + ", version " + getVersion();
        }
        
        public Integer getType() {
            return this.type;
        }
        
        public Integer getVersion() {
            return this.version;
        }
    }
    
}
