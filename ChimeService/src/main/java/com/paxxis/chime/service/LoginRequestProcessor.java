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

import com.paxxis.chime.data.CacheManager;
import com.paxxis.chime.data.UserUtils;
import com.mysql.jdbc.CommunicationsException;
import com.paxxis.chime.client.common.LoginRequest;
import com.paxxis.chime.client.common.LoginResponse;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.database.DatabaseConnection;
import com.paxxis.chime.database.DatabaseConnectionPool;
import com.paxxis.chime.client.common.ErrorMessage;
import com.paxxis.chime.client.common.Message;
import com.paxxis.chime.common.MessagePayload;
import com.paxxis.chime.service.MessageProcessor;
import org.apache.log4j.Logger;
/**
 *
 * @author Robert Englander
 */
public class LoginRequestProcessor extends MessageProcessor {
    private static final Logger _logger = Logger.getLogger(LoginRequestProcessor.class);

    // the database connection pool
    DatabaseConnectionPool _pool;
    
    /**
     * Constructor
     *
     */
    public LoginRequestProcessor(MessagePayload payloadType, DatabaseConnectionPool pool)
    {
        super(payloadType);
        _pool = pool;
    }
    
    protected Message process(boolean ignorePreviousChanges)
    {
        Object payload = getPayload();
        LoginRequest requestMessage = (LoginRequest)new LoginRequest().createInstance(payload);
        
        // build up a response
        Message response = null;
        DatabaseConnection database = _pool.borrowInstance(this);
        
        boolean tryAgain = true;
        boolean retried = false;
        
        while (tryAgain)
        {
            tryAgain = false;
            LoginResponse lr = new LoginResponse();
            lr.setRequest(requestMessage);
            response = lr;
            
            try
            {
                User user = requestMessage.getUser();
                if (user != null)
                {
                    // look for the match in the cache
                    user = CacheManager.instance().getUserSession(user);
                    
                    // if the response is null this is perfectly valid.  it means
                    // that the session doesn't exist.  if that's the case we want to
                    // return a LoginResponse with a null user, indicating to the caller
                    // that the session doesn't exist.
                }
                else
                {
                    String userName = requestMessage.getUserName();
                    String password = requestMessage.getPassword();

                    user = UserUtils.getUserByName(userName, null, database);

                    if (user == null || !password.equals(user.getPassword())) {
                        throw new Exception("The user name and/or password are not valid.");
                    }

                    user.setSessionToken(generateSessionToken(user));

                    // put this user into the session cache
                    CacheManager.instance().putUserSession(user);
                }
                
                lr.setUser(user);
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
    
    //////////////////////////////////////////////////////
    
    private static String generateSessionToken(User user) throws Exception
    {
        long hashCode = System.currentTimeMillis();
        String token = Long.toHexString(hashCode);
        _logger.info("Generated session token: " + token + " for user " + user.getName());
        return token;
    }
    
    
}