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
 * Instruction for creating an array
 *
 * @author Robert Englander
 */
public class CreateArrayInstruction extends Instruction {
    private static final long serialVersionUID = 1L;

    // the name of the array
    private String _name = null;
    
    // the size of the array
    private IValue _size = null;

    public CreateArrayInstruction() {

    }
    
    public CreateArrayInstruction(String name, IValue size) {
        _name = name;
        _size = size;
    }

    public boolean canPoise() {
        return false;
    }

    public boolean process(InstructionQueue queue) {
        Array a = (Array)queue.getVariable(_name);
        int size = 0;
        if (_size != null) {
            size = _size.valueAsInteger();
        }

        if (size < 0) {
            throw new RuntimeException("Array size cannot be less than 0." +
                       "\nAttempt to set array '" + _name + "' to size: " + size);
        }

        a.initialize(size);

        return true;
    }
}
