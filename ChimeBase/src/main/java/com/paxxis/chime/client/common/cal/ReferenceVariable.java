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
public class ReferenceVariable extends RuleVariable 
{
    // the expression that is being referenced
    IValue _ref = null;

    public ReferenceVariable() {

    }
    
    public ReferenceVariable(String name)
    {
        super(name);
    }

    public String getType()
    {
        return "Reference";
    }
    
    public void resetValue()
    {
        _ref = null;
    }
    
    public void setValue(IValue val, boolean trigger)
    {
        _ref = val;
        _monitor.variableChange(this);
    }

    public String valueAsString()
    {
        if (_ref == null)
        {
            return "";
        }
        return _ref.valueAsString();
    }

    public double valueAsDouble()
    {
        return _ref.valueAsDouble();
    }

    public int valueAsInteger()
    {
        return _ref.valueAsInteger();
    }

    public boolean valueAsBoolean()
    {
        return _ref.valueAsBoolean();
    }

    public Object valueAsObject()
    {
        return _ref.valueAsObject();
    }

    public IValue evaluate()
    {
        return this;
    }

}
