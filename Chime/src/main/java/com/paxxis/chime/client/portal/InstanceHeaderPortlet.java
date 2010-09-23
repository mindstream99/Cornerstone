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

package com.paxxis.chime.client.portal;

import java.util.Date;

import com.extjs.gxt.ui.client.core.Template;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.fx.FxConfig;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.paxxis.chime.client.InstanceUpdateListener;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceResponseObject;
import com.paxxis.chime.client.Utils;
import com.paxxis.chime.client.InstanceUpdateListener.Type;
import com.paxxis.chime.client.common.ApplyVoteRequest;
import com.paxxis.chime.client.common.ApplyVoteResponse;
import com.paxxis.chime.client.common.BackReferencingDataInstance;
import com.paxxis.chime.client.common.Dashboard;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.Tag;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.client.common.portal.PortalTemplate;
import com.paxxis.chime.client.common.portal.PortletSpecification;
import com.paxxis.chime.client.editor.AppliedTypesEditListener;
import com.paxxis.chime.client.editor.FieldEditorListener;
import com.paxxis.chime.client.editor.InstancePermissionWindow;
import com.paxxis.chime.client.editor.LayoutEditorWindow;
import com.paxxis.chime.client.editor.MultiReferenceEditorWindow;
import com.paxxis.chime.client.editor.SingleLineTextEditorWindow;
import com.paxxis.chime.client.editor.TextEditorWindow;
import com.paxxis.chime.client.widgets.ChimeMessageBox;
import com.paxxis.chime.client.widgets.InterceptedHtml;
import com.paxxis.chime.client.widgets.LockPanel;
import com.paxxis.chime.client.widgets.PasswordWindow;
import com.paxxis.chime.client.widgets.SubscribePanel;
import com.paxxis.chime.client.widgets.UsefulVoterPanel;
import com.paxxis.chime.client.widgets.PasswordWindow.PasswordChangeListener;

/**
 *
 * @author Robert Englander
 */
public class InstanceHeaderPortlet extends PortletContainer
{
    private static final String VOTEPROMPT = "Voting on data instances is an indication that you are familiar with, or have a vested interest in, this " +
            "kind of data.  Please don't vote only because you like or dislike the data, or because you have no interest in it.<br><br>Would you like to place your vote now?";
    
    private static final String TEMPLATE = "<div id='data-header'>"  +
        "<span id='data-header-msg'>{byline}{backRef}{types}</span></div>";

    private static final String DESCTEMPLATE = "<div id='data-header'>"  +
        "<span id='data-header-msg'>{desc}</span></div>";

    private static Template _template;
    private static Template _descTemplate;

    static
    {
        _template = new Template(TEMPLATE);
        _template.compile();

        _descTemplate = new Template(DESCTEMPLATE);
        _descTemplate.compile();
    }

    private ToolButton refreshButton;
    private ToolButton actionsButton;
    private DataInstance _instance = null;
    private InterceptedHtml _header;
    private InterceptedHtml _description;
    private Menu actionMenu;
    private MenuItem addTypeMenuItem;
    private MenuItem editNameItem;
    private MenuItem changePasswordItem;

    private SeparatorMenuItem layoutSeparator;
    private MenuItem editLayoutItem;
    private SeparatorMenuItem permissionSeparator;
    private MenuItem permissionItem;
    private InstanceUpdateListener updateListener;
    private boolean showDescription = true;
    //private Button pageFavoriteButton = null;
    //private InterceptedHtml rule = null;
    //private boolean isFavorite = false;
    private boolean isStale = false;
    private LayoutContainer mainPanel;
    private LockPanel lockPanel;
    private SubscribePanel subscribePanel;
    //private FavoritePanel favoritePanel;
    
    public InstanceHeaderPortlet(PortletSpecification spec, HeaderType type, InstanceUpdateListener listener)
    {
        super(spec, HeaderType.Shaded, true);
        updateListener = listener;
    }

    public void setStale(DataInstance instance) {
        // we don't do anything with the updated pendingInstance.... yet
        isStale = true;
        refreshButton.removeStyleName("x-tool");
        refreshButton.addStyleName("x-redtool");
        refreshButton.changeStyle("x-redtool-refresh");
        refreshButton.el().blink(FxConfig.NONE);
    }
    
    public void setDataInstance(final DataInstance instance, final UpdateReason reason)
    {
    	Runnable r = new Runnable() {
    		public void run() {
    	        isStale = false;
    	        refreshButton.removeStyleName("x-redtool");
    	        refreshButton.addStyleName("x-tool");
    	        refreshButton.changeStyle("x-tool-refresh");

    	        String name = instance.getName();
    	        if (instance instanceof Tag) {
    	        	if (((Tag)instance).isPrivate()) {
    	        		name += " [Private]";
    	        	}
    	        }
    	        setHeading(name, "detail-header-name");
    	        _instance = instance;

    	        Params params = new Params();
    	        
    	        if (fullView()) {
        	        params.set("types", getTypes(_instance));
        	        params.set("backRef", getBackReference(_instance));
        	        params.set("byline", getByLine(_instance));
        	        params.set("usefulness", getUsefulness(_instance));
    	        }
    	        
    	        String content = _template.applyTemplate(params);
    	        _header.setHtml(content);

    	        if (showDescription) {
    	            params = new Params();
    	            params.set("desc", getDescription(_instance));

    	            content = _descTemplate.applyTemplate(params);
    	            _description.setHtml(content);
    	        }

    	        // anything can be a favorite
    	        boolean vis = true; //(instance instanceof Dashboard);
    	        //favoritePanel.setVisible(vis);

    	        subscribePanel.setDataInstance(instance);
    	        lockPanel.setDataInstance(instance);
    	        //favoritePanel.setDataInstance(instance);

    	        updateActions();
    		}
    	};
    	
    	if (isRendered()) {
    		r.run();
    	} else {
    		addPostRenderRunnable(r);
    	}
    }

    private boolean fullView() {
    	boolean full = true;
    	boolean isUserInstance = _instance.getShapes().get(0).getName().equals("User");
    		
    	if (isUserInstance) {
        	User user = ServiceManager.getActiveUser();
        	return user.isAdmin() || user.getId().equals(_instance.getId());
    	}
    	
    	return true;
    }
    
    private void updateActions() {
    	User user = ServiceManager.getActiveUser();
        boolean canEdit = _instance.canUpdate(user);
        actionsButton.setVisible(canEdit);

        addTypeMenuItem.setEnabled(_instance.getShapes().get(0).canMultiType());

        InstanceId typeId = _instance.getShapes().get(0).getId();
        editNameItem.setEnabled(!typeId.equals(Shape.USER_ID) && !typeId.equals(Shape.REVIEW_ID));

        boolean canLayout = canEdit && _instance instanceof Dashboard;
        layoutSeparator.setVisible(canLayout);
        editLayoutItem.setVisible(canLayout);

        boolean canUpdatePerms = _instance.canUpdatePermissions(user);
        permissionSeparator.setVisible(canUpdatePerms);
        permissionItem.setVisible(canUpdatePerms);

        boolean canChangePw = _instance.getShapes().get(0).getId().equals(Shape.USER_ID) && 
        		(ServiceManager.isAdminLoggedIn() || _instance.getId().equals(ServiceManager.getActiveUser().getId()));
        changePasswordItem.setVisible(canChangePw);
    }

    private void setupRefresh() {
        refreshButton.addSelectionListener(
                new SelectionListener<IconButtonEvent>() {
            @Override
             public void componentSelected(IconButtonEvent ce) {
                refreshInstance();
             }
        });

        //refreshButton.el().blink(FxConfig.NONE);
    }

    private void refreshInstance() {
        updateListener.onUpdate(_instance, Type.Refresh);
    }

    private void setupActionMenu() {
        actionMenu = new Menu();

        editNameItem = new MenuItem("Edit Name");
        actionMenu.add(editNameItem);
        editNameItem.addSelectionListener(
            new SelectionListener<MenuEvent>() {
                @Override
                public void componentSelected(MenuEvent ce) {
                    SingleLineTextEditorWindow w = new SingleLineTextEditorWindow(_instance.getName(),
                        new FieldEditorListener() {
                            public void onSave(DataField field, DataFieldValue value, String text) {
                                _instance.setName(text);
                                updateListener.onUpdate(_instance, Type.Name);
                            }
                        }
                    );

                    w.show();
                }
            }
        );

        MenuItem item = new MenuItem("Edit Description");
        actionMenu.add(item);
        item.addSelectionListener(
            new SelectionListener<MenuEvent>() {
                @Override
                public void componentSelected(MenuEvent ce) {
                    TextEditorWindow w = new TextEditorWindow("Edit Description", _instance.getDescription(),
                        new FieldEditorListener() {
                            public void onSave(DataField field, DataFieldValue value, String text) {
                                _instance.setDescription(text);
                                updateListener.onUpdate(_instance, Type.Description);
                            }
                        }
                    );

                    w.show();
                }
            }
        );

        changePasswordItem = new MenuItem("Change Password");
        actionMenu.add(changePasswordItem);
        changePasswordItem.addSelectionListener(
            new SelectionListener<MenuEvent>() {
                @Override
                public void componentSelected(MenuEvent ce) {
                	PasswordWindow w = new PasswordWindow((User)_instance,
                		new PasswordChangeListener() {

							public void onChange(User user) {
								updateListener.onUpdate(user, Type.Silent);
							}
                		
                		}
                	);
                    w.show();
                }
            }
        );

        actionMenu.add(new SeparatorMenuItem());

        addTypeMenuItem = new MenuItem("Apply Shapes");
        actionMenu.add(addTypeMenuItem);
        addTypeMenuItem.addSelectionListener(
            new SelectionListener<MenuEvent>() {
                @Override
                public void componentSelected(MenuEvent ce) {
                    MultiReferenceEditorWindow w = new MultiReferenceEditorWindow(_instance,
                        new AppliedTypesEditListener() {
                            public void onEdit(DataInstance instance) {
                                updateListener.onUpdate(_instance, Type.Type);
                            }
                        }
                    );

                    w.show();
                }
            }
        );

        permissionSeparator = new SeparatorMenuItem();
        actionMenu.add(permissionSeparator);

        permissionItem = new MenuItem("Update Permissions");
        actionMenu.add(permissionItem);
        permissionItem.addSelectionListener(
            new SelectionListener<MenuEvent>() {
                @Override
                public void componentSelected(MenuEvent ce) {
                    InstancePermissionWindow w = new InstancePermissionWindow(_instance,
                        new InstancePermissionWindow.InstancePermissionListener() {
                            public void onPermissionChanged(DataInstance instance) {
                            	updateListener.onUpdate(instance, Type.ScopeChange);
                            }
                        }
                    );

                    w.show();
                }
            }
        );

        layoutSeparator = new SeparatorMenuItem();
        actionMenu.add(layoutSeparator);

        final LayoutEditorWindow.LayoutEditorListener editListener = new LayoutEditorWindow.LayoutEditorListener() {
            public void onChange(PortalTemplate newTemplate) {
                updateListener.onUpdate(_instance, newTemplate);
            }
        };

        editLayoutItem = new MenuItem("Edit Layout");
        actionMenu.add(editLayoutItem);
        editLayoutItem.addSelectionListener(
            new SelectionListener<MenuEvent>() {
                @Override
                public void componentSelected(MenuEvent ce) {
                    LayoutEditorWindow w = new LayoutEditorWindow((Dashboard)_instance, editListener);
                    w.show();
                }
            }
        );

        actionsButton.addSelectionListener(
                new SelectionListener<IconButtonEvent>() {
            @Override
             public void componentSelected(IconButtonEvent ce) {
                actionMenu.show(actionsButton);
             }
        });

    }

    protected void init() {
    	super.init();
        Object val = getSpecification().getProperty("showDescription");
        if (val != null) {
            if (val.toString().trim().equals("false")) {
                showDescription = false;
            }
        }

        getBody().setStyleAttribute("backgroundColor", "#f1f1f1");

        mainPanel = new LayoutContainer();
        mainPanel.setLayout(new RowLayout());
        mainPanel.setStyleAttribute("backgroundColor", "transparent");
        
        getBody().setLayout(new ColumnLayout());
        getBody().add(mainPanel, new ColumnData(1.0));

        /*
        favoritePanel = new FavoritePanel(
            new FavoritePanel.FavoritePanelListener() {
                public void onFavorite(boolean fav) {
                    updateListener.onUpdateFavorite(_instance, fav);
                }
            }
        );

        addToolbarItem(favoritePanel, 30);
		*/
        
        subscribePanel = new SubscribePanel(
            new SubscribePanel.SubscribePanelListener() {
                public void onSubscribe(boolean subscribe) {
                    updateListener.onUpdateSubscription(_instance, subscribe);
                }
            }
        );

        addToolbarItem(subscribePanel, 30);
        
        lockPanel = new LockPanel(
            new LockPanel.LockPanelListener() {
                public void onLock(boolean lock) {
                    if (_instance.getLockType() == DataInstance.LockType.NONE) {
                        updateListener.onUpdateLock(_instance, DataInstance.LockType.EDIT);
                    } else {
                        updateListener.onUpdateLock(_instance, DataInstance.LockType.NONE);
                    }
                }
            }
        );

        addToolbarItem(lockPanel, 36);
        
        actionsButton = new ToolButton("x-tool-save");
        addHeaderItem(actionsButton);
        actionsButton.setVisible(false);
        setupActionMenu();

        refreshButton = new ToolButton("x-tool-refresh");
        addHeaderItem(refreshButton);
        setupRefresh();

        _header = new InterceptedHtml();
        
        if (true)
        {
            mainPanel.add(_header, new RowData(1, -1, new Margins(5, 5, 0, 5)));
        }
        else
        {
            mainPanel.add(_header, new RowData(1, -1));
        }

        UsefulVoterPanel.VoteListener listener = new UsefulVoterPanel.VoteListener() {
            public void onVote(boolean positive) {
                ApplyVoteRequest req = new ApplyVoteRequest();
                req.setData(_instance);
                req.setPositive(positive);
                sendVoteRequest(req);
            }
        };

        if (showDescription) {
            _description = new InterceptedHtml();
            if (true)
            {
                mainPanel.add(_description, new RowData(1, -1, new Margins(0, 5, 5, 5)));
            }
            else
            {
                mainPanel.add(_description, new RowData(1, -1));
            }
        }

        Timer t = new Timer() {
            @Override
            public void run() {
                if (isStale) {
                    refreshButton.el().blink(FxConfig.NONE);
                }
            }
        };

        t.scheduleRepeating(10000);
        PortalUtils.registerTimer(t);
    }

    public void sendVoteRequest(ApplyVoteRequest request)
    {
        final AsyncCallback callback = new AsyncCallback()
        {
            public void onFailure(Throwable arg0)
            {
                ChimeMessageBox.alert("System Error", "Please contact the system administrator.", null);
            }

            public void onSuccess(Object obj)
            {
                ServiceResponseObject<ApplyVoteResponse> response = (ServiceResponseObject<ApplyVoteResponse>)obj;
                if (response.isResponse())
                {
                    setDataInstance(response.getResponse().getDataInstance(), UpdateReason.InstanceChange);
                }
                else
                {
                    ChimeMessageBox.alert("Error", response.getError().getMessage(), null);
                }
            }
        };

        request.setUser(ServiceManager.getActiveUser());
        ServiceManager.getService().sendApplyVoteRequest(request, callback);
    }

    private String getBackReference(DataInstance instance) {
        StringBuffer buf = new StringBuffer();
        if (instance instanceof BackReferencingDataInstance) {
            BackReferencingDataInstance inst = (BackReferencingDataInstance)instance;
            buf.append("<br><b>Applied to:</b> " + Utils.toHoverUrl(inst.getBackRefId(), inst.getBackRefName()));
        }

        return buf.toString();
    }

    private String getDescription(DataInstance instance) {
    	if (fullView()) {
            return "<b>Description:</b> " + instance.getDescription();
    	} else {
            return instance.getDescription();
    	}
    }

    private String getTypes(DataInstance instance) {
        StringBuffer buf = new StringBuffer();

        String sep = "";
        for (Shape type : instance.getShapes()) {
            buf.append(sep + Utils.toHoverUrl(type));
            sep = "  ";
        }

        return "<br><b>Applied Shapes:</b> " + buf.toString();
    }

    private String getByLine(DataInstance instance)
    {
        String txt;
        Date created = instance.getCreated();
        Date updated = instance.getUpdated();
        if (created.equals(updated))
        {
            txt = "<b>Created by:</b> " + Utils.toHoverUrl(instance.getCreatedBy()) + " on " + created.toLocaleString();
        }
        else
        {
            txt = "<b>Updated by:</b> " + Utils.toHoverUrl(instance.getUpdatedBy()) + " on " + updated.toLocaleString();
        }

        Date expiration = instance.getExpiration();
        if (expiration != null) {
        	txt += "<br><b>Expires on:</b> " + expiration.toLocaleString();
        }
        
        return txt;
    }

    private String getUsefulness(DataInstance instance)
    {
        String s = "";

        int pos = instance.getPositiveCount();
        int neg = instance.getNegativeCount();
        int cnt = pos + neg;

        if (cnt == 1) {
            s = "<br>" + pos + " of " + cnt + " person found this data useful";
        } else if (cnt > 1) {
            s = "<br>" + pos + " of " + cnt + " people found this data useful";
        }

        return s;
    }
}
