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
 * This class represents an Array variable.  Arrays
 * can contain values of any types, including other arrays.
 *
 * @author Robert Englander
 */
public class Array extends RuleVariable {

    private static final long serialVersionUID = 2L;
    private static final String ARRAY = "Array";

    private static MethodProvider<Array> methodProvider = new MethodProvider<Array>(Array.class);
    static {
        methodProvider.initialize();
    }
    
    private List<IValue> elements = null;

    public Array() {

    }
    
    @Override
    protected MethodProvider<Array> getMethodProvider() {
        return methodProvider;
    }

    /**
     * @param name the name of the array variable.
     */
    public Array(String name) {
        super(name);
    }

    public boolean isValueNull() {
    	return null == elements;
    }

    public String getType() {
        return ARRAY;
    }
    
    public void resetValue() {
    	if (!this.getHasParameterDefault()) {
            elements = null;
    	}
    }
    
    /**
     * Initializes the array to the specified size.
     * @param size the size of the array.
     */
    public void initialize(int size) {
        // allocate the array
        elements = new ArrayList<IValue>();
      
        for (int i = 0; i < size; i++) {
            // fill the empty slots with a default integer
            elements.add(i, new IntegerVariable(null));
        }
    }

    /**
     * Initializes the array with the values provided.
     * @param vals the list of values.
     */
    public void initialize(List<IValue> vals) {
        elements = new ArrayList<IValue>();
        elements.addAll(vals);
    }

    /**
     * Initializes the array with the values of another array.
     * @param array the array to copy values from.
     */
    public void initialize(Array array) {
        int size = array.size();
        elements = new ArrayList<IValue>();
        for (int i = 0; i < size; i++)
        {
            IValue elem = array.valueAt(i).evaluate();
            elements.add(elem);
        }
    }

    /**
     * Initializes the array with the values contained in
     * the values array.
     * @param values the values to use to initialize the array.
     */
    public void initialize(IValue[] values) {
        int size = values.length;
        elements = new ArrayList<IValue>();
        for (int i = 0; i < size; i++)
        {
            elements.add(values[i].evaluate());
        }
    }

    public int size() {
        int size = 0;
        if (elements != null) {
            size = elements.size();
        }

        return size;
    }

    /**
     * Indicates that this is an array.
     * @return true
     */
    @Override
    public boolean isArray() {
        return true;
    }

    /**
     * Returns the value at the specified location.
     * @param index the index of the value
     * @return the value at the specified index
     */
    public IValue valueAt(int index) {
        return elements.get(index);
    }

    /**
     * Sets the value at the specified index
     * @param index the index
     * @param val the new value
     */
    public void setValue(int index, IValue val) {
        elements.set(index, val.evaluate());

        if (runtime != null) {
            runtime.variableChange(this);
        }
    }

    /**
     * Sets the value of the array itself.  Arrays can
     * only be assigned other arrays as their value.
     * @param val the array to assign
     */
    protected void setValue(IValue val) {
        // if the value being assigned is an object expression,
        // then we need to evaluate it before determining if
        // the value is actually an array.
        IValue value = val;
        if (val instanceof ObjectMethodExpression) {
            ObjectMethodExpression m = (ObjectMethodExpression)val;
            value = m.execute();
        } else if (val instanceof ArrayIndexer) {
            // we need to evaluate the indexed value
            ArrayIndexer indexer = (ArrayIndexer)val;
            value = (IValue)indexer.valueAsObject();
        }

        if (!value.isArray()) {
            throw new ScriptExecutionException(104, "Can't assign a non array value to array '" + getName() + "'.");
        } else {
            // init this array with the new value
            Array aval = (Array)value;
            initialize(aval);
        }

        // tell the monitor about this change
        if (runtime != null) {
            runtime.variableChange(this);
        }
    }

    public String valueAsString() {
        StringBuffer buffer = new StringBuffer("[");

        boolean first = true;
        if (elements != null) {
            for (IValue value : elements) {
                if (first) {
                    first = false;
                } else {
                    buffer.append(", ");
                }

                buffer.append(value.valueAsString());
            }
        }
        buffer.append("]");
        return buffer.toString();
    }

    public Double valueAsDouble() {
        throw new ScriptExecutionException(105, "Can't get the Double value of an array");
    }

    public Integer valueAsInteger() {
        throw new ScriptExecutionException(105, "Can't get the Integer value of array '" + getName() + "'.");
    }

    public Boolean valueAsBoolean() {
        throw new ScriptExecutionException(105, "Can't get the Boolean value of array '" + getName() + "'.");
    }

    @Override
    public ResultVariable valueAsResult() {
        throw new ScriptExecutionException(105, "Can't get the Result value of an array");
    }

    public Object valueAsObject() {
        return this;
    }

    public Object[] valueAsObjectArray() {
	return elements.toArray();
    }

    public IValue evaluate() {
        Array array = new Array(null);
        array.initialize(this);
        return array;
    }

    /**
     * Appends String values to the array.  The first
     * parameter can be either a single value
     * or an array.
     * @param params
     * @return
     */
    @CSLMethod
    public IValue appendAsString(IValue param) {
        if (param.isArray()) {
            Array array = (Array)param;
            int oldSize = size();
            grow(new IntegerVariable(null, array.size()));
            int newSize = size();
            int j = 0;
            for (int i = oldSize; i < newSize; i++) {
                elements.set(i, new StringVariable(null, array.valueAt(j).valueAsString()));
                j++;
            }
        } else {
            int oldSize = size();
            grow(new IntegerVariable(null, 1));
            elements.set(oldSize, new StringVariable(null, param.valueAsString()));
        }

        return new BooleanVariable(null, true);
    }

    /**
     * Grows the size of the array.  The new elements
     * slots are added to the end of the array, and the
     * existing data is left alone.
     * @param params the first parameter is an integer containing
     * the number of new rows to add to the array.
     * @return true
     */
    @CSLMethod
    public void grow(IValue sz) {
        int size = sz.valueAsInteger();
        for (int i = 0; i < size; i++) {
            elements.add(null);
        }
    }

    /**
     * Returns the size of the array.
     * @return the size
     */
    @CSLMethod
    public IValue getSize() {
        return new IntegerVariable(null, size());
    }

    /**
     * Determines if a value is contained in the array.  This
     * is determined using string values.
     * @param params The value to check is in params[0]
     * @return true if the value is contained in the array,
     *         false otherwise
     */
    @CSLMethod
    public IValue contains(IValue val) {
        // grab the parameter value as a string
        String value = val.valueAsString();

        // iterate over the values in the array, comparing
        // them to the parameter value.  if a match is
        // found we can stop searching.
        boolean has = false;
        for (IValue element : elements) {
            if (element != null) {
                if (value.equals(element.valueAsString())) {
                    has = true;
                    break;
                }
            }
        }

        return new BooleanVariable(null, has);
    }
}



