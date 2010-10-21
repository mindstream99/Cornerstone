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
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.paxxis.chime.client.ChimeAsyncCallback;
import com.paxxis.chime.client.ChimeListStore;
import com.paxxis.chime.client.DataInputListener;
import com.paxxis.chime.client.DataInstanceComboBox;
import com.paxxis.chime.client.DataInstanceResponseObject;
import com.paxxis.chime.client.LoginResponseObject;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceResponseObject;
import com.paxxis.chime.client.ShapeResponseObject;
import com.paxxis.chime.client.SimpleDataInstanceModel;
import com.paxxis.chime.client.StateManager;
import com.paxxis.chime.client.common.Community;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceRequest;
import com.paxxis.chime.client.common.DataInstanceResponse;
import com.paxxis.chime.client.common.EditDataInstanceRequest;
import com.paxxis.chime.client.common.EditDataInstanceResponse;
import com.paxxis.chime.client.common.Scope;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.ShapeRequest;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.client.common.EditDataInstanceRequest.Operation;
import com.paxxis.chime.client.pages.PageManager;
import com.paxxis.chime.client.widgets.ChimeMessageBox;
import com.paxxis.chime.client.widgets.ChimeMessageBoxEvent;
import com.paxxis.chime.client.widgets.ChimeWindow;

/**
 *
 * @author Robert Englander
 */
public class InstanceCreatorWindow extends ChimeWindow
{
    public interface InstanceCreationListener {
        public void onInstanceCreated(DataInstance instance);
    }

    private ButtonBar _buttonBar;
    private Button _okButton;
    private Button _cancelButton;
    private FormPanel _form = new FormPanel();
    private TextArea descriptionField;
    private Shape dataType = null;
    private List<Scope> scopes = null;
    private DataInstanceComboBox typeCombo;
    private ComboBox<SimpleDataInstanceModel> visibilityCombo;
    private ChimeListStore<SimpleDataInstanceModel> visibilityStore;
    private ComboBox<SimpleDataInstanceModel> editableCombo;
    private ChimeListStore<SimpleDataInstanceModel> editableStore;
    private FieldSet visibilitySet;
    private TextField<String> nameField;
    private InstanceCreationListener creationListener;

    public InstanceCreatorWindow()
    {
        super();
        creationListener = null;
        setModal(true);
        setHeading("Create Data Instance");
        setMaximizable(false);
        setMinimizable(false);
        setCollapsible(false);
        setClosable(true);
        setResizable(false);
        setWidth(400);
    }

    public InstanceCreatorWindow(final Shape type, InstanceCreationListener listener) {
        this();
        creationListener = listener;
        setHeading("Create " + type.getName());
        DeferredCommand.addCommand(
            new Command() {
                public void execute() {
                    useShape(type.getName());
                }
            }
        );
    }
    
    protected void init()
    {
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
                getShape((Shape)instance);
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

        typeCombo = new DataInstanceComboBox(listener, Shape.SHAPE_ID, true, true, true);
        typeCombo.setFieldLabel("Shape");
        typeCombo.setReturnFullInstances(true);
        _form.add(typeCombo);

        descriptionField = new TextArea();
        descriptionField.setFieldLabel("Description");
        _form.add(descriptionField);

        visibilitySet = new FieldSet();
        visibilitySet.setLayout(new FormLayout());
        visibilitySet.setHeading("Permissions");

        visibilityCombo = new ComboBox<SimpleDataInstanceModel>();
        visibilityStore = new ChimeListStore<SimpleDataInstanceModel>();
        visibilityCombo.setDisplayField("name");
        visibilityCombo.setEditable(false);
        visibilityCombo.setStore(visibilityStore);
        visibilityCombo.setFieldLabel("Visible By");
        visibilityCombo.setEnabled(false);
        visibilitySet.add(visibilityCombo);

        visibilityCombo.addSelectionChangedListener(

            new SelectionChangedListener<SimpleDataInstanceModel>() {
                    @Override
                    public void selectionChanged(SelectionChangedEvent evt) {
                        setupEditable();
                    }
            }
        );


        editableCombo = new ComboBox<SimpleDataInstanceModel>();
        editableStore = new ChimeListStore<SimpleDataInstanceModel>();
        editableCombo.setDisplayField("name");
        editableCombo.setEditable(false);
        editableCombo.setStore(editableStore);
        editableCombo.setFieldLabel("Editable By");
        editableCombo.setEnabled(false);
        visibilitySet.add(editableCombo);

        _form.add(visibilitySet);

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
        validate();
    }

    protected void useShape(String shape) {
        typeCombo.applyInput(shape);
        typeCombo.setEnabled(false);
    }

    protected void getShape(Shape shape) {
        ChimeAsyncCallback<ShapeResponseObject> callback = new ChimeAsyncCallback<ShapeResponseObject>() {
            public void onSuccess(ShapeResponseObject resp) {
                if (resp.isResponse()) {
                    dataType = resp.getResponse().getShape();
                    setupVisibility();
                    validate();
                    layout();
                }
            }
        };

        scopes = shape.getSocialContext().getScopes();
        ShapeRequest req = new ShapeRequest();
        req.setId(shape.getId());
        ServiceManager.getService().sendShapeRequest(req, callback);
    }

    protected void create() {
        EditDataInstanceRequest req = new EditDataInstanceRequest();
        createInstance(req);
    }

    protected void createInstance(final EditDataInstanceRequest req) {
        ChimeAsyncCallback<DataInstanceResponseObject> callback = new ChimeAsyncCallback<DataInstanceResponseObject>() {
            public void onSuccess(DataInstanceResponseObject resp) {
                if (resp.isResponse()) {
                    DataInstanceResponse response = resp.getResponse();
                    List<DataInstance> instances = response.getDataInstances();

                    // if there are others with this name, ask the user to confirm the create.
                    // in the future, we'll show them to the user as part of the confirmation
                    int count = instances.size();
                    if (count > 0) {
                        String msg = "There are already 1 or more instances with this name.  Do you want to create it anyway?";

                        final Listener<ChimeMessageBoxEvent> l = new Listener<ChimeMessageBoxEvent>() {
                            public void handleEvent(ChimeMessageBoxEvent evt) {
                                Button btn = evt.getButtonClicked();
                                if (btn != null) {
                                    if (btn.getText().equalsIgnoreCase("yes")) {
                                        doCreate(req);
                                    }
                                }
                            }
                        };

                        ChimeMessageBox.confirm("Create", msg, l);
                    } else {
                        doCreate(req);
                    }
                } else {
                    ChimeMessageBox.alert("Error", resp.getError().getMessage(), null);
                }
            }
        };

        DataInstanceRequest request = new DataInstanceRequest();
        request.setUser(ServiceManager.getActiveUser());
        request.addQueryParameter(dataType, "name", nameField.getValue().trim());
        ServiceManager.getService().sendDataInstanceRequest(request, callback);
    }

    protected void appendRequest(EditDataInstanceRequest req) {

    }

    protected void doCreate(EditDataInstanceRequest req) {
        ChimeAsyncCallback<ServiceResponseObject<EditDataInstanceResponse>> callback = 
        			new ChimeAsyncCallback<ServiceResponseObject<EditDataInstanceResponse>>() {
            public void onSuccess(ServiceResponseObject<EditDataInstanceResponse> resp) {
                if (resp.isResponse()) {
                    EditDataInstanceResponse response = resp.getResponse();
                    DataInstance inst = response.getDataInstance();
                    if (creationListener != null) {
                        creationListener.onInstanceCreated(inst);
                    } else {
                        PageManager.instance().openNavigator(true, inst);
                        StateManager.instance().pushInactiveToken("detail:" + inst.getId());
                    }
                    doCancel();
                } else {
                    ChimeMessageBox.alert("Error", resp.getError().getMessage(), null);
                }
            }
        };

        req.setUser(ServiceManager.getActiveUser());

        DataInstance c = visibilityCombo.getSelection().get(0).getDataInstance();
        DataInstance c2 = editableCombo.getSelection().get(0).getDataInstance();

        if (c.getId().equals(c2.getId())) {
            req.addScope(new Scope(new Community(c.getId()), Scope.Permission.RU));
        } else {
            req.addScope(new Scope(new Community(c.getId()), Scope.Permission.R));
            req.addScope(new Scope(new Community(c2.getId()), Scope.Permission.RU));
        }

        req.setName(nameField.getValue().trim());
        req.setOperation(Operation.Create);
        req.addShape(dataType);
        req.setDescription(descriptionField.getValue());

        appendRequest(req);

        ServiceManager.getService().sendEditDataInstanceRequest(req, callback);
    }
    
    private void validate() {
        String name = nameField.getValue();
        boolean valid = name != null && name.trim().length() > 0 && dataType != null;
        _okButton.setEnabled(valid);
    }

    protected void setupVisibility() {
        visibilityStore.removeAll();
        visibilityCombo.setRawValue("");

        if (dataType == null) {
            visibilityCombo.setEnabled(false);
        } else {
            visibilityCombo.setEnabled(true);
            List<Community> list = findCommunities(scopes, true);
            for (Community community : list) {
                visibilityStore.add(new SimpleDataInstanceModel(community));
            }

            visibilityCombo.setValue(visibilityStore.getAt(0));
        }

        setupEditable();
    }

    protected void setupEditable() {
        Community visible = (Community)visibilityCombo.getSelection().get(0).getDataInstance();
        List<Scope> theScopes = new ArrayList<Scope>();
        theScopes.add(new Scope(visible, Scope.Permission.R));

        editableStore.removeAll();
        editableCombo.setRawValue("");

        if (dataType == null) {
            editableCombo.setEnabled(false);
        } else {
            editableCombo.setEnabled(true);
            List<Community> list = findCommunities(theScopes, false);
            for (Community community : list) {
                editableStore.add(new SimpleDataInstanceModel(community));
            }

            editableCombo.setValue(editableStore.getAt(0));
        }
    }

    /**
     * Finds all of the communities that can be used to scope the new data
     * instance.
     *
     * @return
     */
    private List<Community> findCommunities(List<Scope> theScopes, boolean allowAnonymous) {
        User me = ServiceManager.getActiveUser();
        List<Community> communities = me.getCommunities();
        communities.add(Community.Global);

        List<Community> results = new ArrayList<Community>();

        for (Community comm : communities) {
            if (isMatch(comm, theScopes)) {
                Community c = new Community(comm.getId());
                if (c.getId().equals(Community.Global.getId())) {
                    c.setName(comm.getName());
                    results.add(0, c);
                } else {
                    c.setName("Members of " + comm.getName());
                    results.add(c);
                }
            }
        }

        Community justMe = new Community(me.getId());
        justMe.setName("Just Me");
        results.add(0, justMe);

        return results;
    }

    public boolean isMatch(Community community, List<Scope> scopes) {
        boolean isMember = false;
        for (Scope scope : scopes) {
            if (scope.isGlobalCommunity()) {
                isMember = true;
                break;
            } else if (scope.getCommunity().getId().equals(community.getId())) {
                isMember = true;
                break;
            }
        }

        return isMember;
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
        hide();
    }
    
}
