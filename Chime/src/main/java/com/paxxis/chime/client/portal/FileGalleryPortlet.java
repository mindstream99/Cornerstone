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
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.portal.PortletSpecification;
import com.paxxis.chime.client.widgets.FileListContainer;

/**
 *
 * @author Robert Englander
 */
public class FileGalleryPortlet extends PortletContainer {
    private ToolButton actionsButton = null;
    private DataInstance _dataInstance = null;
    private FileListContainer imageContainer;

    private InstanceUpdateListener updateListener;
    private int portletWidth;

    public FileGalleryPortlet(PortletSpecification spec, boolean showHeader,
            int width, InstanceUpdateListener listener)
    {
        super(spec, HeaderType.Shaded, true);
        updateListener = listener;
        portletWidth = width;
    }

    @Override
    public void destroy()
    {
        super.destroy();
    }

    protected void init() {
    	super.init();
        LayoutContainer lc = getBody();

        lc.setLayout(new RowLayout());
        actionsButton = new ToolButton("x-tool-save");

        addHeaderItem(actionsButton);

        setHeading("File Gallery");

        // calling this after setting up our own tool buttons so that the expand and
        // collapse buttons appear after ours
        //setExpandable();

        imageContainer = new FileListContainer(actionsButton, updateListener);
        lc.add(imageContainer, new RowData(1, -1));

        updateEditable();
    }

    public void updateEditable() {
        actionsButton.setVisible(_dataInstance != null &&
        		_dataInstance.canUpdate(ServiceManager.getActiveUser()));
    }

    public void setDataInstance(final DataInstance instance, final UpdateReason reason) {
    	Runnable r = new Runnable() {
    		public void run() {
    	        _dataInstance = instance;
    	        if (reason == UpdateReason.InstanceChange || reason == UpdateReason.FileAdd
    	                || reason == UpdateReason.FileDelete) {
    	            
    	            if (instance.getShapes().get(0).canAttachFiles()) {
    	                setVisible(true);

    	                // which image should be shown?
    	                int idx = 0;
    	                int last = instance.getFiles().size() - 1;
    	                if (reason == UpdateReason.FileAdd) {
    	                    idx = last;
    	                } else if (reason == UpdateReason.FileDelete) {
    	                    idx = imageContainer.getFileIndex();
    	                    if (idx > last) {
    	                        idx = last;
    	                    }
    	                }
    	                imageContainer.setDataInstance(instance, idx);
    	            } else {
    	                setVisible(false);
    	            }

    	            updateEditable();
    	        } else {
    	            imageContainer.setDataInstance(instance, false);
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
