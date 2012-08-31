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

import java.util.ArrayList;
import java.util.List;

import com.paxxis.cornerstone.scripting.parser.CSLRuntime;

/**
 * All rule variables are subclasses of this class.
 *
 * @author Robert Englander
 */
public abstract class RuleVariable extends IValue {

    private static final long serialVersionUID = 1L;

    private static enum Methods {
	isNull
    }
	
    // the parameter name
    private String _name = null;

    // direct listeners of changes
    private final ArrayList<VariableChangeListener> _listeners = new ArrayList<VariableChangeListener>();
    
    // the monitor
    protected transient CSLRuntime runtime = null;

    // durable?
    private boolean _durable = false;
    
    // dynamic?
    private boolean _dynamic = false;
    
    // use macro expansion?
    private boolean macro = false;
    
    private boolean hasParameterDefault = false;

    // can user change the value through script expressions?
    private boolean userMutable = true;
    
    private IValue _expression = null;
    private List<RuleVariable> _dependents = new ArrayList<RuleVariable>();
    private List<RuleVariable> _dependencies = new ArrayList<RuleVariable>();
    
    /**
     * this method is called from with RuleVariable.  It manages dependent variable triggering
     * so that subclasses don't have to.
     * @param val
     */
    protected abstract void setValue(IValue val);

    public abstract void resetValue();

    public RuleVariable() {

    }
    
    public void addVariableChangeListener(VariableChangeListener listener) {
        synchronized (_listeners) {
            if (!_listeners.contains(listener)) {
                _listeners.add(listener);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public void notifyVariableChangeListeners() {
        List<VariableChangeListener> listeners = null;
        synchronized (_listeners) {
            listeners = (ArrayList<VariableChangeListener>)_listeners.clone();
        }
        
        for (VariableChangeListener listener : listeners) {
            listener.onChange(this);
        }
    }
    
    public void reset() {
        clearDependents();
        resetValue();
    }

    public void setParameterDefaultValue(String val) {
    }
    
    protected void setHasParameterDefault(boolean val) {
	this.hasParameterDefault = val;
    }
    
    protected boolean getHasParameterDefault() {
	return this.hasParameterDefault;
    }
    
    public final void setValue(IValue val, boolean trigger) {
        if (trigger) {
            buildDependencies(val);
        }

        setValue(val);

        if (trigger) {
            recalcDependents();
        }
    }
    
    public void addDependent(RuleVariable dep) {
        if (!_dependents.contains(dep)) {
            _dependents.add(dep);
        }
    }
    
    public void removeDependent(RuleVariable dep) {
        if (_dependents.contains(dep)) {
            _dependents.remove(dep);
        }
    }
    
    public void findVariables(List<RuleVariable> deps) {
        if (getName() != null) {
            deps.add(this);
        }
    }

    protected void clearDependents() {
        if (!_durable) {
            for (RuleVariable var : _dependencies) {
                var.removeDependent(this);
            }
            
            _dependencies.clear();
        }
    }
    
    protected void buildDependencies(IValue exp) {
        if (_dynamic) {
            for (RuleVariable var : _dependencies) {
                var.removeDependent(this);
            }
            
            _dependencies.clear();
            
            exp.findVariables(_dependencies);

            for (RuleVariable var : _dependencies) {
                if (var != this) {
                    var.addDependent(this);
                }
            }
            
            _expression = exp;
        }
    }
    
    public void appendDependents(String name, List<RuleVariable> deps) {
        for (RuleVariable var : _dependents) {
            if (var.getName().equals(name)) {
                throw new RuntimeException("Circular Reference To Variable '" + name + "'");
            }
            
            deps.add(var);
            var.appendDependents(name, deps);
        }
    }

    protected void recalcDependents() {
        // get an ordered list of variables dependent on this change, traversing
        // all the way down the tree
	ExtendedArrayList<RuleVariable> depVars = new ExtendedArrayList<RuleVariable>();
        appendDependents(getName(), depVars);
        int last = depVars.size() - 1;
        
        List<RuleVariable> calcVars = new ArrayList<RuleVariable>();
        for (int i = 0; i <= last; i++) {
            RuleVariable v = depVars.get(i);
            int next = i + 1;
            boolean keep = true;
            if (next <= last) {
                keep = !depVars.subList(next, last).contains(v);
            }
            
            if (keep) {
                calcVars.add(v);
            }
        }
        
        for (RuleVariable var : calcVars) {
            var.recalc();
        }
    }

    protected void recalc() {
        setValue(_expression.evaluate(), false);
    }
    
    public void setDurable(boolean durable) {
        _durable = durable;
    }
    
    public boolean isDurable() {
        return _durable;
    }
    
    public void setDynamic(boolean dynamic) {
        _dynamic = dynamic;
    }
    
    public boolean isDynamic() {
        return _dynamic;
    }
    
    public void setUserMutable(boolean userMutable) {
        this.userMutable = userMutable;
    }

    public boolean isUserMutable() {
        return userMutable;
    }
    
    /**
     * Subclasses that support macro expansion must override this.
     */
    public boolean supportsMacroExpansion() {
	return false;
    }
    
    public void setMacro(boolean macro) throws RuleCreationException {
	if (macro) {
	    if (supportsMacroExpansion()) {
		this.macro = macro;
	    } else {
		throw new RuleCreationException(this.getType() + " type does not support macro expansion.");
	    }
	}
    }
    
    public boolean isMacro() {
	return macro;
    }
    
    protected void checkUserMutable() {
        if (!userMutable) {
            throw new RuntimeException("Variable '" + getName() + "' can't be modified directly.");
        }
    }

    public boolean isArray() {
        return false;
    }

    public boolean isTable() {
        return false;
    }

    /**
     * All variables are objects now.
     * 
     * @return true
     */
    public final boolean isObject() {
        return true;
    }

    public RuleVariable(String name) {
        this._name = name;
    }

    public boolean hasName() {
        return _name != null;
    }

    public String getName() {
        return _name;
    }

    public boolean methodHasReturn(String name) {
	switch (Methods.valueOf(name)) {
	    case isNull:
		return true;
	}
		
	return false;
    }

    public int getMethodParameterCount(String name) {
	switch (Methods.valueOf(name)) {
	    case isNull:
		return 0;
	}
		
	return 0;
    }

    public IValue executeMethod(String name, List<IValue> params) {
	switch (Methods.valueOf(name)) {
	    case isNull:
		return new BooleanVariable(null, isNull());
	}
	
	return null;
    }
    
    /**
     * Is the object null?
     */
    public abstract boolean isNull();

    public abstract String getType();
    
    public void setRuntime(CSLRuntime agent) {
        runtime = agent;
    }
}
