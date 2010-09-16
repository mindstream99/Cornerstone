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

package com.paxxis.chime.client.widgets;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.paxxis.chime.client.ChimeListStore;
import com.paxxis.chime.client.DataInputListener;
import com.paxxis.chime.client.DataInstanceComboBox;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.SearchFilter;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.DataInstanceRequest.Operator;

/**
 *
 * @author Robert Englander
 */
public class SocialFilterPopup extends ChimeWindow {

    public interface SocialFilterListener {
        public void onApply(SearchFilter filter);
    }

    private Button okButton;
    private Button cancelButton;
    private FormPanel form;

    private LayoutContainer filterContainer = new LayoutContainer();
    private SearchFilter activeFilter = new SearchFilter();

    private ComboBox<SocialFilterOperatorModel> operatorComboBox;
    private ListStore<SocialFilterOperatorModel> operatorStore;
    private List<SocialFilterOperatorModel> operatorList;

    private DataInstanceComboBox filterInput;

    private SocialFilterListener listener;

    public SocialFilterPopup(SocialFilterListener l) {
        super();
        listener = l;
        baseStyle = "x-popup";

        setModal(true);
    	setHeaderVisible(false);
        setShadow(false);
        setBorders(false);
        setResizable(false);
        setClosable(false);
        setConstrain(true);
        setBodyBorder(false);
        setStyleAttribute("background", "#fbf0d2");
        setStyleAttribute("border", "2px solid #003399");
        setWidth(290);
    }

    protected void init() {
    	form = new FormPanel();
    	form.setHeaderVisible(false);
        form.setBorders(false);
        form.setBodyBorder(false);
        form.setStyleAttribute("padding", "0");
        form.setButtonAlign(HorizontalAlignment.CENTER);
        form.setFrame(true);
        form.setFieldWidth(250);
        form.setHideLabels(true);
       

        operatorList = new ArrayList<SocialFilterOperatorModel>();
        operatorStore = new ChimeListStore<SocialFilterOperatorModel>();
        
        operatorList.add(new SocialFilterOperatorModel(Operator.Reference));
        operatorList.add(new SocialFilterOperatorModel(Operator.NotReference));
        operatorList.add(new SocialFilterOperatorModel(Operator.ContainedIn));
        operatorList.add(new SocialFilterOperatorModel(Operator.NotContainedIn));
        
        operatorComboBox = new ComboBox<SocialFilterOperatorModel>();
        operatorComboBox.setEditable(false);
        operatorComboBox.setDisplayField("description");
        operatorComboBox.setStore(operatorStore);
        operatorStore.add(operatorList);
        operatorStore.commitChanges();
        form.add(operatorComboBox);

        operatorComboBox.addSelectionChangedListener(
            new SelectionChangedListener<SocialFilterOperatorModel>()
            {
                @Override
                public void selectionChanged(final SelectionChangedEvent evt)
                {
                    SocialFilterOperatorModel model = (SocialFilterOperatorModel)evt.getSelectedItem();

                    if (model != null)
                    {
                    	/*
                        Operator operator = model.getOperator();
                        if (operator == Operator.Reference || operator == Operator.NotReference) {
                            if (!filterInput.getDataShapeId().equals(Shape.USER_ID)) {
                                filterInput.setShape(Shape.USER_ID, true);
                                filterInput.setUserCreatedOnly(false);
                            }
                        } else {
                            if (!filterInput.getDataShapeId().equals(Shape.USERCOLLECTION_ID)) {
                                filterInput.setShape(Shape.USERCOLLECTION_ID, true);
                                filterInput.setUserCreatedOnly(true);
                            }
                        }
						
                        activeFilter.setOperator(operator);

                        filterInput.setEnabled(true);
                        */
                    }
                }
            }
        );

        DataInputListener l2 = new DataInputListener() {
            public void onDataInstance(DataInstance instance) {
                activeFilter.setValue(instance.getId().getValue(), instance.getName());

                DataField field = new DataField();
                field.setName("Writer");
                field.setShape(instance.getShapes().get(0));
                
                activeFilter.setDataField(field);
            }

            public void onStringData(String text) {
            }
        };

        filterInput = new DataInstanceComboBox(l2);
        filterInput.setEnabled(false);
        form.add(filterInput);
        
        cancelButton = new Button("Cancel");
        cancelButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                    hide();
                }
            }
        );

        form.addButton(cancelButton);

        okButton = new Button("Apply");
        okButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                    listener.onApply(activeFilter);
                    hide();
                }
            }
        );

        form.addButton(okButton);

        add(form);

        DeferredCommand.addCommand(
            new Command() {
                public void execute() {
                    operatorComboBox.setValue(operatorStore.getAt(0));
                }
            }
        );
    }
}
