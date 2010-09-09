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
import java.util.ArrayList;
import java.util.List;


/**
 * This class represents a rule.  It is normally
 * populated by calling the rule parser, although it
 * can be populated by calling the methods directly.
 *
 * @author Robert Englander
 */
public class Rule implements Serializable
{
    // the name of the rule
    String _name = null;

    // the conditions for firing can be boolean
    // conditions.
    List<IValue> _booleanConditions = new ArrayList<IValue>();

    // the instruction queue to run when this rule fires
    InstructionQueue _queue = new InstructionQueue();

    // the instruction queue to run when this rule fails
    InstructionQueue _errorQueue = new InstructionQueue();

    // the variable that holds the value that
    // is returned from invoke, if the rule is
    // defined to have a return value.
    IValue _returnValue = null;

    // the rule set that this rule belongs to
    RuleSet _ruleSet = null;

    public Rule() {

    }
    
    public Rule(RuleSet set)
    {

        _ruleSet = set;
        _queue.setRuleSet(_ruleSet);
        _errorQueue.setRuleSet(_ruleSet);
    }

    /**
     * Determines if there is at least 1 return statement
     * within the entire rule.  This is provided as support
     * for tools that help the user to write good rules,
     * but does not ensure that the return statements
     * can ever be reached.  The existence of a return
     * statement is not a requirement when a return option
     * is declared.  If the rule execution ends without
     * reaching a return statement, the default value
     * of the declared return data type will be returned.
     * @return true if a return statement exists. false otherwise
     */
    public boolean hasReturnStatement()
    {
        return _queue.hasReturnInstruction();
    }

    public boolean hasReturnValue()
    {
        return (_returnValue != null);
    }

    public void setReturnValue(IValue rv)
    {
        _returnValue = rv.evaluate();

        /*
        try
        {
            _queue.addVariable(rv);
            _errorQueue.addVariable(rv);
        }
        catch (RuleCreationException rce)
        {
            // this can't happen because the variable
            // name "return" is a reserved word and so
            // would be caught by the parser if it was used.
            // AND, the actual name of the variable is
            // #return, and that's illegal in the parser.
        }
        */
    }

    public IValue getReturnValue()
    {
        return _returnValue;
    }

    public boolean hasConditionals()
    {
        return (_booleanConditions.size() > 0);
    }

    public void addCondition(IValue cnd)
    {
        _booleanConditions.add(cnd);
    }

    public boolean evaluateConditionals()
    {
        for (IValue cond : _booleanConditions)
        {
            if (cond.valueAsBoolean() == false)
            {
                return false;
            }
        }

        return true;
    }

    public void setName(String name)
    {
        _name = name;
    }

    public String getName()
    {
        return _name;
    }

    public InstructionQueue getQueue()
    {
        return _queue;
    }

    public InstructionQueue getErrorQueue()
    {
        return _errorQueue;
    }

    /**
     * Process the rule using the parameter values supplied
     * in params.  params is an array of RuleVariables
     * whose names must match the parameters in the rule definition.
     * @param params
     * @return
     */
    public IValue invoke(List<IValue> params)
    {
        process(params);

        IValue result = null;
        if (hasReturnValue())
        {
            result = getReturnValue();
        }

        return result;
    }

    public boolean process(List<IValue> params)
    {
        // tell the rule set that this is the current
        // rule. remember the previous rule so we can
        // set it back as the current when we're done
        Rule prevRule = _ruleSet.getCurrentRule();
        _ruleSet.setCurrentRule(this);

        try
        {
            // tell the monitor that we're about to
            // start running a rule
            _ruleSet.getMonitor().ruleBoundary(this, true);

            // setup the parameters
            if (params != null)
            {
                _queue.setVariableValues(params);
            }

            // run it
            boolean result = _queue.process(_queue);

            // tell the monitor that we've finished running the rule
            _ruleSet.getMonitor().ruleBoundary(this, false);
            _ruleSet.setCurrentRule(prevRule);
            return result;
        }
        catch (Exception rte)
        {
            StringBuffer buffer = new StringBuffer();
            //if (_ruleSet.isErrorDisplayPending())
            {
                //_ruleSet.setErrorDisplayPending(false);

                // let's give the user a message he/she can understand
                buffer.append("Error while processing rule set '");
                buffer.append(_ruleSet.getName() + "'.");
                buffer.append("\nRule Name: " + _ruleSet.getCurrentRule().getName());
                buffer.append("\nError occurred on line number: " + _ruleSet.getCurrentInstruction().getLineNumber());
                buffer.append("\n" + rte.getMessage());
                //System.out.println(buffer);
            }

            // process the error block instructions
            _errorQueue.setRuleSet(_ruleSet);
            boolean result = _errorQueue.process(_queue);

            // does anything use this?
            _ruleSet._lastProcessException = new RuntimeException(buffer.toString());

            // tell the monitor that we've finished running the rule
            _ruleSet.getMonitor().ruleBoundary(this, false);

            _ruleSet.setCurrentRule(prevRule);
            return false;
            //throw new RuntimeException(rte);
        }
    }
}
