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
import com.paxxis.chime.client.common.Cursor;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceRequest.SortOrder;
import com.paxxis.chime.client.common.ReviewsBundle;
import com.paxxis.chime.database.DatabaseConnection;
import com.paxxis.chime.database.DatabaseConnectionPool;
import com.paxxis.chime.client.common.ErrorMessage;
import com.paxxis.chime.client.common.Message;
import com.paxxis.chime.client.common.ReviewsRequest;
import com.paxxis.chime.client.common.ReviewsResponse;
import com.paxxis.chime.client.common.SearchFilter;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.service.MessageProcessor;
import com.mysql.jdbc.CommunicationsException;
import com.paxxis.chime.common.MessagePayload;
import org.apache.log4j.Logger;

/**
 *
 * @author Robert Englander
 */
public class ReviewsRequestProcessor extends MessageProcessor {
    private static final Logger _logger = Logger.getLogger(ReviewsRequestProcessor.class);

    // the database connection pool
    DatabaseConnectionPool _pool;
    
    /**
     * Constructor
     *
     */
    public ReviewsRequestProcessor(MessagePayload payloadType, DatabaseConnectionPool pool)
    {
        super(payloadType);
        _pool = pool;
    }
    
    protected Message process(boolean ignorePreviousChanges)
    {
        Object payload = getPayload();
        ReviewsRequest requestMessage = (ReviewsRequest)new ReviewsRequest().createInstance(payload);

        User user = requestMessage.getUser();

        // build up a response
        Message response = null;
        DatabaseConnection database = _pool.borrowInstance(this);
        
        boolean tryAgain = true;
        boolean retried = false;
        
        while (tryAgain)
        {
            tryAgain = false;
            ReviewsResponse dtr = new ReviewsResponse();
            dtr.setRequest(requestMessage);
            response = dtr;
            
            try
            {
                DataInstance dataInstance = requestMessage.getDataInstance();
                Cursor cursor = requestMessage.getCursor();
                SearchFilter filter = requestMessage.getFilter();
                SortOrder sortOrder = requestMessage.getSortOrder();

                long start = System.currentTimeMillis();

                ReviewsBundle ratingsBundle = ReviewUtils.getReviews(dataInstance.getId(), filter, cursor, sortOrder, database);
                
                long end = System.currentTimeMillis();
                _logger.info(ratingsBundle.getReviews().size() + " of " + ratingsBundle.getCursor().getTotal() + " Review instance(s) retrieved in " + (end - start) + " msecs");
                
                dtr.setRatings(ratingsBundle.getReviews());
                dtr.setCursor(ratingsBundle.getCursor());
            }
            catch (Exception e)
            {
                _logger.error(e);
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
