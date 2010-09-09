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

import java.io.Serializable;


/**
 * This is the base class for all instructions.
 *
 * @author Robert Englander
 */
public abstract class Instruction implements SourceCodeReference, Serializable
{
    // the line number of the instruction
    int _lineNumber = -1;

    public Instruction() {

    }
    
    public boolean canPoise()
    {
        return _lineNumber != -1;
    }
    
    /**
     * Sets the line number.
     * @param number
     */
    public void setLineNumber(int number)
    {
        _lineNumber = number;
    }

    /**
     * Gets the line number
     * @return
     */
    public int getLineNumber()
    {
        return _lineNumber;
    }

    /**
     * Returns true if the instruction either IS
     * a return statement or contains a return statement.
     * @return
     */
    public boolean hasReturnStatement()
    {
        return false;
    }

    /**
     * Process the instruction.  All instruction types must
     * implement this method themselves.
     * @param queue
     * @return
     */
    public abstract boolean process(InstructionQueue queue);

}
