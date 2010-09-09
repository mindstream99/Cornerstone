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
 * This class represents an Array variable.  Arrays
 * can contain values of any types, including other arrays.
 *
 * @author Robert Englander
 */
public class Array extends RuleVariable implements IRuleObject
{
    private static enum Methods
    {
        size,
        contains,
        grow,
        appendAsString
    }

    // the underlying array of values
    List<IValue> _elements = null;

    public Array() {

    }
    
    /**
     * Constructs an array.
     * @param name the name of the array variable.
     */
    public Array(String name)
    {
        super(name);
    }

    public String getType()
    {
        return "Array";
    }
    
    public void resetValue()
    {
        _elements = null;
    }
    
    /**
     * 
     */
    public boolean methodHasReturn(String name)
    {
        switch (Methods.valueOf(name))
        {
            case size:
            case contains:
                return true;
        }

        // no return value
        return false;
    }

    public int getMethodParameterCount(String name)
    {
        switch (Methods.valueOf(name))
        {
            case size:
                return 0;
            case contains:
                return 1;
            case grow:
                return 1;
            case appendAsString:
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
            case size:
                return getSize(params);
            case contains:
                return contains(params);
            case grow:
                return grow(params);
            case appendAsString:
                return appendAsString(params);
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

    /**
     * Initializes the array to the specified size.
     * @param size the size of the array.
     */
    public void initialize(int size)
    {
        // allocate the array
        _elements = new ArrayList<IValue>();
      
        for (int i = 0; i < size; i++)
        {
            // fill the empty slots with a default integer
            _elements.add(i, new IntegerVariable(null));
        }
    }

    /**
     * Initializes the array with the values provided.
     * @param vals the list of values.
     */
    public void initialize(List<IValue> vals)
    {
        // allocate enough space to accomodate the values,
        // and iterate over the passed array to copy its values.
        int size = vals.size();
        _elements = new ArrayList<IValue>();
        for (int i = 0; i < size; i++)
        {
            _elements.add(vals.get(i));
        }
    }

    /**
     * Initializes the array with the values of another array.
     * @param array the array to copy values from.
     */
    public void initialize(Array array)
    {
        // allocate enough space to accomodate the values,
        // and iterate over the passed array to copy its values.
        int size = array.size();
        _elements = new ArrayList<IValue>();
        for (int i = 0; i < size; i++)
        {
            IValue elem = array.valueAt(i).evaluate();
            _elements.add(elem);
        }
    }

    /**
     * Intitializes the array with the values contained in
     * the values array.
     * @param values the values to use to initialize the array.
     */
    public void initialize(IValue[] values)
    {
        // allocate enough space to accomodate the values,
        // and iterate over the passed array to copy its values.
        int size = values.length;
        _elements = new ArrayList<IValue>();
        for (int i = 0; i < size; i++)
        {
            _elements.add(values[i].evaluate());
        }
    }

    /**
     * Returns the size of the array.
     * @return the size of the array.
     */
    public int size()
    {
        int size = 0;
        if (_elements != null)
        {
            size = _elements.size();
        }

        return size;
    }

    /**
     * Indicates that this is an array.
     * @return true
     */
    public boolean isArray()
    {
        return true;
    }

    /**
     * Returns the value at the specified location.
     * @param index the index of the value
     * @return the value at the specified index
     */
    public IValue valueAt(int index)
    {
        return _elements.get(index);
    }

    /**
     * Sets the value at the specified index
     * @param index the index
     * @param val the new value
     */
    public void setValue(int index, IValue val)
    {
        _elements.set(index, val.evaluate());

        if (_monitor != null)
        {
            _monitor.variableChange(this);
        }
    }

    /**
     * Sets the value of the array itself.  Arrays can
     * only be assigned other arrays as their value.
     * @param val the array to assign
     */
    public void setValue(IValue val, boolean trigger)
    {
        // if the value being assigned is an object expression,
        // then we need to evaluate it before determining if
        // the value is actually an array.
        IValue value = val;
        if (val instanceof ObjectMethodExpression)
        {
            ObjectMethodExpression m = (ObjectMethodExpression)val;
            value = m.execute();
        }
        else if (val instanceof ArrayIndexer)
        {
            // we need to evaluate the indexed value
            ArrayIndexer indexer = (ArrayIndexer)val;
            value = (IValue)indexer.valueAsObject();
        }

        if (!value.isArray()) // && !isProxy)
        {
            throw new RuntimeException("Can't assign a non array value to array '" + getName() + "'.");
        }
        else
        {
            // init this array with the new value
            Array aval = (Array)value;
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
        StringBuffer buffer = new StringBuffer("[");

        boolean first = true;
        if (_elements != null)
        {
            for (IValue value : _elements)
            {
                if (first)
                {
                    first = false;
                }
                else
                {
                    buffer.append(", ");
                }

                buffer.append(value.valueAsString());
            }
        }
        buffer.append("]");
        return buffer.toString();
    }

    /**
     * Get the value of this variable as a double.
     * @return it's value
     */
    public double valueAsDouble()
    {
        throw new RuntimeException("Can't get the Double value of an array");
    }

    /**
     * Get the value of this variable as an integer.
     * @return it's value
     */
    public int valueAsInteger()
    {
        throw new RuntimeException("Can't get the Integer value of array '" + getName() + "'.");
    }

    /**
     * Get the value of this variable as a boolean.
     * @return it's value
     */
    public boolean valueAsBoolean()
    {
        throw new RuntimeException("Can't get the Boolean value of array '" + getName() + "'.");
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
     * Get the values of the array as an Object[]
     * @return the object[]
     */
    public Object[] valueAsObjectArray()
    {
        return _elements.toArray();
    }

    /**
     * Evaluate (determine its value) without returning it
     * to the caller.
     */
    public IValue evaluate()
    {
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
    protected IValue appendAsString(List<IValue> params)
    {
        IValue param = params.get(0);
        if (param.isArray())
        {
            Array array = (Array)param;
            int oldSize = size();
            grow(array.size());
            int newSize = size();
            int j = 0;
            for (int i = oldSize; i < newSize; i++)
            {
                _elements.set(i, new StringVariable(null, array.valueAt(j).valueAsString()));
                j++;
            }
        }
        else
        {
            int oldSize = size();
            grow(1);
            _elements.set(oldSize, new StringVariable(null, param.valueAsString()));
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
    protected IValue grow(List<IValue> params)
    {
        int newRows = params.get(0).valueAsInteger();
        grow(newRows);
        return new BooleanVariable(null, true);
    }

    /**
     * @param size the size to grow by
     */
    void grow(int size)
    {
        for (int i = 0; i < size; i++)
        {
            _elements.add(null);
        }
    }

    /**
     * Returns the size of the array.
     * @return the size
     */
    protected IValue getSize(List<IValue> params)
    {
        return new IntegerVariable(null, size());
    }

    /**
     * Determines if a value is contained in the array.  This
     * is determined using string values.
     * @param params The value to check is in params[0]
     * @return true if the value is contained in the array,
     *         false otherwise
     */
    protected IValue contains(List<IValue> params)
    {
        // grab the parameter value as a string
        String value = params.get(0).valueAsString();

        // iterate over the values in the array, comparing
        // them to the parameter value.  if a match is
        // found we can stop searching.
        boolean has = false;
        for (IValue element : _elements)
        {
            if (element != null)
            {
                if (value.equals(element.valueAsString()))
                {
                    has = true;
                    break;
                }
            }
        }

        return new BooleanVariable(null, has);
    }
}



