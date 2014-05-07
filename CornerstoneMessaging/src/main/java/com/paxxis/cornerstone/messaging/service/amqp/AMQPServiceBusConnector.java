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

package com.paxxis.cornerstone.messaging.service.amqp;

import com.paxxis.cornerstone.messaging.common.Session;
import com.paxxis.cornerstone.messaging.common.amqp.AMQPSession;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ExceptionHandler;
import com.rabbitmq.client.impl.DefaultExceptionHandler;
import org.apache.log4j.Logger;

import java.io.IOException;


/**
 *
 * @author Matthew Pflueger
 */
public class AMQPServiceBusConnector extends com.paxxis.cornerstone.messaging.service.ServiceBusConnector {
    private static Logger logger = Logger.getLogger(AMQPServiceBusConnector.class.getName());

    private Connection connection;
    private AMQPSession session;

    private String exchangeName = "amq.headers";
    private String exchangeType = "headers";

    private String host = "localhost";
    private int port = 5672;

    private boolean autoRecover = true;
    private int timeout = 5000;
    private int recoveryInterval = 5000;
    private int heartbeat = 5000;
    private boolean autoTopologyRecover = true;

    private int maxMessagesInFlight = 10;

    private ExceptionHandler exceptionHandler = new DefaultExceptionHandler();


    public AMQPServiceBusConnector() {}

    public Session createSession() {
        try {
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(exchangeName, exchangeType, true);
            //max messages in flight per consumer (false means consumer, true means per channel)
            channel.basicQos(maxMessagesInFlight, false);
            return new AMQPSession(channel, exchangeName, getAcknowledgeMode() == Session.AUTO_ACKNOWLEDGE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Session getSession() {
        return session;
    }

    /**
     * Start the connection.
     */
    @Override
    protected void startConnection() {
        if (session == null) {
            session = (AMQPSession) createSession();
        }
    }

    @Override
    protected void stopConnection() {
        try {
            closeSession();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    /**
     * Determine if we're connected to the bus.
     *
     * @return true if connected (including disconnect pending), false otherwise.
     */
    public boolean isConnected() {
        return connection != null;
    }

    protected void initConnection() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            factory.setPort(port);
            factory.setAutomaticRecoveryEnabled(autoRecover);
            factory.setConnectionTimeout(timeout);
            factory.setNetworkRecoveryInterval(recoveryInterval);
            factory.setRequestedHeartbeat(heartbeat);
            factory.setTopologyRecoveryEnabled(autoTopologyRecover);
            factory.setExceptionHandler(exceptionHandler);

            connection = factory.newConnection();
            session = (AMQPSession) createSession();
        } catch (IOException e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }


    /**
     * Close the session
     */
    protected void closeSession() throws IOException {
        if (session != null) {
            session.getChannel().close();
            session = null;
        }
    }

    /**
     * Close the connection
     */
    protected void closeConnection() throws IOException {
        if (connection != null) {
            connection.close(10000);
            connection = null;
        }
    }

    protected void close() {
        try {
            closeSession();
            closeConnection();
        } catch (IOException e) {
            logger.error(e);
        }
    }


    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isAutoRecover() {
        return autoRecover;
    }

    public void setAutoRecover(boolean autoRecover) {
        this.autoRecover = autoRecover;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getRecoveryInterval() {
        return recoveryInterval;
    }

    public void setRecoveryInterval(int recoveryInterval) {
        this.recoveryInterval = recoveryInterval;
    }

    public int getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
    }

    public boolean isAutoTopologyRecover() {
        return autoTopologyRecover;
    }

    public void setAutoTopologyRecover(boolean autoTopologyRecover) {
        this.autoTopologyRecover = autoTopologyRecover;
    }

    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public int getMaxMessagesInFlight() {
        return maxMessagesInFlight;
    }

    public void setMaxMessagesInFlight(int maxMessagesInFlight) {
        this.maxMessagesInFlight = maxMessagesInFlight;
    }

    public String getExchangeType() {
        return exchangeType;
    }

    public void setExchangeType(String exchangeType) {
        this.exchangeType = exchangeType;
    }
}
