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

package com.paxxis.chime.client.widgets;

import java.io.Serializable;

import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.portal.DataRowModel;

/**
 * 
 * @author Rob Englander
 *
 */
public class FieldValueModel extends DataRowModel {
	public static final String EDIT = "edit";
	
	private static final long serialVersionUID = 1L;

	private Shape shape;
	private DataField dataField;
    
	public FieldValueModel(Shape shape, DataField field, Serializable value) {
		super();
		this.shape = shape;
		dataField = field;
		update(value);
	}

	public Shape getShape() {
		return shape;
	}

	public DataField getDataField() {
		return dataField;
	}
	
	public Serializable getValue() {
		return get(VALUE);
	}
	
	@SuppressWarnings("unchecked")
	public Object set(String propertyName, Object value) {
		boolean remove = (value == null);
		
		if (remove) {
			return super.remove(propertyName);
		} else {
			return super.set(propertyName, value);
		}
	}

	public void update(Serializable val) {
		set(VALUE, val);
        set(NAME, "");
	}
}
