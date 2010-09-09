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

import java.util.HashMap;

/**
 * @author Robert Englander
 */
public class SwitchInstruction extends Instruction
{
    // a map of values to instruction queues
    HashMap<Object, InstructionQueue> _caseMap = new HashMap<Object, InstructionQueue>();

    // the default case queue
    InstructionQueue _defaultCase = null;

    // the value to switch on
    IValue _value = null;

    // true if using string matching to find the
    // case to run, false otherwise (number matching)
    boolean _stringMatching = false;

    // the parent queue
    InstructionQueue _parentQueue = null;

    public SwitchInstruction() {

    }
    
    public SwitchInstruction(InstructionQueue queue)
    {
        // hmmm...
       // _parentQueue = queue;
    }

    public boolean hasReturnStatement()
    {
        if (_defaultCase != null)
        {
            if (_defaultCase.hasReturnInstruction())
            {
                return true;
            }
        }

        Object[] queues = _caseMap.values().toArray();
        int count = queues.length;
        for (int i = 0; i < count; i++)
        {
            InstructionQueue queue = (InstructionQueue)queues[i];
            if (queue.hasReturnInstruction())
            {
                return true;
            }
        }

        return false;
    }

    public void setStringMatching(boolean stringMatching)
    {
        _stringMatching = stringMatching;
    }

    public void setValue(IValue value)
    {
        _value = value;
    }

    public void setDefaultCase(InstructionQueue defCase)
    {
        _defaultCase = defCase;
    }

    public void addCase(String value, InstructionQueue block)
    {
        if (_stringMatching)
        {
            _caseMap.put(value, block);
        }
        else
        {
            _caseMap.put(new Double(value), block);
        }
    }

    public boolean process(InstructionQueue queue)
    {
        Object val;
        if (_stringMatching)
        {
            val = _value.valueAsString();
        }
        else
        {
            val = new Double(_value.valueAsDouble());
        }

        // get the queue for the switch value
        InstructionQueue q = (InstructionQueue)_caseMap.get(val);

        boolean result = true;

        if (q != null)
        {
            // run that block
            q.setRuleSet(queue.getRuleSet());
            result = q.process(q);
        }
        else if (_defaultCase != null)
        {
            // run the default case if there is one
            _defaultCase.setRuleSet(queue.getRuleSet());
            result = _defaultCase.process(_defaultCase);
        }

        return result;
    }
}
