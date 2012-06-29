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
 *
 * @author Robert Englander
 */
public class IfInstruction extends Instruction {
    private static final long serialVersionUID = 1L;

    // the conditional value
    private IValue condition = null;

    // the "if" block instructions
    private InstructionQueue ifBlock = new InstructionQueue();

    // the list of else if blocks
    private List<ElseIfBlock> elseIfBlocks = new ArrayList<ElseIfBlock>();

    // the "else" block instructions
    private static InstructionQueue elseBlock = new InstructionQueue();

    private class ElseIfBlock {
        private IValue cond;
        private InstructionQueue queue;

        ElseIfBlock(IValue condition, InstructionQueue queue) {
            this.cond = condition;
            this.queue = queue;
        }

        IValue getCondition() {
            return cond;
        }

        InstructionQueue getBlock() {
            return queue;
        }
    }

    public IfInstruction() {

    }
    
    public IfInstruction(InstructionQueue queue) {
        ifBlock.setRuleSet(queue.getRuleSet());
        elseBlock.setRuleSet(queue.getRuleSet());
    }

    public boolean hasReturnStatement() {
        if (ifBlock.hasReturnInstruction() ||
            elseBlock.hasReturnInstruction()) {
            return true;
        }

        int count = elseIfBlocks.size();
        for (int i = 0; i < count; i++) {
            ElseIfBlock block = elseIfBlocks.get(i);
            InstructionQueue queue = block.getBlock();
            if (queue.hasReturnInstruction()) {
                return true;
            }
        }

        return false;
    }

    /**
     * sets the conditional value
     * @param condition
     */
    public void setCondition(IValue condition) {
        this.condition = condition;
    }

    public InstructionQueue addElseIfBlock(IValue condition) {
        InstructionQueue queue = new InstructionQueue();
        ElseIfBlock block = new ElseIfBlock(condition, queue);
        elseIfBlocks.add(block);
        queue.setRuleSet(ifBlock.getRuleSet());

        return queue;
    }

    /**
     * returns the instruction queue for the "if" block
     * @return
     */
    public InstructionQueue getIfBlock() {
        return ifBlock;
    }

    /**
     * returns the instruction queue for the "else" block
     * @return
     */
    public InstructionQueue getElseBlock() {
        return elseBlock;
    }

    /**
     * Processes this instruction
     * @param queue
     * @return
     */
    public boolean process(InstructionQueue queue) {
        // FIXME I don't remember why this is done here since
        // it is already done in the constructor.
        ifBlock.setRuleSet(queue.getRuleSet());
        elseBlock.setRuleSet(queue.getRuleSet());

        boolean result = true;

        // evaluate the condition
        if (condition.valueAsBoolean()) {
            // run the if block queue
            result = ifBlock.process(ifBlock);
        } else {
            // try the else if blocks
            boolean done = false;
            for (ElseIfBlock block : elseIfBlocks) {
                if (block.getCondition().valueAsBoolean()) {
                    result = block.getBlock().process(queue);
                    done = true;
                    break;
                }
            }

            if (!done) {
                // run the else block queue
                result = elseBlock.process(elseBlock);
            }
        }

        return result;
    }
}
