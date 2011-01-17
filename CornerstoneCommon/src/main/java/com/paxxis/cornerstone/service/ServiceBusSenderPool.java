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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Hashtable;

import com.paxxis.cornerstone.common.DataLatch;

/**
 *
 * @author Robert Englander
 */
public abstract class ServiceBusSenderPool<T extends DestinationSender> extends CornerstoneConfigurable
{
    public static class PoolEntry<T>
    {
        T sender;
        ServiceBusConnector connector;

        public PoolEntry(ServiceBusConnector connector, T sender)
        {
            this.connector = connector;
            this.sender = sender;
        }

        public T getSender() {
            return sender;
        }
    }

    private class WaitingBorrower
    {
        public DataLatch latch;
        public Object borrower;
        
        public WaitingBorrower(DataLatch latch, Object borrower)
        {
            this.latch = latch;
            this.borrower = borrower;
        }
    }
    
    // free pool
    private ArrayList<PoolEntry<T>> _freePool = new ArrayList<PoolEntry<T>>();

    // assigned connections
    private Hashtable<PoolEntry<T>, Object> _activePool = new Hashtable<PoolEntry<T>, Object>();

    // borrowers waiting for instances to free up
    private Deque<WaitingBorrower> _borrowersInWaiting = new ArrayDeque<WaitingBorrower>();

    // the semaphore for protected the pools
    final private Object _semaphore = new Object();

    private int poolSize = 10;
    private long borrowTimeout = 10000;
    private JndiInitialContextFactory contextFactory;
    private String connectionFactoryName;
    private String requestQueueName;

    
    public ServiceBusSenderPool() {
        
    }

    public void setPoolSize(int size) {
        poolSize = size;
    }

    public void setBorrowTimeout(long borrowTimeout) {
        this.borrowTimeout = borrowTimeout;
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

    /**
     * TODO make sure the connection is still open before returning
     * it to the caller.  it may have been closed.  if so, it needs to
     * be reconnected.
     *
     * @param borrower
     * @return
     */
    @SuppressWarnings("unchecked")
    public PoolEntry<T> borrowInstance(Object borrower)
    {
        PoolEntry<T> entry = null;

        synchronized (_semaphore)
        {
            if (!_freePool.isEmpty())
            {
                // there's a free entry, give it to this borrower.
                entry = _freePool.remove(0);
                _activePool.put(entry, borrower);
            }
        }

        if (entry == null)
        {
            // the borrower will have to wait until an instance
            // is returned by another borrower
            DataLatch latch = new DataLatch();
            _borrowersInWaiting.add(new WaitingBorrower(latch, borrower));
            entry = (PoolEntry<T>) latch.waitForObject(this.borrowTimeout);
            if (latch.hasTimedout()) {
                throw new RuntimeException("Timeout " + this.borrowTimeout + " reached waiting for pool entry");
            }
        }

        return entry;
    }

    public void shutdown() {
        for (int i = 0; i < poolSize; i++) {
            borrowInstance(this).connector.disconnect();
        }
    }

    /**
     * Return a borrowed pool entry
     * @param entry the entry to return
     * @param borrower NOT NEEDED???
     */
    public void returnInstance(PoolEntry<T> entry, Object borrower)
    {
        WaitingBorrower waiting = null;

        synchronized (_semaphore)
        {
            // take this one off the active pool
            _activePool.remove(entry);

            if (_borrowersInWaiting.size() > 0)
            {
                // give this one to the next borrower
                waiting = _borrowersInWaiting.removeFirst();
                _activePool.put(entry, waiting.borrower);
            }
            else
            {
                // put it back into the free pool
                _freePool.add(entry);
            }
        }

        if (waiting != null)
        {
            waiting.latch.setObject(entry);
        }
    }

    public void initialize() {
        for (int i = 0; i < poolSize; i++)
        {
            ServiceBusConnector connector = createConnector();
            T sender = createSender();
            connector.addServiceBusConnectorClient(sender);
            connector.setInitialContextFactory(contextFactory);
            connector.setConnectionFactoryName(this.connectionFactoryName);
            sender.setDestinationName(this.requestQueueName);
            connector.connect();
            PoolEntry<T> entry = new PoolEntry<T>(connector, sender);
            _freePool.add(entry);
        }
    }

    protected ServiceBusConnector createConnector() {
        return new ServiceBusConnector();
    }
    
    protected abstract T createSender();
    
}
