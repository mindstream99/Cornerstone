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

import java.util.List;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.RootPanel;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceRequest;
import com.paxxis.chime.client.common.NamedSearch;
import com.paxxis.chime.client.common.DataInstanceRequest.Depth;
import com.paxxis.chime.client.common.DataInstanceResponse.Reason;
import com.paxxis.chime.client.pages.PageManager;
import com.paxxis.chime.client.pages.PageManager.StaticPageType;
import com.paxxis.chime.client.widgets.LoginPanel;
import com.paxxis.chime.client.widgets.LoginWindow;
import com.paxxis.cornerstone.base.ErrorMessage;
import com.paxxis.cornerstone.base.InstanceId;

/**
 * 
 * @author Robert Englander
 *
 */
public class MainContainer extends LayoutContainer implements StateChangeListener
{
    private static String pendingToken = null;
    
    private Viewport viewport;
    private ContentPanel centerPanel;
    private HeaderPanel headerPanel;
    private FooterPanel footerPanel;
    private LayoutContainer cardPanel;
    private LayoutContainer infoPanel;

    public static void setPendingToken(String token) {
        //pendingToken = token;
    }
    
    public MainContainer(String initialToken)
    {
        initialize(initialToken);
        StateManager.instance().setListener(this);
    }

    protected void initialize(String initialToken)
    {
        viewport = new Viewport();
        viewport.setLayout(new FitLayout());  
        viewport.add(this);  
        setLayout(new BorderLayout());  

        headerPanel = new HeaderPanel();
        BorderLayoutData data = new BorderLayoutData(LayoutRegion.NORTH, headerPanel.getPanelHeight());
        data.setMargins(new Margins());
        add(headerPanel, data);

        footerPanel = new FooterPanel();
        BorderLayoutData data2 = new BorderLayoutData(LayoutRegion.SOUTH, footerPanel.getPanelHeight());
        data2.setMargins(new Margins());
        add(footerPanel, data2);

        createCenter(initialToken);
        RootPanel.get().add(viewport); 
    }

    private void createCenter(final String initialToken)
    {
        cardPanel = new LayoutContainer();
        cardPanel.setLayout(new CardLayout());
        cardPanel.setBorders(false);

        centerPanel = new ContentPanel();
        centerPanel.setBorders(false);
        centerPanel.setHeaderVisible(false);
        centerPanel.setLayout(new FitLayout());

        BorderLayoutData data = new BorderLayoutData(LayoutRegion.CENTER);
        data.setMargins(new Margins(0));
        
        PageManager pageContainer = PageManager.instance();
        pageContainer.open(StaticPageType.Navigator, true);

        centerPanel.add(pageContainer);

        cardPanel.add(centerPanel);

        infoPanel = new LayoutContainer();
        infoPanel.setLayout(new CenterLayout());
        infoPanel.add(new LoginPanel());
        
        cardPanel.add(infoPanel);

        add(cardPanel, data);

        updatePanels();

        ServiceManagerListener listener = new ServiceManagerAdapter()
        {
            public void onLoginResponse(LoginResponseObject resp)
            {
                updatePanels();
            }

            public void onLogout()
            {
                updatePanels();
            }
        };

        ServiceManager.addListener(listener);

        if (initialToken != null) {

            DeferredCommand.addCommand(
                new Command() {
                    public void execute() {
                        // leave this here for now.  but it appears to be unnecessary
                        // and actually creates some problems
                        //onStateChange(initialToken, true);
                    }
                }
            );
        }
    }

    private void updatePanels() {
        if (ServiceManager.isLoggedIn()) {

            // these setActiveItem calls are inside try catch blocks because for some reason
            // the throw exceptions under the debug shell, but still do what they've been asked
            // to do.  I'm sure there's a bug here somewhere
            try {
                ((CardLayout)cardPanel.getLayout()).setActiveItem(centerPanel);
            } catch (Exception e) {

            }

            if (pendingToken != null) {
                onStateChange(pendingToken, true);
                pendingToken = null;
            }
        } else {
            try {
                ((CardLayout)cardPanel.getLayout()).setActiveItem(infoPanel);
            } catch (Exception e) {

            }
        }
    }

    public void onStateChange(final String token, final boolean promptUser)
    {
        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    String[] parts = token.split(":");
                    if (parts[0].equals("detail"))
                    {
                        // part1 is the instance id
                        String instanceId = parts[1];

                        getInstance(instanceId, promptUser, false);
                    }
                    else if (parts[0].equals("home"))
                    {
                        PageManager.instance().openNavigator(true);
                    }
                    else if (parts[0].equals("search"))
                    {
                        PageManager.instance().openSearch(true);
                        if (parts.length == 2) {
                            String instanceId = parts[1];
                            getInstance(instanceId, promptUser, true);
                        }
                    }
                    else if (parts[0].equals("help"))
                    {
                        PageManager.instance().openHelp(true);
                    }
                    else if (parts[0].equals("about"))
                    {
                        PageManager.instance().openAbout(true);
                    }
                }
            }
        );
    }
    
    private void getInstance(final String instanceId, final boolean promptUser, final boolean doSearch)
    {
        final ChimeAsyncCallback<DataInstanceResponseObject> callback = 
        		new ChimeAsyncCallback<DataInstanceResponseObject>() {
            public void onSuccess(DataInstanceResponseObject response) { 
                if (response.isResponse()) {
                    List<DataInstance> list = response.getResponse().getDataInstances();
                    if (list.size() == 1) {
                        DataInstance instance = list.get(0);
                        if (doSearch) {
                        	NamedSearch named = (NamedSearch)instance;
                            PageManager.instance().openSearch(named);
                        } else {
                            PageManager.instance().openNavigator(true, instance);
                        }
                    } else {
                        if (promptUser) {
                            String msg;
                            if (response.getResponse().getReason() == Reason.NoSuchData) {
                                StateManager.instance().goBack("The requested data does not exist or has restricted access.");
                            } else if (response.getResponse().getReason() == Reason.NotVisible) {
                                if (ServiceManager.getActiveUser() == null) {
                                    msg = "The requested data is not visible to everyone.  If you log in you may be able to view this data.";

                                    Runnable r1 = new Runnable() {
                                        public void run() {
                                            getInstance(instanceId, false, doSearch);
                                        }
                                    };

                                    Runnable r2 = new Runnable() {
                                        public void run() {
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
                } else {
                    StateManager.instance().backInactive();
                    ErrorMessage error = response.getError();
                    if (error.getType() == ErrorMessage.Type.SessionExpiration) {
                        StateManager.instance().backInactive();
                        ServiceManager.reLogin(
                            new Runnable() {
                                public void run() {
                                    getInstance(instanceId, false, doSearch);
                                }
                            },
                            new Runnable() {
                                public void run() {
                                }
                            }
                        );
                    }
                }
            }

        };
        
        DataInstanceRequest req = new DataInstanceRequest();
        req.setDepth(Depth.Deep);
        req.setIds(InstanceId.create(instanceId));
        req.setUser(ServiceManager.getActiveUser());
        ServiceManager.getService().sendDataInstanceRequest(req, callback);
    }

}
