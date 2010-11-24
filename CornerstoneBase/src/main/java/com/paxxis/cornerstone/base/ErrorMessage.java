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


/**
 *
 * @author Robert Englander
 */
public class ErrorMessage extends Message {
	private static final long serialVersionUID = 1L;

	public enum Type
    {
        Unknown,
        SessionExpiration,
        StaleDataEdit,
        LockedDataEdit
    }
    
    private final static int VERSION = 1;

    @Override
    public int getMessageType() {
        return messageType();
    }

    public static int messageType() {
        return MessagingConstants.ERRORMESSAGE;
    }

    @Override
    public int getMessageVersion() {
        return messageVersion();
    }

    public static int messageVersion() {
        return VERSION;
    }


    private Type _type = Type.Unknown;
    private String _message;
    private RequestMessage _request = null;
    
    public void setRequest(RequestMessage request)
    {
        _request = request;
    }
    
    public RequestMessage getRequest()
    {
        return _request;
    }
    
    public void setType(Type type)
    {
        _type = type;
    }
    
    public Type getType()
    {
        return _type;
    }
    
    public void setMessage(String msg)
    {
        _message = msg;
    }
    
    public String getMessage()
    {
        return _message;
    }
}
