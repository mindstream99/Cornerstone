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
import com.paxxis.cornerstone.messaging.common.Destination;
import com.paxxis.cornerstone.messaging.common.Message;
import com.paxxis.cornerstone.messaging.common.MessageConsumer;
import com.paxxis.cornerstone.messaging.common.MessageListener;
import com.paxxis.cornerstone.messaging.common.MessagePayload;
import com.paxxis.cornerstone.messaging.common.MessageProducer;
import com.paxxis.cornerstone.messaging.common.Session;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;


public class AMQPSession extends Session {

    private Channel channel;
    private String exchangeName;

    private boolean durableQueue = true;
    private boolean exclusiveQueue = false;
    private boolean autoDeleteQueue = false;
    private boolean autoAck = true;

    AMQPSession() {
        //blank exchange is important - it is the default exchange
        this(null, "", false);
    }

    public AMQPSession(Channel channel, String exchangeName, boolean autoAck) {
        this.channel = channel;
        this.exchangeName = exchangeName;
        this.autoAck = autoAck;
    }

    /**
     * Create binding args for a queue based on the selector in the form of "key=value AND key=value".  Currently
     * this method only supports AND (all) selectors WITH integer values.
     *
     * @param selector The selector to parse to create the binding args
     * @return a map of binding args suitable for a headers exchange
     */
    Map<String, Object> createBindingArgs(String selector) {
        Map<String, Object> args = new HashMap<String, Object>();
        if (selector == null || selector.isEmpty()) {
            return args;
        }

        args.put("x-match", "all");

        String[] s = selector.split("(=|AND)");
        for(int i = 0; i < s.length;) {
            try {
                args.put(s[i].trim(), Integer.valueOf(s[i + 1].trim()));
            } catch (NumberFormatException nfe) {
                args.put(s[i].trim(), s[i + 1].trim());
            }
            i = i + 2;
        }

        return args;
    }

    @Override
    public MessageConsumer createConsumer(
            Destination destination,
            String selector,
            final MessageListener messageListener,
            Map<String, Object> args) {

        try {
            AMQPDestination dest = (AMQPDestination) destination;
            String routingKey = dest.getDestination();

            //as a nicety we declare and bind the queues on the consumer side if the queue is not temporary
            if (!dest.isTemporary()) {
                ConcurrentHashMap<String, Object> p = new ConcurrentHashMap<String, Object>(args);
                p.putIfAbsent("durable", durableQueue);
                p.putIfAbsent("exclusive", exclusiveQueue);
                p.putIfAbsent("autoDelete", autoDeleteQueue);

                channel.queueDeclare(
                        routingKey,
                        (Boolean) p.remove("durable"),
                        (Boolean) p.remove("exclusive"),
                        (Boolean) p.remove("autoDelete"),
                        args);
                channel.queueBind(
                        routingKey,
                        exchangeName,
                        routingKey,
                        createBindingArgs(selector + " AND routingKey = " + routingKey));
            }

            DefaultConsumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(
                        String consumerTag,
                        Envelope envelope,
                        AMQP.BasicProperties properties,
                        byte[] body) throws IOException {
                    messageListener.onMessage(new AMQPMessage(body, envelope, properties, channel));
                }
            };

            String consumerTag = channel.basicConsume(routingKey, autoAck, consumer);
            return new AMQPMessageConsumer(dest, consumer, consumerTag);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MessageProducer createProducer() {
        return new AMQPMessageProducer(channel, exchangeName);
    }

    @Override
    public MessageProducer createProducer(Destination destination) {
        AMQPDestination dest = (AMQPDestination) destination;
        return new AMQPMessageProducer(channel, dest.getExchange(), dest.getDestination());
//        return new AMQPMessageProducer(channel, exchangeName, ((AMQPDestination) destination).getDestination());
    }

    @Override
    public Destination createTemporaryDestination() {
        try {
            AMQP.Queue.DeclareOk ok = channel.queueDeclare();
            return new AMQPDestination(ok.getQueue(), channel, "", true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MessageProducer createProducer(String destination) {
        return new AMQPMessageProducer(channel, exchangeName, destination);
    }

    @Override
    public MessagePayload createJavaObjectPayload() {
        return new AMQPJavaObjectPayload(channel);
    }

    @Override
    public MessagePayload createJSonObjectPayload(Class<? extends com.paxxis.cornerstone.base.Message> clazz) {
        return new AMQPJSonObjectPayload(clazz, channel);
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

    public Channel getChannel() {
        return channel;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public boolean isDurableQueue() {
        return durableQueue;
    }

    public void setDurableQueue(boolean durableQueue) {
        this.durableQueue = durableQueue;
    }

    public boolean isExclusiveQueue() {
        return exclusiveQueue;
    }

    public void setExclusiveQueue(boolean exclusiveQueue) {
        this.exclusiveQueue = exclusiveQueue;
    }

    public boolean isAutoDeleteQueue() {
        return autoDeleteQueue;
    }

    public void setAutoDeleteQueue(boolean autoDeleteQueue) {
        this.autoDeleteQueue = autoDeleteQueue;
    }

}
