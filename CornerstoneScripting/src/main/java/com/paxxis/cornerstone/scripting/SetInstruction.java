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
 * @author Robert Englander
 */
public class SetInstruction extends Instruction {
    private static final long serialVersionUID = 1L;

    private IValue value = null;

    // this keeps the list of variables that
    // we're going to set
    private List<RuleVariable> variables = new ArrayList<RuleVariable>();

    public SetInstruction() {
    }

    public void addVariable(RuleVariable var) {
        variables.add(var);
    }

    public void setValue(IValue value) {
        this.value = value;
    }

    public boolean process(InstructionQueue queue) {
        for (RuleVariable variable : variables) {
            variable.setValue(value, true);
        }

        return true;
    }
}
