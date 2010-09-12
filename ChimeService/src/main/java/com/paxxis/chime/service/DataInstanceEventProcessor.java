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

import com.paxxis.chime.client.common.DataInstanceEvent;
import com.paxxis.chime.database.DatabaseConnectionPool;
import com.paxxis.chime.indexing.Indexer;
import com.paxxis.chime.client.common.ErrorMessage;
import com.paxxis.chime.client.common.Message;
import com.paxxis.chime.common.MessagePayload;
import com.paxxis.chime.notification.Notifier;
import com.paxxis.chime.service.MessageProcessor;
import org.apache.log4j.Logger;


/** 
 *
 * @author Robert Englander
 */
public class DataInstanceEventProcessor extends MessageProcessor {
    private static final Logger _logger = Logger.getLogger(DataInstanceEventProcessor.class);

    // the database connection pool
    DatabaseConnectionPool _pool;
    
    /**
     * Constructor
     *
     */
    public DataInstanceEventProcessor(MessagePayload payloadType, DatabaseConnectionPool pool)
    {
        super(payloadType);
        _pool = pool; 
    }
    
    protected Message process(boolean ignorePreviousChanges)
    {
        Object payload = getPayload();
        DataInstanceEvent eventMessage = (DataInstanceEvent)new DataInstanceEvent().createInstance(payload);
        
        // build up a response
        Message response = null;
        //DatabaseConnection database = _pool.borrowInstance(this);
        
        boolean tryAgain = true;
        boolean retried = false;
        
        _logger.info("Processing data instance event");
        while (tryAgain)
        {
            tryAgain = false;
           
            try
            {
                switch (eventMessage.getEventType())
                {
                    case Event:
                        Notifier.instance().process(eventMessage.getDataInstance(), eventMessage.getUser());
                        break;
                        
                    case BuildIndex:
                        Indexer.instance().rebuildIndex(eventMessage.getUser(), _pool);
                        break;

                    case NewType:
                        Indexer.instance().dataCreated(eventMessage.getDataInstance(), _pool);
                        break;
                    
                    case Create:
                        Indexer.instance().dataCreated(eventMessage.getDataInstance(), _pool);
                        break;
                    
                    case NameChange:
                        Indexer.instance().nameChanged(eventMessage.getDataInstance());
                        break;

                    case FileContentUpdated:
                        break;

                    case Modify:
                    case Review:
                    case Comment:
                    case Tag:
                        Indexer.instance().dataModified(eventMessage.getDataInstance(), _pool);
                        break;
                        
                    case Delete:
                        Indexer.instance().dataDeleted(eventMessage.getDataInstance(), _pool);
                        break;
                }
            }
            catch (Exception e)
            {
                /*
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
                */
                ErrorMessage em = new ErrorMessage();
                em.setMessage(e.getMessage());
                response = em;
            }
        }

        //_pool.returnInstance(database, this);
        
        return response;
    }
}
