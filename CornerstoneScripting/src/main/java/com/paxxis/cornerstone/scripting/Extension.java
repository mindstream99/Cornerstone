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

import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class Extension extends RuleVariable {

    private static final long serialVersionUID = 1L;

    private static enum Methods {
        getName,
        getId,
        getDescription,
        getShapes,
        getFieldValues;
        
	public static boolean contains(String name) {
	    boolean contains = false;
	    for (Methods option : Methods.values()) {
		if (option.toString().equals(name)) {
		    contains = true;
		    break;
		}
	    }
	
	    return contains;
	}
    }

    private ExtensionHelper helper = null;

    public Extension() {

    }

    public Extension(String name) {
        super(name);
    }

    public boolean isNull() {
      //return null == chimeExtension;
      return false;
    }
    
    @Override
    public String getType() {
        return "Extension";
    }

    public void initialize()
    {
    }

    public void setMonitor(CSLRuntime agent) {
        super.setMonitor(agent);
        init();
    }
    
    private void init() {
        ServiceContextProvider provider = _monitor.getServiceContextProvider();
        if (provider == null) {
            throw new RuntimeException("No Service Context Provider available");
        }

        helper = _monitor.getServiceContextProvider().createExtensionHelper();
        helper.initialize();

        if (_monitor != null) {
            _monitor.variableChange(this);
        }
    }

    public boolean methodHasReturn(String name) {
    	if (Methods.contains(name)) {
            return helper.methodHasReturn(name);
    	}
    	
    	return super.methodHasReturn(name);
    }

    public int getMethodParameterCount(String name) {
    	if (Methods.contains(name)) {
            return helper.getMethodParameterCount(name);
    	}
    	
    	return super.getMethodParameterCount(name);
    }

    public IValue executeMethod(String name, List<IValue> params) {
    	if (Methods.contains(name)) {
            return helper.executeMethod(name, params);
    	}

    	return super.executeMethod(name, params);
    }

    @Override
    protected void setValue(IValue val) {
        // if the value being assigned is an object expression,
        // then we need to evaluate it
        @SuppressWarnings("unused")
	IValue value = val;
        if (val instanceof ObjectMethodExpression)
        {
            ObjectMethodExpression m = (ObjectMethodExpression)val;
            value = m.execute();
        }
        else if (val instanceof ArrayIndexer)
        {
            // we need to evaluate the indexed value
            ArrayIndexer indexer = (ArrayIndexer)val;
            value = (IValue)indexer.valueAsObject();
        }
        else if (val instanceof TableIndexer)
        {
            // we need to evaluate the indexed value
            TableIndexer indexer = (TableIndexer)val;
            value = (IValue)indexer.valueAsObject();
        }
    }

    @Override
    public void resetValue() {
    }

    @Override
    public String valueAsString() {
        String id = "";
        return id;
    }

    @Override
    public Double valueAsDouble() {
    	if (isNull()) {
    	    return null;
    	}
        return 0.0;
    }

    @Override
    public Integer valueAsInteger() {
    	if (isNull()) {
    	    return null;
    	}
        return 0;
    }

    @Override
    public Boolean valueAsBoolean() {
        return !isNull();
    }

    @Override
    public Object valueAsObject() {
        return this;
    }

    @Override
    public IValue evaluate() {
        return this;
    }

}
