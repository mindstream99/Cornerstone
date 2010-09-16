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
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.paxxis.chime.client.widgets.ChimeWindow;

/**
 *
 * @author Robert Englander
 */
public class SingleLineTextEditorWindow  extends ChimeWindow
{
    private FieldEditorListener _listener;

    private FormPanel _form;
    private TextField<String> nameField;
    //private SearchFilterEditor _simpleEditorPanel = null;
    private Button _okButton;
    private Button _cancelButton;
    private Html _errorLabel;
    private String initialValue;

    public SingleLineTextEditorWindow(String initial, FieldEditorListener listener)
    {
        initialValue = initial;
        _listener = listener;
    }

    protected void init()
    {
        _form = new FormPanel();

        _form.setHeaderVisible(false);
        _form.setBorders(false);
        _form.setBodyBorder(false);
        _form.setStyleAttribute("padding", "5");
        _form.setButtonAlign(HorizontalAlignment.CENTER);
        _form.setFrame(false);
        _form.setFieldWidth(225);
        _form.setLabelWidth(5);
        _form.setHideLabels(true);

        setModal(true);
        setHeading("Edit Name");
        setMaximizable(false);
        setMinimizable(false);
        setClosable(false);
        setResizable(false);
        setShadow(false);
        setWidth(270);

        //LayoutContainer cont = new LayoutContainer();
        //cont.setLayout(new RowLayout(Orientation.VERTICAL));

        nameField = new TextField<String>();
        nameField.setFieldLabel("Name");
        nameField.setValue(initialValue);

        new KeyNav(nameField)
        {
            public void onKeyPress(final ComponentEvent ce)
            {
                validate();
            }

            public void onEnter(ComponentEvent ce)
            {
                DeferredCommand.addCommand(
                    new Command() {
                        public void execute() {
                            if (_okButton.isEnabled()) {
                                notifyComplete();
                                hide();
                            }
                        }
                    }
                );
            }
        };

        _form.add(nameField);

        //cont.add(_form, new RowData(1, -1, new Margins(7, 5, 3, 5)));

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
        _form.add(_errorLabel); //, new RowData(1, -1, new Margins(5, 5, 3, 5)));

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

        _form.add(bar);

        add(_form);
        nameField.focus();
        
        validate();
    }

    private void notifyComplete()
    {
        String text = nameField.getRawValue().trim();
        _listener.onSave(null, null, text);
    }

    private void validate()
    {
        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    String error = "&nbsp;";
                    boolean valid = true;

                    String value = nameField.getRawValue();
                    if (value == null || value.trim().length() == 0)
                    {
                        error = "Name can't be empty.";
                        valid = false;
                    }

                    boolean changed = !initialValue.equals(value.trim());
                    _okButton.setEnabled(valid && changed);
                    _errorLabel.setHtml("<div id='endslice-error-label'>" + error + "</div>");
                }
            }
        );
    }
}
