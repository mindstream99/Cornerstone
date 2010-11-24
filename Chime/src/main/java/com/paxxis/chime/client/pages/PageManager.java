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

import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.paxxis.chime.client.ActivityMonitor;
import com.paxxis.chime.client.ChimeAsyncCallback;
import com.paxxis.chime.client.DataInstanceResponseObject;
import com.paxxis.chime.client.LoginResponseObject;
import com.paxxis.chime.client.SearchPanel;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceManagerAdapter;
import com.paxxis.chime.client.StateManager;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceRequest;
import com.paxxis.chime.client.common.NamedSearch;
import com.paxxis.chime.client.common.SearchCriteria;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.DataInstanceRequest.Depth;
import com.paxxis.chime.client.common.DataInstanceResponse.Reason;
import com.paxxis.chime.client.widgets.LoginWindow;
import com.paxxis.cornerstone.base.ErrorMessage;
import com.paxxis.cornerstone.base.InstanceId;

/**
 *
 * @author Robert Englander
 */
public class PageManager extends TabPanel
{
    protected class PageTabItem extends TabItem
    {
        Page _page = null;
        StaticPageType _type = null;
        
        public void setPage(Page page)
        {
            // _page should be null.  should we do anything about it?
            _page = page;
            add(page);
        }
        
        public Page getPage()
        {
            return _page;
        }
        
        public StaticPageType getType()
        {
            return _type;
        }
        
        public void setTitle(StaticPageType type, String title)
        {
            String tabTitle = null;
            _type = type;
            
            switch (type)
            {
                case Search:
                    tabTitle = "Search";
                    break;
                case About:
                    tabTitle = "About";
                    break;
                case Help:
                    tabTitle = "Help";
                    break;
                case Navigator:
                    tabTitle = "Navigator";
                    break;
            }

            String tooltip = null;
            
            String newTitle = tabTitle;
            if (newTitle.length() > 30)
            {
                newTitle = newTitle.substring(0, 29) + "...";
            }

            tooltip = tabTitle;
            tabTitle = newTitle;
            
            setText(tabTitle);
            getHeader().setToolTip(tooltip);
        }
    }
    
    public enum StaticPageType
    {
        Navigator,
        Search,
        About,
        Help,
    }
    
    private HashMap<StaticPageType, PageTabItem> _pageMap = new HashMap<StaticPageType, PageTabItem>();
    private HashMap<PageTabItem, StaticPageType> _reversePageMap = new HashMap<PageTabItem, StaticPageType>();
    
    private static PageManager _instance = null;
    
    private Page _searchPage = null;
    //private Page _profilePage = null;
    
    public static PageManager instance()
    {
        if (_instance == null)
        {
            _instance = new PageManager();
        }
        
        return _instance;
    }
    
    private PageManager()
    {  
        init();
    }
    
    private void getInstance(final String typeId, final InstanceId instanceId, final boolean promptUser)
    {
        final ChimeAsyncCallback<DataInstanceResponseObject> callback = 
        		new ChimeAsyncCallback<DataInstanceResponseObject>() {
            public void onSuccess(DataInstanceResponseObject response) { 
                if (response.isResponse()) {
                    List<DataInstance> list = response.getResponse().getDataInstances();
                    if (list.size() == 1)
                    {
                        DataInstance instance = list.get(0);
                        PageManager.instance().openNavigator(false, instance);
                    }
                    else
                    {
                        if (promptUser) {
                            String msg;
                            if (response.getResponse().getReason() == Reason.NoSuchData) {
                                StateManager.instance().goBack("The requested data does not exist or has restricted access.");
                            } else if (response.getResponse().getReason() == Reason.NotVisible) {
                                if (ServiceManager.getActiveUser() == null) {
                                    msg = "The requested data is not visible to everyone.  If you log in you may be able to view this data.";

                                    Runnable r1 = new Runnable() {
                                        public void run() {
                                            getInstance(typeId, instanceId, false);
                                        }
                                    };

                                    Runnable r2 = new Runnable() {
                                        public void run() {
                                            // close the detail page
                                            //closeDetail();
                                            StateManager.instance().backInactive();
                                        }
                                    };

                                    LoginWindow w = new LoginWindow(msg, r1, r2);
                                    w.show();
                                } else {
                                    StateManager.instance().goBack("The requested data does not exist or has restricted access.");
                                }
                            }
                        } else {
                            StateManager.instance().backInactive();
                        }
                    }
                }
                else
                {
                    ErrorMessage error = response.getError();
                    if (error.getType() == ErrorMessage.Type.SessionExpiration)
                    {
                        ServiceManager.reLogin(
                            new Runnable()
                            {
                                public void run()
                                {
                                    getInstance(typeId, instanceId, false);
                                }
                            },
                            new Runnable()
                            {
                                public void run()
                                {
                                    StateManager.instance().backInactive();
                                }
                            }
                        );
                    }
                }
            }

        };
        
        DataInstanceRequest req = new DataInstanceRequest();
        req.setDepth(Depth.Deep);
        req.setIds(instanceId);
        req.setUser(ServiceManager.getActiveUser());
        ServiceManager.getService().sendDataInstanceRequest(req, callback);
    }

    private void init()
    {
        setTabScroll(true);
        setBorders(false);
        setBodyBorder(false);
        ServiceManager.addListener(
            new ServiceManagerAdapter()
            {
                public void onLoginResponse(LoginResponseObject resp) 
                {
                }

                public void onLogout() 
                {
                    closeSearch();
                }
            }
        );
        
        addListener(Events.Select,
            new Listener<ComponentEvent>()
            {
                public void handleEvent(ComponentEvent evt) 
                {
                    StaticPageType type = _reversePageMap.get(getSelectedItem());

                    switch (type)
                    {
                        case Search:
                            StateManager.instance().pushInactiveToken("search");
                            break;
                        case About:
                            StateManager.instance().pushInactiveToken("about");
                            break;
                        case Help:
                            StateManager.instance().pushInactiveToken("help");
                            break;
                        case Navigator:
                            final PageTabItem item = _pageMap.get(type);

                            DeferredCommand.addCommand(
                                new Command()
                                {
                                    public void execute()
                                    {
                                        PortalViewPage p = (PortalViewPage)item.getPage().getContent();
                                        DataInstance instance = p.getDataInstance();
                                        if (instance != null) {
                                            StateManager.instance().pushInactiveToken("detail:" + instance.getId());
                                        }
                                    }
                                }
                            );
                            break;
                    }
                }
            }
        );
    }

    /**
     * Get the active navigator panel
     *
     * @return
     */
    public PortalViewPage getActiveNavigatorPage() {

        PortalViewPage panel = null;

        PageTabItem item = _pageMap.get(StaticPageType.Navigator);
        if (item != null) {
            panel = (PortalViewPage)item.getPage().getContent();
        }

        return panel;
    }

    public DataInstance getActiveNavigatorInstance() {
        DataInstance instance = null;

        PageTabItem item = _pageMap.get(StaticPageType.Navigator);
        if (item != null) {
            PortalViewPage panel = (PortalViewPage)item.getPage().getContent();
            instance = panel.getDataInstance();
        }

        return instance;
    }

    public Page open(StaticPageType type, boolean bringForward)
    {
        Page page = null;
        
        switch (type)
        {
            case Navigator:
                page = openNavigator(bringForward);
                break;
            case Search:
                page = openSearch(bringForward);
                break;
            case About:
                page = openAbout(bringForward);
                break;
            case Help:
                page = openHelp(bringForward);
                break;
        }
        
        return page;
    }

    public void closeSearch()
    {
        ActivityMonitor.instance().setActive();

        PageTabItem item = _pageMap.remove(StaticPageType.Search);

        if (item != null)
        {
            _reversePageMap.remove(item);
            remove(item);
        }
    }

    public void clearHome() {
        final PortalViewPage panel;
        PageTabItem item = _pageMap.get(StaticPageType.Navigator);
        panel = (PortalViewPage)item.getPage().getContent();

        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    panel.clearPage();
                }
            }
        );

        item.setTitle(StaticPageType.Navigator, "&nbsp;&nbsp;&nbsp;");
    }
    
    public Page openNavigator(boolean bringForward, final DataInstance instance)
    {
        ActivityMonitor.instance().setActive();

        final PortalViewPage panel;
        PageTabItem item = _pageMap.get(StaticPageType.Navigator);
        //item.setIconStyle("page-icon");
        panel = (PortalViewPage)item.getPage().getContent();

        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    panel.buildPage(instance);
                }
            }
        );

        item.setTitle(StaticPageType.Navigator, instance.getName());

        if (bringForward)
        {
            setSelection(item);
        }

        return item.getPage();
    }

    public Page openNavigator(boolean bringForward) {
    	return openNavigator(bringForward, (String)null);
    }
    
    public Page openNavigator(boolean bringForward, String followToken)
    {
        ActivityMonitor.instance().setActive(); 
        
        PageTabItem item = _pageMap.get(StaticPageType.Navigator);
        if (item == null)
        {
            item = addPage(new Page(new PortalViewPage(), "Navigator", false, "globe-icon"), StaticPageType.Navigator);
        }
        else
        {
            ((PortalViewPage)item.getPage().getContent()).goHome(followToken);
            //item.setIconStyle("home-icon");
        }
        
        if (bringForward)
        {
            setSelection(item);
        }
        
        return item.getPage();
    }
    
    private String getIcon(DataInstance instance)
    {
        String icon = null;
        
        InstanceId type = instance.getShapes().get(0).getId();
        if (type.equals(Shape.TAG_ID))
        {
            icon = "tag-icon";
        }
        else if (type.equals(Shape.COMMUNITY_ID))
        {
            icon = "group-icon";
        }
        else if (type.equals(Shape.NAMEDSEARCH_ID))
        {
            icon = "search-icon";
        }
        else if (type.equals(Shape.DASHBOARD_ID))
        {
            icon = "page-icon";
        }
        else if (type.equals(Shape.USER_ID))
        {
            icon = "user-icon";
        }
        else
        {
            icon = "database-icon";
        }
        
        return icon;
    }
    
    public void scrollSearch()
    {
        PageTabItem item = _pageMap.get(StaticPageType.Search);
        SearchPanel p = (SearchPanel)item.getPage().getContent();
        p.doScroll();
    }

    public void openSearch(final String keywords) {
        final Page page = openSearch(true);
        
        DeferredCommand.addCommand(
        	new Command() {
        		public void execute() {
        	        SearchPanel panel = (SearchPanel)page.getContent();
        	        panel.setKeywords(keywords);
        		}
        	}
        );
    }

    public void openSearch(final NamedSearch search) {
        final Page page = openSearch(true);
        
        DeferredCommand.addCommand(
        	new Command() {
        		public void execute() {
        	        SearchPanel panel = (SearchPanel)page.getContent();
        	        panel.setSearch(search);
        		}
        	}
        );
    }
    
    public void openSearch(SearchCriteria criteria) {
        openSearch(criteria, true);
    }

    public void openSearch(final SearchCriteria criteria, final boolean showAdvanced)
    {
        final Page page = openSearch(true);
        
        DeferredCommand.addCommand(
        	new Command() {
        		public void execute() {
        	        SearchPanel panel = (SearchPanel)page.getContent();
        	        panel.setCriteria(criteria, showAdvanced);
        		}
        	}
        );
    }
    
    public Page openSearch(boolean bringForward)
    {
        ActivityMonitor.instance().setActive();
        
        PageTabItem item = _pageMap.get(StaticPageType.Search);
        if (item == null)
        {
            if (_searchPage == null)
            {
                _searchPage = new Page(new SearchPanel(false), "Search", true, "search-icon");
            }
            
            item = addPage(_searchPage, StaticPageType.Search);
        }
        
        if (bringForward)
        {
            setSelection(item);
            StateManager.instance().pushInactiveToken("search");
        }
        
        return item.getPage();
    }
    
    public Page openAbout(boolean bringForward)
    {
        ActivityMonitor.instance().setActive();
        
        PageTabItem item = _pageMap.get(StaticPageType.About);
        if (item == null)
        {
            item = addPage(new Page(new HtmlContainer(), "About", true, "info-icon"), StaticPageType.About);
        }
        
        if (bringForward)
        {
            setSelection(item);
        }
        
        return item.getPage();
    }
    
    public Page openHelp(boolean bringForward)
    {
        ActivityMonitor.instance().setActive();
        
        PageTabItem item = _pageMap.get(StaticPageType.Help);
        if (item == null)
        {
            item = addPage(new Page(new HtmlContainer(), "Help", true, "help-icon"), StaticPageType.Help);
        }
        
        if (bringForward)
        {
            setSelection(item);
        }
        
        return item.getPage();
    }
    
    private PageTabItem addPage(Page page, StaticPageType type)
    {
        PageTabItem tabItem = new PageTabItem();
        tabItem.setLayout(new FitLayout());
        tabItem.setText(page.getName());
        tabItem.setIconStyle(page.getIconStyle());
        tabItem.setClosable(page.isClosable());
        tabItem.setPage(page);
        
        _pageMap.put(type, tabItem);
        _reversePageMap.put(tabItem, type);
        
        final Listener<TabPanelEvent> closeListener = new Listener<TabPanelEvent>() 
        {  
            public void handleEvent(TabPanelEvent ce) 
            {  
                TabItem item = ce.getItem();  
                StaticPageType type = _reversePageMap.get(item);
                _reversePageMap.remove(item);
                _pageMap.remove(type);
            }  
        }; 
        
        tabItem.addListener(Events.Close, closeListener);
        
        add(tabItem);
        
        return tabItem;
    }
    
}
