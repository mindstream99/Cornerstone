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
public class InstructionQueue extends Instruction {
    private static final long serialVersionUID = 1L;

    // the list of instructions
    private List<Instruction> instructions = new ArrayList<Instruction>();

    // the variables
    private HashMap<String, RuleVariable> localVariables = new HashMap<String, RuleVariable>();

    // the parameters
    private HashMap<Integer, RuleVariable> paramNames = new HashMap<Integer, RuleVariable>();

    // the associated rule set
    private RuleSet ruleSet = null;

    // the parent queue.
    private InstructionQueue parentQueue = null;

    // indicates if this queue hit a break instruction
    private boolean _hitBreak = false;

    // indicates if this queue is interested in a break instruction
    private boolean _wantsBreak = false;

    public InstructionQueue() {

    }
    
    /**
     * Sets the value of the wantsBreak property, which indicates
     * whether or not this queue is interested in handling
     * a break instruction.
     * @param wantsBreak
     */
    public void setWantsBreak(boolean wantsBreak) {
        this._wantsBreak = wantsBreak;
    }

    /**
     * Sets the value of the break property.
     * @param hitBreak
     */
    public void setBreak(boolean hitBreak) {
        if (_wantsBreak) {
            // this queue wants the break
            this._hitBreak = hitBreak;
        } else if (parentQueue != null) {
            // we don't care, so pass it up the line
            parentQueue.setBreak(hitBreak);
        }
    }

    public boolean getBreak() {
        return _hitBreak;
    }

    /**
     * Sets the values of the parameter variables.  The order
     * of the values must be the same as the order of the
     * parameters.
     * @param vals
     */
    public void setVariableValues(List<IValue> vals) {
        int cnt = vals.size();
        for (int i = 0; i < cnt; i++) {
            Integer idx = new Integer(i);
            RuleVariable var = (RuleVariable)paramNames.get(idx);
            if (var != null) {
                var.setValue(vals.get(i), true);
            }
        }
    }

    public void setParent(InstructionQueue parent) {
        parentQueue = parent;
    }

    public void setRuleSet(RuleSet set) {
        ruleSet = set;
    }

    public RuleSet getRuleSet() {
        // we need to crawl up the chain of instruction
        // queues if we don't yet know who our ruleset is
        if (ruleSet == null && parentQueue != null) {
            return parentQueue.getRuleSet();
        }

        return ruleSet;
    }

    public synchronized void addInstruction(Instruction i) {
        instructions.add(i);
    }

    public String[] getVariableNames() {
        Set<String> keys = localVariables.keySet();
        int cnt = localVariables.size();
        String[] vars = new String[cnt];

        int i = 0;
        int internalCount = 0;
        for (String name : keys) {
            if (name.startsWith("#")) {
                internalCount++;
            } else {
                vars[i] = name;
                i++;
            }
        }

        if (internalCount > 0) {
            int newCount = cnt - internalCount;
            String[] temp = new String[newCount];
            for (int j = 0; j < newCount; j++) {
                temp[j] = vars[j];
            }

            vars = temp;
        }

        return vars;
    }

    public RuleVariable getVariable(String name) {
        RuleVariable var = (RuleVariable)localVariables.get(name);
        if (var == null && parentQueue != null) {
            var = parentQueue.getVariable(name);
        }

        return var;
    }

    public List<RuleVariable> getVisibleVariables(boolean includeParent) {
        List<RuleVariable> variables = new ArrayList<RuleVariable>();
        Collection<RuleVariable> vars = localVariables.values();
        for (RuleVariable variable : vars) {
            if (!variable.getName().startsWith("#")) {
                variables.add(variable);
            }
        }
        
        if (includeParent && parentQueue != null) {
            List<RuleVariable> parentVariables = parentQueue.getVisibleVariables(includeParent);
            for (RuleVariable variable : parentVariables) {
                if (!localVariables.containsKey(variable.getName())) {
                    variables.add(variable);
                }
            }
        }
        
        // sort the list by variable name
        // FIXME use a comparator and collection sorter
        int cnt = variables.size();
        for (int i = 0; i < (cnt - 2); i++) {
            for (int j = 0; j < (cnt - i - 1); j++) {
                RuleVariable v1 = variables.get(j);
                RuleVariable v2 = variables.get(j + 1);
                String name1 = v1.getName();
                String name2 = v2.getName();
                int cmp = name1.compareTo(name2);
                if (cmp > 0) {
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
        Integer num = new Integer(paramNames.size());
        paramNames.put(num, param);
        addVariable(param);
    }

    public void addVariable(RuleVariable var)
                    throws RuleCreationException {
        // make sure the variable name is not already used
        if (localVariables.containsKey(var.getName())) {
            throw new RuleCreationException("Duplicate Variable Name '"
                                                + var.getName() + "'");
        }

        // add the variable
        localVariables.put(var.getName(), var);

        // pass the monitor to the variable
        var.setRuntime(getRuleSet().getRuntime());
        
        // check macro modifier
        if (var.isMacro()) {
            if (!getRuleSet().getRuntime().supportsMacroExpansion()) {
        	throw new RuleCreationException("The language context provider does not support macro expansion.");
            }
        }
    }

    /**
     * Process this queue as if it was an instruction.
     * @param queue the parent queue
     * @return
     */
    public synchronized boolean process(InstructionQueue queue) {
        resetVariables();
        
        // push this context onto the monitor stack
        getRuleSet().getRuntime().push(this);

        boolean result = true;

        // iterate over the instructions
        int cnt = instructions.size();
        int position = 0;
        while (position < cnt && result == true) {
            // get the instruction
            Instruction inst = (Instruction)instructions.get(position);

            // tell the rule set that this is the currently
            // executing instruction
            getRuleSet().setCurrentInstruction(inst);

            // tell the monitor that we're about to run the
            // current instruction
            if (inst.canPoise()) {
                getRuleSet().getRuntime().setPoised();
            }
            
            // process the instruction
            result = inst.process(queue);

            // next instruction
            position++;
        }

        // pop this context off the monitor stack
        getRuleSet().getRuntime().pop(this);
        //resetVariables();

        return result;
    }

    private void resetVariables() {
        Collection<RuleVariable> variables = localVariables.values();
        for (RuleVariable variable : variables) {
            if (!paramNames.containsValue(variable)) {
                if (!variable.isDurable()) {
                    variable.reset();
                }
            }
        }
    }
    
    public boolean hasReturnInstruction() {
        // iterate over the instructions
        for (Instruction inst : instructions) {
            if (inst.hasReturnStatement()) {
                return true;
            }
        }

        return false;
    }

    public boolean isEmpty() {
	return instructions.isEmpty();
    }
}
