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
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.paxxis.chime.client.ChimeAsyncCallback;
import com.paxxis.chime.client.DataInstanceResponseObject;
import com.paxxis.chime.client.LoginResponseObject;
import com.paxxis.chime.client.PaginatedResultsPanel;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceManagerAdapter;
import com.paxxis.chime.client.ServiceManagerListener;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceRequest;
import com.paxxis.chime.client.common.DataInstanceResponse;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.NamedSearch;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.DataInstanceRequest.Depth;
import com.paxxis.chime.client.common.portal.PortletSpecification;
import com.paxxis.chime.client.editor.DataInstanceSelectionListener;
import com.paxxis.chime.client.editor.DataInstanceSelectionWindow;
import com.paxxis.chime.client.widgets.InterceptedHtml;

/**
 *
 * @author Robert Englander
 */
public class NamedSearchPortlet extends PortletContainer {

    public static class NamedSearchPortletProxy extends LayoutProxyPortlet {
        InterceptedHtml _html;

        public NamedSearchPortletProxy(PortletSpecification spec, LayoutProxyListener listener) {
            super(spec, listener);
        }

        @Override
        protected void init() {
            super.init();
            setHeading("Named Search");
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
                        final ChimeAsyncCallback<DataInstanceResponseObject> callback = 
                        		new ChimeAsyncCallback<DataInstanceResponseObject>() {
                            public void onSuccess(DataInstanceResponseObject resp) {
                                if (resp.isResponse()) {
                                    final DataInstanceResponse response = resp.getResponse();
                                    List<DataInstance> instances = response.getDataInstances();
                                    if (instances.size() > 0)
                                    {
                                        DataInstance instance = instances.get(0);
                                        String content = "<b>Instance Name:</b>&nbsp;&nbsp;" + instance.getName() +
                                                "<br><b>Description:</b>&nbsp;&nbsp;" + instance.getDescription();
                                        _html.setHtml(content);

                                        getPropertiesContainer().layout();
                                    }
                                }
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
            DataInstanceSelectionWindow w = new DataInstanceSelectionWindow(Shape.NAMEDSEARCH_ID, 
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

    private ToolButton _refreshButton = null;
    private NamedSearch _namedSearch = null;
    private PaginatedResultsPanel _resultsPanel = null;

    private ServiceManagerListener _serviceManagerListener = null;

    public static void renderProperties(PortletSpecification spec) {
    }

    public NamedSearchPortlet(PortletSpecification spec)
    {
        super(spec, HeaderType.Shaded, true);
    }

    @Override
    public void destroy()
    {
        super.destroy();
        
        ServiceManager.removeListener(_serviceManagerListener);
    }

    @Override
    public void reset()
    {
        _resultsPanel.reset();
    }
    
    protected void init()
    {
    	super.init();
        getBody().setLayout(new FitLayout());

        _refreshButton = new ToolButton("x-tool-refresh", 
                new SelectionListener<IconButtonEvent>() 
        {  
            @Override  
             public void componentSelected(IconButtonEvent ce) 
             {  
                query();
             }  
        });
        
        addHeaderItem(_refreshButton);

        // calling this after setting up our own tool buttons so that the expand and
        // collapse buttons appear after ours
        //setExpandable();
        
        _resultsPanel = new PaginatedResultsPanel(PaginatedResultsPanel.Type.Short);
        getBody().add(_resultsPanel);
        
        _serviceManagerListener = new ServiceManagerAdapter()
        {
            public void onLoginResponse(LoginResponseObject resp) 
            {
                query();
            }

            public void onLogout() 
            {
                query();
            }
        };

        ServiceManager.addListener(_serviceManagerListener);
    }
    
    public DataInstanceRequest getCurrentRequest()
    {
        return (DataInstanceRequest)_resultsPanel.getPriorRequest();
    }
    
    public NamedSearch getNamedSearch()
    {
        return _namedSearch;
    }
    
    public void setNamedSearch(final NamedSearch namedSearch)
    {
    	Runnable r = new Runnable() {
    		public void run() {
    	        _namedSearch = namedSearch;
    	        query();

    	        DeferredCommand.addCommand(
    	            new Command() {
    	                public void execute() {
    	                    setHeadingById(_namedSearch.getId(), _namedSearch.getName());
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
    
    private void query()
    {
        if (_namedSearch != null)
        {
            DataInstanceRequest req = _namedSearch.getSearchCriteria().buildRequest(ServiceManager.getActiveUser(), 20);
            _resultsPanel.query(req);
        }
    }
    
    public void execute(final InstanceId instanceId)
    {
        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    final ChimeAsyncCallback<DataInstanceResponseObject> callback = 
                    		new ChimeAsyncCallback<DataInstanceResponseObject>() {
                        public void onSuccess(DataInstanceResponseObject resp) {
                            if (resp.isResponse()) {
                                final DataInstanceResponse response = resp.getResponse();
                                List<DataInstance> instances = response.getDataInstances();
                                if (instances.size() > 0)
                                {
                                    DataInstance instance = instances.get(0);
                                    if (instance instanceof NamedSearch)
                                    {
                                        NamedSearch named = (NamedSearch)instance;
                                        setNamedSearch(named);
                                    }
                                }
                            }
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
