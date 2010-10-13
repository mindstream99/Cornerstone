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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.paxxis.chime.client.ChimeListStore;
import com.paxxis.chime.client.DataInputListener;
import com.paxxis.chime.client.DataInstanceComboBox;
import com.paxxis.chime.client.SearchCriteriaOperatorModel;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.SearchFilter;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.DataInstanceRequest.Operator;
import com.paxxis.chime.client.widgets.ChimeWindow;

/**
 *
 * @author Robert Englander
 */
public class SearchFilterEditor extends ChimeWindow {
	
	public enum Type {
		Text,
		Number,
		Reference,
		Activity
	}
	
    public interface FilterListener {
        public void onSave(SearchFilter filter);
    }

    private FilterListener _listener = null;

    private ComboBox<SearchCriteriaOperatorModel> _operatorFieldComboBox;
    private ChimeListStore<SearchCriteriaOperatorModel> _operatorStore;
    private List<SearchCriteriaOperatorModel> _textOperatorList;
    private List<SearchCriteriaOperatorModel> _numericOperatorList;
    private List<SearchCriteriaOperatorModel> _referenceOperatorList;
    private List<SearchCriteriaOperatorModel> _activityOperatorList;
    private DataInstanceComboBox _instanceBox;
    
    private FormPanel _form;
    private TextField<String> textValueField = null;
    private TextField<Number> numberValueField = null;
    private DateField dateField = null;
    private Button _okButton;
    private Button _cancelButton;
    private Html _errorLabel;

    private Type filterType;
    private SearchFilter filter;
    private InstanceId shapeId = null;
    private Shape shape = null;
    
    public SearchFilterEditor(Type type, SearchFilter filter, FilterListener listener) {
    	this(type, filter, (InstanceId)null, listener);
    }

    public SearchFilterEditor(Type type, SearchFilter filter, Shape shape, FilterListener listener) {
    	this(type, filter, (InstanceId)null, listener);
    	this.shape = shape;
    }
    
    public SearchFilterEditor(Type type, SearchFilter filter, InstanceId shapeId, FilterListener listener) {
    	filterType = type;
        this.filter = filter;
        _listener = listener;
        this.shapeId = shapeId;
        baseStyle = "x-popup";

        setModal(true);
    	setHeaderVisible(false);
        setShadow(false);
        setBorders(false);
        setResizable(false);
        setClosable(false);
        setConstrain(true);
        setBodyBorder(false);
        setWidth(290);
    }

    public String getText()
    {
        return textValueField.getRawValue();
    }

    public void show(Component parent) {
        int x = parent.getAbsoluteLeft();
        int y = 1 + parent.getAbsoluteTop() + parent.getOffsetHeight();
        setPosition(x, y);
        show();
    }
    
    protected void init()
    {
    	_form = new FormPanel();
        _form.setHeaderVisible(false);
        _form.setBorders(false);
        _form.setBodyBorder(false);
        _form.setStyleAttribute("padding", "0");
        _form.setButtonAlign(HorizontalAlignment.CENTER);
        _form.setFrame(true);
        _form.setFieldWidth(260);
        _form.setHideLabels(true);

        Html label = new Html("<div id='endslice-form-label'>Create filter where <b><i>" 
        		+ filter.getDataField().getName() + "</i></b>...<br>&nbsp;</div>");
        _form.add(label);
        
        _operatorFieldComboBox = new ComboBox<SearchCriteriaOperatorModel>();
        _operatorFieldComboBox.setStore(_operatorStore);
        _operatorFieldComboBox.setEditable(false);
        _operatorFieldComboBox.setDisplayField("description");
        _operatorFieldComboBox.setHideLabel(true);
        _form.add(_operatorFieldComboBox);
        
        _operatorFieldComboBox.addSelectionChangedListener(
            new SelectionChangedListener<SearchCriteriaOperatorModel>()
            {
                @Override
                public void selectionChanged(final SelectionChangedEvent evt) 
                {
                    SearchCriteriaOperatorModel model = (SearchCriteriaOperatorModel)evt.getSelectedItem();

                    if (model != null)
                    {
                        Operator operator = model.getOperator();
                        filter.setOperator(operator);
                        boolean showDateField = operator == Operator.AfterDate ||
	                        operator == Operator.OnOrAfterDate ||
	                        operator == Operator.OnDate ||
	                        operator == Operator.OnOrBeforeDate ||
	                        operator == Operator.BeforeDate;
                        dateField.setVisible(showDateField);
                    }
                }
            }
        );

        if (filterType == Type.Text) {
            textValueField = new TextField<String>();
            textValueField.setHideLabel(true);
            _form.add(textValueField);

            new KeyNav(textValueField) {
                public void onKeyPress(final ComponentEvent ce) {
                	onValueChange();
                }
                
                public void onEnter(ComponentEvent evt) {
                	onOk();
                }
            };
        } else if (filterType == Type.Number) {
            numberValueField = new TextField<Number>();
            numberValueField.setHideLabel(true);
            _form.add(numberValueField);

            new KeyNav(numberValueField) {
                public void onKeyPress(final ComponentEvent ce) {
                	onValueChange();
                }
                
                public void onEnter(ComponentEvent evt) {
                	onOk();
                }
            };
        } else if (filterType == Type.Reference) {
            DataInputListener l = new DataInputListener() {
                public void onDataInstance(DataInstance instance) {
                	if (instance == null) {
                    	filter.setValue(null);
                	} else {
                    	filter.setValue(instance.getId(), instance.getName());
                	}
                	onValueChange();
                }
     
                public void onStringData(String text) {
                	filter.setValue("-1", null);
                	filter.setDataShape(null);
                	onValueChange();
                }
            };
        	
            if (shape != null) {
            	_instanceBox = new DataInstanceComboBox(l, shape, true, false, true);
            } else {
            	_instanceBox = new DataInstanceComboBox(l, shapeId, true, false, true);
            }
            
            _form.add(_instanceBox);
        } else {  // Activity
            dateField = new DateField();
            dateField.setVisible(false);
            _form.add(dateField);
            dateField.setHideLabel(true);
            Listener<FieldEvent> l = new Listener<FieldEvent>() {
    	        public void handleEvent(FieldEvent evt) {
    	        	Date dt = dateField.getValue();
    	        	if (dt != null) {
                        filter.setValue(dt);
                	} else {
                        filter.setValue(null);
                	}

                	validate();
    	        }
    	    };

            dateField.addListener(Events.KeyPress, l);
            dateField.addListener(Events.Valid, l);
            dateField.addListener(Events.Invalid, l);
            dateField.addListener(Events.Change, l);
        }

        _errorLabel = new Html("<div id='endslice-error-label'>&nbsp;</div>");
        _form.add(_errorLabel);

        _okButton = new Button("Ok");
        _okButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                	onOk();
                }
            }
        );

        _form.addButton(_okButton);
        
        _cancelButton = new Button("Cancel");
        _cancelButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                    hide();
                }
            }
        );
        
        _form.addButton(_cancelButton);
        add(_form);
        validate();
    	initialize();
    }

    private void onOk() {
    	validate();
    	if (_okButton.isEnabled()) {
            _listener.onSave(filter);
            hide();
    	}
    }
    
    private void onValueChange() {
        DeferredCommand.addCommand(
            new Command() {
                public void execute() {
                	switch (filterType) {
	                	case Text:
	                    	filter.setValue(textValueField.getRawValue().trim());
	                		break;
	                	case Number:
	                    	filter.setValue(numberValueField.getRawValue().trim());
	                		break;
	                	case Reference:
	                		break;
	                	case Activity:
	                		break;
                	}

                	validate();
                }
            }
        );
    }

    private void validate() {
        String error = "<div id='endslice-error-label'>&nbsp;</div>";
        boolean isValid = true;

        String val = null;
    	switch (filterType) {
	    	case Text:
	    		val = textValueField.getRawValue().trim(); 
	    		isValid = val.length() > 0;
	    		if (!isValid) {
	    			error = "<div id='endslice-error-label'>Filter value can't be empty.</div>";
	    		}
	    		break;
	    	case Number:
	    		val = numberValueField.getRawValue().trim(); 
	    		try {
	    			Double.parseDouble(val);
	    		} catch (Exception e) {
	    			isValid = false;
	    			error = "<div id='endslice-error-label'>Filter value must be a number.</div>";
	    		}
	    		break;
	    	case Reference:
	    		if (filter.getValue() == null || "-1".equals(filter.getValue())) {
	    			isValid = false;
	    			error = "<div id='endslice-error-label'>Please select a filter value.</div>";
	    		}
	    		break;
	    	case Activity:
	    		break;
    	}

    	_errorLabel.setHtml(error);
    	_okButton.setEnabled(isValid);
    }
    
    public void initialize()
    {
        _operatorStore = new ChimeListStore<SearchCriteriaOperatorModel>();
        _textOperatorList = new ArrayList<SearchCriteriaOperatorModel>();
        _numericOperatorList = new ArrayList<SearchCriteriaOperatorModel>();
        _referenceOperatorList = new ArrayList<SearchCriteriaOperatorModel>();
        _activityOperatorList = new ArrayList<SearchCriteriaOperatorModel>();
        
        _textOperatorList.add(new SearchCriteriaOperatorModel(Operator.Contains));
        _textOperatorList.add(new SearchCriteriaOperatorModel(Operator.Like));
        _textOperatorList.add(new SearchCriteriaOperatorModel(Operator.StartsWith));

        _numericOperatorList.add(new SearchCriteriaOperatorModel(Operator.Equals));
        _numericOperatorList.add(new SearchCriteriaOperatorModel(Operator.GreaterThan));
        _numericOperatorList.add(new SearchCriteriaOperatorModel(Operator.GreaterThanOrEquals));
        _numericOperatorList.add(new SearchCriteriaOperatorModel(Operator.LessThan));
        _numericOperatorList.add(new SearchCriteriaOperatorModel(Operator.LessThanOrEquals));
        _numericOperatorList.add(new SearchCriteriaOperatorModel(Operator.NotEquals));

        _referenceOperatorList.add(new SearchCriteriaOperatorModel(Operator.Reference));
        _referenceOperatorList.add(new SearchCriteriaOperatorModel(Operator.NotReference));
        
        _activityOperatorList.add(new SearchCriteriaOperatorModel(Operator.BeforeDate));
        _activityOperatorList.add(new SearchCriteriaOperatorModel(Operator.OnOrBeforeDate));
        _activityOperatorList.add(new SearchCriteriaOperatorModel(Operator.OnDate));
        _activityOperatorList.add(new SearchCriteriaOperatorModel(Operator.AfterDate));
        _activityOperatorList.add(new SearchCriteriaOperatorModel(Operator.OnOrAfterDate));
        _activityOperatorList.add(new SearchCriteriaOperatorModel(Operator.Past24Hours));
        _activityOperatorList.add(new SearchCriteriaOperatorModel(Operator.Past3Days));
        _activityOperatorList.add(new SearchCriteriaOperatorModel(Operator.Past7Days));
        _activityOperatorList.add(new SearchCriteriaOperatorModel(Operator.Past30Days));

        switch (filterType) {
	        case Text:
	            _operatorStore.add(_textOperatorList);
	        	break;
	        case Number:
	            _operatorStore.add(_numericOperatorList);
	        	break;
	        case Reference:
	            _operatorStore.add(_referenceOperatorList);
	        	break;
	        case Activity:
	            _operatorStore.add(_activityOperatorList);
	        	break;
        }
        
        _operatorFieldComboBox.setStore(_operatorStore);
        
        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                	SearchCriteriaOperatorModel model = _operatorStore.getAt(0);
                    _operatorFieldComboBox.setValue(model);
                    textValueField.focus();
                }
            }
        );
    }
}
