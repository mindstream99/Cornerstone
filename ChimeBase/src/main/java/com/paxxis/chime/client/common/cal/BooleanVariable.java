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
 * A BooleanVariable is used to represent local Boolean type
 * variables, as well as anonymous boolean values.
 *
 * @author Robert Englander
 */
public class BooleanVariable extends RuleVariable
{
    // the value
    boolean _value = true;

    public BooleanVariable() {

    }
    /**
     * Constructs the variable
     * @param name the name of the variable, or null if anonymous
     */
    public BooleanVariable(String name)
    {
        // pass the name to the base class constructor
        super(name);
    }

    public String getType()
    {
        return "Boolean";
    }
    
    public void resetValue()
    {
        _value = true;
    }
    
    /**
     * Constructs the variable.
     * @param name the name of the variable, or null if anonymous
     * @param value the initial value
     */
    public BooleanVariable(String name, boolean value)
    {
        super(name);
        _value = value;
    }

    /**
     * Sets the value
     * @param val the new value
     */
    public void setValue(boolean val)
    {
        _value = val;

        _monitor.variableChange(this);
    }

    /**
     * Sets the value
     * @param val the new value
     */
    public void setValue(IValue val, boolean trigger)
    {
        if (trigger)
        {
            buildDependencies(val);
        }
        
        setValue(val.valueAsBoolean());

        if (trigger)
        {
            recalcDependents();
        }
    }

    /**
     * @return the value as an Object
     */
    public Object valueAsObject()
    {
        return new Boolean(valueAsBoolean());
    }

    /**
     * @return the value as a String
     */
    public String valueAsString()
    {
        if (_value == true)
        {
            return "true";
        }
        else
        {
            return "false";
        }
    }

    /**
     * @return the value as a double
     */
    public double valueAsDouble()
    {
        if (_value == true)
        {
            return 1.0;
        }
        else
        {
            return 0.0;
        }
    }

    /**
     * @return the value as an int
     */
    public int valueAsInteger()
    {
        if (_value == true)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    /**
     * @return the value as a boolean
     */
    public boolean valueAsBoolean()
    {
        return _value;
    }

    /**
     * evaluates this variable without returning its value.
     */
    public IValue evaluate()
    {
        // this is a no-op
        return this;
    }

}
