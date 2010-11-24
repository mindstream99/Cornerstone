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
 * This class is used to maintain the version descriptor of
 * a service.  It is currently up to the service developer
 * to create an appropriate subclass and provide an instance
 * of it to the CornerstoneService object.  In the future we'll
 * look to automate that as part of the commit process.
 *
 * @author Robert Englander
 */
public abstract class ServiceVersion 
{
    private String _version;
    
    /** 
     * Constructor
     *
     * @param version the version descriptor
     */
    public ServiceVersion(String version) 
    {
        _version = version;
    }
    
    /**
     * Get the version descriptor.
     *
     * @return the version descriptor
     */
    public String getVersionDescriptor()
    {
        return _version;
    }
}
