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

package com.paxxis.chime.client;

import com.paxxis.chime.client.common.DataField;
import com.extjs.gxt.ui.client.data.BaseTreeModel;
import java.io.Serializable;

/**
 *
 * @author Robert Englander
 */
public class DataFieldModel  extends BaseTreeModel implements Serializable {
	private static final long serialVersionUID = 2L;
	private DataField dataField;
	private DataField subField;
    
    public DataFieldModel() {
    }
    
    public DataFieldModel(DataField dataField) {
    	this(dataField, null);
    }
    
    public DataFieldModel(DataField field, DataField subField) {
        this.dataField = field;
        this.subField = subField;
        String name = dataField.getName();
        if (subField != null) {
        	name += " : " + subField.getName();
        }
        
        set("name", name);
    }

    public DataField getDataField() {
        return dataField;
    }

    public DataField getSubField() {
        return subField;
    }
}
