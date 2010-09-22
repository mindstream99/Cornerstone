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
import java.util.List;

import com.extjs.gxt.ui.client.core.XTemplate;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.fx.FxConfig;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.RowExpander;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.paxxis.chime.client.InstanceUpdateListener;
import com.paxxis.chime.client.Paginator;
import com.paxxis.chime.client.PaginatorContainer;
import com.paxxis.chime.client.PagingListener;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceResponseObject;
import com.paxxis.chime.client.UserMessageModel;
import com.paxxis.chime.client.common.Cursor;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.client.common.UserMessage;
import com.paxxis.chime.client.common.UserMessagesBundle;
import com.paxxis.chime.client.common.UserMessagesRequest;
import com.paxxis.chime.client.common.UserMessagesResponse;
import com.paxxis.chime.client.common.portal.PortletSpecification;
import com.paxxis.chime.client.pages.PageManager;
import com.paxxis.chime.client.pages.PortalViewPage;
import com.paxxis.chime.client.widgets.ChimeMessageBox;
import com.paxxis.chime.client.widgets.ChimeMessageBoxEvent;

/**
 * 
 * @author Robert Englander
 *
 */
public class UserMessagesPortlet extends PortletContainer {

	private ToolButton refreshButton = null;
	private ToolButton toolsButton = null;
    private User user = null;
    private LayoutContainer container;
    private String titleText = "Messages";
    private Grid<UserMessageModel> messageGrid;
    private ListStore<UserMessageModel> listStore;
    
    private ToolBar toolBar;
    private Button deleteSelected;
    private Button deleteAll;
    private Button collapseAll;
    private Button expandAll;
    private RowExpander expander;

    private boolean isStale = false;
    
    private Paginator paginator;
    
    public UserMessagesPortlet(PortletSpecification spec, InstanceUpdateListener listener) {
        super(spec, HeaderType.Shaded, true);
    }
	
    protected void init() {
    	super.init();
        LayoutContainer lc = getBody();

        lc.setLayout(new RowLayout());
        refreshButton = new ToolButton("x-tool-refresh");
        refreshButton.addSelectionListener(
            new SelectionListener<IconButtonEvent>() {
                @Override
                public void componentSelected(IconButtonEvent evt) {
			        Cursor cursor = paginator.getCursor();
			        cursor.prepareFirst();
			        query(cursor);
                }
            }
        );

        toolsButton = new ToolButton("x-tool-save");
        toolsButton.addSelectionListener(
            new SelectionListener<IconButtonEvent>() {
                @Override
                public void componentSelected(IconButtonEvent evt) {
                	if (toolBar.isVisible()) {
                		toolBar.setVisible(false);
                	} else {
                		toolBar.setVisible(true);
                	}
                }
            }
        );

        //addHeaderItem(toolsButton);
        addHeaderItem(refreshButton);

    	PortletSpecification spec = getSpecification();
    	if (spec != null) {
	    	Object obj = getSpecification().getProperty("title");
	    	if (obj != null) {
	    		titleText = obj.toString();
	    	}
    	}

    	setHeading(titleText);
        
    	toolBar = new ToolBar();
    	//toolBar.setVisible(false);
    	
    	//ButtonGroup group = new ButtonGroup(2);
    	//group.setHeading("Delete");
    	
    	deleteSelected = new Button("Selected", IconHelper.createStyle("delete-icon"));  
    	//group.add(deleteSelected);
        deleteSelected.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                    deleteSelected();
                }
            }
        );
    	
    	deleteAll = new Button("All", IconHelper.createStyle("delete-icon"));  
    	//group.add(deleteAll);
        deleteAll.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                    deleteAll();
                }
            }
        );

    	//toolBar.add(group);
        
    	//group = new ButtonGroup(2);
    	//group.setHeading("Sort/Filter");
    	
    	//Button btn = new Button("Sort", IconHelper.createStyle("sort-icon"));  
    	//group.add(btn);
    	//toolBar.add(btn);
    	
    	//btn = new Button("Filter", IconHelper.createStyle("filter-icon"));  
    	//group.add(btn);
    	//toolBar.add(btn);
    	
    	//toolBar.add(group);

    	//group = new ButtonGroup(2);
    	//group.setHeading("View");
    	
    	collapseAll = new Button("Collapse All", IconHelper.createStyle("minus-icon"));  
    	//group.add(collapseAll);
        collapseAll.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                    viewAll(false);
                }
            }
        );

    	expandAll = new Button("Expand All", IconHelper.createStyle("plus-icon"));  
    	//group.add(expandAll);
        expandAll.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                    viewAll(true);
                }
            }
        );

    	//toolBar.add(group);
    	
    	toolBar.add(expandAll);
    	toolBar.add(collapseAll);
    	toolBar.add(new SeparatorToolItem());
    	toolBar.add(deleteSelected);
    	toolBar.add(deleteAll);

    	lc.add(toolBar, new RowData(1, -1));
    	
    	container = new LayoutContainer();
        container.setLayout(new FitLayout());
        container.setHeight(250);
        lc.add(container, new RowData(1, -1));

        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        XTemplate tpl = XTemplate.create("<p><br> {" + 
        		UserMessageModel.BODY + "}</p><br>&nbsp;");
        
        expander = new RowExpander();
        expander.setTemplate(tpl);
        configs.add(expander);
      
        ColumnConfig column = new ColumnConfig();
        column.setId(UserMessageModel.SUBJECT);
        column.setHeader("Subject");
        column.setWidth(300);
        column.setSortable(false);
        column.setMenuDisabled(true);
        configs.add(column);
        
        column = new ColumnConfig();
        column.setId(UserMessageModel.TIMESTAMP);
        column.setHeader("Time");
        column.setWidth(300);
        column.setSortable(false);
        column.setMenuDisabled(true);
        configs.add(column);
        
        ColumnModel cm = new ColumnModel(configs);
        
        listStore = new ListStore<UserMessageModel>();
        messageGrid = new Grid<UserMessageModel>(listStore, cm);
        messageGrid.addPlugin(expander);
        messageGrid.getView().setAutoFill(true);
        container.add(messageGrid);
        
        messageGrid.getSelectionModel().addSelectionChangedListener(
        	new SelectionChangedListener<UserMessageModel>() {
				@Override
				public void selectionChanged(SelectionChangedEvent<UserMessageModel> se) {
					updateToolbatState();
				}
        	}
        );
        
        paginator = new Paginator(
        	new PagingListener() {

				@Override
				public void onFirst() {
			        Cursor cursor = paginator.getCursor();
			        cursor.prepareFirst();
			        query(cursor);
				}

				@Override
				public void onLast() {
			        Cursor cursor = paginator.getCursor();
			        cursor.prepareLast();
			        query(cursor);
				}

				@Override
				public void onNext() {
			        Cursor cursor = paginator.getCursor();
			        cursor.prepareNext();
			        query(cursor);
				}

				@Override
				public void onPrevious() {
			        Cursor cursor = paginator.getCursor();
			        cursor.preparePrevious();
			        query(cursor);
				}

				@Override
				public void onRefresh() {
				}
        		
        	}
        );

        PaginatorContainer paginatorContainer = new PaginatorContainer(paginator);
        lc.add(paginatorContainer, new RowData(1, -1));

        Timer t = new Timer() {
            @Override
            public void run() {
                if (isStale) {
                    refreshButton.el().blink(FxConfig.NONE);
                }
            }
        };

        t.scheduleRepeating(10000);

        layout();
        updateToolbatState();
    }
    
    public void setStale() {
        isStale = true;
        refreshButton.removeStyleName("x-tool");
        refreshButton.addStyleName("x-redtool");
        refreshButton.changeStyle("x-redtool-refresh");
        refreshButton.el().blink(FxConfig.NONE);
    }

    protected void viewAll(boolean expanded) {
    	int cnt = listStore.getCount();
    	for (int i = 0; i < cnt; i++) {
    		if (expanded) {
    			expander.expandRow(i);
    		} else {
    			expander.collapseRow(i);
    		}
    	}
    }
    
    protected void query(Cursor cursor) {
    
    	AsyncCallback<ServiceResponseObject<UserMessagesResponse>> cb = new AsyncCallback<ServiceResponseObject<UserMessagesResponse>>() {
			@Override
			public void onFailure(Throwable arg0) {
			}

			@Override
			public void onSuccess(ServiceResponseObject<UserMessagesResponse> response) {
				if (response.isResponse()) {
					User u = response.getResponse().getUser();
					User activeUser = ServiceManager.getActiveUser();
					if (u.getId().equals(activeUser.getId())) {
						ServiceManager.updateActiveUser(u);
						PortalViewPage navPage = PageManager.instance().getActiveNavigatorPage();
						navPage.replaceDataInstance(u);
					}
					
					setUser(u, UpdateReason.Silent);
				} else {
					
				}
			}
    	};
    	
    	UserMessagesRequest req = new UserMessagesRequest();
    	req.setType(UserMessagesRequest.Type.Query);
    	req.setCursor(cursor);
    	req.setDataInstance(user);
    	req.setUser(ServiceManager.getActiveUser());
    	ServiceManager.getService().sendUserMessagesRequest(req, cb);
    }
    
    protected void deleteAll() {
        String msg = "Delete all of your messages?";
        delete(msg, null);
    }
    
    protected void deleteSelected() {
        String msg = "Delete the selected messages?";
        List<UserMessage> list = null;
        List<UserMessageModel> models = messageGrid.getSelectionModel().getSelectedItems();
        if (models.size() > 0) {
        	list = new ArrayList<UserMessage>();
        	for (UserMessageModel model : models) {
        		list.add(model.getuserMessage());
        	}
        }
        
        delete(msg, list);
    }

    protected void delete(String msg, final List<UserMessage> list) {

        final Listener<ChimeMessageBoxEvent> l = new Listener<ChimeMessageBoxEvent>() {
            public void handleEvent(ChimeMessageBoxEvent evt) {
                Button btn = evt.getButtonClicked();
                if (btn != null) {
                    if (btn.getText().equalsIgnoreCase("yes")) {
                    	executeDelete(list);
                    }
                }
            }
        };

        ChimeMessageBox.confirm("Delete", msg, l);

    }

    private void executeDelete(List<UserMessage> list) {
        AsyncCallback<ServiceResponseObject<UserMessagesResponse>> cb = new AsyncCallback<ServiceResponseObject<UserMessagesResponse>>() {
			@Override
			public void onFailure(Throwable arg0) {
			}

			@Override
			public void onSuccess(ServiceResponseObject<UserMessagesResponse> response) {
				if (response.isResponse()) {
					User u = response.getResponse().getUser();
					User activeUser = ServiceManager.getActiveUser();
					if (u.getId().equals(activeUser.getId())) {
						ServiceManager.updateActiveUser(u);
					}
					
					setUser(u, UpdateReason.Silent);
				} else {
					
				}
			}
    	};
    	
    	UserMessagesRequest req = new UserMessagesRequest();
    	req.setType(UserMessagesRequest.Type.Delete);
    	if (list != null) {
        	req.setDeleteList(list);
    	}
    	req.setDataInstance(user);
    	req.setUser(ServiceManager.getActiveUser());
    	ServiceManager.getService().sendUserMessagesRequest(req, cb);
    }
    
    protected void updateToolbatState() {
    
    	if (user != null) {
        	int total = user.getUserMessagesBundle().getCursor().getTotal();

        	// this renders some of the other setEnabled calls unnecessary, but
        	// it's worth leaving them in place in case we decide we don't want the
        	// toolbar itself being hidden.
        	toolBar.setVisible(total > 0);
        	
        	deleteAll.setEnabled(total > 0);
        	
        	int selCount = messageGrid.getSelectionModel().getSelectedItems().size();
        	deleteSelected.setEnabled(selCount > 0);
        	
        	int cnt = listStore.getCount();
        	expandAll.setEnabled(cnt > 0);
        	collapseAll.setEnabled(cnt > 0);
    	}
    }
    
    public void setUser(final User instance, final UpdateReason reason) {
    	Runnable r = new Runnable() {
    		public void run() {
    	    	user = instance;
    	    	isStale = false;
    	        refreshButton.removeStyleName("x-redtool");
    	        refreshButton.addStyleName("x-tool");
    	        refreshButton.changeStyle("x-tool-refresh");
    	    	
    	    	UserMessagesBundle bundle = user.getUserMessagesBundle();
    	    	paginator.setCursor(bundle.getCursor());
    	    	
    	    	//String txt = titleText + " (" + bundle.getCursor().getTotal() + ")";
    	    	//setHeading(txt);

    	    	listStore.removeAll();
    	    	for (UserMessage msg : bundle.getMessages()) {
        	        listStore.add(new UserMessageModel(msg));
    	    	}
    	    	listStore.commitChanges();
    	    	updateToolbatState();
    		}
    	};
    	
    	if (isRendered()) {
    		r.run();
    	} else {
    		addPostRenderRunnable(r);
    	}
    }
}
