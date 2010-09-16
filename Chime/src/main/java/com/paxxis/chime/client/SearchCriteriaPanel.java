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

package com.paxxis.chime.client;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.TabPanel.TabPosition;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.paxxis.chime.client.common.NamedSearch;
import com.paxxis.chime.client.common.SearchCriteria;
import com.paxxis.chime.client.widgets.AdvancedSearchPanel;
import com.paxxis.chime.client.widgets.ChimeLayoutContainer;
import com.paxxis.chime.client.widgets.SimpleSearchPanel;

/**
 *
 * @author Robert Englander
 */
class SearchCriteriaPanel extends ChimeLayoutContainer
{
    private TabPanel _tabPanel;
    private AdvancedSearchPanel _advancedPanel;
    private SimpleSearchPanel _simplePanel;
    private TabItem _simpleItem;
    private TabItem _advancedItem;
    private SearchProvider _searchProvider;
    private boolean advancedEditor;
    private boolean ready = false;
    private boolean blockRefresh = false;
    
    public SearchCriteriaPanel(SearchProvider provider, boolean advancedEditor)
    {
        this.advancedEditor = advancedEditor;
        ready = !advancedEditor;
        _searchProvider = provider;
        
        ServiceManager.addListener(
        	new ServiceManagerAdapter() {
        		public void onLogout() {
        			_advancedPanel.setCriteria(new SearchCriteria());
        			_simplePanel.setKeywords(null);
        		}
        	}
        );
    }

    public SearchCriteria getCriteria() {
        if (advancedEditor) {
            return _advancedPanel.getCriteria();
        } else {
            return null;
        }
    }

    public void setCriteria(final SearchCriteria criteria, final boolean showAdvanced)
    {
    	Runnable r = new Runnable() {
    		public void run() {
    			ready = true;
    	        if (showAdvanced || advancedEditor) {
    	            _tabPanel.setSelection(_advancedItem);
    	            _advancedPanel.setCriteria(criteria);
    	        } else {
    	            _searchProvider.onSearchRequest(criteria);
    	        }
    		}
    	};
    	
    	if (isRendered()) {
    		r.run();
    	} else {
    		addPostRenderRunnable(r);
    	}
    }

    public void setSearch(final NamedSearch search)
    {
    	Runnable r = new Runnable() {
    		public void run() {
    	        _tabPanel.setSelection(_simpleItem);
    			_simplePanel.setSearch(search);
    		}
    	};
    	
    	if (isRendered()) {
    		r.run();
    	} else {
    		addPostRenderRunnable(r);
    	}
    }

    public void setKeywords(final String keywords) {
    	if (_tabPanel.getSelectedItem() != _simpleItem) {
    		blockRefresh = true;
            _tabPanel.setSelection(_simpleItem);
    		DeferredCommand.addCommand(
    			new Command() {
    				public void execute() {
    		        	_simplePanel.setKeywords(keywords);
    				}
    			}
    		);
    	} else {
        	_simplePanel.setKeywords(keywords);
    	}
    }
    
    @Override
    protected void init()
    {
        setLayout(new FitLayout());
        
        _tabPanel = new TabPanel();
        _tabPanel.setTabScroll(true);
        _tabPanel.setBorders(false);
        _tabPanel.setBodyBorder(false);
        _tabPanel.setTabPosition(TabPosition.TOP);
        _tabPanel.addListener(Events.Select,
            new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent evt) {
                	if (ready) {
                    	if (_tabPanel.getSelectedItem() == _simpleItem) {
                    		if (!blockRefresh) {
                	            _simplePanel.refreshQuery();
                    		} else {
                    			blockRefresh = false;
                    		}
                    	} else {
                    		_advancedPanel.refreshQuery();
                    	}
                	}
                }
    		}
        );

        _advancedPanel = new AdvancedSearchPanel(_searchProvider, advancedEditor);

        if (!advancedEditor) {
            _simplePanel = new SimpleSearchPanel(_searchProvider);
            TabItem tabItem = new TabItem();
            tabItem.setLayout(new FitLayout());
            tabItem.setText("Simple");
            tabItem.setClosable(false);
            tabItem.add(_simplePanel);
            _simpleItem = tabItem;
            _tabPanel.add(tabItem);
        }

        TabItem tabItem = new TabItem();
        tabItem.setLayout(new FitLayout());
        tabItem.setText("Advanced");
        tabItem.setClosable(false);
        tabItem.add(_advancedPanel);
        _advancedItem = tabItem;
        _tabPanel.add(tabItem);

        add(_tabPanel);
    }
    
}
