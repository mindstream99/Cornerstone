package com.paxxis.cornerstone.common;

import java.util.HashMap;
import java.util.Map;

import com.paxxis.cornerstone.base.management.ConfigurationChange;

public interface CornerstoneConfiguration extends IManagedBean {
	public void registerConfigurable(CornerstoneConfigurable configurable);
    public void setOverrideConfiguration(CornerstoneConfiguration overrideConfig);
    public void modifyParameter(ConfigurationChange change);
    public Map<String, Object> findParameters(String startsWith);
    public void setParameters(HashMap<String, Object> localMap);
    public boolean hasValue(String parameter);
    public Object getObjectValue(String parameter);
    public String getStringValue(String parameter, String defaultValue);
    public int getIntValue(String parameter, int defaultValue);
    public boolean getBooleanValue(String parameter, boolean defaultValue);
    public Map<String, Object> getLocalPropertyMap();
}
