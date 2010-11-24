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

import com.paxxis.chime.data.ShapeUtils;
import com.mysql.jdbc.CommunicationsException;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceRequest;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.FindInstancesRequest;
import com.paxxis.chime.client.common.FindInstancesResponse;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.data.SearchUtils;
import com.paxxis.cornerstone.base.Cursor;
import com.paxxis.cornerstone.base.ErrorMessage;
import com.paxxis.cornerstone.base.Message;
import com.paxxis.cornerstone.common.MessagePayload;
import com.paxxis.cornerstone.database.DatabaseConnection;
import com.paxxis.cornerstone.database.DatabaseConnectionPool;
import com.paxxis.cornerstone.service.MessageProcessor;

import org.apache.log4j.Logger;

/**
 * 
 * @author Robert Englander
 */
public class FindInstancesRequestProcessor extends MessageProcessor {
    private static final Logger _logger = Logger.getLogger(FindInstancesRequestProcessor.class);

    // the database connection pool
    DatabaseConnectionPool _pool;
    
    /** 
     * Constructor
     *
     */
    public FindInstancesRequestProcessor(MessagePayload payloadType, DatabaseConnectionPool pool)
    {
        super(payloadType);
        _pool = pool;
    }
    
    protected Message process(boolean ignorePreviousChanges)
    {
        Object payload = getPayload();
        FindInstancesRequest requestMessage = (FindInstancesRequest)new FindInstancesRequest().createInstance(payload);
        
        // build up a response
        Message response = null;
        DatabaseConnection database = _pool.borrowInstance(this);
        
        boolean tryAgain = true;
        boolean retried = false;
        
        while (tryAgain)
        {
            tryAgain = false;
            FindInstancesResponse dtr = new FindInstancesResponse();
            dtr.setRequest(requestMessage);
            response = dtr;
            
            try
            {
                String string = requestMessage.getString();  
                Shape type = requestMessage.getShape();
                if (type == null)
                {
                    String typeName = requestMessage.getShapeName();
                    if (typeName != null) {
                        type = ShapeUtils.getInstance(typeName, database, true);
                    }
                }
                
                Shape appliedType = requestMessage.getAppliedShape();
                
                Cursor cursor = requestMessage.getCursor();
                
                User user = requestMessage.getUser();
                Tools.validateUser(user);
                
                Class instanceClass;
                if (type != null) {
                    instanceClass = Tools.getClass(type);
                } else {
                    instanceClass = DataInstance.class;
                }
                
                boolean userCreatedOnly = requestMessage.getUserCreated();
                boolean directCreatable = requestMessage.getDirectCreatable();

                long start = System.currentTimeMillis();

                InstancesResponse resp; 
                
                if (type != null && type.getName().equals("Tag") && appliedType != null)
                {
                    resp = SearchUtils.findAppliedTagInstances(appliedType, string, cursor, database);
                }
                else if (type != null && type.getName().equals("Shape"))
                {
                    boolean exclude = requestMessage.getExcludeInternals();
                    resp = SearchUtils.findInstancesByIndex(instanceClass, type, string, user, userCreatedOnly, directCreatable, cursor, DataInstanceRequest.SortOrder.ByName, exclude, database);
                }
                else
                {
                    resp = SearchUtils.findInstancesByIndex(instanceClass, type, string, user, userCreatedOnly, directCreatable, cursor, DataInstanceRequest.SortOrder.ByName, database);
                }
                
                long end = System.currentTimeMillis();
                _logger.info(resp.list.size() + " of " + resp.cursor.getTotal() + " Data instance(s) retrieved in " + (end - start) + " msecs");
                
                dtr.setDataInstances(resp.list);
                dtr.setCursor(resp.cursor);
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
