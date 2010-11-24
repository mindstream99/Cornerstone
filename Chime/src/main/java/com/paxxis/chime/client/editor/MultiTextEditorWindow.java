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
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableRowLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.RichTextArea;
import com.paxxis.chime.client.LoginResponseObject;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceManagerAdapter;
import com.paxxis.chime.client.ServiceManagerListener;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.widgets.ChimeWindow;
import com.paxxis.chime.client.widgets.ExpandableTextField;
import com.paxxis.cornerstone.base.InstanceId;

/**
 *
 * @author Robert Englander
 */
public class MultiTextEditorWindow extends ChimeWindow {
    private Html errorLabel;

    private ButtonBar saveCancelButtonBar;
    private Button _okButton;
    private Button _cancelButton;

    private ButtonBar addRestoreButtonBar;
    private Button addButton;
    private Button restoreButton;

    private LayoutContainer listPanel;
    private FormPanel _form = new FormPanel();
    private ServiceManagerListener _serviceManagerListener = null;
    private DataPanel.DataPanelListener dataPanelListener;

    private DataInstance dataInstance;
    private Shape dataType;
    private DataField dataField;
    private List<DataFieldValue> fieldValues = new ArrayList<DataFieldValue>();

    private FieldEditListener editListener;

    public MultiTextEditorWindow(DataInstance instance, Shape type,
                                DataField field, FieldEditListener listener) {
        super();

        editListener = listener;
        dataInstance = instance;
        dataType = type;
        dataField = field;
    }

    @Override
    protected void onRender(Element parent, int index) {
    	super.onRender(parent, index);
        setup();
    }

    private void setup() {

        listPanel.removeAll();
        fieldValues.clear();

        DeferredCommand.addCommand(
            new Command() {
                public void execute() {
                    boolean newOne = true;
                    for (DataFieldValue val : dataInstance.getFieldValues(dataType, dataField)) {
                        addValue(val);
                        newOne = false;
                    }

                    if (newOne) {
                        addValue(null);
                    }

                    validate();
                }
            }
        );
    }

    protected void init() {
        setModal(true);
        setHeading("Edit " + dataField.getName() + " [" + dataType.getName() + "]");
        setMaximizable(false);
        setMinimizable(false);
        setCollapsible(false);
        setClosable(true);
        setResizable(false);
        setWidth(400);
        
        _form.setHeaderVisible(false);
        _form.setBorders(false);
        _form.setBodyBorder(false);
        _form.setStyleAttribute("padding", "5");
        _form.setButtonAlign(HorizontalAlignment.CENTER);
        _form.setFrame(true);
        _form.setFieldWidth(250);
        _form.setLabelWidth(85);
        _form.setHideLabels(true);

        listPanel = new LayoutContainer();
        listPanel.setLayout(new FlowLayout());
        listPanel.setHeight(150);
        listPanel.setScrollMode(Scroll.AUTO);
        listPanel.setBorders(true);

        dataPanelListener = new DataPanel.DataPanelListener() {

            public void onDelete(DataPanel panel) {
                deleteOne(panel);
            }

            public void onDataUpdate(DataPanel panel) {
                updateValue(panel);
            }

            public void onMoveUp(DataPanel panel) {
                move(panel, true);
            }

            public void onMoveDown(DataPanel panel) {
                move(panel, false);
            }
        };

        _form.add(listPanel, new RowData(1, -1));

        addRestoreButtonBar = new ButtonBar();
        addRestoreButtonBar.setAlignment(HorizontalAlignment.LEFT);

        addButton = new Button("Add");
        addButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                    addValue(null);
                }
            }
        );

        addRestoreButtonBar.add(addButton);

        restoreButton = new Button("Restore");
        restoreButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                    setup();
                }
            }
        );

        addRestoreButtonBar.add(restoreButton);

        _form.add(addRestoreButtonBar);

        LayoutContainer c = new LayoutContainer();
        c.setHeight(10);
        _form.add(c);

        errorLabel = new Html("<div id='endslice-error-label'>&nbsp;</div>");
        _form.add(errorLabel);

        c = new LayoutContainer();
        c.setHeight(10);
        _form.add(c);

        saveCancelButtonBar = new ButtonBar();
        saveCancelButtonBar.setAlignment(HorizontalAlignment.CENTER);

        _okButton = new Button("Save");
        _okButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                    notifyListener();
                }
            }
        );

        saveCancelButtonBar.add(_okButton);

        _cancelButton = new Button("Cancel");
        _cancelButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                    doCancel();
                }
            }
        );

        saveCancelButtonBar.add(_cancelButton);

        _form.add(saveCancelButtonBar);
        add(_form, new RowData(1, -1));

        _serviceManagerListener = new ServiceManagerAdapter() {
            public void onLoginResponse(LoginResponseObject resp) {
            }
        };

        ServiceManager.addListener(_serviceManagerListener);
    }

    private void deleteOne(DataPanel panel) {

        int idx = -1;
        Component comp = null;
        for (int i = 0; i < listPanel.getItems().size(); i++) {
            if (panel == listPanel.getItem(i)) {
                idx = i;
                comp = panel;
                break;
            }
        }

        listPanel.remove(comp);
        fieldValues.remove(idx);

        //listPanel.layout();
        for (Component p : listPanel.getItems()) {
            ((DataPanel)p).updateLayout();
        }

        validate();
    }

    private void move(DataPanel panel, boolean up) {
        int idx = -1;
        for (int i = 0; i < listPanel.getItems().size(); i++) {
            if (panel == listPanel.getItem(i)) {
                idx = i;
                break;
            }
        }

        int otherIdx = -1;
        if (up && idx > 0) {
            otherIdx = idx - 1;
        } else if (!up && idx < (listPanel.getItems().size() - 1)) {
            otherIdx = idx + 1;
        }

        if (otherIdx != -1) {
            DataFieldValue val = fieldValues.get(idx);
            DataFieldValue otherVal = fieldValues.get(otherIdx);

            fieldValues.set(idx, otherVal);
            fieldValues.set(otherIdx, val);

            DataPanel otherPanel = (DataPanel)listPanel.getItem(otherIdx);

            String name = "";
            if (val != null) {
                name = val.getValue().toString();
            }
            otherPanel.setValue(name);

            name = "";
            if (otherVal != null) {
                name = otherVal.getValue().toString();
            }
            panel.setValue(name);

            listPanel.scrollIntoView(otherPanel);

            validate();

        }
    }

    private void updateValue(DataPanel panel) {

        int idx = -1;
        for (int i = 0; i < listPanel.getItems().size(); i++) {
            if (panel == listPanel.getItem(i)) {
                idx = i;
                break;
            }
        }

        String value = panel.getValue();
        DataFieldValue val = new DataFieldValue();
        val.setValue(value);

        fieldValues.set(idx, val);

        validate();
    }

    private void addValue(DataFieldValue value) {
        DataPanel panel = new DataPanel(dataPanelListener, dataField, value);
        listPanel.add(panel, new FlowData(0, 0, 5, 0));
        listPanel.layout();

        for (Component p : listPanel.getItems()) {
            ((DataPanel)p).updateLayout();
        }

        listPanel.scrollIntoView(panel);

        fieldValues.add(value);

        if (value == null) {
            panel.setFocus();
        }

        validate();
    }

    protected void handleLoginResponse(final LoginResponseObject resp)
    {
        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                }
            }
        );
    }

    protected void doCancel()
    {
        ServiceManager.removeListener(_serviceManagerListener);
        hide();
    }

    private void notifyListener() {
        List<DataFieldValue> values = dataInstance.getFieldValues(dataType, dataField);
        values.clear();
        values.addAll(fieldValues);

        // we clear the id of each value so that the service will treat it all as new.
        RichTextArea rta = new RichTextArea();
        for (DataFieldValue val : values) {
            val.setId(InstanceId.create("-1"));

            // the only HTML we keep is <br>
            String txt = val.getValue().toString();
            String modified = txt.replaceAll("<br>", "CHIME:BR");
            rta.setHTML(modified);
            txt = rta.getText();
            modified = txt.replaceAll("CHIME:BR", "<br>");
            val.setValue(modified);
        }

        editListener.onEdit(dataInstance, dataType, dataField);
        hide();
    }

    /**
     * Check that the new list of items meets the following criteria:
     *
     * 1) No duplicates
     * 2) No empty references
     * 3) data is correct for the type
     *
     * If the criteria isn't met, an appropriate error message is provided.
     *
     * Compares the new list with the existing list to determine if any changes
     * were made.
     *
     * Finally, checks to see if any more items can be added.
     *
     * Based on all this, the Save, Restore, and Add buttons are enabled or
     * disabled.
     */
    private void validate() {
        boolean hasNulls = false;
        for (DataFieldValue fv : fieldValues) {
            if (fv == null || fv.getValue().toString().trim().length() == 0) {
                hasNulls = true;
                break;
            }
        }

        boolean overSized = false;
        if (!hasNulls) {
            // check if any are more than 500 characters
            for (DataFieldValue fv : fieldValues) {
                if (fv.getValue().toString().trim().length() > 500) {
                    overSized = true;
                    break;
                }
            }
        }

        boolean badData = false; 
        if (!hasNulls && !overSized) {
            for (DataFieldValue fv : fieldValues) {
                String data = fv.getValue().toString().trim();
                if (dataField.getShape().getName().equals("Number")) {
                    try {
                        Double.parseDouble(data);
                    } catch (NumberFormatException e) {
                        badData = true;
                        break;
                    }
                } else if (dataField.getShape().getName().equals("URL")) {
                }
            }
        }

        boolean changes = false;
        if (!hasNulls && !overSized && !badData) {
            if (fieldValues.size() != dataInstance.getFieldValues(dataType, dataField).size()) {
                changes = true;
            } else {
                List<DataFieldValue> list = dataInstance.getFieldValues(dataType, dataField);
                for (int i = 0; i < list.size(); i++) {
                    DataFieldValue f1 = list.get(i);
                    DataFieldValue f2 = fieldValues.get(i);
                    if (!f1.getValue().equals(f2.getValue())) {
                        changes = true;
                        break;
                    }
                }
            }
        }

        // have we hit the limit?
        int max = dataField.getMaxValues();
        boolean maxedOut = fieldValues.size() == max && max != 0;

        String errorText = "&nbsp;";
        if (hasNulls) {
            errorText = "Please fill in empty items";
        } else if (overSized) {
            errorText = "Can't use more than 500 characters";
        } else if (badData) {
            errorText = "Data must be a valid " + dataField.getShape().getName();
        }

        errorLabel.setHtml("<div id='endslice-error-label'>" + errorText + "</div>");
        _okButton.setEnabled(!hasNulls && changes);
        addButton.setEnabled(!maxedOut);
    }

}

/**
 *
 * @author Robert Englander
 */
class DataPanel extends LayoutContainer {

    interface DataPanelListener {
        public void onDelete(DataPanel panel);
        public void onDataUpdate(DataPanel panel);
        public void onMoveUp(DataPanel panel);
        public void onMoveDown(DataPanel panel);
    }

    private DataPanelListener listener;
    private ToolButton upButton;
    private ToolButton downButton;
    private ToolButton deleteButton;
    private ExpandableTextField textField;
    private TableRowLayout theLayout;

    public DataPanel(DataPanelListener l, DataField field, DataFieldValue value) {
        listener = l;
        init(field, value);
    }

    public void setFocus() {
        textField.setFocus();
    }

    public void init(DataField field, final DataFieldValue value) {
        //setLayout(new FilledColumnLayout(HorizontalAlignment.LEFT));
        theLayout = new TableRowLayout();
        setLayout(theLayout);

        upButton = new ToolButton("x-tool-up");
        upButton.addSelectionListener(
            new SelectionListener<IconButtonEvent>() {
                @Override
                public void componentSelected(IconButtonEvent evt) {
                    listener.onMoveUp(DataPanel.this);
                }
            }
        );

        TableData data = new TableData("auto", "auto");
        //add(deleteButton, new FilledColumnLayoutData());
        add(upButton, data);

        downButton = new ToolButton("x-tool-down");
        downButton.addSelectionListener(
            new SelectionListener<IconButtonEvent>() {
                @Override
                public void componentSelected(IconButtonEvent evt) {
                    listener.onMoveDown(DataPanel.this);
                }
            }
        );

        data = new TableData("auto", "auto");
        add(downButton, data);

        deleteButton = new ToolButton("x-tool-close");
        deleteButton.addSelectionListener(
            new SelectionListener<IconButtonEvent>() {
                @Override
                public void componentSelected(IconButtonEvent evt) {
                    listener.onDelete(DataPanel.this);
                }
            }
        );

        data = new TableData("auto", "auto");
        add(deleteButton, data);

        textField = new ExpandableTextField(
            new ExpandableTextField.ExpandableTextListener() {
                public void onChange(String val) {
                    listener.onDataUpdate(DataPanel.this);
                }
            }
        );

        if (value != null) {
            setValue(value.getValue().toString());
        }

        data = new TableData("auto", "auto");
        add(textField, data);
    }

    public void setValue(final String value) {
        DeferredCommand.addCommand(
            new Command() {
                public void execute() {
                    textField.setValue(value);
                }
            }
        );
    }

    public void updateLayout() {
        int w = getWidth() - 3 * deleteButton.getWidth() - 18;
        textField.setWidth(w);
        layout();
    }

    public String getValue() {
        return textField.getValue();
    }
}

