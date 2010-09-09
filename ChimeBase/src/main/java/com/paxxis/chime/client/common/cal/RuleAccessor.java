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
 * The RuleAccessor is used to invoke another rule.
 *
 * @author Robert Englander
 */
public class RuleAccessor extends RuleVariable implements IRuleObject
{
    private static enum Methods
    {
        invoke,
        valueOf
    }
    
    // the name of the rule to invoke
    StringVariable _ruleName = new StringVariable(null, "");

    // the queue we're on
    InstructionQueue _queue;

    public RuleAccessor() {

    }
    
    public RuleAccessor(String name, InstructionQueue queue)
    {
        super(name);
        _queue = queue;
    }

    public void resetValue()
    {
    }
    
    public String getType()
    {
        return "Rule Accessor";
    }
    
    public boolean isObject()
    {
        return true;
    }

    /**
     * Sets the value; in this case it's the rule name.
     * @param val
     */
    public void setValue(IValue val, boolean trigger)
    {
        _ruleName = new StringVariable(null, val.valueAsString());

        _monitor.variableChange(this);
    }

    public String valueAsString()
    {
        return _ruleName.valueAsString();
    }

    public double valueAsDouble()
    {
        return _ruleName.valueAsDouble();
    }

    public int valueAsInteger()
    {
        return _ruleName.valueAsInteger();
    }

    public boolean valueAsBoolean()
    {
        return _ruleName.valueAsBoolean();
    }

    public Object valueAsObject()
    {
        return _ruleName.valueAsObject();
    }

    public IValue evaluate()
    {
        // this is a no-op
        return this;
    }

    public boolean methodHasReturn(String name)
    {
        switch (Methods.valueOf(name))
        {
            case invoke:
                Rule rule = _queue.getRuleSet().getRule(_ruleName.valueAsString());
                return rule.hasReturnValue();
            case valueOf:
                return true;
        }

        return false;
    }

    public int getMethodParameterCount(String name)
    {
        switch (Methods.valueOf(name))
        {
            case invoke:
                return -1;
            case valueOf:
                return 1;
        }

        return 0;
    }

    public IValue executeMethod(String name, List<IValue> params)
    {
        switch (Methods.valueOf(name))
        {
            case invoke:
                return invokeRule(params);
            case valueOf:
                return getRuleValue(params);
        }

        return null;
    }

    IValue getRuleValue(List<IValue> params)
    {
        Rule rule = _queue.getRuleSet().getRule(_ruleName.valueAsString());
        RuleVariable rv = rule.getQueue().getVariable(params.get(0).valueAsString());
        return rv;
    }
    
    IValue invokeRule(List<IValue> params)
    {
        Rule rule = _queue.getRuleSet().getRule(_ruleName.valueAsString());
        
        
        rule.invoke(params);

        IValue result = null;
        if (rule.hasReturnValue())
        {
            result = rule.getReturnValue();
        }
        return result;
    }
}
