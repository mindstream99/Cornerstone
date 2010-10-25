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
import java.util.Date;
import java.util.List;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.paxxis.chime.client.InstanceUpdateListener;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.Utils;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.portal.DataRowModel;

/**
 * 
 * @author Robert Englander
 *
 */
public class DataFieldModel extends DataRowModel {
	public static final String EDIT = "edit";
 
	private static final long serialVersionUID = 1L;

	private DataInstance dataInstance;
	private Shape shape;
	private DataField dataField;
    private InstanceUpdateListener saveListener;

	public DataFieldModel(DataInstance inst, Shape shape, DataField field,
		    InstanceUpdateListener listener) {
		super();
		dataInstance = inst;
		this.shape = shape;
		dataField = field;
		saveListener = listener;
		generateContent();
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

	public void update(DataInstance inst) {
		dataInstance = inst;
		generateContent();
	}
	
	public boolean isEditable() {
        boolean canEdit = ((dataField.isUserEditable() && dataInstance.canUpdate(ServiceManager.getActiveUser())) ||
        		(!dataField.isUserEditable() && ServiceManager.getActiveUser().isAdmin()));

        return canEdit;
	}
	
    private void generateContent() {
    	if (dataField.getShape().isTabular()) {
    		generateTabularContent();
    	} else {
    		generateNonTabularContent();
    	}
    }
    
    private void generateTabularContent() {
        List<DataFieldValue> values = dataInstance.getFieldValues(shape, dataField);
        TabularFieldData tabData = new TabularFieldData(dataField.getShape());
        
        for (DataFieldValue value : values) {
        	Serializable ser = value.getValue();
        	if (ser instanceof DataInstance) {
        		tabData.add((DataInstance)ser);
        	}
        }

        set(DataRowModel.NAME, "<b>" + dataField.getName() + ":</b>");
        set(DataRowModel.VALUE, tabData);
    }
    
    private void generateNonTabularContent() {
    	StringBuffer buffer = new StringBuffer();
        List<DataFieldValue> values = dataInstance.getFieldValues(shape, dataField);
        String stringContent = "";
        String sep = "";
        boolean isInternal = dataField.getShape().isPrimitive();
        for (DataFieldValue value : values) {
            String valueName = value.getValue().toString().trim();
            boolean isImageReference = false;

            if (isInternal) {
                if (shape.getId().equals(Shape.IMAGE_ID) &&
                        dataField.getId().equals(Shape.FILE_ID)) {
                    isImageReference = true;
                    stringContent = sep + value.getValue();
                }
                else if (dataField.getShape().getId().equals(Shape.URL_ID))
                {
                	String vname = valueName.replaceAll(" ", "&nbsp;");
                    String name = Utils.toExternalUrl(valueName, vname);
                    buffer.append(sep + name);
                    sep = "    ";
                }
                else if (dataField.getShape().isNumeric())
                {
                    // TODO when formatting is added to the field definition, we'll
                    // apply whatever is specified
                    Double dval = Double.valueOf(value.getValue().toString());
                    NumberFormat fmt = NumberFormat.getDecimalFormat();
                    String formatted = fmt.format(dval);

                    buffer.append(sep + formatted);
                    sep = "    ";
                }
                else if (dataField.getShape().isDate())
                {
                    Date dval = (Date)value.getValue();
                    DateTimeFormat dtf = DateTimeFormat.getFormat("MMM d, yyyy");
                    String formatted = dtf.format(dval);
                    buffer.append(sep + formatted);
                    sep = "    ";
                }
                else
                {
                    buffer.append(sep + value.getValue());
                    sep = "<br>";
                }

                if (!isImageReference) {
                    String temp = buffer.toString();
                    temp = temp.replaceAll("<ul", "<ul class='forceul'");
                    temp = temp.replaceAll("<ol", "<ol class='forceol'");
                    stringContent = temp;
                }
            } else {
            	String vname = valueName.replaceAll(" ", "&nbsp;");
                String name = Utils.toHoverUrl(value.getReferenceId(), vname);
                buffer.append(sep + name);
                stringContent = buffer.toString();
                sep = "    ";
            }
        }
        
        set(DataRowModel.NAME, "<b>" + dataField.getName() + ":</b>");
        set(DataRowModel.VALUE, stringContent);
    }
}
