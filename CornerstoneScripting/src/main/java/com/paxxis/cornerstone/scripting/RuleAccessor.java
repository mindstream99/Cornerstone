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


/**
 * The RuleAccessor is used to invoke another rule.
 *
 * @author Robert Englander
 */
public class RuleAccessor extends RuleVariable {
    private static final long serialVersionUID = 2L;
    private static MethodProvider<RuleAccessor> methodProvider = new MethodProvider<RuleAccessor>(RuleAccessor.class);
    static {
        methodProvider.initialize();
    }

    // the name of the rule to invoke
    private StringVariable ruleName = new StringVariable(null, "");

    // the queue we're on
    private InstructionQueue instructionQueue;

    public RuleAccessor() {

    }

    public RuleAccessor(String name, InstructionQueue queue) {
        super(name);
        instructionQueue = queue;
    }

    @Override
    protected MethodProvider<RuleAccessor> getMethodProvider() {
        return methodProvider;
    }

    public boolean isValueNull() {
        return false;
    }

    public void resetValue() {
    }

    public String getType() {
        return "Rule Accessor";
    }

    /**
     * Sets the value; in this case it's the rule name.
     * @param val
     */
    protected void setValue(IValue val) {
        setRuleName(new StringVariable(null, val.valueAsString()));

        if (runtime != null) {
            runtime.variableChange(this);
        }
    }

    public String valueAsString() {
        return getRuleName().valueAsString();
    }

    public Double valueAsDouble() {
        return getRuleName().valueAsDouble();
    }

    public Integer valueAsInteger() {
        return getRuleName().valueAsInteger();
    }

    public Boolean valueAsBoolean() {
        return getRuleName().valueAsBoolean();
    }

    @Override
    public ResultVariable valueAsResult() {
        ResultVariable res = new ResultVariable(null, valueAsBoolean());
        return res;
    }

    public Object valueAsObject() {
        return getRuleName().valueAsObject();
    }

    public IValue evaluate() {
        return this;
    }

    public IValue valueOf(List<IValue> params) {
        Rule rule = instructionQueue.getRuleSet().getRule(getRuleName().valueAsString());
        RuleVariable rv = rule.getQueue().getVariable(params.get(0).valueAsString());
        return rv;
    }

    public IValue invoke(List<IValue> params) {
        Rule rule = instructionQueue.getRuleSet().getRule(getRuleName().valueAsString());
        rule.invoke(params);

        IValue result = null;
        if (rule.hasReturnValue()) {
            result = rule.getReturnValue();
        }
        return result;
    }

    public StringVariable getRuleName() {
        return ruleName;
    }

    public void setRuleName(StringVariable ruleName) {
        this.ruleName = ruleName;
    }

    public boolean methodHasReturn(String name) {
        if (name.equals("invoke")) {
            Rule rule = instructionQueue.getRuleSet().getRule(getRuleName().valueAsString());
            return rule.hasReturnValue();
        } else {
            return true;
        }
    }

    public int getMethodParameterCount(String name) {
        if (name.equals("invoke")) {
            return -1;
        } else {
            return 1;
        }
    }

    public IValue executeMethod(String name, List<IValue> params) {
        if (name.equals("invoke")) {
            return invoke(params);
        } else {
            return valueOf(params);
        }
    }}
