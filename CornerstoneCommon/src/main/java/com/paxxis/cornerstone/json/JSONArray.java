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
import java.util.List;
import java.util.Map;

/**
 *
 * @author Robert Englander
 */
public class JSONArray implements JSONValue
{
    ArrayList<JSONValue> _values = new ArrayList<JSONValue>();
    
    public Object getObjectValue() {
    	List<Object> list = new ArrayList<Object>();
    	for (JSONValue value : _values) {
    		list.add(value.getObjectValue());
    	}
    	return list;
    }

    public void add(JSONValue value)
    {
        _values.add(value);
    }
    
    public void put(JSONValue value)
    {
        _values.add(value);
    }

    public List<JSONValue> getList()
    {
        return _values;
    }
    
    public JSONObject getJSONObject(int i)
    {
        return (JSONObject)_values.get(i);
    }
    
    public int length()
    {
        return _values.size();
    }
    
    public void append(StringBuffer buffer)
    {
        buffer.append("[");
        String preop = "";
        
        for (JSONValue value : _values)
        {
            buffer.append(preop + value.toString());
            preop = ",";
        }
        
        buffer.append("]");
    }
    
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        append(buffer);
        return buffer.toString();
    }

	@Override
	public void toMap(String name, Map<String, Object> map) {
		map.put(name, getObjectValue());
	}
}
