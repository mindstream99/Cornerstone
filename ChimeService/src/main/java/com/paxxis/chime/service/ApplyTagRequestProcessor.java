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
import com.paxxis.chime.data.DataInstanceUtils;
import com.mysql.jdbc.CommunicationsException;
import com.paxxis.chime.client.common.ApplyTagRequest; 
import com.paxxis.chime.client.common.ApplyTagRequest.ApplyType;
import com.paxxis.chime.client.common.ApplyTagResponse;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceEvent;
import com.paxxis.chime.client.common.DataInstanceEvent.EventType;
import com.paxxis.chime.client.common.Tag;
import com.paxxis.chime.database.DatabaseConnection;
import com.paxxis.chime.database.DatabaseConnectionPool; 
import com.paxxis.chime.client.common.ErrorMessage;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.Message;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.common.MessagePayload;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Robert Englander
 */
public class ApplyTagRequestProcessor extends MessageProcessor {
    private static final Logger _logger = Logger.getLogger(ApplyTagRequestProcessor.class);

    // the database connection pool
    DatabaseConnectionPool _pool;
    
    NotificationTopicSender _topicSender;

    /**
     * Constructor
     *
     */
    public ApplyTagRequestProcessor(MessagePayload payloadType, DatabaseConnectionPool pool, NotificationTopicSender sender)
    {
        super(payloadType);
        _pool = pool;
        _topicSender = sender;
    }
    
    protected Message process(boolean ignorePreviousChanges)
    {
        Object payload = getPayload();
        ApplyTagRequest requestMessage = (ApplyTagRequest)new ApplyTagRequest().createInstance(payload);
        
        // build up a response
        Message response = null;
        DatabaseConnection database = _pool.borrowInstance(this);
        
        boolean tryAgain = true;
        boolean retried = false;
        
        while (tryAgain)
        {
            tryAgain = false;
            ApplyTagResponse dtr = new ApplyTagResponse();
            dtr.setRequest(requestMessage);
            response = dtr;
            
            try
            {
                InstanceId instanceId = requestMessage.getData().getId();
                long start = System.currentTimeMillis();
                
                User user = requestMessage.getUser();
                Tools.validateUser(user);

                ApplyTagsResponse tagsResponse;
                
                if (requestMessage.getApplyType() == ApplyType.Add)
                {
                    tagsResponse = TagUtils.applyTags(requestMessage.getData(), user,
                        requestMessage.getTags(), database);
                    long end = System.currentTimeMillis();
                    _logger.info("Tag(s) applied in " + (end - start) + " msecs");
                }
                else // ApplyType.Remove
                {
                    tagsResponse = TagUtils.removeTags(requestMessage.getData(), requestMessage.getUser(),
                        requestMessage.getTags(), database);
                    long end = System.currentTimeMillis();
                    _logger.info("Tag(s) removed in " + (end - start) + " msecs");
                }

                DataInstance instance = DataInstanceUtils.getInstance(instanceId,
                        requestMessage.getUser(),
                        database, true, false);
                dtr.setDataInstance(instance);
                
                if (tagsResponse.wasInstanceModified())
                {
                    // post a change event to the service bus
                    DataInstanceEvent event = new DataInstanceEvent();
                    event.setDataInstance(instance);
                    event.setEventType(EventType.Tag);
                    event.setUser(user);

                    _topicSender.send(new ServiceBusMessageProducer(event), getPayloadType());
                    
                    List<Tag> newTags = requestMessage.getTags(); //tagsResponse.getNewTags();
                    for (Tag tag : newTags)
                    {
                        event = new DataInstanceEvent();
                        event.setDataInstance(tag);
                        event.setEventType(EventType.Modify);
                        event.setUser(user);
                        _topicSender.send(new ServiceBusMessageProducer(event), getPayloadType());
                    }
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
