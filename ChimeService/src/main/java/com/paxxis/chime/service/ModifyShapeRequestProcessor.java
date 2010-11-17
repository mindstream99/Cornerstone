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
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceEvent;
import com.paxxis.chime.client.common.ErrorMessage;
import com.paxxis.chime.client.common.Message;
import com.paxxis.chime.client.common.ModifyShapeRequest;
import com.paxxis.chime.client.common.ModifyShapeResponse;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.DataInstanceEvent.EventType;
import com.paxxis.chime.common.MessagePayload;
import com.paxxis.chime.data.DataInstanceUtils;
import com.paxxis.chime.data.ShapeUtils;
import com.paxxis.chime.database.DatabaseConnection;
import com.paxxis.chime.database.DatabaseConnectionPool;

/**
 * 
 * @author Robert Englander
 *
 */
public class ModifyShapeRequestProcessor extends MessageProcessor {
    private static final Logger _logger = Logger.getLogger(ModifyShapeRequestProcessor.class);

    // the database connection pool
    private DatabaseConnectionPool _pool;
    
    private NotificationTopicSender _topicSender;

    /**
     * Constructor
     *
     */
    public ModifyShapeRequestProcessor(MessagePayload payloadType, DatabaseConnectionPool pool, NotificationTopicSender sender)
    {
        super(payloadType);
        _pool = pool;
        _topicSender = sender;
    }
    
    protected Message process(boolean ignorePreviousChanges)
    {
        Object payload = getPayload();
        ModifyShapeRequest requestMessage = (ModifyShapeRequest)new ModifyShapeRequest().createInstance(payload);
        
        // build up a response
        Message response = null;
        DatabaseConnection database = _pool.borrowInstance(this);
        
        boolean tryAgain = true;
        boolean retried = false;
        
        while (tryAgain)
        {
            tryAgain = false;
            ModifyShapeResponse dtr = new ModifyShapeResponse();
            dtr.setRequest(requestMessage);
            response = dtr;

            try
            {
                Tools.validateUser(requestMessage.getUser());

                DataInstanceEvent event = new DataInstanceEvent();
                
                if (requestMessage.getType() == ModifyShapeRequest.Type.ModifyFields) {
                    long start = System.currentTimeMillis(); 
                    Shape shape = ShapeUtils.updateFields(requestMessage.getShape(), requestMessage.getUser(), database);

                    DataInstance inst = DataInstanceUtils.getInstance(shape.getId(), requestMessage.getUser(), database, true, false);
                    dtr.setShape((Shape)inst);
                    
                    event.setEventType(EventType.Modify);
                    event.setDataInstance(inst);

                    long end = System.currentTimeMillis();
                    _logger.info("Shape modified in " + (end - start) + " msecs");
    
                }
                
                _topicSender.send(new ServiceBusMessageProducer(event), getPayloadType());
                
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
