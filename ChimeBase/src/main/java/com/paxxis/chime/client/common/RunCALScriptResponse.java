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

import com.paxxis.chime.client.common.cal.IValue;

/**
 *
 * @author Robert Englander
 */
public class RunCALScriptResponse extends ResponseMessage<RunCALScriptRequest> {
    private final static int VERSION = 1;

    @Override
    public MessageConstants.MessageType getMessageType() {
        return messageType();
    }

    public static MessageConstants.MessageType messageType() {
        return MessageConstants.MessageType.RunCALScriptResponse;
    }

    @Override
    public int getMessageVersion() {
        return messageVersion();
    }

    public static int messageVersion() {
        return VERSION;
    }


    // the result of running the script
    IValue _result = null;

    public IValue getResult()
    {
        return _result;
    }

    public void setResult(IValue value)
    {
        _result = value;
    }
}

