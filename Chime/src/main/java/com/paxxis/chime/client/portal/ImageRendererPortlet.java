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

import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.paxxis.chime.client.DataInstanceResponseObject;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceRequest;
import com.paxxis.chime.client.common.DataInstanceResponse;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.DataInstanceRequest.Depth;
import com.paxxis.chime.client.common.portal.PortletSpecification;
import com.paxxis.chime.client.editor.DataInstanceSelectionListener;
import com.paxxis.chime.client.editor.DataInstanceSelectionWindow;
import com.paxxis.chime.client.widgets.ImageContainer;
import com.paxxis.chime.client.widgets.InterceptedHtml;

/**
 *
 * @author Robert Englander
 */
public class ImageRendererPortlet extends PortletContainer {

    public static class ImageRendererPortletProxy extends LayoutProxyPortlet {
        InterceptedHtml _html;

        public ImageRendererPortletProxy(PortletSpecification spec, LayoutProxyListener listener) {
            super(spec, listener);
        }

        @Override
        protected void init() {
            super.init();
            setHeading("Image Renderer");
            _html = new InterceptedHtml();
            getPropertiesContainer().add(_html);
            render();
        }
        
        protected void render() {

            DeferredCommand.addCommand(
                new Command()
                {
                    public void execute()
                    {
                        final AsyncCallback callback = new AsyncCallback() {
                            public void onSuccess(final Object result)
                            {
                                DataInstanceResponseObject resp = (DataInstanceResponseObject)result;
                                if (resp.isResponse())
                                {
                                    final DataInstanceResponse response = resp.getResponse();
                                    List<DataInstance> instances = response.getDataInstances();
                                    if (instances.size() > 0)
                                    {
                                        DataInstance instance = instances.get(0);
                                        _html = new InterceptedHtml();
                                        String content = "<b>Instance Name:</b>&nbsp;&nbsp;" + instance.getName() +
                                                "<br><b>Description:</b>&nbsp;&nbsp;" + instance.getDescription();
                                        _html.setHtml(content);

                                        getPropertiesContainer().layout();
                                    }
                                }
                            }

                            public void onFailure(Throwable caught)
                            {
                            }
                        };

                        DataInstanceRequest req = new DataInstanceRequest();
                        req.setDepth(Depth.Deep);
                        req.setIds(InstanceId.create(getSpecification().getProperty("instanceId").toString()));
                        req.setUser(ServiceManager.getActiveUser());
                        ServiceManager.getService().sendDataInstanceRequest(req, callback);
                    }
                }
            );
        }

        @Override
        protected void onEdit() {
            DataInstanceSelectionWindow w = new DataInstanceSelectionWindow(Shape.IMAGE_ID,
                	getSpecification().getProperty("instanceId").toString(),
                new DataInstanceSelectionListener() {
                    public void onSave(DataInstance instance) {
                        PortletSpecification pspec = getSpecification();
                        pspec.setProperty("instanceId", instance.getId().getValue());
                        getListener().onLayoutProxyEdit(pspec);
                        render();
                    }
                }
            );

            w.show();
        }
    }

    private ImageContainer imageContainer;

    public ImageRendererPortlet(PortletSpecification spec) {
        super(spec, HeaderType.None, true);
    }

    public void execute(final InstanceId instanceId)
    {
        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    final AsyncCallback callback = new AsyncCallback() {
                        public void onSuccess(final Object result)
                        {
                            DataInstanceResponseObject resp = (DataInstanceResponseObject)result;
                            if (resp.isResponse())
                            {
                                final DataInstanceResponse response = resp.getResponse();
                                List<DataInstance> instances = response.getDataInstances();
                                if (instances.size() > 0)
                                {
                                    DataInstance instance = instances.get(0);
                                    Shape type = instance.getShapes().get(0);
                                    if (type.getName().equals("Image")) {
                                        DataField field = type.getField("File ID");
                                        List<DataFieldValue> vals = instance.getFieldValues(type, field);
                                        if (vals.size() == 1) {
                                            String id = vals.get(0).getName();
                                            getBody().removeAll();
                                            imageContainer = new ImageContainer(getBody(), id, false, null, false);
                                            getBody().add(imageContainer, new FlowData(5, 0, 5, 0));
                                            getBody().layout();
                                        }
                                    }

                                }
                            }
                        }

                        public void onFailure(Throwable caught)
                        {
                        }
                    };

                    DataInstanceRequest req = new DataInstanceRequest();
                    req.setDepth(Depth.Deep);
                    req.setIds(instanceId);
                    req.setUser(ServiceManager.getActiveUser());
                    ServiceManager.getService().sendDataInstanceRequest(req, callback);
                }
            }
        );

    }
}
