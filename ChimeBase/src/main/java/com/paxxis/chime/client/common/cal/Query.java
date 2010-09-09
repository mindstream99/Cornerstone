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
public class Query extends RuleVariable implements IRuleObject
{
    private static enum Methods
    {
        count,
        average
    }

    private IValue shapeName = new StringVariable(null, "");
    private List<QueryParameter> queryParameters = new ArrayList<QueryParameter>();

    public Query() {

    }

    public Query(String name)
    {
        super(name);
    }

    public String getType()
    {
        return "Query";
    }

    public void addParameter(QueryParameter parameter) {
        queryParameters.add(parameter);
    }

    public void resetValue()
    {
    }

    public boolean methodHasReturn(String name)
    {
        switch (Methods.valueOf(name))
        {
            case count:
                return true;
            case average:
                return true;
        }

        return false;
    }

    public boolean isObject()
    {
        return true;
    }

    public int getMethodParameterCount(String name)
    {
        switch (Methods.valueOf(name))
        {
            case count:
                return 0;
            case average:
                return 1;
        }

        return 0;
    }

    public IValue executeMethod(String name, List<IValue> params)
    {
        switch (Methods.valueOf(name))
        {
            case count:
                return executeCount(params);
            case average:
                return executeAverage(params);
        }

        return null;
    }

    private IValue executeAverage(List<IValue> params) {
        // the 1st parameter is the field name to get the data from
        // the field is a field of the data type
        String fieldName = params.get(0).valueAsString();

        QueryProvider provider = _monitor.getQueryProvider();
        if (provider == null) {
            throw new RuntimeException("No Query Provider available");
        }

        return new DoubleVariable(null, provider.getFieldDataAverage(shapeName.valueAsString(), fieldName, queryParameters));
    }

    private IValue executeCount(List<IValue> params)
    {
        // we don't use any parameters

        QueryProvider provider = _monitor.getQueryProvider();
        if (provider == null) {
            throw new RuntimeException("No Query Provider available");
        }

        return new IntegerVariable(null, provider.getCountByShape(shapeName.valueAsString(), queryParameters));
    }

    /**
     * Sets the value of the query itself.  Queries are assigned
     * the data type name to search for
     */
    public void setValue(IValue val, boolean trigger)
    {
        // if the value being assigned is an object expression,
        // then we need to evaluate it
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

        shapeName = value;

        // tell the monitor about this change
        _monitor.variableChange(this);
    }

    public Object valueAsObject()
    {
        return this;
    }

    public String valueAsString()
    {
        return shapeName.valueAsString();
    }

    public double valueAsDouble()
    {
        return 0.0;
    }

    public int valueAsInteger()
    {
        return 0;
    }

    public boolean valueAsBoolean()
    {
        return true;
    }

    public IValue evaluate()
    {
        // this is a no-op
        return this;
    }

}
