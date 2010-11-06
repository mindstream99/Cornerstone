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
import java.util.List;

import com.paxxis.chime.client.InstanceUpdateListener;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.portal.DataRowModel;

/**
 * A model containing the constituent parts of a data field value.  The model supports
 * two modes of operation.  The first mode keeps all values for a field, as a list, consistent
 * with the way data instances store field values.  The second mode is used to model a single
 * value from the list by including the index of the value in the overall list.
 * 
 * @author Robert Englander
 *
 */
public class DataFieldValueModel extends DataRowModel {
	public static final String EDIT = "edit";
	
	private static final long serialVersionUID = 1L;

	private DataInstance dataInstance;
	private Shape shape;
	private DataField dataField;
    private int index;
	private InstanceUpdateListener saveListener;
    
	public DataFieldValueModel(DataInstance inst, Shape shape, DataField field,
		    InstanceUpdateListener listener) {
		this(inst, shape, field, -1, listener);
	}
	
	public DataFieldValueModel(DataInstance inst, Shape shape,
		    InstanceUpdateListener listener) {
		this(inst, shape, null, -1, listener);
	}
	
	public DataFieldValueModel(DataInstance inst, Shape shape, DataField field,
		    int idx, InstanceUpdateListener listener) {
		super();
		this.shape = shape;
		dataField = field;
		index = idx;
		saveListener = listener;
		update(inst);
	}
	
	public InstanceUpdateListener getUpdateListener() {
		return saveListener;
	}
	
	public DataInstance getDataInstance() {
		return dataInstance;
	}
	
	public Shape getShape() {
		return shape;
	}
	
	public DataField getDataField() {
		return dataField;
	}

	public int getValueIndex() {
		return index;
	}
	
	@SuppressWarnings("unchecked")
	public Object set(String propertyName, Object value) {
		boolean remove = (value == null);
		
		if (!(propertyName.equals(DataFieldValueModel.NAME) ||
			propertyName.equals(DataFieldValueModel.VALUE))) {

			DataField field = shape.getField(propertyName);
			List<DataFieldValue> values = dataInstance.getFieldValues(shape, field);
			if (remove) {
				values.clear();
			} else {
				if (!values.isEmpty()) {
					if (value instanceof DataInstance) {
						DataInstance d = (DataInstance)value;
						values.get(0).setValue(d.getName());
						values.get(0).setId(d.getId());
					} else {
						values.get(0).setValue((Serializable)value);
					}
				} else {
					if (value instanceof DataInstance) {
						DataInstance d = (DataInstance)value;
						DataFieldValue newVal = new DataFieldValue(d.getId(), d.getName(), shape.getId(), InstanceId.UNKNOWN, null);
						values.add(newVal);
					} else {
						DataFieldValue newVal = new DataFieldValue((Serializable)value, shape.getId(), InstanceId.UNKNOWN, null);
						values.add(newVal);
					}
				}
			}
		}

		if (remove) {
			return super.remove(propertyName);
		} else {
			return super.set(propertyName, value);
		}
	}
	
	public void update(DataInstance inst) {
		dataInstance = inst;
		if (dataField != null) {
			List<DataFieldValue> values = dataInstance.getFieldValues(shape, dataField);
			set(VALUE, values);
	        set(NAME, "<b>" + dataField.getName() + ":</b>");
		} else {
			// if the data field is null, then each field of the specified shape represents the property name.
			// in this case, the actual data value is stored in the property named by the field.  this form of
			// the model is used only for an instance of a tabular shape, which means that there can never be more
			// than 1 value.
			List<DataField> fields = shape.getFields();
			set(VALUE, dataInstance);
			for (DataField field : fields) {
				List<DataFieldValue> values = dataInstance.getFieldValues(shape, field);
				//set(field.getName(), values);

				if (!values.isEmpty()) {
					if (!values.get(0).getReferenceId().equals(InstanceId.UNKNOWN)) {
						DataInstance t = new DataInstance();
						t.setName(values.get(0).getValue().toString());
						t.setId(values.get(0).getReferenceId());
						set(field.getName(), t);
					} else {
						set(field.getName(), values.get(0).getValue());
					}
				}
			}
		}
	}
	
	public boolean isEditable() {
        boolean canEdit = ((dataField.isUserEditable() && dataInstance.canUpdate(ServiceManager.getActiveUser())) ||
        		(!dataField.isUserEditable() && ServiceManager.getActiveUser().isAdmin()));

        return canEdit;
	}
	
}
