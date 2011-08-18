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

import java.util.HashMap;
import java.util.WeakHashMap;

import com.paxxis.cornerstone.base.management.ConfigurationChange;
import com.paxxis.cornerstone.database.DatabaseConnection;
import com.paxxis.cornerstone.database.DatabaseConnectionPool;
import com.paxxis.cornerstone.database.IDataSet;

/**
 * @author Robert Englander
 */
public class CornerstoneConfiguration implements IManagedBean
{
    // a map of locally defined properties
    private HashMap<String, Object> _localPropertyMap = new HashMap<String, Object>();
    
    private String _dbQuery = null;
    private String _keyColumn = null;
    private String _valueColumn = null;
    
    private HashMap<String, Object> _localMap = null;
    
    private DatabaseConnectionPool _databasePool = null;

    private WeakHashMap<CornerstoneConfigurable, Object> registeredConfigurables = 
    				new WeakHashMap<CornerstoneConfigurable, Object>();
    
    /**
     * Constructor
     */
    public CornerstoneConfiguration()
    {
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
    	_localMap.putAll(overrideConfig._localPropertyMap);
    }
    
    public void modifyParameter(ConfigurationChange change) {
    	_localPropertyMap.put(change.getName(), change.getNewValue());
    	for (CornerstoneConfigurable cfg : registeredConfigurables.keySet()) {
    		cfg.onChange(change.getName());
    	}
    }
    
    /**
     * Sets the map of local parameters.
     *
     * @param localMap the parameter map
     */
    public void setParameters(HashMap<String, Object> localMap)
    {
        // just keep these until we initialize
        _localMap = localMap;
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
        if (_localPropertyMap.containsKey(parameter))
        {
            result = String.valueOf(_localPropertyMap.get(parameter));
        }
        
        return result;
    }
    
    public Object getObjectValue(String parameter) {
    	return _localPropertyMap.get(parameter);
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
    
    /**
     * Initializes the underlying configuration object.  If the
     * noConnection property is set to true, this method is a no-op.
     */
    public void initialize()
    {
        if (_databasePool != null)
        {
            try
            {
                DatabaseConnection database = _databasePool.borrowInstance(this);
                                
                IDataSet dataSet = database.getDataSet(_dbQuery, true);
                
                int keyIdx = dataSet.getFieldIndex(_keyColumn);
                int valueIdx = dataSet.getFieldIndex(_valueColumn);
                
                while (dataSet.next())
                {
                    String name = dataSet.getFieldValue(keyIdx).asString();
                    String value = dataSet.getFieldValue(valueIdx).asString();
                    _localPropertyMap.put(name, value);
                }
                
                dataSet.close();
                _databasePool.returnInstance(database, this);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        
        if (_localMap != null)
        {
            _localPropertyMap.putAll(_localMap);
        }
    }
    
    /**
     * Tears down the underlying configuration object.
     */
    public void destroy()
    {
    }
    
    public void setDatabasePool(DatabaseConnectionPool pool)
    {
        _databasePool = pool;
    }
    
    public void setDbQuery(String query)
    {
        _dbQuery = query;
    }

    public void setValueColumn(String name)
    {
        _valueColumn = name;
    }
    
    public void setKeyColumn(String name)
    {
        _keyColumn = name;
    }

}
