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

import javax.jms.DeliveryMode;

import com.paxxis.cornerstone.common.AbstractBlockingObjectPool;

/**
 *
 * @author Robert Englander
 */
public abstract class ServiceBusSenderPool<T extends DestinationSender> extends AbstractBlockingObjectPool<T>
{
    public static class PoolEntry<T> extends AbstractBlockingObjectPool.PoolEntry<T>
    {
        T sender;
        ServiceBusConnector connector;

        public PoolEntry(T sender, ServiceBusConnector connector)
        {
            super(sender);
            this.connector = connector;
        }

        public T getSender() {
            return getObject();
        }
        
        @Override
        public void shutdown() {
            this.connector.disconnect();
            super.shutdown();
        }
    }

    private JndiInitialContextFactory contextFactory;
    private String connectionFactoryName;
    private String requestQueueName;
    private int deliveryMode = DeliveryMode.NON_PERSISTENT;

    //FIXME this is just to keep similar apis
    public PoolEntry<T> borrowInstance(Object borrower) {
        return borrow(borrower);
    }

    public void setContextFactory(JndiInitialContextFactory factory) {
        contextFactory = factory;
    }
    
    /**
     * Gets the connectionFactoryName for this instance.
     *
     * @return The connectionFactoryName.
     */
    public String getConnectionFactoryName() {
        return this.connectionFactoryName;
    }

    /**
     * Sets the connectionFactoryName for this instance.
     *
     * @param connectionFactoryName The connectionFactoryName.
     */
    public void setConnectionFactoryName(String connectionFactoryName) {
        this.connectionFactoryName = connectionFactoryName;
    }

    /**
     * Gets the requestQueueName for this instance.
     *
     * @return The requestQueueName.
     */
    public String getRequestQueueName() {
        return this.requestQueueName;
    }

    /**
     * Sets the requestQueueName for this instance.
     *
     * @param requestQueueName The requestQueueName.
     */
    public void setRequestQueueName(String requestQueueName) {
        this.requestQueueName = requestQueueName;
    }

    public void setPersistentDelivery(boolean persistent) {
    	deliveryMode = (persistent ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT);
    }

    public boolean isPersistentDelivery() {
    	return deliveryMode == DeliveryMode.PERSISTENT;
    }

    @SuppressWarnings("unchecked")
    @Override
	protected PoolEntry<T> createPoolEntry() {
        ServiceBusConnector connector = createConnector();
        T sender = createSender();
        sender.setPersistentDelivery(isPersistentDelivery());
        
        connector.addServiceBusConnectorClient(sender);
        connector.setInitialContextFactory(contextFactory);
        connector.setConnectionFactoryName(this.connectionFactoryName);
        sender.setDestinationName(this.requestQueueName);
        connector.connect();
        return new PoolEntry<T>(sender, connector);
	}


    protected ServiceBusConnector createConnector() {
        return new ServiceBusConnector();
    }
    
    protected abstract T createSender();
    
}
