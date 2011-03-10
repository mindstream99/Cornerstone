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

package com.paxxis.cornerstone.base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Robert Englander
 *
 */
public abstract class MessageGroup implements Serializable {

	private static final long serialVersionUID = 2L;

	private List<Message> registeredMessages = new ArrayList<Message>();
    private List<String> messageProcessorNames = null;

	protected MessageGroup() {
	}
	
	public abstract int getId();
	public abstract int getVersion();
	
	public void initialize() {
		// by convention, the version should be reflected in the class name.
		// it would be better to have this caught using automation at build time,
		// but until that happens this will have to do.
		String name = this.getClass().getName();
		String ending = "V" + getVersion();
		if (!name.endsWith(ending)) {
			throw new RuntimeException("Message Group Class Name / Version Mismatch. " + "Class " +
								name + ", Version " + getVersion());
		}
	}
	
	public String getMessageSelector() {
    	StringBuilder builder = new StringBuilder();
    	builder.append(MessagingConstants.HeaderConstant.GroupId.name())
    		   .append(" = ")
    		   .append(getId())
    		   .append(" AND ")
    		   .append(MessagingConstants.HeaderConstant.GroupVersion.name())
    		   .append(" = ")
    		   .append(getVersion());
    	
    	return builder.toString();
	}
	
    public void setMessageProcessorNames(List<String> messageProcessorNames) {
        this.messageProcessorNames = messageProcessorNames;
    }

    public List<String> getMessageProcessorNames() {
    	return messageProcessorNames;
    }
    
	protected void register(Message msg) {
		registeredMessages.add(msg);
	}
	
	public List<Message> getMessages() {
		return registeredMessages;
	}
	
	public void validate(Map<TypeVersionTuple, String> processorMap) {
        Map<TypeVersionTuple, String> processors = new HashMap<TypeVersionTuple, String>(processorMap);
        StringBuilder errors = new StringBuilder();
        
		for (Message message : registeredMessages) {
			TypeVersionTuple tuple = new TypeVersionTuple(message.getMessageType(), message.getMessageVersion());
            String processorName = processors.remove(tuple);
            if (processorName == null) {
                //no processor for message type/version
                errors
                    .append("No processor registered for MessageType ")
                    .append(message.getMessageType())
                    .append(", Version ")
                    .append(message.getMessageVersion())
                    .append("\n");
            }
		}
        for (Map.Entry<TypeVersionTuple, String> entry : processors.entrySet()) {
            TypeVersionTuple tuple = entry.getKey();
            String processorName = entry.getValue();
            errors
                .append("No message registered for processsor ")
                .append(processorName)
                .append(" which handles MessageType ")
                .append(tuple.getType())
                .append(", Version ")
                .append(tuple.getVersion())
                .append("\n");
        }
        if (errors.length() > 0) {
            throw new IllegalStateException(errors.toString());
        }
	}
	
    public static class TypeVersionTuple {
        
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
