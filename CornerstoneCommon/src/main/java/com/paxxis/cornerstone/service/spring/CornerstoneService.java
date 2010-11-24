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


package com.paxxis.cornerstone.service.spring;

import com.paxxis.cornerstone.service.IManagedBean;
import com.paxxis.cornerstone.service.IServiceBusManager;
import com.paxxis.cornerstone.service.IServiceController;
import com.paxxis.cornerstone.service.LogManager;
import com.paxxis.cornerstone.service.ServiceVersion;

import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;

/**
 *
 * @author Robert Englander
 */
public final class CornerstoneService implements IServiceController, IManagedBean {
    private static final Logger _logger = Logger.getLogger(CornerstoneService.class);

    // the service display name
    String _displayName = null;
    
    // the service connection managers
    ArrayList<IServiceBusManager> _connectorManagers = new ArrayList<IServiceBusManager>();

    // the version instance
    ServiceVersion _version = null;
    
    LogManager _logManager = null;
    
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
        _logger.info("Initializing " + _displayName + " Service");



        // we must have a display name
        if (_displayName == null)
        {
            throw new RuntimeException("CornerstoneService.displayName cannot be null.");
        }

        if (_logManager == null)
        {
            //throw new RuntimeException("CornerstoneService.logManager cannot be null.");
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
     * Set the version of the service.
     *
     * @param version the version.
     */
    public void setServiceVersion(ServiceVersion version)
    {
        _version = version;
    }
    
    /**
     * Set the connection managers
     *
     * @param managers the connection managers
     */
    public void setConnectionManagers(ArrayList<IServiceBusManager> managers)
    {
        _connectorManagers = new ArrayList<IServiceBusManager>(managers);
    }

    /**
     * Determine if the service can shut down.
     */
    public boolean canShutdown()
    {
        // can't shut down if there are connected service bus connectors
        for (IServiceBusManager mgr : _connectorManagers)
        {
            if (mgr.isConnected())
            {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Shut down the service.
     *
     * @return a string indicating that the service is shutting down, or indicating
     * why it can't shut down.
     */
    public String shutdown()
    {
        if (!canShutdown())
        {
            return "Can't shutdown service while service bus connections are active.";
        }
        
        System.exit(0);
        return "Shutting Down";
    }

    public String getVersion()
    {
        if (_version == null)
        {
            return "Unknown";
        }
        
        return _version.getVersionDescriptor();
    }
    

    /**
     * Set the service log level.
     * 
     * @param level
     */
    public void setLogLevel(String level)
    {
        _logManager.setLogLevel(level);
    }
    
    /**
     * Get the current service log level.
     * 
     * @return the log level
     */
    public String getLogLevel()
    {
        return _logManager.getLogLevel();
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
    public CornerstoneService()
    {
    }
    
}
