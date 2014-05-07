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

import com.paxxis.cornerstone.messaging.common.Destination;
import com.paxxis.cornerstone.messaging.common.MessageConsumer;
import com.rabbitmq.client.DefaultConsumer;

import java.io.IOException;


public class AMQPMessageConsumer implements MessageConsumer {
    private DefaultConsumer messageConsumer;
    private String consumerTag;
    private AMQPDestination destination;

    public AMQPMessageConsumer(AMQPDestination destination, DefaultConsumer messageConsumer, String consumerTag) {
        this.destination = destination;
        this.messageConsumer = messageConsumer;
        this.consumerTag = consumerTag;
    }

    @Override
    public void close() {
        try {
            this.messageConsumer.getChannel().basicCancel(this.consumerTag);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public DefaultConsumer getMessageConsumer() {
        return messageConsumer;
    }

    public String getConsumerTag() {
        return consumerTag;
    }

    @Override
    public Destination getDestination() {
        return null;
    }
}
