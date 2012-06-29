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

import java.util.HashMap;

/**
 * @author Robert Englander
 */
public class SwitchInstruction extends Instruction {
    private static final long serialVersionUID = 1L;

    // a map of values to instruction queues
    private HashMap<Object, InstructionQueue> caseMap = new HashMap<Object, InstructionQueue>();

    // the default case queue
    private InstructionQueue defaultCase = null;

    // the value to switch on
    private IValue value = null;

    // true if using string matching to find the
    // case to run, false otherwise (number matching)
    private boolean stringMatching = false;

    public SwitchInstruction() {

    }
    
    public SwitchInstruction(InstructionQueue queue) {
    }

    public boolean hasReturnStatement() {
        if (defaultCase != null) {
            if (defaultCase.hasReturnInstruction()) {
                return true;
            }
        }

        Object[] queues = caseMap.values().toArray();
        int count = queues.length;
        for (int i = 0; i < count; i++) {
            InstructionQueue queue = (InstructionQueue)queues[i];
            if (queue.hasReturnInstruction()) {
                return true;
            }
        }

        return false;
    }

    public void setStringMatching(boolean stringMatching) {
        this.stringMatching = stringMatching;
    }

    public void setValue(IValue value) {
        this.value = value;
    }

    public void setDefaultCase(InstructionQueue defCase) {
        this.defaultCase = defCase;
    }

    public void addCase(String value, InstructionQueue block) {
        if (stringMatching) {
            caseMap.put(value, block);
        } else {
            caseMap.put(new Double(value), block);
        }
    }

    public boolean process(InstructionQueue queue) {
        Object val;
        if (stringMatching) {
            val = value.valueAsString();
        } else {
            val = new Double(value.valueAsDouble());
        }

        // get the queue for the switch value
        InstructionQueue q = (InstructionQueue)caseMap.get(val);

        boolean result = true;

        if (q != null) {
            // run that block
            q.setRuleSet(queue.getRuleSet());
            result = q.process(q);
        } else if (defaultCase != null) {
            // run the default case if there is one
            defaultCase.setRuleSet(queue.getRuleSet());
            result = defaultCase.process(defaultCase);
        }

        return result;
    }
}
