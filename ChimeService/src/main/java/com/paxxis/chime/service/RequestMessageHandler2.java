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

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.paxxis.chime.client.common.BuildIndexRequestMessage;
import com.paxxis.chime.client.common.DataInstanceRequest;
import com.paxxis.chime.client.common.FindInstancesRequest;
import com.paxxis.chime.client.common.MessagingConstants;
import com.paxxis.chime.client.common.MessagingConstants.PayloadType;
import com.paxxis.chime.common.JavaObjectPayload;
import com.paxxis.chime.common.MessagePayload;
import com.paxxis.chime.data.CacheManager;
import com.paxxis.chime.database.DatabaseConnectionPool;
import com.paxxis.chime.indexing.BuildIndexRequestProcessor;

/**
 *
 * @author Robert Englander
 */
public class RequestMessageHandler2 extends ServiceBusMessageHandler {
    private static final Logger _logger = Logger.getLogger(RequestMessageHandler2.class);

    // the database connection pool
    private DatabaseConnectionPool _databasePool;

    // the topic sender to use to send events 
    NotificationTopicSender _topicSender = null;
    
    private static final HashMap<PayloadType, MessagePayload> _payloadTypes = new HashMap<PayloadType, MessagePayload>();
    private static final HashMap<Integer, Integer> _messageTypes = new HashMap<Integer, Integer>();

    static
    {
        _payloadTypes.put(MessagingConstants.PayloadType.JavaObjectPayload, new JavaObjectPayload());

        _messageTypes.put(DataInstanceRequest.messageType(), DataInstanceRequest.messageVersion());
        _messageTypes.put(FindInstancesRequest.messageType(), FindInstancesRequest.messageVersion());
        _messageTypes.put(BuildIndexRequestMessage.messageType(), BuildIndexRequestMessage.messageVersion());
        
        CacheManager.instance();
    }
    
    public MessageProcessor getProcessor(int type, int version, int payloadType)
    {
        PayloadType ptype = PayloadType.valueOf(payloadType);
        MessagePayload mPayload = _payloadTypes.get(ptype);
        if (_payloadTypes.containsKey(ptype))
        {
            if (_messageTypes.containsKey(type))
            {
                int ver = _messageTypes.get(type);
                if (version == ver)
                {
                    if (type == DataInstanceRequest.messageType())
                    {
                        return new DataInstanceRequestProcessor(mPayload, _databasePool);
                    }
                    else if (type == FindInstancesRequest.messageType())
                    {
                        return new FindInstancesRequestProcessor(mPayload, _databasePool);
                    }
                    else if (type == BuildIndexRequestMessage.messageType())
                    {
                        return new BuildIndexRequestProcessor(mPayload, _databasePool, _topicSender);
                    }
                }
            }
        }
        
        _logger.warn("Unsupported message routed to message handler {"
                +type+ "," +version+ "," +payloadType+ ")");
        return new ErrorProcessor(mPayload);
    }

    public void setConnectionPool(DatabaseConnectionPool pool)
    {
        _databasePool = pool;
    }
    
    public void setEventNotifier(NotificationTopicSender sender)
    {
        _topicSender = sender;
    }
    
}
