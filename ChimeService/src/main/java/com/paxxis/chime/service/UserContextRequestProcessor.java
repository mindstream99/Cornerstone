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

import com.paxxis.chime.data.TagUtils;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.client.common.UserContextRequest;
import com.paxxis.chime.client.common.UserContextResponse;
import com.paxxis.chime.client.common.UserSocialContext;
import com.paxxis.chime.database.DatabaseConnection;
import com.paxxis.chime.database.DatabaseConnectionPool;
import com.paxxis.chime.client.common.ErrorMessage;
import com.paxxis.chime.client.common.Message;
import com.paxxis.chime.service.MessageProcessor;
import com.mysql.jdbc.CommunicationsException;
import com.paxxis.chime.common.MessagePayload;
import org.apache.log4j.Logger;


/**
 *
 * @author Robert Englander
 */
public class UserContextRequestProcessor extends MessageProcessor {
    private static final Logger _logger = Logger.getLogger(UserContextRequestProcessor.class);

    // the database connection pool
    DatabaseConnectionPool _pool;
    
    /**
     * Constructor
     *
     */
    public UserContextRequestProcessor(MessagePayload payloadType, DatabaseConnectionPool pool)
    {
        super(payloadType);
        _pool = pool;
    }
    
    protected Message process(boolean ignorePreviousChanges)
    {
        Object payload = getPayload();
        UserContextRequest requestMessage = (UserContextRequest)new UserContextRequest().createInstance(payload);
        
        // build up a response
        Message response = null;
        DatabaseConnection database = _pool.borrowInstance(this);
        
        boolean tryAgain = true;
        boolean retried = false;
        
        while (tryAgain)
        {
            tryAgain = false;
            UserContextResponse dtr = new UserContextResponse();
            dtr.setRequest(requestMessage);
            response = dtr;
            
            try
            {
                User user = requestMessage.getUser();
                Tools.validateUser(user);

                UserSocialContext context = new UserSocialContext();
                context.setUser(user);

                long start = System.currentTimeMillis();
                context.setUserTagContexts(TagUtils.getTagContexts(context.getUser(), database));
                long end = System.currentTimeMillis();
                _logger.info("User context retrieved in " + (end - start) + " msecs");
                
                dtr.setUserSocialContext(context);
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
