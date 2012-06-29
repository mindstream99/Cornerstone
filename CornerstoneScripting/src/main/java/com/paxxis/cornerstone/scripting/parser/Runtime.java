package com.paxxis.cornerstone.scripting.parser;

import java.io.Serializable;

import com.paxxis.cornerstone.scripting.InstructionQueue;
import com.paxxis.cornerstone.scripting.Rule;
import com.paxxis.cornerstone.scripting.RuleSet;
import com.paxxis.cornerstone.scripting.RuleVariable;
import com.paxxis.cornerstone.scripting.ServiceContextProvider;

public class Runtime implements Serializable {
    private static final long serialVersionUID = 1L;

    // the associated rule set
    private RuleSet _ruleSet;

    // the context provider
    transient private ServiceContextProvider contextProvider = null;

    public Runtime() {
    }

    public void setServiceContextProvider(ServiceContextProvider provider) {
        contextProvider = provider;
    }

    public ServiceContextProvider getServiceContextProvider() {
        return contextProvider;
    }
    
    public void setRuleSet(RuleSet ruleSet) {
        _ruleSet = ruleSet;
    }
    
    /**
     * Tell the monitor that an instruction at a specific line
     * number is about to execute.
     */
    public void setPoised() {
        @SuppressWarnings("unused")
	int line = _ruleSet.getCurrentInstruction().getLineNumber();
    }

    public void ruleBoundary(Rule rule, boolean starting) {
	// TODO what is this for?
    }

    public void variableChange(RuleVariable var) {
        if (var.getName() != null && !var.getName().startsWith("#")) {
            // TODO does this do anything?
        }
    }

    public void push(InstructionQueue context) {
        @SuppressWarnings("unused")
	String[] names = context.getVariableNames();
    }

    public void pop(InstructionQueue context) {
    }
}
