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
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.paxxis.chime.client.InstanceUpdateListener;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.client.common.portal.PortletSpecification;
import com.paxxis.chime.client.widgets.InterceptedHtml;

public class UserMessagesPortlet extends PortletContainer {

	private ToolButton actionsButton = null;
    private User user = null;
    private InstanceUpdateListener updateListener;
    private InterceptedHtml message;
    
    public UserMessagesPortlet(PortletSpecification spec, InstanceUpdateListener listener) {
        super(spec, HeaderType.Shaded, true);
        updateListener = listener;
    }
	
    protected void init() {
    	super.init();
        LayoutContainer lc = getBody();

        lc.setLayout(new RowLayout());
        actionsButton = new ToolButton("x-tool-save");
       
        addHeaderItem(actionsButton);

        setHeading("Messages");
        message = new InterceptedHtml();
        
        LayoutContainer dummy = new LayoutContainer();
        dummy.setHeight(100);
        lc.add(dummy, new RowData(1, -1));
        
        layout();
    }

    public void setUser(final User instance, final UpdateReason reason) {
    	Runnable r = new Runnable() {
    		public void run() {
    	    	user = instance;

    	        layout();
    		}
    	};
    	
    	if (isRendered()) {
    		r.run();
    	} else {
    		addPostRenderRunnable(r);
    	}
    }
}
