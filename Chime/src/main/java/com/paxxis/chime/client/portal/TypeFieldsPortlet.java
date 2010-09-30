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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.BoxComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.paxxis.chime.client.InstanceUpdateListener;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.portal.PortletSpecification;

/**
 *
 * @author Robert Englander
 */
public class TypeFieldsPortlet extends PortletContainer {

    private InstanceUpdateListener updateListener;
    private Shape dataType;
    private InstanceFieldPortlet portlet = null;
    private List<String> exclusions = new ArrayList<String>();
    
    public TypeFieldsPortlet(PortletSpecification spec, InstanceUpdateListener listener) {
        super(spec, HeaderType.Shaded, true);
        updateListener = listener;
    }

    protected void init() {
    	super.init();
    	
    	PortletSpecification spec = getSpecification();
    	if (spec != null) {
        	Object obj = spec.getProperty("exclusions");
        	if (obj != null) {
        		if (obj instanceof List<?>) {
        			exclusions.clear();
        			exclusions.addAll((List<String>)obj);
        		}
        	}
    	}
    	
    	getBody().setLayout(new ColumnLayout());
        addListener(Events.Resize,
            new Listener<BoxComponentEvent>() {
                public void handleEvent(BoxComponentEvent evt) {
                    layout();
                }
            }
        );
    }

    public void updateDataInstance(DataInstance instance, boolean refresh) {
        portlet.setDataInstance(instance, dataType, exclusions, refresh);
    }

    public void updateDataInstance(DataInstance instance) {
        updateDataInstance(instance, true);
    }

    public void setDataInstance(final DataInstance instance, final String shapeId) {
    	Runnable r = new Runnable() {
    		public void run() {
    	    	for (Shape type : instance.getShapes()) {
    	    		if (type.getId().equals(shapeId)) {
    	    			setDataInstance(instance, type);
    	    			break;
    	    		}
    	    	}
    		}
    	};
    	
    	if (isRendered()) {
    		r.run();
    	} else {
    		addPostRenderRunnable(r);
    	}
    }
    
    public void setDataInstance(final DataInstance instance, final Shape type) {
    	Runnable r = new Runnable() {
    		public void run() {
    	        dataType = type;

    	    	PortletSpecification spec = getSpecification();
    	    	String titleText = type.getName();
    	    	if (spec != null) {
        	    	Object obj = getSpecification().getProperty("title");
        	    	if (obj != null) {
        	    		titleText = obj.toString();
        	    	}
    	    	}
    	        setHeading(titleText);

    	        getBody().removeAll();

    	        portlet = new InstanceFieldPortlet(getSpecification(), HeaderType.Transparent, updateListener);
    	        getBody().add(portlet, new ColumnData(1.0));

    	        DeferredCommand.addCommand(
    	            new Command() {
    	                public void execute() {
    	                    updateDataInstance(instance);
    	                }
    	            }
    	        );
    		}
    	};
    	
    	if (isRendered()) {
    		r.run();
    	} else {
    		addPostRenderRunnable(r);
    	}
    }
}
