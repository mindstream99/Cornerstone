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

import com.extjs.gxt.ui.client.core.Template;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.paxxis.chime.client.DataInstanceResponseObject;
import com.paxxis.chime.client.LoginResponseObject;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceManagerAdapter;
import com.paxxis.chime.client.ServiceManagerListener;
import com.paxxis.chime.client.Utils;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceRequest;
import com.paxxis.chime.client.common.DataInstanceResponse;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.DataInstanceRequest.Depth;
import com.paxxis.chime.client.common.portal.PortletSpecification;
import com.paxxis.chime.client.widgets.InterceptedHtml;

/**
 *
 * @author Robert Englander
 */
public class DataInstancePortlet extends PortletContainer
{
    public static class DataInstancePortletProxy extends LayoutProxyPortlet {

        public DataInstancePortletProxy(PortletSpecification spec, LayoutProxyListener listener) {
            super(spec, listener);
        }

        @Override
        protected void init() {
            super.init();
            setHeading("Data Field");
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
                                        InterceptedHtml _html = new InterceptedHtml();

                                        String fieldName = getSpecification().getProperty("fieldName").toString();
                                        String content = "<b>Instance Name:</b>&nbsp;&nbsp;" + instance.getName() +
                                                "<br><b>Description:</b>&nbsp;&nbsp;" + instance.getDescription() +
                                                "<br><b>Data Field:</b>&nbsp;&nbsp;" + instance.getShapes().get(0).getName() + "." + fieldName;
                                        _html.setHtml(content);

                                        getPropertiesContainer().add(_html);
                                        getPropertiesContainer().layout();
                                    }
                                }
                            }

                            public void onFailure(Throwable caught)
                            {
                            }
                        };

                        DataInstanceRequest req = new DataInstanceRequest();
                        req.setDepth(Depth.Shallow);
                        req.setIds(InstanceId.create(getSpecification().getProperty("instanceId").toString()));
                        req.setUser(ServiceManager.getActiveUser());
                        ServiceManager.getService().sendDataInstanceRequest(req, callback);
                    }
                }
            );
        }

        @Override
        protected void onEdit() {
        }
    }

    private static final String NOLINKTEMPLATE = "<div id='portal'>{content}</div>";
    private static final String LINKTEMPLATE = "<div id='portal'>{content}&nbsp;&nbsp;{link}</div>";
    
    private static Template _noLinkTemplate;
    private static Template _linkTemplate;

    static
    {
        _noLinkTemplate = new Template(NOLINKTEMPLATE);
        _noLinkTemplate.compile();
        _linkTemplate = new Template(LINKTEMPLATE);
        _linkTemplate.compile();
    }

    private ToolButton _refreshButton = null;
    private InstanceId _instanceId = InstanceId.create("-1");
    private String _fieldName = null;
    private boolean showHeader;
    
    private InterceptedHtml _html = null;
    private ServiceManagerListener _serviceManagerListener = null;

    public DataInstancePortlet(PortletSpecification spec, HeaderType type)
    {
        super(spec, type, true);
        showHeader = getHeaderType() != HeaderType.None;
    }
    
    @Override
    public void destroy()
    {
        super.destroy();
        
        ServiceManager.removeListener(_serviceManagerListener);
    }

    protected void init()
    {
    	super.init();
        getBody().setLayout(new RowLayout());

        _refreshButton = new ToolButton("x-tool-refresh", 
                new SelectionListener<IconButtonEvent>() 
        {  
            @Override  
             public void componentSelected(IconButtonEvent ce) 
             {  
                runQuery();
             }  
        });
        
        addHeaderItem(_refreshButton);

        // calling this after setting up our own tool buttons so that the expand and
        // collapse buttons appear after ours
        //setExpandable();
        
        _html = new InterceptedHtml();
        
        if (showHeader)
        {
            getBody().add(_html, new RowData(1, -1, new Margins(5)));
        }
        else
        {
            getBody().add(_html, new RowData(1, -1));
        }
        
        _serviceManagerListener = new ServiceManagerAdapter()
        {
            public void onLoginResponse(LoginResponseObject resp) 
            {
                runQuery();
            }

            public void onLogout() 
            {
                runQuery();
            }
        };
        
        ServiceManager.addListener(_serviceManagerListener);
    }
    
    public void setDataInstance(InstanceId instanceId, String field)
    {
    	_instanceId = instanceId;
        _fieldName = field;

        Runnable r = new Runnable() {
    		public void run() {
    	        runQuery();
    		}
    	};
    	
    	if (isRendered()) {
    		r.run();
    	} else {
    		addPostRenderRunnable(r);
    	}
    }
    
    private void showDataInstance(DataInstance instance)
    {
        if (showHeader)
        {
            setHeading(instance.getName());
        }
        
        DataField dataField = instance.getShapes().get(0).getField(_fieldName);
        List<DataFieldValue> values = instance.getFieldValues(instance.getShapes().get(0), dataField);
        if (values.size() > 0)
        {
            DataFieldValue value = values.get(0);
            String text = value.getValue().toString();
            
            Params params = new Params();
            params.set("content", text);

            if (showHeader)
            {
                String link = Utils.toHoverUrl(instance, "Details...");
                //String link = "<a href=\"" + GWT.getHostPageBaseURL() +
                //     "#detail:" + instance.getTypes().getId() + "-" + instance.getId() + "\" target=\"_self\">Get Details</a>";
                params.set("link", link);
                String content = _linkTemplate.applyTemplate(params);
                _html.setHtml(content);
            }
            else
            {
                String content = _noLinkTemplate.applyTemplate(params);
                _html.setHtml(content);
            }
        }
    }
    
    private void runQuery()
    {
        final AsyncCallback callback = new AsyncCallback() {
            public void onSuccess(final Object result) 
            {
                boolean isEmpty = true;
                
                DataInstanceResponseObject resp = (DataInstanceResponseObject)result;
                if (resp.isResponse())
                {
                    final DataInstanceResponse response = resp.getResponse();
                    List<DataInstance> instances = response.getDataInstances();
                    if (instances.size() > 0)
                    {
                        DataInstance instance = instances.get(0);
                        showDataInstance(instance);
                        isEmpty = false;
                    }
                }
        
                setVisible(!isEmpty);
                layout();
                /*
                if (isEmpty)
                {
                    _html.setHtml("");
                    if (_showHeader)
                    {
                        setHeading("---");
                    }
                }
                
                layout();
                */
            }
            
            public void onFailure(Throwable caught) 
            {
            }
        };
        
        DataInstanceRequest req = new DataInstanceRequest();
        req.setDepth(Depth.Deep);
        req.setIds(_instanceId);
        req.setUser(ServiceManager.getActiveUser());
        ServiceManager.getService().sendDataInstanceRequest(req, callback);
    }
}
