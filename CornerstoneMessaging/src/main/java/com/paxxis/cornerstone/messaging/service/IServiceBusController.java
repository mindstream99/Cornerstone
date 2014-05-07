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

import com.paxxis.cornerstone.messaging.service.IServiceBusManager;

/**
 * Defines the complete set of operations that a service bus connector manager (management object)
 * implements.  IServiceBusController extends IServiceBusManager, which defines
 * the operations that are exposed to a management container.  The methods in this interface
 * define the additional methods used by a service bus manager that are not meant to
 * be exposed to the management container.
 *
 * @author Robert Englander
 */
public interface IServiceBusController extends IServiceBusManager
{
    /**
     * Notify the object that the connection status has changed.
     */
    public void notifyConnectionStatusChange();
    
    /**
     * Notify the object that the connection dropped.
     */
    public void notifyConnectionFailed();

    /**
     * Set the collaborating management object.
     *
     * @param collaborator the collaborating management object
     */
    public void setCollaborator(IServiceBusController collaborator);
}
