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
import com.mysql.jdbc.CommunicationsException;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.FindTagsRequest;
import com.paxxis.chime.client.common.FindTagsResponse;
import com.paxxis.chime.client.common.Tag;
import com.paxxis.chime.database.DatabaseConnection;
import com.paxxis.chime.database.DatabaseConnectionPool;
import com.paxxis.chime.client.common.ErrorMessage;
import com.paxxis.chime.client.common.Message;
import com.paxxis.chime.common.MessagePayload;
import com.paxxis.chime.service.MessageProcessor;
import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class FindTagsRequestProcessor extends MessageProcessor
{
    // the database connection pool
    DatabaseConnectionPool _pool;
    
    /**
     * Constructor
     *
     */
    public FindTagsRequestProcessor(MessagePayload payloadType, DatabaseConnectionPool pool)
    {
        super(payloadType);
        _pool = pool;
    }
    
    protected Message process(boolean ignorePreviousChanges)
    {
        Object payload = getPayload();
        FindTagsRequest requestMessage = (FindTagsRequest)new FindTagsRequest().createInstance(payload);
        
        // build up a response
        Message response = null;
        DatabaseConnection database = _pool.borrowInstance(this);
        
        boolean tryAgain = true;
        boolean retried = false;
        
        while (tryAgain)
        {
            tryAgain = false;
            FindTagsResponse dtr = new FindTagsResponse();
            dtr.setRequest(requestMessage);
            response = dtr;
            
            try 
            {
                List<String> strings = requestMessage.getStrings();
                List<Tag> list;
                Shape type = requestMessage.getShape();
                if (type == null)
                {
                    list = TagUtils.findTags(strings, database); 
                }
                else
                {
                    list = TagUtils.findAppliedTags(strings, type, database); 
                }
                
                dtr.setTags(list);
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
