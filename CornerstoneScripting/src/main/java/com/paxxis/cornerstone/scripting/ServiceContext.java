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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.paxxis.cornerstone.base.Strategy;
import com.paxxis.cornerstone.scripting.extension.CSLExtension;
import com.paxxis.cornerstone.scripting.extension.CSLExtensionHelper;
import com.paxxis.cornerstone.scripting.extension.ExtensionHelper;
import com.paxxis.cornerstone.scripting.extension.ExtensionManager;
import com.paxxis.cornerstone.scripting.strategy.MacroExpansionStrategy;

/**
 * Provides service access toCSL objects, since CSL objects do not know anything about the
 * environment that they run in.
 * 
 * @author Robert Englander
 */
public final class ServiceContext {
    private static final Logger LOGGER = Logger.getLogger(ServiceContext.class);

    private boolean allowsWhileLoops = false;
    private ExtensionManager extensionManager = null;    
    private List<Strategy> strategies = new ArrayList<Strategy>();

    public ServiceContext() {

    }

    public void setStrategies(Collection<? extends Strategy> strategies) {
	this.strategies.addAll(strategies);
    }

    public void initialize() {
    }

    public void destroy() {

    }

    public ExtensionHelper createExtensionHelper(String extId) {
	// find the extension based on the id
	CSLExtension ext = extensionManager.getExtension(extId);
	CSLExtensionHelper helper =  new CSLExtensionHelper();
	helper.initialize(ext);
	return helper;
    }

    public void setExtensionManager(ExtensionManager mgr) {
	extensionManager = mgr;
    }

    public void setAllowsWhileLoops(boolean allows) {
	this.allowsWhileLoops = allows;
    }

    public boolean allowsWhileLoops() {
	return allowsWhileLoops;
    }

    public boolean supportsMacroExpansion() {
	MacroExpansionStrategy strategy = getStrategy(MacroExpansionStrategy.class);
	return strategy != null;
    }
    
    public String performMacroExpansion(String value) {
	MacroExpansionStrategy strategy = getStrategy(MacroExpansionStrategy.class);
	if (null != strategy) {
	    value = strategy.expand(value);
	}
	return value;
    }

    @SuppressWarnings("unchecked")
    public <T extends Strategy> T getStrategy(Class<T> clazz) {
	T result = null;
	for (Strategy strategy : strategies) {
	    if (clazz.isAssignableFrom(strategy.getClass())) {
		result = (T)strategy;
		break;
	    }
	}
	return result;
    }

}
