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
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class Table extends RuleVariable {
    private static final long serialVersionUID = 1L;

    private static enum Methods {
        setColumnNames,
        setPrimaryColumn,
        setSecondaryColumn,
        setTitle,
        setColumnFormat;
        
	public static boolean contains(String name) {
	    boolean contains = false;
	    for (Methods option : Methods.values()) {
		if (option.toString().equals(name)) {
		    contains = true;
		    break;
		}
	    }
		
	    return contains;
	}
    }

    // the underlying array of values
    private ArrayList<ArrayList<IValue>> elements = new ArrayList<ArrayList<IValue>>();

    // the column names
    private ArrayList<IValue> columnNames = new ArrayList<IValue>();

    // column formats
    private HashMap<Integer, String> columnFormats = new HashMap<Integer, String>();
    
    public Table() {
    }

    public Table(String name) {
        super(name);
    }

    public boolean isNull() {
    	return false;
    }
    
    public String getColumnFormat(int col) {
    	String fmt = columnFormats.get(col);
    	return fmt; 
    }
    
    public List<IValue> getColumnNames() {
        return columnNames;
    }
    
    public HashMap<Integer, String> getColumnFormats() {
    	return columnFormats;
    }
    
    public String getType() {
        return "Table";
    }

    public void initialize(Table table) {
        int rows = table.rowCount();
        int cols = table.columnCount();

        elements = new ArrayList<ArrayList<IValue>>();

        for (int i = 0; i < rows; i++) {
            addRow(null);
            for (int j = 0; j < cols; j++) {
                IValue elem = table.valueAt(i, j).evaluate();
                elements.get(i).add(elem);
            }
        }

        columnNames.clear();
        columnFormats.clear();
        
        for (IValue name : table.getColumnNames()) {
            columnNames.add(new StringVariable(null, name.valueAsString()));
        }
        
        columnFormats.putAll(table.getColumnFormats());
    }

    public void resetValue() {
    	if (this.getHasParameterDefault()) {
            elements = new ArrayList<ArrayList<IValue>>();
            columnNames = new ArrayList<IValue>();
            columnFormats = new HashMap<Integer, String>();
    	}
    }

    public boolean methodHasReturn(String name) {
    	if (Methods.contains(name)) {
    		return false;
    	}
        return super.methodHasReturn(name);
    }

    public int getMethodParameterCount(String name) {
    	if (Methods.contains(name)) {
            switch (Methods.valueOf(name)) {
                case setColumnNames:
                    return 1;
                case setColumnFormat:
                    return 2;
            }
    	}

        return super.getMethodParameterCount(name);
    }

    public IValue executeMethod(String name, List<IValue> params) {
    	if (Methods.contains(name)) {
            switch (Methods.valueOf(name)) {
                case setColumnNames:
                    return setColumnNames(params);
                case setColumnFormat:
                    return setColumnFormat(params);
            }
    	}

        return super.executeMethod(name, params);
    }

    public List<IValue> getRow(int idx) {
        return elements.get(idx);
    }

    public int rowCount() {
        return elements.size();
    }

    public int columnCount() {
        return columnNames.size();
    }

    public boolean isTable() {
        return true;
    }

    /**
     * @return the value at the specified location
     */
    public IValue valueAt(int row, int col) {
        return elements.get(row).get(col);
    }

    /**
     * Sets the value at the specified location
     */
    public void setValue(int row, int col, IValue val) {
        // if the specified row doesn't exist, expand the table to accomodate it
        int rows = rowCount();
        while ((row + 1) > rows) {
            addRow(null);
            rows++;
        }

        elements.get(row).set(col, val.evaluate());

        if (runtime != null) {
            runtime.variableChange(this);
        }
    }

    /**
     * Sets the value of the table itself.  Tables can
     * only be assigned other tables as their value.
     * @param val the table to assign
     */
    protected void setValue(IValue val) {
        // if the value being assigned is an object expression,
        // then we need to evaluate it before determining if
        // the value is actually a table.
        IValue value = val;
        if (val instanceof ObjectMethodExpression) {
            ObjectMethodExpression m = (ObjectMethodExpression)val;
            value = m.execute();
        } else if (val instanceof TableIndexer) {
            // we need to evaluate the indexed value
            TableIndexer indexer = (TableIndexer)val;
            value = (IValue)indexer.valueAsObject();
        }

        if (!value.isTable()) {
            throw new ScriptExecutionException(501, "Can't assign a non table value to table '" + getName() + "'.");
        } else {
            // init this table with the new value
            Table aval = (Table)value;
            initialize(aval);
        }

        // tell the monitor about this change
        if (runtime != null) {
            runtime.variableChange(this);
        }
    }

    /**
     * Get the value of this variable as a string.
     * @return it's value
     */
    public String valueAsString() {
        return "[table]";
    }

    /**
     * Get the value of this variable as a double.
     * @return it's value
     */
    public Double valueAsDouble() {
        throw new ScriptExecutionException(503, "Can't get the Double value of a table");
    }

    /**
     * Get the value of this variable as an integer.
     * @return it's value
     */
    public Integer valueAsInteger() {
        throw new ScriptExecutionException(504, "Can't get the Integer value of a table");
    }

    /**
     * Get the value of this variable as a boolean.
     * @return it's value
     */
    public Boolean valueAsBoolean() {
        throw new ScriptExecutionException(505, "Can't get the Boolean value of a table");
    }

    @Override
    public ResultVariable valueAsResult() {
	throw new ScriptExecutionException(506, "Can't get the Result value of a table");
    }

    /**
     * Get the value of this variable as an object.  We
     * just return ourself as an IValue
     * @return it's value
     */
    public Object valueAsObject() {
        return this;
    }

    /**
     * Evaluate (determine its value) without returning it
     * to the caller.
     */
    public IValue evaluate() {
        Table table = new Table(null);
        table.initialize(this);
        return table;
    }

    protected IValue setColumnFormat(List<IValue> params) {
    	// the first parameter is the column index
    	int col = params.get(0).valueAsInteger();
    	
    	// the second parameter is the format
    	String fmt = params.get(1).valueAsString();
    	
    	columnFormats.put(col, fmt);

    	return new BooleanVariable(null, true);
    }
    
    public void setColumns(List<String> names) {
        columnNames.clear();
        int cnt = names.size();
        for (int i = 0; i < cnt; i++) {
            columnNames.add(new StringVariable(null, names.get(i)));
        }
    }
    
    protected IValue setColumnNames(List<IValue> params) {
        // the first parameter must be an array
        if (!(params.get(0) instanceof Array)) {
            throw new ScriptExecutionException(510, "setColumnNames parameter must be an Array");
        }

        columnNames.clear();

        Array names = (Array)params.get(0);
        int cnt = names.size();
        for (int i = 0; i < cnt; i++) {
            columnNames.add(new StringVariable(null, names.valueAt(i).valueAsString()));
        }

        return new BooleanVariable(null, true);
    }

    protected IValue setColumnName(List<IValue> params) {
        // the first parameter is the column index
        int col = params.get(0).valueAsInteger();

        // the 2nd parameters is the column name
        String name = params.get(1).valueAsString();

        columnNames.set(col, new StringVariable(null, name));

        return new BooleanVariable(null, true);
    }

    protected IValue addRow(List<IValue> params) {
        ArrayList<IValue> row = new ArrayList<IValue>();
        for (int i = 0; i < columnNames.size(); i++) {
            row.add(new IntegerVariable(null, 0));
        }

        elements.add(row);

        return new IntegerVariable(null, elements.size());
    }

}




