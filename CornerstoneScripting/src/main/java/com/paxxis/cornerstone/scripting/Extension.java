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

import java.util.List;

import com.paxxis.cornerstone.scripting.extension.ExtensionHelper;
import com.paxxis.cornerstone.scripting.parser.CSLRuntime;

/**
 *
 * @author Robert Englander
 */
public class Extension extends RuleVariable {
    private static final long serialVersionUID = 2L;
    private static MethodProvider<Extension> methodProvider = new MethodProvider<Extension>(Extension.class);
    static {
        methodProvider.initialize();
    }

    private ExtensionHelper helper = null;

    public Extension() {

    }

    public Extension(String name) {
        super(name);
    }

    @Override
    protected MethodProvider<Extension> getMethodProvider() {
        return methodProvider;
    }

    public boolean isValueNull() {
        return false;
    }

    @Override
    public String getType() {
        return "Extension";
    }

    public void initialize()
    {
    }

    public void setRuntime(CSLRuntime agent) {
        super.setRuntime(agent);
        init();
    }

    private void init() {
        ServiceContext provider = runtime.getServiceContext();
        if (provider == null) {
            throw new RuntimeException("No Service Context Provider available");
        }

        helper = runtime.getServiceContext().createExtensionHelper(getName());
        helper.initialize();

        if (runtime != null) {
            runtime.variableChange(this);
        }
    }

    public boolean methodHasReturn(String name) {
        return helper.methodHasReturn(name);
    }

    public int getMethodParameterCount(String name) {
        return helper.getMethodParameterCount(name);
    }

    public IValue executeMethod(String name, List<IValue> params) {
        return helper.executeMethod(name, params);
    }

    @Override
    protected void setValue(IValue val) {
        // if the value being assigned is an object expression,
        // then we need to evaluate it
        @SuppressWarnings("unused")
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
        return id;
    }

    @Override
    public Double valueAsDouble() {
        if (isValueNull()) {
            return null;
        }
        return 0.0;
    }

    @Override
    public Integer valueAsInteger() {
        if (isValueNull()) {
            return null;
        }
        return 0;
    }

    @Override
    public Boolean valueAsBoolean() {
        return !isValueNull();
    }

    @Override
    public ResultVariable valueAsResult() {
        ResultVariable res = new ResultVariable(null, valueAsBoolean());
        return res;
    }

    @Override
    public Object valueAsObject() {
        return this;
    }

    @Override
    public IValue evaluate() {
        return this;
    }

}
