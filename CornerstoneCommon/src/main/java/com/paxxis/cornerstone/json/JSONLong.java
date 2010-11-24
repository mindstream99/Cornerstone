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

/**
 *
 * @author Robert Englander
 */
public class JSONLong implements JSONValue
{
    long _value = 0;
    
    public JSONLong(String val)
    {
        try
        {
            _value = Long.parseLong(val);
        }
        catch (NumberFormatException e)
        {
            // hmmm, what to do.....
        }
    }
    
    public JSONLong(long val)
    {
        setValue(val);
    }
    
    public long getValue()
    {
        return _value;
    }
    
    public void setValue(long val)
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
}

