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

import com.paxxis.chime.client.common.Shape;
import com.paxxis.cornerstone.base.InstanceId;

import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class ShapeVariable extends RuleVariable implements IRuleObject {

    private static enum Methods {
        getName,
        getId,
        getDescription
    }

    private Shape shape = null;

    public ShapeVariable() {

    }

    public ShapeVariable(String name) {
        super(name);
    }

    @Override
    public String getType() {
        return "Shape";
    }

    public void initialize(ShapeVariable var)
    {

    }

    public Shape getShape() {
        return shape;
    }
    
    public boolean methodHasReturn(String name)
    {
        switch (Methods.valueOf(name))
        {
            case getName:
                return true;
            case getId:
                return true;
            case getDescription:
                return true;
        }

        return false;
    }

    @Override
    public boolean isObject()
    {
        return true;
    }

    public int getMethodParameterCount(String name)
    {
        switch (Methods.valueOf(name))
        {
            case getName:
            case getId:
            case getDescription:
                return 0;
        }

        return 0;
    }

    public IValue executeMethod(String name, List<IValue> params)
    {
        switch (Methods.valueOf(name))
        {
            case getName:
                return executeGetName(params);
            case getId:
                return executeGetId(params);
            case getDescription:
                return executeGetDescription(params);
        }

        return null;
    }

    private IValue executeGetName(List<IValue> params) {
        String name = "";
        if (shape != null) {
            name = shape.getName();
        }

        return new StringVariable(null, name);
    }

    private IValue executeGetId(List<IValue> params) {
        String id = "";
        if (shape != null) {
            id = shape.getId().getValue();
        }

        return new StringVariable(null, id);
    }

    private IValue executeGetDescription(List<IValue> params) {
        String id = "";
        if (shape != null) {
            id = shape.getDescription();
        }

        return new StringVariable(null, id);
    }

    public void setShape(Shape shape) {
        this.shape = shape;

        if (_monitor != null)
        {
            _monitor.variableChange(this);
        }
    }

    @Override
    public void setValue(IValue val, boolean trigger) {
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

        InstanceId id;
        if (value instanceof ShapeVariable) {
            id = ((ShapeVariable)value).getShape().getId();
        } else {
            id = InstanceId.create(value.valueAsString());
        }


        QueryProvider provider = _monitor.getQueryProvider();
        if (provider == null) {
            throw new RuntimeException("No Query Provider available");
        }

        shape = provider.getShapeById(id);

        if (_monitor != null)
        {
            _monitor.variableChange(this);
        }
    }

    @Override
    public void resetValue() {
    }

    @Override
    public String valueAsString() {
        String name = "";
        if (shape != null) {
            name = shape.getId().getValue();
        }

        return name;
    }

    @Override
    public double valueAsDouble() {
        return 0.0;
    }

    @Override
    public int valueAsInteger() {
        return 0;
    }

    @Override
    public boolean valueAsBoolean() {
        return true;
    }

    @Override
    public Object valueAsObject() {
        return this;
    }

    @Override
    public IValue evaluate() {
        //ShapeVariable inst = new ShapeVariable(null);
        //inst.initialize(this);
        return this; //inst;
    }

}
