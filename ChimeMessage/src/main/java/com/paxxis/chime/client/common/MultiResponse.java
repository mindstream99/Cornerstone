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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class MultiResponse extends ResponseMessage<MultipleRequest> {
	private static final long serialVersionUID = 1L;
	private final static int VERSION = 1;

    @Override
    public int getMessageType() {
        return messageType();
    }

    public static int messageType() {
        return MessagingConstants.MULTIRESPONSE; 
    }

    @Override
    public int getMessageVersion() {
        return messageVersion();
    }

    public static int messageVersion() {
        return VERSION;
    }

    
    // the requests/responses
    //List<RequestResponse> _pairs = new ArrayList<RequestResponse>();
    ArrayList<RequestMessage> _requests = new ArrayList<RequestMessage>();
    ArrayList<ResponseMessage> _responses = new ArrayList<ResponseMessage>();
    
    public List<RequestResponse> getPairs()
    {
        ArrayList<RequestResponse> pairs = new ArrayList<RequestResponse>();
        int cnt = _requests.size();
        for (int i = 0; i < cnt; i++)
        {
            RequestResponse pair = new RequestResponse();
            pair.request = _requests.get(i);
            pair.response = _responses.get(i);
            pairs.add(pair);
        }
        
        return pairs;
    }
    
    public void addPair(RequestMessage request, ResponseMessage response)
    {
        RequestResponse pair = new RequestResponse();
        _requests.add(request);
        _responses.add(response);
    }
}

