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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.paxxis.cornerstone.scripting.parser.ParseException;

/**
 * 
 * @author Robert Englander
 *
 */
public class MethodProvider<T extends RuleVariable> {
    private Class<T> typeClass;
    private Map<String, Method> methods = new HashMap<String, Method>();
    private Map<String, Boolean> returns = new HashMap<String, Boolean>();
    private Map<String, Integer> paramCounts = new HashMap<String, Integer>();
    
    public MethodProvider(Class<T> clazz) {
        typeClass = clazz;
    }
    
    public void initialize() {
        Method[] classMethods = typeClass.getMethods();
        for (Method method : classMethods) {
            CSLMethod anno = method.getAnnotation(CSLMethod.class);
            if (anno != null) {
                String name = method.getName();

                Class<?> returnType = method.getReturnType();
                if (IValue.class.isAssignableFrom(returnType)) {
                    // this method has a return value
                    returns.put(name, true);
                } else if (void.class.equals(returnType)) {
                    // no return
                    returns.put(name, false);
                } else {
                    // bad return type
                    throw new RuntimeException("CSLMethod " + name + " has non IValue return type (" + returnType.getName() + ")");
                }
                
                Class<?>[] paramTypes = method.getParameterTypes();
                for (Class<?> paramType : paramTypes) {
                    if (!IValue.class.isAssignableFrom(paramType)) {
                        // bad parameter type
                        throw new RuntimeException("CSLMethod " + name + " has non IValue parameter type (" + paramType.getName() + ")");
                    }
                }
                
                paramCounts.put(name, paramTypes.length);
                methods.put(name, method);
            }
        }
    }
    
    public boolean hasReturn(String name) {
        return returns.get(name);
    }

    public int getParameterCount(String name) {
        return paramCounts.get(name);
    }

    public IValue execute(RuleVariable rv, String name, List<IValue> params) throws ParseException {
        Method method = methods.get(name);
        if (method == null) {
            throw new ParseException("No such method named " + name);
        }
        
        int paramCount = getParameterCount(name);
        if (paramCount != params.size()) {
            throw new ParseException("Wrong number of parameters");
        }

        try {
            Object result = method.invoke(rv, params.toArray());
            if (result != null) {
                return (IValue)result;
            } else {
                return new BooleanVariable(null, true);
            }
        } catch (Exception e) {
            throw new ParseException(e.getMessage());
        }
    }

}
