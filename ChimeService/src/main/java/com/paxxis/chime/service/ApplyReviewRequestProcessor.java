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

import com.paxxis.chime.data.ReviewUtils;
import com.paxxis.chime.data.DataInstanceUtils;
import com.mysql.jdbc.CommunicationsException;
import com.paxxis.chime.client.common.ApplyReviewRequest;
import com.paxxis.chime.client.common.ApplyReviewResponse;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceEvent;
import com.paxxis.chime.client.common.DataInstanceEvent.EventType;
import com.paxxis.chime.database.DatabaseConnection;
import com.paxxis.chime.database.DatabaseConnectionPool;
import com.paxxis.chime.client.common.ErrorMessage;
import com.paxxis.chime.client.common.Message;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.common.MessagePayload;
import com.paxxis.chime.service.MessageProcessor;
import com.paxxis.chime.service.NotificationTopicSender;
import com.paxxis.chime.service.ServiceBusMessageProducer;
import org.apache.log4j.Logger;

/**
 *
 * @author Robert Englander
 */
public class ApplyReviewRequestProcessor extends MessageProcessor {
    private static final Logger _logger = Logger.getLogger(ApplyReviewRequestProcessor.class);

    // the database connection pool
    DatabaseConnectionPool _pool;
    
    NotificationTopicSender _topicSender;

    /**
     * Constructor
     *
     */
    public ApplyReviewRequestProcessor(MessagePayload payloadType, DatabaseConnectionPool pool, NotificationTopicSender sender)
    {
        super(payloadType);
        _pool = pool;
        _topicSender = sender;
    }
    
    protected Message process(boolean ignorePreviousChanges)
    {
        Object payload = getPayload();
        ApplyReviewRequest requestMessage = (ApplyReviewRequest)new ApplyReviewRequest().createInstance(payload);
        
        // build up a response
        Message response = null;
        DatabaseConnection database = _pool.borrowInstance(this);
        
        boolean tryAgain = true;
        boolean retried = false;
        
        while (tryAgain)
        {
            tryAgain = false;
            ApplyReviewResponse dtr = new ApplyReviewResponse();
            dtr.setRequest(requestMessage);
            response = dtr;
            
            try
            {
                long start = System.currentTimeMillis();

                User user = requestMessage.getUser();
                Tools.validateUser(user);

                DataInstance instance = ReviewUtils.applyReview(requestMessage.getData(),
                        requestMessage.getReview(), user, database);
                long end = System.currentTimeMillis();
                _logger.info("Rating applied in " + (end - start) + " msecs");

                DataInstanceEvent event = new DataInstanceEvent();
                event.setDataInstance(instance);
                event.setEventType(EventType.Review);
                event.setUser(user);
                
                _topicSender.send(new ServiceBusMessageProducer(event), getPayloadType());

                event = new DataInstanceEvent();

                DataInstance review = instance.getSocialContext().getReviewsBundle().getReviews().get(0);
                review = DataInstanceUtils.getInstance(review.getId(), user, database, true, false);
                event.setDataInstance(review);
                event.setEventType(EventType.Create);
                event.setUser(user);
                _topicSender.send(new ServiceBusMessageProducer(event), getPayloadType());

                dtr.setDataInstance(instance);

                // remove this instance from the cache.  this is kind of lazy.  overall we should
                // be reworking all writes to update the cache with the new data instead of just
                // trashing it.
                //CacheManager.instance().remove(dtr.getDataInstance());
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
