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

import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.paxxis.chime.client.ChimeAsyncCallback;
import com.paxxis.chime.client.DataInputListener;
import com.paxxis.chime.client.DataInstanceComboBox;
import com.paxxis.chime.client.DataInstanceResponseObject;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceRequest;
import com.paxxis.chime.client.common.DataInstanceResponse;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.DataInstanceRequest.Depth;
import com.paxxis.chime.client.widgets.ChimeWindow;

/**
 *
 * @author Robert Englander
 */
public class DataInstanceSelectionWindow extends ChimeWindow
{
    private DataInstanceSelectionListener _listener;

    
    private Button _okButton;
    private Button _cancelButton;
    private DataInstanceComboBox instanceBox;
    private LayoutContainer _mainContainer;
    private ButtonBar _buttonBar;
    private InstanceId dataShape;
    private DataInstance selectedInstance = null;
    private String initialId = null;
    
   // private ReferenceEditorPanel _simpleEditorPanel = null;

    public DataInstanceSelectionWindow(InstanceId shape, DataInstanceSelectionListener listener) {
    	this(shape, null, listener);
    }
    
    public DataInstanceSelectionWindow(InstanceId shape, String initialId, DataInstanceSelectionListener listener) {
        _listener = listener;
        dataShape = shape;
        this.initialId = initialId;
        setModal(true);
        setMaximizable(false);
        setMinimizable(false);
        setClosable(false);
        setResizable(false);
        setWidth(300);
        setHeading("Select Data Instance");
    }
    
    protected void init()
    {
        setLayout(new FitLayout());
        _mainContainer = new LayoutContainer();
        _mainContainer.setLayout(new RowLayout(Orientation.VERTICAL));
        
        DataInputListener listener = new DataInputListener()
        {
            public void onDataInstance(DataInstance instance) 
            {
            	selectedInstance = instance;
            	validate();
            }

            public void onStringData(String text) 
            {
            	selectedInstance = null;
            	validate();
            }
        };
        
        instanceBox = new DataInstanceComboBox(listener, dataShape, true, false, false);

        new KeyNav(instanceBox) 
        {
            public void onKeyPress(ComponentEvent ce) 
            {
                DeferredCommand.addCommand(
                    new Command()
                    {
                        public void execute()
                        {
                            validate();
                        }
                    }
                );
            }

        };

        _mainContainer.add(instanceBox, new RowData(1, -1, new Margins(7, 5, 3, 5)));
        
        _buttonBar = new ButtonBar();
        _buttonBar.setAlignment(HorizontalAlignment.CENTER);
        
        _okButton = new Button("Ok");
        _okButton.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent evt) 
                {
                	_listener.onSave(selectedInstance);
                	hide();
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
        
        _mainContainer.add(_buttonBar, new RowData(1, -1, new Margins(4)));
        add(_mainContainer);
        instanceBox.focus();

        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    validate();
                }
            }
        );

        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
            		loadInitial();
                }
            }
        );
        
    }

    private void loadInitial() {
    	if (initialId != null) {
            final ChimeAsyncCallback<DataInstanceResponseObject> callback = 
            			new ChimeAsyncCallback<DataInstanceResponseObject>() {
                public void onSuccess(DataInstanceResponseObject resp) {
                    if (resp.isResponse()) {
                        final DataInstanceResponse response = resp.getResponse();
                        List<DataInstance> instances = response.getDataInstances();
                        if (instances.size() > 0)
                        {
                            DataInstance instance = instances.get(0);
                            instanceBox.applyInput(instance.getName());
                        }
                    }
                }
            };

            DataInstanceRequest req = new DataInstanceRequest();
            req.setDepth(Depth.Shallow);
            req.setIds(InstanceId.create(initialId));
            req.setUser(ServiceManager.getActiveUser());
            ServiceManager.getService().sendDataInstanceRequest(req, callback);
    	}
    }
    
    private boolean validate()
    {
    	boolean valid = (selectedInstance != null);
        _okButton.setEnabled(valid);
        return valid;
    }
}
