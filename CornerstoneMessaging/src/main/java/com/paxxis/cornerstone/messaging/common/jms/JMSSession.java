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

import com.paxxis.cornerstone.base.MessagingConstants;
import com.paxxis.cornerstone.messaging.common.Destination;
import com.paxxis.cornerstone.messaging.common.Message;
import com.paxxis.cornerstone.messaging.common.MessageConsumer;
import com.paxxis.cornerstone.messaging.common.MessageListener;
import com.paxxis.cornerstone.messaging.common.MessagePayload;
import com.paxxis.cornerstone.messaging.common.MessageProducer;
import com.paxxis.cornerstone.messaging.common.Session;
import com.paxxis.cornerstone.service.JndiInitialContextFactory;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.util.Map;

public class JMSSession extends Session {

    private javax.jms.Session session;
    private JndiInitialContextFactory contextFactory;

    public JMSSession(javax.jms.Session session, JndiInitialContextFactory contextFactory) {
        this.session = session;
        this.contextFactory = contextFactory;
    }

    public javax.jms.Session getSession() {
        return session;
    }

    @Override
    public Destination createTemporaryDestination() {
        try {
            return new JMSDestination(session.createTemporaryQueue(), true);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MessageConsumer createConsumer(
            Destination destination,
            String selector,
            MessageListener messageListener,
            Map<String, Object> args) {
        JMSDestination dest = (JMSDestination) destination;
        try {
            return new JMSMessageConsumer(
                    dest,
                    session.createConsumer(dest.getDestination(), selector),
                    messageListener);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MessageProducer createProducer() {
        try {
            return new JMSMessageProducer(session.createProducer(null));
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MessageProducer createProducer(String destinationName) {
        try {
            javax.jms.Destination dest = null;
            if (destinationName != null) {
                dest = (javax.jms.Destination) contextFactory.createInitialContext().lookup(destinationName);
            }
            return createProducer(new JMSDestination(dest));
        } catch (NamingException ne) {
            throw new RuntimeException(ne);
        }
    }

    @Override
    public MessageProducer createProducer(Destination destination) {
        try {
            return new JMSMessageProducer(session.createProducer(((JMSDestination) destination).getDestination()));
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MessagePayload createJavaObjectPayload() {
        return new JMSJavaObjectPayload(session);
    }

    @Override
    public MessagePayload createJSonObjectPayload(Class<? extends com.paxxis.cornerstone.base.Message> clazz) {
        return new JMSJSonObjectPayload(session, clazz);
    }

    @Override
    public Message createMessage(com.paxxis.cornerstone.base.Message message, MessagingConstants.PayloadType payloadType) {
        switch (payloadType) {
            case JavaObjectPayload:
                return this.createJavaObjectPayload().createMessage(message);
            case JsonObjectPayload:
                return this.createJSonObjectPayload(message.getClass()).createMessage(message);
            default:
                throw new UnsupportedOperationException("Unknown payload type " + payloadType);
        }
    }
}
