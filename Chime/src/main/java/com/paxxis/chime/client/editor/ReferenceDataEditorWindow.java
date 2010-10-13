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

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.widgets.ChimeWindow;

/**
 *
 * @author Robert Englander
 */
public class ReferenceDataEditorWindow extends ChimeWindow 
{
    private FieldEditorListener _listener;
    
    private ReferenceEditorPanel _simpleEditorPanel = null;
    private Button _okButton;
    private Button _cancelButton;
    private Html _errorLabel;
    private DataField _field;
    private DataFieldValue _value;
    
    public ReferenceDataEditorWindow(DataField field, DataFieldValue value, FieldEditorListener listener)
    {
        _field = field;
        _value = value;
        _listener = listener;
    }
    
    protected void init()
    {
        setModal(true);
        setLayout(new FitLayout());
        
        setMaximizable(false);
        setMinimizable(false);
        setClosable(false);
        setResizable(false);
        setWidth(300);
        
        LayoutContainer cont = new LayoutContainer();
        cont.setLayout(new RowLayout(Orientation.VERTICAL));

        String suffix = "";
        
        if (_value == null) {
            setHeading("Create New '" + _field.getName() + "' Data" + suffix);
            _simpleEditorPanel = new ReferenceEditorPanel(_field.getShape());
        } else {
            setHeading("Edit '" + _field.getName() + "' Data" + suffix);
            _simpleEditorPanel = new ReferenceEditorPanel(_field.getShape(), _value.getValue().toString());
        }
        
        _simpleEditorPanel.setChangeListener(
            new ReferenceEditorPanel.EditorPanelChangeListener() 
            {
                public void onChange() {
                    validate();
                }

                public void onComplete() {
                    if (validate()) {
                        notifyComplete();
                        hide();
                    }
                }
            }
        );

        cont.add(_simpleEditorPanel, new RowData(1, -1, new Margins(7, 5, 3, 5)));
        
        ButtonBar bar = new ButtonBar();
        bar.setAlignment(HorizontalAlignment.CENTER);
        
        _okButton = new Button("Ok");
        _okButton.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent evt) 
                {
                    notifyComplete();
                    hide();
                }
            }
        );

        _errorLabel = new Html("<div id='endslice-error-label'>&nbsp;</div>");
        cont.add(_errorLabel, new RowData(1, -1, new Margins(5, 5, 3, 5)));
        
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

        bar.add(_okButton);
        bar.add(_cancelButton);
        
        cont.add(bar, new RowData(1, -1, new Margins(2, 5, 5, 2)));
        
        _simpleEditorPanel.initialize();
        
        add(cont);
        
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
    
    private void notifyComplete()
    {
        DataInstance instance = _simpleEditorPanel.getResult();
        
        _listener.onSave(_field, _value, instance.getId().getValue());
    }
    
    private boolean validate()
    {
        String error = "&nbsp;";
        boolean valid = true;

        if (_simpleEditorPanel.getResult() == null)
        {
            error = "Data can't be empty";
            valid = false;
        }

        _okButton.setEnabled(valid);
        _errorLabel.setHtml("<div id='endslice-error-label'>" + error + "</div>");
        
        return valid;
    }
}
