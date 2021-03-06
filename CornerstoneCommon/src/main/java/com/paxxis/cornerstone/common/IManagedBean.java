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

/**
 * This interface defines common lifecycle methods for a managed bean.  Normally
 * the container will initiate calls to these methods.
 * 
 * @author Robert Englander
 */
public interface IManagedBean
{
    /**
     * Initializes the managed bean.
     */
    public void initialize() throws Exception;
    
    /**
     * Tears down the managed bean.
     */
    public void destroy() throws Exception;
}
