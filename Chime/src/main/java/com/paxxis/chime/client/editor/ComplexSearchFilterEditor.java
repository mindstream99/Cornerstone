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

import java.io.Serializable;
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
import com.paxxis.chime.client.ChimeAsyncCallback;
import com.paxxis.chime.client.ChimeListStore;
import com.paxxis.chime.client.DataFieldModel;
import com.paxxis.chime.client.DataInputListener;
import com.paxxis.chime.client.DataInstanceComboBox;
import com.paxxis.chime.client.SearchCriteriaOperatorModel;
import com.paxxis.chime.client.SearchFilterModifyListener;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ShapeResponseObject;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.SearchFilter;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.ShapeRequest;
import com.paxxis.chime.client.common.DataInstanceRequest.Operator;
import com.paxxis.chime.client.editor.SearchFilterEditor.FilterListener;
import com.paxxis.chime.client.widgets.ChimeWindow;

/**
 *
 * @author Robert Englander
 */
public class ComplexSearchFilterEditor extends ChimeWindow
{
    // the DataType field
    private DataInstanceComboBox _dataTypeComboBox;
    
    // filter entry panel pieces
    private ComboBox<DataFieldModel> _dataFieldComboBox;
    private ChimeListStore<DataFieldModel> _dataFieldStore;
    
    private ComboBox<SearchCriteriaOperatorModel> _operatorFieldComboBox;
    private ChimeListStore<SearchCriteriaOperatorModel> _operatorStore;
    private List<SearchCriteriaOperatorModel> _textOperatorList;
    private List<SearchCriteriaOperatorModel> _numericOperatorList;
    private List<SearchCriteriaOperatorModel> _dateOperatorList;
    private List<SearchCriteriaOperatorModel> _referenceOperatorList;
    private List<SearchCriteriaOperatorModel> _activityOperatorList;
    private DataInstanceComboBox _filterInput;
    
    private TextField<String> textField = null;
    private TextField<Number> numberField = null;
    private DateField dateField = null;
    
    private FormPanel form;
    private Button _okButton;
    private Button _cancelButton;
    private Html _errorLabel;

    private Html _filterLabel;
    private SearchFilter filter;
    private SearchFilterModifyListener _modifyListener;
    
    private FilterListener filterListener;
    
    public ComplexSearchFilterEditor(FilterListener listener)
    {
    	super();
    	filterListener = listener;
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

    public void show(Component parent) {
        int x = parent.getAbsoluteLeft();
        int y = 1 + parent.getAbsoluteTop() + parent.getOffsetHeight();
        setPosition(x, y);
        show();
    }

    protected void init()
    {
    	form = new FormPanel();
        form.setHeaderVisible(false);
        form.setBorders(false);
        form.setBodyBorder(false);
        form.setStyleAttribute("padding", "0");
        form.setButtonAlign(HorizontalAlignment.CENTER);
        form.setFrame(true);
        form.setFieldWidth(260);
        form.setHideLabels(true);

        filter = new SearchFilter();
        
        _operatorStore = new ChimeListStore<SearchCriteriaOperatorModel>();
        _textOperatorList = new ArrayList<SearchCriteriaOperatorModel>();
        _numericOperatorList = new ArrayList<SearchCriteriaOperatorModel>();
        _dateOperatorList = new ArrayList<SearchCriteriaOperatorModel>();
        _referenceOperatorList = new ArrayList<SearchCriteriaOperatorModel>();
        _activityOperatorList = new ArrayList<SearchCriteriaOperatorModel>();
        
        _textOperatorList.add(new SearchCriteriaOperatorModel(Operator.Contains));
        _textOperatorList.add(new SearchCriteriaOperatorModel(Operator.Like));
        _textOperatorList.add(new SearchCriteriaOperatorModel(Operator.StartsWith));

        _dateOperatorList.add(new SearchCriteriaOperatorModel(Operator.BeforeDate));
        _dateOperatorList.add(new SearchCriteriaOperatorModel(Operator.OnOrBeforeDate));
        _dateOperatorList.add(new SearchCriteriaOperatorModel(Operator.OnDate));
        _dateOperatorList.add(new SearchCriteriaOperatorModel(Operator.AfterDate));
        _dateOperatorList.add(new SearchCriteriaOperatorModel(Operator.OnOrAfterDate));
        _dateOperatorList.add(new SearchCriteriaOperatorModel(Operator.Past24Hours));
        _dateOperatorList.add(new SearchCriteriaOperatorModel(Operator.Past3Days));
        _dateOperatorList.add(new SearchCriteriaOperatorModel(Operator.Past7Days));
        _dateOperatorList.add(new SearchCriteriaOperatorModel(Operator.Past30Days));
        
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

        Html label = new Html("<div id='endslice-form-label'>Shape:</div>");
        form.add(label);

        DataInputListener l1 = new DataInputListener()
        {
            public void onDataInstance(DataInstance instance)
            {
            	if (instance != null) {
                    initCriteria(instance);
            	} else {
            		setupFields(null);
            	}
            }

            public void onStringData(String text) 
            {
                //setupFields(null);
            }
        };
        
        _dataTypeComboBox = new DataInstanceComboBox(l1, Shape.SHAPE_ID, true, false, true);
        form.add(_dataTypeComboBox);

        _dataFieldComboBox = new ComboBox<DataFieldModel>();
        _dataFieldStore = new ChimeListStore<DataFieldModel>();
        _dataFieldComboBox.setStore(_dataFieldStore);
        _dataFieldComboBox.setEditable(false);
        _dataFieldComboBox.setDisplayField("name");
        _dataFieldComboBox.addSelectionChangedListener(
            new SelectionChangedListener<DataFieldModel>()
            {
                @Override
                public void selectionChanged(SelectionChangedEvent evt) 
                {
                    DataFieldModel model = (DataFieldModel)evt.getSelectedItem();
                    if (model != null)
                    {
                        _operatorStore.removeAll();
                        _operatorFieldComboBox.setValue(null);

                        DataField field = model.getDataField();

                        filter.clear();
                        filter.setDataField(field);
                        updateState();

                        if (field.getName().equals("Name"))
                        {
                            _operatorStore.add(_textOperatorList);
                            _filterInput.setVisible(false);
                            textField.setVisible(true);
                            numberField.setVisible(false);
                            dateField.setVisible(false);
                            textField.setRawValue("");
                        }
                        else if (field.getName().equals("Description"))
                        {
                            _operatorStore.add(_textOperatorList);
                            _filterInput.setVisible(false);
                            textField.setVisible(true);
                            numberField.setVisible(false);
                            dateField.setVisible(false);
                            textField.setRawValue("");
                        }
                        else if (field.getName().equals("Average Rating"))
                        {
                            _operatorStore.add(_numericOperatorList);
                            _filterInput.setVisible(false);
                            textField.setVisible(false);
                            dateField.setVisible(false);
                            numberField.setVisible(true);
                            numberField.setRawValue("");
                        }
                        else if (field.getName().equals("Tag"))
                        {
                            _operatorStore.add(_referenceOperatorList);
                            _filterInput.setShape(Shape.TAG_ID, true);
                            _filterInput.setVisible(true);
                            textField.setVisible(false);
                            dateField.setVisible(false);
                            numberField.setVisible(false);
                        }
                        else if (field.getName().equals("Editor (User)"))
                        {
                            _operatorStore.add(_referenceOperatorList);
                            _filterInput.setShape(Shape.USER_ID, true);
                            _filterInput.setVisible(true);
                            textField.setVisible(false);
                            dateField.setVisible(false);
                            numberField.setVisible(false);
                        }
                        else if (field.getName().equals("Editor (Community)"))
                        {
                            _operatorStore.add(_referenceOperatorList);
                            _filterInput.setShape(Shape.COMMUNITY_ID, true);
                            _filterInput.setVisible(true);
                            textField.setVisible(false);
                            dateField.setVisible(false);
                            numberField.setVisible(false);
                        }
                        else if (field.getName().equals("Activity"))
                        {
                            _operatorStore.add(_activityOperatorList);
                            _filterInput.setVisible(false);
                            textField.setVisible(false);
                            dateField.setVisible(false);
                            numberField.setVisible(false);
                        }
                        else if (field.getName().equals("Created/Updated"))
                        {
                            _operatorStore.add(_activityOperatorList);
                            _filterInput.setVisible(false);
                            textField.setVisible(false);
                            dateField.setVisible(false);
                            numberField.setVisible(false);
                        }
                        else
                        {
                            Shape type = field.getShape();
                            if (type.isNumeric()) {
                                _operatorStore.add(_numericOperatorList);
                                _filterInput.setVisible(false);
                                textField.setVisible(false);
                                dateField.setVisible(false);
                                numberField.setVisible(true);
                                numberField.setRawValue("");
                            } else if (type.isDate()) {
                                _operatorStore.add(_dateOperatorList);
                                _filterInput.setVisible(false);
                                textField.setVisible(false);
                                numberField.setVisible(false);
                                dateField.setVisible(false); // operator choice drives this
                                dateField.setRawValue("");
                            } else if (type.isPrimitive()) {
                                _operatorStore.add(_textOperatorList);
                                _filterInput.setVisible(false);
                                textField.setVisible(true);
                                dateField.setVisible(false);
                                numberField.setVisible(false);
                                textField.setRawValue("");
                            } else {
                                _operatorStore.add(_referenceOperatorList);
                                _filterInput.setShape(type.getId(), !type.isPrimitive());
                                _filterInput.setVisible(true);
                                textField.setVisible(false);
                                dateField.setVisible(false);
                                numberField.setVisible(false);
                            }
                        }

                        _operatorFieldComboBox.setValue(_operatorStore.getAt(0));
                    }
                    
                    validate();
                }
            }
        );
        
        _filterLabel = new Html("<div id='endslice-form-label'>Create search filter where...</div>");
        form.add(_filterLabel);
        form.add(_dataFieldComboBox);

        _operatorFieldComboBox = new ComboBox<SearchCriteriaOperatorModel>();
        _operatorFieldComboBox.setStore(_operatorStore);
        _operatorFieldComboBox.setEditable(false);
        _operatorFieldComboBox.setDisplayField("description");
        form.add(_operatorFieldComboBox);
        
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
                        updateState();
                    }
                    
                    validate();
                }
            }
        );

        DataInputListener l2 = new DataInputListener() {
            public void onDataInstance(DataInstance instance) {
            	if (instance != null) {
                    filter.setValue(instance.getId(), instance.getName());
            	} else {
                    filter.setValue(null);
            	}

            	validate();
            }
 
            public void onStringData(String text) {
            	filter.setValue("-1");
            	validate();
            }
        };
            
        _filterInput = new DataInstanceComboBox(l2);
        _filterInput.setVisible(false);
        form.add(_filterInput);
        
        dateField = new DateField();
        dateField.setVisible(false);
        form.add(dateField);
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

        textField = new TextField<String>();
        textField.setVisible(false);
        form.add(textField);
        textField.setHideLabel(true);
        new KeyNav(textField) {
            public void onKeyPress(final ComponentEvent ce) {
            	DeferredCommand.addCommand(
            		new Command() {
            			public void execute() {
            				String text = textField.getRawValue();
                            applyFilterInput(text);
            			}
            		}
            	);
            }
            
            public void onEnter(ComponentEvent evt) {
            	onOk();
            }
        };
        
        numberField = new TextField<Number>();
        numberField.setVisible(false);
        form.add(numberField);
        numberField.setHideLabel(true);
        new KeyNav(numberField) {
            public void onKeyPress(final ComponentEvent ce) {
            	DeferredCommand.addCommand(
            		new Command() {
            			public void execute() {
            				String text = numberField.getRawValue();
                            applyFilterInput(text);
            			}
            		}
            	);
            }
            
            public void onEnter(ComponentEvent evt) {
            	onOk();
            }
        };
        
        _errorLabel = new Html("<div id='endslice-error-label'>&nbsp;</div>");
        form.add(_errorLabel);

        _okButton = new Button("Ok");
        _okButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                	onOk();
                }
            }
        );

        form.addButton(_okButton);
        
        _cancelButton = new Button("Cancel");
        _cancelButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                    hide();
                }
            }
        );
        
        form.addButton(_cancelButton);
        add(form);
        
        updateState();
        validate();
    }
    
    private void validate() {
        String error = "&nbsp;";

    	boolean enable = true;
    	if (filter.getDataShape() == null) {
    		enable = false;
    		error = "Please select a Shape.";
    	} else if (textField.isVisible()) {
    		Serializable val = filter.getValue();
    		enable = val != null && val.toString().length() > 0;
    		if (!enable) {
        		error = "Please enter a text value.";
    		}
    	} else if (numberField.isVisible()) {
    		Serializable val = filter.getValue();
    		if (val != null) {
    			try {
    				Double.parseDouble(val.toString());
    			} catch (Exception e) {
    				enable = false;
    				error = "Please enter a valid number.";
    			}
    		} else {
				enable = false;
				error = "Please enter a valid number.";
    		}
    	} else if (_filterInput.isVisible()) {
    		if (filter.getValue() == null) {
        		enable = false;
        		error = "Please select a value.";
    		} else {
        		enable = !filter.getValue().toString().equals("-1");
        		if (!enable) {
            		error = "Please select a value.";
        		}
    		}
    	}

    	error = "<div id='endslice-error-label'>" + error + "</div>";
    	_errorLabel.setHtml(error);
    	_okButton.setEnabled(enable);
    }
    
    private void onOk() {
    	filterListener.onSave(filter);
    	hide();
    }
    
    private void applyFilterInput(String text)
    {
        filter.setValue(text);
        updateState();
        validate();
    }
    
    protected void updateState()
    {
        if (filter.getDataShape() == null)
        {
            _filterLabel.setEnabled(false);
            _dataFieldComboBox.setEnabled(false);
            _operatorFieldComboBox.setEnabled(false);
            _filterInput.setEnabled(false);
            textField.setEnabled(false);
            numberField.setEnabled(false);
        }
        else
        {
            _filterLabel.setEnabled(true);
            _dataFieldComboBox.setEnabled(true);
            _operatorFieldComboBox.setEnabled(true);
            _filterInput.setEnabled(true);
            textField.setEnabled(true);
            numberField.setEnabled(true);
        }
    }
    
    protected void initCriteria(final DataInstance shape)
    {
        final ChimeAsyncCallback<ShapeResponseObject> callback = new ChimeAsyncCallback<ShapeResponseObject>() {
            public void onSuccess(ShapeResponseObject resp) { 
                Shape shape = resp.getResponse().getShape();

                // we don't want Internals
                if (shape.isPrimitive()) {
                    _dataTypeComboBox.invalidate(shape.getName() + " is not searchable");
                } else {
                    setupFields(shape);
                }
            }
        };

        // we've been handed a shallow instance, so we need to go get the
        // full datatype instance
        ShapeRequest request = new ShapeRequest();
        request.setId(shape.getId());

        ServiceManager.getService().sendShapeRequest(request, callback);
    }

    protected void setupFields(Shape type)
    {
        filter.setDataShape(type);
        _dataFieldStore.removeAll();
        
        DataField f = new DataField();
        f.setName("Name");
        _dataFieldStore.add(new DataFieldModel(f));
        f = new DataField();
        f.setName("Description");
        _dataFieldStore.add(new DataFieldModel(f));
        f = new DataField();
        f.setName("Average Rating");
        _dataFieldStore.add(new DataFieldModel(f));
        f = new DataField();
        f.setName("Tag");
        _dataFieldStore.add(new DataFieldModel(f));
        f = new DataField();
        f.setName("Editor (User)");
        _dataFieldStore.add(new DataFieldModel(f));
        f = new DataField();
        f.setName("Editor (Community)");
        _dataFieldStore.add(new DataFieldModel(f));
        f = new DataField();
        f.setName("Activity");
        _dataFieldStore.add(new DataFieldModel(f));
        f = new DataField();
        f.setName("Created/Updated");
        _dataFieldStore.add(new DataFieldModel(f));

        if (type != null) {
            List<DataField> fields = type.getFields();
            for (DataField field : fields)
            {
                if (!field.isPrivate())
                {
                    _dataFieldStore.add(new DataFieldModel(field));
                }
            }
        }

        _dataFieldComboBox.setValue(_dataFieldStore.getAt(0));

        updateState();
        validate();
    }
}
