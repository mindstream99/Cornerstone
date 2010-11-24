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

package com.paxxis.chime.client;

import com.paxxis.chime.client.common.UserMessagesResponse;
import com.paxxis.cornerstone.common.JavaObjectPayload;
import com.paxxis.cornerstone.service.ResponseHandler;
import com.paxxis.cornerstone.service.SimpleMessageProcessor;


/**
 *
 * @author Robert Englander
 */
public class UserMessagesResponseHandler extends ResponseHandler<UserMessagesResponse>
{
    private static int _supportedType = UserMessagesResponse.messageType();
    private static int _supportedVersion = UserMessagesResponse.messageVersion();

    @Override
    protected SimpleMessageProcessor getProcessor(int type, int version, int payloadType)
    {
        if (type == _supportedType && version == _supportedVersion)
        {
            return new UserMessagesResponseProcessor(new JavaObjectPayload(), getResponseListener());
        }

        return super.getProcessor(type, version, payloadType);
    }

}
