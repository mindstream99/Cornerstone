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

package com.paxxis.cornerstone.scripting;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Robert Englander
 */
public class DateVariable extends RuleVariable {
    private static final long serialVersionUID = 2L;
    private static MethodProvider<DateVariable> methodProvider = new MethodProvider<DateVariable>(DateVariable.class);
    static {
        methodProvider.initialize();
    }

    // the value
    private Date value = null;
    private Date parameterDefault = null;

    public DateVariable() {

    }
    
    public DateVariable(String name) {
        super(name);
    }

    public DateVariable(String name, Date value) {
        super(name);
        this.value = value;
    }

    public DateVariable(String name, String value) {
        super(name);
        setValue(value);
    }

    @Override
    protected MethodProvider<DateVariable> getMethodProvider() {
        return methodProvider;
    }
    
    public String getType() {
        return "Date";
    }
    
    public void resetValue() {
    	if (this.getHasParameterDefault() && value == null) {
    	    value = parameterDefault;
    	}
    }
    
	public String getDefaultValue() {
		return (parameterDefault == null) ? "null" : parameterDefault.toString();
	}

	public void setParameterDefaultValue(String val) {
    	if (val == null) {
    	    this.parameterDefault = null;
    	} else {
            try {
                try {
                    parameterDefault = new Date(java.sql.Timestamp.valueOf(val).getTime());
                } catch (Exception ee) {
                    parameterDefault = new Date(java.sql.Date.valueOf(val).getTime());
                }
            } catch (Exception e) {
                throw new ScriptExecutionException(130, "Bad date format used to set date default value: " + val);
            }
    	}
		setHasParameterDefault(true);
	}

	public boolean isValueNull() {
    	return null == value;
    }

    @CSLMethod
    public IValue format(IValue fmt) {
        SimpleDateFormat formatter = new SimpleDateFormat(fmt.valueAsString());
        String res = formatter.format(value);
        return new StringVariable(null, res);
    }

    private IValue increment(long inc) {
        long newVal = inc + value.getTime();
        Date temp = new Date(newVal);
        this.setValue(new DateVariable(null, temp), true);
        return new BooleanVariable(null, true);
    }
    
    @CSLMethod
    public IValue incrementDays(IValue incr) {
        long inc = incr.valueAsInteger();
        inc = inc * 24 * 60 * 60 * 1000;
        return increment(inc);
    }
    
    @CSLMethod
    public IValue incrementHours(IValue incr) {
        long inc = incr.valueAsInteger();
        inc = inc * 60 * 60 * 1000;
        return increment(inc);
    }
    
    @CSLMethod
    public IValue incrementMinutes(IValue incr) {
        long inc = incr.valueAsInteger();
        inc = inc * 60 * 1000;
        return increment(inc);
    }
    
    @CSLMethod
    public IValue incrementSeconds(IValue incr) {
        long inc = incr.valueAsInteger();
        inc = inc * 1000;
        return increment(inc);
    }

    @CSLMethod
    public void setNow() {
        Date temp = new Date();
        this.setValue(new DateVariable(null, temp), true);
    }

    @CSLMethod
    public IValue isAfter(IValue param) {
        BooleanVariable result = new BooleanVariable(null);
        if (param instanceof DateVariable) {
            if (value.compareTo((Date)param.valueAsObject()) > 0) {
                result.setValue(true);
            } else {
                result.setValue(false);
            }
        } else {
            throw new ScriptExecutionException(131, "Non Date value passed to after() method: " + getName());
        }

        return result;
    }

    @SuppressWarnings("deprecation")
    private void setValue(RuleVariable rv) {
    	if (rv instanceof DateVariable) {
    	    DateVariable dv = (DateVariable)rv;
    	    value = dv.value;
    	} else {
    	    String sval = rv.valueAsString();
    	    setValue(sval);
    	}
    }
    
    protected void setValue(IValue val) {
        if (val instanceof RuleVariable) {
        	RuleVariable rv = (RuleVariable)val;
        	setValue(rv);
        } else {
            String dstring = val.valueAsString();
            setValue(dstring);
        }
    }
    
    @SuppressWarnings("deprecation")
    private void setValue(String dt) {
    	if (dt == null) {
    	    value = null;
    	} else {
            try {
                try {
                    parameterDefault = new Date(java.sql.Timestamp.valueOf(dt).getTime());
                } catch (Exception ee) {
                    parameterDefault = new Date(java.sql.Date.valueOf(dt).getTime());
                }
            } catch (Exception e) {
                throw new ScriptExecutionException(132, "Bad date format used to set date value: " + dt);
            }
    	}
        
        if (runtime != null) {
            runtime.variableChange(this);
        }
    }

    public Object valueAsObject() {
        return value;
    }

    /**
     * returns the date value as a String as formatted by the DateFormatter
     *  using the default constructor
     * @return
     */
    @SuppressWarnings("deprecation")
    public String valueAsString() {
    	if (value == null) {
    		return "null";
    	} 
    	
    	java.sql.Timestamp dt = new java.sql.Timestamp(value.getTime());
    	return dt.toString();
    }

    public Double valueAsDouble() {
    	if (value == null) {
    	    return null;
    	}
    	
        return Double.valueOf(value.getTime());
    }

    public Integer valueAsInteger() {
    	if (value == null) {
    	    return null;
    	}

    	return 0;
    }

    public Boolean valueAsBoolean() {
    	return null != value;
    }

    @Override
    public ResultVariable valueAsResult() {
        ResultVariable res = new ResultVariable(null, valueAsBoolean());
        return res;
    }

    public IValue evaluate() {
        return new DateVariable(null, value);
    }

}
