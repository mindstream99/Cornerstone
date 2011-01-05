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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TemporaryQueue;

import org.apache.log4j.Logger;

import com.paxxis.cornerstone.base.ErrorMessage;
import com.paxxis.cornerstone.base.MessagingConstants;
import com.paxxis.cornerstone.base.RequestMessage;
import com.paxxis.cornerstone.base.ResponseMessage;
import com.paxxis.cornerstone.common.JavaObjectPayload;
import com.paxxis.cornerstone.common.MessagePayload;
import com.paxxis.cornerstone.common.ResponsePromise;

/**
 *
 * @author Robert Englander
 */
public class RequestQueueSender extends CornerstoneConfigurable implements IServiceBusConnectorClient {
    private static final Logger logger = Logger.getLogger(RequestQueueSender.class);

    // the temporary queue used for getting responses
    private TemporaryQueue responseQueue = null;

    // the request queue
    private Queue queue = null;

    // the message consumer for the responseQueue
    private MessageConsumer responseConsumer = null;

    // the message sender for sending requests
    private MessageProducer requestSender = null;

    // the name of the request queue
    private String requestQueueName = null;

    // the service bus connector
    private ServiceBusConnector connector = null;

    // teardown pending flag
    private boolean teardownPending = false;

    // mapping of correlation ids to message listeners
    private Map<String, MessageListener> listenerMap = 
        new HashMap<String, MessageListener>();

    private AtomicLong correlationId = new AtomicLong(0);

    public RequestQueueSender() {
    }

    public void setRequestQueueName(String name) {
        requestQueueName = name;
    }

    protected String getRequestQueueName() {
        return requestQueueName;
    }

    public void setServiceBusConnector(ServiceBusConnector connector) {
        this.connector = connector;
    }

    /**
     * Is a teardown pending?
     *
     * @return true if a disconnect has been initiated and is not
     * yet complete, false otherwise.
     */
    public boolean isTeardownPending() {
        return teardownPending;
    }

    /**
     * Close the JMS session objects
     */
    protected void closeDown() throws JMSException {
        
        requestSender.close();
        requestSender = null;

        responseConsumer.close();
        responseConsumer = null;

        responseQueue.delete();
        responseQueue = null;

        queue = null;
    }

    public void halt() {
    }

    /**
     * Setup the JMS specific objects.<br><br>
     * This method is called by the ServiceBusConnector as part of its
     * connection process.  It should not be called directly by users...
     *
     * @throws RuntimeException if the setup could not be completed
     */
    public void setup() {

        try {
            // lookup the request queue
            queue = (Queue)connector.getInitialContextFactory().createInitialContext().lookup(requestQueueName);

            requestSender = connector.createMessageProducer(queue);
            requestSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            responseQueue = connector.createTemporaryQueue();

            // create a message consumer
            responseConsumer = connector.createConsumer(responseQueue, null);

            responseConsumer.setMessageListener(
                new MessageListener() {
                    public void onMessage(Message msg) {
                        try
                        {
                            MessageListener listener = 
                                listenerMap.remove(msg.getJMSCorrelationID());
                            if (listener != null) {
                                listener.onMessage(msg);
                            }
                        } catch (Exception e) {
                            // this needs to be logged (and reported through mgmt interface?)
                            logger.error(e);
                        }
                    }
                }
            );
        } catch(Throwable t) {
            try {
                closeDown();
            } catch (JMSException e) {
                // is there any need to include this in the runtime exception below?
            }

            throw new RuntimeException(t);
        }
   }

    /**
     * Tear down the sender.
     */
    public void tearDown(final ShutdownListener listener) {
        // don't do this if we're already in the process of tearing down
        if (!isTeardownPending()) {
            teardownPending = true;

            try {
                closeDown();
                teardownPending = false;
                listener.onShutdownComplete();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Send a request message and return the response.
     *
     * @param clazz the response class
     * @param msg the message 
     * @param handler the message handler
     * @param timeout the number of milliseconds to wait for the response.  A timeout of
     * 0 or less is an error
     * @param payloadType the message payload type
     *
     * @return the response
     * @throws a RequestTimeoutException if response timed out
     */
    public synchronized <REQ extends RequestMessage, RESP extends ResponseMessage<REQ>> RESP send(
            Class<RESP> clazz, 
            REQ msg,
            final SimpleServiceBusMessageHandler handler,
			long timeout, 
			MessagePayload payloadType) {


        final ResponsePromise<RESP> promise = new ResponsePromise<RESP>(timeout); 
        MessageListener listener = new MessageListener() {
            public void onMessage(Message msg) {
                handler.init(
                        connector.getSession(), 
                        connector.getAcknowledgeMode() == Session.CLIENT_ACKNOWLEDGE);
                promise.setObject(handler.processMessage(msg));
            }
        };

        RESP response = null;
        try {
            send(msg, promise, listener, payloadType); 
        } catch (SendException se) {
			try {
				response = clazz.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
            response.setError(se.getErrorMessage());
            return response;
        }

        response = promise.getResponse();
        if (response == null) {
            throw new RequestTimeoutException();
        }
        return response;
    }

    /**
     * Send a request message and return without waiting for a response but
     * with a promise for one
     *
     * @param msg the message 
     * @param payloadType the message payload type
     *
     * @return a response promise
     */
    public synchronized <REQ extends RequestMessage, 
                        RESP extends ResponseMessage<REQ>> ResponsePromise<RESP> send(
            REQ msg,
            MessagePayload payloadType) {
        ResponsePromise<RESP> promise = new ResponsePromise<RESP>();
        send(msg, promise, null, payloadType);
        return promise;
    }


    /**
     * Send a request message and return without waiting for a response but
     * with a promise for one
     *
     * @param msg the message 
     * @param promise the response promise to populate
     * @param payloadType the message payload type
     *
     * @return a response promise
     */
    public synchronized <REQ extends RequestMessage,
                        RESP extends ResponseMessage<REQ>, 
                        P extends ResponsePromise<RESP>> void send(
            REQ msg,
            final P promise, 
            final MessagePayload payloadType) {
        send(msg, promise, null, payloadType);
    }

    /**
     * Send a request message and return without waiting for a response but
     * with a promise for one
     *
     * @param msg the message 
     * @param promise the response promise to populate
     * @param listener the message listener to invoke on receiving a response
     * but before populating the promise
     * @param payloadType the message payload type
     *
     * @return a response promise
     */
    public synchronized <REQ extends RequestMessage, 
                        RESP extends ResponseMessage<REQ>, 
                        P extends ResponsePromise<RESP>> void send(
            REQ msg,
            final P promise, 
            final MessageListener listener,
            final MessagePayload payloadType) {

        if (promise == null) {
            throw new NullPointerException("promise is null");
        }

        if (!connector.isConnected()) {
            ErrorMessage errorMsg = new ErrorMessage();
            errorMsg.setMessage("Unable to send request.  Not connected to service bus.");
            throw new SendException(errorMsg);
        }

        MessageListener promiseListener = new MessageListener() {
            public void onMessage(Message message) {
                    if (listener != null) {
                        listener.onMessage(message);
                    }
                    promise.setObject(payloadType.getPayload(message));
            }
        };

        try {
            Message message = prepareMessage(msg, payloadType);
            listenerMap.put(message.getJMSCorrelationID(), promiseListener);
            requestSender.send(message);
        } catch (JMSException je) {
            ErrorMessage errorMsg = new ErrorMessage();
            errorMsg.setMessage("Unable to send request. " + je.getMessage());
            throw new SendException(errorMsg);
        }
    }

    /**
     * Send a request message and return without waiting for a response but
     * with a promise for one
     *
     * @param msg the message 
     * @param listener the message listener to invoke on receiving a response
     * but before populating the promise
     * @param payloadType the message payload type
     *
     * @return a response promise
     */
    public synchronized <REQ extends RequestMessage,
                        RESP extends ResponseMessage<REQ>> ResponsePromise<RESP> send(
            REQ msg,
            MessageListener listener, 
            MessagePayload payloadType) {
        ResponsePromise<RESP> promise = new ResponsePromise<RESP>();
        send(msg, promise, listener, payloadType);
        return promise;
    }

    /**
     * Prepare a message for sending.
     *
     * @param requester the service requester
     */
    private Message prepareMessage(
            com.paxxis.cornerstone.base.Message msg,
            MessagePayload payloadType) 
            throws JMSException {

        Message message = payloadType.createMessage(connector.getSession());

        message.setIntProperty(MessagingConstants.HeaderConstant.MessageType.name(), msg.getMessageType());
        message.setIntProperty(MessagingConstants.HeaderConstant.MessageVersion.name(), msg.getMessageVersion());
        message.setIntProperty(MessagingConstants.HeaderConstant.PayloadType.name(), payloadType.getType().getValue());

        Object payload = msg.getAsPayload(payloadType.getType());
        if (payloadType instanceof JavaObjectPayload) {
            ((ObjectMessage)message).setObject((Serializable)payload);
        }

        message.setJMSReplyTo(responseQueue);
        message.setJMSCorrelationID(generateCorrelationId());
        return message;
    }

    private String generateCorrelationId() {
        return String.valueOf(correlationId.incrementAndGet());
    }
}
