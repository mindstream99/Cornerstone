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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.paxxis.cornerstone.base.InstanceId;
import com.paxxis.cornerstone.base.monitoring.ServiceInstance;
import com.paxxis.cornerstone.service.CornerstoneConfigurable;
import com.paxxis.cornerstone.service.ICornerstoneService;
import com.paxxis.cornerstone.service.IServiceBusManager;
import com.paxxis.cornerstone.service.IServiceController;
import com.paxxis.cornerstone.service.LogManager;
import com.paxxis.cornerstone.service.ServiceVersion;
import com.paxxis.cornerstone.service.UnknownVersion;

/**
 *
 * @author Robert Englander
 */
public class CornerstoneService extends CornerstoneConfigurable implements IServiceController, ICornerstoneService {
    private static final Logger _logger = Logger.getLogger(CornerstoneService.class);

    /** the service instance */
    private ServiceInstance serviceInstance = new ServiceInstance();
    
    // the service connection managers
    private ArrayList<IServiceBusManager> _connectorManagers = new ArrayList<IServiceBusManager>();

    // the version instance
    private ServiceVersion _version = new UnknownVersion();
    
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
        FileSystemXmlApplicationContext ctx = new FileSystemXmlApplicationContext(args[0]);
        ctx.registerShutdownHook();
    }

    /**
     * Initializes the main service object.
     */
    public void initialize() {

        // we must have a display name
        if (serviceInstance.getDisplayName() == null) {
            throw new RuntimeException("CornerstoneService.displayName cannot be null.");
        }

        if (_logManager == null) {
            //throw new RuntimeException("CornerstoneService.logManager cannot be null.");
        }

        try {
			serviceInstance.setHostName(InetAddress.getLocalHost().getCanonicalHostName());
		} catch (UnknownHostException e) {
			serviceInstance.setHostName("UNKNOWN");
		}
        
		serviceInstance.setInstanceId(InstanceId.create(UUID.randomUUID().toString()));
		serviceInstance.setStartTime(new Date());

		_logger.info("Initializing " + toString());

    }
    
    @Override
    public String toString() {
    	String text = serviceInstance.getDisplayName() + ". Service ID: " 
    	                + serviceInstance.getServiceId() 
    	                + " Instance ID: " + serviceInstance.getInstanceId();
    
    	return text;
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
    public void setDisplayName(String name) {
    	serviceInstance.setDisplayName(name);
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
    
    public void setServiceId(String id) {
    	serviceInstance.setServiceId(InstanceId.create(id));
    }
       
    public ServiceInstance getServiceInstance() {
    	return serviceInstance;
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
     */
    public void shutdown() {
        if (canShutdown()) {
            System.exit(0);
        }
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
