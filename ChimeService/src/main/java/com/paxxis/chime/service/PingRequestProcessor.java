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

import com.paxxis.chime.client.common.Cursor;
import com.paxxis.chime.data.CacheManager;
import com.paxxis.chime.data.DataInstanceUtils;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.ErrorMessage;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.Message;
import com.paxxis.chime.client.common.PingRequest;
import com.paxxis.chime.client.common.PingResponse;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.client.common.UserMessagesBundle;
import com.paxxis.chime.common.MessagePayload;
import com.paxxis.chime.data.UserMessageUtils;
import com.paxxis.chime.data.UserUtils;
import com.paxxis.chime.database.DatabaseConnection;
import com.paxxis.chime.database.DatabaseConnectionPool;
import org.apache.log4j.Logger;

/**
 *
 * @author Robert Englander
 */
public class PingRequestProcessor extends MessageProcessor {
    private static final Logger _logger = Logger.getLogger(PingRequestProcessor.class);

    // the database connection pool
    private DatabaseConnectionPool _pool;

    /**
     * Constructor
     *
     */
    public PingRequestProcessor(MessagePayload payloadType, DatabaseConnectionPool pool)
    {
        super(payloadType);
        _pool = pool;
    }
    
    protected Message process(boolean ignorePreviousChanges)
    {
        Object payload = getPayload();
        PingRequest requestMessage = (PingRequest)new PingRequest().createInstance(payload);
        DatabaseConnection database = _pool.borrowInstance(this);
        
        // build up a response
        Message response = null;
        
        boolean tryAgain = true;
        
        while (tryAgain)
        {
            tryAgain = false;
            PingResponse lr = new PingResponse();
            lr.setRequest(requestMessage);
            response = lr;
            
            try
            {
                User user = requestMessage.getUser();
                if (requestMessage.isSessionPing()) {
                    if (user != null) {
                        if (requestMessage.getUserActivity()) {
                            if (CacheManager.instance().isExpiringUserSession(user)) {
                                // saved :)
                                CacheManager.instance().putUserSession(user);
                                CacheManager.instance().removeExpiringUserSession(user);
                                _logger.info("Ping received for user " + user.getName() + ". Session valid: " + true);
                            } else {
                                boolean result = null != CacheManager.instance().getUserSession(user);
                                _logger.info("Ping received for user " + user.getName() + ". Session valid: " + result);
                                if (result == false) {
                                    lr.setExpired();
                                }
                            }
                        } else {
                            if (CacheManager.instance().isExpiringUserSession(user)) {
                                lr.setPendingTimeout(true);
                                _logger.info("Ping received for user " + user.getName() + ". Session Expiring");
                            }
                        }

                    UserMessagesBundle bundle = UserMessageUtils.getMessages(user, new Cursor(DataInstanceUtils.USRMSGLIMIT), database);
                    user.setUserMessagesBundle(bundle);
                    //user = UserUtils.getUserById(user.getId(), user, database);
                    lr.setUser(user);

                    }
                } else {
                    // if the message includes an active detail id, fetch the instance and return it in the response
                    InstanceId id = requestMessage.getActiveDetailInstanceId();
                    if (!id.equals(InstanceId.create("-1"))) {
                        DataInstance inst = DataInstanceUtils.getInstance(id, user, database, true, true);
                        lr.setActiveDetailInstance(inst);
                    }

                    id = requestMessage.getActivePortalInstanceId();
                    if (!id.equals(InstanceId.create("-1"))) {
                        DataInstance inst = DataInstanceUtils.getInstance(id, user, database, true, true);
                        lr.setActivePortalInstance(inst);
                    }

                    UserMessagesBundle bundle = UserMessageUtils.getMessages(user, new Cursor(DataInstanceUtils.USRMSGLIMIT), database);
                    user.setUserMessagesBundle(bundle);
                    //user = UserUtils.getUserById(user.getId(), user, database);
                    lr.setUser(user);
                    
                }
            }
            catch (Exception e)
            {
                _logger.error(e);
                ErrorMessage em = new ErrorMessage();
                em.setMessage(e.getMessage());
                response = em;
            }
        }

        _pool.returnInstance(database, this);

        return response;
    }
}

