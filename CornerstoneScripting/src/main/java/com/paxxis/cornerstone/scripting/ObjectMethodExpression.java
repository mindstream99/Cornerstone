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

/**
 *
 * @author Robert Englander
 */
public class ObjectMethodExpression extends IValue {

    private static final long serialVersionUID = 1L;
    
    private RuleVariable object = null;
    private String methodName;
    private List<IValue> parameters = new ArrayList<IValue>();
    private boolean validateValueOf = false;
    private InstructionQueue queue = null;
    private boolean pendingResolution = false;
    
    public ObjectMethodExpression() {
    }

    public void findVariables(List<RuleVariable> deps) {
        for (IValue param : parameters) {
            param.findVariables(deps);
        }
    }

    public boolean isArray() {
        return false;
    }

    public boolean isTable() {
        return false;
    }

    public void setObjectName(String name, InstructionQueue q) {
        queue = q;
        RuleVariable rv = q.getVariable(name);

        if (rv == null) {
            RuleSet ruleSet = q.getRuleSet();
            Rule r = ruleSet.getRule(name);
            if (r == null)
            {
        	ruleSet.addReferencedRuleName(this, name);
        	pendingResolution = true;
            } else {
                RuleAccessor ra = new RuleAccessor(null, q);
                ra.setMonitor(q.getRuleSet().getMonitor());
                ra.setValue(new StringVariable(null, name), true);
                rv = ra;
            }
            
        } else if (!rv.isObject()) {
            throw new RuntimeException("Variable '" + name + "' is not an object.");
        }

        object = rv;

    }

    public void resolveReferencedRule(String name) {
        RuleSet ruleSet = queue.getRuleSet();
        Rule r = ruleSet.getRule(name);
        if (r == null)
        {
            throw new RuntimeException("Attempt to invoke unknown rule '" + name + "'.");
        } else {
            RuleAccessor ra = new RuleAccessor(null, queue);
            ra.setMonitor(queue.getRuleSet().getMonitor());
            ra.setValue(new StringVariable(null, name), true);
            object = ra;
        }
    }
    
    public void setMethodName(String name, boolean validate) {
        setMethodName(name);
        validateValueOf = validate;
    }
    
    public void setMethodName(String name) {
        if (object == null && !pendingResolution) {
            throw new RuntimeException("Method '" + name + "' called on unknown object.");
        }
        
        methodName = name;
    }

    public void addParameter(IValue val) {
        parameters.add(val);
    }

    public void validateParameters() {
        int pcount = 0;
        try {
            if (!pendingResolution) {
                pcount = object.getMethodParameterCount(methodName);
            }
        } catch (Exception e) {
            if (object instanceof RuleAccessor) {
                throw new RuntimeException("Invalid method name '" + methodName + "' on Rule " + ((RuleAccessor)object).getRuleName().valueAsString());
            }
            
            throw new RuntimeException("Unknown method name '" + methodName + "' on Object " + ((RuleVariable)object).getName());
        }
        
        if (!pendingResolution && (parameters.size() != pcount && pcount != -1)) {
            throw new RuntimeException("Wrong number of parameters (" + parameters.size() + ") for calling " + methodName + " on Object ");
        }

        if (object instanceof RuleAccessor) {
            RuleAccessor ra = (RuleAccessor)object;
            if (methodName.equals("valueOf") && validateValueOf) {
                if (parameters.get(0) instanceof RuleVariable) {
                    RuleVariable rv = (RuleVariable)parameters.get(0);
                    if (rv.getName() == null) {
                        String dataName = rv.valueAsString();
                        Rule rule = queue.getRuleSet().getRule(ra.valueAsString());
                        RuleVariable variable = rule.getQueue().getVariable(dataName);
                        if (variable == null) {
                            throw new RuntimeException("Unknown variable name '" + dataName + "' on Rule " + rule.getName());
                        }
                    }
                }
            }
        }
    }
    
    private void checkReturn() {
        if (!object.methodHasReturn(methodName)) {
            // you shouldn't have called this!
            throw new RuntimeException("Can't get value of method or rule without a return value");
        }
    }
    
    public Object valueAsObject() {
	checkReturn();
	
        IValue result = execute();
        return result.valueAsObject();
    }

    public String valueAsString() {
	checkReturn();
	
        IValue result = execute();
        return result.valueAsString();
    }

    public Integer valueAsInteger() {
	checkReturn();
	
        IValue result = execute();
        return result.valueAsInteger();
    }

    public Boolean valueAsBoolean() {
	checkReturn();
	
        IValue result = execute();
        return result.valueAsBoolean();
    }

    public Double valueAsDouble() {
	checkReturn();
	
        IValue result = execute();
        return result.valueAsDouble();
    }

    public IValue evaluate() {
        return execute();
    }


    public IValue execute() {
        return object.executeMethod(methodName, parameters);
    }
}
