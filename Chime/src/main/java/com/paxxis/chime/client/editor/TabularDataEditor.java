package com.paxxis.chime.client.editor;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.paxxis.chime.client.LoginResponseObject;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceManagerAdapter;
import com.paxxis.chime.client.ServiceManagerListener;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.widgets.ChimeGrid;
import com.paxxis.chime.client.widgets.ChimeWindow;
import com.paxxis.chime.client.widgets.DataFieldModel;
import com.paxxis.chime.client.widgets.InterceptedHtmlGridCellRenderer;

public class TabularDataEditor extends ChimeWindow {

    private Html errorLabel;

    private ButtonBar saveCancelButtonBar;
    private Button _okButton;
    private Button _cancelButton;

    private ButtonBar addRestoreButtonBar;
    private Button addButton;
    private Button restoreButton;
    private Button createButton = null;

    private ChimeGrid<DataFieldModel> fieldGrid;
    private ListStore<DataFieldModel> listStore = new ListStore<DataFieldModel>();
    private FormPanel _form = new FormPanel();
    private ServiceManagerListener _serviceManagerListener = null;

    private DataInstance dataInstance;
    private Shape dataType;
    private DataField dataField;
    //private List<DataFieldValue> fieldValues = new ArrayList<DataFieldValue>();

    private FieldEditListener editListener = null;
    private AppliedTypesEditListener typesListener = null;

    public TabularDataEditor(DataInstance instance, Shape type,
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

        listStore.removeAll();

        DeferredCommand.addCommand(
            new Command() {
                public void execute() {
                    boolean newOne = true;

                    for (DataFieldValue val : dataInstance.getFieldValues(dataType, dataField)) {
                        addValue(val);
                        newOne = false;
                    }

                    if (newOne) {
                        addValue((DataFieldValue)null);
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
        setWidth(600);

        _form.setHeaderVisible(false);
        _form.setBorders(false);
        _form.setBodyBorder(false);
        _form.setStyleAttribute("padding", "5");
        _form.setButtonAlign(HorizontalAlignment.CENTER);
        _form.setFrame(true);
        _form.setFieldWidth(450);
        _form.setLabelWidth(85);
        _form.setHideLabels(true);

        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        Shape shape = dataField.getShape();
        String lastColId = "";
        List<DataField> dataFields = shape.getFields();
        for (DataField field : dataFields) {
	        ColumnConfig column = new ColumnConfig();
	        column.setId(field.getName());
	        column.setFixed(false);
	        column.setHeader(field.getName());
	        column.setWidth(150);
	        column.setSortable(false);
	        column.setMenuDisabled(true);
	        column.setRenderer(new InterceptedHtmlGridCellRenderer());
	        configs.add(column);
	        lastColId = field.getName();
        }

        ColumnModel cm = new ColumnModel(configs);
        fieldGrid = new ChimeGrid<DataFieldModel>(listStore, cm);
        fieldGrid.getView().setAutoFill(false);
        fieldGrid.setSelectionModel(null);
        fieldGrid.getView().setForceFit(false);
        fieldGrid.setHideHeaders(false);
        fieldGrid.setTrackMouseOver(false);
        fieldGrid.setStripeRows(true);
        fieldGrid.setAutoExpandColumn(lastColId);
        fieldGrid.setBorders(true);
        fieldGrid.setHeight(300);

        _form.add(fieldGrid);
        
        addRestoreButtonBar = new ButtonBar();
        addRestoreButtonBar.setAlignment(HorizontalAlignment.LEFT);

        addButton = new Button("Add");
        addButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                    addValue((DataFieldValue)null);
                }
            }
        );

        addRestoreButtonBar.add(addButton);

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
        InstanceCreatorWindow w = new InstanceCreatorWindow(dataField.getShape(),
            new InstanceCreatorWindow.InstanceCreationListener() {
                public void onInstanceCreated(DataInstance instance) {
                    DataFieldValue val = new DataFieldValue();
                    val.setValue(instance.getName());
                    val.setShapeId(dataField.getShape().getId());
                    val.setReferenceId(instance.getId());
                    addValue(val);
                }
            }
        );

        w.show();
    }

    private void addValue(DataFieldValue value) {

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
        //values.addAll(fieldValues);

        // we clear the id of each value so that the service will treat it all as new.
        for (DataFieldValue val : values) {
            val.setId(InstanceId.create("-1"));
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
        //validateFieldData();
    }

    /*
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
                    if (f1.getReferenceId().equals(f2.getReferenceId())) {
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
                    if (!f1.getReferenceId().equals(f2.getReferenceId())) {
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
    */
}

