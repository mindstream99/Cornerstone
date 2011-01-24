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
package com.paxxis.cornerstone.common;

import com.paxxis.cornerstone.base.ResponseMessage;



/**
 * A response promise which is an abstraction over a DataLatch that enforces type safety
 * and provides the ability for clients of services to set reasonable timeouts for a response...
 */
public class ResponsePromise<RESP extends ResponseMessage<?>> extends DataLatch {

    private long timeout;
    
    public ResponsePromise() {
        this(10000);
    }
    
    public ResponsePromise(long timeout) {
        this.timeout = timeout;
    }
    
    @SuppressWarnings("unchecked")
    public RESP getResponse(long timeout) {
        return (RESP) waitForObject(timeout);
    }
    
    public RESP getResponse() {
        return getResponse(this.timeout);
    }

    /**
     * Enforce the timeout value provided at construct time
     */
    @Override
    public RESP waitForObject() {
        return getResponse(this.timeout);
    }

    public boolean hasResponse() {
        return hasObject();
    }

}

