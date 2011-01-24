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

import javax.jms.MessageListener;

import com.paxxis.cornerstone.base.ResponseMessage;
import com.paxxis.cornerstone.common.MessagePayload;

/**
 * 
 * @author Rob Englander
 */
public abstract class AsyncResponseListener<R extends ResponseMessage<?>> extends ResponseListener<R>
        implements MessageListener {
    
    private MessagePayload payloadType;
    
    public AsyncResponseListener(MessagePayload payloadType) {
        this.payloadType = payloadType;
    }
    
    public final void onMessage(javax.jms.Message msg) {
        @SuppressWarnings("unchecked")
        R result = (R) payloadType.getPayload(msg);
        if (result.isError()) {
            onError(result.getErrorMessage());
        } else {
	        onComplete(result);
        }
    }
}
