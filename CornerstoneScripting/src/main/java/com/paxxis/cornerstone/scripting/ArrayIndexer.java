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
 * The ArrayIndexer is used to set and get the value of
 * an array at a given location (index).
 *
 * @author Robert Englander
 */
public class ArrayIndexer extends RuleVariable {
    private static final long serialVersionUID = 1L;

    // the array variable
    private Array array;

    // the index value
    private IValue index = null;

    public ArrayIndexer() {

    }
    
    /**
     * Constructs the indexer.
     * @param queue the queue it belongs to
     * @param array the array variable being indexed
     * @param index the index
     */
    public ArrayIndexer(InstructionQueue queue,
                        Array array, IValue index) {
        // we'll use the same name as the array
        super(array.getName());

        // save the array, index, and queue
        this.array = array;
        this.index = index;
    }

    public boolean isNull() {
    	return null == index;
    }
    
    public String getType() {
        return "Array Index";
    }
    
    public void resetValue() {
    }
    
    /**
     * Sets the value of the array at the established index
     * @param val the new value
     */
    protected void setValue(IValue val) {
        array.setValue(index.valueAsInteger(), val.evaluate());
    }

    public Object valueAsObject() {
    	if (isNull()) {
    	    return null;
    	}
    	
        return array.valueAt(index.valueAsInteger()).valueAsObject();
    }

    public String valueAsString() {
    	if (isNull()) {
    	    return null;
    	}
    	
        return array.valueAt(index.valueAsInteger()).valueAsString();
    }

    public Double valueAsDouble() {
    	if (isNull()) {
    	    return null;
    	}
    	
        return array.valueAt(index.valueAsInteger()).valueAsDouble();
    }

    public Integer valueAsInteger() {
    	if (isNull()) {
    	    return null;
    	}
    	
        return array.valueAt(index.valueAsInteger()).valueAsInteger();
    }

    public Boolean valueAsBoolean() {
    	if (isNull()) {
    	    return null;
    	}
    	
        return array.valueAt(index.valueAsInteger()).valueAsBoolean();
    }


    @Override
    public ResultVariable valueAsResult() {
        ResultVariable res = new ResultVariable(null, valueAsBoolean());
        return res;
    }

    public IValue evaluate() {
        return array.valueAt(index.valueAsInteger());
    }

}
