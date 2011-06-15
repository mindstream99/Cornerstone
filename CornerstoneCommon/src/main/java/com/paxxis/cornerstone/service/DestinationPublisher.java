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
package com.paxxis.cornerstone.service;

import javax.jms.Destination;

import com.paxxis.cornerstone.base.RequestMessage;
import com.paxxis.cornerstone.common.MessagePayload;



/**
 * An interface for generic publishers of messages to topics.
 * 
 * @author Matthew Pflueger
 */
public interface DestinationPublisher {

    /**
     * publish a message to the destination with no response.
     *
     * @param msg the message 
     * @param payloadType the message payload type
     *
     */
    <REQ extends RequestMessage> void publish(REQ msg, MessagePayload payloadType);

    /**
     * publish a message to the destination with no response.
     *
     * @param dest the destination to send the message to
     * @param msg the message 
     * @param payloadType the message payload type
     *
     */
    <REQ extends RequestMessage> void publish(Destination dest, REQ msg, MessagePayload payloadType);
}