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

/**
 * Convenience parent class for processors that handle event like messages.
 *  
 * @author Matthew Pflueger
 */
public abstract class EventMessageProcessor<REQ extends RequestMessage>
        extends BaseMessageProcessor<REQ, ResponseMessage<REQ>> {

    /**
     * Subclasses implement this method to messages that require no immediate response.
     * 
     * @param requestMessage
     * @throws Exception
     */
    protected abstract void process(REQ requestMessage) throws Exception;
    
    @Override
    protected boolean process(REQ requestMessage, ResponseMessage<REQ> responseMessage) throws Exception {
        process(requestMessage);
        return false;
    }
    
    
    /*
     * The following methods are overriden to prevent response messages from being created
     * unnecessarily...
     */
    
    @Override
    public Class<ResponseMessage<REQ>> getResponseMessageClass() {
        return null;
    }
    
    @Override
    protected ResponseMessage<REQ> createResponseMessage() {
        return null;
    }
    
    @Override
    public Integer getResponseMessageType() {
        return null;
    }
    
    @Override
    public Integer getResponseMessageVersion() {
        return null;
    }
}
