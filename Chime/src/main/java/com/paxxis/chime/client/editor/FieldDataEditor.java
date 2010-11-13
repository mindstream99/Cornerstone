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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
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
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.widgets.ChimeGrid;
import com.paxxis.chime.client.widgets.ChimeWindow;
import com.paxxis.chime.client.widgets.FieldDataGridCellRenderer;
import com.paxxis.chime.client.widgets.FieldValueModel;
import com.paxxis.chime.client.widgets.InstanceCellRenderer;

/**
 * 
 * @author Rob Englander
 *
 */
public class FieldDataEditor extends ChimeWindow {
    enum EditorType {
        FieldData,
        Files,
        Images,
        AppliedTypes
    }

    private Html errorLabel;

    private ButtonBar saveCancelButtonBar;
    private Button _okButton;
    private Button _cancelButton;

    private ToolBar toolBar;
    private Button addButton;
    private Button restoreButton;
    private Button deleteButton;
    private Button createButton = null;

    private ChimeGrid<FieldValueModel> fieldGrid;
    private ListStore<FieldValueModel> listStore = new ListStore<FieldValueModel>();
    private ServiceManagerListener _serviceManagerListener = null;

    private DataInstance dataInstance;
    private DataInstance origInstance;
    private Shape dataShape;
    private DataField dataField;

    private FieldEditListener editListener = null;
    private AppliedTypesEditListener typesListener = null;
    private EditorType editorType = EditorType.FieldData;

    public FieldDataEditor(DataInstance instance, AppliedTypesEditListener listener) {
        editorType = EditorType.AppliedTypes;
        typesListener = listener;
        dataInstance = instance;
		origInstance = instance.copy();
        dataShape = null;
        dataField = null;
    }
    
    public FieldDataEditor(DataInstance instance, boolean useImages, FieldEditListener listener) {
        super();

        if (useImages) {
            editorType = EditorType.Images;
        } else {
            editorType = EditorType.Files;
        }

        editListener = listener;
        dataInstance = instance;
		origInstance = instance.copy();
        dataShape = null;
        dataField = null;
    }

    public FieldDataEditor(DataInstance instance, Shape shape,
            DataField field, FieldEditListener listener) {
		super();
		
		editListener = listener;
		dataInstance = instance;
		origInstance = instance.copy();
		dataShape = shape;
		dataField = field;
        editorType = EditorType.FieldData;
	}

    protected void init() {
    	initialize();
    	listStore.addStoreListener(
    		new StoreListener<FieldValueModel>() {
    			  public void storeAdd(StoreEvent<FieldValueModel> se) {
    				  validate();
    			  }

    			  public void storeClear(StoreEvent<FieldValueModel> se) {
    				  validate();
    			  }

    			  public void storeDataChanged(StoreEvent<FieldValueModel> se) {
    				  validate();
    			  }

    			  public void storeRemove(StoreEvent<FieldValueModel> se) {
    				  validate();
    			  }

    			  public void storeUpdate(StoreEvent<FieldValueModel> se) {
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

        switch (editorType) {
	        case AppliedTypes:
	        	setupAppliedShapes();
	        	break;
	        case Files:
	        	setupFileData();
	        	break;
	        case Images:
	        	setupImageData();
	        	break;
	        case FieldData:
	        	setupFieldData();
	        	break;
        }
    }

    private void setupFileData() {
        DeferredCommand.addCommand(
            new Command() {
                public void execute() {
                    for (DataInstance val : dataInstance.getFiles()) {
                        addFileValue(val);
                    }

                    validate();
                }
            }
        );
    }
    
    private void setupImageData() {
        DeferredCommand.addCommand(
            new Command() {
                public void execute() {
                    for (DataInstance val : dataInstance.getImages()) {
                        addFileValue(val);
                    }

                    validate();
                }
            }
        );
    }
    
    private void setupAppliedShapes() {
        DeferredCommand.addCommand(
            new Command() {
                public void execute() {
                    for (Shape val : dataInstance.getShapes()) {
                        addShapeValue(val);
                    }

                    validate();
                }
            }
        );
    }
    
    private void setupFieldData() {
        DeferredCommand.addCommand(
            new Command() {
                public void execute() {
                    for (DataFieldValue val : dataInstance.getFieldValues(dataShape, dataField)) {
                    	InstanceId id = val.getReferenceId();
                    	if (id.equals(InstanceId.UNKNOWN)) {
                            addValue(val.getValue());
                    	} else {
                        	String name = val.getValue().toString();
                    		DataInstance inst = new DataInstance();
                    		inst.setName(name);
                    		inst.setId(id);
                    		inst.addShape(dataField.getShape());
                            addValue(inst);
                    	}
                    }

                    validate();
                }
            }
        );
    }
    
    private void initialize() {
    	setLayout(new RowLayout());
        setModal(true);
        switch (editorType) {
        case FieldData:
            setHeading("Edit " + dataField.getName() + " [" + dataShape.getName() + "]");
            break;
        case Images:
            setHeading("Edit Images");
            break;
        case Files:
            setHeading("Edit Files");
            break;
        case AppliedTypes:
            setHeading("Edit Applied Shapes");
            break;
        }

        setMaximizable(false);
        setMinimizable(false);
        setCollapsible(false);
        setClosable(true);
        setResizable(false);
        setWidth(350);

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

    	if ( (editorType == EditorType.FieldData && dataField != null && dataField.getShape().isDirectCreatable()) ||
    		  editorType == EditorType.Files || editorType == EditorType.Images) {
            createButton = new Button("Create");
            createButton.setIconStyle("new-icon");
            createButton.setIconAlign(IconAlign.BOTTOM);
            createButton.addSelectionListener(
                new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent evt) {
                       createNewInstance();
                    }
                }
            );

            toolBar.add(createButton);
    	}

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
        column.setId(FieldValueModel.BLANK); 
        column.setFixed(true);
        column.setHeader("");
        column.setWidth(2);
        column.setSortable(false);
        column.setMenuDisabled(true);
        configs.add(column);
        column.setRenderer(new InstanceCellRenderer(new Margins(1), false));

        column = new ColumnConfig();
        column.setId(FieldValueModel.VALUE);
        column.setFixed(false);
        column.setHeader("");
        column.setWidth(150);
        column.setSortable(false);
        column.setMenuDisabled(true);
        
        if (editorType == EditorType.AppliedTypes) {
            column.setEditor(getReferenceCellEditor(Shape.SHAPE_ID));
            column.setRenderer(new InstanceCellRenderer(new Margins(1), false));
        } else if (editorType == EditorType.Files) {
            column.setEditor(getReferenceCellEditor(Shape.FILE_ID));
            column.setRenderer(new InstanceCellRenderer(new Margins(1), false));
        } else if (editorType == EditorType.Images) {
            column.setEditor(getReferenceCellEditor(Shape.IMAGE_ID));
            column.setRenderer(new InstanceCellRenderer(new Margins(1), false));
        } else {
            column.setEditor(getCellEditor(dataField.getShape()));
            column.setRenderer(new FieldDataGridCellRenderer(new Margins(1), false));
        }

        configs.add(column);

        ColumnModel cm = new ColumnModel(configs);
        fieldGrid = new ChimeGrid<FieldValueModel>(listStore, cm, false, true);
        fieldGrid.getView().setAutoFill(true);
        GridSelectionModel<FieldValueModel> sm = new GridSelectionModel<FieldValueModel>();
        sm.setSelectionMode(SelectionMode.SINGLE);
        fieldGrid.setSelectionModel(sm);
        fieldGrid.getView().setForceFit(false);
        fieldGrid.getView().setShowDirtyCells(false);
        fieldGrid.setHideHeaders(true);
        fieldGrid.setTrackMouseOver(false);
        fieldGrid.setStripeRows(false);
        fieldGrid.setBorders(true);
        fieldGrid.setHeight(200);
        
        sm.addSelectionChangedListener(
        	new SelectionChangedListener<FieldValueModel>() {
				@Override
				public void selectionChanged(SelectionChangedEvent<FieldValueModel> evt) {
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

    private CellEditor getCellEditor(final Shape shape) {
    	CellEditor editor = null;
    	
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
    		editor = getReferenceCellEditor(shape.getId());
        }
    	
    	return editor;
    }

    private CellEditor getReferenceCellEditor(InstanceId shapeId) {
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

        final DataInstanceComboBox combo = new DataInstanceComboBox(listener, shapeId, true, false, true);
        combo.setReturnFullInstances(true);
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
    	FieldValueModel model = fieldGrid.getSelectionModel().getSelectedItem();
    	listStore.remove(model);
    }

    private void createNewInstance() {
        if (editorType == EditorType.FieldData) {
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
        } else {
            FileCreatorWindow.FileType type;
            if (editorType == EditorType.Files) {
                type = FileCreatorWindow.FileType.File;
            } else {
                type = FileCreatorWindow.FileType.Image;
            }

            FileCreatorWindow w = new FileCreatorWindow(type, dataInstance,
                new FileCreatorWindow.FileCreationListener() {
                    public void onFileCreated(DataInstance fileInstance) {
                        addValue(fileInstance);
                    }
            });

            w.show();
        }
    }

    private void addValue(Serializable value) {
    	switch (editorType) {
	    	case AppliedTypes:
	    		addShapeValue(value);
	    		break;
	    	case Files:
	    	case Images:
	    		addFileValue(value);
	    		break;
	    	case FieldData:
	    		addFieldValue(value);
	    		break;
    	}
    }
    
    private void addFileValue(Serializable value) {
    	if (value == null) {
    		DataInstance inst = new DataInstance();
    		inst.setId(InstanceId.UNKNOWN);
    		inst.setName("");
    		value = inst;
    	}

        FieldValueModel model = new FieldValueModel(dataShape, dataField, value);
        listStore.add(model);
        validate();
    }
    
    private void addShapeValue(Serializable value) {
    	if (value == null) {
    		Shape shape = new Shape();
    		shape.setId(InstanceId.UNKNOWN);
    		shape.setName("");
    		value = shape;
    	}

        FieldValueModel model = new FieldValueModel(dataShape, dataField, value);
        listStore.add(model);
        validate();
    }
    
    private void addFieldValue(Serializable value) {
    	if (value == null) {
    		FieldValueModel model;
    		Shape shape = dataField.getShape();
    		if (shape.isPrimitive()) {
        		if (shape.isNumeric()) {
                    model = new FieldValueModel(dataShape, dataField,  0.0);
        		} else if (shape.isDate()) {
                    model = new FieldValueModel(dataShape, dataField, new Date());
        		} else {
                    model = new FieldValueModel(dataShape, dataField, "");
        		}
    		} else {
                DataInstance inst = new DataInstance();
                inst.setName("");
                inst.addShape(dataField.getShape());
                inst.setId(InstanceId.UNKNOWN);
                model = new FieldValueModel(dataShape, dataField,  inst);
    		}

    		listStore.add(model);
    	} else {
            FieldValueModel model = new FieldValueModel(dataShape, dataField, value);
            listStore.add(model);
    	}

        validate();
    }

    protected void doCancel() {
        ServiceManager.removeListener(_serviceManagerListener);
        hide();
    }

    private void notifyListener() {
    	
    	switch (editorType) {
	    	case AppliedTypes:
	    		notifyAppliedShapes();
	    		break;
	    	case Files:
	    	case Images:
	    		notifyFiles();
	    		break;
	    	case FieldData:
	    		notifyFieldData();
	    		break;
    	}
    	
    	hide();
    }
    
    private void notifyFiles() {
        List<DataInstance> files = new ArrayList<DataInstance>();
        for (FieldValueModel model : listStore.getModels()) {
            files.add((DataInstance)model.getValue());
        }

        if (editorType == EditorType.Files) {
            dataInstance.setFiles(files);
        } else {
            dataInstance.setImages(files);
        }

        editListener.onEdit(dataInstance, dataShape, dataField);
    }
    
    private void notifyAppliedShapes() {
        List<Shape> types = new ArrayList<Shape>();
        for (FieldValueModel model : listStore.getModels()) {
            types.add((Shape)model.getValue());
        }
        
        dataInstance.setShapes(types);
        typesListener.onEdit(dataInstance);
    }
    
    private void notifyFieldData() {
        List<DataFieldValue> values = dataInstance.getFieldValues(dataShape, dataField);
        
        // existing field data is always cleared by the service and replaced
        // with the new data.  So clear the values and build new values from the listStore.
        values.clear();

        List<FieldValueModel> models = listStore.getModels();
        for (FieldValueModel model : models) {
        	Serializable ser = model.getValue();
        	dataInstance.appendFieldValue(dataShape, dataField, ser, false);
        }

        editListener.onEdit(dataInstance, dataShape, dataField);
    }

    private void validate() {
    	boolean canDelete = true;
        if (editorType == EditorType.FieldData) {
            validateFieldData();
        } else if (editorType == EditorType.AppliedTypes) {
            validateAppliedTypes();
            canDelete = listStore.getCount() > 1; 
        } else {
            validateFileData();
        }

        deleteButton.setEnabled(fieldGrid.getSelectionModel().getSelectedItem() != null && canDelete);
    }
    
    private void validateAppliedTypes() {
    	List<FieldValueModel> models = listStore.getModels();

    	boolean hasNulls = false;
        for (FieldValueModel model : listStore.getModels()) {
        	if (model.getValue() == null ||
        			((DataInstance)model.getValue()).getId().equals(InstanceId.UNKNOWN)) {
        		hasNulls = true;
        		break;
        	}
        }

        boolean hasDuplicates = false;
        String duplicateName = null;

        if (!hasNulls) {
            int cnt = models.size();
            for (int i = 0; i < (cnt - 1); i++) {
                for (int j = (i + 1); j < cnt; j++) {
                    DataInstance f1 = (DataInstance)models.get(i).getValue();
                    DataInstance f2 = (DataInstance)models.get(j).getValue();
                    if (f1.getId().equals(f2.getId())) {
                        hasDuplicates = true;
                        duplicateName = f1.getName();
                        break;
                    }
                }
            }
        }

        boolean changes = false;
        if (!hasNulls && !hasDuplicates) {
            if (listStore.getCount() != origInstance.getShapes().size()) {
                changes = true;
            } else {
                List<Shape> list = origInstance.getShapes();
                for (int i = 0; i < list.size(); i++) {
                    Shape shape = list.get(i);
                    Shape shape2 = (Shape)listStore.getAt(i).getValue();
                    if (!shape.getId().equals(shape2.getId())) {
                        changes = true;
                        break;
                    }
                }
            }
        }

        String invalidName = "";
        boolean hasNonMulti = false;
        if (!(hasNulls || hasDuplicates || changes)) {
            for (FieldValueModel model : models) {
                Shape t = (Shape)model.getValue();
                if (!t.canMultiType()) {
                    invalidName = t.getName();
                    hasNonMulti = true;
                    break;
                }
            }
        }

        String errorText = "&nbsp;";
        if (hasNonMulti) {
            errorText = invalidName + " can't be applied to an instance";
        } else if (hasNulls) {
            errorText = "Please fill in empty shapes";
        }
        else if (hasDuplicates) {
            errorText = "Duplicate shape: " + duplicateName;
        }

        errorLabel.setHtml("<div id='endslice-error-label'>" + errorText + "</div>");
        boolean enable = !hasDuplicates && !hasNulls && changes && !hasNonMulti;
        _okButton.setEnabled(enable);
        restoreButton.setEnabled(hasDuplicates || hasNulls || changes || hasNonMulti);
    }

    private void validateFileData() {
    	List<FieldValueModel> models = listStore.getModels();

    	boolean hasNulls = false;
        for (FieldValueModel model : listStore.getModels()) {
        	if (model.getValue() == null ||
        			((DataInstance)model.getValue()).getId().equals(InstanceId.UNKNOWN)) {
        		hasNulls = true;
        		break;
        	}
        }

        boolean hasDuplicates = false;
        String duplicateName = null;

        if (!hasNulls) {
            int cnt = models.size();
            for (int i = 0; i < (cnt - 1); i++) {
                for (int j = (i + 1); j < cnt; j++) {
                    DataInstance f1 = (DataInstance)models.get(i).getValue();
                    DataInstance f2 = (DataInstance)models.get(j).getValue();
                    if (f1.getId().equals(f2.getId())) {
                        hasDuplicates = true;
                        duplicateName = f1.getName();
                        break;
                    }
                }
            }
        }

        boolean changes = false;
        List<DataInstance> theList;
        if (editorType == EditorType.Files) {
            theList = dataInstance.getFiles();
        } else {
            theList = dataInstance.getImages();
        }

        if (!hasNulls && !hasDuplicates) {
            if (models.size() != theList.size()) {
                changes = true;
            } else {
                for (int i = 0; i < theList.size(); i++) {
                    DataInstance f1 = theList.get(i);
                    DataInstance f2 = (DataInstance)models.get(i).getValue();
                    if (f1.getId() != f2.getId()) {
                        changes = true;
                        break;
                    }
                }
            }
        }

        String errorText = "&nbsp;";
        if (hasNulls) {
            errorText = "Please fill in empty items";
        }
        else if (hasDuplicates) {
            errorText = "Duplicate item: " + duplicateName;
        }

        errorLabel.setHtml("<div id='endslice-error-label'>" + errorText + "</div>");
        boolean canSave = !hasDuplicates && !hasNulls && changes;
        _okButton.setEnabled(canSave);
        restoreButton.setEnabled(hasDuplicates || hasNulls || changes);
    }
    
    private void validateFieldData() {
        boolean hasNulls = false;
        if (!dataField.getShape().isPrimitive()) {
            for (FieldValueModel model : listStore.getModels()) {
            	if (model.getValue() == null ||
            			((DataInstance)model.getValue()).getId().equals(InstanceId.UNKNOWN)) {
            		hasNulls = true;
            		break;
            	}
            }
        }

        boolean hasDuplicates = false;

        boolean changes = false;
        if (!hasNulls && !hasDuplicates) {
            if (listStore.getCount() != origInstance.getFieldValues(dataShape, dataField).size()) {
                changes = true;
            } else {
                List<DataFieldValue> list = origInstance.getFieldValues(dataShape, dataField);
                for (int i = 0; i < list.size(); i++) {
                    DataFieldValue fieldVal = list.get(i);
                    FieldValueModel model = listStore.getAt(i);
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
        restoreButton.setEnabled(hasDuplicates || hasNulls || changes);
        addButton.setEnabled(!maxedOut);
        if (createButton != null) {
            createButton.setEnabled(!maxedOut);
        }
    }
    
    /**
     * Determine if the contents of a data field value is equivalent to the contents
     * of a data field value model.  Equivalence here refers to field by field data. 
     */
    private boolean equivalent(DataFieldValue fieldValue, FieldValueModel model) {
    	Serializable modelSer = model.getValue();
    	Serializable otherSer;
    	if (dataField.getShape().isPrimitive()) {
        	otherSer = fieldValue.getValue();
    	} else {
    		modelSer = ((DataInstance)modelSer).getId();
        	otherSer = fieldValue.getReferenceId();
    	}

    	return modelSer.equals(otherSer);
    }
}

abstract class FieldEditorDataInputListener implements DataInputListener {
	protected CellEditor editor = null;
	
	public void setEditor(CellEditor e) {
		editor = e;
	}

}
