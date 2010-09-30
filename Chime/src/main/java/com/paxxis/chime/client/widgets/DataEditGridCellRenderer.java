package com.paxxis.chime.client.widgets;

import java.util.List;

import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.paxxis.chime.client.InstanceUpdateListener;
import com.paxxis.chime.client.InstanceUpdateListener.Type;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.editor.FieldEditListener;
import com.paxxis.chime.client.editor.FieldEditorListener;
import com.paxxis.chime.client.editor.MultiReferenceEditorWindow;
import com.paxxis.chime.client.editor.MultiTextEditorWindow;
import com.paxxis.chime.client.editor.TextFieldEditorWindow;

public class DataEditGridCellRenderer implements GridCellRenderer<DataFieldModel> {
    private FieldEditListener fieldEditListener;
    private FieldEditorListener textListener;
    private DataFieldModel model = null;
    private InstanceUpdateListener saveListener = null;

    public DataEditGridCellRenderer() {
        textListener  = new FieldEditorListener() {
            public void onSave(DataField field, DataFieldValue value, String text) {
                sendEdit(field, value, text);
            }
        };

        fieldEditListener = new FieldEditListener() {
            public void onEdit(DataInstance instance, Shape type, DataField field) {
                sendEdit(instance, type, field);
            }
        };
    }
    
    private void sendEdit(DataField field, DataFieldValue value, String text) {
        DataInstance inst = model.getDataInstance();
        Shape shape = model.getShape();

        // if the value is null, then this is new data, otherwise this is modified data
        if (value != null) {
            if (field.getShape().isPrimitive()) {
                value.setName(text);
            } else {
                value.setReferenceId(InstanceId.create(text));
            }
        } else {
            if (field.getShape().isPrimitive()) {
                value = new DataFieldValue(text, field.getShape().getId(), InstanceId.UNKNOWN, null);
            } else {
                value = new DataFieldValue(InstanceId.create(text), text, 
                		field.getShape().getId(), InstanceId.UNKNOWN, null);
            }
            List<DataFieldValue> list = inst.getFieldValues(shape, field);
         
            list.add(value);
        }
        
        saveListener.onUpdate(inst, Type.FieldData);
    }

    private void sendEdit(DataInstance instance, Shape type, DataField field) {
        saveListener.onUpdate(instance, Type.FieldData);
    }

    @Override
	public Object render(final DataFieldModel model, String property,
			ColumnData config, int rowIndex, int colIndex,
			ListStore<DataFieldModel> store, Grid<DataFieldModel> grid) {

    	this.model = model;
    	saveListener = model.getUpdateListener();
        final ToolButton btn = new ToolButton("x-tool-save");
        boolean canEdit = model.isEditable();
        btn.setVisible(canEdit);
        
        if (canEdit) {
            btn.addSelectionListener(
                    new SelectionListener<IconButtonEvent>() {
                @Override
                 public void componentSelected(IconButtonEvent ce) {
                    DataField field = model.getDataField();
                    Shape shape = model.getShape();
                    DataInstance inst = model.getDataInstance();
                    if (!field.getShape().isPrimitive()) {
                        MultiReferenceEditorWindow w = new MultiReferenceEditorWindow(inst, shape, field, fieldEditListener);
                        w.show();
                    } else {
                        String typeName = field.getShape().getName();
                        if (typeName.equals("Rich Text")) {
                            List<DataFieldValue> list = inst.getFieldValues(shape, field);
                            DataFieldValue val = null;
                            if (list.size() == 1) {
                                val = list.get(0);
                            }

                            TextFieldEditorWindow editor = new TextFieldEditorWindow(shape, field, val, textListener);
                            editor.show();
                        } else { // if (typeName.equals("Text")) {
                            MultiTextEditorWindow w = new MultiTextEditorWindow(inst, shape, field, fieldEditListener);

                            int x = btn.getAbsoluteLeft();
                            int y = btn.getAbsoluteTop() + btn.getOffsetHeight() + 1;
                            w.show(); //At(x, y);
                        }
                    }
                 }
            });
        }
        
        LayoutContainer lc = new LayoutContainer();
        lc.setLayout(new RowLayout());
        lc.add(btn, new RowData(1, -1, new Margins(3, 0, 0, 0)));
		return lc;
	}
}