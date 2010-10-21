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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.paxxis.chime.client.ChimeAsyncCallback;
import com.paxxis.chime.client.DataInstanceResponseObject;
import com.paxxis.chime.client.FilledColumnLayout;
import com.paxxis.chime.client.LoginResponseObject;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceManagerListener;
import com.paxxis.chime.client.UserContext;
import com.paxxis.chime.client.Utils;
import com.paxxis.chime.client.UserContext.UserContextListener;
import com.paxxis.chime.client.common.Community;
import com.paxxis.chime.client.common.Dashboard;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceRequest;
import com.paxxis.chime.client.common.DataInstanceResponse;
import com.paxxis.chime.client.common.Folder;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.client.common.DataInstanceRequest.Depth;

/**
 * 
 * @author Robert Englander
 *
 */
public abstract class Navigator extends ContentPanel {
    public class ContentItemModel extends BaseTreeModel implements Serializable {

		private static final long serialVersionUID = 1L;

		private String iconName = "page-icon";
        private boolean isFolder;
        private boolean pending = false;

        public ContentItemModel() {
        }

        public ContentItemModel(String name, InstanceId id, boolean folder) {
        	this(name, id.getValue(), folder);
        }
        
        public ContentItemModel(String name, String id, boolean folder) {
            set("name", name);
            set("id", id);
            isFolder = folder;
            if (isFolder) {
                iconName = null;
            }
        }

        public ContentItemModel(String name) {
            this(name, InstanceId.UNKNOWN.getValue(), true);
        }

        public void setPending(boolean val) {
            pending = val;
        }

        public boolean isPending() {
            return pending;
        }

        public void setIconName(String name) {
            iconName = name;
        }

        public String getIconName() {
            return iconName;
        }

        public boolean isFolder() {
            return isFolder;
        }

        public String getName() {
            return (String)get("name");
        }

        public String getId() {
            return get("id");
        }

        @Override
        public String toString() {
            return getName();
        }

    }

    class PagesProxy extends RpcProxy<List<ContentItemModel>> {
        public void load(Object config, AsyncCallback<List<ContentItemModel>> callback) {
            List<ContentItemModel> children = new ArrayList<ContentItemModel>();
            ContentItemModel parent = (ContentItemModel)config;
            if (parent == null) {
            	if (!isSearch) {
                    children.add(new ContentItemModel(Utils.toHoverUrl(Dashboard.USERDEFAULT, "Chime Home"), Dashboard.USERDEFAULT, false));
                    children.add(new ContentItemModel(Utils.toHoverUrl(Dashboard.ABOUT, "About Chime"), Dashboard.ABOUT, false));
                    children.add(new ContentItemModel(Utils.toHoverUrl(Dashboard.HELP, "Chime Help"), Dashboard.HELP, false));
            	}

                if (ServiceManager.isLoggedIn()) {
                    userFolder = new ContentItemModel("My Favorites");
                    children.add(userFolder);

                    User user = ServiceManager.getActiveUser();
                    List<Community> communities = user.getCommunities();
                    communityFolders.clear();
                    for (Community community : communities) {
                        ContentItemModel folder = new ContentItemModel(community.getName() + " Favorites", community.getId(), true);
                        folder.setPending(true);
                        children.add(folder);
                        communityFolders.add(folder);
                    }
                }

                callback.onSuccess(children);

            } else {
                if (parent.isFolder()) {
                    if (!parent.getName().equals("My Favorites")) {
                        getChildPages(parent, callback);
                    } else {
                    	boolean expand = treePanel.isExpanded(parent);
                        if (ServiceManager.isLoggedIn()) {
                            User user = ServiceManager.getActiveUser();
                            List<DataInstance> pages = user.getFavorites();
                            for (DataInstance page : pages) {
                            	boolean use = true;
                            	String link = "";
                            	if (isSearch) {
                            		if (page.getShapes().get(0).getId().equals(Shape.NAMEDSEARCH_ID)) {
                            			link = Utils.toHoverSearchUrl(page.getId(), page.getName());
                            		} else if (page.getShapes().get(0).getId().equals(Shape.FOLDER_ID)) {
                            			link = Utils.toHoverUrl(page.getId(), page.getName());
                            		} else {
                            			use = false;
                            		}
                            	} else {
                            		link = Utils.toHoverUrl(page);
                            	}
                            	
                            	if (use) {
                                    children.add(new ContentItemModel(link, page.getId(), 
                                    		page.getShapes().get(0).getId().equals(Shape.FOLDER_ID)));
                            	}
                            }

                        }
                        callback.onSuccess(children);
                        if (expand) {
                            treePanel.setExpanded(parent, true);
                        }
                    }
                } else {
                    callback.onSuccess(children);
                }
            }
        }

        private void getChildPages(final ContentItemModel model, final AsyncCallback<List<ContentItemModel>> cb) {
        	final boolean expand = treePanel.isExpanded(model);
            final ChimeAsyncCallback<DataInstanceResponseObject> callback = 
            		new ChimeAsyncCallback<DataInstanceResponseObject>() {
                public void onSuccess(DataInstanceResponseObject resp) {
                    if (resp.isResponse()) {
                        final DataInstanceResponse response = resp.getResponse();
                        List<DataInstance> instances = response.getDataInstances();
                        if (instances.size() > 0) {
                            List<ContentItemModel> children = new ArrayList<ContentItemModel>();
                            final DataInstance dataInstance = instances.get(0);
                            if (dataInstance instanceof Folder) {
                            	Folder folder = (Folder)dataInstance;
                                List<DataInstance> pages = folder.getChildren();
                                for (DataInstance page : pages) {
                                	boolean use = true;
                                	String link = "";
                                	if (isSearch) {
                                		if (page.getShapes().get(0).getId().equals(Shape.NAMEDSEARCH_ID)) {
                                			link = Utils.toHoverSearchUrl(page.getId(), page.getName());
                                		} else {
                                			use = false;
                                		}
                                	} else {
                                		link = Utils.toHoverUrl(page);
                                	}
                                	
                                	if (use) {
                                        children.add(new ContentItemModel(link, page.getId(), 
                                        		page.getShapes().get(0).getId().equals(Shape.FOLDER_ID)));
                                	}
                                }
                            } else  if (dataInstance instanceof Community){
                                final Community community = (Community)instances.get(0);
                                List<DataInstance> pages = community.getFavorites();
                                for (DataInstance page : pages) {
                                	boolean use = true;
                                	String link = "";
                                	if (isSearch) {
                                		if (page.getShapes().get(0).getId().equals(Shape.NAMEDSEARCH_ID)) {
                                			link = Utils.toHoverSearchUrl(page.getId(), page.getName());
                                		} else {
                                			use = false;
                                		}
                                	} else {
                                		link = Utils.toHoverUrl(page);
                                	}
                                	
                                	if (use) {
                                        children.add(new ContentItemModel(link, page.getId(), 
                                        		page.getShapes().get(0).getId().equals(Shape.FOLDER_ID)));
                                	}
                                }
                            }

                            cb.onSuccess(children);
                            
                            if (expand) {
                                treePanel.setExpanded(model, true);
                            }
                        }

                        model.setPending(false);
                    }
                    else
                    {
                        cb.onFailure(new Exception(resp.getError().getMessage()));
                    }
                }
            };

            DataInstanceRequest req = new DataInstanceRequest();
            req.setDepth(Depth.Deep);
            req.setIds(InstanceId.create(model.getId()));
            req.setUser(ServiceManager.getActiveUser());
            ServiceManager.getService().sendDataInstanceRequest(req, callback);
        }
    }

    protected ChimeTreePanel<ContentItemModel> treePanel;
    protected TreeStore<ContentItemModel> store;
    protected TreeLoader<ContentItemModel> loader;
    protected InterceptedHtml ruleHtml;

    protected ContentItemModel userFolder = null;
    protected List<ContentItemModel> communityFolders = new ArrayList<ContentItemModel>();
    protected boolean isSearch;
    private String title;
    
    protected LayoutContainer upperContainer;

    protected Navigator(String title, boolean search) {
    	super();
        isSearch = search;
        this.title = title;
        setHeaderVisible(!isSearch);
        setBorders(false);
        setBodyBorder(false);
    }
    
    protected void init() {
        if (!isSearch) {
            setHeading(title);
        }

        setLayout(new RowLayout());
        setScrollMode(Scroll.AUTOY);

        loader = new BaseTreeLoader<ContentItemModel>(new PagesProxy()) {
            @Override
            public boolean hasChildren(ContentItemModel parent) {
                return parent.isFolder();
            }
        };

        store = new TreeStore<ContentItemModel>(loader);
        String urlType = (isSearch ? "search" : "detail");

        upperContainer = new LayoutContainer();
        upperContainer.setLayout(new FilledColumnLayout(HorizontalAlignment.LEFT));
        upperContainer.setStyleAttribute("font", "normal 12px arial, tahoma, sans-serif");
        add(upperContainer, new RowData(1, -1, new Margins(7, 4, 4, 4)));
        upperContainer.setVisible(false);
        
        ruleHtml = new InterceptedHtml();
        ruleHtml.setHtml("<hr COLOR=\"black\"/>");
        add(ruleHtml, new RowData(1, -1, new Margins(4)));
        ruleHtml.setVisible(false);
        
        treePanel = new ChimeTreePanel<ContentItemModel>(urlType, store, true);
        treePanel.setAutoLoad(false);
        treePanel.setDisplayProperty("name");
        add(treePanel, new RowData(1, -1));

        ServiceManager.addListener(
            new ServiceManagerListener() {

                public void onLoginResponse(LoginResponseObject resp) {
                    update(null);
                }

                public void onLogout() {
                    update(null);
                }

                public void onDataInstanceUpdated(DataInstance instance) {
                }

            }
        );

        UserContext.addListener(
            new UserContextListener() {
                public void onPortalPageChange(Community community) {
                	if (community == null) {
                        update(userFolder);
                	} else {
                		for (ContentItemModel folder : communityFolders) {
                			if (folder.getId().equals(community.getId().getValue())) {
                				if (!folder.isPending()) {
                    				update(folder);
                				}

                				break;
                			}
                		}
                	}
                }

                public void onSearchFavoritesChange() {
				}
            }
        );

        loader.load(null);
    }

    protected void update(final ContentItemModel model) {
        treePanel.getSelectionModel().deselectAll();
        DeferredCommand.addCommand(
            new Command() {
                public void execute() {
                    loader.loadChildren(model);
                }
            }
        );
    }
}
