package com.paxxis.cornerstone.common;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import com.paxxis.cornerstone.base.management.ConfigurationChange;

public class CornerstoneConfiguration implements IManagedBean {
    
	private Map<String, Object> localPropertyMap = new HashMap<String, Object>();
    private Map<String, Object> localMap = null;
    private Map<CornerstoneConfigurable, Object> registeredConfigurables = 
    				new WeakHashMap<CornerstoneConfigurable, Object>();

    public CornerstoneConfiguration() {
    }

    protected void setup() {
    }
    
    @Override
    public final void initialize() {
    	setup();
        if (localMap != null) {
            localPropertyMap.putAll(localMap);
        }
    }
    
    @Override
    public void destroy() {
    	
    }

    public void registerConfigurable(CornerstoneConfigurable configurable) {
    	if (!registeredConfigurables.containsKey(configurable)) {
    		registeredConfigurables.put(configurable, null);
    	}
    }

    /**
     * Use another configuration instance to set parameter values.  this is useful
     * for creating an override configuration instance that only needs to define
     * the properties that are being overriden, while the remainder come from the
     * default config.
     * 
     * @param overrideConfig
     */
    public void setOverrideConfiguration(CornerstoneConfiguration overrideConfig) {
    	localMap.putAll(overrideConfig.getLocalPropertyMap());
    }
    
    public void modifyParameter(ConfigurationChange change) {
    	localPropertyMap.put(change.getName(), change.getNewValue());
    	for (CornerstoneConfigurable cfg : registeredConfigurables.keySet()) {
    		cfg.onChange(change.getName());
    	}
    }
    
    public Map<String, Object> findParameters(String startsWith) {
        Map<String, Object> results = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry : localPropertyMap.entrySet()) {
            if (entry.getKey().startsWith(startsWith)) {
                results.put(entry.getKey(), entry.getValue());
            }
        }
        return results;
    }
    
    /**
     * Sets the map of local parameters.
     *
     * @param localMap the parameter map
     */
    public void setParameters(HashMap<String, Object> localMap)
    {
        // just keep these until we initialize
        this.localMap = localMap;
    }
    
    public boolean hasValue(String parameter) {
        return localPropertyMap.containsKey(parameter);
    }
    
    /**
     * Get the value of a parameter.
     *
     * @param parameter the name of the parameter
     *
     * @return the parameter value, or null if no such parameter exists
     */
    private String getValue(String parameter)
    {
        String result = null;
        if (localPropertyMap.containsKey(parameter))
        {
            result = String.valueOf(localPropertyMap.get(parameter));
        }
        
        return result;
    }
    
    public Object getObjectValue(String parameter) {
    	return localPropertyMap.get(parameter);
    }
    
    /**
     * Get the value of a parameter as a String.
     *
     * @param parameter the parameter name
     * @param defaultValue the value to assign if the parameter is not
     * found.
     *
     * @return the parameter value as a String
     */
    public String getStringValue(String parameter, String defaultValue)
    {
        String result = defaultValue;
        
        String value = getValue(parameter);
        if (value != null)
        {
            result = value;
        }
        
        return result;
    }
    
    /**
     * Get the value of a parameter as an int.
     *
     * @param parameter the parameter name
     * @param defaultValue the value to assign if the parameter is not
     * found.
     *
     * @return the parameter value as an int
     */
    public int getIntValue(String parameter, int defaultValue)
    {
        int result = defaultValue;
        
        String value = getValue(parameter);
        if (value != null)
        {
            result = Integer.parseInt(value);
        }
        
        return result;
    }
    
    /**
     * Get the value of a parameter as a boolean.
     *
     * @param parameter the parameter name
     * @param defaultValue the value to assign if the parameter is not
     * found.
     *
     * @return the parameter value as a boolean
     */
    public boolean getBooleanValue(String parameter, boolean defaultValue)
    {
        boolean result = defaultValue;
        
        String value = getValue(parameter);
        if (value != null)
        {
            result = value.equals("true");
        }
        
        return result;
    }

    protected Map<String, Object> getLocalPropertyMap() {
		return localPropertyMap;
	}
}
