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
public class JndiInitialContextFactory extends CornerstoneConfigurable
{
    
    // the JNDI context info for doing lookups
    private String _contextFactory = "";
    
    // the provider url for the lookup service
    private String _providerUrl = "";
    
    // the security principal
    private String _securityPrincipal = "";
    
    // simple authentication credentials
    private String _securityCredentials = "";
    
    // name of connection factory for simple namespace/scoping
    private String _connectionFactoryName = "";

    // the destination name mappings
    private Hashtable<String, String> destinationMap = new Hashtable<String, String>();

    /**
     * Creates a new instance of JndiInitialContextFactory
     */
    public JndiInitialContextFactory()
    {
    }

    /**
     * Sub classes that want to modify the url should override this method
     */
    protected String prepareProviderUrl(String url) {
    	return url;
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
            Hashtable<String, Object> env = new Hashtable<String, Object>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, _contextFactory);
            env.put(Context.PROVIDER_URL, prepareProviderUrl(_providerUrl));
            env.put(Context.SECURITY_PRINCIPAL, _securityPrincipal);
            env.put(Context.SECURITY_CREDENTIALS, _securityCredentials);
            env.put(Context.SECURITY_AUTHENTICATION, "simple");

            env.put("connectionFactoryNames", _connectionFactoryName);

            for (String key : destinationMap.keySet()) {
                env.put(key, destinationMap.get(key));
            }

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
    
    /**
     * Sets the connection factory name
     * 
     * @param connectionFactoryName the connection factory name
     */
    public void setConnectionFactoryName(String connectionFactoryName)
    {
        _connectionFactoryName = connectionFactoryName;
    }
}
