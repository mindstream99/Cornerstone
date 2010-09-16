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
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.paxxis.chime.client.InstanceUpdateListener;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.portal.PortletSpecification;
import com.paxxis.chime.client.widgets.ShapeFields;

/**
 *
 * @author Robert Englander
 */
public class ShapeFieldsPortlet  extends PortletContainer {

    private InstanceUpdateListener updateListener;
    private Shape dataType;
    private ShapeFields fields;

    public ShapeFieldsPortlet(PortletSpecification spec, InstanceUpdateListener listener) {
        super(spec, HeaderType.None, true);
        updateListener = listener;
    }

    protected void init() {
    	super.init();
    	getBody().setLayout(new RowLayout());
    }

    public void updateDataInstance(final DataInstance instance) {
        fields.setDataInstance(dataType);
    }

    public void setDataInstance(final Shape type) {
    	Runnable r = new Runnable() {
    		public void run() {
    	    	dataType = type;

    	        getBody().removeAll();

    	        fields = new ShapeFields(updateListener);
    	        getBody().add(fields, new RowData(1, -1, new Margins(2)));

    	        DeferredCommand.addCommand(
    	            new Command() {
    	                public void execute() {
    	                    updateDataInstance(dataType);
    	                    getBody().layout();
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
