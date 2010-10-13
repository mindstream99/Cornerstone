package com.paxxis.chime.client.widgets;

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
        StringBuffer buffer = new StringBuffer();
        List<DataFieldValue> values = dataInstance.getFieldValues(shape, dataField);
        String stringContent = "";
        String sep = "";
        for (DataFieldValue value : values) {
            String valueName = value.getValue().toString().trim();
            boolean isInternal = dataField.getShape().isPrimitive();

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
                    sep = "   ";
                }
                else if (dataField.getShape().isNumeric())
                {
                    // TODO when formatting is added to the field definition, we'll
                    // apply whatever is specified
                    Double dval = Double.valueOf(value.getValue().toString());
                    NumberFormat fmt = NumberFormat.getDecimalFormat();
                    String formatted = fmt.format(dval);

                    buffer.append(sep + formatted);
                    sep = "   ";
                }
                else if (dataField.getShape().isDate())
                {
                    Date dval = (Date)value.getValue();
                    DateTimeFormat dtf = DateTimeFormat.getFormat("MMM d, yyyy");
                    String formatted = dtf.format(dval);
                    buffer.append(sep + formatted);
                    sep = "   ";
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
                sep = "   ";
            }
        }
        
        set(DataRowModel.NAME, "<b>" + dataField.getName() + ":</b>");
        set(DataRowModel.VALUE, stringContent);
    }
}
