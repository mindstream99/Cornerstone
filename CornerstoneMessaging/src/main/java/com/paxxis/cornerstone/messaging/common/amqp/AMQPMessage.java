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
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Models a message in AMQP - very similar to RabbitMQ client's GetResponse
 * @author Matthew Pflueger
 */
public class AMQPMessage implements com.paxxis.cornerstone.messaging.common.Message {

    private byte[] body;
    private Envelope envelope;
    private AMQP.BasicProperties props;
    private Channel channel;


    public AMQPMessage(byte[] body) {
        this(body, null, new AMQP.BasicProperties.Builder().build(), null);
    }

    public AMQPMessage(byte[] body, Envelope envelope, AMQP.BasicProperties props, Channel channel) {
        this.body = body;
        this.props = props;
        this.envelope = envelope;
        this.channel = channel;
    }

    public byte[] getBody() {
        return body;
    }

    public Envelope getEnvelope() {
        return envelope;
    }

    public AMQP.BasicProperties getProps() {
        return props;
    }

    @Override
    public void setReplyTo(Destination replyTo) {
        props = props.builder().replyTo(((AMQPDestination) replyTo).getDestination()).build();
    }

    @Override
    public Destination getReplyTo() {
        return new AMQPDestination(props.getReplyTo(), this.channel, "", true); //replyTo queues are always temporary...
    }

    @Override
    public Object getProperty(String name) {
        return props.getHeaders().get(name);
    }

    @Override
    public void setProperty(String name, Object value) {
        Map<String, Object> headers = props.getHeaders() == null
            ? new HashMap<String, Object>()
            : new HashMap<String, Object>(props.getHeaders());

        headers.put(name, value);
        props = props.builder().headers(headers).build();
    }

    @Override
    public void setCorrelationID(String correlationID) {
        props = props.builder().correlationId(correlationID).build();
    }

    @Override
    public String getCorrelationID() {
        return props.getCorrelationId();
    }

    @Override
    public void acknowledge() {
        if (channel != null && envelope != null) {
            try {
                channel.basicAck(envelope.getDeliveryTag(), false);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
    }
}
