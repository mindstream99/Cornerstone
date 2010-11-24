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

package com.paxxis.chime.client.editor;

import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldSetEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.widget.Slider;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SliderField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.paxxis.chime.client.ChimeAsyncCallback;
import com.paxxis.chime.client.ChimeListStore;
import com.paxxis.chime.client.DataInputListener;
import com.paxxis.chime.client.DataInstanceComboBox;
import com.paxxis.chime.client.DataInstanceResponseObject;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceRequest;
import com.paxxis.chime.client.common.DataInstanceResponse;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.DataInstanceRequest.Depth;
import com.paxxis.chime.client.common.portal.PortletSpecification;
import com.paxxis.chime.client.widgets.ChimeWindow;
import com.paxxis.chime.client.widgets.charts.ChimeChartFactory;
import com.paxxis.chime.client.widgets.charts.ChimeChartFactory.ChartType;
import com.paxxis.chime.client.widgets.charts.ChimeChartFactory.ChartTypeModel;
import com.paxxis.cornerstone.base.InstanceId;

/**
 * 
 * @author Robert Englander
 *
 */
public class AnalyticPortletEditorWindow extends ChimeWindow
{
	public interface AnalyticPortletEditListener {
		public void onSave(DataInstance analytic, boolean autoUpdate, int updateFreq);
		public void onSave(DataInstance analytic, boolean autoUpdate, int updateFreq, ChartType chartType, String ChartTitle,
				int axisColumn, int valueColumn, Integer minValue, Integer maxValue);
	}
	
    private AnalyticPortletEditListener _listener;

    
    private FormPanel _form = new FormPanel();
    private Button _okButton;
    private Button _cancelButton;
    private DataInstanceComboBox instanceBox;
    private FieldSet chartFieldSet;
    private FieldSet autoUpdateFieldSet;
    private SliderField autoUpdateRate;
    private InstanceId dataShape;
    private DataInstance selectedInstance = null;

    // charting options
    ChimeListStore<ChartTypeModel> chartStore;
    ComboBox<ChartTypeModel> chartBox;
    TextField<String> titleField;
    TextField<Integer> axisColumnField;
    TextField<Integer> valueColumnField;
    TextField<Integer> minField;
    TextField<Integer> maxField;
    ChartType chartType = ChartType.Pie;
    String chartTitle = "";
    int chartAxisCol = -1;
    int chartValueCol = -1;
    Integer maxValue = null;
    Integer minValue = null;
    
    public AnalyticPortletEditorWindow(InstanceId shape, PortletSpecification spec, AnalyticPortletEditListener listener) {
    	this(shape, listener);
    	initialize(spec);
    }
    
    public AnalyticPortletEditorWindow(InstanceId shape, AnalyticPortletEditListener listener) {
        _listener = listener;
        dataShape = shape;
    	setHeading("Select Analytic");
    	setModal(true);
        setMaximizable(false);
        setMinimizable(false);
        setClosable(false);
        setResizable(false);
        setShadow(false);
        setWidth(400);
    }
    
    private void initialize(final PortletSpecification spec) {
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
                                    if (instance.getShapes().get(0).getId().equals(Shape.ANALYTIC_ID)) {
                                    	setDefaults(instance, spec);
                                    }
                                }
                            }
                        }
                    };

                    DataInstanceRequest req = new DataInstanceRequest();
                    req.setDepth(Depth.Deep);
                    req.setIds(InstanceId.create(spec.getProperty("instanceId").toString()));
                    req.setUser(ServiceManager.getActiveUser());
                    ServiceManager.getService().sendDataInstanceRequest(req, callback);
                }
            }
        );
    }
    
    private void setDefaults(DataInstance instance, PortletSpecification spec) {
    	// the current analytic
    	instanceBox.applyInput(instance.getName());

    	Object obj = spec.getProperty("autoUpdate");
    	if (obj != null) {
    		boolean autoUpdate = obj.toString().equals("true");
            autoUpdateFieldSet.setExpanded(autoUpdate);
    		
            obj = spec.getProperty("updateFreq");
            if (obj != null) {
            	int freq = (Integer.parseInt(obj.toString()));
            	autoUpdateRate.getSlider().setValue(freq);
            }
    	}

    	obj = spec.getProperty("chartType");
    	if (obj != null) {
    		chartFieldSet.setExpanded(true);

    		ChartTypeModel m = null;
    		for (ChartTypeModel model : chartStore.getModels()) {
    			String typeName = model.getChartType().toString();
    			String name = obj.toString();
    			if (typeName.equals(name)) {
    				m = model;
    				break;
    			}
    		}

    		chartBox.setValue(m);
    		
    		chartTitle = spec.getProperty("chartTitle").toString();
        	titleField.setValue(chartTitle);
    		
        	chartAxisCol = Integer.parseInt(spec.getProperty("chartAxisColumn").toString());
        	axisColumnField.setValue(chartAxisCol);
    		
        	chartValueCol = Integer.parseInt(spec.getProperty("chartValueColumn").toString());
        	valueColumnField.setValue(chartValueCol);
        	
        	Object object = spec.getProperty("minValue");
        	if (object != null) {
        		minValue = Integer.parseInt(object.toString());
        		minField.setValue(minValue);
        	}

        	object = spec.getProperty("maxValue");
        	if (object != null) {
        		maxValue = Integer.parseInt(object.toString());
        		maxField.setValue(maxValue);
        	}

        	validate();
    	}
    }
    
    protected void init() {
        _form.setHeaderVisible(false);
        _form.setBorders(false);
        _form.setBodyBorder(false);
        _form.setStyleAttribute("padding", "0");
        _form.setButtonAlign(HorizontalAlignment.CENTER);
        _form.setFrame(true);
        _form.setFieldWidth(350);
        _form.setHideLabels(true);

        DataInputListener listener = new DataInputListener()
        {
            public void onDataInstance(DataInstance instance) 
            {
            	selectedInstance = instance;
            	validate();
            }

            public void onStringData(String text) 
            {
            	selectedInstance = null;
            	validate();
            }
        };
        
        instanceBox = new DataInstanceComboBox(listener, dataShape, true, false, false);

        new KeyNav(instanceBox) 
        {
            public void onKeyPress(ComponentEvent ce) 
            {
                DeferredCommand.addCommand(
                    new Command()
                    {
                        public void execute()
                        {
                            validate();
                        }
                    }
                );
            }

        };

        _form.add(instanceBox);
        
        autoUpdateFieldSet = new FieldSet();
        autoUpdateFieldSet.setHeading("Auto Update");
        autoUpdateFieldSet.setCheckboxToggle(true);
        autoUpdateFieldSet.setExpanded(false);
        Listener<FieldSetEvent> l = new Listener<FieldSetEvent>() {
            public void handleEvent(FieldSetEvent evt) {
            	validate();
            }
        };
        autoUpdateFieldSet.addListener(Events.Expand, l);
        autoUpdateFieldSet.addListener(Events.Collapse, l);
        FormLayout fl = new FormLayout();
        fl.setLabelWidth(100);
        autoUpdateFieldSet.setLayout(fl);
        
        Slider slider = new Slider();
        slider.setMinValue(10);
        slider.setMaxValue(60);
        slider.setValue(30);
        slider.setIncrement(1);
        slider.setMessage("Update every {0} seconds");
        slider.setData("text", "");
        autoUpdateRate = new SliderField(slider);
        autoUpdateRate.setFieldLabel("Update Freq");
        autoUpdateFieldSet.add(autoUpdateRate);
        
        chartFieldSet = new FieldSet();
        chartFieldSet.setHeading("Render As Chart");
        chartFieldSet.setCheckboxToggle(true);
        chartFieldSet.setExpanded(false);
        l = new Listener<FieldSetEvent>() {
            public void handleEvent(FieldSetEvent evt) {
            	validate();
            }
        };
        chartFieldSet.addListener(Events.Expand, l);
        chartFieldSet.addListener(Events.Collapse, l);

        fl = new FormLayout();
        fl.setLabelWidth(100);
        chartFieldSet.setLayout(fl);
        
        chartStore = ChimeChartFactory.getListStore();
        chartBox = new ComboBox<ChartTypeModel>();
        chartBox.setStore(chartStore);
        chartBox.setEditable(false);
        chartBox.setDisplayField("name");
        chartBox.setFieldLabel("Chart Type");
        chartBox.addSelectionChangedListener(
            new SelectionChangedListener<ChartTypeModel>()
            {
                @Override
                public void selectionChanged(final SelectionChangedEvent evt) 
                {
                	chartType = ((ChartTypeModel)evt.getSelectedItem()).getChartType();
                	validate();
                }
            }
        );
        chartBox.select(chartStore.getAt(0));
        chartFieldSet.add(chartBox);
        
        titleField = new TextField<String>();
        titleField.setFieldLabel("Title");
        titleField.setAllowBlank(true);
        new KeyNav(titleField) {
            public void onKeyPress(final ComponentEvent ce) {
            	DeferredCommand.addCommand(
            		new Command() {
            			public void execute() {
            				String val = titleField.getRawValue();
            				if (val == null) {
            					chartTitle = "";
            				} else {
            					chartTitle = val.trim();
            				}
            				
            				validate();
            			}
            		}
            	);
            }
        };
        chartFieldSet.add(titleField);
        
        axisColumnField = new TextField<Integer>();
        axisColumnField.setFieldLabel("Axis Column");
        axisColumnField.setAllowBlank(true);
        new KeyNav(axisColumnField) {
            public void onKeyPress(final ComponentEvent ce) {
            	DeferredCommand.addCommand(
            		new Command() {
            			public void execute() {
            				String val = axisColumnField.getRawValue();
            				if (val == null) {
            					chartAxisCol = -1;
            				} else {
            					chartAxisCol = Integer.valueOf(val.trim());
            				}
            				
            				validate();
            			}
            		}
            	);
            }
        };
        chartFieldSet.add(axisColumnField);
        
        valueColumnField = new TextField<Integer>();
        valueColumnField.setFieldLabel("Value Column");
        valueColumnField.setAllowBlank(true);
        new KeyNav(valueColumnField) {
            public void onKeyPress(final ComponentEvent ce) {
            	DeferredCommand.addCommand(
            		new Command() {
            			public void execute() {
            				String val = valueColumnField.getRawValue();
            				if (val == null) {
            					chartValueCol = -1;
            				} else {
            					chartValueCol = Integer.valueOf(val.trim());
            				}
            				
            				validate();
            			}
            		}
            	);
            }
        };
        chartFieldSet.add(valueColumnField);
        
        minField = new TextField<Integer>();
        minField.setFieldLabel("Minimum Value");
        minField.setAllowBlank(true);
        new KeyNav(minField) {
            public void onKeyPress(final ComponentEvent ce) {
            	DeferredCommand.addCommand(
            		new Command() {
            			public void execute() {
            				String val = minField.getRawValue();
            				if (val == null) {
            					minValue = null;
            				} else {
            					minValue = Integer.valueOf(val.trim());
            				}
            				
            				validate();
            			}
            		}
            	);
            }
        };
        chartFieldSet.add(minField);
        
        maxField = new TextField<Integer>();
        maxField.setFieldLabel("Maximum Value");
        maxField.setAllowBlank(true);
        new KeyNav(maxField) {
            public void onKeyPress(final ComponentEvent ce) {
            	DeferredCommand.addCommand(
            		new Command() {
            			public void execute() {
            				String val = maxField.getRawValue();
            				if (val == null) {
            					maxValue = null;
            				} else {
            					maxValue = Integer.valueOf(val.trim());
            				}
            				
            				validate();
            			}
            		}
            	);
            }
        };
        chartFieldSet.add(maxField);
        
        _form.add(autoUpdateFieldSet);
        _form.add(chartFieldSet);

        _okButton = new Button("Ok");
        _okButton.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent evt) 
                {
                	boolean autoUpdate = autoUpdateFieldSet.isExpanded();
                	int updateFreq = -1;
                	if (autoUpdate) {
                		updateFreq = autoUpdateRate.getSlider().getValue();
                	}
                	if (chartFieldSet.isExpanded()) {
                		_listener.onSave(selectedInstance, 
                				autoUpdate, updateFreq, chartType, chartTitle, chartAxisCol, chartValueCol,
                				minValue, maxValue);
                	} else {
                    	_listener.onSave(selectedInstance, autoUpdate, updateFreq);
                	}
                	hide();
                }
            }
        );

        _cancelButton = new Button("Cancel");
        _cancelButton.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent evt) 
                {
                    hide();
                }
            
            }
        );

        _form.addButton(_okButton);
        _form.addButton(_cancelButton);
        
        add(_form);
        instanceBox.focus();

        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    validate();
                }
            }
        );
        
    }

    private boolean validate()
    {
    	boolean valid = (selectedInstance != null);
    	boolean chartValid = true;
    	if (chartFieldSet.isExpanded()) {
    		chartValid = (chartTitle.length() > 0 &&
    				chartAxisCol > 0 && chartValueCol > 0 && chartAxisCol != chartValueCol);
    	}
    	
        _okButton.setEnabled(valid && chartValid);
        return valid;
    }
}
