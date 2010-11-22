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

import com.paxxis.chime.client.common.ApplyVoteResponse;
import com.paxxis.chime.common.JavaObjectPayload;
import com.paxxis.chime.service.ResponseHandler;
import com.paxxis.chime.service.SimpleMessageProcessor;

/**
 *
 * @author Robert Englander
 */
public class ApplyVoteResponseHandler extends ResponseHandler<ApplyVoteResponse>
{
    private static int _supportedType = ApplyVoteResponse.messageType();
    private static int _supportedVersion = ApplyVoteResponse.messageVersion();

    @Override
    protected SimpleMessageProcessor getProcessor(int type, int version, int payloadType)
    {
        if (type == _supportedType && version == _supportedVersion)
        {
            return new ApplyVoteResponseProcessor(new JavaObjectPayload(), getResponseListener());
        }

        return super.getProcessor(type, version, payloadType);
    }
}
