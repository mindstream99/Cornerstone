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
import javax.jms.InvalidDestinationException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.TemporaryQueue;

import org.apache.log4j.Logger;

import com.paxxis.cornerstone.base.RequestMessage;
import com.paxxis.cornerstone.base.ResponseMessage;



/**
 *
 * @author Robert Englander
 */
public abstract class MessageProcessor<REQ extends RequestMessage, RESP extends ResponseMessage<REQ>> 
							extends SimpleMessageProcessor<REQ, RESP> {
    private static final Logger logger = Logger.getLogger(MessageProcessor.class);
    

	@SuppressWarnings("unchecked")
    public REQ getMessagePayload() {
		return (REQ) getPayload();
	}

    public void run() {
        try {
            Message message = getJMSMessage();
            Destination tq = message.getJMSReplyTo();

            RESP resp = process(false, tq);
            
            if (resp != null) {

				Message responseMsg = getPayloadType().createMessage(getSession(), resp);
                // stamp the message
                responseMsg.setLongProperty("TIMESTAMP", System.currentTimeMillis());
                // copy the correlation id from the incoming message
                responseMsg.setJMSCorrelationID(message.getJMSCorrelationID());

                if (tq != null) {
                    MessageProducer qsender = getSession().createProducer(null);
                    try {
	                    qsender.send(tq, responseMsg);
                    } catch (InvalidDestinationException ide) {
                        //this is a valid state...
                        logger.debug(ide);
                    } finally {
	                    qsender.close();
                    }
                } 
            }

            if (isClientAck()) {
                message.acknowledge();
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }
    
}
