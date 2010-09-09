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

import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class StockSymbol extends RuleVariable implements IRuleObject
{
    private static enum Methods
    {
        getQuote
    }

    private IValue symbol;

    public StockSymbol() {
    }

    public StockSymbol(String name)
    {
        super(name);
    }

    public String getType()
    {
        return "StockSymbol";
    }

    public void initialize(IValue sym)
    {
        symbol = sym;
    }

    public void resetValue()
    {
    }

    /**
     *
     */
    public boolean methodHasReturn(String name)
    {
        switch (Methods.valueOf(name))
        {
            case getQuote:
                return true;
        }

        // no return value
        return false;
    }

    public int getMethodParameterCount(String name)
    {
        switch (Methods.valueOf(name))
        {
            case getQuote:
                return 0;
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
            case getQuote:
                return getQuote(params);
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
        else if (val instanceof ArrayIndexer)
        {
            // we need to evaluate the indexed value
            ArrayIndexer indexer = (ArrayIndexer)val;
            value = (IValue)indexer.valueAsObject();
        }
        else if (val instanceof TableIndexer)
        {
            // we need to evaluate the indexed value
            TableIndexer indexer = (TableIndexer)val;
            value = (IValue)indexer.valueAsObject();
        }

        // init this table with the new value
        initialize(val);

        // tell the monitor about this change
        _monitor.variableChange(this);
    }

    /**
     * Get the value of this variable as a string.
     * @return it's value
     */
    public String valueAsString()
    {
        return symbol.valueAsString();
    }

    /**
     * Get the value of this variable as a double.
     * @return it's value
     */
    public double valueAsDouble()
    {
        return symbol.valueAsDouble();
    }

    /**
     * Get the value of this variable as an integer.
     * @return it's value
     */
    public int valueAsInteger()
    {
        return symbol.valueAsInteger();
    }

    /**
     * Get the value of this variable as a boolean.
     * @return it's value
     */
    public boolean valueAsBoolean()
    {
        return symbol.valueAsBoolean();
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
        // this is a no-op
        return this;
    }

    protected IValue getQuote(List<IValue> params)
    {
        double price = 0.0;
        QueryProvider provider = _monitor.getQueryProvider();
        if (provider == null) {
            throw new RuntimeException("No Query Provider available");
        }

        price = provider.getStockPrice(symbol.valueAsString());
        return new DoubleVariable(null, price);
    }

}


