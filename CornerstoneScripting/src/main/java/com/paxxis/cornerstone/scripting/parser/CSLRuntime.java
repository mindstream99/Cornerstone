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

package com.paxxis.cornerstone.scripting.parser;

import java.io.Serializable;
import java.util.Map;

import com.paxxis.cornerstone.scripting.InstructionQueue;
import com.paxxis.cornerstone.scripting.Rule;
import com.paxxis.cornerstone.scripting.RuleSet;
import com.paxxis.cornerstone.scripting.RuleVariable;
import com.paxxis.cornerstone.scripting.ServiceContext;


/**
 * The runtime is used to provide debug and context to the framework
 *
 * @author Robert Englander
 */
public class CSLRuntime implements Serializable {
    private static final long serialVersionUID = 1L;

    // the associated rule set
    private RuleSet ruleSet;

    // a context provider
    transient private ServiceContext serviceContext = null;

    public CSLRuntime() {
    }

    public void setServiceContext(ServiceContext provider) {
        serviceContext = provider;
    }

    public ServiceContext getServiceContext() {
        return serviceContext;
    }
    
    public void setRuleSet(RuleSet ruleSet) {
        this.ruleSet = ruleSet;
    }
    
    /**
     * Tell the monitor that an instruction at a specific line
     * number is about to execute.
     */
    public void setPoised() {
        @SuppressWarnings("unused")
	int line = ruleSet.getCurrentInstruction().getLineNumber();
    }

    public void ruleBoundary(Rule rule, boolean starting) {
    }

    public void variableChange(RuleVariable var) {
        if (var.getName() != null && !var.getName().startsWith("#")) {
            //System.out.println(var.getName() + " = " + var.valueAsObject());
        }
    }

    public void push(InstructionQueue context) {
        @SuppressWarnings("unused")
	String[] names = context.getVariableNames();
    }

    public void pop(InstructionQueue context) {
    }

    public String performMacroExpansion(String value) {
	// FIXME
	return serviceContext.performMacroExpansion(value);
    }

    public boolean supportsMacroExpansion() {
	return serviceContext.supportsMacroExpansion();
    }

    public Map<String, String[]> getSessionParameters() {
    	if (this.ruleSet == null) {
    		return null;
    	}
    	
    	return ruleSet.getSessionParameters();
	}

}
