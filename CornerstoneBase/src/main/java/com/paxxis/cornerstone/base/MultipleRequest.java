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

package com.paxxis.cornerstone.base;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class MultipleRequest extends RequestMessage {
	private static final long serialVersionUID = 1L;

	private final static int VERSION = 1;

    @Override
    public int getMessageType() {
        return messageType();
    }

    public static int messageType() {
        return MessagingConstants.MULTIREQUEST;
    }

    @Override
    public int getMessageVersion() {
        return messageVersion();
    }

    public static int messageVersion() {
        return VERSION;
    }

    
    private List<RequestMessage> _requests = new ArrayList<RequestMessage>();
    
    public List<RequestMessage> getRequests()
    {
        return _requests;
    }

    public void addRequest(RequestMessage request)
    {
        _requests.add(request);
    }
}