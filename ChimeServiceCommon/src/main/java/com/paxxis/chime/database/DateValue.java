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


package com.paxxis.chime.database;

import com.paxxis.chime.client.common.InstanceId;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.text.DateFormatter;

/**
 *
 * @author Robert Englander
 */
public class DateValue implements IDataValue
{
    Date _data;

    public DateValue(Date data)
    {
        _data = data;
    }
    
    public String asSQLValue()
    {
        return asString();
    }
    
    public String asString() 
    {
        DateFormatter formatter = new DateFormatter();
        
        String result;
        try
        {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            formatter.setFormat(format);
            result = formatter.valueToString(_data);
        }
        catch (ParseException e)
        {
            result = e.getMessage();
        }
        
        return result;
    }

    public Float asFloat() 
    {
        return null;
    }

    public Double asDouble() 
    {
        return null;
    }

    public Integer asInteger() 
    {
        return null;
    }

    public Long asLong() 
    {
        return null;
    }

    public Date asDate()
    {
        return _data;
    }
    
    public boolean isNull()
    {
        return _data == null;
    }

    public InstanceId asInstanceId() {
        return null;
    }
}
