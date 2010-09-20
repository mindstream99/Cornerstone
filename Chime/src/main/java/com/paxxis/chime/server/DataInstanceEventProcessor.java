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

import com.paxxis.chime.client.common.DataInstanceEvent;
import com.paxxis.chime.client.common.ErrorMessage;
import com.paxxis.chime.client.common.Message;
import com.paxxis.chime.client.common.DataInstanceEvent.EventType;
import com.paxxis.chime.common.MessagePayload;
import com.paxxis.chime.service.MessageProcessor;

/**
 * 
 * @author Robert Englander
 *
 */
public class DataInstanceEventProcessor extends MessageProcessor {

	private UpdateEventListener listener;
	
    public DataInstanceEventProcessor(MessagePayload payloadType, UpdateEventListener l) {
        super(payloadType);
        listener = l;
    }
    
    protected Message process(boolean ignorePreviousChanges) {
        // build up a response
        Message response = null;

        try {
            Object payload = getPayload();
            DataInstanceEvent event = (DataInstanceEvent)new DataInstanceEvent().createInstance(payload);
            boolean tryAgain = true;
            
            while (tryAgain) {
                tryAgain = false;
                
                try {
                	if (event.getEventType() == EventType.Modify) {
                    	listener.onDataInstanceUpdate(event);
                	}
                } catch (Exception e) {
                    ErrorMessage em = new ErrorMessage();
                    em.setMessage(e.getMessage());
                    response = em;
                }
            }
    	} catch (Exception e) {
        	System.out.println("Exception: " + e.getLocalizedMessage());
    	}

        return response;
    }
}

