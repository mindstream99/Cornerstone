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

package com.paxxis.chime.indexing;

import com.paxxis.chime.client.common.BuildIndexRequestMessage;
import com.paxxis.chime.client.common.BuildIndexResponseMessage;
import com.paxxis.chime.database.DatabaseConnection;
import com.paxxis.chime.database.DatabaseConnectionPool;
import com.paxxis.chime.client.common.ErrorMessage;
import com.paxxis.chime.client.common.Message;
import com.paxxis.chime.service.MessageProcessor;
import com.paxxis.chime.service.NotificationTopicSender;
import com.mysql.jdbc.CommunicationsException;
import com.paxxis.chime.common.MessagePayload;
import com.paxxis.chime.service.ServiceBusMessageProducer;
import javax.jms.JMSException;
import org.apache.log4j.Logger;

/** 
 *
 * @author Robert Englander
 */
public class BuildIndexRequestProcessor extends MessageProcessor {
    private static final Logger _logger = Logger.getLogger(BuildIndexRequestProcessor.class);

    // the database connection pool
    private DatabaseConnectionPool _pool;
    
    private NotificationTopicSender _topicSender;
    
    public BuildIndexRequestProcessor(MessagePayload payloadType, DatabaseConnectionPool pool, NotificationTopicSender sender)
    {
        super(payloadType);
        _pool = pool;
        _topicSender = sender;
    }
    
    protected Message process(boolean ignorePreviousChanges)
    {
        Object payload = getPayload();
        BuildIndexRequestMessage requestMessage = (BuildIndexRequestMessage)new BuildIndexRequestMessage().createInstance(payload);
        
        // build up a response
        Message response = null;
        DatabaseConnection database = _pool.borrowInstance(this);
        
        boolean tryAgain = true;
        boolean retried = false;
        
        while (tryAgain)
        {
            tryAgain = false;
            response = new BuildIndexResponseMessage();
            BuildIndexResponseMessage qrm = (BuildIndexResponseMessage)response;
            qrm.setRequest(requestMessage);
            
            try
            {
                Indexer.instance().rebuildIndex(null, _pool);
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

                    try
                    {
                        _topicSender.send(new ServiceBusMessageProducer(em), getPayloadType());
                    }
                    catch (JMSException ee)
                    {
                        _logger.error(ee);
                    }

                    response = em;
                }
            }
        }

        _pool.returnInstance(database, this);
        
        return response;
    }
    
}
