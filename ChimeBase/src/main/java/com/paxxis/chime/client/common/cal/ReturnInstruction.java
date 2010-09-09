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

/**
 * @author Robert Englander
 */
public class ReturnInstruction extends Instruction
{
    // the value to return
    IValue _return;

    // the rule itself
    Rule _rule;
    
    public ReturnInstruction()
    {
    }

    public boolean hasReturnStatement()
    {
        return true;
    }

    public void setValue(Rule rule, IValue val)
                    throws RuleCreationException
    {
        /*
        RuleVariable rv = queue.getVariable("#return");
        if (rv == null)
        {
            // this is an illegal attempt to return a value
            // because the rule doesn't have a return defined
            throw new RuleCreationException("Illegal return statement.  Rule does not define a return type.");
        }
        */
        _rule = rule;
        _return = val;
    }

    public boolean process(InstructionQueue queue)
    {
        _rule.setReturnValue(_return);
        
        // return false because processing should not
        // continue beyond this instruction.
        return false;
    }
}
