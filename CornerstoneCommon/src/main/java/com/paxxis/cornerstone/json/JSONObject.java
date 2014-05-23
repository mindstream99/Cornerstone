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

package com.paxxis.cornerstone.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Robert Englander
 */
public class JSONObject implements JSONValue
{
    private Hashtable<String, JSONValue> _pairs = new Hashtable<String, JSONValue>();
    
    public Set<String> getNames()
    {
        return _pairs.keySet();
    }
    
    public void set(JSONString name, JSONValue value)
    {
        set(name.getValue(), value);
    }
    
    public void put(String name, boolean value)
    {
        _pairs.put(name, new JSONBoolean(value));
    }
    
    public void put(String name, int value)
    {
        _pairs.put(name, new JSONInteger(value));
    }
    
    public void put(String name, long value)
    {
        _pairs.put(name, new JSONLong(value));
    }
    
    public void put(String name, double value)
    {
        _pairs.put(name, new JSONDouble(value));
    }
    
    public void put(String name, String value)
    {
        _pairs.put(name, new JSONString(value));
    }
    
    public void put(String name, JSONValue value)
    {
        _pairs.put(name, value);
    }
    
    public void set(String name, JSONValue value)
    {
        _pairs.put(name, value);
    }
    
    public boolean contains(String name)
    {
        return _pairs.containsKey(name);
    }

    public boolean has(String name)
    {
        return _pairs.containsKey(name);
    }
    
    public JSONValue get(String name)
    {
        return _pairs.get(name);
    }
    
    public JSONObject getJSONObject(String name)
    {
        JSONValue val = get(name);
        
        JSONObject result = null;
        if (val != null && val instanceof JSONObject)
        {
            result = (JSONObject)val;
        }
        
        return result;
    }
    
    public JSONArray getJSONArray(String name)
    {
        JSONValue val = get(name);
        
        JSONArray result = null;
        if (val != null && val instanceof JSONArray)
        {
            result = (JSONArray)val;
        }
        
        return result;
    }
    
    public String getString(String name)
    {
        JSONValue val = get(name);
        
        String result = null;
        if (val != null) {
    		result = val.getObjectValue().toString();
        }
        
        return result;
    }
    
    public int getInt(String name)
    {
        JSONValue val = get(name);
        
        int result = 0;
        if (val != null && val instanceof JSONInteger)
        {
            result = ((JSONInteger)val).getValue();
        }
        
        return result;
    }
    
    public long getLong(String name)
    {
        JSONValue val = get(name);
        
        long result = 0;
        if (val != null)
        {
            if (val instanceof JSONLong)
            {
                result = ((JSONLong)val).getValue();
            }
            else if (val instanceof JSONInteger)
            {
                result = ((JSONInteger)val).getValue();
            }
        }
        
        return result;
    }
    
    public double getDouble(String name)
    {
        JSONValue val = get(name);
        
        double result = 0.0;
        if (val != null)
        {
            if (val instanceof JSONDouble)
            {
                result = ((JSONDouble)val).getValue();
            }
            else if (val instanceof JSONInteger)
            {
                result = ((JSONInteger)val).getValue();
            }
        }
        
        return result;
    }

    public boolean getBoolean(String name)
    {
        return getBoolean(name, false);
    }
    
    public boolean getBoolean(String name, boolean def)
    {
        JSONValue val = get(name);
        
        boolean result = def;
        if (val != null && val instanceof JSONBoolean)
        {
            result = ((JSONBoolean)val).isTrue();
        }
        
        return result;
    }
    
    public void append(StringBuffer buffer)
    {
        buffer.append("{");
        
        List<String> names = new ArrayList<String>(_pairs.keySet());
        Collections.sort(names);
        String preop = "";
        for (String name : names)
        {
            buffer.append(preop + '"' + name + '"' + ":");
            JSONValue value = _pairs.get(name);
            buffer.append(value.toString());
            preop = ",";
        }
        
        buffer.append("}");
    }

    @Override
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        append(buffer);
        return buffer.toString();
    }

	public Map<String, Object> toMap() {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		toMap(map, this);
		return map;
	}
	
	private void toMap(Map<String, Object> map, JSONObject json) {
        List<String> names = new ArrayList<String>(_pairs.keySet());
        Collections.sort(names);
        for (String name : names) {
            JSONValue value = _pairs.get(name);
    		map.put(name, value.getObjectValue());
        }
	}

	@Override
	public void toMap(String name, Map<String, Object> map) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getObjectValue() {
		return toMap();
	}
}











