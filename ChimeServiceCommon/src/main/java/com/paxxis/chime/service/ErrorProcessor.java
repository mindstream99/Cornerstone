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

package com.paxxis.chime.service;

import com.paxxis.chime.client.common.Message;
import com.paxxis.chime.client.common.ErrorMessage;
import com.paxxis.chime.common.MessagePayload;

/** 
 *
 * @author Robert Englander
 */
public class ErrorProcessor extends MessageProcessor
{
    public ErrorProcessor(MessagePayload payloadType)
    {
        super(payloadType);
    }

    @Override
    protected ErrorMessage process(boolean ignorePreviousChanges)
    {
        Object payload = getPayload();
        Message em = new ErrorMessage().createInstance(payload);
        return (ErrorMessage)em;
    }

}