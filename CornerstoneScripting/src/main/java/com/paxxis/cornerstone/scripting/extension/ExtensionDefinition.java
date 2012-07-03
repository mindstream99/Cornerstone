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

import java.util.HashMap;

/**
 * 
 * @author Rob Englander
 *
 */
public class ExtensionDefinition {

    private String id;
    private String name;
    private String className;
    private String cslClassName;
    private HashMap<String, Object> propertyMap = new HashMap<String, Object>();

    public ExtensionDefinition() {
    }

    public void setPropertyMap(HashMap<String, Object> map) {
        propertyMap.clear();
        propertyMap.putAll(map);
    }

    public HashMap<String, Object> getPropertyMap() {
        return propertyMap;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getCslClassName() {
        return cslClassName;
    }

    public void setCslClassName(String className) {
        this.cslClassName = className;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
