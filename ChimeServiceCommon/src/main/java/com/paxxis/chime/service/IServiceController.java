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

package com.paxxis.chime.service;

/**
 * Defines the methods that are exposed to the management container for
 * controlling the service itself.
 *
 * @author Robert Englander
 */
public interface IServiceController
{
    /**
     * Shut down the service.
     *
     * @return A message indicating the reason that the service can not
     * be shut down, or a message indicating that a shutdown is being
     * initiated.
     */
    public String shutdown();
    
    /**
     * Determine if the service can currently be shut down.
     *
     * @return true if the service can be shut down, false otherwise.
     */
    public boolean canShutdown();
    
    /**
     * Get the service version.
     *
     * @return the full version descriptor of the service.
     */
    public String getVersion();

    /**
     * Set the service log level.
     * 
     * @param level
     */
    public void setLogLevel(String level);
    
    /**
     * Get the current service log level.
     * 
     * @return the log level
     */
    public String getLogLevel();
    
}
