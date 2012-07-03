/* Copyright 2010 the original author or authors.
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
package com.paxxis.cornerstone.scripting.extension;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


/**
 * 
 * @author Rob Englander
 *
 */
public class ExtensionManager {
    private static final Logger logger = Logger.getLogger(ExtensionManager.class);

    private List<ExtensionDefinition> definitions = new ArrayList<ExtensionDefinition>();
    private List<CSLExtension> extensions = new ArrayList<CSLExtension>();
    
    public ExtensionManager() {
    }

    public List<CSLExtension> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<ExtensionDefinition> defs) {
        definitions.addAll(defs);
    }

    public void initialize() {
        for (ExtensionDefinition def : definitions) {
            try {
                Class<?> clazz = Class.forName(def.getClassName());
                CSLExtension ext = (CSLExtension) clazz.newInstance();
                ext.setExtensionManager(this);
                ext.setId(def.getId());
                ext.setName(def.getName());
                ext.setClassName(def.getClassName());
                ext.setCSLClassName(def.getCslClassName());
                ext.setPropertyMap(def.getPropertyMap());

                ext.initialize();
                extensions.add(ext);
            } catch (Exception ex) {
                logger.error(ex);
            }
        }
    }

    public void destroy() {
    	for (CSLExtension ext : extensions) {
    	    ext.destroy();
    	}
    }
    
    public <E extends CSLExtension> E getExtension(Class<E> extClass) {
	E extension = null;
	for (CSLExtension ext : getExtensions()) {
	    if (ext.getClass().equals(extClass)) {
		extension = (E)ext;
	    }
	}
		
	return extension;
    }

    public CSLExtension getExtension(String extId) {
	CSLExtension ext = null;
	for (CSLExtension e : extensions) {
	    if (e.getId().equals(extId)) {
		ext = e;
		break;
	    }
	}
	return ext;
    }
    
}



