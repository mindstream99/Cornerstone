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

import com.paxxis.chime.data.CommunityUtils;
import com.paxxis.chime.client.common.Community;
import com.paxxis.chime.client.common.DataInstanceEvent;
import com.paxxis.chime.client.common.DataInstanceEvent.EventType;
import com.paxxis.chime.client.common.EditCommunityRequest;
import com.paxxis.chime.client.common.EditCommunityResponse;
import com.paxxis.chime.client.common.User;
import com.mysql.jdbc.CommunicationsException;
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
public class EditCommunityRequestProcessor extends MessageProcessor {
    private static final Logger _logger = Logger.getLogger(EditCommunityRequestProcessor.class);

    // the database connection pool
    private DatabaseConnectionPool _pool;

    private NotificationTopicSender _topicSender;

    /**
     * Constructor
     *
     */
    public EditCommunityRequestProcessor(MessagePayload payloadType, DatabaseConnectionPool pool, NotificationTopicSender sender)
    {
        super(payloadType);
        _pool = pool;
        _topicSender = sender;
    }

    protected Message process(boolean ignorePreviousChanges)
    {
        Object payload = getPayload();
        EditCommunityRequest requestMessage = (EditCommunityRequest)new EditCommunityRequest().createInstance(payload);

        // build up a response
        Message response = null;
        DatabaseConnection database = _pool.borrowInstance(this);

        boolean tryAgain = true;
        boolean retried = false;

        while (tryAgain)
        {
            tryAgain = false;
            EditCommunityResponse dtr = new EditCommunityResponse();
            dtr.setRequest(requestMessage);
            response = dtr;

            try
            {
                DataInstanceEvent event = new DataInstanceEvent();

                User user = requestMessage.getUser();
                Tools.validateUser(user);

                if (requestMessage.getOperation() == EditCommunityRequest.Operation.Create)
                {
                    long start = System.currentTimeMillis();
                    Community newCommunity = CommunityUtils.createCommunity(requestMessage.getName(),
                                            requestMessage.getDescription(), user, database);
                    long end = System.currentTimeMillis();
                    _logger.info("Community created in " + (end - start) + " msecs");

                    event.setEventType(EventType.Create);
                    event.setDataInstance(newCommunity);

                    dtr.setCommunity(newCommunity);
                }

                // remove this instance from the cache.  this is kind of lazy.  overall we should
                // be reworking all writes to update the cache with the new data instead of just
                // trashing it.
                //CacheManager.instance().remove(dtr.getDataInstance());

                event.setUser(user);
                _topicSender.send(new ServiceBusMessageProducer(event), getPayloadType());
            }
            catch (SessionValidationException sve)
            {
                ErrorMessage em = new ErrorMessage();
                em.setRequest(requestMessage);
                em.setMessage(sve.getMessage());
                em.setType(ErrorMessage.Type.SessionExpiration);
                response = em;
            }
            catch (StaleDataEditException sve)
            {
                ErrorMessage em = new ErrorMessage();
                em.setRequest(requestMessage);
                em.setMessage(sve.getMessage());
                em.setType(ErrorMessage.Type.StaleDataEdit);
                response = em;
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
