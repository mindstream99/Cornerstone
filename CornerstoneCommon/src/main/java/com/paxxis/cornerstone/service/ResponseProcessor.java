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

/**
 *
 * @author Robert Englander
 */
public abstract class ResponseProcessor<REQ extends RequestMessage, RESP extends ResponseMessage<REQ>> 
									extends SimpleMessageProcessor<REQ, RESP> {
    private ResponseListener<RESP> _listener;

    public ResponseProcessor(MessagePayload type, ResponseListener<RESP> listener) {
        super(type);
        _listener = listener;
    }

    protected abstract RESP renderMessage(Object payload);

    protected RESP process(boolean ignorePreviousChanges) {
    	RESP responseMessage = renderMessage(getPayload());

        if (_listener != null) {
            _listener.onComplete(responseMessage);
        }

        return responseMessage;
    }
}
