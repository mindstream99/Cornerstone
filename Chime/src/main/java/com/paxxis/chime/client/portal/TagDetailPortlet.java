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

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.paxxis.chime.client.InstanceUpdateListener;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.portal.PortletSpecification;

/**
 *
 * @author Robert Englander
 */
public class TagDetailPortlet extends PortletContainer {
    private InstanceUpdateListener updateListener;
    private LayoutContainer container;
    private PortalContainer portal = null;
    private TagShapeUsagePortlet shapeUsage;
    private TaggedInstancesPortlet instanceUsage;
    
    public TagDetailPortlet(PortletSpecification spec, HeaderType type, InstanceUpdateListener listener) {
        super(spec, HeaderType.None, false);
        updateListener = listener;
    }

    public void setDataInstance(final DataInstance instance) {
        Runnable r = new Runnable() {
        	public void run() {
                container.removeAll();
                portal = new PortalContainer(2, true, "0px 0px 0px 0px", "0px 0px 0px 5px");
                portal.setColumnWidth(0, 0.5);
                portal.setColumnWidth(1, 0.5);
                shapeUsage = new TagShapeUsagePortlet(1);
                shapeUsage.setDataInstance(instance);
                portal.add(shapeUsage, 1);
                
                instanceUsage = new TaggedInstancesPortlet(2);
                instanceUsage.setDataInstance(instance);
                portal.add(instanceUsage, 0);

                container.add(portal, new RowData(1, -1));
                container.layout();
         	}
        };
    	
    	if (isRendered()) {
    		r.run();
    	} else {
    		addPostRenderRunnable(r);
    	}
    }

    protected void init() {
    	super.init();
    	getBody().setLayout(new RowLayout());
        container = new LayoutContainer();
        container.setBorders(false);
        container.setLayout(new RowLayout());
        getBody().add(container, new RowData(1, -1));
    }
    
}

