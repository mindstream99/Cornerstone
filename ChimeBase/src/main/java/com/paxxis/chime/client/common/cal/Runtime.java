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


/**
 * The debug monitor is used to debug CAL rules.
 *
 * @author Robert Englander
 */
public class Runtime implements Serializable
{
    // the associated rule set
    private RuleSet _ruleSet;

    // a query provider
    transient public QueryProvider queryProvider = null;

    public Runtime()
    {
    }

    public void setQueryProvider(QueryProvider provider) {
        queryProvider = provider;
    }

    public QueryProvider getQueryProvider() {
        return queryProvider;
    }
    
    public void setRuleSet(RuleSet ruleSet)
    {
        _ruleSet = ruleSet;
    }
    
    /**
     * Tell the monitor that an instruction at a specific line
     * number is about to execute.
     */
    public void setPoised()
    {
        int line = _ruleSet.getCurrentInstruction().getLineNumber();
        int x = 1;
    }

    public void ruleBoundary(Rule rule, boolean starting)
    {
    }

    public void variableChange(RuleVariable var)
    {
        if (var.getName() != null && !var.getName().startsWith("#"))
        {
            //System.out.println(var.getName() + " = " + var.valueAsObject());
        }
    }

    public void push(InstructionQueue context)
    {
        String[] names = context.getVariableNames();
        int x = 1;
    }

    public void pop(InstructionQueue context)
    {
    }
}
