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

package com.paxxis.chime.service;

import com.paxxis.chime.client.common.MessageConstants;
import com.paxxis.chime.client.common.ErrorMessage;
import com.paxxis.chime.common.DataLatch;
import com.paxxis.chime.common.JavaObjectPayload;
import com.paxxis.chime.common.MessagePayload;
import java.io.Serializable;
import java.util.HashMap;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.TemporaryQueue;

/**
 *
 * @author Robert Englander
 */
public class RequestQueueSender extends ChimeConfigurable implements IServiceBusConnectorClient {
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

    class RequestLatch extends DataLatch {
        private SimpleServiceBusMessageHandler handler;

        public RequestLatch(SimpleServiceBusMessageHandler handler) {
            this.handler = handler;
        }

        public SimpleServiceBusMessageHandler getMessageHandler() {
            return handler;
        }
    }

    // mapping of correlation ids to RequestLatch instances
    private HashMap<Long, RequestLatch> _monitorMap = new HashMap<Long, RequestLatch>();

    // mapping of correlation ids to message listeners
    private HashMap<Long, MessageListener> _listenerMap = new HashMap<Long, MessageListener>();

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
     * connection process.  It should not be called directly by users
     * of RequestQueueReceiver instances.
     *
     *
     * @throws RuntimeException if the setup could not be completed
     */
    public void setup() {
        boolean success = false;

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
                            long cid = Long.parseLong(msg.getJMSCorrelationID());

                            // if there's a latch for this message in the map
                            // we just pass the message to it.
                            DataLatch mon = null;
                            synchronized (_monitorMap) {
                                mon = _monitorMap.get(cid);
                            }

                            if (mon != null) {
                                synchronized (_monitorMap) {
                                    // take the monitor out of the map
                                    _monitorMap.remove(cid);
                                }

                                //mon.setObject(payload);
                                mon.setObject(msg);
                            } else {
                                MessageListener listener = _listenerMap.get(cid);
                                if (listener != null) {
                                    _listenerMap.remove(cid);
                                    listener.onMessage(msg);
                                }
                            }
                        } catch (Exception e) {
                            // this needs to be logged (and reported through mgmt interface?)
                        }
                    }
                }
            );
        } catch(Throwable t) {
            try {
                closeDown();
            } catch (JMSException e) {
                // TODO is there any need to include this in the runtime exception below?
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
     * @param requester the service requester
     * @param timeout the number of milliseconds to wait for the response.  A timeout of
     * 0 means there is no timeout - i.e. wait forever.
     *
     * @return the response, or null if the response timed out.
     */
    public synchronized com.paxxis.chime.client.common.Message send(ServiceBusMessageProducer requester, SimpleServiceBusMessageHandler handler,
            long timeout, MessagePayload payloadType) {
        if (!connector.isConnected()) {
            ErrorMessage errorMsg = new ErrorMessage();
            errorMsg.setMessage("Unable to send request.  Not connected to service bus.");
            return errorMsg;
        } else {
            try {
                Message message = prepareMessage(requester, payloadType);

                RequestLatch mon = new RequestLatch(handler);

                synchronized (_monitorMap) {
                    _monitorMap.put(requester.getCorrelationId(), mon);
                }

                // send the message to the service request queue
                requestSender.send(message);

                // wait for the response
                Message response = (Message)mon.waitForObject(timeout);
                if (response == null) {
                    throw new RequestTimeoutException();
                }

                mon.getMessageHandler().init(connector.getSession());
                return mon.getMessageHandler().processMessage(response);
            } catch (JMSException je) {
                throw new RuntimeException(je);
            }
        }
    }

    /**
     * Send a request message and return without waiting for a response.
     *
     * @param requester the service requester
     */
    public synchronized void send(ServiceBusMessageProducer requester, MessagePayload payloadType) {
        send(requester, null, payloadType);
    }

    /**
     * Send a request message and return without waiting for a response.
     *
     * @param requester the service requester
     * @param listener the listener for a response to this request
     */
    public synchronized void send(ServiceBusMessageProducer requester, MessageListener listener, MessagePayload payloadType) {
        if (!connector.isConnected()) {
            ErrorMessage errorMsg = new ErrorMessage();
            errorMsg.setMessage("Unable to send request.  Not connected to service bus.");
            throw new SendException(errorMsg);
        } else {
            try {
                com.paxxis.chime.client.common.Message msg = requester.getMessage();
                Message message = prepareMessage(requester, payloadType);
                _listenerMap.put(requester.getCorrelationId(), listener);
                requestSender.send(message);
            } catch (JMSException je) {
                ErrorMessage errorMsg = new ErrorMessage();
                errorMsg.setMessage("Unable to send request. " + je.getMessage());
                throw new SendException(errorMsg);
            }
        }
    }

    /**
     * Prepare a message for sending.
     *
     * @param requester the service requester
     */
    private Message prepareMessage(ServiceBusMessageProducer requester, MessagePayload payloadType) throws JMSException {
        Message message = payloadType.createMessage(connector.getSession());

        com.paxxis.chime.client.common.Message msg = requester.getMessage();

        message.setIntProperty(MessageConstants.HeaderConstant.MessageType.name(), msg.getMessageType().getValue());
        message.setIntProperty(MessageConstants.HeaderConstant.MessageVersion.name(), msg.getMessageVersion());
        message.setIntProperty(MessageConstants.HeaderConstant.PayloadType.name(), payloadType.getType().getValue());

        Object payload = msg.getAsPayload(payloadType.getType());
        if (payloadType instanceof JavaObjectPayload) {
            ((ObjectMessage)message).setObject((Serializable)payload);
        }

        message.setJMSReplyTo(responseQueue);

        long cid = requester.getNewCorrelationId();
        message.setJMSCorrelationID(String.valueOf(cid));

        return message;
    }
}
