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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


/**
 * The instruction queue contains an ordered collection
 * of instructions.  The instructions are executed by
 * calling the process method.
 *
 * @author Robert Englander
 */
public class InstructionQueue extends Instruction
{
    // the list of instructions
    List<Instruction> _instructions = new ArrayList<Instruction>();

    // the variables
    HashMap<String, RuleVariable> _variables = new HashMap<String, RuleVariable>();

    // the parameters
    HashMap<Integer, RuleVariable> _paramNames = new HashMap<Integer, RuleVariable>();

    // the associated rule set
    RuleSet _ruleSet = null;

    // the parent queue.
    InstructionQueue _parentQueue = null;

    // indicates if this queue hit a break instruction
    boolean _hitBreak = false;

    // indicates if this queue is interested in a break instruction
    boolean _wantsBreak = false;

    public InstructionQueue() {

    }
    
    /**
     * Sets the value of the wantsBreak property, which indicates
     * whether or not this queue is interested in handling
     * a break instruction.
     * @param wantsBreak
     */
    public void setWantsBreak(boolean wantsBreak)
    {
        _wantsBreak = wantsBreak;
    }

    /**
     * Sets the value of the break property.
     * @param hitBreak
     */
    public void setBreak(boolean hitBreak)
    {
        if (_wantsBreak)
        {
            // this queue wants the break
            _hitBreak = hitBreak;
        }
        else if (_parentQueue != null)
        {
            // we don't care, so pass it up the line
            _parentQueue.setBreak(hitBreak);
        }
    }

    public boolean getBreak()
    {
        return _hitBreak;
    }

    /**
     * Sets the values of the parameter variables.  The order
     * of the values must be the same as the order of the
     * parameters.
     * @param vals
     */
    public void setVariableValues(List<IValue> vals)
    {
        int cnt = vals.size();
        for (int i = 0; i < cnt; i++)
        {
            Integer idx = new Integer(i);
            RuleVariable var = (RuleVariable)_paramNames.get(idx);
            if (var != null)
            {
                var.setValue(vals.get(i), true);
            }
        }
    }

    public void setParent(InstructionQueue parent)
    {
        _parentQueue = parent;
    }

    public void setRuleSet(RuleSet set)
    {
        _ruleSet = set;
    }

    public RuleSet getRuleSet()
    {
        // we need to crawl up the chain of instruction
        // queues if we don't yet know who our ruleset is
        if (_ruleSet == null && _parentQueue != null)
        {
            return _parentQueue.getRuleSet();
        }

        return _ruleSet;
    }

    public synchronized void addInstruction(Instruction i)
    {
        _instructions.add(i);
    }

    public String[] getVariableNames()
    {
        Set<String> keys = _variables.keySet();
        int cnt = _variables.size();
        String[] vars = new String[cnt];

        int i = 0;
        int internalCount = 0;
        for (String name : keys)
        {
            if (name.startsWith("#"))
            {
                internalCount++;
            }
            else
            {
                vars[i] = name;
                i++;
            }
        }

        if (internalCount > 0)
        {
            int newCount = cnt - internalCount;
            String[] temp = new String[newCount];
            for (int j = 0; j < newCount; j++)
            {
                temp[j] = vars[j];
            }

            vars = temp;
        }

        return vars;
    }

    public RuleVariable getVariable(String name)
    {
        RuleVariable var = (RuleVariable)_variables.get(name);
        if (var == null && _parentQueue != null)
        {
            var = _parentQueue.getVariable(name);
        }

        return var;
    }

    public List<RuleVariable> getVisibleVariables(boolean includeParent)
    {
        List<RuleVariable> variables = new ArrayList<RuleVariable>();
        Collection<RuleVariable> vars = _variables.values();
        for (RuleVariable variable : vars)
        {
            if (!variable.getName().startsWith("#"))
            {
                variables.add(variable);
            }
        }
        
        if (includeParent && _parentQueue != null)
        {
            List<RuleVariable> parentVariables = _parentQueue.getVisibleVariables(includeParent);
            for (RuleVariable variable : parentVariables)
            {
                if (!_variables.containsKey(variable.getName()))
                {
                    variables.add(variable);
                }
            }
        }
        
        // sort the list by variable name
        int cnt = variables.size();
        for (int i = 0; i < (cnt - 2); i++)
        {
            for (int j = 0; j < (cnt - i - 1); j++)
            {
                RuleVariable v1 = variables.get(j);
                RuleVariable v2 = variables.get(j + 1);
                String name1 = v1.getName();
                String name2 = v2.getName();
                int cmp = name1.compareTo(name2);
                if (cmp > 0)
                {
                    // switch positions
                    variables.set(j, v2);
                    variables.set(j + 1, v1);
                }
            }
        }
        
        return variables;
    }
    
    public void addParameter(RuleVariable param)
                    throws RuleCreationException
    {
        // keep track of the names of the parameters in
        // the order of appearance.  we'll use this
        // later to resolve parameter values when
        // the rule that uses this queue is invoked
        Integer num = new Integer(_paramNames.size());
        _paramNames.put(num, param);
        addVariable(param);
    }

    public void addVariable(RuleVariable var)
                    throws RuleCreationException
    {
        // make sure the variable name is not already used
        if (_variables.containsKey(var.getName()))
        {
            throw new RuleCreationException("Duplicate Variable Name '"
                                                + var.getName() + "'");
        }

        // add the variable
        _variables.put(var.getName(), var);

        // pass the monitor to the variable
        var.setMonitor(getRuleSet().getMonitor());
    }

    /**
     * Process this queue as if it was an instruction.
     * @param queue the parent queue
     * @return
     */
    public synchronized boolean process(InstructionQueue queue)
    {
        resetVariables();
        
        // push this context onto the monitor stack
        getRuleSet().getMonitor().push(this);

        boolean result = true;

        // iterate over the instructions
        int cnt = _instructions.size();
        int position = 0;
        while (position < cnt && result == true)
        {
            // get the instruction
            Instruction inst = (Instruction)_instructions.get(position);

            // tell the rule set that this is the currently
            // executing instruction
            getRuleSet().setCurrentInstruction(inst);

            // tell the monitor that we're about to run the
            // current instruction
            if (inst.canPoise())
            {
                getRuleSet().getMonitor().setPoised();
            }
            
            // process the instruction
            result = inst.process(queue);

            // next instruction
            position++;
        }

        // pop this context off the monitor stack
        getRuleSet().getMonitor().pop(this);
        //resetVariables();

        return result;
    }

    private void resetVariables()
    {
        Collection<RuleVariable> variables = _variables.values();
        for (RuleVariable variable : variables)
        {
            if (!_paramNames.containsValue(variable))
            {
                if (!variable.isDurable())
                {
                    variable.reset();
                }
            }
        }
    }
    
    public boolean hasReturnInstruction()
    {
        // iterate over the instructions
        for (Instruction inst : _instructions)
        {
            if (inst.hasReturnStatement())
            {
                return true;
            }
        }

        return false;
    }
}
