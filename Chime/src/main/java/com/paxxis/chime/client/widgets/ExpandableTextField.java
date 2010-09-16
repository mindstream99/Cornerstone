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

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.BoxComponentEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Popup;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.paxxis.chime.client.FilledColumnLayout;
import com.paxxis.chime.client.FilledColumnLayoutData;

/**
 *
 * @author Robert Englander
 */
public class ExpandableTextField extends LayoutContainer {

    public interface ExpandableTextListener {
        public void onChange(String val);
    }

    class ExpandedEditor extends Popup {

        private TextArea textArea;
        private ToolBar toolBar;
        private LabelToolItem labelItem;

        public ExpandedEditor(int width) {
            setSize(width, 200);
            setShadow(false);
            setBorders(true);
            setConstrainViewport(true);
            setStyleAttribute("background", "white");
            setStyleAttribute("border", "1px solid #003399");
            setLayout(new RowLayout());
            textArea = new TextArea();
            textArea.setMaxLength(500);
            add(textArea, new RowData(1, 1));
            textArea.setValue(getValue());
            textArea.focus();
            new KeyNav(this) {
                public void onKeyPress(final ComponentEvent ce) {
                    DeferredCommand.addCommand(
                        new Command() {
                            public void execute() {
                                setTextValue(textArea.getValue());
                                updateLabel();
                            }
                        }
                    );
                }

                public void onEnter(ComponentEvent ce) {
                    hide();
                }
            };

            toolBar = new ToolBar();
            labelItem = new LabelToolItem();
            labelItem.setStyleAttribute("color", "white");
            toolBar.add(labelItem);
            toolBar.add(new FillToolItem());

            Button close = new Button();
            close.addSelectionListener(
                new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        hide();
                    }
                }
            );
            close.setIconStyle("close-icon");
            toolBar.add(close);
            add(toolBar, new RowData(1, -1));
            updateLabel();
        }

        private void updateLabel() {
            int count = getValue().length();
            if (count <= 500) {
                labelItem.setLabel("Characters remaing: " + (500 - count));
            } else {
                labelItem.setLabel("Over the limit by: " + (count - 500));
            }
        }
    }

    private TextField<String> textField;
    private Button triggerButton;
    private ExpandableTextListener listener;

    public ExpandableTextField(ExpandableTextListener l) {
        super();
        listener = l;
        init();
    }

    public void setFocus() {
        textField.focus();
    }
    
    private void setTextValue(String val) {
        setValue(val);
        notifyListener();
    }

    public void setValue(String value) {
        if (value == null) {
            value = "";
        }
        textField.setValue(value);
    }
    
    public String getValue() {
        String value = "";
        String v = textField.getValue();
        if (v != null) {
            value = v;
        }

        return value.trim();
    }

    private void notifyListener() {
        listener.onChange(textField.getValue());
    }

    private void init() {
        addListener(Events.Resize,
            new Listener<BoxComponentEvent>() {
                public void handleEvent(BoxComponentEvent evt) {
                    layout(true);
                }
            }
        );
    	
        setLayout(new FilledColumnLayout(HorizontalAlignment.RIGHT));

        textField = new TextField<String>();
        add(textField, new FilledColumnLayoutData());
        textField.setMaxLength(500);

        textField.addKeyListener(
            new KeyListener() {

            }
        );

        new KeyNav(textField) {
            public void onKeyPress(final ComponentEvent ce) {
                DeferredCommand.addCommand(
                    new Command() {
                        public void execute() {
                            notifyListener();
                        }
                    }
                );
            }

            public void onEnter(ComponentEvent ce) {

            }

            public void onDown(ComponentEvent ce) {
                onTriggerClick();
            }
        };

        //ToolBar tb = new ToolBar();

        triggerButton = new Button();
        triggerButton.setIconStyle("expand-icon");
        //tb.add(triggerButton);
        add(triggerButton, new FilledColumnLayoutData());
        triggerButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent ce) {
                    onTriggerClick();
                }
            }
        );
    }

    protected void onTriggerClick() {
        int w = getWidth() + 5;
        ExpandedEditor popup = new ExpandedEditor(w);

        int x = getElement().getAbsoluteLeft();
        int y = getElement().getAbsoluteTop();
        popup.showAt(x, y);
    }
}
