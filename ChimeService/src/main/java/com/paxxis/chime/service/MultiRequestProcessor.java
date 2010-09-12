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

import com.paxxis.chime.client.common.ErrorMessage;
import com.mysql.jdbc.CommunicationsException;
import com.paxxis.chime.client.common.MultiRequest;
import com.paxxis.chime.client.common.MultiRequestError;
import com.paxxis.chime.client.common.MultiResponse;
import com.paxxis.chime.client.common.RequestMessage;
import com.paxxis.chime.client.common.ResponseMessage;
import com.paxxis.chime.database.DatabaseConnection;
import com.paxxis.chime.database.DatabaseConnectionPool;
import com.paxxis.chime.database.DatabaseException;
import com.paxxis.chime.client.common.Message;
import com.paxxis.chime.service.MessageProcessor;
import com.paxxis.chime.client.common.MessageConstants;
import com.paxxis.chime.common.MessagePayload;
import java.util.List;

/** 
 *
 * @author Robert Englander
 */
public class MultiRequestProcessor extends MessageProcessor
{
    class ErrorMessageException extends Exception {
        private ErrorMessage error;
        public ErrorMessageException(ErrorMessage msg) {
            error = msg;
        }

        public ErrorMessage getErrorMessage() {
            return error;
        }
    }

    // the database connection pool
    DatabaseConnectionPool _pool;
    RequestMessageHandler _messageHandler = null;
    
    /**
     * Constructor
     *
     */
    public MultiRequestProcessor(MessagePayload payloadType, DatabaseConnectionPool pool)
    {
        super(payloadType);
        _pool = pool;
    }
    
    public void setMessageHandler(RequestMessageHandler handler)
    {
        _messageHandler = handler;
    }
    
    protected Message process(boolean ignorePreviousChanges)
    {
        Object payload = getPayload();
        MultiRequest requestMessage = (MultiRequest)new MultiRequest().createInstance(payload);
        
        // build up a response
        Message response = null;
        DatabaseConnection database = _pool.borrowInstance(this);
        
        boolean tryAgain = true;
        boolean retried = false;
        long correlator = -1;
        
        while (tryAgain)
        {
            tryAgain = false;
            MultiResponse dtr = new MultiResponse();
            dtr.setRequest(requestMessage);
            response = dtr;
            
            try
            {
                database.startTransaction();
                
                // execute each request

                boolean ignoreChange = false;
                List<RequestMessage> requests = requestMessage.getRequests();
                for (RequestMessage request : requests)
                {
                    correlator = request.getCorrelator();
                    MessageProcessor proc = _messageHandler.getProcessor(request.getMessageType().getValue(), request.getMessageVersion(),
                            MessageConstants.PayloadType.JavaObjectPayload.getValue());
                    Message resp = proc.execute(request, ignoreChange);
                    ignoreChange = true;

                    if (resp instanceof ErrorMessage) {
                        throw new ErrorMessageException((ErrorMessage)resp);
                    }
                    
                    dtr.addPair(request, (ResponseMessage)resp);
                }
                
                database.commitTransaction();
            }
            catch (ErrorMessageException e) {
                MultiRequestError em = new MultiRequestError();
                em.setCorrelator(correlator);
                em.setMessage(e.getErrorMessage().getMessage());
                em.setType(e.getErrorMessage().getType());
                response = em;
            }
            catch (Exception e)
            {
                if (e.getCause() instanceof CommunicationsException) {
                    if (!retried && !database.isConnected()) {
                        retried = true;
                        tryAgain = true;
                        
                        _pool.connect(database);
                    }
                } else {
                    MultiRequestError em = new MultiRequestError();
                    em.setCorrelator(correlator);
                    em.setMessage(e.getMessage());
                    response = em;
                }
            }
            finally {
                try
                {
                    database.rollbackTransaction();
                }
                catch (DatabaseException dbe)
                {

                }
            }
        }

        _pool.returnInstance(database, this);
        
        return response;
    }
    
}
