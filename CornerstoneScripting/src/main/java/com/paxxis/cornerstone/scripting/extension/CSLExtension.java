/* Copyright 2010 the original author or authors.
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
package com.paxxis.cornerstone.scripting.extension;

import java.util.HashMap;

import com.paxxis.cornerstone.scripting.PropertySetter;

/**
 * @author Rob Englander
 */
public abstract class CSLExtension {

    private String id = null;
    private String name = null;
    private ExtensionManager manager = null;
    private String className = null;
    private String cslClassName = null;
    private HashMap<String, Object> propertyMap = new HashMap<String, Object>();
    private boolean enabled = true;
    private PropertySetter propertySetter;
    
    protected CSLExtension() {
	propertySetter = new PropertySetter(this);
    }

    public abstract void initialize();
    public abstract void destroy();
    
    public boolean isEnabled() {
    	return enabled;
    }
    
    /**
     * This method is called on the extension when the enabled state is being changed.  Extensions
     * must override this method and handle their own enable state changes.  
     * 
     * @param newState the new enable state
     */
    public abstract void onEnable(boolean newState);
    
    public void setEnabled(boolean enabled) {
    	if (this.enabled != enabled) {
    	    this.enabled = enabled;
    	    onEnable(enabled);
    	}
    }
    
    public void setPropertyMap(HashMap<String, Object> map) {
        propertyMap.clear();
        propertyMap.putAll(map);
	HashMap<String, Object> propMap = getPropertyMap();
	for (String name : propMap.keySet()) {
	    Object value = propMap.get(name);
	    propertySetter.setValue(name, value);
	}
    }

    public HashMap<String, Object> getPropertyMap() {
        return propertyMap;
    }

    public void setClassName(String name) {
        className = name;
    }

    public String getClassName() {
        return cslClassName;
    }

    public void setCSLClassName(String name) {
        cslClassName = name;
    }

    public String getCSLClassName() {
        return cslClassName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setExtensionManager(ExtensionManager mgr) {
        manager = mgr;
    }

    public ExtensionManager getExtensionManager() {
        return manager;
    }
}
