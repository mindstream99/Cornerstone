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

import com.paxxis.chime.data.DataInstanceUtils;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceEvent;
import com.paxxis.chime.client.common.DataInstanceEvent.EventType;
import com.paxxis.chime.client.common.LockRequest;
import com.paxxis.chime.client.common.LockResponse;
import com.paxxis.chime.client.common.User;
import com.mysql.jdbc.CommunicationsException;
import com.paxxis.chime.data.LockUtils;
import com.paxxis.cornerstone.base.ErrorMessage;
import com.paxxis.cornerstone.base.Message;
import com.paxxis.cornerstone.common.MessagePayload;
import com.paxxis.cornerstone.database.DatabaseConnection;
import com.paxxis.cornerstone.database.DatabaseConnectionPool;
import com.paxxis.cornerstone.service.MessageProcessor;
import com.paxxis.cornerstone.service.NotificationTopicSender;
import com.paxxis.cornerstone.service.ServiceBusMessageProducer;

import org.apache.log4j.Logger;

/**
 *
 * @author Robert Englander
 */
public class LockRequestProcessor  extends MessageProcessor {
    private static final Logger _logger = Logger.getLogger(LockRequestProcessor.class);

    // the database connection pool
    DatabaseConnectionPool _pool;

    NotificationTopicSender _topicSender;

    /**
     * Constructor
     *
     */
    public LockRequestProcessor(MessagePayload payloadType, DatabaseConnectionPool pool, NotificationTopicSender sender)
    {
        super(payloadType);
        _pool = pool;
        _topicSender = sender;
    }

    protected Message process(boolean ignorePreviousChanges)
    {
        Object payload = getPayload();
        LockRequest requestMessage = (LockRequest)new LockRequest().createInstance(payload);

        // build up a response
        Message response = null;
        DatabaseConnection database = _pool.borrowInstance(this);

        boolean tryAgain = true;
        boolean retried = false;

        while (tryAgain)
        {
            tryAgain = false;
            LockResponse dtr = new LockResponse();
            dtr.setRequest(requestMessage);
            response = dtr;

            try
            {
                User user = requestMessage.getUser();
                Tools.validateUser(user);

                DataInstance.LockType lockType = requestMessage.getLockType();
                DataInstance instance = DataInstanceUtils.getInstance(requestMessage.getData().getId(),
                        requestMessage.getUser(), database, false, true);

                if (lockType != DataInstance.LockType.NONE) {
                    if (instance.getLockType() != DataInstance.LockType.NONE) {
                        if (!user.getId().equals(instance.getLockedBy().getId())) {
                            throw new BadLockException("The data is already locked by another user or workflow.");
                        }
                    }
                } else {
                    if (instance.getLockType() == DataInstance.LockType.EDIT) {
                        if (!user.getId().equals(instance.getLockedBy().getId())) {
                            throw new BadLockException("The data is locked by another user.");
                        }
                    }
                }

                long start = System.currentTimeMillis();

                instance = LockUtils.applyLock(requestMessage.getData(),
                        requestMessage.getLockType(), user, database);
                long end = System.currentTimeMillis();
                _logger.info("Lock updated in " + (end - start) + " msecs");

                dtr.setDataInstance(instance);

                DataInstanceEvent event = new DataInstanceEvent();
                event.setDataInstance(instance);
                event.setEventType(EventType.Modify);
                event.setUser(user);

                _topicSender.send(new ServiceBusMessageProducer(event), getPayloadType());

                // remove this instance from the cache.  this is kind of lazy.  overall we should
                // be reworking all writes to update the cache with the new data instead of just
                // trashing it.
                //CacheManager.instance().remove(dtr.getDataInstance());
            }
            catch (Exception e)
            {
                if (e.getCause() instanceof CommunicationsException)
                {
                    if (!retried && !database.isConnected())
                    {
                        retried = true;
                        tryAgain = true;

                        _pool.connect(database);
                    }
                }
                else
                {
                    ErrorMessage em = new ErrorMessage();
                    em.setMessage(e.getMessage());
                    response = em;
                }
            }
        }

        _pool.returnInstance(database, this);

        return response;
    }

}
