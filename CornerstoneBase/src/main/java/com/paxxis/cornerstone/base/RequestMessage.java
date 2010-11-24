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
public abstract class RequestMessage extends Message
{
	private static final long serialVersionUID = 1L;
	private long _correlator = -1;
    
    public RequestMessage()
    {
        
    }
    
    public void setCorrelator(long value)
    {
        _correlator = value;
    }
    
    public long getCorrelator()
    {
        return _correlator;
    }

    /**
     * This method is called by the service processor prior to processing
     * the request.  subclasses that require additional work before being
     * processed should override this method.
     */
    public void prepareForProcessing()
    {
    }

}
