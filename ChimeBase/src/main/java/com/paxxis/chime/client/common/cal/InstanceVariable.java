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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.cornerstone.base.InstanceId;

/**
 * A CAL wrapper around a DataInstance
 * 
 * @author Robert Englander
 */
public class InstanceVariable extends RuleVariable implements IRuleObject {

    private static enum Methods {
        getName,
        getId,
        getDescription,
        getShapes,
        getFieldValues
    }

    private DataInstance dataInstance = null;

    public InstanceVariable() {

    }

    public InstanceVariable(DataInstance inst) {
        dataInstance = inst;
    }
    
    public InstanceVariable(String name) {
        super(name);
    }

    @Override
    public String getType() {
        return "DataInstance";
    }

    public void initialize(InstanceVariable var)
    {
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
            case getShapes:
                return true;
            case getFieldValues:
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
            case getShapes:
                return 0;
            case getFieldValues:
                return 2;
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
            case getShapes:
                return executeGetShapes(params);
            case getFieldValues:
                return executeGetFieldValues(params);
        }

        return null;
    }

    private IValue executeGetFieldValues(List<IValue> params) {

        // the first paramater must be the shape
        IValue shapeVal = params.get(0);
        if (!(shapeVal instanceof ShapeVariable)) {
            throw new RuntimeException("First parameter to Instance:getFieldValues must be a Shape");
        }

        ShapeVariable shape = (ShapeVariable)shapeVal;

        // the second parameters is the field name
        String fieldName = params.get(1).valueAsString();

        Shape type = shape.getShape();
        DataField field = type.getField(fieldName);
        List<DataFieldValue> vals = dataInstance.getFieldValues(type, field);

        List<IValue> results = new ArrayList<IValue>();
        for (DataFieldValue val : vals) {
            if (val.isInternal()) {
            	Serializable ser = val.getValue();
            	IValue s;
            	if (ser instanceof Date) {
            		s = new DateVariable(null, (Date)ser);
            	} else {
                    s = new StringVariable(null, val.getValue().toString());
            	}
                results.add(s);
            } else {
                InstanceVariable inst = new InstanceVariable();
                inst.setMonitor(_monitor);
                inst.setValue(new StringVariable(null, val.getReferenceId().getValue()), true);
                results.add(inst);
            }
        }

        Array data = new Array();
        data.initialize(results);
        return data;
    }

    private IValue executeGetShapes(List<IValue> params) {
        Array shapes = new Array();
        List<IValue> vals = new ArrayList<IValue>();
        int i = 0;
        if (dataInstance != null) {
            for (Shape type : dataInstance.getShapes()) {
                ShapeVariable shape = new ShapeVariable();
                shape.setShape(type);
                vals.add(shape);
            }
        }

        shapes.initialize(vals);
        return shapes;
    }

    private IValue executeGetDescription(List<IValue> params) {
        String desc = "";
        if (dataInstance != null) {
            desc = dataInstance.getDescription();
        }

        return new StringVariable(null, desc);
    }

    private IValue executeGetName(List<IValue> params) {
        String name = "";
        if (dataInstance != null) {
            name = dataInstance.getName();
        }

        return new StringVariable(null, name);
    }

    private IValue executeGetId(List<IValue> params) {
        String id = "";
        if (dataInstance != null) {
            id = dataInstance.getId().getValue();
        }

        return new StringVariable(null, id);
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

        if (value instanceof InstanceVariable) {
            dataInstance = ((InstanceVariable)value).dataInstance;
        } else {
            InstanceId id = InstanceId.create(value.valueAsString());

            QueryProvider provider = _monitor.getQueryProvider();
            if (provider == null) {
                throw new RuntimeException("No Query Provider available");
            }

            dataInstance = provider.getDataInstanceById(id);
        }

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
        if (dataInstance != null) {
            name = dataInstance.getName();
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
        //InstanceVariable inst = new InstanceVariable(null);
        //inst.initialize(this);
        return this; //inst;
    }

}
