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

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
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
public class TypeFieldGroupsPortlet extends PortletContainer {

    private InstanceUpdateListener updateListener;
    private boolean pendingData = false;

    public TypeFieldGroupsPortlet(PortletSpecification spec, InstanceUpdateListener listener) {
        super(spec, HeaderType.None, false);
        updateListener = listener;
    }

    public void init() {
    	super.init();
    	getBody().setLayout(new RowLayout());
    }

    public void setDataInstance(final DataInstance instance, final UpdateReason reason) {
    	Runnable r = new Runnable() {
    		public void run() {
    	        if (reason == UpdateReason.InstanceChange || reason == UpdateReason.AppliedTypeChange 
    	        		|| reason == UpdateReason.UpdateEvent) {
    	            getBody().removeAll();

    	            for (final Shape type : instance.getShapes()) {
    	            	if (!type.isPrimitive()) {
        	            	final TypeFieldsPortlet portlet = new TypeFieldsPortlet(null, updateListener);
        	                getBody().add(portlet, new RowData(1, -1, new Margins(5, 0, 0, 0)));
        	                
        	                DeferredCommand.addCommand(
        	                    new Command() {
        	                        public void execute() {
        	                            portlet.setDataInstance(instance, type);
        	                            getBody().layout();
        	                        }
        	                    }
        	                );
    	            	}
    	            }
    	        } else  {
    	            for (Component comp : getBody().getItems()) {
    	                TypeFieldsPortlet portlet = (TypeFieldsPortlet)comp;
    	                portlet.updateDataInstance(instance, reason == UpdateReason.FieldChange);
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
}