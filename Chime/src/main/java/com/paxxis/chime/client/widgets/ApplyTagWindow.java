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

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.paxxis.chime.client.ChimeAsyncCallback;
import com.paxxis.chime.client.DataInputListener;
import com.paxxis.chime.client.DataInstanceComboBox;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceResponseObject;
import com.paxxis.chime.client.ShapeResponseObject;
import com.paxxis.chime.client.common.Community;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.EditDataInstanceRequest;
import com.paxxis.chime.client.common.EditDataInstanceResponse;
import com.paxxis.chime.client.common.Scope;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.ShapeRequest;
import com.paxxis.chime.client.common.Tag;

/**
 *
 * @author Robert Englander
 */
public class ApplyTagWindow extends ChimeWindow
{
    public interface ApplyTagListener
    {
        public void onApply(Tag tag);
    }

    private ApplyTagListener _listener;
    private Button _okButton;
    private Button _cancelButton;
    private DataInstanceComboBox _tagBox;
    private Html _descLabel;
    private TextArea _descField;
    private CheckBox _privateField;
    private boolean _isNewTagMode = false;
    private LayoutContainer _mainContainer;
    private ButtonBar _buttonBar;
    
    public ApplyTagWindow(ApplyTagListener listener)
    {
        _listener = listener;
    }
    
    protected void init()
    {
        setModal(true);
        setLayout(new FitLayout());
        setHeading("Apply Tag");
        
        setMaximizable(false);
        setMinimizable(false);
        setClosable(false);
        setResizable(false);
        setWidth(300);
        
        _mainContainer = new LayoutContainer();
        _mainContainer.setLayout(new RowLayout(Orientation.VERTICAL));
        
        DataInputListener listener = new DataInputListener()
        {
            public void onDataInstance(DataInstance instance) 
            {
                _listener.onApply((Tag)instance);
                hide();
            }

            public void onStringData(String text) 
            {
                setNewTagMode();
            }
        };
        
        _tagBox = new DataInstanceComboBox(listener, Shape.TAG_ID, true, false, false);
        _tagBox.setReturnFullInstances(true);

        new KeyNav(_tagBox) 
        {
            public void onKeyPress(ComponentEvent ce) 
            {
                DeferredCommand.addCommand(
                    new Command()
                    {
                        public void execute()
                        {
                            updateButtonState();
                        }
                    }
                );
            }

        };

        _mainContainer.add(_tagBox, new RowData(1, -1, new Margins(5, 5, 3, 5)));
        
        _descLabel = new Html("<div id='endslice-form-label'>This is a new tag.  Please enter a description:</div>");
        _mainContainer.add(_descLabel, new RowData(1, -1, new Margins(5, 5, 3, 5)));
        _descLabel.setVisible(false);
        
        _descField = new TextArea();
        _mainContainer.add(_descField, new RowData(1, -1, new Margins(5, 5, 3, 5)));
        _descField.setVisible(false);

        _privateField = new CheckBox();
        _privateField.setBoxLabel("Make this tag private");
        _privateField.setToolTip("A private tag can only be seen by you.");
        _mainContainer.add(_privateField, new RowData(1, -1, new Margins(5, 5, 6, 5)));
        _privateField.setVisible(false);

        _buttonBar = new ButtonBar();
        _buttonBar.setAlignment(HorizontalAlignment.CENTER);
        
        _okButton = new Button("Ok");
        _okButton.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent evt) 
                {
                    if (_isNewTagMode)
                    {
                        String name = _tagBox.getRawValue();
                        String desc = _descField.getRawValue();
                        boolean isPrivate = _privateField.getValue();
                        _okButton.setEnabled(false);
                        applyNewTag(name, desc, isPrivate);
                    }
                    else
                    {
                        _tagBox.processKeyboardSelection(true);
                    }
                }
            }
        );

        _cancelButton = new Button("Cancel");
        _cancelButton.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent evt) 
                {
                    hide();
                }
            
            }
        );

        _buttonBar.add(_okButton);
        _buttonBar.add(_cancelButton);
        
        _mainContainer.add(_buttonBar);
        
        add(_mainContainer);
        
        _tagBox.focus();
        
        updateButtonState();
    }

    private void applyNewTag(final String name, final String desc, final boolean isPrivate)
    {
        // we need to go get the Tag type from the service.
        ShapeRequest req = new ShapeRequest();
        req.setId(Shape.TAG_ID);
        
        final ChimeAsyncCallback<ShapeResponseObject> callback = new ChimeAsyncCallback<ShapeResponseObject>() {
            public void onSuccess(ShapeResponseObject response) { 
                if (response.isResponse()) {
                    Shape dataType = response.getResponse().getShape();
                    applyNewTag(dataType, name, desc, isPrivate);
                } else {
                    ChimeMessageBox.alert("System Error", response.getError().getMessage(), null);
                }
            }

        };
        
        ServiceManager.getService().sendShapeRequest(req, callback);
    }

    private void applyNewTag(Shape type, final String name, final String desc, boolean isPrivate)
    {
        // the first step is to create a new Tag instance.
        EditDataInstanceRequest req = new EditDataInstanceRequest();
        req.addShape(type);
        req.setName(name);
        req.setOperation(EditDataInstanceRequest.Operation.Create);
        req.setUser(ServiceManager.getActiveUser());
        
        if (isPrivate) {
            Scope scope = new Scope(new Community(ServiceManager.getActiveUser().getId()), Scope.Permission.RU);
            req.addScope(scope);
        } else {
            Scope scope = new Scope(Community.Global, Scope.Permission.R);
            req.addScope(scope);
            scope = new Scope(Community.ChimeAdministrators, Scope.Permission.RU);
            req.addScope(scope);
        }

        req.setDescription(desc);
        
        final ChimeAsyncCallback<ServiceResponseObject<EditDataInstanceResponse>> callback = 
        		new ChimeAsyncCallback<ServiceResponseObject<EditDataInstanceResponse>>() {
            public void onSuccess(ServiceResponseObject<EditDataInstanceResponse> response) { 
                if (response.isResponse()) {
                    Tag tag = (Tag)response.getResponse().getDataInstance();
                    _listener.onApply(tag);
                    hide();
                } else {
                    ChimeMessageBox.alert("System Error", response.getError().getMessage(), null);
                }
            }

        };
        
        ServiceManager.getService().sendEditDataInstanceRequest(req, callback);
    }
    
    private void setNewTagMode()
    {
        _isNewTagMode = true;
        
        _descLabel.setVisible(true);
        _descField.setVisible(true);
        _privateField.setVisible(true);
        
        _tagBox.setEnabled(false);
        
        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    int h = _tagBox.getSize().height + 
                                _descLabel.getSize().height + 
                                _descField.getSize().height + 
                                _privateField.getSize().height + 
                                _buttonBar.getSize().height +
                                _mainContainer.getSize().height;
                    setHeight(h);
                    layout();
                }
            }
        );

        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    _descField.focus();
                }
            }
        );
    }
    
    private void updateButtonState()
    {
        _okButton.setEnabled(_tagBox.getRawValue().length() > 0);
    }
}
