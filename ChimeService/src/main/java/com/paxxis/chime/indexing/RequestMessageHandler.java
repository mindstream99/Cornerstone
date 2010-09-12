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
import com.paxxis.chime.database.DatabaseConnectionPool;
import com.paxxis.chime.service.ErrorProcessor;
import com.paxxis.chime.common.MessagePayload;
import com.paxxis.chime.service.MessageProcessor;
import com.paxxis.chime.service.NotificationTopicSender;
import com.paxxis.chime.client.common.MessageConstants;
import com.paxxis.chime.client.common.MessageConstants.MessageType;
import com.paxxis.chime.client.common.MessageConstants.PayloadType;
import com.paxxis.chime.common.JavaObjectPayload;
import com.paxxis.chime.service.ServiceBusMessageHandler;
import java.util.HashMap;
import org.apache.log4j.Logger;

/**
 *
 * @author Robert Englander
 */
public class RequestMessageHandler extends ServiceBusMessageHandler {
    private static final Logger _logger = Logger.getLogger(RequestMessageHandler.class);

    // the database connection pool
    private DatabaseConnectionPool _databasePool;

    // the topic sender to use to send events 
    NotificationTopicSender _topicSender = null;
    
    private static final HashMap<PayloadType, MessagePayload> _payloadTypes = new HashMap<PayloadType, MessagePayload>();
    private static final HashMap<MessageType, Integer> _messageTypes = new HashMap<MessageType, Integer>();

    static
    {
        _payloadTypes.put(MessageConstants.PayloadType.JavaObjectPayload, new JavaObjectPayload());

        _messageTypes.put(BuildIndexRequestMessage.messageType(), BuildIndexRequestMessage.messageVersion());
    }
    
    public MessageProcessor getProcessor(int type, int version, int payloadType)
    {
        PayloadType ptype = PayloadType.valueOf(payloadType);
        MessagePayload mPayload = _payloadTypes.get(ptype);
        if (_payloadTypes.containsKey(ptype))
        {
            MessageType mtype = MessageType.valueOf(type);
            if (_messageTypes.containsKey(mtype))
            {
                int ver = _messageTypes.get(mtype);
                if (version == ver)
                {
                    if (mtype == BuildIndexRequestMessage.messageType())
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
