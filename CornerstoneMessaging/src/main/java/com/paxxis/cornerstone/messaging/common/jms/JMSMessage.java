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

import com.paxxis.cornerstone.messaging.common.Destination;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * @author Matthew Pflueger
 */
public class JMSMessage implements com.paxxis.cornerstone.messaging.common.Message {
    protected Message message;

    JMSMessage(Message message) {
        this.message = message;
    }

    @Override
    public void setReplyTo(Destination replyTo) {
        try {
            this.message.setJMSReplyTo(((JMSDestination) replyTo).getDestination());
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Destination getReplyTo() {
        try {
            return new JMSDestination(this.message.getJMSReplyTo());
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object getProperty(String name) {
        try {
            return this.message.getObjectProperty(name);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setProperty(String name, Object value) {
        try {
            this.message.setObjectProperty(name, value);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setCorrelationID(String correlationID) {
        try {
            this.message.setJMSCorrelationID(correlationID);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getCorrelationID() {
        try {
            return this.message.getJMSCorrelationID();
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void acknowledge() {
        try {
            this.message.acknowledge();
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
