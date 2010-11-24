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

package com.paxxis.chime.server;

import java.util.ArrayList;
import java.util.Hashtable;

import com.paxxis.cornerstone.common.DataLatch;
import com.paxxis.cornerstone.service.CornerstoneConfiguration;
import com.paxxis.cornerstone.service.JndiInitialContextFactory;
import com.paxxis.cornerstone.service.RequestQueueSender;
import com.paxxis.cornerstone.service.ServiceBusConnector;

/**
 *
 * @author Robert Englander
 */
public class ServiceBusSenderPool
{
    public class PoolEntry
    {
        RequestQueueSender sender;
        ServiceBusConnector connector;

        public PoolEntry(ServiceBusConnector connector, RequestQueueSender sender)
        {
            this.connector = connector;
            this.sender = sender;
        }

        public RequestQueueSender getSender() {
            return sender;
        }
    }

    // free pool
    private ArrayList<PoolEntry> _freePool = new ArrayList<PoolEntry>();

    // assigned connections
    private Hashtable<PoolEntry, Object> _activePool = new Hashtable<PoolEntry, Object>();

    // borrowers waiting for instances to free up
    private ArrayList<DataLatch> _borrowersInWaiting = new ArrayList<DataLatch>();

    // the semaphore for protected the pools
    final private Object _semaphore = new Object();

    private int poolSize;
    private CornerstoneConfiguration config;
    private JndiInitialContextFactory contextFactory;
    
    public ServiceBusSenderPool() {
    	
    }

    public void setPoolSize(int size) {
    	poolSize = size;
    }

    public void setCornerstoneConfiguration(CornerstoneConfiguration cfg) {
    	config = cfg;
    }
    
    public void setContextFactory(JndiInitialContextFactory factory) {
    	contextFactory = factory;
    }
    
    /**
     * TODO make sure the connection is still open before returning
     * it to the caller.  it may have been closed.  if so, it needs to
     * be reconnected.
     *
     * @param borrower
     * @return
     */
    public PoolEntry borrowInstance(Object borrower)
    {
        PoolEntry entry = null;

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
            _borrowersInWaiting.add(latch);
            entry = (PoolEntry)latch.waitForObject();
        }

        return entry;
    }

    public void shutdown() {
        for (int i = 0; i < poolSize; i++) {
            borrowInstance(this).connector.disconnect();
        }
    }

    public void returnInstance(PoolEntry entry, Object borrower)
    {
        DataLatch notifyMonitor = null;

        synchronized (_semaphore)
        {
            // take this one off the active pool
            _activePool.remove(entry);

            if (_borrowersInWaiting.size() > 0)
            {
                // give this one to the next borrower
                notifyMonitor = _borrowersInWaiting.remove(0);
                _activePool.put(entry, borrower);
            }
            else
            {
                // put it back into the free pool
                _freePool.add(entry);
            }
        }

        if (notifyMonitor != null)
        {
            notifyMonitor.setObject(entry);
        }
    }

    public void initialize() {
    	String factoryName = config.getStringValue("chime.service.connectionFactoryName", "");
    	String requestQueueName = config.getStringValue("chime.service.requestQueueName", "");
    	
        for (int i = 0; i < poolSize; i++)
        {
            ServiceBusConnector connector = new ServiceBusConnector();
            RequestQueueSender sender = new RequestQueueSender();
            connector.addServiceBusConnectorClient(sender);
            connector.setInitialContextFactory(contextFactory);
            connector.setConnectionFactoryName(factoryName);
            sender.setRequestQueueName(requestQueueName);
            connector.connect();
            PoolEntry entry = new PoolEntry(connector, sender);
            _freePool.add(entry);
        }
    }

}
