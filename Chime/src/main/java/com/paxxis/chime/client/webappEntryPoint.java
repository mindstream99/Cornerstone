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

import com.extjs.gxt.themes.client.Slate;
import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.util.ThemeManager;
import com.extjs.gxt.ui.client.widget.layout.AnchorLayout;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.History;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.AreaChart;
import com.google.gwt.visualization.client.visualizations.Gauge;
import com.google.gwt.visualization.client.visualizations.OrgChart;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.pages.PageManager;

/**
 *
 * @author Robert Englander
 */
public class webappEntryPoint implements EntryPoint 
{
    MainContainer appView;
    ServiceManagerListener listener = null;

    public webappEntryPoint() 
    {
    }
 
    public void onModuleLoad() 
    {
    	@SuppressWarnings("unused")
    	AnchorLayout junk = new AnchorLayout();
    	
        ThemeManager.register(Slate.SLATE);
        GXT.setDefaultTheme(Slate.SLATE, true);

        Runnable onLoadCallback = new Runnable() {
            public void run() {
            }
        };

		// Load the visualization api's we use
		VisualizationUtils.loadVisualizationApi(onLoadCallback, Gauge.PACKAGE);
		VisualizationUtils.loadVisualizationApi(onLoadCallback, AreaChart.PACKAGE);
		VisualizationUtils.loadVisualizationApi(onLoadCallback, OrgChart.PACKAGE);
          
        // this will get the activity manager loaded and running
        ActivityMonitor.instance();

        final String token = History.getToken();
        
        if (ServiceManager.hasLoginCookie()) {
            listener = new ServiceManagerListener() {

                public void onLoginResponse(LoginResponseObject resp) {
                    ServiceManager.removeListener(listener);
                    DeferredCommand.addCommand(
                    	new Command() {
                    		public void execute() {
                                finishLoading(token);
                                PageManager.instance().openNavigator(true, token);
                    		}
                    	}
                    );		
                    
                }

                public void onLogout() {
                }

                public void onDataInstanceUpdated(DataInstance instance) {
                }

            };

            ServiceManager.addListener(listener);

            DeferredCommand.addCommand(
                new Command() {
                    public void execute() {
                        ServiceManager.loginFromCookie();
                    }
                }
            );
        } else {
            finishLoading(token);
        }

    }

    private void finishLoading(String token) {
        appView = new MainContainer(token);
        StateManager.setMainContainer(appView);
    }
}
