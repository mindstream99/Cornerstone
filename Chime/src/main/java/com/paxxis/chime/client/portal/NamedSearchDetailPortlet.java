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
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.paxxis.chime.client.InstanceUpdateListener;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.NamedSearch;
import com.paxxis.chime.client.common.SearchCriteria;
import com.paxxis.chime.client.common.portal.PortletSpecification;
import com.paxxis.chime.client.editor.NamedSearchEditor;
import com.paxxis.chime.client.widgets.NamedSearchDetailPanel;

/**
 *
 * @author Robert Englander
 */
public class NamedSearchDetailPortlet extends PortletContainer {
    private InstanceUpdateListener updateListener;
    private NamedSearchDetailPanel detailPanel;
    private ToolButton actionsButton = null;
    private NamedSearch dataInstance = null;

    public NamedSearchDetailPortlet(PortletSpecification spec, HeaderType type, InstanceUpdateListener listener) {
        super(spec, HeaderType.Shaded, true);
        updateListener = listener;
    }

    public void setDataInstance(final DataInstance instance) {
    	Runnable r = new Runnable() {
    		public void run() {
    	        getBody().removeAll();

    	        if (instance instanceof NamedSearch) {
    	            dataInstance = (NamedSearch)instance;
    	            detailPanel = new NamedSearchDetailPanel(dataInstance, 300);
    	            getBody().add(detailPanel, new FlowData(0));
    	            actionsButton.setVisible(dataInstance.canUpdate(ServiceManager.getActiveUser()));
    	        }

    	        getBody().layout();
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
        //fieldListener  = new FileEditorListener() {
        //    public void onEdit(String fileId, String mimeType, String extension, long size) {
        //        sendEdit(fileId, mimeType, extension, size);
        //    }
        //};

        actionsButton = new ToolButton("x-tool-save");

        addHeaderItem(actionsButton);
        setHeading("Search Details");
        actionsButton.addSelectionListener(
                new SelectionListener<IconButtonEvent>() {
            @Override
            public void componentSelected(IconButtonEvent ce) {
                NamedSearchEditor editor = new NamedSearchEditor(dataInstance,
                    new NamedSearchEditor.NamedSearchEditListener() {

                        public void onEdit(SearchCriteria criteria) {
                            if (criteria != null) {
                                updateListener.onUpdate(dataInstance, criteria);
                            }
                        }
                    }
                );
                editor.show();
            }
        });

    }
}
