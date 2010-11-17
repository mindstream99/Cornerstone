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

import java.io.Serializable;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.Shape;

/**
 *
 * @author Robert Englander
 */
public class DataFieldModel  extends BaseTreeModel implements Serializable {
	public static final String NAME = "Name";
	public static final String DESCRIPTION = "Description";
	public static final String SHAPE = "Shape";
	public static final String MAXVALUES = "Max Values";
	public static final String FORMAT = "Format";
	
	private static final long serialVersionUID = 2L;
	private DataField dataField;
	private DataField subField;
    
    public DataFieldModel() {
    }
    
    public DataFieldModel(DataField dataField) {
    	this(dataField, null);
    }
    
	@SuppressWarnings("unchecked")
	public Object set(String propertyName, Object value) {
		boolean remove = (value == null);
		
		if (NAME.equals(propertyName)) {
			dataField.setName((String)value);
		} else if (DESCRIPTION.equals(propertyName)) {
			dataField.setDescription((String)value);
		} else if (SHAPE.equals(propertyName)) {
			dataField.setShape((Shape)value);
		} else if (MAXVALUES.equals(propertyName)) {
			dataField.setMaxValues((int)Double.parseDouble(value.toString()));
		} else if (FORMAT.equals(propertyName)) {
			dataField.setFormat((String)value); 
		}

		if (remove) {
			return super.remove(propertyName);
		} else {
			return super.set(propertyName, value);
		}
	}
    
    public DataFieldModel(DataField field, DataField subField) {
        this.dataField = field;
        this.subField = subField;
        String name = dataField.getName();
        if (subField != null) {
        	name += " : " + subField.getName();
        }
        
        super.set(NAME, name);
        super.set(DESCRIPTION, field.getDescription());
        super.set(SHAPE, field.getShape());
        super.set(MAXVALUES, field.getMaxValues());
        super.set(FORMAT, field.getFormat());
    }

    public DataField getDataField() {
        return dataField;
    }

    public DataField getSubField() {
        return subField;
    }
}
