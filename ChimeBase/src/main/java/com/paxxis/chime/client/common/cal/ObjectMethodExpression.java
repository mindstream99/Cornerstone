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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class ObjectMethodExpression extends IValue
{
    IRuleObject _object = null;
    String _methodName;
    List<IValue> _parameters = new ArrayList<IValue>();
    boolean _validateValueOf = false;
    InstructionQueue _queue = null;
    
    public ObjectMethodExpression()
    {
    }

    public void findVariables(List<RuleVariable> deps)
    {
        for (IValue param : _parameters)
        {
            param.findVariables(deps);
        }
    }

    public boolean isArray()
    {
        return false;
    }

    public boolean isTable()
    {
        return false;
    }

    public void setObjectName(String name, InstructionQueue queue)
    {
        _queue = queue;
        RuleVariable rv = queue.getVariable(name);

        if (rv == null)
        {
            Rule r = queue.getRuleSet().getRule(name);
            if (r == null)
            {
                throw new RuntimeException("No such rule or object '" + name + "'.");
            }
            
            RuleAccessor ra = new RuleAccessor(null, queue);
            ra.setMonitor(queue.getRuleSet().getMonitor());
            ra.setValue(new StringVariable(null, name), true);
            rv = ra;
        }
        else if (!rv.isObject())
        {
            throw new RuntimeException("Variable '" + name + "' is not an object.");
        }

        _object = (IRuleObject)rv;

    }

    public void setMethodName(String name, boolean validate)
    {
        setMethodName(name);
        _validateValueOf = validate;
    }
    
    public void setMethodName(String name)
    {
        if (_object == null)
        {
            throw new RuntimeException("Method '" + name + "' called on unknown object.");
        }
        
        _methodName = name;
    }

    public void addParameter(IValue val)
    {
        _parameters.add(val);
    }

    public void validateParameters()
    {
        int pcount = 0;
        try
        {
            pcount = _object.getMethodParameterCount(_methodName);
        }
        catch (Exception e)
        {
            if (_object instanceof RuleAccessor)
            {
                throw new RuntimeException("Invalid method name '" + _methodName + "' on Rule " + ((RuleAccessor)_object)._ruleName.valueAsString());
            }
            
            throw new RuntimeException("Unknown method name '" + _methodName + "' on Object " + ((RuleVariable)_object).getName());
        }
        
        if (_parameters.size() != pcount && pcount != -1)
        {
            throw new RuntimeException("Wrong number of parameters (" + _parameters.size() + ") for calling " + _methodName + " on Object ");
        }

        if (_object instanceof RuleAccessor)
        {
            RuleAccessor ra = (RuleAccessor)_object;
            if (_methodName.equals("valueOf") && _validateValueOf)
            {
                if (_parameters.get(0) instanceof RuleVariable)
                {
                    RuleVariable rv = (RuleVariable)_parameters.get(0);
                    if (rv.getName() == null)
                    {
                        String dataName = rv.valueAsString();
                        Rule rule = _queue.getRuleSet().getRule(ra.valueAsString());
                        RuleVariable variable = rule.getQueue().getVariable(dataName);
                        if (variable == null)
                        {
                            throw new RuntimeException("Unknown variable name '" + dataName + "' on Rule " + rule.getName());
                        }
                    }
                }
            }
        }
    }
    
    public Object valueAsObject()
    {
        if (!_object.methodHasReturn(_methodName))
        {
            // you shouldn't have called this!
            throw new RuntimeException("Can't get value of method or rule without a return value");
        }

        IValue result = execute();
        return result.valueAsObject();
    }

    public String valueAsString()
    {
        if (!_object.methodHasReturn(_methodName))
        {
            // you shouldn't have called this!
            throw new RuntimeException("Can't get value of method or rule without a return value");
        }

        IValue result = execute();
        return result.valueAsString();
    }

    public int valueAsInteger()
    {
        if (!_object.methodHasReturn(_methodName))
        {
            // you shouldn't have called this!
            throw new RuntimeException("Can't get value of method or rule without a return value");
        }

        IValue result = execute();
        return result.valueAsInteger();
    }

    public boolean valueAsBoolean()
    {
        if (!_object.methodHasReturn(_methodName))
        {
            // you shouldn't have called this!
            throw new RuntimeException("Can't get value of method or rule without a return value");
        }

        IValue result = execute();
        return result.valueAsBoolean();
    }

    public double valueAsDouble()
    {
        if (!_object.methodHasReturn(_methodName))
        {
            // you shouldn't have called this!
            throw new RuntimeException("Can't get value of method or rule without a return value");
        }

        IValue result = execute();
        return result.valueAsDouble();
    }

    public IValue evaluate()
    {
        return execute();
    }


    protected IValue execute()
    {
        return _object.executeMethod(_methodName, _parameters);
    }
}
