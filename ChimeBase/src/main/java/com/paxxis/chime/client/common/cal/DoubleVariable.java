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
 *
 * @author Robert Englander
 */
public class DoubleVariable extends RuleVariable
{
    // the value
    double _value = 0.0;

    public DoubleVariable() {

    }
    
    public DoubleVariable(String name)
    {
        super(name);
    }

    public DoubleVariable(String name, double value)
    {
        super(name);
        _value = value;
    }

    public DoubleVariable(String name, String value)
    {
        super(name);
        _value = new Double(value).doubleValue();
    }

    public String getType()
    {
        return "Double";
    }
    
    public void resetValue()
    {
        _value = 0.0;
    }
    
    public void setValue(IValue val, boolean trigger)
    {
        if (trigger)
        {
            buildDependencies(val);
        }
        
        _value = val.valueAsDouble();

        _monitor.variableChange(this);

        if (trigger)
        {
            recalcDependents();
        }
    }

    public Object valueAsObject()
    {
        return new Double(_value);
    }

    public String valueAsString()
    {
        return Double.toString(_value);
    }

    public double valueAsDouble()
    {
        return _value;
    }

    public int valueAsInteger()
    {
        return (int)_value;
    }

    public boolean valueAsBoolean()
    {
        return _value == 1.0;
    }

    public IValue evaluate()
    {
        return new DoubleVariable(null, _value);
    }

}
