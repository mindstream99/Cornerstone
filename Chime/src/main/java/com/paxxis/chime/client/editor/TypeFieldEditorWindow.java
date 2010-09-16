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

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.paxxis.chime.client.DataInputListener;
import com.paxxis.chime.client.DataInstanceComboBox;
import com.paxxis.chime.client.LoginResponseObject;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceManagerAdapter;
import com.paxxis.chime.client.ServiceManagerListener;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.EditDataInstanceRequest;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.widgets.ChimeWindow;

/**
 *
 * @author Robert Englander
 */
public class TypeFieldEditorWindow extends ChimeWindow
{
    private ButtonBar _buttonBar;
    private Button _okButton;
    private Button _cancelButton;
    private FormPanel _form = new FormPanel();
    private TextArea descriptionField;
    private Shape dataType = null;
    private DataInstanceComboBox typeCombo;
    private ServiceManagerListener _serviceManagerListener = null;
    private TextField<String> nameField;
    private NumberField maxValuesField;
    private DataField dataField;
    private FieldDefinitionEditListener editListener;

    public TypeFieldEditorWindow(FieldDefinitionEditListener listener) {
        this(null, listener);
    }

    public TypeFieldEditorWindow(DataField field, FieldDefinitionEditListener listener) {
        super();
        dataField = field;
        this.editListener = listener;
    }
    
    protected void init() {
        setModal(true);

        if (dataField == null) {
            setHeading("Create Shape Field");
        } else {
            setHeading("Edit Shape Field");
        }

        setMaximizable(false);
        setMinimizable(false);
        setCollapsible(false);
        setClosable(true);
        setResizable(false);
        setWidth(400);
        
        _form.setHeaderVisible(false);
        _form.setBorders(false);
        _form.setBodyBorder(false);
        _form.setStyleAttribute("padding", "5");
        _form.setButtonAlign(HorizontalAlignment.CENTER);
        _form.setFrame(true);
        _form.setFieldWidth(250);
        _form.setLabelWidth(85);

        DataInputListener listener = new DataInputListener() {

            public void onDataInstance(DataInstance instance) {
                dataType = (Shape)instance;
                validate();
            }

            public void onStringData(String text) {
            }

        };

        nameField = new TextField<String>();
        nameField.setFieldLabel("Name");
        new KeyNav(nameField) {
            @Override
            public void onKeyPress(final ComponentEvent cd) {
                DeferredCommand.addCommand(
                    new Command() {
                        public void execute() {
                            validate();
                        }
                    }
                );
            }
        };

        _form.add(nameField);

        descriptionField = new TextArea();
        descriptionField.setFieldLabel("Description");
        new KeyNav(descriptionField) {
            @Override
            public void onKeyPress(final ComponentEvent cd) {
                DeferredCommand.addCommand(
                    new Command() {
                        public void execute() {
                            validate();
                        }
                    }
                );
            }
        };
        _form.add(descriptionField);

        typeCombo = new DataInstanceComboBox(listener, Shape.SHAPE_ID, true, false, true);
        typeCombo.setExcludeInternals(false);
        typeCombo.setFieldLabel("Shape");
        typeCombo.setReturnFullInstances(true);
        _form.add(typeCombo);

        maxValuesField = new NumberField();
        maxValuesField.setFieldLabel("Max Values");
        descriptionField.setFieldLabel("Description");
        new KeyNav(maxValuesField) {
            @Override
            public void onKeyPress(final ComponentEvent cd) {
                DeferredCommand.addCommand(
                    new Command() {
                        public void execute() {
                            validate();
                        }
                    }
                );
            }
        };
        _form.add(maxValuesField);
        
        _buttonBar = new ButtonBar();
        _buttonBar.setAlignment(HorizontalAlignment.CENTER);

        _okButton = new Button("Ok");
        _okButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                    create();
                }
            }
        );
        
        _buttonBar.add(_okButton);
        
        _cancelButton = new Button("Cancel");
        _cancelButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                    doCancel();
                }
            }
        );
        
        _buttonBar.add(_cancelButton);
        
        _form.add(_buttonBar);
        add(_form);
        
        _serviceManagerListener = new ServiceManagerAdapter() {
            public void onLoginResponse(LoginResponseObject resp) {
            }
        };

        ServiceManager.addListener(_serviceManagerListener);

        validate();

        if (dataField != null) {
            nameField.setValue(dataField.getName());
            descriptionField.setValue(dataField.getDescription());
            useDataType(dataField.getShape());
            maxValuesField.setValue(dataField.getMaxValues());
        }
    }

    protected void useDataType(Shape type) {
        typeCombo.applyInput(type.getName());
        dataType = type;
    }

    protected void getDataType(Shape type) {
    }

    protected void create() {
        DataField field = new DataField();
        field.setShape(dataType);
        field.setName(nameField.getValue().trim());
        field.setDescription(descriptionField.getValue().trim());
        field.setMaxValues(maxValuesField.getValue().intValue());
        editListener.onEdit(field, FieldDefinitionEditListener.Type.Add);
        hide();
    }

    protected void createInstance(final EditDataInstanceRequest req) {
    }

    protected void appendRequest(EditDataInstanceRequest req) {

    }

    protected void doCreate(EditDataInstanceRequest req) {
    }
    
    private void validate() {
        String name = nameField.getValue();
        boolean validName = name != null && name.trim().length() > 0;

        String desc = descriptionField.getValue();
        boolean validDesc = desc != null && desc.trim().length() > 0;

        boolean validType = (dataType != null);

        Number number = maxValuesField.getValue();
        boolean validNumber = (number != null && number.doubleValue() >= 0);

        _okButton.setEnabled(validName && validDesc && validType && validNumber);
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
    
}
