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

package com.paxxis.chime.extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class ExtensionDefinition {

    public String id;
    public String name;
    public String className;
    public String calClassName;
    public String userName;
    public String communityId;
    public List<ExtensionShapeMapping> shapeMapList = new ArrayList<ExtensionShapeMapping>();
    private HashMap<String, String> propertyMap = new HashMap<String, String>();

    public ExtensionDefinition() {
    }

    public void setPropertyMap(HashMap<String, String> map) {
        propertyMap.clear();
        propertyMap.putAll(map);
    }

    public HashMap<String, String> getPropertyMap() {
        return propertyMap;
    }

    public List<ExtensionShapeMapping> getShapeMapList() {
        return shapeMapList;
    }

    public void setShapeMappings(List<ExtensionShapeMapping> list) {
        shapeMapList.clear();
        shapeMapList.addAll(list);
    }

    public String getCommunityId() {
        return communityId;
    }

    public void setCommunityId(String id) {
        this.communityId = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getCalClassName() {
        return calClassName;
    }

    public void setCalClassName(String className) {
        this.calClassName = className;
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
