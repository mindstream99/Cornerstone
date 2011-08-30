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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Robert Englander
 */
public abstract class CornerstoneConfigurable implements IManagedBean {

	/** the configuration object to use to populate property values */
    private CornerstoneConfiguration _configuration = null;
    
    /** contains a mapping of property names to configuration values */ 
    private HashMap<String, Object> _propertyMap = new HashMap<String, Object>();
    
    /** flag indicating if this instance should register for changes */
    private boolean registerForChanges = true;
    
    //this is for dynamically finding config properties for objects based on a configured prefix
    private Collection<String> configPropertyPrefixes = new ArrayList<String>();
    
    public CornerstoneConfigurable() {
    }
    
	public void setConfigurationPropertyMap(Map<String, ?> localMap) {
        _propertyMap.putAll(localMap);
    }

	@SuppressWarnings("unchecked")
    private <T> T getConfigurationValue(String propName, T defaultValue, Collection<String> prefixes) {
	    CornerstoneConfiguration config = getCornerstoneConfiguration();
	    if (config == null || prefixes.isEmpty()) {
	        return defaultValue;
	    }
	    
	    String configPropName = null;
	    Object value = null;
	    for (String prefix : prefixes) {
	        configPropName = prefix + "." + propName;
	        value = config.getObjectValue(configPropName);
	        if (value != null) {
	            break;
	        }
	    }
	    
	    if (value == null) {
	        return defaultValue;
	    }
	    
        if (defaultValue != null) {
            value = convert(defaultValue.getClass(), value);
        }
        
        if (registerForChanges) {
            //only bother putting a dynamically found property in the map if we are registered
            //for changes AND we have a config property for it in the database - never seen
            //before config properties added to the database will still be picked up in the onChange
            _propertyMap.put(propName, configPropName);
        }
        
        return (T) value;
	}
	
    void onChange(String configPropName) {
    	// the actual property name we want is the key mapped to this configPropName.
        // we don't support this for non string mappings (yet).
    	Collection<String> props = new ArrayList<String>();
    	for (String key : _propertyMap.keySet()) {
    		Object obj = _propertyMap.get(key);
    		if (obj instanceof String) {
        		String value = (String)obj;
    			if (value.equals(configPropName)) {
        			props.add(key);
        		}
    		}
    	}
    	
    	for (String prefix : configPropertyPrefixes) {
    	    //lets see if the propName starts with any of our defined prefixes..
    	    if (configPropName.startsWith(prefix) && configPropName.charAt(prefix.length()) == '.') {
    	        //it does so lets add it to the list to be configured and also add it to our
    	        //property map so loadConfigurationPropertyValues will find it...
    	        //NOTE: this logic will only happen if a property was ADDED to the database
    	        //post startup - if the item was in the db on startup it will already be in propertyMap
    	        //due to being found via reflectConfigurationPropertyValues which ran in initialization 
    	        String property = configPropName.substring(prefix.length());
    	        props.add(property);
    	        _propertyMap.put(property, configPropName);
    	    }
    	}
    	
    	props.add(configPropName); //why do we do this?
    	loadConfigurationPropertyValues(props);
    }
    
    protected void reflectConfigurationPropertyValues() {
        CornerstoneConfiguration config = getCornerstoneConfiguration();
        Collection<String> prefixes = getConfigPropertyPrefixes();
        if (config == null || prefixes == null || prefixes.size() < 1) {
            return;
        }
        
        Method[] methods = this.getClass().getMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            if (!methodName.startsWith("set") || methodName.length() < 5) {
                //not a set method or not a property that is at least 2 letters long
                //(ie. methods with names like setZ will not be looked at)
                continue;
            }
            
            Class<?>[] params = method.getParameterTypes(); 
            if (params.length != 1) {
                continue;
            }
            
            //looks like we found a setter method, lets look for a value now...
            String propName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
            Object value = getConfigurationValue(propName, null, prefixes);
            if (value == null) {
                continue;
            }

            try {
                method.invoke(this, convert(params[0], value));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    /**
     * Load property values from the configuration.
     */
    public void loadConfigurationPropertyValues(Collection<String> props) {
        CornerstoneConfiguration config = getCornerstoneConfiguration();
        if (config == null || props == null || props.isEmpty()) {
            return;
        }
        

        Method[] methods = this.getClass().getMethods();
        
        for (String propName : props) {
        	Object configObject = _propertyMap.get(propName);
        	if (configObject == null) {
        	    continue;
        	}
        	

        	Object value = null;
        	if (configObject instanceof List<?>) {
        		List<String> valueList = new ArrayList<String>();
        		List<?> configList = (List<?>)configObject;
        		for (Object o : configList) {
        			String v = config.getStringValue(o.toString(), "");
        			valueList.add(v);
        		}
        		
        		value = valueList;
        	} else {
                value = config.getObjectValue(configObject.toString());
        	}

        	if (value == null) {
        	    continue;
        	}
        	
            // get the setter
            String firstLetter = propName.substring(0, 1).toUpperCase();
            String setterName = "set" + firstLetter + propName.substring(1);

            for (Method method : methods) {
                if (!method.getName().equals(setterName)) {
                    continue;
                }
                Class<?>[] paramClasses = method.getParameterTypes();
                if (paramClasses.length == 1) {
                    // this is the one we want, so convert the value to this type
                    Object objValue = convert(paramClasses[0], value);
                    
                    try {
                        method.invoke(this, objValue);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    break;
                }
            } //for (Method method : methods)
            
    	} //for (String propName : props)
    }

    @SuppressWarnings("rawtypes")
    private Object convert(Class cls, Object value) {
        Object objValue = null;
        if (cls.getName().equals("java.lang.String")) {
            objValue = String.valueOf(value);
        } else if (cls.getName().equals("int")) {
            objValue = Integer.valueOf(value.toString());
        } else if (cls.getName().equals("long")) {
            objValue = Long.valueOf(value.toString());
        } else if (cls.getName().equals("float")) {
            objValue = Float.valueOf(value.toString());
        } else if (cls.getName().equals("double")) {
            objValue = Double.valueOf(value.toString());
        } else if (cls.getName().equals("boolean")) {
            objValue = Boolean.valueOf(value.toString());
        } else if (cls.getName().equals("java.util.List")) {
        	objValue = value;                                    
        } else {
            //this covers any class (Enums most importantly) that has
            //a static valueOf(java.lang.String) method
            try {
                @SuppressWarnings("unchecked")
                Method valueOf = cls.getMethod(
                        "valueOf", 
                        String.class);
                objValue = valueOf.invoke(null, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        return objValue;
    }
    
    /**
     * Set the cornerstoneConfiguration property.  Setting this property
     * causes the initialization process to use the configuration object
     * to retrieve property values immediately.
     *
     */
    public void setCornerstoneConfiguration(CornerstoneConfiguration configuration) {
        _configuration = configuration;
        Set<String> props = _propertyMap.keySet();
        loadConfigurationPropertyValues(props);
    }
    
    /**
     * Get the Configuration object
     *
     */
    public CornerstoneConfiguration getCornerstoneConfiguration() {
        return _configuration;
    }
    
    public void setRegisterForChanges(boolean register) {
    	this.registerForChanges = register;
    }
    
    /**
     * Initialize the object
     */
    public void initialize() {
        reflectConfigurationPropertyValues();
    	if (registerForChanges && _configuration != null) {
            _configuration.registerConfigurable(this);
    	}
    }
    
    /**
     * Tear down the object
     */
    public void destroy() {
    }

    public void addConfigPropertyPrefix(String configPrefix) {
        if (configPropertyPrefixes == null) {
            configPropertyPrefixes = new ArrayList<String>();
        }
        if (!configPropertyPrefixes.contains(configPrefix)) {
            configPropertyPrefixes.add(configPrefix);
        }
    }
    
    public Collection<String> getConfigPropertyPrefixes() {
        return configPropertyPrefixes;
    }

    public void setConfigPropertyPrefixes(Collection<String> configPropertyPrefixes) {
        this.configPropertyPrefixes = configPropertyPrefixes;
    }
}
