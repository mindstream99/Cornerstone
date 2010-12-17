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

import java.util.Hashtable;

import com.paxxis.cornerstone.base.Message;
import com.paxxis.cornerstone.base.MessagingConstants;
import com.paxxis.cornerstone.base.MessagingConstants.PayloadType;
import com.paxxis.cornerstone.common.JavaObjectPayload;
import com.paxxis.cornerstone.common.MessagePayload;

/**
 *
 * @author Robert Englander
 */
public class ResponseHandler<T extends Message> extends SimpleServiceBusMessageHandler
{

    private static Hashtable<MessagingConstants.PayloadType, MessagePayload> _payloadTypes =
            new Hashtable<MessagingConstants.PayloadType, MessagePayload>();

    static {
        _payloadTypes.put(MessagingConstants.PayloadType.JavaObjectPayload, new JavaObjectPayload());
    }

    private ResponseListener _listener = null;

    public ResponseHandler() {
    }

    public void setResponseListener(ResponseListener<T> listener) {
        _listener = listener;
    }

    protected ResponseListener<T> getResponseListener() {
        return _listener;
    }

    protected MessagePayload getMessagePayloadType(PayloadType type) {
        return _payloadTypes.get(type);
    }

    protected SimpleMessageProcessor getProcessor(int type, int version, int payloadType) {
        // this is an unknown message type.  the subclass doesn't recognize it and neither do I.
        throw new RuntimeException("Unknown response message type: " + type + " version " + version);
    }

}
