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
public class ExtensionShapeMapping {
    private String objectName = null;
    private List<String> nameConfig = new ArrayList<String>();
    private String shapeId = null;
    private HashMap<String, String> fieldMap = new HashMap<String, String>();

    public ExtensionShapeMapping() {
    }

    public HashMap<String, String> getFieldMap() {
        return fieldMap;
    }

    public void setFieldMap(HashMap<String, String> fieldMap) {
        this.fieldMap = fieldMap;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }
    
    public void setNameConfig(List<String> nameConfig) {
		this.nameConfig = nameConfig;
	}

	public List<String> getNameConfig() {
		return nameConfig;
	}

    public String getShapeId() {
        return shapeId;
    }

    public void setShapeId(String id) {
        this.shapeId = id;
    }
}
