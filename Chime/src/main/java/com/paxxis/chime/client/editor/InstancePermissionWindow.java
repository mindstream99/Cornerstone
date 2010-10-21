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
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.paxxis.chime.client.ChimeAsyncCallback;
import com.paxxis.chime.client.ChimeListStore;
import com.paxxis.chime.client.LoginResponseObject;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceManagerListener;
import com.paxxis.chime.client.ServiceResponseObject;
import com.paxxis.chime.client.SimpleDataInstanceModel;
import com.paxxis.chime.client.common.Community;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.EditDataInstanceRequest;
import com.paxxis.chime.client.common.EditDataInstanceResponse;
import com.paxxis.chime.client.common.Scope;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.client.common.EditDataInstanceRequest.Operation;
import com.paxxis.chime.client.common.Scope.Permission;
import com.paxxis.chime.client.widgets.ChimeWindow;

/**
 *
 * @author Robert Englander
 */
public class InstancePermissionWindow extends ChimeWindow
{
    public interface InstancePermissionListener {
        public void onPermissionChanged(DataInstance instance);
    }

    private ButtonBar _buttonBar;
    private Button _okButton;
    private Button _cancelButton;
    private FormPanel _form = new FormPanel();
    private DataInstance dataInstance = null;
    private List<Scope> currentScopes;
    private Scope currentScope;
    private Scope currentEditScope;
    private ComboBox<SimpleDataInstanceModel> visibilityCombo;
    private ChimeListStore<SimpleDataInstanceModel> visibilityStore;
    private ComboBox<SimpleDataInstanceModel> editableCombo;
    private ChimeListStore<SimpleDataInstanceModel> editableStore;
    private FieldSet visibilitySet;
    private ServiceManagerListener _serviceManagerListener = null;
    private InstancePermissionListener permissionListener;

    public InstancePermissionWindow(final DataInstance inst, InstancePermissionListener listener) {
        super();
        dataInstance = inst;

        currentScope = getReadScope(inst.getSocialContext().getScopes());
        User user = ServiceManager.getActiveUser();

        Community limitingCommunity = getLimitingScope(inst.getShapes());
        
        currentScopes = new ArrayList<Scope>();
        if (currentScope.isGlobalCommunity()) {
        	currentScopes.add(new Scope(Community.Global, Permission.R));
        } else {
            if (!currentScope.getCommunity().getId().equals(user.getId())) {
            	// currently scoped to a community
                List<Community> communities = user.getCommunities();
                for (Community community : communities) {
                	if (isValidScope(community, limitingCommunity)) {
                    	currentScopes.add(new Scope(community, Permission.R));
                	}
                }
                
                if (isValidScope(Community.Global, limitingCommunity)) {
                	currentScopes.add(new Scope(Community.Global, Permission.R));
                }
            } else {
            	// currently scoped to the user only
            	currentScopes.add(new Scope(new Community(user.getId()), Permission.R));
            	List<Community> communities = user.getCommunities();
                for (Community community : communities) {
                	if (isValidScope(community, limitingCommunity)) {
                    	currentScopes.add(new Scope(community, Permission.R));
                	}
                }

                if (isValidScope(Community.Global, limitingCommunity)) {
                	currentScopes.add(new Scope(Community.Global, Permission.R));
                }
            }
        }

        // there should be only 1 scope with update permission.  If that turns out not to be true, then
        // the code that writes the permissions is wrong and must be fixed.
        
        currentEditScope = getEditScope(inst.getSocialContext().getScopes());

        permissionListener = listener;

        setHeading("Modify Permissions");
        setModal(true);
        setMaximizable(false);
        setMinimizable(false);
        setCollapsible(false);
        setClosable(true);
        setResizable(false);
        setWidth(400);
        
    }

    private Community getLimitingScope(List<Shape> types) {
    	Community result = Community.Global;
    	User user = ServiceManager.getActiveUser();
    	
    	for (Shape type : types) {
    		List<Scope> typeScopes = type.getSocialContext().getScopes();
    		Scope scope = getReadScope(typeScopes);
    		if (scope.getCommunity().getId().equals(user.getId())) {
    			return scope.getCommunity();
    		} else if (!scope.getCommunity().getId().equals(Community.Global.getId())) {
    			if (result.getId().equals(Community.Global.getId())) {
    				result = scope.getCommunity();
    			}
    		}
    	}
    	
    	return result;
    }
    
    private boolean isValidScope(Community community, Community limitingCommunity) {
    	boolean valid = true;
    	User user = ServiceManager.getActiveUser();
    	if (limitingCommunity.getId().equals(user.getId())) {
    		if (!community.getId().equals(user.getId())) {
    			valid = false;
    		}
    	} else if (!limitingCommunity.getId().equals(Community.Global.getId())) {
    		// the community can't be global
    		if (community.getId().equals(Community.Global.getId())) {
    			valid = false;
    		}
    	}
    	
    	return valid;
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
                        validate();
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
        editableCombo.addSelectionChangedListener(

                new SelectionChangedListener<SimpleDataInstanceModel>() {
                        @Override
                        public void selectionChanged(SelectionChangedEvent evt) {
                            validate();
                        }
                }
            );

        visibilitySet.add(editableCombo);

        _form.add(visibilitySet);

        _buttonBar = new ButtonBar();
        _buttonBar.setAlignment(HorizontalAlignment.CENTER);

        _okButton = new Button("Ok");
        _okButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                    update();
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
        
        setupVisibility();
        validate();
    }

    protected void update() {
        ChimeAsyncCallback<ServiceResponseObject<EditDataInstanceResponse>> callback = 
        	            new ChimeAsyncCallback<ServiceResponseObject<EditDataInstanceResponse>>() {
            public void onSuccess(ServiceResponseObject<EditDataInstanceResponse> resp) {
                if (resp.isResponse()) {
                    EditDataInstanceResponse response = resp.getResponse();
                    DataInstance inst = response.getDataInstance();
                    if (permissionListener != null) {
                        permissionListener.onPermissionChanged(inst);
                    }
                    doCancel();
                } else {
                    MessageBox.alert("Error", resp.getError().getMessage(), null);
                }
            }
        };

        EditDataInstanceRequest req = new EditDataInstanceRequest();
        req.setUser(ServiceManager.getActiveUser());
        req.setDataInstance(dataInstance);
        req.setOperation(Operation.ModifyScopes);

        DataInstance c = visibilityCombo.getSelection().get(0).getDataInstance();
        DataInstance c2 = editableCombo.getSelection().get(0).getDataInstance();
        if (c.getId().equals(c2.getId())) {
            req.addScope(new Scope(new Community(c.getId()), Scope.Permission.RU));
        } else {
            req.addScope(new Scope(new Community(c.getId()), Scope.Permission.R));
            req.addScope(new Scope(new Community(c2.getId()), Scope.Permission.RU));
        }

        ServiceManager.getService().sendEditDataInstanceRequest(req, callback);
    }

    private void validate() {
        DataInstance c = visibilityCombo.getSelection().get(0).getDataInstance();
        DataInstance c2 = editableCombo.getSelection().get(0).getDataInstance();
        boolean valid = (!c.getId().equals(currentScope.getCommunity().getId()) ||
        		           !c2.getId().equals(currentEditScope.getCommunity().getId()));
        _okButton.setEnabled(valid);
    }

    protected void setupVisibility() {
        visibilityStore.removeAll();
        visibilityCombo.setRawValue("");

        visibilityCombo.setEnabled(true);
        List<Community> list = getCommunities(currentScopes, ServiceManager.getActiveUser(), true, false);
        for (Community community : list) {
            visibilityStore.add(new SimpleDataInstanceModel(community));
        }

        SimpleDataInstanceModel model = visibilityStore.findModel("id", currentScope.getCommunity().getId().getValue());
        visibilityCombo.setValue(model);
        setupEditable();
    }

    protected void setupEditable() {
    	
        Community visible = (Community)visibilityCombo.getSelection().get(0).getDataInstance();
        List<Scope> theScopes = new ArrayList<Scope>();

        editableStore.removeAll();
        editableCombo.setRawValue("");
        editableCombo.setEnabled(true);

        boolean addCommunityPrefix = true;
        
        User user = ServiceManager.getActiveUser();
        if (visible.getId().equals(user.getId())) {
        	// private visibility
        	theScopes.add(new Scope(new Community(user.getId()), Scope.Permission.RU));
        } else if (!visible.getId().equals(Community.Global.getId())) {
        	// single community visibility
        	theScopes.add(new Scope(new Community(user.getId()), Scope.Permission.RU));
        	
        	if (visible.getName().startsWith("Moderator")) {
            	theScopes.add(new Scope(new Community(visible.getId(), visible.getName()), Scope.Permission.RM));
        	} else {
            	theScopes.add(new Scope(new Community(visible.getId(), visible.getName()), Scope.Permission.RU));
        	}

        	addCommunityPrefix = false;
        } else {
        	// global visibility
        	theScopes.add(new Scope(new Community(user.getId()), Scope.Permission.RU));

        	for (Community comm : user.getCommunities()) {
            	theScopes.add(new Scope(comm, Scope.Permission.RU));
            }

            for (Community comm : user.getModeratedCommunities()) {
            	theScopes.add(new Scope(comm, Scope.Permission.RM));
            }

            theScopes.add(new Scope(Community.Global, Scope.Permission.RU));
        }

        boolean includeModerators = dataInstance instanceof Community;
        List<Community> comms = getCommunities(theScopes, user, addCommunityPrefix, includeModerators);
        for (Community comm : comms) {
        	editableStore.add(new SimpleDataInstanceModel(comm));
        }
        
        SimpleDataInstanceModel model = editableStore.findModel("id", currentEditScope.getCommunity().getId().getValue());
        editableCombo.setValue(model);
    }

    private Scope getReadScope(List<Scope> scopes) {
    	Scope result = null;
    	for (Scope scope : scopes) {
    		if (scope.getPermission() == Scope.Permission.R) {
    			result = scope;
    			break;
    		}
    	}

    	if (result == null) {
        	for (Scope scope : scopes) {
        		if (scope.getPermission() == Scope.Permission.RU || scope.getPermission() == Scope.Permission.RUC) {
        			result = scope;
        			break;
        		}
        	}
    	}
    	
    	return result;
    }
    
    private Scope getEditScope(List<Scope> scopes) {
    	Scope result = null;
    	for (Scope scope : scopes) {
    		if (scope.getPermission() == Scope.Permission.RU || scope.getPermission() == Scope.Permission.RUC ||
    				scope.getPermission() == Scope.Permission.RM) {
    			result = scope;
    			break;
    		}
    	}
    	
    	return result;
    }
    
    /**
     * Converts a list of scopes to a list of communities
     *
     * @return
     */
    private List<Community> getCommunities(List<Scope> theScopes, User user, boolean addCommunityPrefix,
    		boolean includeModerators) {
        List<Community> results = new ArrayList<Community>();

        // we want to add global last
        boolean addGlobal = false;
        for (Scope scope : theScopes) {
            Community c = new Community(scope.getCommunity().getId());
            if (c.getId().equals(user.getId())) {
                c.setName("Just Me");
                results.add(0, c);
            } else if (!scope.isGlobalCommunity()) {
            	if (addCommunityPrefix) {
                	c.setName("Members of " + scope.getCommunity().getName());
            	} else {
                	c.setName(scope.getCommunity().getName());
            	}

            	if (includeModerators) {
                	if (addCommunityPrefix) {
                    	c.setName("Moderators of " + scope.getCommunity().getName());
                	} else {
                    	c.setName(scope.getCommunity().getName());
                	}
            	}
            	
            	results.add(c);
            } else {
            	addGlobal = true;
            }
        }

        if (addGlobal) {
        	results.add(Community.Global);
        }
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
