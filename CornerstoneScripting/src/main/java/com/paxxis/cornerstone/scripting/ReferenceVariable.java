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



/**
 *
 * @author Robert Englander
 */
public class ReferenceVariable extends RuleVariable {
    private static final long serialVersionUID = 1L;

    // the expression that is being referenced
    private IValue ref = null;

    public ReferenceVariable() {
    }
    
    public ReferenceVariable(String name) {
        super(name);
    }

    public String getType() {
        return "Reference";
    }
    
    public void resetValue() {
        ref = null;
    }
    
    protected void setValue(IValue val) {
        ref = val;
        if (_monitor != null) {
            _monitor.variableChange(this);
        }
    }

    public boolean isNull() {
	boolean isNull = ref == null;
	if (!isNull) {
	    // parameter passing can result in a null reference being injected into
	    // a reference parameter, so we need to consider that possibility.
	    if (ref instanceof ReferenceVariable) {
		ReferenceVariable rv = (ReferenceVariable)ref;
		isNull = rv.isNull();
	    }
	}
		
	return isNull;
    }
	
    public String valueAsString() {
    	if (isNull()) {
    	    return null;
    	}
        return ref.valueAsString();
    }

    public Double valueAsDouble() {
    	if (isNull()) {
    	    return null;
    	}
        return ref.valueAsDouble();
    }

    public Integer valueAsInteger() {
    	if (isNull()) {
    	    return null;
    	}
        return ref.valueAsInteger();
    }

    public Boolean valueAsBoolean() {
    	if (isNull()) {
    	    return null;
    	}
        return ref.valueAsBoolean();
    }

    @Override
    public ResultVariable valueAsResult() {
        ResultVariable res = new ResultVariable(null, valueAsBoolean());
        return res;
    }

    public Object valueAsObject() {
    	if (isNull()) {
    	    return null;
    	}
        return ref.valueAsObject();
    }

    public IValue evaluate() {
        return ref.evaluate();
    }

}
