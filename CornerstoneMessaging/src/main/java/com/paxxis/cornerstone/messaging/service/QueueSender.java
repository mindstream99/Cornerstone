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
package com.paxxis.cornerstone.messaging.service;

import com.paxxis.cornerstone.base.RequestMessage;
import com.paxxis.cornerstone.base.ResponseMessage;
import com.paxxis.cornerstone.messaging.common.MessageListener;
import com.paxxis.cornerstone.messaging.common.ResponsePromise;


/**
 * An interface for request/response over a queue
 * 
 * @author Matthew Pflueger
 */
public interface QueueSender extends DestinationPublisher {

    /**
     * Send a request message and return without waiting for a response but
     * with a promise for one
     *
     * @param msg the message 
     *
     * @return a response promise
     */
    <REQ extends RequestMessage, RESP extends ResponseMessage<REQ>> ResponsePromise<RESP> send(
            REQ msg, Class<RESP> clazz);

    /**
     * Send a request message and return without waiting for a response but
     * with a promise for one
     *
     * @param msg the message 
     * @param promise the response promise to populate
     *
     * @return a response promise
     */
    <REQ extends RequestMessage, RESP extends ResponseMessage<REQ>, P extends ResponsePromise<RESP>> void send(
            REQ msg, final P promise, Class<RESP> clazz);

    /**
     * Send a request message and return without waiting for a response but
     * with a promise for one
     *
     * @param msg the message 
     * @param promise the response promise to populate
     * @param listener the message listener to invoke on receiving a response
     * but before populating the promise
     *
     * @return a response promise
     */
    <REQ extends RequestMessage, RESP extends ResponseMessage<REQ>, P extends ResponsePromise<RESP>> void send(
            REQ msg, final P promise, final MessageListener listener, Class<RESP> clazz);

    /**
     * Send a request message and return without waiting for a response but
     * with a promise for one
     *
     * @param msg the message 
     * @param listener the message listener to invoke on receiving a response
     * but before populating the promise
     *
     * @return a response promise
     */
    <REQ extends RequestMessage, RESP extends ResponseMessage<REQ>> ResponsePromise<RESP> send(
            REQ msg, MessageListener listener, Class<RESP> clazz);

}