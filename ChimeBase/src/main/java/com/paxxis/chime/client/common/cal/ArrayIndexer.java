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
 * The ArrayIndexer is used to set and get the value of
 * an array at a given location (index).
 *
 * @author Robert Englander
 */
public class ArrayIndexer extends RuleVariable
{
    // the array variable
    Array _array;

    // the index value
    IValue _index;

    // the instruction queue that this indexer belongs to
    InstructionQueue _queue;

    public ArrayIndexer() {

    }
    
    /**
     * Constructs the indexer.
     * @param queue the queue it belongs to
     * @param array the array variable being indexed
     * @param index the index
     */
    public ArrayIndexer(InstructionQueue queue,
                        Array array, IValue index)
    {
        // we'll use the same name as the array
        super(array.getName());

        // save the array, index, and queue
        _array = array;
        _index = index;
        _queue = queue;
    }

    public String getType()
    {
        return "Array Index";
    }
    
    public void resetValue()
    {
        
    }
    
    /**
     * Sets the value of the array at the established index
     * @param val the new value
     */
    public void setValue(IValue val, boolean trigger)
    {
        _array.setValue(_index.valueAsInteger(), val.evaluate());
    }

    /**
     * Get the value of this variable as an object.
     * @return it's value
     */
    public Object valueAsObject()
    {
        return _array.valueAt(_index.valueAsInteger()).valueAsObject();
    }

    /**
     * Get the value of this variable as a string.
     * @return it's value
     */
    public String valueAsString()
    {
        return _array.valueAt(_index.valueAsInteger()).valueAsString();
    }

    /**
     * Get the value of this variable as a double.
     * @return it's value
     */
    public double valueAsDouble()
    {
        return _array.valueAt(_index.valueAsInteger()).valueAsDouble();
    }

    /**
     * Get the value of this variable as an integer.
     * @return it's value
     */
    public int valueAsInteger()
    {
        return _array.valueAt(_index.valueAsInteger()).valueAsInteger();
    }

    /**
     * Get the value of this variable as a boolean.
     * @return it's value
     */
    public boolean valueAsBoolean()
    {
        return _array.valueAt(_index.valueAsInteger()).valueAsBoolean();
    }

    /**
     * Evaluate (determine its value) without returning it
     * to the caller.
     */
    public IValue evaluate()
    {
        return _array.valueAt(_index.valueAsInteger());
    }

}
