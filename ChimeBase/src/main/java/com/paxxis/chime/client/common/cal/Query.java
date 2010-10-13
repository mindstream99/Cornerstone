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

import com.paxxis.chime.client.common.DataInstance;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class Query extends RuleVariable implements IRuleObject
{
    private static enum Methods {
        fetch,
        addFilter,
        addShapeFilter
    }

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
            case fetch:
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
            case fetch:
                return 0;
            case addFilter:
                return 2;
            case addShapeFilter:
                return 3;
        }

        return 0;
    }

    public IValue executeMethod(String name, List<IValue> params)
    {
        switch (Methods.valueOf(name))
        {
            case fetch:
                return executeFetch(params);
            case addFilter:
                return executeAddFilter(params);
            case addShapeFilter:
                return executeAddShapeFilter(params);
        }

        return null;
    }

    private IValue executeAddFilter(List<IValue> params) {


        return new BooleanVariable(null, true);
    }

    private IValue executeAddShapeFilter(List<IValue> params) {


        return new BooleanVariable(null, true);

    }

    private IValue executeFetch(List<IValue> params)
    {
        // we don't use any parameters

        QueryProvider provider = _monitor.getQueryProvider();
        if (provider == null) {
            throw new RuntimeException("No Query Provider available");
        }

        Array result = new Array();
        List<DataInstance> instances = provider.getDataInstances(queryParameters);
        List<IValue> ivals = new ArrayList<IValue>();
        for (DataInstance instance : instances) {
            ivals.add(new InstanceVariable(instance));
        }
        result.initialize(ivals);
        return result;
    }

    /**
     * Sets the value of the query itself.  Queries are assigned
     * the data type name to search for
     */
    public void setValue(IValue val, boolean trigger)
    {
    }

    public Object valueAsObject()
    {
        return this;
    }

    public String valueAsString()
    {
        return "";
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
