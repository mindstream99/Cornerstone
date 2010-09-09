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
public class Table extends RuleVariable implements IRuleObject
{
    private static enum Methods
    {
        setColumnNames,
        setPrimaryColumn,
        setSecondaryColumn,
        setTitle
    }

    // the underlying array of values
    private ArrayList<ArrayList<IValue>> _elements = new ArrayList<ArrayList<IValue>>();

    // the column names
    private ArrayList<IValue> _columnNames = new ArrayList<IValue>();

    public Table() {
    }

    public Table(String name)
    {
        super(name);
    }

    public List<IValue> getColumnNames() {
        return _columnNames;
    }
    
    public String getType()
    {
        return "Table";
    }

    public void initialize(Table table)
    {
        int rows = table.rowCount();
        int cols = table.columnCount();

        _elements = new ArrayList<ArrayList<IValue>>();

        for (int i = 0; i < rows; i++)
        {
            addRow(null);
            for (int j = 0; j < cols; j++)
            {
                IValue elem = table.valueAt(i, j).evaluate();
                _elements.get(i).add(elem);
            }
        }

        _columnNames.clear();
        for (IValue name : table.getColumnNames()) {
            _columnNames.add(new StringVariable(null, name.valueAsString()));
        }
    }

    public void resetValue()
    {
        _elements = new ArrayList<ArrayList<IValue>>();
        _columnNames = new ArrayList<IValue>();
    }

    /**
     *
     */
    public boolean methodHasReturn(String name)
    {

        // no return value
        return false;
    }

    public int getMethodParameterCount(String name)
    {
        switch (Methods.valueOf(name))
        {
            case setColumnNames:
                return 1;
        }

        return 0;
    }

    /**
     *
     */
    public IValue executeMethod(String name, List<IValue> params)
    {
        switch (Methods.valueOf(name))
        {
            case setColumnNames:
                return setColumnNames(params);
        }

        // maybe we ought to be throwing an exception here, since
        // this should never happen.
        return null;
    }

    /**
     * Indicates that an array is an object.
     * @return true
     */
    public boolean isObject()
    {
        // arrays are objects
        return true;
    }

    public List<IValue> getRow(int idx) {
        return _elements.get(idx);
    }

    public int rowCount()
    {
        return _elements.size();
    }

    public int columnCount()
    {
        return _columnNames.size();
    }

    public boolean isTable()
    {
        return true;
    }

    /**
     * @return the value at the specified location
     */
    public IValue valueAt(int row, int col)
    {
        return _elements.get(row).get(col);
    }

    /**
     * Sets the value at the specified location
     */
    public void setValue(int row, int col, IValue val)
    {
        // if the specified row doesn't exist, expand the table to accomodate it
        int rows = rowCount();
        while ((row + 1) > rows) {
            addRow(null);
            rows++;
        }

        _elements.get(row).set(col, val.evaluate());

        if (_monitor != null)
        {
            _monitor.variableChange(this);
        }
    }

    /**
     * Sets the value of the table itself.  Tables can
     * only be assigned other tables as their value.
     * @param val the table to assign
     */
    public void setValue(IValue val, boolean trigger)
    {
        // if the value being assigned is an object expression,
        // then we need to evaluate it before determining if
        // the value is actually a table.
        IValue value = val;
        if (val instanceof ObjectMethodExpression)
        {
            ObjectMethodExpression m = (ObjectMethodExpression)val;
            value = m.execute();
        }
        else if (val instanceof TableIndexer)
        {
            // we need to evaluate the indexed value
            TableIndexer indexer = (TableIndexer)val;
            value = (IValue)indexer.valueAsObject();
        }

        if (!value.isTable())
        {
            throw new RuntimeException("Can't assign a non table value to table '" + getName() + "'.");
        }
        else
        {
            // init this table with the new value
            Table aval = (Table)value;
            initialize(aval);
        }

        // tell the monitor about this change
        _monitor.variableChange(this);
    }

    /**
     * Get the value of this variable as a string.
     * @return it's value
     */
    public String valueAsString()
    {
        return "[table]";
    }

    /**
     * Get the value of this variable as a double.
     * @return it's value
     */
    public double valueAsDouble()
    {
        throw new RuntimeException("Can't get the Double value of a table");
    }

    /**
     * Get the value of this variable as an integer.
     * @return it's value
     */
    public int valueAsInteger()
    {
        throw new RuntimeException("Can't get the Integer value of a table");
    }

    /**
     * Get the value of this variable as a boolean.
     * @return it's value
     */
    public boolean valueAsBoolean()
    {
        throw new RuntimeException("Can't get the Boolean value of a table");
    }

    /**
     * Get the value of this variable as an object.  We
     * just return ourself as an IValue
     * @return it's value
     */
    public Object valueAsObject()
    {
        return this;
    }

    /**
     * Evaluate (determine its value) without returning it
     * to the caller.
     */
    public IValue evaluate()
    {
        Table table = new Table(null);
        table.initialize(this);
        return table;
    }

    protected IValue setColumnNames(List<IValue> params) {
        // the first parameter must be an array
        if (!(params.get(0) instanceof Array)) {
            throw new RuntimeException("setColumnNames parameter must be an Array");
        }

        _columnNames.clear();

        Array names = (Array)params.get(0);
        int cnt = names.size();
        for (int i = 0; i < cnt; i++) {
            _columnNames.add(new StringVariable(null, names.valueAt(i).valueAsString()));
        }

        return new BooleanVariable(null, true);
    }

    protected IValue setColumnName(List<IValue> params) {
        // the first parameter is the column index
        int col = params.get(0).valueAsInteger();

        // the 2nd parameters is the column name
        String name = params.get(1).valueAsString();

        _columnNames.set(col, new StringVariable(null, name));

        return new BooleanVariable(null, true);
    }

    protected IValue addRow(List<IValue> params)
    {
        ArrayList<IValue> row = new ArrayList<IValue>();
        for (int i = 0; i < _columnNames.size(); i++) {
            row.add(new IntegerVariable(null, 0));
        }

        _elements.add(row);

        return new IntegerVariable(null, _elements.size());
    }

}




