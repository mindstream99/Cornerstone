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

class ArrayListEx extends ArrayList<RuleVariable> {

    public ArrayListEx subList(int start, int end) {

        ArrayListEx sub = new ArrayListEx();
        for (int i = start; i <= end; i++) {
            sub.add(get(i));
        }

        return sub;
    }
}

/**
 * All rule variables are subclasses of this class.
 *
 * @author Robert Englander
 */
public abstract class RuleVariable extends IValue
{
    // the parameter name
    private String _name = null;

    // direct listeners of changes
    private final ArrayList<VariableChangeListener> _listeners = new ArrayList<VariableChangeListener>();
    
    // the monitor
    protected transient Runtime _monitor = null;

    // durable?
    private boolean _durable = false;
    
    // dynamic?
    private boolean _dynamic = false;
    private IValue _expression = null;
    private List<RuleVariable> _dependents = new ArrayList<RuleVariable>();
    private List<RuleVariable> _dependencies = new ArrayList<RuleVariable>();
    
    public abstract void setValue(IValue val, boolean trigger);
    public abstract void resetValue();

    public RuleVariable() {

    }
    
    public void addVariableChangeListener(VariableChangeListener listener)
    {
        synchronized (_listeners)
        {
            if (!_listeners.contains(listener))
            {
                _listeners.add(listener);
            }
        }
    }
    
    public void notifyVariableChangeListeners()
    {
        List<VariableChangeListener> listeners = null;
        synchronized (_listeners)
        {
            listeners = (ArrayList<VariableChangeListener>)_listeners.clone();
        }
        
        for (VariableChangeListener listener : listeners)
        {
            listener.onChange(this);
        }
    }
    
    public void reset()
    {
        clearDependents();
        resetValue();
    }
    
    public void addDependent(RuleVariable dep)
    {
        if (!_dependents.contains(dep))
        {
            _dependents.add(dep);
        }
    }
    
    public void removeDependent(RuleVariable dep)
    {
        if (_dependents.contains(dep))
        {
            _dependents.remove(dep);
        }
    }
    
    public void findVariables(List<RuleVariable> deps)
    {
        if (getName() != null)
        {
            deps.add(this);
        }
    }

    protected void clearDependents()
    {
        if (!_durable)
        {
            for (RuleVariable var : _dependencies)
            {
                var.removeDependent(this);
            }
            
            _dependencies.clear();
        }
    }
    
    protected void buildDependencies(IValue exp)
    {
        if (_dynamic)
        {
            for (RuleVariable var : _dependencies)
            {
                var.removeDependent(this);
            }
            
            _dependencies.clear();
            
            exp.findVariables(_dependencies);

            for (RuleVariable var : _dependencies)
            {
                if (var != this)
                {
                    var.addDependent(this);
                }
            }
            
            
            _expression = exp;
        }
    }
    
    public void appendDependents(String name, List<RuleVariable> deps)
    {
        for (RuleVariable var : _dependents)
        {
            if (var.getName().equals(name))
            {
                throw new RuntimeException("Circular Reference To Variable '" + name + "'");
            }
            
            deps.add(var);
            var.appendDependents(name, deps);
        }
    }

    protected void recalcDependents()
    {
        // get an ordered list of variables dependent on this change, traversing
        // all the way down the tree
        ArrayListEx depVars = new ArrayListEx();
        appendDependents(getName(), depVars);
        int last = depVars.size() - 1;
        
        List<RuleVariable> calcVars = new ArrayList<RuleVariable>();
        for (int i = 0; i <= last; i++)
        {
            RuleVariable v = depVars.get(i);
            int next = i + 1;
            boolean keep = true;
            if (next <= last)
            {
                keep = !depVars.subList(next, last).contains(v);
            }
            
            if (keep)
            {
                calcVars.add(v);
            }
        }
        
        for (RuleVariable var : calcVars)
        {
            var.recalc();
        }
    }

    protected void recalc()
    {
        setValue(_expression.evaluate(), false);
    }
    
    public void setDurable(boolean durable)
    {
        _durable = durable;
    }
    
    public boolean isDurable()
    {
        return _durable;
    }
    
    public void setDynamic(boolean dynamic)
    {
        _dynamic = dynamic;
    }
    
    public boolean isDynamic()
    {
        return _dynamic;
    }
    
    public boolean isArray()
    {
        return false;
    }

    public boolean isTable()
    {
        return false;
    }

    public boolean isObject()
    {
        return false;
    }

    public RuleVariable(String name)
    {
        _name = name;
    }

    public boolean hasName()
    {
        return _name != null;
    }

    public String getName()
    {
        return _name;
    }

    public abstract String getType();
    
    public void setMonitor(Runtime agent)
    {
        // if we have a null name, or if we have
        // an internal name that starts with a #,
        // then we don't want to be monitored
        //if (_name != null && !_name.startsWith("#"))
        {
            _monitor = agent;
        }
    }
}
