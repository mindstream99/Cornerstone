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
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Matthew Pflueger
 */
public abstract class AMQPMessagePayload implements com.paxxis.cornerstone.messaging.common.MessagePayload {

    private Channel channel;

    protected AMQPMessagePayload() {}

    protected AMQPMessagePayload(Channel channel) {
        this.channel = channel;
    }

    public AMQPMessage createMessage() {
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(MessagingConstants.HeaderConstant.PayloadType.name(), getType().getValue());
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .contentType(getType().getContentType())
                .timestamp(new Date())
                .headers(headers)
                .build();

        return new AMQPMessage(null, null, props, this.channel);
    }

    public Channel getChannel() {
        return channel;
    }

}
