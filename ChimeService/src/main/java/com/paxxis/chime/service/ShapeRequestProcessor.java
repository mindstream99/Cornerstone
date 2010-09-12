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
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.ShapeRequest;
import com.paxxis.chime.client.common.ShapeResponse;
import com.paxxis.chime.database.DatabaseConnection;
import com.paxxis.chime.database.DatabaseConnectionPool;
import com.paxxis.chime.client.common.ErrorMessage;
import com.paxxis.chime.client.common.Message;
import com.mysql.jdbc.CommunicationsException;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.common.MessagePayload;


/**
 *
 * @author Robert Englander
 */
public class ShapeRequestProcessor extends MessageProcessor
{
    // the database connection pool
    DatabaseConnectionPool _pool;
    
    /**
     * Constructor
     *
     */
    public ShapeRequestProcessor(MessagePayload payloadType, DatabaseConnectionPool pool)
    {
        super(payloadType);
        _pool = pool;
    }
    
    protected Message process(boolean ignorePreviousChanges)
    {
        Object payload = getPayload();
        ShapeRequest requestMessage = (ShapeRequest)new ShapeRequest().createInstance(payload);
        
        // build up a response
        Message response = null;
        DatabaseConnection database = _pool.borrowInstance(this);
        
        boolean tryAgain = true;
        boolean retried = false;
        
        while (tryAgain)
        {
            tryAgain = false;
            ShapeResponse dtr = new ShapeResponse();
            dtr.setRequest(requestMessage);
            response = dtr;
            
            try {
                if (requestMessage.isByName()) {
                    String shapeName = requestMessage.getName();
                    Shape shape = ShapeUtils.getInstance(shapeName, database, true);
                    dtr.setShape(shape);
                } else {
                    InstanceId shapeId = requestMessage.getId();
                    Shape shape = ShapeUtils.getInstanceById(shapeId, database, true);
                    dtr.setShape(shape);
                }
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
