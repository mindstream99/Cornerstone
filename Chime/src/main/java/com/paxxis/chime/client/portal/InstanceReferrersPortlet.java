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

import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.paxxis.chime.client.PaginatedResultsPanel;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.common.Cursor;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceRequest;
import com.paxxis.chime.client.common.DataInstanceResponse;
import com.paxxis.chime.client.common.portal.PortletSpecification;

/**
 *
 * @author Robert Englander
 */
public class InstanceReferrersPortlet extends PortletContainer {
    private static final int HEIGHT = 300;
    private static final int EMPTYHEIGHT = 90;

    private PaginatedResultsPanel resultsPanel;
    private DataInstance dataInstance;
    
    public InstanceReferrersPortlet(PortletSpecification spec, int width) {
        super(spec, HeaderType.Shaded, true);
    }

    protected void init() {
    	super.init();
        LayoutContainer lc = getBody();

        lc.setLayout(new RowLayout());

        ToolButton refreshButton = new ToolButton("x-tool-refresh");
        addHeaderItem(refreshButton);
        refreshButton.addSelectionListener(
                new SelectionListener<IconButtonEvent>() {
            @Override
             public void componentSelected(IconButtonEvent ce) {
                getReferences();
             }
        });
        
        setHeading("Referrers");

        resultsPanel = new PaginatedResultsPanel(PaginatedResultsPanel.Type.Short, "No Referrers");
        resultsPanel.setResultsListener(
            new PaginatedResultsPanel.ResultsListener() {
                public void onDataInstanceResponse(DataInstanceResponse response) {
                    if (response.getDataInstances().size() == 0) {
                        resultsPanel.setHeight(EMPTYHEIGHT);
                    } else {
                        resultsPanel.setHeight(HEIGHT);
                    }
                }
            }
        );

        resultsPanel.setHeight(HEIGHT);
        lc.add(resultsPanel, new RowData(1, -1));

    }

    public void setDataInstance(final DataInstance instance, final UpdateReason reason) {
    	dataInstance = instance;
    	Runnable r = new Runnable() {
    		public void run() {
    	        if (reason == UpdateReason.InstanceChange) {
    	        	getReferences();
    	        }
    		}
    	};
    	
    	if (isRendered()) {
    		r.run();
    	} else {
    		addPostRenderRunnable(r);
    	}
    }
    
    private void getReferences() {
        DataInstanceRequest req = new DataInstanceRequest();
        req.setUser(ServiceManager.getActiveUser());
        req.setCursor(new Cursor(20));
        req.setIds(dataInstance.getId());
        req.setStyle(DataInstanceRequest.Style.ReferenceSearch);
        resultsPanel.query(req);
    }
}
