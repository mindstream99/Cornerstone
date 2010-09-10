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

import com.paxxis.chime.client.common.DiscussionsResponse;
import com.paxxis.chime.common.MessagePayload;
import com.paxxis.chime.service.ResponseListener;
import com.paxxis.chime.service.ResponseProcessor;

/**
 *
 * @author Robert Englander
 */
public class DiscussionsResponseProcessor extends ResponseProcessor<DiscussionsResponse>
{
    public DiscussionsResponseProcessor(MessagePayload type, ResponseListener<DiscussionsResponse> listener)
    {
        super(type, listener);
    }

    protected DiscussionsResponse renderMessage(Object payload)
    {
        return (DiscussionsResponse)new DiscussionsResponse().createInstance(payload);
    }
}