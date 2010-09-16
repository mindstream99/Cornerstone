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

import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.paxxis.chime.client.Utils;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.ShapeTagContext;
import com.paxxis.chime.client.common.Tag;
import com.paxxis.chime.client.common.portal.PortletSpecification;
import com.paxxis.chime.client.common.portal.PortletSpecification.PortletType;
import com.paxxis.chime.client.widgets.InterceptedHtml;



/**
 * @author Robert Englander
 *
 */
public class TagShapeUsagePortlet extends PortletContainer {
    /** the min and max point size */
    private static final int MINSIZE = 12;
    private static final int MAXSIZE = 36;
    private static final double PTRANGE = MAXSIZE - MINSIZE;

    private TabPanel tabPanel;
    private PortletSpecification spec;
    private Tag tag = null;
    private InterceptedHtml globalCloud;
    private InterceptedHtml userCloud;
    
    private boolean globalItemAttached = false;
    private TabItem globalItem;
    
    public TagShapeUsagePortlet(long id) {
		super(null, HeaderType.Shaded, true);
		spec = new PortletSpecification(PortletType.TagContent, id);
	}

    public PortletSpecification getSpecification() {
    	return spec;
    }
    
    protected void init() {
    	super.init();
    	setHeading("Applied to Instances of Shapes");
    	
    	getBody().setHeight(300);
        getBody().setLayout(new FitLayout());

        tabPanel = new TabPanel();
        globalItem = new TabItem("By Everyone");
        globalItem.setScrollMode(Scroll.AUTO);
        globalCloud = new InterceptedHtml();
        globalItem.add(globalCloud, new FlowData(new Margins(5)));
        tabPanel.add(globalItem);
        globalItemAttached = true;

        TabItem tabItem = new TabItem("By Me");
        tabItem.setScrollMode(Scroll.AUTO);
        userCloud = new InterceptedHtml();
        tabItem.add(userCloud, new FlowData(new Margins(5)));
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
    	    	
    	    	if (globalItemAttached) {
        	    	globalCloud.setHtml(renderCloud(tag.getCommunityContext()));
    	    	}

    	    	userCloud.setHtml(renderCloud(tag.getUserContext()));
    		}
    	};
    	
    	if (isRendered()) {
    		r.run();
    	} else {
    		addPostRenderRunnable(r);
    	}
    }
    
    private String renderCloud(List<ShapeTagContext> contexts) {
        long min = 99999;
        long max = 0;

    	for (ShapeTagContext context : contexts) {
            if (context.getUsageCount() > max) {
                max = context.getUsageCount();
            }

            if (context.getUsageCount() < min) {
                min = context.getUsageCount();
            }
        }

        long range = max - min + 1;

        StringBuffer buffer = new StringBuffer();
    	for (ShapeTagContext context : contexts) {
    		Shape shape = context.getShape();
    		long count = context.getUsageCount();

    		// derive the pt size
            double factor = ((double)count - (double)min) / (double)range;
            int size = (int)(PTRANGE * factor + MINSIZE);
            
            String shapeName = shape.getName().replaceAll(" ", "&nbsp;");
            String link = "<span style=\"font-size:" + size + "px; padding-top: 2px;\">" + 
            	Utils.toHoverUrl(shape) + "</span>&nbsp;&nbsp;&nbsp;";
            buffer.append(link);
    	}

    	return buffer.toString();
    }
}
