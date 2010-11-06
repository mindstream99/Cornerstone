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

package com.paxxis.chime.client.editor;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.RowEditor;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid.ClicksToEdit;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.paxxis.chime.client.DataInputListener;
import com.paxxis.chime.client.DataInstanceComboBox;
import com.paxxis.chime.client.LoginResponseObject;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceManagerAdapter;
import com.paxxis.chime.client.ServiceManagerListener;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.widgets.ChimeGrid;
import com.paxxis.chime.client.widgets.ChimeWindow;
import com.paxxis.chime.client.widgets.DataDeleteGridCellRenderer;
import com.paxxis.chime.client.widgets.DataFieldValueModel;
import com.paxxis.chime.client.widgets.FieldDataGridCellRenderer;

/**
 * 
 * @author Robert Englander
 *
 */
public class TabularDataEditor extends ChimeWindow {

    private Html errorLabel;

    private ButtonBar saveCancelButtonBar;
    private Button _okButton;
    private Button _cancelButton;

    private ButtonBar addRestoreButtonBar;
    private Button addButton;
    private Button restoreButton;
    private Button createButton = null;

    private ChimeGrid<DataFieldValueModel> fieldGrid;
    private ListStore<DataFieldValueModel> listStore = new ListStore<DataFieldValueModel>();
    private FormPanel _form = new FormPanel();
    private ServiceManagerListener _serviceManagerListener = null;

    private DataInstance dataInstance;
    private Shape dataType;
    private DataField dataField;

    private FieldEditListener editListener = null;
    private AppliedTypesEditListener typesListener = null;

    public TabularDataEditor(DataInstance instance, Shape type,
                                DataField field, FieldEditListener listener) {
        super();

        editListener = listener;
        dataInstance = instance;
        dataType = type;
        dataField = field;
    }

    protected void onRender(Element parent, int index) { 
    	super.onRender(parent, index);
        setup();
    }
    
    private void setup() {

        listStore.removeAll();

        DeferredCommand.addCommand(
            new Command() {
                public void execute() {
                    boolean newOne = true;

                    for (DataFieldValue val : dataInstance.getFieldValues(dataType, dataField)) {
                        addValue(val);
                        newOne = false;
                    }

                    if (newOne) {
                        addValue((DataFieldValue)null);
                    }

                    validate();
                }
            }
        );
    }

    protected void init() {
        setModal(true);
        setHeading("Edit " + dataField.getName() + " [" + dataType.getName() + "]");

        setMaximizable(false);
        setMinimizable(false);
        setCollapsible(false);
        setClosable(true);
        setResizable(false);
        setWidth(600);

        _form.setHeaderVisible(false);
        _form.setBorders(false);
        _form.setBodyBorder(false);
        _form.setStyleAttribute("padding", "5");
        _form.setButtonAlign(HorizontalAlignment.CENTER);
        _form.setFrame(true);
        _form.setFieldWidth(450);
        _form.setLabelWidth(85);
        _form.setHideLabels(true);

        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        ColumnConfig column = new ColumnConfig();
        column.setId("delete");
        column.setFixed(true);
        column.setHeader("");
        column.setWidth(35);
        column.setSortable(false);
        column.setMenuDisabled(true);
        column.setRenderer(new DataDeleteGridCellRenderer());
        configs.add(column);

        Shape shape = dataField.getShape();
        //String lastColId = "";
        List<DataField> dataFields = shape.getFields();
        for (DataField field : dataFields) {
	        column = new ColumnConfig();
	        column.setId(field.getName());
	        column.setFixed(false);
	        column.setHeader(field.getName());
	        column.setWidth(150);
	        column.setSortable(false);
	        column.setMenuDisabled(true);
	        column.setEditor(getCellEditor(field));
	        column.setRenderer(new FieldDataGridCellRenderer(new Margins(1), false));
	        configs.add(column);
	        //lastColId = field.getName();
        }

        ColumnModel cm = new ColumnModel(configs);
        fieldGrid = new ChimeGrid<DataFieldValueModel>(listStore, cm, false, true);
        fieldGrid.getView().setAutoFill(false);
        GridSelectionModel<DataFieldValueModel> sm = new GridSelectionModel<DataFieldValueModel>();
        sm.setSelectionMode(SelectionMode.SINGLE);
        fieldGrid.setSelectionModel(sm);
        fieldGrid.getView().setForceFit(false);
        fieldGrid.getView().setShowDirtyCells(false);
        fieldGrid.setHideHeaders(false);
        fieldGrid.setTrackMouseOver(false);
        fieldGrid.setStripeRows(false);
        //fieldGrid.setAutoExpandColumn(lastColId);
        fieldGrid.setBorders(true);
        fieldGrid.setHeight(300);

        // add the row editor plugin
        RowEditor<DataFieldValueModel> editor = new RowEditor<DataFieldValueModel>();
        editor.setClicksToEdit(ClicksToEdit.TWO);
        fieldGrid.addPlugin(editor);

        _form.add(fieldGrid);
        
        addRestoreButtonBar = new ButtonBar();
        addRestoreButtonBar.setAlignment(HorizontalAlignment.LEFT);

        addButton = new Button("Add");
        addButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                    addValue((DataFieldValue)null);
                }
            }
        );

        addRestoreButtonBar.add(addButton);

    	boolean show = true;
    	if (dataField != null && !dataField.getShape().isDirectCreatable()) {
    		show = false;
    	}
    	
    	if (show) {
            createButton = new Button("Create...");
            createButton.addSelectionListener(
                new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent evt) {
                        createNewInstance();
                    }
                }
            );

            addRestoreButtonBar.add(createButton);
    	}

        restoreButton = new Button("Restore");
        restoreButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                    setup();
                }
            }
        );

        addRestoreButtonBar.add(new FillToolItem());
        addRestoreButtonBar.add(restoreButton);

        _form.add(addRestoreButtonBar);

        LayoutContainer c = new LayoutContainer();
        c.setHeight(10);
        _form.add(c);

        errorLabel = new Html("<div id='endslice-error-label'>&nbsp;</div>");
        _form.add(errorLabel);

        c = new LayoutContainer();
        c.setHeight(10);
        _form.add(c);

        saveCancelButtonBar = new ButtonBar();
        saveCancelButtonBar.setAlignment(HorizontalAlignment.CENTER);

        _okButton = new Button("Save");
        _okButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                    notifyListener();
                }
            }
        );

        saveCancelButtonBar.add(_okButton);

        _cancelButton = new Button("Cancel");
        _cancelButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                    doCancel();
                }
            }
        );

        saveCancelButtonBar.add(_cancelButton);

        _form.add(saveCancelButtonBar);
        add(_form);

        _serviceManagerListener = new ServiceManagerAdapter() {
            public void onLoginResponse(LoginResponseObject resp) {
            }
        };

        ServiceManager.addListener(_serviceManagerListener);
    }

    private CellEditor getCellEditor(final DataField field) {
    	CellEditor editor = null;
    	
    	Shape shape = field.getShape();
    	if (shape.isPrimitive()) {
    		if (shape.isNumeric()) {
    			NumberField f = new NumberField();
    			f.setAutoValidate(true);
    	    	editor = new CellEditor(f);
                f.setAutoWidth(true);
    		} else if (shape.isDate()) {
    			DateField df = new DateField();
    			df.setAutoValidate(true);
                df.setAutoWidth(true);
    			editor = new CellEditor(df);
    		} else {
    			TextField<String> f = new TextField<String>();
                f.setAutoWidth(true);
    	    	editor = new CellEditor(f);
    		}
    	} else {
    		EditorDataInputListener listener = new EditorDataInputListener() {
            	
                @Override
                public void onDataInstance(DataInstance instance) {
                	if (instance == null) {
                    	editor.setValue(null);
                	} else {
                    	editor.setValue(instance.getName());
                    	editor.setData("DataInstance", instance);
                	}
                }

                @Override
                public void onStringData(String text) {
                	editor.setValue(null);
                }
                
            };

            final DataInstanceComboBox combo = new DataInstanceComboBox(listener, shape, true, false, true);
            AdapterField af = new AdapterField(combo);
            af.setResizeWidget(true);

            editor = new CellEditor(af) {  
                @Override  
                public Object preProcessValue(Object value) {  
                    if (value == null) {  
                        return value;  
                    }  

                    final String val;
                    if (value instanceof DataInstance) {
                        DataInstance instance = (DataInstance)value;
                        val = instance.getName();
                    } else {
                    	DataFieldValue dfVal = (DataFieldValue)value;
                    	val = dfVal.toString();
                    }
                    
                    DeferredCommand.addCommand(
                    	new Command() {
                    		public void execute() {
                                DataInstanceComboBox combo = (DataInstanceComboBox)((AdapterField)getField()).getWidget();
                                combo.getStore().removeAll();
                                combo.applyInput(val);
                    		}
                    	}
                    );
                    
                    return val;
                }  
            
                @Override  
                public Object postProcessValue(Object value) {  
                    if (value == null) {  
                        return value;  
                    }  

                    DataInstance instance = getData("DataInstance");
                    DataFieldValue val = new DataFieldValue(instance.getId(), instance.getName(), instance.getShapes().get(0).getId(),
                    							InstanceId.UNKNOWN, null);
                    return val;
                }  
            };
            
            listener.setEditor(editor);
        }
    	
    	return editor;
    }
    
    private void createNewInstance() {
        InstanceCreatorWindow w = new InstanceCreatorWindow(dataField.getShape(),
            new InstanceCreatorWindow.InstanceCreationListener() {
                public void onInstanceCreated(DataInstance instance) {
                    DataFieldValue val = new DataFieldValue();
                    val.setValue(instance.getName());
                    val.setShapeId(dataField.getShape().getId());
                    val.setReferenceId(instance.getId());
                    addValue(val);
                }
            }
        );

        w.show();
    }

    private void addValue(DataFieldValue value) {
    	if (value == null) {
            DataInstance inst = new DataInstance();
            inst.setName("XTabularRowData");
            inst.addShape(dataField.getShape());
            DataFieldValueModel model = new DataFieldValueModel(inst, dataField.getShape(), null);
            listStore.add(model);
    	} else {
            DataFieldValueModel model = new DataFieldValueModel((DataInstance)value.getValue(), dataField.getShape(), null);
            listStore.add(model);
    	}

        validate();
    }

    protected void handleLoginResponse(final LoginResponseObject resp)
    {
        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                }
            }
        );
    }

    protected void doCancel()
    {
        ServiceManager.removeListener(_serviceManagerListener);
        hide();
    }

    private void notifyListener() {
        List<DataFieldValue> values = dataInstance.getFieldValues(dataType, dataField);
        
        // existing tabular field data is always cleared by the service and replaced
        // with the new data.  So clear the values and build new values from the listStore.
        values.clear();

        List<DataFieldValueModel> models = listStore.getModels();
        for (DataFieldValueModel model : models) {
        	DataInstance inst = model.getDataInstance();
        	dataInstance.appendFieldValue(dataType, dataField, inst, false);
        }

        if (typesListener != null) {
            typesListener.onEdit(dataInstance);
        } else {
            editListener.onEdit(dataInstance, dataType, dataField);
        }

        hide();
    }

    /**
     * Check that the new list of items meets the following criteria:
     *
     * 1) No duplicates
     * 2) No empty references
     *
     * If the criteria isn't met, an appropriate error message is provided.
     *
     * Compares the new list with the existing list to determine if any changes
     * were made.
     *
     * Finally, checks to see if any more items can be added.
     *
     * Based on all this, the Save, Restore, and Add buttons are enabled or
     * disabled.
     */
    private void validate() {
        //validateFieldData();
    }

    /*
    private void validateFieldData() {
        boolean hasNulls = false;
        for (DataFieldValue fv : fieldValues) {
            if (fv == null) {
                hasNulls = true;
                break;
            }
        }

        boolean hasDuplicates = false;
        String duplicateName = null;

        if (!hasNulls) {
            int cnt = fieldValues.size();
            for (int i = 0; i < (cnt - 1); i++) {
                for (int j = (i + 1); j < cnt; j++) {
                    DataFieldValue f1 = fieldValues.get(i);
                    DataFieldValue f2 = fieldValues.get(j);
                    if (f1.getReferenceId().equals(f2.getReferenceId())) {
                        hasDuplicates = true;
                        duplicateName = f1.getValue().toString();
                        break;
                    }
                }
            }
        }

        boolean changes = false;
        if (!hasNulls && !hasDuplicates) {
            if (fieldValues.size() != dataInstance.getFieldValues(dataType, dataField).size()) {
                changes = true;
            } else {
                List<DataFieldValue> list = dataInstance.getFieldValues(dataType, dataField);
                for (int i = 0; i < list.size(); i++) {
                    DataFieldValue f1 = list.get(i);
                    DataFieldValue f2 = fieldValues.get(i);
                    if (!f1.getReferenceId().equals(f2.getReferenceId())) {
                        changes = true;
                        break;
                    }
                }
            }
        }

        // have we hit the limit?
        int max = dataField.getMaxValues();
        boolean maxedOut = fieldValues.size() == max && max != 0;

        String errorText = "&nbsp;";
        if (hasNulls) {
            errorText = "Please fill in empty items";
        }
        else if (hasDuplicates) {
            errorText = "Duplicate item: " + duplicateName;
        }

        errorLabel.setHtml("<div id='endslice-error-label'>" + errorText + "</div>");
        _okButton.setEnabled(!hasDuplicates && !hasNulls && changes);
        addButton.setEnabled(!maxedOut);
        if (createButton != null) {
            createButton.setEnabled(!maxedOut);
        }
    }
    */
}

abstract class EditorDataInputListener implements DataInputListener {
	protected CellEditor editor = null;
	
	public void setEditor(CellEditor e) {
		editor = e;
	}

}
