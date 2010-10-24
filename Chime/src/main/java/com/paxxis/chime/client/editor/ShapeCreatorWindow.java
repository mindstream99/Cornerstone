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
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.paxxis.chime.client.ChimeAsyncCallback;
import com.paxxis.chime.client.ChimeListStore;
import com.paxxis.chime.client.LoginResponseObject;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceManagerAdapter;
import com.paxxis.chime.client.ServiceManagerListener;
import com.paxxis.chime.client.ServiceResponseObject;
import com.paxxis.chime.client.ShapeResponseObject;
import com.paxxis.chime.client.SimpleDataInstanceModel;
import com.paxxis.chime.client.StateManager;
import com.paxxis.chime.client.common.Community;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.EditShapeRequest;
import com.paxxis.chime.client.common.EditShapeResponse;
import com.paxxis.chime.client.common.Scope;
import com.paxxis.chime.client.common.ShapeRequest;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.client.pages.PageManager;
import com.paxxis.chime.client.widgets.ChimeMessageBox;
import com.paxxis.chime.client.widgets.ChimeWindow;

/**
 *
 * @author Robert Englander
 */
public class ShapeCreatorWindow extends ChimeWindow
{
    private ButtonBar _buttonBar;
    private Button _okButton;
    private Button _cancelButton;
    private FormPanel _form = new FormPanel();
    private TextArea descriptionField;
    private ComboBox<SimpleDataInstanceModel> visibilityCombo;
    private ChimeListStore<SimpleDataInstanceModel> visibilityStore;
    private ComboBox<SimpleDataInstanceModel> editableCombo;
    private ChimeListStore<SimpleDataInstanceModel> editableStore;
    private FieldSet permissionSet;
    private ServiceManagerListener _serviceManagerListener = null;
    private TextField<String> nameField;
    private CheckBox tabularField;

    public ShapeCreatorWindow()
    {
        super();
    }
    
    protected void init()
    {
        setModal(true);
        setHeading("Create Shape");
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
        _form.setLabelSeparator("");

        nameField = new TextField<String>();
        nameField.setFieldLabel("Name:");
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
        descriptionField.setFieldLabel("Description:");

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

        tabularField = new CheckBox();
        tabularField.setHideLabel(true);
        tabularField.setBoxLabel("Tabular");
        _form.add(tabularField);
        
        permissionSet = new FieldSet();
        permissionSet.setLayout(new FormLayout());
        permissionSet.setHeading("Permissions");

        visibilityCombo = new ComboBox<SimpleDataInstanceModel>();
        visibilityStore = new ChimeListStore<SimpleDataInstanceModel>();
        visibilityCombo.setDisplayField("name");
        visibilityCombo.setEditable(false);
        visibilityCombo.setStore(visibilityStore);
        visibilityCombo.setFieldLabel("Visible By");
        permissionSet.add(visibilityCombo);

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
        permissionSet.add(editableCombo);
        _form.add(permissionSet);

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
        setupVisibility();
    }

    protected void create() {
        EditShapeRequest req = new EditShapeRequest();
        createInstance(req);
    }

    protected void createInstance(final EditShapeRequest req) {
        ChimeAsyncCallback<ShapeResponseObject> callback = new ChimeAsyncCallback<ShapeResponseObject>() {
            public void onSuccess(ShapeResponseObject resp) {
                if (resp.isResponse()) {
                    String msg = "There is already a shape called '" + nameField.getValue().trim() +
                            "'.  Please choose another name";

                    ChimeMessageBox.alert("Create", msg, null);

                } else {
                    doCreate(req);
                }
            }
        };

        ShapeRequest request = new ShapeRequest();
        request.setName(nameField.getValue().trim());
        ServiceManager.getService().sendShapeRequest(request, callback);
    }

    protected void doCreate(EditShapeRequest req) {
        ChimeAsyncCallback<ServiceResponseObject<EditShapeResponse>> callback = 
        				new ChimeAsyncCallback<ServiceResponseObject<EditShapeResponse>>() {
            public void onSuccess(ServiceResponseObject<EditShapeResponse> resp) {
                if (resp.isResponse()) {
                    EditShapeResponse response = resp.getResponse();
                    DataInstance inst = response.getShape();
                    PageManager.instance().openNavigator(true, inst);
                    StateManager.instance().pushInactiveToken("detail:" + inst.getId());
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
            req.addScope(new Scope(new Community(c.getId()), Scope.Permission.RUC));
        } else {
            req.addScope(new Scope(new Community(c.getId()), Scope.Permission.R));
            req.addScope(new Scope(new Community(c2.getId()), Scope.Permission.RUC));
        }

        req.setName(nameField.getValue().trim());
        req.setDescription(descriptionField.getValue().trim());
        req.setOperation(EditShapeRequest.Operation.Create);
        req.setTabular(tabularField.getValue());

        ServiceManager.getService().sendEditDataTypeRequest(req, callback);
    }
    
    private void validate() {
        String name = nameField.getValue();
        boolean nameValid = name != null && name.trim().length() > 0;

        String desc = descriptionField.getValue();
        boolean descValid = desc != null && desc.trim().length() > 0;
        _okButton.setEnabled(nameValid && descValid);
    }

    protected void setupVisibility() {
        visibilityStore.removeAll();
        visibilityCombo.setRawValue("");

        User user = ServiceManager.getActiveUser();
        List<Community> communities = user.getCommunities();

        List<Community> results = new ArrayList<Community>();

        communities.add(0, Community.Global);

        for (Community comm : communities) {
            Community c = new Community(comm.getId());
            if (c.getId().equals(Community.Global.getId())) {
                c.setName(comm.getName());
                results.add(c);
            } else {
                c.setName("Members of " + comm.getName());
                results.add(c);
            }
        }

        Community justMe = new Community(user.getId());
        justMe.setName("Just Me");
        communities.add(0, justMe);

        for (Community community : communities) {
            SimpleDataInstanceModel model = new SimpleDataInstanceModel(community);
            visibilityStore.add(model);
        }

        visibilityCombo.setValue(visibilityStore.getAt(0));

        setupEditable();
    }

    protected void setupEditable() {
        Community visible = (Community)visibilityCombo.getSelection().get(0).getDataInstance();
        List<Scope> theScopes = new ArrayList<Scope>();
        theScopes.add(new Scope(visible, Scope.Permission.R));

        editableStore.removeAll();
        editableCombo.setRawValue("");

        List<Community> list = findCommunities(theScopes, false);
        for (Community community : list) {
            editableStore.add(new SimpleDataInstanceModel(community));
        }

        editableCombo.setValue(editableStore.getAt(0));

    }

    /**
     * Finds all of the communities that can be used to scope the new data
     * type.
     *
     * @return
     */
    private List<Community> findCommunities(List<Scope> theScopes, boolean allowAnonymous) {
        User me = ServiceManager.getActiveUser();
        List<Community> communities = me.getCommunities();
        //communities.add(Community.Anonymous);
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
        ServiceManager.removeListener(_serviceManagerListener);
        hide();
    }
    
}
