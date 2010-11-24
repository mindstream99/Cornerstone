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


package com.paxxis.cornerstone.database;

import com.paxxis.cornerstone.base.InstanceId;

import java.util.Date;

/**
 *
 * @author Robert Englander
 */
public class StringData implements IDataValue
{
    String _data = null;
    boolean _addQuotes = true;
    
    public StringData(String data)
    {
        if (false || data == null) {
            _data = data;
        } else {
            _data = data.trim();
        }
    }

    public StringData(String data, boolean addQuotes)
    {
        this(data);
        _addQuotes = addQuotes;
    }
    
    public String asSQLValue()
    {
        if (_data == null) {
            return "null";
        }
        
        String result = null;
        
        //if (-1 != _data.indexOf("\'"))
        {
            StringBuffer buffer = new StringBuffer();
            
            if (_addQuotes)
            {
                buffer.append("'");
            }
            
            for (int i = 0; i < _data.length(); i++) 
            {
                char c = _data.charAt(i);
                if (c == '\'') 
                {
                    buffer.append("\'\'");
                }
                //else if (c == '%')
                //{
                //    buffer.append("\\%");
                //}
                //else if (c == '_')
                //{
                //    buffer.append("\\_");
                //}
                else if (c < ' ')
                {
                    buffer.append(" ");
                    //buffer.append("CHAR(" + Character.getNumericValue(c) + ")");
                }
                else
                {
                    buffer.append(c);
                }
            }
            
            if (_addQuotes)
            {
                buffer.append("'");
            }
            
            result = buffer.toString();
        }
        //else
        //{
        //    result = "'" + _data + "'";
        //}
        
        return result;
    }
    
    public String asString() 
    {
        return _data;
    }

    public Float asFloat() 
    {
        return Float.parseFloat(_data);
    }

    public Double asDouble() 
    {
        return Double.parseDouble(_data);
    }

    public Integer asInteger() 
    {
        return Integer.parseInt(_data);
    }

    public Long asLong() 
    {
        return Long.parseLong(_data);
    }

    public Date asDate()
    {
        return null;
    }

    public boolean isNull()
    {
        return _data == null;
    }

    public InstanceId asInstanceId() {
        return InstanceId.create(_data);
    }
}
