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

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.paxxis.cornerstone.base.MessagingConstants;
import com.paxxis.cornerstone.messaging.common.*;
import com.paxxis.cornerstone.service.ShutdownListener;
import org.apache.log4j.Logger;

import com.google.common.collect.MapMaker;
import com.paxxis.cornerstone.base.ErrorMessage;
import com.paxxis.cornerstone.base.RequestMessage;
import com.paxxis.cornerstone.base.ResponseMessage;
import com.paxxis.cornerstone.common.BlockingThreadPoolExecutor;
import com.paxxis.cornerstone.messaging.common.ResponseMessageListener;
import com.paxxis.cornerstone.messaging.common.ResponsePromise;


/**
 *
 * @author Robert Englander
 */
public class RequestQueueSender extends DestinationSender implements QueueSender {
    private static final Logger logger = Logger.getLogger(RequestQueueSender.class);

    // the temporary queue used for getting responses
    private Destination responseQueue = null;

    // the message consumer for the responseQueue
    private MessageConsumer responseConsumer = null;

    // mapping of correlation ids to message listeners
	private Map<String, MessageListener> listenerMap = null;

    private AtomicLong correlationId = new AtomicLong(0);

    private long timeout = 10000;
	private long listenerTimeout = 60000;
	private int concurrencyLevel = 16;
    private BlockingThreadPoolExecutor messageExecutor;
    
    public RequestQueueSender() {
    }

    public void initialize() {
        if (this.messageExecutor == null) {
            //unfortunately it seems we have nothing better for a name (no destination name yet) to
            //use for the thread pool :(
            this.messageExecutor = new BlockingThreadPoolExecutor();
            this.messageExecutor.setThreadPoolName(this.getClass().getSimpleName());
            this.messageExecutor.initialize();
        }

		if (this.listenerMap == null) {
			this.listenerMap = new MapMaker().concurrencyLevel(concurrencyLevel).makeMap();
		}
    }
    
    /**
     * The blocking thread pool executor that will execute the listeners for response messages.
     * @param messageExecutor
     */
    public void setMessageExecutor(BlockingThreadPoolExecutor messageExecutor) {
        this.messageExecutor = messageExecutor;
    }
    
    /**
     * The timeout, defaults to 10 seconds...
     * @param timeout
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

	/**
	 * Listener timeout, defaults to 60 seconds...
	 * 
	 * @param listenerTimeout
	 */
	public void setListenerTimeout(long listenerTimeout) {
		this.listenerTimeout = listenerTimeout;
	}

	/**
	 * The concurrency level for the listener map, defaults to 16...
	 * 
	 * @param concurrencyLevel
	 */
	public void setConcurrencyLevel(int concurrencyLevel) {
		this.concurrencyLevel = concurrencyLevel;
	}

	/**
	 * Close the JMS session objects
	 */
    protected void closeDown() {
        //tell the broker to stop sending us messages...
        responseConsumer.close();
        responseConsumer = null;
        responseQueue.delete();
        responseQueue = null;
        messageExecutor.shutdown(new ShutdownListener() {
            @Override
            public void onShutdownComplete() {
                logger.info("Message executor shutdown complete");
            }
        });
        super.closeDown();
    }

    /**
     * Setup the MS specific objects.<br><br>
     * This method is called by the ServiceBusConnector as part of its
     * connection process.  It should not be called directly by users...
     *
     * @throws RuntimeException if the setup could not be completed
     */
    public void setup() {
    	super.setup();
        try {
            responseQueue = getConnector().getSession().createTemporaryDestination();

            // create a message consumer
            responseConsumer = getConnector().getSession().createConsumer(responseQueue, null, new MessageListener() {
                public void onMessage(final Message msg) {
                    try {
                        final MessageListener listener =
                                listenerMap.remove(msg.getCorrelationID());
                        if (listener != null) {
                            RequestQueueSender.this.messageExecutor.submit(new Runnable() {
                                public void run() {
                                    listener.onMessage(msg);
                                }
                            });
                        } else {
                            logger.debug("Possible race condition detected: Null listener for message");
                        }
                    } catch (Exception e) {
                        // this needs to be logged (and reported through mgmt interface?)
                        logger.error(e);
                    }
                }
            });
        } catch(Throwable t) {
            try {
                closeDown();
            } catch (Exception e) {
                // is there any need to include this in the runtime exception below?
            }

            throw new RuntimeException(t);
        }
   }


    /* (non-Javadoc)
     * @see com.paxxis.cornerstone.service.QueueSender#send(REQ, com.paxxis.cornerstone.common.MessagePayload)
     */
    @Override
    public synchronized <REQ extends RequestMessage, 
                        RESP extends ResponseMessage<REQ>> ResponsePromise<RESP> send(
            REQ msg,
            Class<RESP> clazz) {
        ResponsePromise<RESP> promise = new ResponsePromise<RESP>(this.timeout);
        send(msg, promise, null, clazz);
        return promise;
    }


    /* (non-Javadoc)
     * @see com.paxxis.cornerstone.service.QueueSender#send(REQ, P, com.paxxis.cornerstone.common.MessagePayload)
     */
    @Override
    public synchronized <REQ extends RequestMessage,
                        RESP extends ResponseMessage<REQ>, 
                        P extends ResponsePromise<RESP>> void send(
            REQ msg,
            final P promise,
            final Class<RESP> clazz) {

        send(msg, promise, null, clazz);

    }

    /* (non-Javadoc)
     * @see com.paxxis.cornerstone.service.QueueSender#send(REQ, P, javax.jms.MessageListener, com.paxxis.cornerstone.common.MessagePayload)
     */
    @Override
    public synchronized <REQ extends RequestMessage, 
                        RESP extends ResponseMessage<REQ>, 
                        P extends ResponsePromise<RESP>> void send(
            REQ msg,
            final P promise, 
            final MessageListener listener,
            final Class<RESP> clazz) {

        if (promise == null) {
            throw new NullPointerException("promise is null");
        }

        if (!getConnector().isConnected()) {
            ErrorMessage errorMsg = new ErrorMessage();
            errorMsg.setMessage("Unable to send request.  Not connected to service bus.");
            throw new SendException(errorMsg);
        }

        MessageListener promiseListener = new MessageListener() {
            @SuppressWarnings("unchecked")
            public void onMessage(Message message) {
                    if (listener != null) {
                        listener.onMessage(message);
                    }

                    RESP responseMessage = (RESP) getConnector().getSession().getPayload(message, payloadType, clazz);
                    responseMessage.setResponseReceivedOn(System.currentTimeMillis());
                    if (logger.isDebugEnabled()) {
                        logger.debug(responseMessage.reportTimings());
                    }
                    
                    if (listener instanceof ResponseMessageListener) {
                        ((ResponseMessageListener<RESP>) listener).onResponseMessage(responseMessage);
                    }
                    
                    promise.setObject(responseMessage);
            }
        };

        try {
            Message message = prepareMessage(msg);
            listenerMap.put(message.getCorrelationID(), promiseListener);
            getMessageSender().send(message);
        } catch (Exception je) {
            ErrorMessage errorMsg = new ErrorMessage();
            errorMsg.setMessage("Unable to send request. " + je.getMessage());
            throw new SendException(errorMsg);
        }
    }

    /* (non-Javadoc)
     * @see com.paxxis.cornerstone.service.QueueSender#send(REQ, javax.jms.MessageListener, com.paxxis.cornerstone.common.MessagePayload)
     */
    @Override
    public synchronized <REQ extends RequestMessage,
                        RESP extends ResponseMessage<REQ>> ResponsePromise<RESP> send(
            REQ msg,
            MessageListener listener,
            Class<RESP> clazz) {
        ResponsePromise<RESP> promise = new ResponsePromise<RESP>(this.timeout);
        send(msg, promise, listener, clazz);
        return promise;
    }

    /**
     * Prepare a message for sending.
     *
     */
    protected Message prepareMessage(
            RequestMessage requestMessage) {

        Message message = super.prepareMessage(requestMessage, payloadType);
        if (responseQueue == null) {
            setup();
        }
        message.setReplyTo(responseQueue);
        message.setCorrelationID(generateCorrelationId());
        return message;
    }

    private String generateCorrelationId() {
        return String.valueOf(correlationId.incrementAndGet());
    }
}
