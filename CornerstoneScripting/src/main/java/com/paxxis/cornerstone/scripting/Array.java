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

    private static final long serialVersionUID = 1L;
    private static final String ARRAY = "Array";
    
    private static enum Methods {
        size,
        contains,
        grow,
        appendAsString;
        
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

    private List<IValue> elements = null;

    public Array() {

    }
    
    /**
     * @param name the name of the array variable.
     */
    public Array(String name) {
        super(name);
    }

    public boolean isNull() {
    	return null == elements;
    }

    public String getType() {
        return ARRAY;
    }
    
    public void resetValue() {
        elements = null;
    }
    
    public boolean methodHasReturn(String name) {
    	if (Methods.contains(name)) {
    	    switch (Methods.valueOf(name)) {
                case size:
                case contains:
                    return true;
                default:
                    return false;
            }
    	}

        return super.methodHasReturn(name);
    }

    public int getMethodParameterCount(String name) {

    	if (Methods.contains(name)) {
    	    switch (Methods.valueOf(name)) {
                case size:
                    return 0;
                case contains:
                    return 1;
                case grow:
                    return 1;
                case appendAsString:
                    return 1;
                default:
                    return 0;
            }
    	}
        
        return super.getMethodParameterCount(name);
    }
    
    public IValue executeMethod(String name, List<IValue> params) {
    	if (Methods.contains(name)) {
            switch (Methods.valueOf(name)) {
                case size:
                    return getSize(params);
                case contains:
                    return contains(params);
                case grow:
                    return grow(params);
                case appendAsString:
                    return appendAsString(params);
                default:
                    throw new RuntimeException("No such method: " + name);
            }
    	}

        return super.executeMethod(name, params);
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

        if (_monitor != null) {
            _monitor.variableChange(this);
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
            throw new RuntimeException("Can't assign a non array value to array '" + getName() + "'.");
        } else {
            // init this array with the new value
            Array aval = (Array)value;
            initialize(aval);
        }

        // tell the monitor about this change
        if (_monitor != null) {
            _monitor.variableChange(this);
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
        throw new RuntimeException("Can't get the Double value of an array");
    }

    public Integer valueAsInteger() {
        throw new RuntimeException("Can't get the Integer value of array '" + getName() + "'.");
    }

    public Boolean valueAsBoolean() {
        throw new RuntimeException("Can't get the Boolean value of array '" + getName() + "'.");
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
    protected IValue appendAsString(List<IValue> params) {
        IValue param = params.get(0);
        if (param.isArray()) {
            Array array = (Array)param;
            int oldSize = size();
            grow(array.size());
            int newSize = size();
            int j = 0;
            for (int i = oldSize; i < newSize; i++) {
                elements.set(i, new StringVariable(null, array.valueAt(j).valueAsString()));
                j++;
            }
        } else {
            int oldSize = size();
            grow(1);
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
    protected IValue grow(List<IValue> params) {
        int newRows = params.get(0).valueAsInteger();
        grow(newRows);
        return new BooleanVariable(null, true);
    }

    /**
     * @param size the size to grow by
     */
    void grow(int size) {
        for (int i = 0; i < size; i++) {
            elements.add(null);
        }
    }

    /**
     * Returns the size of the array.
     * @return the size
     */
    protected IValue getSize(List<IValue> params) {
        return new IntegerVariable(null, size());
    }

    /**
     * Determines if a value is contained in the array.  This
     * is determined using string values.
     * @param params The value to check is in params[0]
     * @return true if the value is contained in the array,
     *         false otherwise
     */
    protected IValue contains(List<IValue> params) {
        // grab the parameter value as a string
        String value = params.get(0).valueAsString();

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



