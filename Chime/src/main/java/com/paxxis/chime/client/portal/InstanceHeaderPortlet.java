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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.extjs.gxt.ui.client.event.BoxComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.fx.FxConfig;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Timer;
import com.paxxis.chime.client.ChimeAsyncCallback;
import com.paxxis.chime.client.InstanceUpdateListener;
import com.paxxis.chime.client.InstanceUpdateListener.Type;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceResponseObject;
import com.paxxis.chime.client.Utils;
import com.paxxis.chime.client.common.ApplyVoteRequest;
import com.paxxis.chime.client.common.ApplyVoteResponse;
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
import com.paxxis.chime.client.widgets.ChimeGrid;
import com.paxxis.chime.client.widgets.ChimeMessageBox;
import com.paxxis.chime.client.widgets.FieldDataGridCellRenderer;
import com.paxxis.chime.client.widgets.LockPanel;
import com.paxxis.chime.client.widgets.PasswordWindow;
import com.paxxis.chime.client.widgets.PasswordWindow.PasswordChangeListener;
import com.paxxis.chime.client.widgets.SubscribePanel;

/**
 *
 * @author Robert Englander
 */
public class InstanceHeaderPortlet extends PortletContainer {
    private ToolButton refreshButton;
    private ToolButton actionsButton;
    private DataInstance _instance = null;
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
    private boolean isStale = false;
    private LockPanel lockPanel;
    private SubscribePanel subscribePanel;
    //private FavoritePanel favoritePanel;

    private ChimeGrid<DataRowModel> grid;
    private ListStore<DataRowModel> listStore;
    
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

    	        listStore.removeAll();
    	        if (fullView()) {
    	        	DataRowModel model = new DataRowModel(DataRowModel.NAME, "<b>Updated By:</b>");
    	        	model.set(DataRowModel.VALUE, getByLine(_instance));
    	        	listStore.add(model);

    	            Date expiration = instance.getExpiration();
    	            if (expiration != null) {
        	        	model = new DataRowModel(DataRowModel.NAME, "<b>Expires On:</b>");
        	        	model.set(DataRowModel.VALUE, expiration.toLocaleString());
        	        	listStore.add(model);
    	            }
    	            
    	            if (_instance instanceof Shape) {
    	            	if (!_instance.getId().equals(Shape.SHAPE_ID)) {
            	        	model = new DataRowModel(DataRowModel.NAME, "<b>Tabular:</b>");
            	        	String tabular = "No";
            	        	Shape s = (Shape)_instance;
            	        	if (s.isTabular()) {
            	        		tabular = "Yes";
            	        	}
            	        	
            	        	model.set(DataRowModel.VALUE, tabular);
            	            listStore.add(model);
    	            	}
    	            } else {
        	        	model = new DataRowModel(DataRowModel.NAME, "<b>Applied Shapes:</b>");
        	        	model.set(DataRowModel.VALUE, getTypes(_instance));
        	            listStore.add(model);
    	            }
        	        
    	        	if (_instance.isBackReferencing() && !(instance instanceof Shape)) {
        	        	model = new DataRowModel(DataRowModel.NAME, "<b>Parent:</b>");
        	        	model.set(DataRowModel.VALUE, getBackReference(_instance));
        	        	listStore.add(model);
    	        	}
    	        }
    	        
    	        if (showDescription) {
    	        	DataRowModel model = new DataRowModel(DataRowModel.NAME, "<b>Description:</b>");
    	        	model.set(DataRowModel.VALUE, getDescription(_instance));
    	        	listStore.add(model);
    	        }

    	        listStore.commitChanges();
    	        
    	        subscribePanel.setDataInstance(instance);
    	        lockPanel.setDataInstance(instance);
    	        //favoritePanel.setDataInstance(instance);

    	        updateActions();
    	        
    	        // I don't know why this is necessary, but for some reason the grid is not sizing its
    	        // width correctly.
    	        DeferredCommand.addCommand(
    	        	new Command() {
    	        		public void execute() {
    	        	    	grid.setWidth(getWidth());
    	                	grid.getView().refresh(false);
    	        		}
    	        	}
    	        );
    		}
    	};
    	
    	if (isRendered()) {
    		r.run();
    	} else {
    		addPostRenderRunnable(r);
    	}
    }

    private boolean fullView() {
    	boolean isUserInstance = _instance.getShapes().get(0).getId().equals(Shape.USER_ID);
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
        getBody().setLayout(new RowLayout());
        
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

        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        ColumnConfig column = new ColumnConfig();
        column.setId(DataRowModel.NAME);
        column.setFixed(true);
        column.setHeader("");
        column.setWidth(150);
        column.setSortable(false);
        column.setMenuDisabled(true);
        column.setRenderer(new FieldDataGridCellRenderer());
        configs.add(column);
        
        column = new ColumnConfig();
        column.setId(DataRowModel.VALUE);
        column.setHeader("");
        column.setWidth(300);
        column.setSortable(false);
        column.setMenuDisabled(true);
        column.setRenderer(new FieldDataGridCellRenderer());
        configs.add(column);
        
        ColumnModel cm = new ColumnModel(configs);
        
        listStore = new ListStore<DataRowModel>();
        grid = new ChimeGrid<DataRowModel>(listStore, cm);
        grid.getView().setAutoFill(true);
        grid.setSelectionModel(null);
        grid.getView().setForceFit(true);
        grid.setHideHeaders(true);
        grid.setTrackMouseOver(false);
        grid.setAutoHeight(true);
        grid.setAutoExpandColumn(DataRowModel.VALUE);
        
        getBody().add(grid, new RowData(1, -1, new Margins(0)));
                
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
        getBody().addListener(Events.Resize,
            new Listener<BoxComponentEvent>() {
                public void handleEvent(BoxComponentEvent evt) {
                	DeferredCommand.addCommand(
                		new Command() {
                			public void execute() {
                                getBody().layout();
                                grid.setWidth(getBody().getWidth());
                                grid.getView().refresh(false);
                			}
                		}
                	);
                }
            }
        );
    }

    public void sendVoteRequest(ApplyVoteRequest request) {
        final ChimeAsyncCallback<ServiceResponseObject<ApplyVoteResponse>> callback = 
        		new ChimeAsyncCallback<ServiceResponseObject<ApplyVoteResponse>>() {
            public void onSuccess(ServiceResponseObject<ApplyVoteResponse> response) {
                if (response.isResponse()) {
                    setDataInstance(response.getResponse().getDataInstance(), UpdateReason.InstanceChange);
                } else {
                    ChimeMessageBox.alert("Error", response.getError().getMessage(), null);
                }
            }
        };

        request.setUser(ServiceManager.getActiveUser());
        ServiceManager.getService().sendApplyVoteRequest(request, callback);
    }

    private String getBackReference(DataInstance instance) {
        StringBuffer buf = new StringBuffer();
        if (instance.isBackReferencing() && !(instance instanceof Shape)) {
            buf.append( Utils.toHoverUrl(instance.getBackRefId(), instance.getBackRefName()));
        }

        return buf.toString();
    }
 
    private String getDescription(DataInstance instance) {
        return instance.getDescription();
    }

    private String getTypes(DataInstance instance) {
        StringBuffer buf = new StringBuffer();

        String sep = "";
        for (Shape type : instance.getShapes()) {
            buf.append(sep + Utils.toHoverUrl(type));
            sep = "  ";
        }

        return buf.toString();
    }

    private String getByLine(DataInstance instance)
    {
        String txt;
        Date created = instance.getCreated();
        Date updated = instance.getUpdated();
        if (created.equals(updated))
        {
            txt = Utils.toHoverUrl(instance.getCreatedBy()) + " on " + created.toLocaleString();
        }
        else
        {
            txt = Utils.toHoverUrl(instance.getUpdatedBy()) + " on " + updated.toLocaleString();
        }
        return txt;
    }
}
