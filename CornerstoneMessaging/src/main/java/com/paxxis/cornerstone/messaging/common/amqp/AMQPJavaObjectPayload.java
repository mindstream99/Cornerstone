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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Matthew Pflueger
 */
public class AMQPJavaObjectPayload extends AMQPMessagePayload {

    public AMQPJavaObjectPayload(Channel channel) {
        super(channel);
    }

    public PayloadType getType() {
//        return PayloadType.JsonObjectPayload;
        return PayloadType.JavaObjectPayload;
    }

    public Object getPayload(com.paxxis.cornerstone.messaging.common.Message message) {
        AMQPMessage msg = (AMQPMessage) message;
        Object result = null;

        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;

        try {
            bis = new ByteArrayInputStream(msg.getBody());
            ois = new ObjectInputStream(bis);
            result = ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                ois.close();
                bis.close();
            } catch (Exception e) {
                //ignore
            }
        }

        return result;
    }

	public AMQPMessage createMessage(com.paxxis.cornerstone.base.Message data) {
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;

        try {
            AMQPMessage msg = createMessage();
            Map<String, Object> headers = new HashMap<String, Object>(msg.getProps().getHeaders());
            headers.put(MessagingConstants.HeaderConstant.MessageType.name(), data.getMessageType());
            headers.put(MessagingConstants.HeaderConstant.MessageVersion.name(), data.getMessageVersion());

            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(data);
            oos.flush();

            return new AMQPMessage(
                    bos.toByteArray(),
                    null,
                    msg.getProps().builder().headers(headers).build(),
                    getChannel());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                oos.close();
                bos.close();
            } catch (Exception e) {
                //ignore
            }
        }
    }
}
