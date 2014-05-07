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

import com.rabbitmq.client.Channel;

import java.io.IOException;

public class AMQPDestination implements com.paxxis.cornerstone.messaging.common.Destination {

    private String destination;
    private Channel channel;
    private boolean temporary;
    private String exchange;

    public AMQPDestination(String destination, Channel channel) {
        this(destination, channel, "", false);
    }

    public AMQPDestination(String destination, Channel channel, String exchange, boolean temporary) {
        this.destination = destination;
        this.channel = channel;
        this.temporary = temporary;
        this.exchange = exchange;
    }

    @Override
    public void delete() {
        if (channel != null) {
            try {
                channel.queueDelete(getDestination());
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
    }

    @Override
    public boolean isTemporary() {
        return this.temporary;
    }

    public String getDestination() {
        return this.destination;
    }

    public String getExchange() {
        return exchange;
    }

    public Channel getChannel() {
        return channel;
    }

}
