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

package com.paxxis.cornerstone.messaging.service;

import com.paxxis.cornerstone.service.ShutdownListener;

/**
 * Interface implemented by clients of a ServiceBusConnector.  Multiple
 * connector clients can use a single instance of ServiceBusConnector.
 * 
 * 
 * @author Robert Englander
 */
public interface IServiceBusConnectorClient {
    public void setup();
    public void halt();
    public void tearDown(ShutdownListener listener);
    public boolean isTeardownPending();
    public void setServiceBusConnector(ServiceBusConnector connector);
}
