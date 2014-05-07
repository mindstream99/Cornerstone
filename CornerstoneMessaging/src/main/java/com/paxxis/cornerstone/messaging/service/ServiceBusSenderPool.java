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
import com.paxxis.cornerstone.common.AbstractBlockingObjectPool;

/**
 *
 * @author Robert Englander
 */
public abstract class ServiceBusSenderPool<T extends DestinationSender>
        extends AbstractBlockingObjectPool<T>
        implements DestinationPublisher {

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

    public PoolEntry<T> borrowInstance(Object borrower) {
        return borrow(borrower);
    }

    @SuppressWarnings("unchecked")
    @Override
	protected PoolEntry<T> createPoolEntry() {
        ServiceBusConnector connector = createConnector();
        T sender = createSender();
        connector.addServiceBusConnectorClient(sender);
        connector.connect();
        return new PoolEntry<T>(sender, connector);
	}


    /* These methods are used by Spring to create connectors and senders when called */

    protected abstract ServiceBusConnector createConnector();

    protected abstract T createSender();

    @Override
    public <REQ extends RequestMessage> void publish(REQ msg) {
        PoolEntry<T> entry = this.borrow();
        try {
            entry.getSender().publish(msg);
        } finally {
            this.returnInstance(entry);
        }
    }


	@Override
	public <REQ extends RequestMessage> void publish(String dest, REQ msg) {
        PoolEntry<T> entry = this.borrow();
        try {
            entry.getSender().publish(dest, msg);
        } finally {
            this.returnInstance(entry);
        }
	}
}
