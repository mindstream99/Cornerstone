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
 *
 * @author Robert Englander
 */
public class IfInstruction extends Instruction
{
    // the conditional value
    IValue _condition = null;

    // the "if" block instructions
    InstructionQueue _ifBlock = new InstructionQueue();

    // the list of else if blocks
    List<ElseIfBlock> _elseIfBlocks = new ArrayList<ElseIfBlock>();

    // the "else" block instructions
    InstructionQueue _elseBlock = new InstructionQueue();

    private class ElseIfBlock
    {
        IValue _condition;
        InstructionQueue _queue;

        ElseIfBlock(IValue condition, InstructionQueue queue)
        {
            _condition = condition;
            _queue = queue;
        }

        IValue getCondition()
        {
            return _condition;
        }

        InstructionQueue getBlock()
        {
            return _queue;
        }
    }

    public IfInstruction() {

    }
    
    public IfInstruction(InstructionQueue queue)
    {
        _ifBlock.setRuleSet(queue.getRuleSet());
        _elseBlock.setRuleSet(queue.getRuleSet());
    }

    public boolean hasReturnStatement()
    {
        if (_ifBlock.hasReturnInstruction() ||
            _elseBlock.hasReturnInstruction())
        {
            return true;
        }

        int count = _elseIfBlocks.size();
        for (int i = 0; i < count; i++)
        {
            ElseIfBlock block = _elseIfBlocks.get(i);
            InstructionQueue queue = block.getBlock();
            if (queue.hasReturnInstruction())
            {
                return true;
            }
        }

        return false;
    }

    /**
     * sets the conditional value
     * @param condition
     */
    public void setCondition(IValue condition)
    {
        _condition = condition;
    }

    public InstructionQueue addElseIfBlock(IValue condition)
    {
        InstructionQueue queue = new InstructionQueue();
        ElseIfBlock block = new ElseIfBlock(condition, queue);
        _elseIfBlocks.add(block);
        queue.setRuleSet(_ifBlock.getRuleSet());

        return queue;
    }

    /**
     * returns the instruction queue for the "if" block
     * @return
     */
    public InstructionQueue getIfBlock()
    {
        return _ifBlock;
    }

    /**
     * returns the instruction queue for the "else" block
     * @return
     */
    public InstructionQueue getElseBlock()
    {
        return _elseBlock;
    }

    /**
     * Processes this instruction
     * @param queue
     * @return
     */
    public boolean process(InstructionQueue queue)
    {
        // I don't remember why this is done here since
        // it is already done in the constructor.
        _ifBlock.setRuleSet(queue.getRuleSet());
        _elseBlock.setRuleSet(queue.getRuleSet());

        boolean result = true;

        // evaluate the condition
        if (_condition.valueAsBoolean())
        {
            // run the if block queue
            result = _ifBlock.process(_ifBlock);
        }
        else
        {
            // try the else if blocks
            boolean done = false;
            for (ElseIfBlock block : _elseIfBlocks)
            {
                if (block.getCondition().valueAsBoolean())
                {
                    result = block.getBlock().process(queue);
                    done = true;
                    break;
                }
            }

            if (!done)
            {
                // run the else block queue
                result = _elseBlock.process(_elseBlock);
            }
        }

        return result;
    }
}
