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

package com.paxxis.chime.client.common;

//import com.paxxis.chime.json.JSONObject;

import java.io.Serializable;

public abstract class Message implements Serializable {
	private static final long serialVersionUID = 1L;

	public abstract int getMessageType();
    public abstract int getMessageVersion();

    public Message() {
    }

    public final Object getAsPayload(MessagingConstants.PayloadType type) {

        Object result;

        switch (type) {
            case JavaObjectPayload:
                result = this;
                break;

            default:
                throw new RuntimeException("Message.createInstance Unknown Payload Type: " + type);
        }

        return result;
    }

    public final Message createInstance(Object source)
    {
        if (source instanceof Message)
        {
            return (Message)source;
        }
        //else if (source instanceof String)
        //{
        //    Message instance = newInstance();
        //    instance.populateFromJSON((String)source);
        //    return instance;
        //}

        throw new RuntimeException("Message.createInstance invalid source class: " + source.getClass().getName());
    }
}

