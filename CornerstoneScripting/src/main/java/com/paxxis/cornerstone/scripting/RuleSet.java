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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.paxxis.cornerstone.scripting.parser.CSLRuntime;

/**
 * A related collection of rules.  It provides services
 * such as rule lookup and variable change
 * event dispatching.
 *
 * @author Robert Englander
 */
public class RuleSet implements Serializable {
    private static final long serialVersionUID = 1L;

    // the rules
    private HashMap<String, Rule> rules = new HashMap<String, Rule>();

    // a list of locally referenced rules (i.e. invoked from other rules defined in
    // the same file.
    private HashMap<ObjectMethodExpression, String> ruleReferences = new HashMap<ObjectMethodExpression, String>();
    
    // the mapping of rules to named events
    private HashMap<String, String> eventMap = new HashMap<String, String>();

    // the most recent exception
    private Exception lastProcessException = null;

    // the rule currently executing
    private Rule currentRule = null;

    // the output handler
    private IOutputHandler outputHandler = new DefaultOutputHandler();
    
    // indicates that any error
    // which may have occurred is still pending display
    // to the user.
    private boolean errorDisplayPending = true;

    // the instruction currently executing
    private Instruction currentInstruction = null;

    // the name of the rule set
    private String name = null;

    // the monitor agent
    private transient CSLRuntime runtime = null;

    // the source code used to generate this rule set
    private String sourceCode = null;

    // the globally available reference variables
    private List<ReferenceVariable> references = new ArrayList<ReferenceVariable>();
    
    public RuleSet() {

    }
    
    public RuleSet(String name, String sourceCode, CSLRuntime monitor, List<ReferenceVariable> globalRefs) {
    	this(name, sourceCode, monitor);
    	if (globalRefs != null) {
    	    references.addAll(globalRefs);
    	}
    }
    
    public RuleSet(String name, String sourceCode, CSLRuntime monitor) {
        this.name = name;
        this.sourceCode = sourceCode;

        if (monitor == null)
        {
            monitor = new CSLRuntime();
        }
        
        this.runtime = monitor;
        this.runtime.setRuleSet(this);
    }

    public List<ReferenceVariable> getReferenceVariables() {
    	List<ReferenceVariable> list = new ArrayList<ReferenceVariable>();
    	list.addAll(references);
    	return list;
    }
    
    public void setOutputHandler(IOutputHandler handler) {
        outputHandler = handler;
    }
    
    public IOutputHandler getOutputHandler() {
        return outputHandler;
    }
    
    public String getSourceCode() {
        return sourceCode;
    }

    public CSLRuntime getMonitor() {
        return runtime;
    }

    public String getName() {
        return name;
    }

    public void reset() {
        setCurrentRule(null);
        setCurrentInstruction(null);
        setErrorDisplayPending(true);
        lastProcessException = null;
    }

    public void setCurrentRule(Rule current) {
        currentRule = current;
    }

    public Rule getCurrentRule() {
        return currentRule;
    }

    public void setErrorDisplayPending(boolean pending) {
        errorDisplayPending = pending;
    }

    public boolean isErrorDisplayPending() {
        return errorDisplayPending;
    }

    public void setCurrentInstruction(Instruction inst) {
        currentInstruction = inst;
    }

    public Instruction getCurrentInstruction() {
        return currentInstruction;
    }

    public void mapRule(String event, String rule) {
        eventMap.put(event, rule);
    }

    public String getMappedRuleName(String event) {
        return (String)eventMap.get(event);
    }

    public Rule getMappedRule(String event) {
        Rule rule = null;
        String name = getMappedRuleName(event);
        if (name != null) {
            rule = getRule(name);
        }

        return rule;
    }

    public boolean runRule(String name) {
        boolean result = false;
        Rule rule = getRule(name);
        if (rule != null) {
            result = rule.process(null);
        }

        return result;
    }

    public boolean runMappedRule(String eventName) {
        // reset before we start.  just a note that
        // if a rule is run directly, the caller should
        // call reset on the rule set before processing
        // the rule.
        reset();

        boolean result = true;
        Rule rule = getMappedRule(eventName);
        if (rule != null) {
            try {
                result = rule.process(null);
                lastProcessException = null;

                // if the mapped rule has a return
                // value , then that's what we want
                // to return as a boolean.
                if (rule.hasReturnValue()) {
                    IValue rv = rule.getReturnValue();
                    result = rv.valueAsBoolean();
                }
            } catch (Exception e) {
                // well, something went wrong
                result = false;
                lastProcessException = new Exception(e.getMessage());
            }
        }

        return result;
    }

    public Exception getLastProcessException() {
        return lastProcessException;
    }

    public Rule getRule(String name) {
        return (Rule)rules.get(name);
    }

    public void addRule(Rule rule)
                throws RuleCreationException {
        if (rules.containsKey(rule.getName())) {
            throw new RuleCreationException("Duplicate Rule Name: " + rule.getName());
        }

        rules.put(rule.getName(), rule);
    }
    
    public void addReferencedRuleName(ObjectMethodExpression exp, String name) {
	ruleReferences.put(exp, name);
    }
    
    public void resoveRuleReferences() {
	for (ObjectMethodExpression exp : ruleReferences.keySet()) {
	    exp.resolveReferencedRule(ruleReferences.get(exp));
	}
    }

}
