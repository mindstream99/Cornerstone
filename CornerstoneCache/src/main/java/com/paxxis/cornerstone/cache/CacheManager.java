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
package com.paxxis.cornerstone.cache;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.infinispan.Cache;
import org.infinispan.config.Configuration;
import org.infinispan.config.FluentGlobalConfiguration;
import org.infinispan.config.FluentGlobalConfiguration.GlobalJmxStatisticsConfig;
import org.infinispan.config.FluentGlobalConfiguration.TransportConfig;
import org.infinispan.config.GlobalConfiguration;
import org.infinispan.config.InfinispanConfiguration;
import org.infinispan.jmx.PlatformMBeanServerLookup;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.jgroups.JGroupsTransport;

/**
 * 
 * @author Rob Englander
 *
 */
public class CacheManager extends CacheConfigurable {
    private static final Logger logger = Logger.getLogger(CacheManager.class);

    /** the named caches to preload on initialization */
    private List<String> preloadCacheNames = new ArrayList<String>();
    
    private EmbeddedCacheManager cacheManager = null;
    
    private String cacheConfigLocation;
    
    private GlobalConfiguration globalConfig;
    private Configuration defaultConfig;
    
    private String transportChannelLookup = null;
    private String transportConfigurationFile = null;
    private String transportConfigurationXml = null;
    private String transportConfigurationString = null;
    

    private String machineId = null;
    private String rackId = null;
    private String clusterName = null;
    private String nodeName = null;
    private String siteId = null;
    
    private Boolean exposeGlobalJmxStatistics = null; 
    private String jmxDomain = null; 
    private Boolean allowDuplicateDomains = null;
    private String cacheManagerName = null;

    // a mapping of cache names to their specific configurations
    private Map<String, Configuration> namedConfigs;
    
    @Override
    protected void defineConfiguration() {
        if (getCacheConfigLocation() != null) {
            //cache managers can get their default config from a file - those values can then be
            //overriden by values in the configuration database...
            defineConfiguration(getCacheConfigLocation());
            return;
        } 
        
        if (transportChannelLookup == null &&
                transportConfigurationFile == null &&
                transportConfigurationXml == null &&
                transportConfigurationString == null) {
            globalConfig = GlobalConfiguration.getNonClusteredDefault();
            defineConfiguration(globalConfig);
        } else {
            globalConfig = GlobalConfiguration.getClusteredDefault();
            defineConfiguration(globalConfig);
        }
        
        defaultConfig = new Configuration();
        defineConfiguration(defaultConfig);
    }
    
    protected void defineConfiguration(String configLocation) {
        try {
            //lets get our configuration from the configured file first 
            InfinispanConfiguration infinispanConfig = InfinispanConfiguration.newInfinispanConfiguration(
                    configLocation,
                    Thread.currentThread().getContextClassLoader());
            
            defineConfiguration(infinispanConfig);
        } catch (Exception e) {
            logger.error("Error during configuration of cache", e);
            throw new RuntimeException(e);
        }
    }
        
    protected void defineConfiguration(InfinispanConfiguration infinispanConfig) {
        globalConfig = infinispanConfig.parseGlobalConfiguration();
        defineConfiguration(globalConfig);
        defaultConfig = infinispanConfig.parseDefaultConfiguration();
        namedConfigs = infinispanConfig.parseNamedConfigurations();
        defineConfiguration(defaultConfig);
    }
    
    protected void defineConfiguration(GlobalConfiguration config) {
        FluentGlobalConfiguration fluentGlobalConfig = globalConfig.fluent();
            
        TransportConfig transport = fluentGlobalConfig.transport();
        
        String channelLookup = choose(
                        getTransportChannelLookup(),
                        globalConfig.getTransportProperties().getProperty(JGroupsTransport.CHANNEL_LOOKUP));
        if (channelLookup != null) {
            transport.addProperty(JGroupsTransport.CHANNEL_LOOKUP, channelLookup);
        }
        
        String configurationFile = choose(
                        getTransportConfigurationFile(), 
                        globalConfig.getTransportProperties().getProperty(JGroupsTransport.CONFIGURATION_FILE));
        if (configurationFile != null) {
            transport.addProperty(JGroupsTransport.CONFIGURATION_FILE, configurationFile);
        }
        
        String configurationXml = choose(
                        getTransportConfigurationXml(), 
                        globalConfig.getTransportProperties().getProperty(JGroupsTransport.CONFIGURATION_XML));
        if (configurationXml != null) {
            transport.addProperty(JGroupsTransport.CONFIGURATION_XML, configurationXml);
        }
        
        String configurationString = choose(
                        getTransportConfigurationString(), 
                        globalConfig.getTransportProperties().getProperty(JGroupsTransport.CONFIGURATION_STRING));
        if (configurationString != null) {
            transport.addProperty(JGroupsTransport.CONFIGURATION_STRING, configurationString);
        }
        
        transport.machineId(choose(getMachineId(), globalConfig.getMachineId()));
        transport.rackId(choose(getRackId(), globalConfig.getRackId()));
        transport.clusterName(choose(getClusterName(), globalConfig.getClusterName()));
        transport.nodeName(choose(getNodeName(), globalConfig.getTransportNodeName()));
        transport.siteId(choose(getSiteId(), globalConfig.getSiteId()));
        
        
        if (choose(isExposeGlobalJmxStatistics(), globalConfig.isExposeGlobalJmxStatistics())) {
            GlobalJmxStatisticsConfig globalJmxStatistics = fluentGlobalConfig.globalJmxStatistics();
            globalJmxStatistics.jmxDomain(choose(getJmxDomain(), globalConfig.getJmxDomain()));
            globalJmxStatistics.mBeanServerLookupClass(PlatformMBeanServerLookup.class);
        } else {
            fluentGlobalConfig.globalJmxStatistics().disable();
        }
        
    }

    
    
    @Override
    public void initialize() {
        super.initialize();

        if (logger.isDebugEnabled()) {
                logger.debug(
                        getBeanName() + 
                        " global configuration:\n\n " + 
                        globalConfig.toXmlString() + 
                        "\n\n");
                logger.debug(
                        getBeanName() +
                        " default configuration:\n\n " + 
                        defaultConfig.toXmlString() + 
                        "\n\n");
        }
            
        cacheManager = new DefaultCacheManager(globalConfig, defaultConfig);
        //QUESTION do we really need to do this - won't the caches be initialized on app startup anyway?
        for (String name : preloadCacheNames) {
            cacheManager.getCache(name);
        }
    }

    
    public Configuration getNamedConfiguration(String name) {
    	Configuration cfg = namedConfigs.get(name);
    	return cfg;
    }
    
    public synchronized void setPreloadCacheNames(List<String> names) {
    	preloadCacheNames.clear();
    	preloadCacheNames.addAll(names);
    }
    
    @SuppressWarnings("rawtypes")
    public Cache getCache(String cacheName) {
        return cacheManager.getCache(cacheName);
    }

    /**
     * Define the cache with the given configuration and return it.  Package-private as it should
     * only be used by our NamedCache
     * 
     * @param cacheName
     * @param namedConfig
     * @return the cache
     */
    @SuppressWarnings("rawtypes")
    Cache getCache(String cacheName, Configuration namedConfig) {
        Configuration config = cacheManager.defineConfiguration(cacheName, namedConfig);
        if (logger.isDebugEnabled()) {
            logger.debug(cacheName + " configuration:\n\n " + config.toXmlString() + "\n\n");
        }
        return cacheManager.getCache(cacheName);
    }

    /**
     * Return global config for this cache manager. Package-private as only Cornerstone caches
     * should be using this.
     * @return
     */
    GlobalConfiguration getGlobalConfig() {
        return globalConfig;
    }

    /**
     * Return default config for this cache manager. Package-private as only Cornerstone caches
     * should be using this.
     * @return
     */
    Configuration getDefaultConfig() {
        return defaultConfig;
    }

    public String getCacheManagerName() {
        return cacheManagerName;
    }

    public void setCacheManagerName(String cacheManagerName) {
        this.cacheManagerName = cacheManagerName;
    }

    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    public String getRackId() {
        return rackId;
    }

    public void setRackId(String rackId) {
        this.rackId = rackId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public Boolean isExposeGlobalJmxStatistics() {
        return exposeGlobalJmxStatistics;
    }

    public void setExposeGlobalJmxStatistics(Boolean exposeGlobalJmxStatistics) {
        this.exposeGlobalJmxStatistics = exposeGlobalJmxStatistics;
    }

    public String getJmxDomain() {
        return jmxDomain;
    }

    public void setJmxDomain(String jmxDomain) {
        this.jmxDomain = jmxDomain;
    }

    public Boolean isAllowDuplicateDomains() {
        return allowDuplicateDomains;
    }

    public void setAllowDuplicateDomains(Boolean allowDuplicateDomains) {
        this.allowDuplicateDomains = allowDuplicateDomains;
    }

    /**
     * Caches should always have a config file that can be referenced somewhere for default properties
     * 
     * @return the location of the config file
     */
    public String getCacheConfigLocation() {
        return cacheConfigLocation;
    }
    
    public void setCacheConfigLocation(String cacheConfigLocation) {
        this.cacheConfigLocation = cacheConfigLocation;
    }

    public String getTransportConfigurationFile() {
        return transportConfigurationFile;
    }

    public void setTransportConfigurationFile(String transportConfigurationFile) {
        this.transportConfigurationFile = transportConfigurationFile;
    }

    public String getTransportChannelLookup() {
        return transportChannelLookup;
    }

    public void setTransportChannelLookup(String transportChannelLookup) {
        this.transportChannelLookup = transportChannelLookup;
    }

    public String getTransportConfigurationXml() {
        return transportConfigurationXml;
    }

    public void setTransportConfigurationXml(String transportConfigurationXml) {
        this.transportConfigurationXml = transportConfigurationXml;
    }

    public String getTransportConfigurationString() {
        return transportConfigurationString;
    }

    public void setTransportConfigurationString(String transportConfigurationString) {
        this.transportConfigurationString = transportConfigurationString;
    }
}
