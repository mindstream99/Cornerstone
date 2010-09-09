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

package com.paxxis.chime.client.common.cal;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class DateVariable extends RuleVariable implements IRuleObject
{
    private static final String[] monthNames =
    {
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December"
    };

    private static enum Methods
    {
        after,
        getDay,
        getMonth,
        getMonthName,
        getYear,
        getHour,
        getMinutes,
        getSeconds,
        setNow
    }

    // the value
    Date _value = null;

    public DateVariable() {

    }
    
    public DateVariable(String name)
    {
        super(name);
    }

    public DateVariable(String name, Date value)
    {
        super(name);
        _value = value;
    }

    public DateVariable(String name, String value)
    {
        super(name);
        setValue(value);
    }

    public String getType()
    {
        return "Date";
    }
    
    public void resetValue()
    {
        _value = null;
    }
    
    public boolean isObject()
    {
        return true;
    }

    /**
     * Returns a boolean value that indicates if a
     * particular method has a return value.
     * @param name the method name
     * @return true if it has a return value, false otherwise.
     */
    public boolean methodHasReturn(String name)
    {
        switch (Methods.valueOf(name))
        {
            case after:
            case getDay:
            case getMonth:
            case getMonthName:
            case getYear:
            case getHour:
            case getMinutes:
            case getSeconds:
                return true;
        }

        // no return value
        return false;
    }

    public int getMethodParameterCount(String name)
    {
        switch (Methods.valueOf(name))
        {
            case after:
                return 1;
            case getMonth:
                return 0;
            case getMonthName:
                return 0;
            case getYear:
                return 0;
            case getDay:
                return 0;
            case getHour:
                return 0;
            case getMinutes:
                return 0;
            case getSeconds:
                return 0;
            case setNow:
                return 0;
        }
        
        return 0;
    }

    /**
     * Executes the specified method.  The parameters to
     * the method are supplied as an array of values.
     * @param name the method name
     * @param params the parameters to pass to the method.
     * @return the return value of the method
     */
    public IValue executeMethod(String name, List<IValue> params)
    {
        switch (Methods.valueOf(name))
        {
            case after:
                return isAfter(params);
            case getMonth:
                return getDateValue(params, Calendar.MONTH);
            case getMonthName:
                return getMonthName(params);
            case getYear:
                return getDateValue(params, Calendar.YEAR);
            case getDay:
                return getDateValue(params, Calendar.DAY_OF_MONTH);
            case getHour:
                return getDateValue(params, Calendar.HOUR);
            case getMinutes:
                return getDateValue(params, Calendar.MINUTE);
            case getSeconds:
                return getDateValue(params, Calendar.SECOND);
            case setNow:
                setNow(params);
                return new BooleanVariable(null, true);
        }

        // maybe we ought to be throwing an exception here, since
        // this should never happen.
        return null;
    }

    public IValue getDateValue(List<IValue> params, int field)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(_value);
        return new IntegerVariable(null, cal.get(field));
    }

    public IValue getMonthName(List<IValue> params)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(_value);
        return new StringVariable(null, monthNames[cal.get(Calendar.MONTH)]);
    }

    public void setNow(List<IValue> params)
    {
        _value = new Date();
    }

    public IValue isAfter(List<IValue> params)
    {
        // there should be 1 parameter and it has to
        // be a Date variable
        BooleanVariable result = new BooleanVariable(null);
        if (params.get(0) instanceof DateVariable)
        {
            if (_value.compareTo((Date)params.get(0).valueAsObject()) > 0)
            {
                result.setValue(true);
            }
            else
            {
                result.setValue(false);
            }
        }
        else
        {
            throw new RuntimeException("Non Date value passed to after() method: " + getName());
        }

        return result;
    }

    public void setValue(IValue val, boolean trigger)
    {
        // we need to know if the date is in database
        // format or not
        String dstring = val.valueAsString();
        Date d = new Date();

        if (trigger)
        {
            buildDependencies(val);
        }
        
        setValue(dstring);

        if (trigger)
        {
            recalcDependents();
        }
    }
    
    private void setValue(String dt)
    {
        try
        {
            _value = new Date(dt);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Bad date format used to set date value: " + dt);
        }
        
        _monitor.variableChange(this);
    }

    public Object valueAsObject()
    {
        return _value;
    }

    /**
     * returns the date value as a String as formatted by the DateFormatter
     *  using the default constructor
     * @return
     */
    public String valueAsString()
    {
        return _value.toLocaleString();
    }

    public double valueAsDouble()
    {
        return 0.0;
    }

    public int valueAsInteger()
    {
        return 0;
    }

    public boolean valueAsBoolean()
    {
        return _value.equals("true");
    }

    public IValue evaluate()
    {
        return new DateVariable(null, _value);
    }

}
