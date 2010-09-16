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

import java.util.List;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.paxxis.chime.client.DataInstanceResponseObject;
import com.paxxis.chime.client.FilledColumnLayoutData;
import com.paxxis.chime.client.SearchProvider;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.Utils;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceRequest;
import com.paxxis.chime.client.common.DataInstanceResponse;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.NamedSearch;
import com.paxxis.chime.client.common.DataInstanceRequest.Depth;
import com.paxxis.chime.client.pages.PageManager;
import com.paxxis.chime.client.widgets.ChimeTreePanel.InterceptedLinkListener;

/**
 *
 * @author Robert Englander
 */
public class SimpleSearchPanel extends Navigator {

    private SearchProvider _searchProvider;
    private String recentKeywords = "";
    private NamedSearch recentNamedSearch = null;
    private ToolButton refreshButton;
    private LayoutContainer htmlPanel;
    private InterceptedHtml activeHtml;
    
    public SimpleSearchPanel(SearchProvider provider) {
    	super("", true);
        _searchProvider = provider;
    }

    public void onResize(int width, int height) {
    	updateActiveLayout();
    }
    
    public void onRender(Element parent, int pos) {
    	super.onRender(parent, pos);
    	init(); 
    }

    protected void init() {
    	super.init();

        refreshButton = new ToolButton("x-tool-refresh", 
                new SelectionListener<IconButtonEvent>() {
            @Override  
             public void componentSelected(IconButtonEvent ce) {
                refreshQuery();
             }  
        });
 
        
        treePanel.setLinkListener( 
        	new InterceptedLinkListener() {
        		public boolean onLink(String id) {
        			openSearch(InstanceId.create(id));
        			return true;
        		}
        	}
        );

        ToolBar bar = new ToolBar();
        bar.setStyleAttribute("background", "transparent");
        bar.setBorders(false);
        bar.add(refreshButton);
         
        upperContainer.add(bar, new FilledColumnLayoutData(25));

        htmlPanel = new LayoutContainer();
        htmlPanel.setLayout(new RowLayout());

        activeHtml = new InterceptedHtml();
        htmlPanel.add(activeHtml, new RowData(1, -1));
        upperContainer.add(htmlPanel, new FilledColumnLayoutData());

        setActiveHtml(null);
    }

    public void setSearch(NamedSearch search) {
        recentNamedSearch = search;
        recentKeywords = "";
        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    PageManager.instance().openSearch(recentNamedSearch.getSearchCriteria(), false);
                	String txt = "Active Named Search: " + Utils.toHoverUrl(recentNamedSearch);
                	setActiveHtml(txt);
                }
            }
        );
    }
    
    private void openSearch(InstanceId id) {

        final AsyncCallback callback = new AsyncCallback() {
            public void onSuccess(final Object result)
            {
                DataInstanceResponseObject resp = (DataInstanceResponseObject)result;
                if (resp.isResponse())
                {
                    final DataInstanceResponse response = resp.getResponse();
                    List<DataInstance> instances = response.getDataInstances();
                    if (instances.size() > 0)
                    {
                        recentNamedSearch = (NamedSearch)instances.get(0);
                        recentKeywords = "";
                        DeferredCommand.addCommand(
                            new Command()
                            {
                                public void execute()
                                {
                                    PageManager.instance().openSearch(recentNamedSearch.getSearchCriteria(), false);
                                	String txt = "Active Named Search: " + Utils.toHoverUrl(recentNamedSearch);
                                	setActiveHtml(txt);
                                }
                            }
                        );
                    }
                }
            }

            public void onFailure(Throwable caught)
            {
            }
        };

        DataInstanceRequest req = new DataInstanceRequest();
        req.setDepth(Depth.Deep);
        req.setIds(id);
        req.setUser(ServiceManager.getActiveUser());
        ServiceManager.getService().sendDataInstanceRequest(req, callback);
    }

    public void refreshQuery() {
    	if (recentNamedSearch != null) {
    		_searchProvider.onSearchRequest(recentNamedSearch.getSearchCriteria());
    	} else {
            _searchProvider.onSearchRequest(recentKeywords);
    	}
    }
    
    public void setActiveHtml(String msg) {
    	boolean visible = true;
    	if (msg == null || msg.trim().length() == 0) {
        	activeHtml.setHtml("");
        	visible = false;
    	} else {
        	activeHtml.setHtml(msg);
    	}
    	
    	upperContainer.setVisible(visible);
    	ruleHtml.setVisible(visible);
    	
    	if (visible) {
        	DeferredCommand.addCommand(
        		new Command() {
        			public void execute() {
        				updateActiveLayout();
        			}
        		}
        	);
    	}
    }
    
    private void updateActiveLayout() {
    	DeferredCommand.addCommand(
    		new Command() {
    			public void execute() {
    				upperContainer.layout(true);
    			}
    		}
    	);
    }
    
    public void setKeywords(String keywords) {
    	recentNamedSearch = null;
    	String txt = null;
    	if (keywords == null) {
    		recentKeywords = "";
    	} else {
    		recentKeywords = keywords;
        	txt = "Active Keyword Search: <i>" + keywords + "</i>";
    	}

    	refreshQuery();
    	setActiveHtml(txt);
    }
}
