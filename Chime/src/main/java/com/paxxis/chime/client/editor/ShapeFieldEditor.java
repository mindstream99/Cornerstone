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
import com.paxxis.chime.client.DataFieldModel;
import com.paxxis.chime.client.DataInstanceComboBox;
import com.paxxis.chime.client.InstanceUpdateListener;
import com.paxxis.chime.client.LoginResponseObject;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceManagerAdapter;
import com.paxxis.chime.client.ServiceManagerListener;
import com.paxxis.chime.client.InstanceUpdateListener.Type;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.widgets.ChimeGrid;
import com.paxxis.chime.client.widgets.ChimeWindow;
import com.paxxis.chime.client.widgets.DataFieldCellRenderer;
import com.paxxis.chime.client.widgets.FieldValueModel;

/**
 * 
 * @author Robert Englander
 *
 */
public class ShapeFieldEditor extends ChimeWindow {
    private Html errorLabel;

    private ButtonBar saveCancelButtonBar;
    private Button _okButton;
    private Button _cancelButton;

    private ToolBar toolBar;
    private Button addButton;
    private Button restoreButton;
    private Button deleteButton;

    private ChimeGrid<DataFieldModel> fieldGrid;
    private ListStore<DataFieldModel> listStore = new ListStore<DataFieldModel>();
    private ServiceManagerListener _serviceManagerListener = null;
    private InstanceUpdateListener saveListener;

    private Shape shape;
    private Shape origShape;
    
    public ShapeFieldEditor(Shape sh, InstanceUpdateListener listener) {
        shape = sh;
        saveListener = listener;
		origShape = shape.copy();
    }

    protected void init() {
    	initialize();
    	listStore.addStoreListener(
    		new ListStoreEditChangeHandler<DataFieldModel>() {
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
    	origShape.copy(shape);
    	
        listStore.removeAll();
        DeferredCommand.addCommand(
            new Command() {
                public void execute() {
                    for (DataField field : shape.getFields()) {
                        addValue(field);
                    }

                    validate();
                }
            }
        );

    }
    
    private void initialize() {
    	setLayout(new RowLayout());
        setModal(true);
        setHeading("Edit Fields");
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
        
        String[] cols = {
        	DataFieldModel.NAME,
        	DataFieldModel.DESCRIPTION,
        	DataFieldModel.SHAPE,
        	DataFieldModel.MAXVALUES,
        	DataFieldModel.FORMAT
        };

        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        for (String col : cols) {
            ColumnConfig column = new ColumnConfig();
            column.setId(col);
            column.setFixed(false);
            column.setHeader(col);
            column.setWidth(150);
            column.setSortable(false);
            column.setMenuDisabled(true);
            column.setRenderer(new DataFieldCellRenderer(false));
            column.setEditor(getCellEditor(col));
            configs.add(column);
        }

        ColumnModel cm = new ColumnModel(configs);
        fieldGrid = new ChimeGrid<DataFieldModel>(listStore, cm, false, true);
        fieldGrid.getView().setAutoFill(true);
        GridSelectionModel<DataFieldModel> sm = new GridSelectionModel<DataFieldModel>();
        sm.setSelectionMode(SelectionMode.SINGLE);
        fieldGrid.setSelectionModel(sm);
        fieldGrid.getView().setForceFit(false);
        fieldGrid.getView().setShowDirtyCells(false);
        fieldGrid.setHideHeaders(false);
        fieldGrid.setTrackMouseOver(false);
        fieldGrid.setStripeRows(false);
        fieldGrid.setBorders(true);
        fieldGrid.setHeight(200);
        fieldGrid.setAutoExpandColumn(DataFieldModel.FORMAT);
        
        sm.addSelectionChangedListener(
        	new SelectionChangedListener<DataFieldModel>() {
				@Override
				public void selectionChanged(SelectionChangedEvent<DataFieldModel> evt) {
					validate();
				}
        	}
        );

        // add the row editor plugin
        final RowEditor<FieldValueModel> editor = new RowEditor<FieldValueModel>();
        editor.setClicksToEdit(ClicksToEdit.TWO);
        fieldGrid.addPlugin(editor);

        addButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                	editor.stopEditing(false);
                	addValue(null);
                    editor.startEditing(listStore.getCount() - 1, true);
                }
            }
        );

        add(fieldGrid, new RowData(1, -1, new Margins(0)));

        errorLabel = new Html("<div id='endslice-error-label'>&nbsp;</div>");
        add(errorLabel, new RowData(1, -1, new Margins(3, 3, 5, 3)));

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

    private CellEditor getCellEditor(String name) {
    	CellEditor editor = null;
    	
		if (DataFieldModel.NAME.equals(name) || DataFieldModel.DESCRIPTION.equals(name) || 
				DataFieldModel.FORMAT.equals(name)) {
			TextField<String> f = new TextField<String>();
            f.setAutoWidth(true);
	    	editor = new CellEditor(f);
		} else if (DataFieldModel.MAXVALUES.equals(name)) {
			NumberField f = new NumberField();
			f.setAutoValidate(true);
	    	editor = new CellEditor(f);
            f.setAutoWidth(true);
		} else {
    		editor = getReferenceCellEditor();
        }
    	
    	return editor;
    }

    private CellEditor getReferenceCellEditor() {
		FieldEditorDataInputListener listener = new FieldEditorDataInputListener() {
        	
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

        final DataInstanceComboBox combo = new DataInstanceComboBox(listener, Shape.SHAPE_ID, true, false, true);
        combo.setReturnFullInstances(true);
        combo.setExcludeInternals(false);
        AdapterField af = new AdapterField(combo);
        af.setResizeWidget(true);

        CellEditor editor = new CellEditor(af) {  
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
            	if (value != null) {  
                    value = getData("DataInstance");
                }  

                return value;
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
        
        return editor;
    }

    private void deleteSelected() {
    	DataFieldModel model = fieldGrid.getSelectionModel().getSelectedItem();
    	listStore.remove(model);
    }
    
    private void addValue(DataField field) {
    	if (field == null) {
    		field = new DataField();
    		field.setName("new field");
    		field.setId(InstanceId.UNKNOWN.getValue());
    	}

        DataFieldModel model = new DataFieldModel(field);
        listStore.add(model);
        validate();
    }
    
    protected void doCancel() {
        ServiceManager.removeListener(_serviceManagerListener);
        hide();
    }

    private void notifyListener() {
        List<DataField> fields = shape.getFields();
        
        fields.clear();

        List<DataFieldModel> models = listStore.getModels();
        for (DataFieldModel model : models) {
        	DataField field = model.getDataField();
        	shape.addField(field);
        }

        saveListener.onUpdate(shape, Type.UpdateFieldDefinitions);

        hide();
    }
    
    private void validate() {
    	
    	
    	DataFieldModel model = fieldGrid.getSelectionModel().getSelectedItem();
    	boolean canDelete = false;
    	if (model != null) {
    		DataField field = model.getDataField();
    		canDelete = field.getId().equals(InstanceId.UNKNOWN.getValue());
    	}
    	
    	deleteButton.setEnabled(canDelete);
    }
    
}
