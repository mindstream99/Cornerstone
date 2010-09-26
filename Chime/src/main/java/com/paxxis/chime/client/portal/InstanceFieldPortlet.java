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

import com.extjs.gxt.ui.client.event.BoxComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.paxxis.chime.client.InstanceUpdateListener;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.portal.PortletSpecification;
import com.paxxis.chime.client.widgets.DataInstanceFields;

/**
 *
 * @author Robert Englander
 */
public class InstanceFieldPortlet extends PortletContainer 
{
    private boolean _showHeader;
    private DataInstanceFields _fields = null;
    private InstanceUpdateListener _saveListener;

    public InstanceFieldPortlet(PortletSpecification spec, HeaderType type, InstanceUpdateListener saveListener)
    {
        super(spec, HeaderType.None, true);
        _showHeader = false; //type != HeaderType.None;
        _saveListener = saveListener;
    }
    
    public void setDataInstance(final DataInstance instance, final Shape type, final List<String> exclusions, final boolean refresh)
    { 
    	Runnable r = new Runnable() {
    		public void run() {
    	        _fields.setDataInstance(instance, type, exclusions, refresh);
    		}
    	};
    	
    	if (isRendered()) {
    		r.run();
    	} else {
    		addPostRenderRunnable(r);
    	}
    }
    
    public void onResizeXXX(int width, int height) {
    	super.onResize(width, height);
    	if (_fields != null) {
        	DeferredCommand.addCommand(
        		new Command() {
        			public void execute() {
        	        	_fields.setWidth(getWidth());
        			}
        		}
        	);
    	}
    	layout();
    }

    protected void init()
    {
    	super.init();
    	getBody().setLayout(new RowLayout());
        _fields = new DataInstanceFields(_saveListener);
        
        if (_showHeader)
        {
            getBody().add(_fields, new RowData(1, -1, new Margins(4)));
        }
        else
        {
            getBody().add(_fields, new RowData(1, -1));
        }
        
        getBody().layout();
        getBody().addListener(Events.Resize,
            new Listener<BoxComponentEvent>() {
                public void handleEvent(BoxComponentEvent evt) {
                	DeferredCommand.addCommand(
                		new Command() {
                			public void execute() {
                                //getBody().layout();
                			}
                		}
                	);
                }
            }
        );
    }
}
