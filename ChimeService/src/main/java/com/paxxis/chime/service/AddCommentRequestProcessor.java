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
import com.paxxis.chime.client.common.AddCommentRequest;
import com.paxxis.chime.client.common.AddCommentResponse;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceEvent;
import com.paxxis.chime.client.common.ErrorMessage;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.Message;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.client.common.DataInstanceEvent.EventType;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.common.MessagePayload;
import com.paxxis.chime.data.CacheManager;
import com.paxxis.chime.data.CommentUtils;
import com.paxxis.chime.data.DataInstanceUtils;
import com.paxxis.chime.database.DatabaseConnection;
import com.paxxis.chime.database.DatabaseConnectionPool;

/**
 *
 * @author Robert Englander
 */
public class AddCommentRequestProcessor extends MessageProcessor {
    private static final Logger _logger = Logger.getLogger(AddCommentRequestProcessor.class);

    // the database connection pool
    DatabaseConnectionPool _pool;
    
    NotificationTopicSender _topicSender;

    /**
     * Constructor
     *
     */
    public AddCommentRequestProcessor(MessagePayload payloadType, DatabaseConnectionPool pool, NotificationTopicSender sender)
    {
        super(payloadType);
        _pool = pool;
        _topicSender = sender;
    }
    
    protected Message process(boolean ignorePreviousChanges)
    {
        Object payload = getPayload();
        AddCommentRequest requestMessage = (AddCommentRequest)new AddCommentRequest().createInstance(payload);
        
        // build up a response
        Message response = null;
        DatabaseConnection database = _pool.borrowInstance(this);
        
        boolean tryAgain = true;
        boolean retried = false;
        
        while (tryAgain)
        {
            tryAgain = false;
            AddCommentResponse dtr = new AddCommentResponse();
            dtr.setRequest(requestMessage);
            response = dtr;
            
            try
            {
                InstanceId instanceId = requestMessage.getData().getId();
                User user = requestMessage.getUser();

                Tools.validateUser(user);

                long start = System.currentTimeMillis();
                
                CommentUtils.addComment(requestMessage.getData(), user,
                             requestMessage.getComment().getDescription(), database);

                long end = System.currentTimeMillis();
                
                DataInstance instance = DataInstanceUtils.getInstance(requestMessage.getData().getId(),
                                            requestMessage.getUser(),
                                            database, true, false);
                
                _logger.info("Comment added in " + (end - start) + " msecs");

                dtr.setDataInstance(instance);

                DataInstanceEvent event = new DataInstanceEvent();
                event.setDataInstance(instance);
                event.setEventType(EventType.Comment);
                event.setUser(user);

                _topicSender.send(new ServiceBusMessageProducer(event), getPayloadType());
                
                // remove this instance from the cache.  this is kind of lazy.  overall we should
                // be reworking all writes to update the cache with the new data instead of just
                // trashing it.
                //CacheManager.instance().remove(dtr.getDataInstance());

                // if the instance is back referencing, we need to clear the back referenced instance
                // from the cache as well.  could we be updating the cached instance instead?
                if (instance.isBackReferencing() && !(instance instanceof Shape)) {
                    InstanceId backRef = instance.getBackRefId();
                    DataInstance back = CacheManager.instance().get(backRef);
                    if (back != null) {
                       // CacheManager.instance().remove(back);
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
