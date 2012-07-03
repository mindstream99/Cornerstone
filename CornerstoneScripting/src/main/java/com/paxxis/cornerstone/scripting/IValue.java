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

import java.io.Serializable;
import java.util.List;

/**
 * All values support rendering themselves as any of
 * the basic types, and also perform a set of standard operations.
 *
 * @author Robert Englander
 */
public abstract class IValue implements Serializable {
    private static final long serialVersionUID = 1L;

    public IValue() {
    }
    
    /**
     * Render as a string
     */
    public abstract String valueAsString();

    /**
     * Render as a double
     */
    public abstract Double valueAsDouble();

    /**
     * Render as an integer
     */
    public abstract Integer valueAsInteger();

    /**
     * Render as a boolean
     */
    public abstract Boolean valueAsBoolean();

    /**
     * Render as a Result
     */
    public abstract ResultVariable valueAsResult();

    /**
     * Render as an object
     */
    public abstract Object valueAsObject();

    /**
     * Some values are expressions and do not have
     * specific values until they are evaluated.
     */
    public abstract IValue evaluate();
    
    /**
     * Determine if the value is an array.
     */
    public abstract boolean isArray();

    /**
     * Determine if the value is a table.
     */
    public abstract boolean isTable();

    /**
     * Add this value's variables to the list.
     *
     * @param deps the list
     */
    public abstract void findVariables(List<RuleVariable> deps);
}
