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
package com.paxxis.cornerstone.messaging.common;


import com.paxxis.cornerstone.base.MessagingConstants;

import java.util.Collections;
import java.util.Map;

/**
 * @author Matthew Pflueger
 */
public abstract class Session {

    public static final int AUTO_ACKNOWLEDGE = 1;

    public static final int CLIENT_ACKNOWLEDGE = 2;

    public static final int DUPS_OK_ACKNOWLEDGE = 3;

    public static final int SESSION_TRANSACTED = 0;

    /**
     * Create a message consumer for a specified destination.
     *
     * @param dest the destination for which a consumer is created
     * @param selector the message selector (or null if no selector is used)
     * @param messageListener the application level message listener
     * @param arguments optional arguments for the specific consumer implementation
     *
     * @return a message consumer
     */
    public abstract MessageConsumer createConsumer(
            Destination dest,
            String selector,
            MessageListener messageListener,
            Map<String, Object> arguments);

    public abstract Destination createTemporaryDestination();

    public MessageConsumer createConsumer(String selector, MessageListener messageListener) {
        return createConsumer(createTemporaryDestination(), selector, messageListener);
    }

    public MessageConsumer createConsumer(Destination dest, String selector, MessageListener messageListener) {
        return createConsumer(dest, selector, messageListener, Collections.EMPTY_MAP);
    }

    /**
     * Create a message sender for a destination
     *
     * @param destination the destination
     *
     * @return a message producer
     */
    public abstract MessageProducer createProducer(Destination destination);
    public abstract MessageProducer createProducer(String destination);
    public abstract MessageProducer createProducer();

    public abstract MessagePayload createJavaObjectPayload();
    public abstract MessagePayload createJSonObjectPayload(Class<? extends com.paxxis.cornerstone.base.Message> clazz);

    public abstract Message createMessage(com.paxxis.cornerstone.base.Message message, MessagingConstants.PayloadType payloadType);

    public Object getPayload(Message message, MessagingConstants.PayloadType payloadType, Class<? extends com.paxxis.cornerstone.base.Message> clazz) {
        switch (payloadType) {
            case JavaObjectPayload:
                return this.createJavaObjectPayload().getPayload(message);
            case JsonObjectPayload:
                return this.createJSonObjectPayload(clazz).getPayload(message);
            default:
                throw new UnsupportedOperationException("Unknown payload type " + payloadType);
        }
    }
}
