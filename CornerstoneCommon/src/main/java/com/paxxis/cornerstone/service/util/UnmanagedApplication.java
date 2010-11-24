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


package com.paxxis.cornerstone.service.util;

import com.paxxis.cornerstone.service.LogManager;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;

/**
 *
 * @author Robert Englander
 */
public class UnmanagedApplication 
{
    // the service display name
    private String _displayName = null;
    
    private LogManager _logManager = null;
    
    /**
     * The main
     *
     * @param args the command line arguments.  There should be only
     * 1 command line argument -- the spring factory xml file.
     */
    public static void main(String[] args)
    {
        // all we do is load the container
        GenericApplicationContext ctx = new GenericApplicationContext();
        ctx.registerShutdownHook();
        ctx.refresh();
        XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
        xmlReader.loadBeanDefinitions(new FileSystemResource(args[0]));
        ctx.getBeanFactory().preInstantiateSingletons();
    }

    /**
     * Initializes the main service object.
     */
    public void initialize()
    {
        System.out.println("Initializing Unmanaged Application " + _displayName);

        // we must have a display name
        if (_displayName == null)
        {
            throw new RuntimeException("UnmanagedApplication.displayName cannot be null.");
        }

        if (_logManager == null)
        {
            throw new RuntimeException("UnmanagedApplication.logManager cannot be null.");
        }
    }
    
    /**
     * Tear down the service.
     */
    public void destroy()
    {
    }
    
    /**
     * Set the display name for the service
     *
     * @param name the service display name
     */
    public void setDisplayName(String name)
    {
        _displayName = name;
    }
    
    /**
     * Set the log manager.
     * 
     * @param manager
     */
    public void setLogManager(LogManager manager)
    {
        _logManager = manager;
    }

    /** 
     * Constructor
     */
    public UnmanagedApplication()
    {
    }
    
}
