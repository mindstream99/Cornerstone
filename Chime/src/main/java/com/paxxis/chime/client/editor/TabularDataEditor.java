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
import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.RowEditor;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid.ClicksToEdit;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.paxxis.chime.client.DataInputListener;
import com.paxxis.chime.client.DataInstanceComboBox;
import com.paxxis.chime.client.LoginResponseObject;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceManagerAdapter;
import com.paxxis.chime.client.ServiceManagerListener;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.widgets.ChimeGrid;
import com.paxxis.chime.client.widgets.ChimeWindow;
import com.paxxis.chime.client.widgets.FieldDataGridCellRenderer;
import com.paxxis.chime.client.widgets.InstanceCellRenderer;
import com.paxxis.chime.client.widgets.TabularDataFieldValueModel;
import com.paxxis.cornerstone.base.InstanceId;

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

    private ToolBar toolBar;
    private Button addButton;
    private Button deleteButton;
    private Button restoreButton;

    private ChimeGrid<TabularDataFieldValueModel> fieldGrid;
    private ListStore<TabularDataFieldValueModel> listStore = new ListStore<TabularDataFieldValueModel>();
    private ServiceManagerListener _serviceManagerListener = null;

    private DataInstance dataInstance;
    private DataInstance origInstance;
    private Shape dataType;
    private DataField dataField;

    private FieldEditListener editListener = null;

    public TabularDataEditor(DataInstance instance, Shape type,
                                DataField field, FieldEditListener listener) {
        super();

        editListener = listener;
        dataInstance = instance;
        origInstance = instance.copy();
        dataType = type;
        dataField = field;
    }

    protected void init() {
    	initialize();
    	listStore.addStoreListener(
    		new ListStoreEditChangeHandler<TabularDataFieldValueModel>() {
				@Override
				public void onChange() {
					validate();
				}
    		}
    	);
    	setup();
    	validate();
    }
    
    private void setup() {
    	// this form of copy is used so that the data instance object itself isn't recreated.
    	// this is necessary because the current implementation of the controller, in this
    	// case DataDetailPanel, expects the instance to be directly modified.
    	origInstance.copy(dataInstance);
    	
        listStore.removeAll();

        DeferredCommand.addCommand(
            new Command() {
                public void execute() {
                    for (DataFieldValue val : dataInstance.getFieldValues(dataType, dataField)) {
                        addValue(val);
                    }

                    validate();
                }
            }
        );
    }

    private void initialize() {
    	setLayout(new RowLayout());
        setModal(true);
        setHeading("Edit " + dataField.getName() + " [" + dataType.getName() + "]");

        setMaximizable(false);
        setMinimizable(false);
        setCollapsible(false);
        setClosable(true);
        setResizable(false);
        setWidth(600);

        toolBar = new ToolBar();
        toolBar.setAlignment(HorizontalAlignment.LEFT);
        
        addButton = new Button("Add");
        addButton.setIconStyle("add-icon");
        addButton.setIconAlign(IconAlign.BOTTOM);

        toolBar.add(addButton);

        deleteButton = new Button("Delete");
        deleteButton.setIconStyle("delete-icon");
        deleteButton.setIconAlign(IconAlign.BOTTOM);
        deleteButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                    deleteSelected();
                }
            }
        );

        toolBar.add(deleteButton);

        restoreButton = new Button("Restore");
        restoreButton.setIconStyle("restore-icon");
        restoreButton.setIconAlign(IconAlign.BOTTOM);
        restoreButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                    setup();
                }
            }
        );

        toolBar.add(restoreButton);

        add(toolBar, new RowData(1, -1, new Margins(0)));
        
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        ColumnConfig column = new ColumnConfig();
        column.setId(TabularDataFieldValueModel.BLANK); 
        column.setFixed(true);
        column.setHeader("");
        column.setWidth(2);
        column.setSortable(false);
        column.setMenuDisabled(true);
        configs.add(column);
        column.setRenderer(new InstanceCellRenderer(new Margins(1), false));

        Shape shape = dataField.getShape();
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
        }

        ColumnModel cm = new ColumnModel(configs);
        fieldGrid = new ChimeGrid<TabularDataFieldValueModel>(listStore, cm, false, true);
        fieldGrid.getView().setAutoFill(false);
        GridSelectionModel<TabularDataFieldValueModel> sm = new GridSelectionModel<TabularDataFieldValueModel>();
        sm.setSelectionMode(SelectionMode.SINGLE);
        fieldGrid.setSelectionModel(sm);
        fieldGrid.getView().setForceFit(false);
        fieldGrid.getView().setShowDirtyCells(false);
        fieldGrid.setHideHeaders(false);
        fieldGrid.setTrackMouseOver(false);
        fieldGrid.setStripeRows(false);
        fieldGrid.setBorders(true);
        fieldGrid.setHeight(300);
        sm.addSelectionChangedListener(
        	new SelectionChangedListener<TabularDataFieldValueModel>() {
				@Override
				public void selectionChanged(SelectionChangedEvent<TabularDataFieldValueModel> evt) {
					validate();
				}
        	}
        );

        // add the row editor plugin
        final RowEditor<TabularDataFieldValueModel> editor = new RowEditor<TabularDataFieldValueModel>();
        editor.setClicksToEdit(ClicksToEdit.TWO);
        fieldGrid.addPlugin(editor);
        addButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                    editor.stopEditing(false);
                	addValue((DataFieldValue)null);
                	editor.startEditing(listStore.getCount() - 1, true);
                }
            }
        );

        add(fieldGrid, new RowData(1, -1, new Margins(3)));
        
        errorLabel = new Html("<div id='endslice-error-label'>&nbsp;</div>");
        add(errorLabel, new RowData(1, -1, new Margins(3, 3, 10, 3)));

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

        add(saveCancelButtonBar, new RowData(1, -1, new Margins(7, 3, 3, 3)));
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
    		} else if (shape.isBoolean()) {
    			CheckBox f = new CheckBox();
    			f.setBoxLabel(field.getName());
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
                    String val = null;
                    if (value == null) {
                    	updateCombo(val);
                    } else {
                        if (value instanceof DataInstance) {
                            DataInstance instance = (DataInstance)value;
                            val = instance.getName();
                        } else {
                        	DataFieldValue dfVal = (DataFieldValue)value;
                        	val = dfVal.toString();
                        }
                    	
                    	updateCombo(val);
                    }
                    
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

                private void updateCombo(final String val) {
                    DeferredCommand.addCommand(
                    	new Command() {
                    		public void execute() {
                                DataInstanceComboBox combo = (DataInstanceComboBox)((AdapterField)getField()).getWidget();
                                combo.applyInput(val);
                    		}
                    	}
                    );
                }
            };
            
            listener.setEditor(editor);
        }
    	
    	return editor;
    }
    
    private void deleteSelected() {
    	TabularDataFieldValueModel model = fieldGrid.getSelectionModel().getSelectedItem();
    	listStore.remove(model);
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
            TabularDataFieldValueModel model = new TabularDataFieldValueModel(inst, dataField.getShape(), null);
            listStore.add(model);
    	} else {
            TabularDataFieldValueModel model = new TabularDataFieldValueModel((DataInstance)value.getValue(), dataField.getShape(), null);
            listStore.add(model);
    	}

        validate();
    }

    protected void doCancel() {
        ServiceManager.removeListener(_serviceManagerListener);
        hide();
    }

    private void notifyListener() {
        List<DataFieldValue> values = dataInstance.getFieldValues(dataType, dataField);
        
        // existing tabular field data is always cleared by the service and replaced
        // with the new data.  So clear the values and build new values from the listStore.
        values.clear();

        List<TabularDataFieldValueModel> models = listStore.getModels();
        for (TabularDataFieldValueModel model : models) {
        	DataInstance inst = model.getDataInstance();
        	dataInstance.appendFieldValue(dataType, dataField, inst, false);
        }

        editListener.onEdit(dataInstance, dataType, dataField);

        hide();
    }

    /**
     * Compares the new list with the existing list to determine if any changes
     * were made.
     *
     * Also checks to see if any more items can be added.
     *
     * Based on all this, the Save and Restore are enabled or
     * disabled.
     */
    private void validate() {
        boolean hasNulls = false;
        boolean hasDuplicates = false;

        boolean changes = false;
        if (!hasNulls && !hasDuplicates) {
            if (listStore.getCount() != origInstance.getFieldValues(dataType, dataField).size()) {
                changes = true;
            } else {
                List<DataFieldValue> list = origInstance.getFieldValues(dataType, dataField);
                for (int i = 0; i < list.size(); i++) {
                    DataFieldValue fieldVal = list.get(i);
                    TabularDataFieldValueModel model = listStore.getAt(i);
                    if (!equivalent(fieldVal, model)) {
                        changes = true;
                        break;
                    }
                }
            }
        }

        // have we hit the limit?
        int max = dataField.getMaxValues();
        boolean maxedOut = listStore.getCount() >= max && max != 0;

        String errorText = "&nbsp;";
        if (hasNulls) {
            errorText = "Please fill in empty items";
        }

        errorLabel.setHtml("<div id='endslice-error-label'>" + errorText + "</div>");
        boolean canSave = !hasDuplicates && !hasNulls && changes;
        _okButton.setEnabled(canSave);
        restoreButton.setEnabled(canSave);
        addButton.setEnabled(!maxedOut);
        deleteButton.setEnabled(fieldGrid.getSelectionModel().getSelectedItem() != null);
    }
    
    /**
     * Determine if the contents of a data field value is equivalent to the contents
     * of a data field value model.  Equivalence here refers to field by field data. 
     */
    private boolean equivalent(DataFieldValue fieldValue, TabularDataFieldValueModel model) {
    	DataInstance modelInstance = model.getDataInstance();
    	DataInstance otherInstance = (DataInstance)fieldValue.getValue();
    	for (DataField modelField : modelInstance.getShapes().get(0).getFields()) {
    		DataField otherField = otherInstance.getShapes().get(0).getField(modelField.getName());
    		List<DataFieldValue> modelValues = modelInstance.getFieldValues(modelField.getName());
    		List<DataFieldValue> otherValues = otherInstance.getFieldValues(otherField.getName());
    		if (modelValues.size() != otherValues.size()) {
    			return false;
    		}
    		
    		if (modelValues.size() > 0) {
    			// modified rows will have a DataFieldValue as the value within the DataFieldValues
    			// retrieved here.  in that case, it's those instances we want to use for comparison.
    			DataFieldValue modelValue = modelValues.get(0);
    			if (modelValue.getValue() instanceof DataFieldValue) {
    				modelValue = (DataFieldValue)modelValue.getValue();
    			}
    			
    			DataFieldValue otherValue = otherValues.get(0);
    			if (otherValue.getValue() instanceof DataFieldValue) {
    				otherValue = (DataFieldValue)otherValue.getValue();
    			}
    			
    			// check for equivalence but don't include the id in that check because if the user
    			// has already edited the value, the id will be UNKNOWN.  we don't care about that.
    			if (!modelValue.equals(otherValue, true)) {
    				return false;
    			}
    		}
    	}
    	
    	return true;
    }
}

abstract class EditorDataInputListener implements DataInputListener {
	protected CellEditor editor = null;
	
	public void setEditor(CellEditor e) {
		editor = e;
	}

}
