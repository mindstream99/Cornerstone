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

import javax.jms.MessageListener;

import com.paxxis.cornerstone.base.RequestMessage;
import com.paxxis.cornerstone.base.ResponseMessage;
import com.paxxis.cornerstone.common.MessagePayload;
import com.paxxis.cornerstone.common.ResponsePromise;
import com.paxxis.cornerstone.service.ServiceBusSenderPool.PoolEntry;

/**
 * 
 * @author Matthew Pflueger
 */
public class RequestQueueSenderPool extends DestinationPublisherPool<RequestQueueSender> implements QueueSender {
    
    /* (non-Javadoc)
     * @see com.paxxis.cornerstone.service.QueueSender#send(com.paxxis.cornerstone.base.RequestMessage, com.paxxis.cornerstone.common.MessagePayload)
     */
    @Override
    public <REQ extends RequestMessage, RESP extends ResponseMessage<REQ>> ResponsePromise<RESP> send(
            REQ msg, MessagePayload payloadType) {
        PoolEntry<RequestQueueSender> entry = getSenderPool().borrow(this);
        try {
            return entry.getSender().send(msg, payloadType);
        } finally {
            getSenderPool().returnInstance(entry);
        }
    }

    /* (non-Javadoc)
     * @see com.paxxis.cornerstone.service.QueueSender#send(com.paxxis.cornerstone.base.RequestMessage, com.paxxis.cornerstone.common.ResponsePromise, com.paxxis.cornerstone.common.MessagePayload)
     */
    @Override
    public <REQ extends RequestMessage, RESP extends ResponseMessage<REQ>, P extends ResponsePromise<RESP>> void send(
            REQ msg, P promise, MessagePayload payloadType) {
        PoolEntry<RequestQueueSender> entry = getSenderPool().borrow(this);
        try {
            entry.getSender().send(msg, promise, payloadType);
        } finally {
        	getSenderPool().returnInstance(entry);
        }
    }

    /* (non-Javadoc)
     * @see com.paxxis.cornerstone.service.QueueSender#send(com.paxxis.cornerstone.base.RequestMessage, com.paxxis.cornerstone.common.ResponsePromise, javax.jms.MessageListener, com.paxxis.cornerstone.common.MessagePayload)
     */
    @Override
    public <REQ extends RequestMessage, RESP extends ResponseMessage<REQ>, P extends ResponsePromise<RESP>> void send(
            REQ msg, P promise, MessageListener listener, MessagePayload payloadType) {
        // TODO Auto-generated method stub
        PoolEntry<RequestQueueSender> entry = getSenderPool().borrow(this);
        try {
            entry.getSender().send(msg, promise, listener, payloadType);
        } finally {
        	getSenderPool().returnInstance(entry);
        }

    }

    /* (non-Javadoc)
     * @see com.paxxis.cornerstone.service.QueueSender#send(com.paxxis.cornerstone.base.RequestMessage, javax.jms.MessageListener, com.paxxis.cornerstone.common.MessagePayload)
     */
    @Override
    public <REQ extends RequestMessage, RESP extends ResponseMessage<REQ>> ResponsePromise<RESP> send(
            REQ msg, MessageListener listener, MessagePayload payloadType) {
        PoolEntry<RequestQueueSender> entry = getSenderPool().borrow(this);
        try {
            return entry.getSender().send(msg, listener, payloadType);
        } finally {
        	getSenderPool().returnInstance(entry);
        }
    }

}
