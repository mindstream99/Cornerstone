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

import java.util.Hashtable;
import java.util.Map;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * This is a factory object for creating instances of
 * a JNDI initial context based on a context factory
 * class and a provider URL.  This factory is commonly
 * passed to other objects that need to perform administered
 * object lookups from a JNDI source.
 *
 * @author Robert Englander
 */
public class JndiInitialContextFactory extends ChimeConfigurable
{
    /**
     * This is the name of the configuration parameter for the contextFactory property.<br><br> 
     * chime.service.JndiInitialContextFactory.contextFactory
     */
    private static final String CONTEXTFACTORY = "chime.service.JndiInitialContextFactory.contextFactory";
    
    /**
     * This is the name of the configuration parameter for the providerUrl property.<br><br> 
     * chime.service.JndiInitialContextFactory.providerUrl
     */
    private static final String PROVIDERURL = "chime.service.JndiInitialContextFactory.providerUrl";

    /**
     * This is the name of the configuration parameter for the securityPrincipal property.<br><br> 
     * chime.service.JndiInitialContextFactory.securityPrincipal
     */
    private static final String SECURITYPRINCIPAL = "chime.service.JndiInitialContextFactory.securityPrincipal";

    /**
     * This is the name of the configuration parameter for the securityCredentials property.<br><br> 
     * com.mindstream.chime.service.common.JndiInitialContextFactory.securityCredentials
     */
    private static final String SECURITYCREDENTIALS = "com.mindstream.chime.service.common.JndiInitialContextFactory.securityCredentials";

    // the JNDI context info for doing lookups
    String _contextFactory = "";
    
    // the provider url for the lookup service
    String _providerUrl = "";
    
    // the security principal
    String _securityPrincipal = "";
    
    // simple authentication credentials
    String _securityCredentials = "";

    // the destination name mappings
    private Hashtable<String, String> destinationMap = new Hashtable<String, String>();

    /**
     * Creates a new instance of JndiInitialContextFactory
     */
    public JndiInitialContextFactory()
    {
    }

    /**
     * Initializes property values from the endsliceConfiguration.
     */
    public void loadConfigurationPropertyValues()
    {
        ChimeConfiguration config = getEndsliceConfiguration();
        
        if (config != null)
        {
            // override contextFactory and providerUrl
            setContextFactory(config.getStringValue(CONTEXTFACTORY, _contextFactory));
            setProviderUrl(config.getStringValue(PROVIDERURL, _providerUrl));
            
            // override securityPrincipal and securityCredentials
            setSecurityPrincipal(config.getStringValue(SECURITYPRINCIPAL, _securityPrincipal));
            setSecurityCredentials(config.getStringValue(SECURITYCREDENTIALS, _securityCredentials));
        }
    }
    
    /**
     * Initialization
     */
    public void initialize()
    {
        // the final override of property values comes from the System properties
        setContextFactory(System.getProperty(CONTEXTFACTORY, _contextFactory));
        setProviderUrl(System.getProperty(PROVIDERURL, _providerUrl));
    }
    
    /**
     * Factory method for creating an initial context instance.
     *
     * @return an InitialContext for performing JNDI lookups.
     */
    public Context createInitialContext()
    {
        Context ctx = null;

        try
        {
            Hashtable env = new Hashtable();
            env.put(Context.INITIAL_CONTEXT_FACTORY, _contextFactory);
            env.put(Context.PROVIDER_URL, _providerUrl);
            env.put(Context.SECURITY_PRINCIPAL, _securityPrincipal);
            env.put(Context.SECURITY_CREDENTIALS, _securityCredentials);
            env.put(Context.SECURITY_AUTHENTICATION, "simple");

            env.put("connectionFactoryNames", "chimeFactory");

            for (String key : destinationMap.keySet()) {
                env.put(key, destinationMap.get(key));
            }

            env.put("queue.ChimeRequestQueue", "chimeRequestQueue");
            env.put("queue.ChimeEventQueue", "chimeEventQueue");
            env.put("topic.ChimeEventTopic", "chimeEventTopic");
            env.put("topic.ChimeUpdateTopic", "chimeUpdateTopic");
            ctx = new InitialContext(env);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        return ctx;
    }

    public void setDestinations(Map<String, String> map) {
        destinationMap.clear();
        destinationMap.putAll(map);
    }
    
    /**
     * Sets the context factory class.
     *
     * @param factory the context factory class.
     */
    public void setContextFactory(String factory)
    {
        _contextFactory = factory;
    }
    
    /**
     * Sets the provider URL.
     *
     * @param providerUrl the provider URL
     */
    public void setProviderUrl(String providerUrl)
    {
        _providerUrl = providerUrl;
    }
    
    /**
     * Sets the security principal.
     *
     * @param principal the security principal
     */
    public void setSecurityPrincipal(String principal)
    {
        _securityPrincipal = principal;
    }

    /**
     * Sets the security credentials.
     *
     * @param credentials the security credentials
     */
    public void setSecurityCredentials(String credentials)
    {
        _securityCredentials = credentials;
    }
}
