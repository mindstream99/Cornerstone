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

import java.util.Map;


/**
 *
 * @author Robert Englander
 */
public class JSONDouble implements JSONValue
{
    double _value = 0.0;
    
    public JSONDouble(String val)
    {
        try
        {
            _value = Double.parseDouble(val);
        }
        catch (NumberFormatException e)
        {
            // hmmm, what to do.....
        }
    }
    
    public JSONDouble(double val)
    {
        setValue(val);
    }
    
    public Object getObjectValue() {
    	return new Double(_value);
    }

    public double getValue()
    {
        return _value;
    }
    
    public void setValue(double val)
    {
        _value = val;
    }
    
    public void append(StringBuffer buffer)
    {
        buffer.append(toString());
    }

    public String toString()
    {
        return String.valueOf(_value);
    }

	@Override
	public void toMap(String name, Map<String, Object> map) {
		map.put(name, _value);
	}
}
