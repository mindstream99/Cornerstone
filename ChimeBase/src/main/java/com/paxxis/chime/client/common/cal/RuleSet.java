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

import java.io.Serializable;
import java.util.HashMap;

/**
 * A related collection of rules.  It provides services
 * such as rule lookup and variable change
 * event dispatching.
 *
 * @author Robert Englander
 */
public class RuleSet implements Serializable
{
    // the rules
    HashMap<String, Rule> _rules = new HashMap<String, Rule>();

    // the mapping of rules to named events
    HashMap<String, String> _eventMap = new HashMap<String, String>();

    // the most recent exception
    Exception _lastProcessException = null;

    // the rule currently executing
    Rule _currentRule = null;

    // the output handler
    IOutputHandler _outputHandler = new DefaultOutputHandler();
    
    // indicates that any error
    // which may have occurred is still pending display
    // to the user.
    boolean _errorDisplayPending = true;

    // the instruction currently executing
    Instruction _currentInstruction = null;

    // the name of the rule set
    String _name = null;

    // the monitor agent
    transient Runtime _monitor = null;

    // the source code used to generate this rule set
    String _sourceCode = null;

    public RuleSet() {

    }
    
    public RuleSet(String name, String sourceCode, Runtime monitor)
    {
        _name = name;
        _sourceCode = sourceCode;

        if (monitor == null)
        {
            monitor = new Runtime();
        }
        
        _monitor = monitor;
        _monitor.setRuleSet(this);
    }

    private void exitMonitor()
    {
        //Monitor.unregisterRuleSet(this);
    }

    public void setOutputHandler(IOutputHandler handler)
    {
        _outputHandler = handler;
    }
    
    public IOutputHandler getOutputHandler()
    {
        return _outputHandler;
    }
    
    public String getSourceCode()
    {
        return _sourceCode;
    }

    public Runtime getMonitor()
    {
        return _monitor;
    }

    public String getName()
    {
        return _name;
    }

    public void reset()
    {
        setCurrentRule(null);
        setCurrentInstruction(null);
        setErrorDisplayPending(true);
        _lastProcessException = null;
    }

    public void setCurrentRule(Rule current)
    {
        _currentRule = current;
    }

    public Rule getCurrentRule()
    {
        return _currentRule;
    }

    public void setErrorDisplayPending(boolean pending)
    {
        _errorDisplayPending = pending;
    }

    public boolean isErrorDisplayPending()
    {
        return _errorDisplayPending;
    }

    public void setCurrentInstruction(Instruction inst)
    {
        _currentInstruction = inst;
    }

    public Instruction getCurrentInstruction()
    {
        return _currentInstruction;
    }

    public void mapRule(String event, String rule)
    {
        _eventMap.put(event, rule);
    }

    public String getMappedRuleName(String event)
    {
        return (String)_eventMap.get(event);
    }

    public Rule getMappedRule(String event)
    {
        Rule rule = null;
        String name = getMappedRuleName(event);
        if (name != null)
        {
            rule = getRule(name);
        }

        return rule;
    }

    public boolean runRule(String name)
    {
        boolean result = false;
        Rule rule = getRule(name);
        if (rule != null)
        {
            result = rule.process(null);
        }

        return result;
    }

    public boolean runMappedRule(String eventName)
    {
        // reset before we start.  just a note that
        // if a rule is run directly, the caller should
        // call reset on the rule set before processing
        // the rule.
        reset();

        boolean result = true;
        Rule rule = getMappedRule(eventName);
        if (rule != null)
        {
            try
            {
                result = rule.process(null);
                _lastProcessException = null;

                // if the mapped rule has a return
                // value , then that's what we want
                // to return as a boolean.
                if (rule.hasReturnValue())
                {
                    IValue rv = rule.getReturnValue();
                    result = rv.valueAsBoolean();
                }
            }
            catch (Exception e)
            {
                // well, something went wrong
                result = false;
                _lastProcessException = new Exception(e.getMessage());
            }
        }

        return result;
    }

    public Exception getLastProcessException()
    {
        return _lastProcessException;
    }

    public Rule getRule(String name)
    {
        return (Rule)_rules.get(name);
    }

    public void addRule(Rule rule)
                throws RuleCreationException
    {
        if (_rules.containsKey(rule.getName()))
        {
            throw new RuleCreationException("Duplicate Rule Name: " + rule.getName());
        }

        _rules.put(rule.getName(), rule);
    }

}
