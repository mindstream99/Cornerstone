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
package com.paxxis.cornerstone.common;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import com.paxxis.cornerstone.base.Message;
import com.paxxis.cornerstone.base.MessagingConstants;
import com.paxxis.cornerstone.base.MessagingConstants.PayloadType;

/**
 * 
 * @author Liliya Grinberg
 * 
 */
public class JSonObjectPayload implements MessagePayload {
	private ObjectMapper mapper;
	private Class<? extends Message> clazz;

	public JSonObjectPayload(Class<? extends Message> clazz) {
	    this(new ObjectMapper(), clazz);
	}
	
	public JSonObjectPayload(ObjectMapper mapper, Class<? extends Message> clazz) {
		this.mapper = mapper;
		this.clazz = clazz;
		this.mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public PayloadType getType() {
		return PayloadType.JsonObjectPayload;
	}

	public Object getPayload(javax.jms.Message msg) {
		Object data = null;
		try {
			String jsonData = ((TextMessage) msg).getText();
			data = mapper.readValue(jsonData, clazz);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return data;
	}

	public javax.jms.Message createMessage(Session session) {
		try {
			TextMessage msg = session.createTextMessage();
			msg.setIntProperty(MessagingConstants.HeaderConstant.PayloadType.name(),
					getType().getValue());
			return msg;
		} catch (JMSException e) {
			throw new RuntimeException(e);
		}
	}

	public javax.jms.Message createMessage(Session session, com.paxxis.cornerstone.base.Message data) {
		try {
			TextMessage msg = (TextMessage) createMessage(session);
			msg.setIntProperty(MessagingConstants.HeaderConstant.MessageType.name(), data.getMessageType());
			msg.setIntProperty(MessagingConstants.HeaderConstant.MessageVersion.name(), data.getMessageVersion());

			msg.setText(mapper.writeValueAsString(data));
			return msg;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
