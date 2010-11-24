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

import com.paxxis.cornerstone.base.ErrorMessage;
import com.paxxis.cornerstone.base.Message;
import com.paxxis.cornerstone.common.MessagePayload;

/**
 *
 * @author Robert Englander
 */
public abstract class ResponseProcessor<T extends Message> extends SimpleMessageProcessor {
    private ResponseListener<T> _listener;

    public ResponseProcessor(MessagePayload type, ResponseListener<T> listener) {
        super(type);
        _listener = listener;
    }

    protected abstract T renderMessage(Object payload);

    protected Message process(boolean ignorePreviousChanges) {
        T responseMessage = renderMessage(getPayload());

        if (_listener != null) {
            if (responseMessage instanceof ErrorMessage) {
                _listener.onError((ErrorMessage)responseMessage);
            } else {
                _listener.onComplete(responseMessage);
            }
        }

        return responseMessage;
    }
}