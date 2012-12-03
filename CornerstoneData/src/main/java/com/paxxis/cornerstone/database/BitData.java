package com.paxxis.cornerstone.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import com.paxxis.cornerstone.base.InstanceId;

/**
 * Represents bit data. This is different from boolean only in that it will return 1 or 0 instead of true and false if used to create a sql command. 
 * 
 * 
 * java.sql.TYPES = BIT (-7)
 * @author kglover
 *
 */
public class BitData implements IDataValue {
	
	private static final long serialVersionUID = 1L;

	private Boolean _data;
	
    public BitData(Boolean data) {
        _data = data;
    }
    
    public BitData(Integer data) {
    	
    	if(data == 1)
    		_data = true;
    	else
    		_data = false;
    }
    
    public String asSQLValue() {
        return asString();
    }
    
    public String asString() {
    	if(_data)
    		return "1";
    	else
    		return "0";
    }

    public Float asFloat() {
        return null;
    }

    public Double asDouble() {
        return null;
    }

    public Integer asInteger() {
        if(_data)
        	return 1;
    	
        return 0;
    }

    public Long asLong() {
    	if(_data)
        	return 1L;
    	
        return 0L;
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
       stmt.setBoolean(idx, _data);
	}

	@Override
	public Boolean asBoolean() {
		return _data;
	}
}
