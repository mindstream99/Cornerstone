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
public class StringData implements IDataValue {
	private static final long serialVersionUID = 1L;

	private String _data = null;
    private boolean _addQuotes = true;
    
    public static String toSQLValue(String str) {
    	return toSQLValue(str, true);
    }
    
    public static String toSQLValue(String str, boolean addQuotes) {
        if (str == null) {
            return "null";
        }
        
        StringBuilder buffer = new StringBuilder();
        
        if (addQuotes) {
            buffer.append("'");
        }
        
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '\'') {
                buffer.append("\'\'");
            } else if (c < ' ') {
                buffer.append(" ");
            } else {
                buffer.append(c);
            }
        }
        
        if (addQuotes) {
            buffer.append("'");
        }
        
        return buffer.toString();
    }
    
    public StringData(String data) {
        if (false || data == null) {
            _data = data;
        } else {
            _data = data.trim();
        }
    }

    public StringData(String data, boolean addQuotes) {
        this(data);
        _addQuotes = addQuotes;
    }
    
    public String asSQLValue() {
    	return toSQLValue(_data, _addQuotes);
    }
    
    public String asString() {
        return _data;
    }

    public Float asFloat() {
        return Float.parseFloat(_data);
    }

    public Double asDouble() {
        return Double.parseDouble(_data);
    }

    public Integer asInteger() {
        return Integer.parseInt(_data);
    }

    public Long asLong() {
        return Long.parseLong(_data);
    }

    public Date asDate() {
        return null;
    }

    public boolean isNull() {
        return _data == null;
    }

    public InstanceId asInstanceId() {
        return InstanceId.create(_data);
    }
}
