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

import java.util.List;

/**
 * @author Robert Englander
 */
public class StringVariable extends RuleVariable implements IRuleObject
{
    private static enum Methods
    {
        length,
        makeUpperCase,
        subString,
        contains
    }

    // the value
    String _value = "";

    public StringVariable() {

    }
    
    public StringVariable(String name)
    {
        super(name);
    }

    public StringVariable(String name, String value)
    {
        super(name);
        _value = value;
    }

    public String getType()
    {
        return "String";
    }
    
    public void resetValue()
    {
        _value = "";
    }
    
    public boolean methodHasReturn(String name)
    {
        switch (Methods.valueOf(name))
        {
            case length:
            case subString:
            case contains:
                return true;
        }

        return false;
    }

    public boolean isObject()
    {
        return true;
    }

    public int getMethodParameterCount(String name)
    {
        switch (Methods.valueOf(name))
        {
            case length:
                return 0;
            case makeUpperCase:
                return 0;
            case subString:
                return 2;
            case contains:
                return 1;
        }

        return 0;
    }

    public IValue executeMethod(String name, List<IValue> params)
    {
        switch (Methods.valueOf(name))
        {
            case length:
                return getLength(params);
            case makeUpperCase:
                makeUpperCase(params);
                return new BooleanVariable(null,true);
            case subString:
                return subString(params);
            case contains:
                return contains(params);
        }

        return null;
    }

    /**
     * determines if the passed string parameter is contained
     * within this string
     * @param params
     * @return
     */
    private IValue contains(List<IValue> params)
    {
        String sub = params.get(0).valueAsString();
        boolean contains = -1 != _value.indexOf(sub);
        return new BooleanVariable(null, contains);
    }

    private IValue getLength(List<IValue> params)
    {
        // we don't use any parameters
        int size = _value.length();
        return new IntegerVariable(null, size);
    }

    private void makeUpperCase(List<IValue> params)
    {
        // this function doesn't use any parameters
        setValue(new StringVariable(null, _value.toUpperCase()), true);
    }

    /**
     * subString is used for obtaining a segment of this string, hence it's usage is akin to the underlying
     * Java method function of substring(int beginIndex, int endIndex), with the first character being index 0
     * @param params  beginIndex - the beginning index, inclusive.
     *                endIndex - the ending index, exclusive.
     * @return    a new string that is a substring of this string. The substring begins at the specified beginIndex and extends to the character at index endIndex - 1. Thus the length of the substring is endIndex-beginIndex
     * Throws:
     *       IndexOutOfBoundsException - if the beginIndex is negative, or endIndex is larger than the length of this String object, or beginIndex is larger than endIndex.
     */
    private IValue subString(List<IValue> params)
    {
        int beginIndex = params.get(0).valueAsInteger();
        int endIndex = params.get(1).valueAsInteger();
        String substring = _value.substring(beginIndex, endIndex);
        return new StringVariable("",substring);

    }

    public void setValue(IValue val, boolean trigger)
    {
        if (trigger)
        {
            buildDependencies(val);
        }
        
        _value = val.valueAsString();

        _monitor.variableChange(this);

        if (trigger)
        {
            recalcDependents();
        }
    }

    public Object valueAsObject()
    {
        return _value;
    }

    public String valueAsString()
    {
        return _value;
    }

    public double valueAsDouble()
    {
        double result = Double.valueOf(_value).doubleValue();
        return result;
    }

    public int valueAsInteger()
    {
        // we need to truncate doubles...
        int result = (int)valueAsDouble();
        return result;
    }

    public boolean valueAsBoolean()
    {
        return _value.equals("true");
    }

    public IValue evaluate()
    {
        return new StringVariable(null, _value);
    }

}
