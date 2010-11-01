package com.paxxis.chime.client.editor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableRowLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
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

public class MultiDateEditorWindow extends ChimeWindow {
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
    private DatePanel.DatePanelListener dataPanelListener;

    private DataInstance dataInstance;
    private Shape dataType;
    private DataField dataField;
    private List<DataFieldValue> fieldValues = new ArrayList<DataFieldValue>();

    private FieldEditListener editListener;

    public MultiDateEditorWindow(DataInstance instance, Shape type,
                                DataField field, FieldEditListener listener) {
        super();

        editListener = listener;
        dataInstance = instance;
        dataType = type;
        dataField = field;
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

        dataPanelListener = new DatePanel.DatePanelListener() {

            public void onDelete(DatePanel panel) {
                deleteOne(panel);
            }

            public void onDataUpdate(DatePanel panel) {
                updateValue(panel);
            }

            public void onMoveUp(DatePanel panel) {
                move(panel, true);
            }

            public void onMoveDown(DatePanel panel) {
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
        setup();
    }

    private void deleteOne(DatePanel panel) {

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
            ((DatePanel)p).updateLayout();
        }

        validate();
    }

    private void move(DatePanel panel, boolean up) {
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

            DatePanel otherPanel = (DatePanel)listPanel.getItem(otherIdx);

            Date dt = new Date();
            if (val != null) {
                dt = (Date)val.getValue();
            }
            otherPanel.setValue(dt);

            dt = new Date();
            if (otherVal != null) {
                dt = (Date)otherVal.getValue();
            }
            panel.setValue(dt);

            listPanel.scrollIntoView(otherPanel);

            validate();

        }
    }

    private void updateValue(DatePanel panel) {

        int idx = -1;
        for (int i = 0; i < listPanel.getItems().size(); i++) {
            if (panel == listPanel.getItem(i)) {
                idx = i;
                break;
            }
        }

        Date value = panel.getValue();
        DataFieldValue val = new DataFieldValue();
        val.setValue(value);

        fieldValues.set(idx, val);

        validate();
    }

    private void addValue(DataFieldValue value) {
        DatePanel panel = new DatePanel(dataPanelListener, dataField, value);
        listPanel.add(panel, new FlowData(0, 0, 5, 0));
        listPanel.layout();

        for (Component p : listPanel.getItems()) {
            ((DatePanel)p).updateLayout();
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
        for (DataFieldValue val : values) {
            val.setId(InstanceId.create("-1"));
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
                    if (f1.getValue().equals(f2.getValue())) {
                        hasDuplicates = true;
                        duplicateName = f1.getValue().toString();
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
        }
        else if (hasDuplicates) {
            errorText = "Duplicate item: " + duplicateName;
        }

        errorLabel.setHtml("<div id='endslice-error-label'>" + errorText + "</div>");
        _okButton.setEnabled(!hasDuplicates && !hasNulls && changes);
        addButton.setEnabled(!maxedOut);
    }
}

/**
 *
 * @author Robert Englander
 */
class DatePanel extends LayoutContainer {

    interface DatePanelListener {
        public void onDelete(DatePanel panel);
        public void onDataUpdate(DatePanel panel);
        public void onMoveUp(DatePanel panel);
        public void onMoveDown(DatePanel panel);
    }

    private DatePanelListener listener;
    private ToolButton upButton;
    private ToolButton downButton;
    private ToolButton deleteButton;
    private DateField dateField;
    private TableRowLayout theLayout;

    public DatePanel(DatePanelListener l, DataField field, DataFieldValue value) {
        listener = l;
        init(field, value);
    }

    public void setFocus() {
        //dateField.setFocus();
    }

    public void init(DataField field, final DataFieldValue value) {
        theLayout = new TableRowLayout();
        setLayout(theLayout);

        upButton = new ToolButton("x-tool-up");
        upButton.addSelectionListener(
            new SelectionListener<IconButtonEvent>() {
                @Override
                public void componentSelected(IconButtonEvent evt) {
                    listener.onMoveUp(DatePanel.this);
                }
            }
        );

        TableData data = new TableData("auto", "auto");
        add(upButton, data);

        downButton = new ToolButton("x-tool-down");
        downButton.addSelectionListener(
            new SelectionListener<IconButtonEvent>() {
                @Override
                public void componentSelected(IconButtonEvent evt) {
                    listener.onMoveDown(DatePanel.this);
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
                    listener.onDelete(DatePanel.this);
                }
            }
        );

        data = new TableData("auto", "auto");
        add(deleteButton, data);

        dateField = new DateField();
        Listener<FieldEvent> l = new Listener<FieldEvent>() {
	        public void handleEvent(FieldEvent evt) {
	            listener.onDataUpdate(DatePanel.this);
	        }
	    };

        dateField.addListener(Events.KeyPress, l);
        dateField.addListener(Events.Valid, l);
        dateField.addListener(Events.Invalid, l);
        dateField.addListener(Events.Change, l);
        
        if (value != null) {
            setValue((Date)value.getValue());
        }

        data = new TableData("auto", "auto");
        add(dateField, data);
    }

    public void setValue(final Date value) {
        DeferredCommand.addCommand(
            new Command() {
                public void execute() {
                    dateField.setValue(value);
                }
            }
        );
    }

    public void updateLayout() {
        int w = getWidth() - 3 * deleteButton.getWidth() - 18;
        dateField.setWidth(w);
        layout();
    }

    public Date getValue() {
        return dateField.getValue();
    }
}

