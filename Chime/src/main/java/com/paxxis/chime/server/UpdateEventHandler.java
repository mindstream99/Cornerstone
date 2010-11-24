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

package com.paxxis.chime.server;

import java.util.Hashtable;

import com.paxxis.chime.client.common.DataInstanceEvent;
import com.paxxis.chime.client.common.MessageConstants;
import com.paxxis.cornerstone.common.JavaObjectPayload;
import com.paxxis.cornerstone.common.MessagePayload;
import com.paxxis.cornerstone.service.ErrorProcessor;
import com.paxxis.cornerstone.service.MessageProcessor;
import com.paxxis.cornerstone.service.ServiceBusMessageHandler;

/**
 * 
 * @author Robert Englander
 *
 */
public class UpdateEventHandler extends ServiceBusMessageHandler {
    private static Hashtable<Integer, MessagePayload> _payloadTypes = new Hashtable<Integer, MessagePayload>();
    private static Hashtable<Integer, Integer> _messageTypes = new Hashtable<Integer, Integer>();
 
    static {
        _payloadTypes.put(MessageConstants.PayloadType.JavaObjectPayload.getValue(), new JavaObjectPayload());

        _messageTypes.put(DataInstanceEvent.messageType(), DataInstanceEvent.messageVersion());
    }
    
    private UpdateEventListener listener;
    
    public UpdateEventHandler(UpdateEventListener l) {
    	super();
    	listener = l;
    }
    
    public MessageProcessor getProcessor(int type, int version, int payloadType) {
        if (_payloadTypes.containsKey(payloadType)) {
            if (_messageTypes.containsKey(type)) {
                int ver = _messageTypes.get(type);
                if (version == ver) {
                    if (type == DataInstanceEvent.messageType()) {
                        return new DataInstanceEventProcessor(_payloadTypes.get(payloadType), listener);
	                }
                }
            }
        }
        
        // we don't support this request.  we should probably send
        // an event through the mgmt interface and log this
        return new ErrorProcessor(_payloadTypes.get(payloadType));
    }
}
