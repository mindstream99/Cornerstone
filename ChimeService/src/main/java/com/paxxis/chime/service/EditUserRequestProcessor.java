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

import org.apache.log4j.Logger;

import com.mysql.jdbc.CommunicationsException;
import com.paxxis.chime.client.common.DataInstanceEvent;
import com.paxxis.chime.client.common.EditUserRequest;
import com.paxxis.chime.client.common.EditUserResponse;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.client.common.DataInstanceEvent.EventType;
import com.paxxis.chime.data.UserUtils;
import com.paxxis.cornerstone.base.ErrorMessage;
import com.paxxis.cornerstone.base.Message;
import com.paxxis.cornerstone.common.MessagePayload;
import com.paxxis.cornerstone.database.DatabaseConnection;
import com.paxxis.cornerstone.database.DatabaseConnectionPool;
import com.paxxis.cornerstone.service.MessageProcessor;
import com.paxxis.cornerstone.service.NotificationTopicSender;
import com.paxxis.cornerstone.service.ServiceBusMessageProducer;

/**
 *
 * @author Robert Englander
 */
public class EditUserRequestProcessor extends MessageProcessor {
    private static final Logger _logger = Logger.getLogger(EditUserRequestProcessor.class);

    // the database connection pool
    DatabaseConnectionPool _pool;

    NotificationTopicSender _topicSender;

    /**
     * Constructor
     *
     */
    public EditUserRequestProcessor(MessagePayload payloadType, DatabaseConnectionPool pool, NotificationTopicSender sender)
    {
        super(payloadType);
        _pool = pool;
        _topicSender = sender;
    }

    protected Message process(boolean ignorePreviousChanges)
    {
        Object payload = getPayload();
        EditUserRequest requestMessage = (EditUserRequest)new EditUserRequest().createInstance(payload);

        // build up a response
        Message response = null;
        DatabaseConnection database = _pool.borrowInstance(this);

        boolean tryAgain = true;
        boolean retried = false;

        while (tryAgain)
        {
            tryAgain = false;
            EditUserResponse dtr = new EditUserResponse();
            dtr.setRequest(requestMessage);
            response = dtr;

            try
            {
                DataInstanceEvent event = new DataInstanceEvent();

                User user = requestMessage.getUser();
                Tools.validateUser(user);

                if (requestMessage.getOperation() == EditUserRequest.Operation.Create)
                {
                    long start = System.currentTimeMillis();
                    User newUser = UserUtils.createUser(requestMessage.getName(), requestMessage.getLoginId(),
                                            requestMessage.getDescription(), requestMessage.getPassword(), user, database);
                    long end = System.currentTimeMillis();
                    _logger.info("User created in " + (end - start) + " msecs");

                    event.setEventType(EventType.Create);
                    event.setDataInstance(newUser);

                    dtr.setUser(newUser);
                }
                else if (requestMessage.getOperation() == EditUserRequest.Operation.Modify)
                {
                    // this operation is used to modify the user's password
                    long start = System.currentTimeMillis();
                    User newUser = UserUtils.changePassword(user, requestMessage.getData().getId(),
                                            requestMessage.getOldPassword(), requestMessage.getPassword(), database);
                    long end = System.currentTimeMillis();
                    _logger.info("Password changed in " + (end - start) + " msecs");

                    event.setEventType(EventType.Modify);
                    event.setDataInstance(newUser);

                    dtr.setUser(newUser);
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
