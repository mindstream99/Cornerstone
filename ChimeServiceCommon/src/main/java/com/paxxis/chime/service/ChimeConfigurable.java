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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Robert Englander
 */
public abstract class ChimeConfigurable implements IManagedBean
{
    // the configuration object to use to populate
    // property values. 
    private ChimeConfiguration _configuration = null;
    
    // contains a mapping of property names to configuration
    // value names 
    private HashMap<String, Object> _propertyMap = new HashMap<String, Object>();
    
    /** 
     * Constructor 
     */
    public ChimeConfigurable()
    {
    }
    
    @SuppressWarnings("unchecked")
	public void setConfigurationPropertyMap(Map localMap)
    {
        _propertyMap.putAll(localMap);
    }

    /**
     * Load property values from the configuration.
     */
    public void loadConfigurationPropertyValues()
    {
        ChimeConfiguration config = getChimeConfiguration();
        
        if (config != null)
        {
            Method[] methods = this.getClass().getMethods();
            
            Set<String> props = _propertyMap.keySet();
            for (String propName : props)
            {
                String configKey = (String)_propertyMap.get(propName);
                String value = config.getStringValue(configKey, null);
                
                if (value != null)
                {
                    // get the setter
                    String firstLetter = propName.substring(0, 1).toUpperCase();
                    String setterName = "set" + firstLetter + propName.substring(1);

                    for (Method method : methods)
                    {
                        if (method.getName().equals(setterName))
                        {
                            Class[] paramClasses = method.getParameterTypes();
                            if (paramClasses.length == 1)
                            {
                                // this is the one we want, so convert the value to this type
                                Object objValue = null;
                                if (paramClasses[0].getName().equals("java.lang.String"))
                                {
                                    objValue = String.valueOf(value);
                                }
                                else if (paramClasses[0].getName().equals("int"))
                                {
                                    objValue = Integer.valueOf(value);
                                }
                                else if (paramClasses[0].getName().equals("long"))
                                {
                                    objValue = Long.valueOf(value);
                                }
                                else if (paramClasses[0].getName().equals("float"))
                                {
                                    objValue = Float.valueOf(value);
                                }
                                else if (paramClasses[0].getName().equals("double"))
                                {
                                    objValue = Double.valueOf(value);
                                }
                                else if (paramClasses[0].getName().equals("boolean"))
                                {
                                    objValue = Boolean.valueOf(value);
                                }
                                
                                try
                                {
                                    method.invoke(this, objValue);
                                }
                                catch (Exception e)
                                {
                                    throw new RuntimeException(e);
                                }

                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    
    /**
     * Set the chimeConfiguration property.  Setting this property
     * causes the initialization process to use the configuration object
     * to retrieve property values immediately.
     *
     */
    public void setChimeConfiguration(ChimeConfiguration configuration)
    {
        _configuration = configuration;
        loadConfigurationPropertyValues();
    }
    
    /**
     * Get the Configuration object
     *
     */
    public ChimeConfiguration getChimeConfiguration()
    {
        return _configuration;
    }
    
    /**
     * Initialize the object
     */
    public void initialize()
    {
    }
    
    /**
     * Tear down the object
     */
    public void destroy()
    {
    }
}
