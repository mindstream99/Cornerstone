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

import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.paxxis.chime.client.PaginatedResultsPanel;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.common.Cursor;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceRequest;
import com.paxxis.chime.client.common.Tag;
import com.paxxis.chime.client.common.DataInstanceRequest.Depth;
import com.paxxis.chime.client.common.DataInstanceRequest.Operator;
import com.paxxis.chime.client.common.DataInstanceRequest.SortOrder;
import com.paxxis.chime.client.common.portal.PortletSpecification;
import com.paxxis.chime.client.common.portal.PortletSpecification.PortletType;

/**
 * @author Robert Englander
 *
 */
public class TaggedInstancesPortlet extends PortletContainer {

    private TabPanel tabPanel;
    private PortletSpecification spec;
    private Tag tag = null;
    
    private boolean globalItemAttached = false;
    private TabItem globalItem;

    private PaginatedResultsPanel globalResultsPanel;
    private PaginatedResultsPanel userResultsPanel;
    
	public TaggedInstancesPortlet(long id) {
		super(null, HeaderType.Shaded, true);
		spec = new PortletSpecification(PortletType.TagContent, id);
	}

    public PortletSpecification getSpecification() {
    	return spec;
    }

    protected void init() {
    	super.init();
    	setHeading("Applied to Instances");
    	
    	getBody().setHeight(300);
        getBody().setLayout(new FitLayout());

        tabPanel = new TabPanel();
        globalItem = new TabItem("By Everyone");
        globalItem.setLayout(new FitLayout());
        globalResultsPanel = new PaginatedResultsPanel(PaginatedResultsPanel.Type.Short);
        globalItem.add(globalResultsPanel);
        tabPanel.add(globalItem);
        globalItemAttached = true;

        TabItem tabItem = new TabItem("By Me");
        tabItem.setLayout(new FitLayout());
        userResultsPanel = new PaginatedResultsPanel(PaginatedResultsPanel.Type.Short);
        tabItem.add(userResultsPanel);
        tabPanel.add(tabItem);

        getBody().add(tabPanel);
        getBody().layout();
    }

    public void setDataInstance(final DataInstance instance) {
    	Runnable r = new Runnable() {
    		public void run() {
    	    	tag = (Tag)instance;
    	    	if (tag.isPrivate() && globalItemAttached) {
    	    		tabPanel.remove(globalItem);
    	    		globalItemAttached = false;
    	    	} else if (!tag.isPrivate() && !globalItemAttached) {
    	    		tabPanel.insert(globalItem, 0);
    	    		globalItemAttached = true;
    	    	}
    	    	
                DataInstanceRequest req = new DataInstanceRequest();
                req.setUser(ServiceManager.getActiveUser());
                req.setCursor(new Cursor(20));
                req.setDepth(Depth.Shallow);
                req.setSortOrder(SortOrder.ByName);
                req.addQueryParameter(null, "Tag", tag.getId().getValue(), Operator.Reference);

                if (globalItemAttached) {
                    globalResultsPanel.query(req);
                    
                    // TODO we need an appropriate query for the user panel
                    req = new DataInstanceRequest();
                    req.setUser(ServiceManager.getActiveUser());
                    req.setCursor(new Cursor(20));
                    req.setDepth(Depth.Shallow);
                    req.setSortOrder(SortOrder.ByName);
                    req.addQueryParameter(null, "TagAppliedUser", tag.getId().getValue(), Operator.Reference);
                    userResultsPanel.query(req);
    	    	} else {
                    userResultsPanel.query(req);
    	    	}
    		}
    	};
    	
    	if (isRendered()) {
    		r.run();
    	} else {
    		addPostRenderRunnable(r);
    	}
    }
}
