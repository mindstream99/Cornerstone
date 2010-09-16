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
import com.paxxis.chime.client.widgets.CommentsPanel;

/**
 *
 * @author Robert Englander
 */
public class DiscussionDetailPortlet extends PortletContainer {
    private InstanceUpdateListener updateListener;
    private CommentsPanel commentsPanel = null;
    private LayoutContainer mainContainer;

    public DiscussionDetailPortlet(PortletSpecification spec, HeaderType type, InstanceUpdateListener listener) {
        super(spec, HeaderType.Shaded, true);
        updateListener = listener;
    }

    public void setDataInstance(final DataInstance instance, final UpdateReason reason) {
    	Runnable r = new Runnable() {
    		public void run() {
    	    	commentsPanel.setDataInstance(instance, reason);
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
    	
        setHeading("Discussion Details");

        mainContainer = new LayoutContainer();
        mainContainer.setLayout(new RowLayout());
        mainContainer.setHeight(400);

        commentsPanel = new CommentsPanel(CommentsPanel.Type.Post, null);
        mainContainer.add(commentsPanel, new RowData(1, 1));
        getBody().add(mainContainer, new RowData(1, -1));
    }
}

