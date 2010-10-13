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

import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.paxxis.chime.client.InstanceUpdateListener;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.InstanceUpdateListener.Type;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.portal.PortletSpecification;
import com.paxxis.chime.client.editor.FileEditorListener;
import com.paxxis.chime.client.widgets.FileUploader;
import com.paxxis.chime.client.widgets.ImageContainer;
import com.paxxis.chime.client.widgets.FileUploader.FileType;

/**
 *
 * @author Robert Englander
 */
public class ImageDetailPortlet extends PortletContainer {
    private InstanceUpdateListener updateListener;
    private ToolButton actionsButton = null;
    private DataInstance dataInstance = null;
    private FileEditorListener fieldListener;
    private ImageContainer imageContainer;

    public ImageDetailPortlet(PortletSpecification spec, HeaderType type, InstanceUpdateListener listener) {
        super(spec, HeaderType.Shaded, true);
        updateListener = listener;
    }

    public void setDataInstance(final DataInstance instance) {
    	Runnable r = new Runnable() {
    		public void run() {
    	        dataInstance = instance;
    	        getBody().removeAll();

    	        Shape type = dataInstance.getShapes().get(0);
    	        DataField field = type.getField("File ID");
    	        List<DataFieldValue> vals = dataInstance.getFieldValues(type, field);
    	        if (vals.size() == 1) {
    	            imageContainer = new ImageContainer(getBody(), vals.get(0).getValue().toString(), false, null, true);
    	            getBody().add(imageContainer, new FlowData(5, 0, 5, 0));
    	        }

    	        getBody().layout();
    	        actionsButton.setVisible(dataInstance.canUpdate(ServiceManager.getActiveUser()));
    		}
    	};
    	
    	if (isRendered()) {
    		r.run();
    	} else {
    		addPostRenderRunnable(r);
    	}
    }

    private void sendEdit(String fileId, String mimeType, String extension, long size) {

        // we replace the values to match the new file

        // File ID
        Shape type = dataInstance.getShapes().get(0);
        DataField field = type.getField("File ID");
        DataFieldValue newValue = new DataFieldValue(fileId, field.getShape().getId(), InstanceId.UNKNOWN, null);

        List<DataFieldValue> list = dataInstance.getFieldValues(dataInstance.getShapes().get(0), field);
        list.clear();
        list.add(newValue);

        // MIME Type
        field = type.getField("MIME Type");
        newValue = new DataFieldValue(mimeType, field.getShape().getId(), InstanceId.UNKNOWN, null);

        list = dataInstance.getFieldValues(dataInstance.getShapes().get(0), field);
        list.clear();
        list.add(newValue);

        // Extension
        field = type.getField("Extension");
        newValue = new DataFieldValue(extension, field.getShape().getId(), InstanceId.UNKNOWN, null);

        list = dataInstance.getFieldValues(dataInstance.getShapes().get(0), field);
        list.clear();
        list.add(newValue);

        // Size
        field = type.getField("Size");
        newValue = new DataFieldValue(String.valueOf(size), field.getShape().getId(), InstanceId.UNKNOWN, null);

        list = dataInstance.getFieldValues(dataInstance.getShapes().get(0), field);
        list.clear();
        list.add(newValue);

        updateListener.onUpdate(dataInstance, Type.FieldData);
    }

    protected void init() {
    	super.init();
        fieldListener  = new FileEditorListener() {
            public void onEdit(String fileId, String mimeType, String extension, long size) {
                sendEdit(fileId, mimeType, extension, size);
            }
        };

        actionsButton = new ToolButton("x-tool-save");

        //getBody().setLayout(new RowLayout());
        addHeaderItem(actionsButton);
        setHeading("Image Details");
        actionsButton.addSelectionListener(
                new SelectionListener<IconButtonEvent>() {
            @Override
            public void componentSelected(IconButtonEvent ce) {
                Shape type = dataInstance.getShapes().get(0);
                DataField field = type.getField("File ID");
                List<DataFieldValue> values = dataInstance.getFieldValues(type, field);
                DataFieldValue val = null;
                if (values.size() == 1) {
                    val = values.get(0);
                }

                FileUploader editor = new FileUploader(FileType.Image, field, val, fieldListener);
                editor.show();
            }
        });

    }
}
