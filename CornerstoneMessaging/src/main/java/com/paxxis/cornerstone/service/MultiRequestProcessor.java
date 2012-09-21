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

/**
 * Interface for processors to implement that encapsulate multiple requests in one.
 *  
 * @author Matthew Pflueger
 */
public interface MultiRequestProcessor {
    
    //FIXME really this whole interface is only necessary for MultiRequestProcessor in Chime Service
    //but maybe a good thing anyway...
    void setServiceBusMessageHandler(ServiceBusMessageHandler handler);

}
