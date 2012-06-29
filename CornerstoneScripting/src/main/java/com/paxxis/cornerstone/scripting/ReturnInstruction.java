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

/**
 * @author Robert Englander
 */
public class ReturnInstruction extends Instruction {
    private static final long serialVersionUID = 1L;

    // the value to return
    private IValue returnValue;

    // the rule itself
    private Rule rule;
    
    public ReturnInstruction() {
    }

    public boolean hasReturnStatement() {
        return true;
    }

    public void setValue(Rule rule, IValue val)
                    throws RuleCreationException {
        this.rule = rule;
        this.returnValue = val;
    }

    public boolean process(InstructionQueue queue) {
        rule.setReturnValue(returnValue);
        
        // return false because processing should not
        // continue beyond this instruction.
        return false;
    }
}
