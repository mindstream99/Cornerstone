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
import com.paxxis.cornerstone.messaging.common.Message;
import com.paxxis.cornerstone.messaging.common.MessageProducer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

import java.io.IOException;

public class AMQPMessageProducer implements MessageProducer {

    private Channel channel;
    private String exchangeName;

    //routingKey may or may not be used depending on the exchange type and can be null
    private String routingKey;

    private Boolean persistent = null;
    private Long timeToLive = null;
    private Integer priority = null;


    public AMQPMessageProducer(Channel channel, String exchangeName) {
        this(channel, exchangeName, null);
    }

    public AMQPMessageProducer(Channel channel, String exchangeName, String routingKey) {
        this.channel = channel;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
    }

    private AMQP.BasicProperties makeProps(AMQP.BasicProperties props) {
        AMQP.BasicProperties.Builder builder = props.builder();
        if (this.persistent != null) {
            builder.deliveryMode(this.persistent ? 2 : 1);
        }
        if (this.timeToLive != null) {
            builder.expiration(timeToLive.toString());
        }
        if (this.priority != null) {
            builder.priority(priority);
        }
        return builder.build();
    }

    @Override
    public void send(Message message) {
        AMQPMessage msg = (AMQPMessage) message;
        msg.setProperty("routingKey", routingKey);
        try {
            this.channel.basicPublish(
                    exchangeName,
                    routingKey,
                    makeProps(msg.getProps()),
                    msg.getBody());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void send(Destination destination, Message message) {
        AMQPMessage msg = (AMQPMessage) message;
        String routingKey = ((AMQPDestination) destination).getDestination();
        msg.setProperty("routingKey", routingKey);
        try {
            this.channel.basicPublish(
                    exchangeName,
                    routingKey,
                    makeProps(msg.getProps()),
                    msg.getBody());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        //noop as AMQP does not have the concept of an identifiable producer - you just publish on the channel
        //and since a channel can be used across multiple publishers we don't want to close that...
    }

    @Override
    public MessageProducer setPersistent(boolean persistent) {
        this.persistent = persistent;
        return this;
    }

    @Override
    public MessageProducer setTimeToLive(long timeToLive) {
        this.timeToLive = timeToLive;
        return this;
    }

    @Override
    public MessageProducer setPriority(int priority) {
        this.priority = priority;
        return this;
    }
}
