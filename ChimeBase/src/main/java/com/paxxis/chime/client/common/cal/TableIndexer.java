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
 *
 * @author Robert Englander
 */
public class TableIndexer extends RuleVariable
{
    // the table variable
    Table _table;

    IValue _rowIndex;
    IValue _colIndex;

    // the instruction queue that this indexer belongs to
    InstructionQueue _queue;

    public TableIndexer() {

    }
    
    /**
     * Constructs the indexer.
     */
    public TableIndexer(InstructionQueue queue,
                        Table table, IValue row, IValue col)
    {
        // we'll use the same name
        super(table.getName());

        _table = table;
        _rowIndex = row;
        _colIndex = col;
        _queue = queue;
    }

    public String getType()
    {
        return "Table Index";
    }

    public void resetValue()
    {

    }

    public void setValue(IValue val, boolean trigger)
    {
        _table.setValue(_rowIndex.valueAsInteger(), _colIndex.valueAsInteger(), val.evaluate());
    }

    /**
     * Get the value of this variable as an object.
     * @return it's value
     */
    public Object valueAsObject()
    {
        return _table.valueAt(_rowIndex.valueAsInteger(), _colIndex.valueAsInteger()).valueAsObject();
    }

    /**
     * Get the value of this variable as a string.
     * @return it's value
     */
    public String valueAsString()
    {
        return _table.valueAt(_rowIndex.valueAsInteger(), _colIndex.valueAsInteger()).valueAsString();
    }

    /**
     * Get the value of this variable as a double.
     * @return it's value
     */
    public double valueAsDouble()
    {
        return _table.valueAt(_rowIndex.valueAsInteger(), _colIndex.valueAsInteger()).valueAsDouble();
    }

    /**
     * Get the value of this variable as an integer.
     * @return it's value
     */
    public int valueAsInteger()
    {
        return _table.valueAt(_rowIndex.valueAsInteger(), _colIndex.valueAsInteger()).valueAsInteger();
    }

    /**
     * Get the value of this variable as a boolean.
     * @return it's value
     */
    public boolean valueAsBoolean()
    {
        return _table.valueAt(_rowIndex.valueAsInteger(), _colIndex.valueAsInteger()).valueAsBoolean();
    }

    /**
     * Evaluate (determine its value) without returning it
     * to the caller.
     */
    public IValue evaluate()
    {
        return _table.valueAt(_rowIndex.valueAsInteger(), _colIndex.valueAsInteger());
    }

}
