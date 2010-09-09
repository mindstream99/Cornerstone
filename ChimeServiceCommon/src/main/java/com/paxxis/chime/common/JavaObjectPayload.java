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

package com.paxxis.chime.common;

import com.paxxis.chime.client.common.MessageConstants;
import com.paxxis.chime.client.common.MessageConstants.PayloadType;
import java.io.Serializable;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;

/**
 *
 * @author Robert Englander
 */
public class JavaObjectPayload implements MessagePayload {

    public PayloadType getType() {
        return PayloadType.JavaObjectPayload;
    }

    public Object getPayload(javax.jms.Message msg) {
        Object result = null;
        try {
            result = ((ObjectMessage)msg).getObject();
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public javax.jms.Message createMessage(Session session) {
        try {
            ObjectMessage msg = session.createObjectMessage();
            msg.setIntProperty(MessageConstants.HeaderConstant.PayloadType.name(),
                    getType().getValue());
            return msg;
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    public javax.jms.Message createMessage(Session session, Object data) {
        try {
            ObjectMessage msg = (ObjectMessage)createMessage(session);
            msg.setObject((Serializable)data);
            return msg;
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
