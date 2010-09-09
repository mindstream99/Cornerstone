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
import java.util.List;

/**
 * @author Robert Englander
 */
public class SetInstruction extends Instruction
{
    IValue _value = null;

    // this keeps the list of variables that
    // we're going to set
    List<RuleVariable> _variables = new ArrayList<RuleVariable>();

    public SetInstruction()
    {
    }

    /**
     * 
     * @param var 
     */
    public void addVariable(RuleVariable var)
    {
        _variables.add(var);
    }

    public void setValue(IValue value)
    {
        _value = value;
    }

    public boolean process(InstructionQueue queue)
    {
        // go through each variable and set the value

        for (RuleVariable variable : _variables)
        {
            variable.setValue(_value, true);
        }

        return true;
    }
}
