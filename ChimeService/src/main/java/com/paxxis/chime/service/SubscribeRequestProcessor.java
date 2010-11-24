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
import com.paxxis.chime.client.common.SubscribeRequest;
import com.paxxis.chime.client.common.SubscribeResponse;
import com.paxxis.chime.client.common.User;
import com.mysql.jdbc.CommunicationsException;
import com.paxxis.chime.data.RegisteredInterestUtils;
import com.paxxis.cornerstone.base.ErrorMessage;
import com.paxxis.cornerstone.base.Message;
import com.paxxis.cornerstone.common.MessagePayload;
import com.paxxis.cornerstone.database.DatabaseConnection;
import com.paxxis.cornerstone.database.DatabaseConnectionPool;
import com.paxxis.cornerstone.service.MessageProcessor;
import com.paxxis.cornerstone.service.NotificationTopicSender;

import org.apache.log4j.Logger;

/**
 *
 * @author Robert Englander
 */
public class SubscribeRequestProcessor extends MessageProcessor {
    private static final Logger _logger = Logger.getLogger(SubscribeRequestProcessor.class);

    // the database connection pool
    private DatabaseConnectionPool _pool;

    private NotificationTopicSender _topicSender;

    /**
     * Constructor
     *
     */
    public SubscribeRequestProcessor(MessagePayload payloadType, DatabaseConnectionPool pool, NotificationTopicSender sender)
    {
        super(payloadType);
        _pool = pool;
        _topicSender = sender;
    }

    protected Message process(boolean ignorePreviousChanges)
    {
        Object payload = getPayload();
        SubscribeRequest requestMessage = (SubscribeRequest)new SubscribeRequest().createInstance(payload);

        // build up a response
        Message response = null;
        DatabaseConnection database = _pool.borrowInstance(this);

        boolean tryAgain = true;
        boolean retried = false;

        while (tryAgain)
        {
            tryAgain = false;
            SubscribeResponse dtr = new SubscribeResponse();
            dtr.setRequest(requestMessage);
            response = dtr;

            try
            {
                User user = requestMessage.getUser();
                Tools.validateUser(user);

                boolean subscribe = requestMessage.isSubscribe();
                DataInstance inst = requestMessage.getData();

                long start = System.currentTimeMillis();

                RegisteredInterestUtils.registerInterest(inst, user, subscribe, database);

                inst = DataInstanceUtils.getInstance(inst.getId(), user, database, true, true);

                long end = System.currentTimeMillis();

                _logger.info("Data instance subscription updated in " + (end - start) + " msecs");

                dtr.setDataInstance(inst);
            }
            catch (Exception e)
            {
                _logger.error(e);
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
