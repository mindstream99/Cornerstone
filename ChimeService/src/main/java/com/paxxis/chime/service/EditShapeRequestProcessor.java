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
import com.paxxis.chime.data.DataInstanceUtils;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceEvent;
import com.paxxis.chime.client.common.DataInstanceEvent.EventType;
import com.mysql.jdbc.CommunicationsException;
import com.paxxis.chime.client.common.EditShapeRequest;
import com.paxxis.chime.client.common.EditShapeResponse;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.cornerstone.base.ErrorMessage;
import com.paxxis.cornerstone.base.Message;
import com.paxxis.cornerstone.common.MessagePayload;
import com.paxxis.cornerstone.database.DatabaseConnection;
import com.paxxis.cornerstone.database.DatabaseConnectionPool;
import com.paxxis.cornerstone.service.MessageProcessor;
import com.paxxis.cornerstone.service.NotificationTopicSender;
import com.paxxis.cornerstone.service.ServiceBusMessageProducer;

import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Robert Englander
 */
public class EditShapeRequestProcessor extends MessageProcessor {
    private static final Logger _logger = Logger.getLogger(EditShapeRequestProcessor.class);

    // the database connection pool
    DatabaseConnectionPool _pool;
    
    NotificationTopicSender _topicSender;

    /**
     * Constructor
     *
     */
    public EditShapeRequestProcessor(MessagePayload payloadType, DatabaseConnectionPool pool, NotificationTopicSender sender)
    {
        super(payloadType);
        _pool = pool;
        _topicSender = sender;
    }
    
    protected Message process(boolean ignorePreviousChanges)
    {
        Object payload = getPayload();
        EditShapeRequest requestMessage = (EditShapeRequest)new EditShapeRequest().createInstance(payload);
        
        // build up a response
        Message response = null;
        DatabaseConnection database = _pool.borrowInstance(this);
        
        boolean tryAgain = true;
        boolean retried = false;
        
        while (tryAgain)
        {
            tryAgain = false;
            EditShapeResponse dtr = new EditShapeResponse();
            dtr.setRequest(requestMessage);
            response = dtr;

            try
            {
                Tools.validateUser(requestMessage.getUser());

                DataInstanceEvent event = new DataInstanceEvent();
                
                if (requestMessage.getOperation() == EditShapeRequest.Operation.Create)
                {
                    long start = System.currentTimeMillis(); 
                    Shape shape = ShapeUtils.createInstance(requestMessage.getName(), requestMessage.getDescription(),
                                            requestMessage.getUser(), requestMessage.getFieldDefinitions(),
                                            requestMessage.isTabular(), requestMessage.getScopes(), database);

                    DataInstance inst = DataInstanceUtils.getInstance(shape.getId(), null, database, true, false);

                    List<DataField> fields = shape.getFields();
                    shape = (Shape)inst;
                    for (DataField field : fields) {
                        shape.addField(field);
                    }

                    dtr.setShape(shape);
                    
                    event.setEventType(EventType.NewType);
                    event.setDataInstance(inst);

                    long end = System.currentTimeMillis();
                    _logger.info("Shape created in " + (end - start) + " msecs");
    
                }
                else if (requestMessage.getOperation() == EditShapeRequest.Operation.AddFields)
                {
                    long start = System.currentTimeMillis();
                    Shape shape = ShapeUtils.addFields(requestMessage.getId(), requestMessage.getFieldDefinitions(), database);

                    DataInstance inst = DataInstanceUtils.getInstance(shape.getId(), requestMessage.getUser(), database, true, false);
                    dtr.setShape((Shape)inst);

                    event.setEventType(EventType.Modify);
                    event.setDataInstance(inst);
                    
                    long end = System.currentTimeMillis();
                    _logger.info("Shape fields added in " + (end - start) + " msecs");
                }
                else
                {
                    long start = System.currentTimeMillis();
                    Shape shape = ShapeUtils.removeFields(requestMessage.getId(), requestMessage.getFieldDefinitions(), database);
                    dtr.setShape(shape);

                    // this updates the cache
                    DataInstance inst = DataInstanceUtils.getInstance(shape.getId(), null, database, true, false);

                    event.setEventType(EventType.Modify);
                    event.setDataInstance(inst);

                    long end = System.currentTimeMillis();
                    _logger.info("Shape fields removed in " + (end - start) + " msecs");
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
