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

import com.paxxis.cornerstone.common.MessagePayload;

import javax.jms.Session;
import javax.jms.Message;

/**
 *
 * @author Robert Englander
 */
public abstract class SimpleMessageProcessor implements Runnable {
    // the session to use for sending responses
    private Session session;

    // the request message
    private Message message;

    private MessagePayload payloadType;

    private Object _payload = null;

    private boolean clientAck = false;

    public SimpleMessageProcessor(MessagePayload type) {
        payloadType = type;
    }

    public void setClientAck(boolean ack) {
        clientAck = ack;
    }

    public boolean isClientAck() {
        return clientAck;
    }
    
    public Session getSession() {
        return session;
    }

    public Message getMessage() {
        return message;
    }

    public MessagePayload getPayloadType() {
        return payloadType;
    }

    public void init(Session session, Message message) {
        this.session = session;
        this.message = message;
    }

    protected abstract com.paxxis.cornerstone.base.Message process(boolean ignorePreviousChanges) ;

    protected Object getPayload() {
        if (_payload != null) {
            return _payload;
        } else {
            try {
                return payloadType.getPayload(message);
            } catch (RuntimeException ex) {
                //Logger.getLogger(MessageProcessor.class.getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException(ex);
            }
        }
    }

    public com.paxxis.cornerstone.base.Message execute(Object payload, boolean ignorePreviousChanges) {
        _payload = payload;
        return process(ignorePreviousChanges);
    }

    public com.paxxis.cornerstone.base.Message execute(boolean ignorePreviousChanges) {
        return process(ignorePreviousChanges);
    }

    public void run() {
        try {
            // process the request
            execute(false);
        } catch (Exception e) {
            // we should really do something here.  write to log.  inform mgmt object.
        }
    }
}
