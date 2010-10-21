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
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.paxxis.chime.client.ChimeAsyncCallback;
import com.paxxis.chime.client.DataInstanceResponseObject;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceManagerListener;
import com.paxxis.chime.client.ServiceResponseObject;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceRequest;
import com.paxxis.chime.client.common.DataInstanceResponse;
import com.paxxis.chime.client.common.ErrorMessage;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.RunCALScriptRequest;
import com.paxxis.chime.client.common.RunCALScriptResponse;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.client.common.DataInstanceRequest.Depth;
import com.paxxis.chime.client.common.cal.IValue;
import com.paxxis.chime.client.common.cal.Table;
import com.paxxis.chime.client.common.portal.PortletSpecification;
import com.paxxis.chime.client.editor.AnalyticPortletEditorWindow;
import com.paxxis.chime.client.widgets.AnalyticDetailPanel;
import com.paxxis.chime.client.widgets.ChimeMessageBox;
import com.paxxis.chime.client.widgets.InterceptedHtml;
import com.paxxis.chime.client.widgets.charts.ChimeChartFactory;
import com.paxxis.chime.client.widgets.charts.ChimeChartFactory.ChartType;

/**
 *
 * @author Robert Englander
 */
public class AnalyticPortlet extends PortletContainer {

    public static class AnalyticPortletProxy extends LayoutProxyPortlet {
        InterceptedHtml _html;

        public AnalyticPortletProxy(PortletSpecification spec, LayoutProxyListener listener) {
            super(spec, listener);
        }

        @Override
        protected void init() {
            super.init();
            setHeading("Analytic");
            _html = new InterceptedHtml();
            getPropertiesContainer().add(_html);
            render();
        }

        private void render() {
            DeferredCommand.addCommand(
                    new Command() {
                        public void execute() {
                            final ChimeAsyncCallback<DataInstanceResponseObject> callback = new ChimeAsyncCallback<DataInstanceResponseObject>() {
                                public void onSuccess(final DataInstanceResponseObject resp) {
                                    if (resp.isResponse()) {
                                        final DataInstanceResponse response = resp.getResponse();
                                        List<DataInstance> instances = response.getDataInstances();
                                        if (instances.size() > 0) {
                                            DataInstance instance = instances.get(0);
                                            Object renderChart = getSpecification().getProperty("chartType");
                                            String content;
                                            if (renderChart != null) {
                                            	String tmp = getSpecification().getProperty("chartAxisColumn").toString();
                                            	int axisCol = Integer.valueOf(tmp);
                                            	tmp = getSpecification().getProperty("chartValueColumn").toString();
                                            	int valueCol = Integer.valueOf(tmp);
                                                content = "<b>Instance Name:</b>&nbsp;&nbsp;" + instance.getName() +
                                                "<br><b>Chart Type:</b>&nbsp;&nbsp;" + renderChart.toString() +
                                                "<br><b>Title:</b>&nbsp;&nbsp;" + getSpecification().getProperty("chartTitle") +
                                                "<br><b>Axis Column:</b>&nbsp;&nbsp;" + axisCol +
                                                "<br><b>Value Column:</b>&nbsp;&nbsp;" + valueCol;
                                            } else {
                                                content = "<b>Instance Name:</b>&nbsp;&nbsp;" + instance.getName() +
                                                "<br><b>Description:</b>&nbsp;&nbsp;" + instance.getDescription();
                                            }

                                            boolean au = getSpecification().getProperty("autoUpdate").toString().equals("true");
                                            if (au) {
                                                Object obj = getSpecification().getProperty("updateFreq");
                                                if (obj != null) {
                                                	String uf = obj.toString();
                                                	content += "<br><b>Auto Update:</b>&nbsp;&nbsp;" + uf + "&nbsp;seconds";
                                                }
                                            }

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
            AnalyticPortletEditorWindow w = new AnalyticPortletEditorWindow(Shape.ANALYTIC_ID, getSpecification(),
                new AnalyticPortletEditorWindow.AnalyticPortletEditListener() {
                    public void onSave(DataInstance analytic, boolean autoUpdate, int updateFreq) {
                        PortletSpecification pspec = getSpecification();
                        pspec.removeProperty("chartType");
                    	pspec.removeProperty("chartTitle");
                    	pspec.removeProperty("chartAxisColumn");
                    	pspec.removeProperty("chartValueColumn");
                        pspec.setPinned(false);
                        pspec.setShowHeader(true);
                        if (autoUpdate) {
                        	pspec.setProperty("autoUpdate", true);
                        	pspec.setProperty("updateFreq", updateFreq);
                        } else {
                        	pspec.setProperty("autoUpdate", false);
                        }
                        pspec.setProperty("instanceId", analytic.getId());
                        pspec.setProperty("height", 250);
                        getListener().onLayoutProxyEdit(pspec);
                        render();
                    }

    				public void onSave(DataInstance analytic, boolean autoUpdate, int updateFreq, ChartType chartType,
    						String chartTitle, int axisColumn, int valueColumn, Integer minValue, Integer maxValue) {

                        PortletSpecification pspec = getSpecification();
                        pspec.setPinned(false);
                        pspec.setShowHeader(true);
                        if (autoUpdate) {
                        	pspec.setProperty("autoUpdate", true);
                        	pspec.setProperty("updateFreq", updateFreq);
                        } else {
                        	pspec.setProperty("autoUpdate", false);
                        }
                        pspec.setProperty("instanceId", analytic.getId().getValue());
                        pspec.setProperty("height", 250);
                    	pspec.setProperty("chartType", chartType.toString());
                    	pspec.setProperty("chartTitle", chartTitle);
                    	pspec.setProperty("chartAxisColumn", axisColumn);
                    	pspec.setProperty("chartValueColumn", valueColumn);
                    	
                    	if (minValue != null) {
                        	pspec.setProperty("minValue", minValue.toString());
                    	}
                    	
                    	if (maxValue != null) {
                        	pspec.setProperty("maxValue", maxValue.toString());
                    	}

                    	getListener().onLayoutProxyEdit(pspec);
                        render();
    				}
                }
            );

            w.show();
        }
    }

    private ToolButton _refreshButton = null;
    private DataInstance _analytic = null;
    private AnalyticDetailPanel _resultsPanel = null;

    private ServiceManagerListener _serviceManagerListener = null;

    public static void renderProperties(PortletSpecification spec) {
    }

    public AnalyticPortlet(PortletSpecification spec)
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
        //_resultsPanel.reset();
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
                runScript();
             }
        });

        addHeaderItem(_refreshButton);

        Object au = getSpecification().getProperty("autoUpdate");
        if (au != null) {
            if (au.toString().equals("true")) {
            	Timer t = new Timer() {
            		public void run() {
            			runScript();
            		}
            	};
            	
            	Object f = getSpecification().getProperty("updateFreq");
            	int freq = 30000;
            	if (f != null) {
            		freq = 1000 * Integer.parseInt(f.toString());
            	}
            	t.scheduleRepeating(freq);
            	PortalUtils.registerTimer(t);
            }
        }
    }

    public DataInstance getDataInstance()
    {
        return _analytic;
    }

    private void setDataInstance(final DataInstance analytic)
    {
    	Runnable r = new Runnable() {
    		public void run() {
    	        _analytic = analytic;
    	        query();

    	        DeferredCommand.addCommand(
    	            new Command() {
    	                public void execute() {
    	                    setHeadingById(_analytic.getId(), _analytic.getName());
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
        if (_analytic != null)
        {
            _resultsPanel = new AnalyticDetailPanel(_analytic, -1, false);
            getBody().add(_resultsPanel);

            runScript();
        }
    }

    private void runScript() {
        ChimeAsyncCallback<ServiceResponseObject<RunCALScriptResponse>> callback = 
        			new ChimeAsyncCallback<ServiceResponseObject<RunCALScriptResponse>>() {
            public void onSuccess(ServiceResponseObject<RunCALScriptResponse> response) {
                if (response.isResponse()) {
                    RunCALScriptResponse resp = response.getResponse();
                    IValue result = resp.getResult();
                    if (result instanceof Table) {
                        Object renderChart = getSpecification().getProperty("chartType");
                        if (renderChart != null) {
                        	ChartType chartType = ChimeChartFactory.getChartType(renderChart.toString());
                        	String title = getSpecification().getProperty("chartTitle").toString();
                        	int axisCol = Integer.valueOf(getSpecification().getProperty("chartAxisColumn").toString());
                        	int valueCol = Integer.valueOf(getSpecification().getProperty("chartValueColumn").toString());
                        	
                        	Object object = getSpecification().getProperty("minValue");
                        	Integer minValue = null;
                        	if (object != null) {
                        		minValue = Integer.parseInt(object.toString());
                        	}
                        	
                        	object = getSpecification().getProperty("maxValue");
                        	Integer maxValue = null;
                        	if (object != null) {
                        		maxValue = Integer.parseInt(object.toString());
                        	}
                        	
                        	showChartResult((Table)result, chartType, title, axisCol, valueCol, minValue, maxValue);
                        } else {
                            showTableResult((Table)result);
                        }
                    }
                }
                else
                {
                    ErrorMessage msg = response.getError();
                    String text;
                    if (msg.getType() == ErrorMessage.Type.SessionExpiration) {
                        ServiceManager.logout();
                        text = "Your session has expired.  Please login again.";
                    } else {
                        text = msg.getMessage();
                    }

                    ChimeMessageBox.alert("Error", text, null);
                }
            }
        };

        if (_analytic != null) {
            User user = ServiceManager.getActiveUser();
            RunCALScriptRequest req = new RunCALScriptRequest();
            req.setData(_analytic);
            req.setUser(user);
            ServiceManager.getService().sendRunCALScriptRequest(req, callback);
        }
    }

    private void showTableResult(Table table) {
        _resultsPanel.showResult(table);
        getBody().layout();
    }

    private void showChartResult(Table table, ChartType type, String title, int axisCol, int valueCol,
    		Integer minValue, Integer maxValue) {
        _resultsPanel.showChartResult(table, type, title, axisCol, valueCol, minValue, maxValue);
        getBody().layout();
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
                                if (instances.size() > 0) {
                                    DataInstance instance = instances.get(0);
                                    if (instance.getShapes().get(0).getName().equals("Analytic")) {
                                        setDataInstance(instance);
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
