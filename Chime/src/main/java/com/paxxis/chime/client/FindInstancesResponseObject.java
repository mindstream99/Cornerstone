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

import com.paxxis.chime.client.common.FindInstancesResponse;
import com.paxxis.cornerstone.base.ErrorMessage;

import java.io.Serializable;

/**
 *
 * @author Robert Englander
 */
public class FindInstancesResponseObject implements Serializable
{
    private ErrorMessage _errorMessage = null;
    private FindInstancesResponse _responseMessage = null;
    
    public FindInstancesResponseObject()
    {
    }
    
    public boolean isError()
    {
        return _errorMessage != null;
    }
    
    public boolean isResponse()
    {
        return _responseMessage != null;
    }
    
    public void setResponse(FindInstancesResponse resp)
    {
        _responseMessage = resp;
        _errorMessage = null;
    }
    
    public FindInstancesResponse getResponse()
    {
        return _responseMessage;
    }
    
    public void setError(ErrorMessage resp)
    {
        _errorMessage = resp;
        _responseMessage = null;
    }
    
    public ErrorMessage getError()
    {
        return _errorMessage;
    }
}
