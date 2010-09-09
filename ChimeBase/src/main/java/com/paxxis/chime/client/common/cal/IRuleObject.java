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
 * All Object types implement this interface.
 *
 * @author Robert Englander
 */
public interface IRuleObject
{
    /**
     * Determine if the method has a return value.
     *
     * @param name the method name.
     * @return true if the method returns a value, false otherwise.
     */
    public boolean methodHasReturn(String name);
    
    /**
     * Execute a method.
     *
     * @param name the method name.
     * @params a list of method parameters.
     * @return the return value, or null if the method has
     * no return value.
     */
    public IValue executeMethod(String name, List<IValue> params);
    
    /**
     * Find out how many parameters are required for a method.
     *
     * @param name the method name
     * @return the number of parameters required.
     */
    public int getMethodParameterCount(String name);
}
