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
import com.paxxis.cornerstone.messaging.common.Message;
import com.paxxis.cornerstone.messaging.common.MessageProducer;

import javax.jms.*;

public class JMSMessageProducer implements MessageProducer {

    private javax.jms.MessageProducer messageProducer;

    public JMSMessageProducer(javax.jms.MessageProducer messageProducer) {
        this.messageProducer = messageProducer;
    }

    @Override
    public void send(Message message) {
        try {
            this.messageProducer.send(((JMSMessage) message).message);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void send(Destination destination, Message message) {
        try {
            this.messageProducer.send(
                ((JMSDestination) destination).getDestination(),
                ((JMSMessage) message).message);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            this.messageProducer.close();
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MessageProducer setPersistent(boolean persistent) {
        try {
            this.messageProducer.setDeliveryMode(persistent ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    @Override
    public MessageProducer setTimeToLive(long timeToLive) {
        try {
            this.messageProducer.setTimeToLive(timeToLive);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    @Override
    public MessageProducer setPriority(int priority) {
        try {
            this.messageProducer.setPriority(priority);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
        return this;
    }
}
