/* Copyright 2010 the original author or authors.
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
package com.paxxis.cornerstone.scripting.extension;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.paxxis.cornerstone.scripting.Array;
import com.paxxis.cornerstone.scripting.IValue;
import com.paxxis.cornerstone.scripting.RuleVariable;
import com.paxxis.cornerstone.scripting.StringVariable;

/**
 * 
 * @author Rob Englander
 *
 */
public class CSLExtensionHelper implements ExtensionHelper {

    private HashMap<String, Method> methods = new HashMap<String, Method>();
    private CSLExtension extension = null;
    
    public void initialize(CSLExtension ext) {
        try {
            extension = ext;
            String calClassName = extension.getCSLClassName();
            Class<?> clazz = Class.forName(calClassName);
            Method[] list = clazz.getMethods();
            for (Method m : list) {
                methods.put(m.getName(), m);
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(CSLExtensionHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean methodHasReturn(String name)
    {
        boolean result = false;
        Method m = methods.get(name);
        if (m != null) {
            result = !m.getReturnType().getName().equals("void");
        }

        return result;
    }

    public int getMethodParameterCount(String name)
    {
        int result = -1;
        Method m = methods.get(name);
        if (m != null) {
            result = m.getParameterTypes().length;
        }

        if (result == -1) {
            throw new RuntimeException("Extension " + extension.getId() + " has no such method: " + name);
        }

        return result;
    }

	private boolean isRuleVariable(Class<?> clazz) {

    	boolean isRuleVariable = false;
    	while (clazz.getSuperclass() != null) {
    	    if (clazz.getSuperclass().equals(RuleVariable.class)) {
    		isRuleVariable = true;
    		break;
    	    }
    	    clazz = clazz.getSuperclass();
    	}
    	
    	return isRuleVariable;
    }
    
    public IValue executeMethod(String name, List<IValue> params) {
        Method m = methods.get(name);
        Object[] paramArray = new Object[params.size()];
        for (int i = 0; i < params.size(); i++) {
            IValue param = params.get(i);
            if (m.getParameterTypes()[i].equals(String.class)) {
                paramArray[i] = param.valueAsString();
            } else if (m.getParameterTypes()[i].equals(Boolean.class) ||
        	m.getParameterTypes()[i].equals(Boolean.TYPE)) {
        	paramArray[i] = param.valueAsBoolean();
            } else if (isRuleVariable(m.getParameterTypes()[i])) {
            	paramArray[i] = param.valueAsObject();
            } else {
            	// defaulting to a number
            	paramArray[i] = param.valueAsDouble();
            }
        }

        try {
            Object result = m.invoke(extension, paramArray);
            if (result instanceof IValue) {
            	return (IValue)result;
            } else if (result instanceof List) {
                List<?> list = (List<?>)result;
                List<IValue> ivals = new ArrayList<IValue>();
                for (Object o : list) {
                    StringVariable var = new StringVariable(null, o.toString());
                    ivals.add(var);
                }

                Array returnArray = new Array(null);
                returnArray.initialize(ivals);
                return returnArray;
            } else {
                return new StringVariable(null, result.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException("Extension " + extension.getId() + " invocation failure: " + e.getCause().getLocalizedMessage());
        }
    }

    @Override
    public void initialize() {
    }
}
