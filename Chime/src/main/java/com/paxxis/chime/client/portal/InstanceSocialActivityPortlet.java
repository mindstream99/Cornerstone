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

import com.extjs.gxt.ui.client.event.BoxComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.paxxis.chime.client.InstanceUpdateListener;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.portal.PortletSpecification;
import com.paxxis.chime.client.pages.SocialContextPanel;

/**
 *
 * @author Robert Englander
 */
public class InstanceSocialActivityPortlet extends PortletContainer 
{
    private LayoutContainer mainContainer;
    private SocialContextPanel _socialPanel = null;
    private DataInstance _dataInstance = null;
    private InstanceUpdateListener updateListener = null;

    public InstanceSocialActivityPortlet(PortletSpecification spec, HeaderType type,
            InstanceUpdateListener listener)
    {
        super(spec, HeaderType.None, false);
        updateListener = listener;
    }

    public DataInstance getDataInstance()
    {
        return _dataInstance;
    }
    
    @Override
    public void reset()
    {
        //_socialPanel.reset();
    }

    protected void init() {
    	super.init();
        setHeading("Social Activity");

        mainContainer = new LayoutContainer();
        mainContainer.setLayout(new RowLayout());
        mainContainer.setHeight(350);
        getBody().addListener(Events.Resize,
            new Listener<BoxComponentEvent>() {

                public void handleEvent(BoxComponentEvent evt) {
                    mainContainer.layout(true);
                }

            }
        );

        _socialPanel = new SocialContextPanel(updateListener);

        Object val = getSpecification().getProperty("showComments");
        if (val != null) {
            if (val.toString().trim().equals("false")) {
                _socialPanel.setShowComments(false);
            }
        }

        mainContainer.add(_socialPanel, new RowData(1, 1));

        getBody().setLayout(new RowLayout());

        getBody().add(mainContainer, new RowData(1, -1));
    }

    public void setDataInstance(final DataInstance instance, final UpdateReason reason)
    {
    	Runnable r = new Runnable() {
    		public void run() {
    	        _dataInstance = instance;
    	        _socialPanel.setDataInstance(instance, reason);
    		}
    	};
    	
    	if (isRendered()) {
    		r.run();
    	} else {
    		addPostRenderRunnable(r);
    	}
    }
}
