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

import com.paxxis.chime.client.common.extension.CALExtension;
import com.paxxis.chime.client.common.extension.ChimeExtension;
import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class Extension extends RuleVariable implements IRuleObject {

    private static enum Methods {
        getName,
        getId,
        getDescription,
        getShapes,
        getFieldValues
    }

    private ExtensionHelper helper = null;
    private ChimeExtension chimeExtension = null;
    private CALExtension calExtension = null;

    public Extension() {

    }

    public Extension(String name) {
        super(name);
    }

    @Override
    public String getType() {
        return "Extension";
    }

    public void initialize(InstanceVariable var)
    {
    }

    public void setMonitor(Runtime agent) {
        super.setMonitor(agent);
        init();
    }
    
    private void init() {

        String id = getName();

        QueryProvider provider = _monitor.getQueryProvider();
        if (provider == null) {
            throw new RuntimeException("No Query Provider available");
        }

        chimeExtension = provider.getExtension(id);
        helper = _monitor.getQueryProvider().createExtensionHelper();
        calExtension = chimeExtension.getCalExtension();

        // get the java methods for the cal extension
        helper.initialize(chimeExtension, calExtension);

        if (_monitor != null)
        {
            _monitor.variableChange(this);
        }
    }

    public boolean methodHasReturn(String name)
    {
        return helper.methodHasReturn(name);
    }

    @Override
    public boolean isObject()
    {
        return true;
    }

    public int getMethodParameterCount(String name)
    {
        return helper.getMethodParameterCount(name);
    }

    public IValue executeMethod(String name, List<IValue> params) {
        return helper.executeMethod(name, params);
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
    }

    @Override
    public void resetValue() {
    }

    @Override
    public String valueAsString() {
        String id = "";
        if (chimeExtension != null) {
            id = chimeExtension.getId();
        }

        return id;
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
        return this; //inst;
    }

}
