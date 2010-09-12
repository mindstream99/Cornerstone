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

import com.paxxis.chime.data.CommentUtils;
import com.paxxis.chime.client.common.CommentsBundle;
import com.paxxis.chime.client.common.CommentsRequest;
import com.paxxis.chime.client.common.CommentsResponse;
import com.paxxis.chime.client.common.Cursor;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceRequest.SortOrder;
import com.paxxis.chime.database.DatabaseConnection;
import com.paxxis.chime.database.DatabaseConnectionPool;
import com.paxxis.chime.client.common.ErrorMessage;
import com.paxxis.chime.client.common.Message;
import com.paxxis.chime.client.common.SearchFilter;
import com.paxxis.chime.client.common.User;
import com.mysql.jdbc.CommunicationsException;
import com.paxxis.chime.common.MessagePayload;
import org.apache.log4j.Logger;

/** 
 *
 * @author Robert Englander
 */
public class CommentsRequestProcessor extends MessageProcessor {
    private static final Logger _logger = Logger.getLogger(CommentsRequestProcessor.class);

    // the database connection pool
    DatabaseConnectionPool _pool;
    
    /**
     * Constructor
     *
     */
    public CommentsRequestProcessor(MessagePayload payloadType, DatabaseConnectionPool pool)
    {
        super(payloadType);
        _pool = pool;
    }
    
    protected Message process(boolean ignorePreviousChanges)
    {
        Object payload = getPayload();
        CommentsRequest requestMessage = (CommentsRequest)new CommentsRequest().createInstance(payload);
        User user = requestMessage.getUser();

        // build up a response
        Message response = null;
        DatabaseConnection database = _pool.borrowInstance(this);
        
        boolean tryAgain = true;
        boolean retried = false;
        
        while (tryAgain) 
        {
            tryAgain = false;
            CommentsResponse dtr = new CommentsResponse();
            dtr.setRequest(requestMessage);
            response = dtr;
            
            try
            {
                DataInstance dataInstance = requestMessage.getDataInstance();
                Cursor cursor = requestMessage.getCursor();
                SearchFilter filter = requestMessage.getFilter();
                SortOrder order = requestMessage.getSortOrder();
                
                long start = System.currentTimeMillis();

                CommentsBundle commentsBundle = CommentUtils.getComments(dataInstance.getId(), filter, cursor, order, database);
                
                long end = System.currentTimeMillis();
                _logger.info(commentsBundle.getComments().size() + " of " + commentsBundle.getCursor().getTotal() + " Comment instance(s) retrieved in " + (end - start) + " msecs");
                
                dtr.setComments(commentsBundle.getComments());
                dtr.setCursor(commentsBundle.getCursor());
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
