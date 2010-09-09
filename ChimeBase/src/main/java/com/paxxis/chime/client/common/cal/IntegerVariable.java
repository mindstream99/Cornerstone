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

/**
 * @author Robert Englander
 */
public class IntegerVariable extends RuleVariable
{
    // the value
    int _value = 0;

    public IntegerVariable() {

    }
    
    public IntegerVariable(String name)
    {
        super(name);
    }

    public IntegerVariable(String name, String value)
    {
        super(name);
        _value = new Integer(value).intValue();
    }

    public IntegerVariable(String name, int value)
    {
        super(name);
        _value = value;
    }

    public String getType()
    {
        return "Integer";
    }
    
    public void resetValue()
    {
        _value = 0;
    }
    
    public void setValue(int val)
    {
        _value = val;

        _monitor.variableChange(this);
    }

    public void setValue(IValue val, boolean trigger)
    {
        if (trigger)
        {
            buildDependencies(val);
        }

        setValue(val.valueAsInteger());
        
        if (trigger)
        {
            recalcDependents();
        }
    }

    public Object valueAsObject()
    {
        return new Integer(_value);
    }

    public String valueAsString()
    {
        return Integer.toString(_value);
    }

    public double valueAsDouble()
    {
        return (double)_value;
    }

    public int valueAsInteger()
    {
        return _value;
    }

    public boolean valueAsBoolean()
    {
        return _value == 1;
    }

    public IValue evaluate()
    {
        return new IntegerVariable(null, _value);
    }

}
