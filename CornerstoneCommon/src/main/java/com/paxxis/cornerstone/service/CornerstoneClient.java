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

package com.paxxis.cornerstone.service;

import com.paxxis.cornerstone.base.RequestMessage;
import com.paxxis.cornerstone.base.ResponseMessage;
import com.paxxis.cornerstone.common.MessagePayload;
import com.paxxis.cornerstone.common.ResponsePromise;

/**
 * 
 * @author Robert Englander
 *
 */
public class CornerstoneClient {
     
    private MessagePayload payloadType;
    private long timeout;
    private RequestQueueSender sender;
    
    public CornerstoneClient(MessagePayload payloadType, RequestQueueSender sender, long timeout) {
        this.payloadType = payloadType;
        this.sender = sender;
        this.timeout = timeout;
    }
    
	public CornerstoneClient(MessagePayload payloadType, RequestQueueSender sender){
		this(payloadType, sender, 0L);
    }

	public <REQ extends RequestMessage, RESP extends ResponseMessage<REQ>> RESP 
    						execute(Class<RESP> clazz, REQ request, ResponseHandler<RESP> handler) {
        return sender.send(clazz, request, handler, timeout, payloadType);
    }

	public <REQ extends RequestMessage, RESP extends ResponseMessage<REQ>> void executeAsync(REQ request,
			AsyncDataResponseHandler<RESP> listener) {
		sender.send(request, null, listener, payloadType);
	}

    public <REQ extends RequestMessage, RESP extends ResponseMessage<REQ>> ResponsePromise<RESP> executePromise(REQ request) {
        return sender.<RESP>send(request, payloadType);
    }
}

