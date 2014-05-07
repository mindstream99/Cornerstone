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
package com.paxxis.cornerstone.messaging.common.jms;

import com.paxxis.cornerstone.base.Message;
import com.paxxis.cornerstone.base.MessagingConstants;
import com.paxxis.cornerstone.base.MessagingConstants.PayloadType;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;

/**
 *
 * @author Robert Englander
 */
public class JMSJavaObjectPayload extends JMSMessagePayload {

    public JMSJavaObjectPayload(Session session) {
        super(session);
    }

    public PayloadType getType() {
        return PayloadType.JavaObjectPayload;
    }

    public Object getPayload(com.paxxis.cornerstone.messaging.common.Message message) {
        ObjectMessage msg = (ObjectMessage) ((JMSMessage) message).message;
        try {
            return msg.getObject();
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    public JMSMessage createMessage() {
        try {
            ObjectMessage msg = session.createObjectMessage();
            msg.setIntProperty(MessagingConstants.HeaderConstant.PayloadType.name(), getType().getValue());
            return new JMSMessage(msg);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

	public JMSMessage createMessage(Message data) {
        try {
            JMSMessage message = createMessage();
            ObjectMessage msg = (ObjectMessage) message.message;
			msg.setIntProperty(MessagingConstants.HeaderConstant.MessageType.name(), data.getMessageType());
			msg.setIntProperty(MessagingConstants.HeaderConstant.MessageVersion.name(), data.getMessageVersion());

            msg.setObject(data);
            return message;
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
