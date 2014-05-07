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
package com.paxxis.cornerstone.messaging.common.amqp;

import com.paxxis.cornerstone.base.MessagingConstants;
import com.paxxis.cornerstone.base.MessagingConstants.PayloadType;
import com.rabbitmq.client.Channel;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.HashMap;
import java.util.Map;


/**
 * 
 * @author Matthew Pflueger
 */
public class AMQPJSonObjectPayload extends AMQPMessagePayload {
	private ObjectMapper mapper;
	private Class<? extends com.paxxis.cornerstone.base.Message> clazz;

	public AMQPJSonObjectPayload(Class<? extends com.paxxis.cornerstone.base.Message> clazz, Channel channel) {
	    this(new ObjectMapper(), clazz, channel);
	}

	public AMQPJSonObjectPayload(
	        ObjectMapper mapper,
	        Class<? extends com.paxxis.cornerstone.base.Message> clazz,
	        Channel channel) {
	    super(channel);
		this.mapper = mapper;
		this.clazz = clazz;
		this.mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public PayloadType getType() {
		return PayloadType.JsonObjectPayload;
	}

	public Object getPayload(com.paxxis.cornerstone.messaging.common.Message message) {
	    AMQPMessage msg = (AMQPMessage) message;
		Object data = null;
		try {
			String jsonData = new String(msg.getBody());
			data = mapper.readValue(jsonData, clazz);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return data;
	}

	public AMQPMessage createMessage(com.paxxis.cornerstone.base.Message data) {
	    AMQPMessage msg = createMessage();
	    Map<String, Object> headers = new HashMap<String, Object>(msg.getProps().getHeaders());
	    headers.put(MessagingConstants.HeaderConstant.MessageType.name(), data.getMessageType());
        headers.put(MessagingConstants.HeaderConstant.MessageVersion.name(), data.getMessageVersion());

        try {
            return new AMQPMessage(
                    mapper.writeValueAsString(data).getBytes(),
                    null,
                    msg.getProps().builder().headers(headers).build(),
                    getChannel());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
