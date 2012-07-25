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

import com.paxxis.cornerstone.scripting.extension.ExtensionHelper;


/**
 * This interface is implemented by services or applications that provide access to
 * some of their functionality to CSL objects, since CSL objects do not know anything about the
 * environment that they run in.
 * 
 * @author Robert Englander
 */
public interface ContextProvider {
    public ExtensionHelper createExtensionHelper(String extId);
    public boolean allowsWhileLoops();
    public boolean supportsMacroExpansion();
    public String performMacroExpansion(String value);
}
