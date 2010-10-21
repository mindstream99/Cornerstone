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

package com.paxxis.chime.client.pages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.paxxis.chime.client.ChimeAsyncCallback;
import com.paxxis.chime.client.DataInstanceResponseObject;
import com.paxxis.chime.client.InstanceUpdateListener;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceResponseObject;
import com.paxxis.chime.client.UserContext;
import com.paxxis.chime.client.common.Community;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceRequest;
import com.paxxis.chime.client.common.EditDataInstanceRequest;
import com.paxxis.chime.client.common.EditDataInstanceResponse;
import com.paxxis.chime.client.common.EditNamedSearchRequest;
import com.paxxis.chime.client.common.EditPageTemplateRequest;
import com.paxxis.chime.client.common.EditShapeRequest;
import com.paxxis.chime.client.common.EditShapeResponse;
import com.paxxis.chime.client.common.ErrorMessage;
import com.paxxis.chime.client.common.FieldDefinition;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.LockRequest;
import com.paxxis.chime.client.common.LockResponse;
import com.paxxis.chime.client.common.MultiRequest;
import com.paxxis.chime.client.common.MultiResponse;
import com.paxxis.chime.client.common.NamedSearch;
import com.paxxis.chime.client.common.ResponseMessage;
import com.paxxis.chime.client.common.SearchCriteria;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.SubscribeRequest;
import com.paxxis.chime.client.common.SubscribeResponse;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.client.common.DataInstanceRequest.Depth;
import com.paxxis.chime.client.common.EditDataInstanceRequest.Operation;
import com.paxxis.chime.client.common.portal.PortalTemplate;
import com.paxxis.chime.client.pages.PageManager.StaticPageType;
import com.paxxis.chime.client.portal.InstanceHeaderPortlet;
import com.paxxis.chime.client.portal.PagePortlet;
import com.paxxis.chime.client.portal.PortalContainer;
import com.paxxis.chime.client.portal.PortalUtils;
import com.paxxis.chime.client.portal.UpdateReason;
import com.paxxis.chime.client.portal.UserMessagesPortlet;
import com.paxxis.chime.client.widgets.ChimeLayoutContainer;
import com.paxxis.chime.client.widgets.ChimeMessageBox;
import com.paxxis.chime.client.widgets.ChimeMessageBoxEvent;

/**
 *
 * @author Robert Englander
 */
public class DataDetailPanel extends ChimeLayoutContainer implements InstanceUpdateListener
{
    private enum ActivityLocation {
        Left,
        Bottom,
        Right
    }
    
    public class FieldDataHolder
    {
        private DataField _field;
        private Serializable _value;

        public FieldDataHolder(DataField field, Serializable value)
        {
            _field = field;
            _value = value;
        }

        public DataField getField()
        {
            return _field;
        }

        public Serializable getValue()
        {
            return _value;
        }
    }
    
    private DataInstance refreshInstance = null;
    private DataInstance _dataInstance = null;
    private DataInstance _origDataInstance = null;
    private ContentPanel resultPanel;
    private PortalContainer _portal = null;
    private int defaultMargin = 4;
    
    public DataDetailPanel() {
        this(4);
    }

    public DataDetailPanel(int margin) {
        defaultMargin = margin;
    }
    
    protected void init()
    {
        setBorders(false);
        setLayout(new BorderLayout());
        
        resultPanel = new ContentPanel();
        resultPanel.setHeaderVisible(false);
        resultPanel.setLayout(new FitLayout());
        resultPanel.setBorders(true);
        resultPanel.setBodyBorder(false);
        BorderLayoutData data = new BorderLayoutData(LayoutRegion.CENTER);
        data.setMinSize(100);
        data.setMaxSize(2000);
        data.setMargins(new Margins(defaultMargin));
        data.setSplit(true);
        add(resultPanel, data);
    }
     
    public void release()
    {
    }
    
    private void requery(final UpdateReason reason)
    {
        if (_dataInstance != null)
        {
            final ChimeAsyncCallback<DataInstanceResponseObject> callback = 
            			new ChimeAsyncCallback<DataInstanceResponseObject>() {
                public void onSuccess(DataInstanceResponseObject response) { 
                    DataInstance instance = null;
                    if (response.isResponse())
                    {
                        List<DataInstance> list = response.getResponse().getDataInstances();
                        if (list.size() == 1)
                        {
                            instance = list.get(0);
                        }
                    }

                    setDataInstance(instance, reason);
                }

            };

            DataInstanceRequest req = new DataInstanceRequest();
            req.setDepth(Depth.Deep);
            req.setIds(_dataInstance.getId());
            req.setUser(ServiceManager.getActiveUser());
            ServiceManager.getService().sendDataInstanceRequest(req, callback);
        }
    }
    
    public DataInstance getDataInstance()
    {
        return _dataInstance;
    }

    public void clearInstance() {
    	Runnable r = new Runnable() {
    		public void run() {
                _dataInstance = null;
                _origDataInstance = null;
                refreshInstance = null;
                _portal = null;
                resultPanel.removeAll();
                resultPanel.layout();
    		}
    	};
    	
    	if (isRendered()) {
    		r.run();
    	} else {
    		addPostRenderRunnable(r);
    	}
    }
    
    public boolean setDataInstance(DataInstance instance, UpdateReason reason) {

        refreshInstance = null;
        _dataInstance = instance;
        _origDataInstance = _dataInstance.copy();

        if (reason != UpdateReason.Silent) {
            if (!PortalUtils.updatePortal(_portal, instance, reason)) {
                _portal = PortalUtils.buildPortal(instance, this);

                DeferredCommand.addCommand(
                    new Command() {
                        public void execute() {
                            resultPanel.removeAll();
                            resultPanel.add(_portal);
                            resultPanel.layout();
                        }
                    }
                );
            }
        }

        return true;
    }

    public void onShow() {
    	super.onShow();
    	if (refreshInstance != null) {
    		compareForRefresh(refreshInstance);
    	}
    }
    
    public void compareForRefresh(DataInstance instance) {
    	try {
            if (_dataInstance.getId().equals(instance.getId())) {
            	if (instance.isGone()) {
            		informGone();
            	} else {
                	if (userMessagesUpdated(instance)) {
                    	if (isVisible()) {
                            UserMessagesPortlet portlet = _portal.getUserMessagesPortlet();
                            if (portlet != null) {
                                portlet.setStale();
                            }
                    	}
                	}
                	
                    if (instance.getUpdated().after(_dataInstance.getUpdated())) {
                    	if (isVisible()) {
                    		doRefresh(instance);
                    	} else {
                    		refreshInstance = instance;
                    	}
                    }
            	}
            }
    	} catch (Exception e) {
    		
    	}
    }

    /**
     * Informs the user that the current data instance is gone, and takes the user to his/her
     * home after the user acknowledges the message.
     */
    private void informGone() {
    	Listener<ChimeMessageBoxEvent> listener = new Listener<ChimeMessageBoxEvent>() {
			@Override
			public void handleEvent(ChimeMessageBoxEvent evt) {
                PageManager.instance().open(StaticPageType.Navigator, true);
			}
    	};

    	String msg = "The data you are viewing has been removed.";
    	ChimeMessageBox.alert("Chime", msg, listener);
    }
    
    private boolean userMessagesUpdated(DataInstance newInstance) {
    	boolean result = false;
    	
    	if (newInstance instanceof User) {
    		User newUser = (User)newInstance;
    		User oldUser = (User)_dataInstance;
    		Date newDate = newUser.getUserMessagesBundle().getLatestUpdate();
    		Date oldDate = oldUser.getUserMessagesBundle().getLatestUpdate();
    		if ( (oldDate == null && newDate != null) || (newDate == null && oldDate != null)) {
    			result = true;
    		} else if (oldDate != null && newDate != null) {
    			result = newDate.after(oldDate);
    		}
    	}
    	
    	return result;
    }
    
    private void doRefresh(DataInstance instance) {
    	refreshInstance = null;
    	if (instance.getPortalTemplate().isAutoUpdate()) {
    		setDataInstance(instance, UpdateReason.UpdateEvent);
    	} else {
            // the instance needs a refresh, so tell the header portlet about that
            InstanceHeaderPortlet portlet = _portal.getInstanceHeaderPortlet();
            if (portlet != null) {
                portlet.setStale(instance);
            } else {
                PagePortlet pagePortlet = _portal.getPagePortlet();
                pagePortlet.setStale(instance);
            }
    	}
    }
    
    public void onUpdateFavorite(DataInstance instance, boolean favorite) {

        ChimeAsyncCallback<ServiceResponseObject<EditDataInstanceResponse>> callback = 
        			new ChimeAsyncCallback<ServiceResponseObject<EditDataInstanceResponse>>() {
            public void onSuccess(ServiceResponseObject<EditDataInstanceResponse> response) {
                if (response.isResponse()) {
                    EditDataInstanceResponse resp = response.getResponse();

                    User userInst = (User)resp.getDataInstance();
                    ServiceManager.updateActiveUser(userInst);

                    setDataInstance(_dataInstance, UpdateReason.PrimaryChange);
                    UserContext.notifyPortalPageChange(null);
                } else {
                    ErrorMessage msg = response.getError();
                    processErrorMessage(msg);
                }
            }
        };

        User user = ServiceManager.getActiveUser();
        EditDataInstanceRequest req = new EditDataInstanceRequest();
        req.setDataInstance(user);
        req.setUser(user);
        DataField field = user.getShapes().get(0).getField("Favorites");
        DataFieldValue val = new DataFieldValue();

        if (favorite) {
            req.setOperation(Operation.AddFieldData);
            val.setReferenceId(instance.getId());
        } else {
            req.setOperation(Operation.DeleteFieldData);
            List<DataFieldValue> list = user.getFieldValues(user.getShapes().get(0), field);
            for (DataFieldValue value : list) {
                if (value.getReferenceId().equals(instance.getId())) {
                    val = value;
                    break;
                }
            }
        }

        req.addFieldData(user.getShapes().get(0), field, val);
        ServiceManager.getService().sendEditDataInstanceRequest(req, callback);
    }

    public void onUpdateLock(DataInstance instance, DataInstance.LockType lockType) {
        final ChimeAsyncCallback<ServiceResponseObject<LockResponse>> callback = 
        			new ChimeAsyncCallback<ServiceResponseObject<LockResponse>>() {
            public void onSuccess(ServiceResponseObject<LockResponse> response) {
                if (response.isResponse()) {
                    LockResponse resp = response.getResponse();

                    DataInstance inst = resp.getDataInstance();
                    _origDataInstance = inst.copy();
                    setDataInstance(inst, UpdateReason.PrimaryChange);
                } else {
                	ErrorMessage error = response.getError();
                	processErrorMessage(error);
                }
            }

        };

        LockRequest req = new LockRequest();
        req.setUser(ServiceManager.getActiveUser());
        req.setData(instance);
        req.setLockType(lockType);
        ServiceManager.getService().sendLockRequest(req, callback);
    }

    public void onUpdateSubscription(DataInstance instance, boolean subscribe) {
        final ChimeAsyncCallback<ServiceResponseObject<SubscribeResponse>> callback = 
        			new ChimeAsyncCallback<ServiceResponseObject<SubscribeResponse>>() {
            public void onSuccess(ServiceResponseObject<SubscribeResponse> response) {
                if (response.isResponse()) {
                    SubscribeResponse resp = response.getResponse();

                    DataInstance inst = resp.getDataInstance();
                    _origDataInstance = inst.copy();
                    setDataInstance(inst, UpdateReason.PrimaryChange);
                } else {
                	ErrorMessage error = response.getError();
                	processErrorMessage(error);
                }
            }

        };

        SubscribeRequest req = new SubscribeRequest();
        req.setUser(ServiceManager.getActiveUser());
        req.setData(instance);
        req.setSubscribe(subscribe);
        ServiceManager.getService().sendSubscribeRequest(req, callback);
    }

    public void onUpdate(Shape instance, DataField field, Type type) {
        switch (type) {
            case AddFieldDefinition:
                addFieldDefinition(instance, field);
                break;
        }
    }

    private void addFieldDefinition(Shape dataType, DataField field) {
        final ChimeAsyncCallback<ServiceResponseObject<EditShapeResponse>> callback = 
        			new ChimeAsyncCallback<ServiceResponseObject<EditShapeResponse>>() {
            public void onSuccess(ServiceResponseObject<EditShapeResponse> response) {
                if (response.isResponse()) {
                    EditShapeResponse resp = response.getResponse();

                    Shape inst = resp.getShape();
                    _origDataInstance = inst.copy();
                    setDataInstance(inst, UpdateReason.FieldChange);
                } else {
                	ErrorMessage error = response.getError();
                	processErrorMessage(error);
                }
            }

        };

        EditShapeRequest req = new EditShapeRequest();
        req.setUser(ServiceManager.getActiveUser());
        req.setId(dataType.getId());
        req.setOperation(EditShapeRequest.Operation.AddFields);

        FieldDefinition def = new FieldDefinition();
        def.name = field.getName();
        def.description = field.getDescription();
        def.typeName = field.getShape().getName();
        def.maxValues = field.getMaxValues();

        req.addFieldDefinition(def);

        ServiceManager.getService().sendEditDataTypeRequest(req, callback);
    }

    public void onUpdate(DataInstance instance, PortalTemplate template) {
        onUpdatePageLayout(instance, template);
    }

    public void onUpdate(NamedSearch instance, SearchCriteria criteria) {
        onUpdateNamedSearch(instance, criteria);
    }
    
    public void onUpdate(DataInstance instance, InstanceUpdateListener.Type changeType) {
        switch (changeType) {
            case FieldData:
                onUpdateFieldData(instance);
                break;
            case ImageAdd:
                onUpdatePrimaryData(instance, UpdateReason.ImageAdd);
                break;
            case ImageRemove:
                onUpdatePrimaryData(instance, UpdateReason.ImageDelete);
                break;
            case FileAdd:
                onUpdatePrimaryData(instance, UpdateReason.FileAdd);
                break;
            case FileRemove:
                onUpdatePrimaryData(instance, UpdateReason.FileDelete);
                break;
            case ReviewApplied:
            case CommentApplied:
            case DiscussionApplied:
            case TagApplied:
                resetInstance(instance, UpdateReason.PrimaryChange);
                break;
            case Name:
                onUpdatePrimaryData(instance, UpdateReason.PrimaryChange);
                break;
            case Description:
                onUpdatePrimaryData(instance, UpdateReason.PrimaryChange);
                break;
            case Type:
                onUpdateTypeData(instance);
                break;
            case Refresh:
                requery(UpdateReason.InstanceChange);
                break;
            case ScopeChange:
            	resetInstance(instance, UpdateReason.PrimaryChange);
            	break;
            case Silent:
            	onSilentChange(instance);
        }
    }

    private void onSilentChange(DataInstance instance) {
        _origDataInstance = instance.copy();
        setDataInstance(instance, UpdateReason.Silent);
    }
    
    private void resetInstance(DataInstance instance, UpdateReason reason) {
        _origDataInstance = instance.copy();
        setDataInstance(instance, reason);
    }

    private void onUpdateNamedSearch(NamedSearch instance, SearchCriteria criteria) {
        final ChimeAsyncCallback<ServiceResponseObject<EditDataInstanceResponse>> callback = 
        		new ChimeAsyncCallback<ServiceResponseObject<EditDataInstanceResponse>>() {
            public void onSuccess(ServiceResponseObject<EditDataInstanceResponse> response) {
                if (response.isResponse()) {
                    EditDataInstanceResponse resp = response.getResponse();
                    DataInstance inst = resp.getDataInstance(); 
                    resetInstance(inst, UpdateReason.FieldChange);
                } else {
                	ErrorMessage error = response.getError();
                	processErrorMessage(error);
                }
            }

        };

        EditNamedSearchRequest req = new EditNamedSearchRequest();
        req.setSearchCriteria(instance.getSearchCriteria());
        req.setDataInstance(instance);
        req.addShape(instance.getShapes().get(0));
        req.setOperation(Operation.ModifyFieldData);
        req.setUser(ServiceManager.getActiveUser());
        ServiceManager.getService().sendEditDataInstanceRequest(req, callback);
    }

    private void onUpdateTypeData(DataInstance instance) {
        final ChimeAsyncCallback<ServiceResponseObject<EditDataInstanceResponse>> callback = 
        			new ChimeAsyncCallback<ServiceResponseObject<EditDataInstanceResponse>>() {
            public void onSuccess(ServiceResponseObject<EditDataInstanceResponse> response) {
                if (response.isResponse()) {
                    EditDataInstanceResponse resp = response.getResponse();

                    DataInstance inst = resp.getDataInstance();
                    _origDataInstance = inst.copy();
                    setDataInstance(inst, UpdateReason.AppliedTypeChange);
                } else {
                    ErrorMessage msg = response.getError();
                    processErrorMessage(msg);
                }
            }

        };

        EditDataInstanceRequest req = new EditDataInstanceRequest();
        req.setDataInstance(instance);
        req.setOperation(Operation.UpdateTypes);
        req.setUser(ServiceManager.getActiveUser());
        ServiceManager.getService().sendEditDataInstanceRequest(req, callback);

    }

    private void onUpdatePageLayout(DataInstance instance, PortalTemplate template) {
        final ChimeAsyncCallback<ServiceResponseObject<EditDataInstanceResponse>> callback = 
        			new ChimeAsyncCallback<ServiceResponseObject<EditDataInstanceResponse>>() {
            public void onSuccess(ServiceResponseObject<EditDataInstanceResponse> response) {
                if (response.isResponse()) {
                    EditDataInstanceResponse resp = response.getResponse();

                    DataInstance inst = resp.getDataInstance();
                    _origDataInstance = inst.copy();
                    setDataInstance(inst, UpdateReason.InstanceChange);
                } else {
                	ErrorMessage error = response.getError();
                	processErrorMessage(error);
                }
            }

        };

        EditPageTemplateRequest req = new EditPageTemplateRequest();
        req.setTemplate(template);
        req.setDataInstance(instance);
        req.addShape(instance.getShapes().get(0));

        if (!template.getId().equals("-1")) {
            req.setOperation(Operation.ModifyFieldData);
        } else {
            req.setOperation(Operation.AddFieldData);

        }

        req.setUser(ServiceManager.getActiveUser());
        ServiceManager.getService().sendEditDataInstanceRequest(req, callback);

    }

    private void onUpdatePrimaryData(DataInstance instance, final UpdateReason reason) {
        final ChimeAsyncCallback<ServiceResponseObject<EditDataInstanceResponse>> callback = 
        			new ChimeAsyncCallback<ServiceResponseObject<EditDataInstanceResponse>>() {
            public void onSuccess(ServiceResponseObject<EditDataInstanceResponse> response) {
                if (response.isResponse()) {
                    EditDataInstanceResponse resp = response.getResponse();

                    DataInstance inst = resp.getDataInstance();
                    _origDataInstance = inst.copy();
                    setDataInstance(inst, reason);
                } else {
                    ErrorMessage msg = response.getError();
                    processErrorMessage(msg);
                }
            }

        };

        EditDataInstanceRequest req = new EditDataInstanceRequest();
        req.setDataInstance(instance);
        req.setOperation(Operation.UpdatePrimaryData);
        req.setUser(ServiceManager.getActiveUser());
        ServiceManager.getService().sendEditDataInstanceRequest(req, callback);

    }

    private void onUpdateFieldData(DataInstance instance) {
        // we need 3 request messages.  1 to add data, 1 to remove data, and
        // 1 to modify data.  we'll submit them all as an atomic unit using a multi request

        MultiRequest request = new MultiRequest();
        request.setUser(ServiceManager.getActiveUser());

        List<FieldDataHolder> addedPairs = new ArrayList<FieldDataHolder>();
        List<FieldDataHolder> removedPairs = new ArrayList<FieldDataHolder>();
        List<FieldDataHolder> modifiedPairs = new ArrayList<FieldDataHolder>();

        for (Shape dataType : _origDataInstance.getShapes()) {
            List<DataField> fields = dataType.getFields();

            for (DataField field : fields)
            {
                String fname = field.getName();
                List<DataFieldValue> newValues = _dataInstance.getFieldValues(dataType, field);

                DataField origField = dataType.getField(fname);
                List<DataFieldValue> origValues = _origDataInstance.getFieldValues(dataType, origField);

                List<DataFieldValue> added = additionalData(newValues, origValues);
                List<DataFieldValue> removed = additionalData(origValues, newValues);
                List<DataFieldValue> changed = changedData(newValues, origValues);

                for (DataFieldValue v : added)
                {
                    FieldDataHolder pair = new FieldDataHolder(field, v);
                    addedPairs.add(pair);
                }

                for (DataFieldValue v : removed)
                {
                    FieldDataHolder pair = new FieldDataHolder(field, v);
                    removedPairs.add(pair);
                }

                for (DataFieldValue v : changed)
                {
                    FieldDataHolder pair = new FieldDataHolder(field, v);
                    modifiedPairs.add(pair);
                }
            }

            if (addedPairs.size() > 0) {
                EditDataInstanceRequest addRequest = getEditDataInstanceRequest(_dataInstance, dataType,
                        addedPairs, EditDataInstanceRequest.Operation.AddFieldData);
                request.addRequest(addRequest);
            }

            if (removedPairs.size() > 0) {
                EditDataInstanceRequest removeRequest = getEditDataInstanceRequest(_dataInstance, dataType,
                        removedPairs, EditDataInstanceRequest.Operation.DeleteFieldData);
                request.addRequest(removeRequest);
            }

            if (modifiedPairs.size() > 0) {
                EditDataInstanceRequest modifyRequest = getEditDataInstanceRequest(_dataInstance, dataType,
                        modifiedPairs, EditDataInstanceRequest.Operation.ModifyFieldData);
                request.addRequest(modifyRequest);
            }
        }

        //String msg = "Are you sure you want to make these changes?";
        //if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, msg, "Chime", 
        //        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE))
        {

            final ChimeAsyncCallback<ServiceResponseObject<MultiResponse>> callback = 
            			new ChimeAsyncCallback<ServiceResponseObject<MultiResponse>>() {
                public void onSuccess(ServiceResponseObject<MultiResponse> response) { 
                    if (response.isResponse()) {
                        MultiResponse resp = response.getResponse();

                        // we want the last response
                        ResponseMessage rmsg = resp.getPairs().get(resp.getPairs().size() - 1).response;

                        if (rmsg instanceof EditDataInstanceResponse)
                        {
                            DataInstance inst = ((EditDataInstanceResponse)rmsg).getDataInstance();

                            checkUserChange(inst, _origDataInstance);
                            
                            _origDataInstance = inst.copy();
                            setDataInstance(inst, UpdateReason.FieldChange);
                        }
                    } else {
                        _dataInstance = _origDataInstance.copy();
                        ErrorMessage msg = response.getError();
                        processErrorMessage(msg);
                    }
                }

            };

            ServiceManager.getService().sendMultiRequest(request, callback);
        }
    }

    private void processErrorMessage(ErrorMessage msg) {
        String text;
        if (msg.getType() == ErrorMessage.Type.StaleDataEdit ||
        		msg.getType() == ErrorMessage.Type.LockedDataEdit) {
            requery(UpdateReason.InstanceChange);
            text = msg.getMessage() + "<br><br>The data has been refreshed to reflect its current state.";
        } else if (msg.getType() == ErrorMessage.Type.SessionExpiration) {
            ServiceManager.logout();
            text = "Your session has expired.  Please login again.";
        } else {
            text = msg.getMessage();
        }

        ChimeMessageBox.alert("Error", text, null);
    }
    
    private void checkUserChange(DataInstance newInstance, DataInstance oldInstance) {
        Shape newType = newInstance.getShapes().get(0);
        Shape oldType = oldInstance.getShapes().get(0);
        if (newType.getId().equals(Shape.USER_ID)) {

            ServiceManager.updateActiveUser((User)newInstance);
            
            DataField newField = newType.getField(User.FAVORITES_FIELD);
            DataField oldField = oldType.getField(User.FAVORITES_FIELD);
            List<DataFieldValue> newPages = newInstance.getFieldValues(newType, newField);
            List<DataFieldValue> oldPages = oldInstance.getFieldValues(oldType, oldField);

            boolean portalChange = false;
            if (newPages.size() != oldPages.size()) {
                portalChange = true;
            } else if (newPages.size() > 0) {
                for (DataFieldValue newVal : newPages) {
                    InstanceId newId = newVal.getReferenceId();
                    boolean found = false;
                    for (DataFieldValue oldVal : oldPages) {
                        InstanceId oldId = oldVal.getReferenceId();
                        if (oldId.equals(newId)) {
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        portalChange = true;
                        break;
                    }
                }
            }

            if (portalChange) {
                UserContext.notifyPortalPageChange(null);
            	UserContext.notifySearchFavoritesChange();
            }
        } else if (newType.getId().equals(Shape.FOLDER_ID)) {

        	List<DataInstance> searches = ServiceManager.getActiveUser().getFavorites();

        	for (DataInstance search : searches) {
        		if (search.getId().equals(newInstance.getId())) {
        			UserContext.notifySearchFavoritesChange();
        			break;
        		}
        	} 
        } else if (newType.getId().equals(Shape.COMMUNITY_ID)) {
            DataField newField = newType.getField(Community.FAVORITES_FIELD);
            DataField oldField = oldType.getField(Community.FAVORITES_FIELD);
            List<DataFieldValue> newPages = newInstance.getFieldValues(newType, newField);
            List<DataFieldValue> oldPages = oldInstance.getFieldValues(oldType, oldField);

            boolean portalChange = false;
            if (newPages.size() != oldPages.size()) {
                portalChange = true;
            } else if (newPages.size() > 0) {
                for (DataFieldValue newVal : newPages) {
                	InstanceId newId = newVal.getReferenceId();
                    boolean found = false;
                    for (DataFieldValue oldVal : oldPages) {
                    	InstanceId oldId = oldVal.getReferenceId();
                        if (oldId.equals(newId)) {
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        portalChange = true;
                        break;
                    }
                }
            }
            
            if (portalChange) {
            	// is the active user a member of this community?
            	List<Community> communities = ServiceManager.getActiveUser().getCommunities();

            	for (Community community : communities) {
            		if (community.getId().equals(newInstance.getId())) {
            			UserContext.notifyPortalPageChange(community);
            			break;
            		}
            	}
            }
        }
    }

    private EditDataInstanceRequest getEditDataInstanceRequest(DataInstance instance, Shape dataType, List<FieldDataHolder> properties,
            EditDataInstanceRequest.Operation op)
    {
        EditDataInstanceRequest request = new EditDataInstanceRequest();

        for (FieldDataHolder pair : properties)
        {
            request.addFieldData(dataType, pair.getField(), pair.getValue());
        }

        request.setUser(ServiceManager.getActiveUser());
        request.setDataInstance(instance);
        request.setOperation(op);
        request.addShape(dataType);
        request.setName(instance.getName());
        
        return request;
    }

    private List<DataFieldValue> additionalData(List<DataFieldValue> source, List<DataFieldValue> base)
    {
        List<DataFieldValue> values = new ArrayList<DataFieldValue>();
        for (DataFieldValue sourceValue : source)
        {
            boolean found = false;
            InstanceId sourceId = sourceValue.getId();
            for (DataFieldValue baseValue : base)
            {
            	InstanceId negOne = InstanceId.create("-1");
            	InstanceId baseId = baseValue.getId();
                if (!baseId.equals(negOne))
                {
                    if (sourceId.equals(negOne))
                    {
                        // this is, by definition, a new field
                        break;
                    }
                    else if (sourceId.equals(baseId))
                    {
                        found = true;
                        break;
                    }
                }
            }

            if (!found)
            {
                values.add(sourceValue);
            }
        }
        
        return values;
    }

    private List<DataFieldValue> changedData(List<DataFieldValue> source, List<DataFieldValue> base)
    {
        List<DataFieldValue> values = new ArrayList<DataFieldValue>();
        for (DataFieldValue sourceValue : source)
        {
            boolean found = false;
            if (sourceValue.isInternal())
            {
                String sourceVal = sourceValue.getValue().toString();
                InstanceId sourceId = sourceValue.getId();

                for (DataFieldValue baseValue : base)
                {
                    String baseVal = baseValue.getValue().toString();
                    InstanceId baseId = baseValue.getId();
                    if (!baseId.equals(InstanceId.create("-1")))
                    {
                        if (sourceId.equals(baseId) && !sourceVal.equals(baseVal))
                        {
                            found = true;
                            break;
                        }
                    }
                }
            }
            else
            {
            	InstanceId sourceRef = sourceValue.getReferenceId();
            	InstanceId sourceId = sourceValue.getId();

                for (DataFieldValue baseValue : base)
                {
                	InstanceId baseRef = baseValue.getReferenceId();
                	InstanceId baseId = baseValue.getId();
                    if (!baseId.equals(InstanceId.create("-1")))
                    {
                        if (sourceId.equals(baseId) && !sourceRef.equals(baseRef))
                        {
                            found = true;
                            break;
                        }
                    }
                }
            }
            
            if (found)
            {
                values.add(sourceValue);
            }
        }
        
        return values;
    }
    
}
