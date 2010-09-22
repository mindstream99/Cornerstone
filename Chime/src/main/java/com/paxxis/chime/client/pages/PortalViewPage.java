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

import com.paxxis.chime.client.DataInstanceResponseObject;
import com.paxxis.chime.client.LoginResponseObject;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceManagerAdapter;
import com.paxxis.chime.client.StateManager;
import com.paxxis.chime.client.portal.UpdateReason;
import com.paxxis.chime.client.widgets.ContentNavigator;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceRequest;
import com.paxxis.chime.client.common.DataInstanceRequest.Depth;
import com.paxxis.chime.client.common.DataInstanceResponse;
import com.paxxis.chime.client.common.User;
import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class PortalViewPage extends LayoutContainer
{

    private DataDetailPanel detailPanel = null;
    
    public PortalViewPage()
    {
        ServiceManager.addListener(
            new ServiceManagerAdapter()
            {
                public void onLoginResponse(LoginResponseObject resp) 
                {
                    DeferredCommand.addCommand(
                        new Command() {
                            public void execute() {
                                goHome();
                            }
                        }
                    );
                }

                public void onLogout() 
                {
                    DeferredCommand.addCommand(
                        new Command() {
                            public void execute() {
                                goHome();
                            }
                        }
                    );
                }
            }
        );

        init();
    }
    
    private void init()
    {
        setBorders(false);

        setLayout(new BorderLayout());

        BorderLayoutData data = new BorderLayoutData(LayoutRegion.CENTER);
        data.setMargins(new Margins(2, 2, 2, 2));
        data.setSplit(true);

        detailPanel = new DataDetailPanel(0);
        add(detailPanel, data);

        BorderLayoutData data2 = new BorderLayoutData(LayoutRegion.WEST, 250, 150, 450);
        data2.setMargins(new Margins(2, 2, 2, 2));
        data2.setCollapsible(true);
        data2.setFloatable(true);
        data2.setSplit(true);
        ContentNavigator navigator = new ContentNavigator();
        add(navigator, data2);

        //goHome();
    }

    public void compareForRefresh(DataInstance instance) {
        detailPanel.compareForRefresh(instance);
    }

    public void goHome() {
    	goHome(null);
    }
    
    public void goHome(final String followToken) {

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
                        final DataInstance instance = instances.get(0);
                        DeferredCommand.addCommand(
                            new Command()
                            {
                                public void execute()
                                {
                                    PageManager.instance().openNavigator(true, instance);
                                    StateManager.instance().pushInactiveToken("detail:" + instance.getId());
                                    
                                    DeferredCommand.addCommand(
                                    	new Command() {
                                    		public void execute() {
                                    			StateManager.getMainContainer().onStateChange(followToken, true);
                                    		}
                                    	}
                                    );
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

        if (ServiceManager.isLoggedIn()) {
            User user = ServiceManager.getActiveUser();
            if (!isHome(user)) {
                DataInstanceRequest req = new DataInstanceRequest();
                req.setDepth(Depth.Deep);
                req.setIds(user.getHomePageId());
                req.setUser(ServiceManager.getActiveUser());
                ServiceManager.getService().sendDataInstanceRequest(req, callback);
            }
        } else {
            PageManager.instance().clearHome();
        }

    }

    private boolean isHome(User user) {
        boolean isHome = false;

        DataInstance inst = getDataInstance();
        if (inst != null) {
            isHome = inst.getId().equals(user.getHomePageId());
        }

        return isHome;
    }

    public void replaceDataInstance(DataInstance instance) {
    	detailPanel.setDataInstance(instance, UpdateReason.Silent);
    }
    
    public DataInstance getDataInstance() {
        return detailPanel.getDataInstance();
    }

    public void clearPage() {
        detailPanel.clearInstance();
    }
     
    public void buildPage(DataInstance instance)
    {
        detailPanel.setDataInstance(instance, UpdateReason.InstanceChange);
    }
    
}
