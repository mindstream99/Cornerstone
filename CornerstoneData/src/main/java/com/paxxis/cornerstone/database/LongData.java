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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

/**
 *
 * @author Robert Englander
 */
public class LongData implements IDataValue {
	private static final long serialVersionUID = 1L;
	private Long _data = null;

    public LongData(Long data) {
        _data = data;
    }
    
    public LongData(String data) {
    	_data = Long.valueOf(data);
    }

    public String asSQLValue() {
        return asString();
    }
    
    public String asString() {
        return String.valueOf(_data);
    }

    public Float asFloat() {
        return _data.floatValue();
    }

    public Double asDouble() {
        return _data.doubleValue();
    }

    public Integer asInteger() {
        return _data.intValue();
    }

    public Long asLong() {
        return _data;
    }

    public Date asDate() {
        return null;
    }
    
    public boolean isNull() {
        return _data == null;
    }

    public InstanceId asInstanceId() {
        return null;
    }

	public void insert(PreparedStatement stmt, int idx) throws SQLException {
        stmt.setLong(idx, _data);
	}

	@Override
	public Boolean asBoolean() {
		return 1L == _data;
	}
}
