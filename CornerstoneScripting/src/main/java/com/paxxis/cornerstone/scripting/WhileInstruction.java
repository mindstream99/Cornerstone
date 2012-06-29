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
public class WhileInstruction extends Instruction {
    private static final long serialVersionUID = 1L;

    // the conditional
    private IValue condition = null;

    // the instruction queue block
    private InstructionQueue block = new InstructionQueue();

    public WhileInstruction() {

    }
    
    public WhileInstruction(InstructionQueue queue) {
        block.setRuleSet(queue.getRuleSet());
    }

    @Override
    public boolean hasReturnStatement() {
        return block.hasReturnInstruction();
    }

    public void setCondition(IValue condition) {
        this.condition = condition;
    }

    public InstructionQueue getBlock() {
        return block;
    }

    public boolean process(InstructionQueue queue) {
	
	ServiceContextProvider context = block.getRuleSet().getMonitor().getServiceContextProvider(); 
	if (context != null && !context.allowsWhileLoops()) {
	    throw new RuntimeException("The service context does not allow WHILE loops.");
	}
	
        // set the rule set
        block.setRuleSet(queue.getRuleSet());

        // while instructions want the break
        block.setWantsBreak(true);

        // clear the break before we start
        block.setBreak(false);

        boolean result = true;

        // push this context onto the monitor stack
        block.getRuleSet().getMonitor().push(block);

        while (condition.valueAsBoolean()) {
            if (!block.process(block)) {
                result = block.getBreak();
                break;
            }

            block.getRuleSet().setCurrentInstruction(this);
            block.getRuleSet().getMonitor().setPoised();

        }

        block.getRuleSet().getMonitor().pop(block);
        return result;
    }
}

