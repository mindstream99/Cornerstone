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

package com.paxxis.chime.client.common.extension;

import com.paxxis.chime.client.common.Community;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.User;
import java.util.HashMap;

/**
 *
 * @author Robert Englander
 */
public abstract class ChimeExtension {

    private String id = null;
    private String name = null;
    private ExtensionContext context = null;
    private User user = null;
    private Community community = null;
    private String calClassName = null;
    private HashMap<String, String> propertyMap = new HashMap<String, String>();
    private HashMap<String, InstanceId> idMap = new HashMap<String, InstanceId>();
    private HashMap<String, HashMap<String, String>> fieldMap = new HashMap<String, HashMap<String, String>>();

    protected ChimeExtension() {
    }

    public abstract void initialize();
    public abstract DataInstance getDataInstance(InstanceId id);
    public abstract CALExtension getCalExtension();

    public void addMapping(String objectName, InstanceId shapeId, HashMap<String, String> fieldMap) {
        this.idMap.put(objectName, shapeId);
        this.fieldMap.put(objectName, fieldMap);
    }

    public InstanceId getShapeId(String objectName) {
        return idMap.get(objectName);
    }

    public String getShapeFieldName(String objectName, String fieldName) {
        return fieldMap.get(objectName).get(fieldName);
    }

    public void setPropertyMap(HashMap<String, String> map) {
        propertyMap.clear();
        propertyMap.putAll(map);
    }

    public HashMap<String, String> getPropertyMap() {
        return propertyMap;
    }

    public void setCalClassName(String name) {
        calClassName = name;
    }

    public String getCalClassName() {
        return calClassName;
    }

    public void setCommunity(Community community) {
        this.community = community;
    }

    public Community getCommunity() {
        return community;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setExtensionContext(ExtensionContext ctx) {
        context = ctx;
    }

    public ExtensionContext getExtensionContext() {
        return context;
    }

    public MemoryIndexer getMemoryIndexer() {
        return null;
    }
}
