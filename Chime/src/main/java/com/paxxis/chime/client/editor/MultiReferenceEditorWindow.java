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
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableRowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.paxxis.chime.client.DataInputListener;
import com.paxxis.chime.client.DataInstanceComboBox;
import com.paxxis.chime.client.LoginResponseObject;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceManagerAdapter;
import com.paxxis.chime.client.ServiceManagerListener;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.widgets.ChimeWindow;

/**
 *
 * @author Robert Englander
 */
public class MultiReferenceEditorWindow extends ChimeWindow {

    enum EditorType {
        FieldData,
        Files,
        Images,
        AppliedTypes
    }

    private Html errorLabel;

    private ButtonBar saveCancelButtonBar;
    private Button _okButton;
    private Button _cancelButton;

    private ButtonBar addRestoreButtonBar;
    private Button addButton;
    private Button restoreButton;
    private Button createButton = null;

    private LayoutContainer listPanel;
    private FormPanel _form = new FormPanel();
    private ServiceManagerListener _serviceManagerListener = null;
    private TextDataPanel.DataPanelListener dataPanelListener;

    private DataInstance dataInstance;
    private Shape dataType;
    private DataField dataField;
    private List<DataFieldValue> fieldValues = new ArrayList<DataFieldValue>();
    private List<DataInstance> fileValues = new ArrayList<DataInstance>();

    private FieldEditListener editListener = null;
    private AppliedTypesEditListener typesListener = null;
    private EditorType editorType = EditorType.FieldData;

    public MultiReferenceEditorWindow(DataInstance instance, AppliedTypesEditListener listener) {
        super();

        editorType = EditorType.AppliedTypes;
        typesListener = listener;
        dataInstance = instance;
        dataType = null;
        dataField = null;
    }

    public MultiReferenceEditorWindow(DataInstance instance, boolean useImages, FieldEditListener listener) {
        super();

        if (useImages) {
            editorType = EditorType.Images;
        } else {
            editorType = EditorType.Files;
        }

        editListener = listener;
        dataInstance = instance;
        dataType = null;
        dataField = null;
    }

    public MultiReferenceEditorWindow(DataInstance instance, Shape type,
                                DataField field, FieldEditListener listener) {
        super();

        editListener = listener;
        dataInstance = instance;
        dataType = type;
        dataField = field;
    }

    protected void onRender(Element parent, int index) { 
    	super.onRender(parent, index);
        setup();
    }
    
    private void setup() {

        listPanel.removeAll();
        fieldValues.clear();
        fileValues.clear();

        DeferredCommand.addCommand(
            new Command() {
                public void execute() {
                    boolean newOne = true;

                    if (editorType == EditorType.FieldData) {
                        for (DataFieldValue val : dataInstance.getFieldValues(dataType, dataField)) {
                            addValue(val);
                            newOne = false;
                        }
                    } else if (editorType == EditorType.AppliedTypes) {
                        for (DataInstance type : dataInstance.getShapes()) {
                            addValue(type);
                            newOne = false;
                        }
                    } else {
                        List<DataInstance> vals;
                        if (editorType == EditorType.Files) {
                            vals = dataInstance.getFiles();
                        } else {
                            vals = dataInstance.getImages();
                        }

                        for (DataInstance inst : vals) {
                            addValue(inst);
                            newOne = false;
                        }
                    }

                    if (newOne) {
                        if (editorType == EditorType.FieldData) {
                            addValue((DataFieldValue)null);
                        } else {
                            addValue((DataInstance)null);
                        }
                    }

                    validate();
                }
            }
        );
    }

    protected void init() {
        setModal(true);

        switch (editorType) {
            case FieldData:
                setHeading("Edit " + dataField.getName() + " [" + dataType.getName() + "]");
                break;
            case Images:
                setHeading("Edit Images");
                break;
            case Files:
                setHeading("Edit Files");
                break;
            case AppliedTypes:
                setHeading("Edit Applied Shapes");
                break;
        }

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

        dataPanelListener = new TextDataPanel.DataPanelListener() {

            public void onDelete(TextDataPanel panel) {
                deleteOne(panel);
            }

            public void onDataUpdate(TextDataPanel panel) {
                updateValue(panel);
            }

            public void onMoveUp(TextDataPanel panel) {
                move(panel, true);
            }

            public void onMoveDown(TextDataPanel panel) {
                move(panel, false);
            }
        };

        _form.add(listPanel);
        
        addRestoreButtonBar = new ButtonBar();
        addRestoreButtonBar.setAlignment(HorizontalAlignment.LEFT);

        addButton = new Button("Add");
        addButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                    if (editorType == EditorType.FieldData) {
                        addValue((DataFieldValue)null);
                    } else {
                        addValue((DataInstance)null);
                    }
                }
            }
        );

        addRestoreButtonBar.add(addButton);

        if (editorType != EditorType.AppliedTypes) {
        	boolean show = true;
        	if (dataField != null && !dataField.getShape().isDirectCreatable()) {
        		show = false;
        	}
        	
        	if (show) {
                createButton = new Button("Create...");
                createButton.addSelectionListener(
                    new SelectionListener<ButtonEvent>() {
                        @Override
                        public void componentSelected(ButtonEvent evt) {
                            createNewInstance();
                        }
                    }
                );

                addRestoreButtonBar.add(createButton);
        	}
        }

        restoreButton = new Button("Restore");
        restoreButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                    setup();
                }
            }
        );

        addRestoreButtonBar.add(new FillToolItem());
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
        add(_form);

        _serviceManagerListener = new ServiceManagerAdapter() {
            public void onLoginResponse(LoginResponseObject resp) {
            }
        };

        ServiceManager.addListener(_serviceManagerListener);
    }

    private void createNewInstance() {
        if (editorType == EditorType.FieldData) {
            InstanceCreatorWindow w = new InstanceCreatorWindow(dataField.getShape(),
                new InstanceCreatorWindow.InstanceCreationListener() {
                    public void onInstanceCreated(DataInstance instance) {
                        DataFieldValue val = new DataFieldValue();
                        val.setName(instance.getName());
                        val.setShapeId(dataField.getShape().getId());
                        val.setReferenceId(instance.getId());
                        addValue(val);
                    }
                }
            );

            w.show();
        } else {
            FileCreatorWindow.FileType type;
            if (editorType == EditorType.Files) {
                type = FileCreatorWindow.FileType.File;
            } else {
                type = FileCreatorWindow.FileType.Image;
            }

            FileCreatorWindow w = new FileCreatorWindow(type, dataInstance,
                new FileCreatorWindow.FileCreationListener() {
                    public void onFileCreated(DataInstance fileInstance) {
                        addValue(fileInstance);
                    }
            });

            w.show();
        }
    }

    private void deleteOne(TextDataPanel panel) {

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

        if (editorType == EditorType.FieldData) {
            fieldValues.remove(idx);
        } else {
            fileValues.remove(idx);
        }

        //listPanel.layout();
        for (Component p : listPanel.getItems()) {
            ((TextDataPanel)p).updateLayout();
        }

        validate();
    }

    private void move(TextDataPanel panel, boolean up) {
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
            String name = "";
            if (editorType == EditorType.FieldData) {
                DataFieldValue val = fieldValues.get(idx);
                DataFieldValue otherVal = fieldValues.get(otherIdx);

                fieldValues.set(idx, otherVal);
                fieldValues.set(otherIdx, val);
                TextDataPanel otherPanel = (TextDataPanel)listPanel.getItem(otherIdx);

                if (val != null) {
                    name = val.getName();
                }
                otherPanel.setValue(name);

                name = "";
                if (otherVal != null) {
                    name = otherVal.getName();
                }
                panel.setValue(name);

                listPanel.scrollIntoView(otherPanel);
            } else {
                DataInstance val = fileValues.get(idx);
                DataInstance otherVal = fileValues.get(otherIdx);

                fileValues.set(idx, otherVal);
                fileValues.set(otherIdx, val);
                TextDataPanel otherPanel = (TextDataPanel)listPanel.getItem(otherIdx);

                if (val != null) {
                    name = val.getName();
                }
                otherPanel.setValue(name);

                name = "";
                if (otherVal != null) {
                    name = otherVal.getName();
                }
                panel.setValue(name);

                listPanel.scrollIntoView(otherPanel);
            }

            validate();

        }
    }

    private void updateValue(TextDataPanel panel) {

        int idx = -1;
        for (int i = 0; i < listPanel.getItems().size(); i++) {
            if (panel == listPanel.getItem(i)) {
                idx = i;
                break;
            }
        }

        DataInstance inst = panel.getDataInstance();
        if (editorType != EditorType.FieldData) {
            fileValues.set(idx, inst);
        } else {
            DataFieldValue val = new DataFieldValue();
            val.setName(inst.getName());
            val.setReferenceId(inst.getId());
            fieldValues.set(idx, val);
        }

        validate();
    }

    private void addValue(DataInstance value) {
        TextDataPanel panel;
        if (editorType == EditorType.AppliedTypes || editorType == EditorType.FieldData) {
            panel = new TextDataPanel(dataPanelListener, value);
        } else {
            panel = new TextDataPanel(dataPanelListener, value, editorType == EditorType.Files);
        }


        listPanel.add(panel, new FlowData(0, 0, 5, 0));
        listPanel.layout();

        for (Component p : listPanel.getItems()) {
            ((TextDataPanel)p).updateLayout();
        }

        listPanel.scrollIntoView(panel);

        fileValues.add(value);

        if (value == null) {
            panel.setFocus();
        }

        validate();
    }

    private void addValue(DataFieldValue value) {
        TextDataPanel panel = new TextDataPanel(dataPanelListener, dataField, value);
        listPanel.add(panel, new FlowData(0, 0, 5, 0));
        listPanel.layout();

        for (Component p : listPanel.getItems()) {
            ((TextDataPanel)p).updateLayout();
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
        if (editorType == EditorType.FieldData) {
            List<DataFieldValue> values = dataInstance.getFieldValues(dataType, dataField);
            values.clear();
            values.addAll(fieldValues);

            // we clear the id of each value so that the service will treat it all as new.
            for (DataFieldValue val : values) {
                val.setId(InstanceId.create("-1"));
            }
        } else if (editorType == EditorType.Files) {
            dataInstance.setFiles(fileValues);
        } else if (editorType == EditorType.Images) {
            dataInstance.setImages(fileValues);
        } else {
            List<Shape> types = new ArrayList<Shape>();
            for (DataInstance inst : fileValues) {
                types.add((Shape)inst);
            }
            dataInstance.setShapes(types);
        }

        if (typesListener != null) {
            typesListener.onEdit(dataInstance);
        } else {
            editListener.onEdit(dataInstance, dataType, dataField);
        }

        hide();
    }

    /**
     * Check that the new list of items meets the following criteria:
     *
     * 1) No duplicates
     * 2) No empty references
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
        if (editorType == EditorType.FieldData) {
            validateFieldData();
        } else if (editorType == EditorType.AppliedTypes) {
            validateAppliedTypes();
        } else {
            validateFileData();
        }
    }

    private void validateAppliedTypes() {
        boolean hasNulls = false;
        for (DataInstance fv : fileValues) {
            if (fv == null) {
                hasNulls = true;
                break;
            }
        }

        boolean hasDuplicates = false;
        String duplicateName = null;

        if (!hasNulls) {
            int cnt = fileValues.size();
            for (int i = 0; i < (cnt - 1); i++) {
                for (int j = (i + 1); j < cnt; j++) {
                    DataInstance f1 = fileValues.get(i);
                    DataInstance f2 = fileValues.get(j);
                    if (f1.getId().equals(f2.getId())) {
                        hasDuplicates = true;
                        duplicateName = f1.getName();
                        break;
                    }
                }
            }
        }

        boolean changes = false;
        List<? extends DataInstance> theList;
        if (editorType == EditorType.AppliedTypes) {
            theList = dataInstance.getShapes();
        } else if (editorType == EditorType.Files) {
            theList = dataInstance.getFiles();
        } else {
            theList = dataInstance.getImages();
        }

        if (!hasNulls && !hasDuplicates) {
            if (fileValues.size() != theList.size()) {
                changes = true;
            } else {
                for (int i = 0; i < theList.size(); i++) {
                    DataInstance f1 = theList.get(i);
                    DataInstance f2 = fileValues.get(i);
                    if (f1.getId() != f2.getId()) {
                        changes = true;
                        break;
                    }
                }
            }
        }

        String invalidName = "";
        boolean hasNonMulti = false;
        for (DataInstance inst : fileValues) {
            Shape t = (Shape)inst;
            if (!t.canMultiType()) {
                invalidName = t.getName();
                hasNonMulti = true;
                break;
            }
        }

        String errorText = "&nbsp;";
        if (fileValues.size() == 0) {
            errorText = "At least 1 shape must be applied";
        } else if (hasNonMulti) {
            errorText = invalidName + " can't be applied to an instance";
        } else if (hasNulls) {
            errorText = "Please fill in empty shapes";
        }
        else if (hasDuplicates) {
            errorText = "Duplicate shape: " + duplicateName;
        }

        errorLabel.setHtml("<div id='endslice-error-label'>" + errorText + "</div>");
        _okButton.setEnabled(!hasDuplicates && !hasNulls && changes && !hasNonMulti && fileValues.size() > 0);
    }

    private void validateFileData() {
        boolean hasNulls = false;
        for (DataInstance fv : fileValues) {
            if (fv == null) {
                hasNulls = true;
                break;
            }
        }

        boolean hasDuplicates = false;
        String duplicateName = null;

        if (!hasNulls) {
            int cnt = fileValues.size();
            for (int i = 0; i < (cnt - 1); i++) {
                for (int j = (i + 1); j < cnt; j++) {
                    DataInstance f1 = fileValues.get(i);
                    DataInstance f2 = fileValues.get(j);
                    if (f1.getId().equals(f2.getId())) {
                        hasDuplicates = true;
                        duplicateName = f1.getName();
                        break;
                    }
                }
            }
        }

        boolean changes = false;
        List<DataInstance> theList;
        if (editorType == EditorType.Files) {
            theList = dataInstance.getFiles();
        } else {
            theList = dataInstance.getImages();
        }

        if (!hasNulls && !hasDuplicates) {
            if (fileValues.size() != theList.size()) {
                changes = true;
            } else {
                for (int i = 0; i < theList.size(); i++) {
                    DataInstance f1 = theList.get(i);
                    DataInstance f2 = fileValues.get(i);
                    if (f1.getId() != f2.getId()) {
                        changes = true;
                        break;
                    }
                }
            }
        }

        String errorText = "&nbsp;";
        if (hasNulls) {
            errorText = "Please fill in empty items";
        }
        else if (hasDuplicates) {
            errorText = "Duplicate item: " + duplicateName;
        }

        errorLabel.setHtml("<div id='endslice-error-label'>" + errorText + "</div>");
        _okButton.setEnabled(!hasDuplicates && !hasNulls && changes);
    }

    private void validateFieldData() {
        boolean hasNulls = false;
        for (DataFieldValue fv : fieldValues) {
            if (fv == null) {
                hasNulls = true;
                break;
            }
        }

        boolean hasDuplicates = false;
        String duplicateName = null;

        if (!hasNulls) {
            int cnt = fieldValues.size();
            for (int i = 0; i < (cnt - 1); i++) {
                for (int j = (i + 1); j < cnt; j++) {
                    DataFieldValue f1 = fieldValues.get(i);
                    DataFieldValue f2 = fieldValues.get(j);
                    if (f1.getReferenceId() == f2.getReferenceId()) {
                        hasDuplicates = true;
                        duplicateName = f1.getName();
                        break;
                    }
                }
            }
        }

        boolean changes = false;
        if (!hasNulls && !hasDuplicates) {
            if (fieldValues.size() != dataInstance.getFieldValues(dataType, dataField).size()) {
                changes = true;
            } else {
                List<DataFieldValue> list = dataInstance.getFieldValues(dataType, dataField);
                for (int i = 0; i < list.size(); i++) {
                    DataFieldValue f1 = list.get(i);
                    DataFieldValue f2 = fieldValues.get(i);
                    if (f1.getReferenceId() != f2.getReferenceId()) {
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
        }
        else if (hasDuplicates) {
            errorText = "Duplicate item: " + duplicateName;
        }

        errorLabel.setHtml("<div id='endslice-error-label'>" + errorText + "</div>");
        _okButton.setEnabled(!hasDuplicates && !hasNulls && changes);
        addButton.setEnabled(!maxedOut);
        if (createButton != null) {
            createButton.setEnabled(!maxedOut);
        }
    }
}

/**
 * A panel that contains a button to delete, a handle to drag into another
 * position, and a DataInstanceComboBox, all laid out in a single row.
 *
 * @author Robert Englander
 */
class TextDataPanel extends LayoutContainer {

    interface DataPanelListener {
        public void onDelete(TextDataPanel panel);
        public void onDataUpdate(TextDataPanel panel);
        public void onMoveUp(TextDataPanel panel);
        public void onMoveDown(TextDataPanel panel);
    }

    private DataPanelListener listener;
    private ToolButton upButton;
    private ToolButton downButton;
    private ToolButton deleteButton;
    private DataInstanceComboBox instanceCombo;
    private DataInstance dataInstance = null;
    private TableRowLayout theLayout;
    private boolean useFiles;
    private MultiReferenceEditorWindow.EditorType editorType;

    public TextDataPanel(DataPanelListener l, DataField field, DataFieldValue value) {
        listener = l;
        String name = null;
        if (value != null) {
            name = value.getName();
        }

        editorType = MultiReferenceEditorWindow.EditorType.FieldData;
        init(field, name);
    }

    public TextDataPanel(DataPanelListener l, DataInstance value, boolean useFiles) {
        listener = l;
        this.useFiles = useFiles;
        String name = null;
        if (value != null) {
            name = value.getName();
        }

        if (useFiles) {
            editorType = MultiReferenceEditorWindow.EditorType.Files;
        } else {
            editorType = MultiReferenceEditorWindow.EditorType.Images;
        }

        init(null, name);
    }

    public TextDataPanel(DataPanelListener l, DataInstance value) {
        listener = l;
        String name = null;
        if (value != null) {
            name = value.getName();
        }
        editorType = MultiReferenceEditorWindow.EditorType.AppliedTypes;

        init(null, name);
    }

    public void setFocus() {
        instanceCombo.focus();
    }

    public void init(DataField field, final String value) {
        //setLayout(new FilledColumnLayout(HorizontalAlignment.LEFT));
        theLayout = new TableRowLayout();
        setLayout(theLayout);

        upButton = new ToolButton("x-tool-up");
        upButton.addSelectionListener(
            new SelectionListener<IconButtonEvent>() {
                @Override
                public void componentSelected(IconButtonEvent evt) {
                    listener.onMoveUp(TextDataPanel.this);
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
                    listener.onMoveDown(TextDataPanel.this);
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
                    listener.onDelete(TextDataPanel.this);
                }
            }
        );

        data = new TableData("auto", "auto");
        add(deleteButton, data);

        DataInputListener instListener = new DataInputListener() {

            public void onDataInstance(DataInstance instance) {
                dataInstance = instance;
                listener.onDataUpdate(TextDataPanel.this);
            }

            public void onStringData(String text) {
            }

        };

        if (editorType == MultiReferenceEditorWindow.EditorType.AppliedTypes) {
            instanceCombo = new DataInstanceComboBox(instListener, Shape.SHAPE_ID, true, false, true);
            instanceCombo.setReturnFullInstances(true);
        } else if (field != null) {
            instanceCombo = new DataInstanceComboBox(instListener, field.getShape(), true, false, true);
        } else if (useFiles) {
            instanceCombo = new DataInstanceComboBox(instListener, Shape.FILE_ID, true, false, true);
        } else {
            instanceCombo = new DataInstanceComboBox(instListener, Shape.IMAGE_ID, true, false, true);
        }

        if (value != null) {
            setValue(value);
        }
        
        data = new TableData("auto", "auto");
        add(instanceCombo, data);
    }

    public void setValue(final String value) {
        DeferredCommand.addCommand(
            new Command() {
                public void execute() {
                    instanceCombo.setRawValue(value);
                }
            }
        );
    }

    public void updateLayout() {
        int w = getWidth() - 3 * deleteButton.getWidth() - 18;
        instanceCombo.setWidth(w);
        layout();
    }

    public DataInstance getDataInstance() {
        return dataInstance;
    }
}

