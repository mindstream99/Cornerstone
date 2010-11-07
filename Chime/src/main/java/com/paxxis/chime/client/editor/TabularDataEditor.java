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
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreListener;
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
    private DataInstance origInstance;
    private Shape dataType;
    private DataField dataField;

    private FieldEditListener editListener = null;
    private AppliedTypesEditListener typesListener = null;

    public TabularDataEditor(DataInstance instance, Shape type,
                                DataField field, FieldEditListener listener) {
        super();

        editListener = listener;
        dataInstance = instance;
        origInstance = instance.copy();
        dataType = type;
        dataField = field;
    }

    protected void onRender(Element parent, int index) { 
    	super.onRender(parent, index);
    	
    	listStore.addStoreListener(
    		new StoreListener<DataFieldValueModel>() {
    			  public void storeAdd(StoreEvent<DataFieldValueModel> se) {
    				  validate();
    			  }

    			  public void storeClear(StoreEvent<DataFieldValueModel> se) {
    				  validate();
    			  }

    			  public void storeDataChanged(StoreEvent<DataFieldValueModel> se) {
    				  validate();
    			  }

    			  public void storeRemove(StoreEvent<DataFieldValueModel> se) {
    				  validate();
    			  }

    			  public void storeUpdate(StoreEvent<DataFieldValueModel> se) {
    				  validate();
    			  }
    		}
    	);
    	setup();
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

    protected void doCancel() {
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
                    DataFieldValueModel model = listStore.getAt(i);
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
        if (createButton != null) {
            createButton.setEnabled(!maxedOut);
        }
    }
    
    /**
     * Determine if the contents of a data field value is equivalent to the contents
     * of a data field value model.  Equivalence here refers to field by field data. 
     */
    private boolean equivalent(DataFieldValue fieldValue, DataFieldValueModel model) {
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
