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

import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ProgressBar;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.paxxis.chime.client.DataInputListener;
import com.paxxis.chime.client.DataInstanceResponseObject;
import com.paxxis.chime.client.LoginResponseObject;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceManagerAdapter;
import com.paxxis.chime.client.ServiceManagerListener;
import com.paxxis.chime.client.ServiceResponseObject;
import com.paxxis.chime.client.ShapeResponseObject;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceRequest;
import com.paxxis.chime.client.common.DataInstanceResponse;
import com.paxxis.chime.client.common.EditDataInstanceRequest;
import com.paxxis.chime.client.common.EditDataInstanceResponse;
import com.paxxis.chime.client.common.Scope;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.ShapeRequest;
import com.paxxis.chime.client.common.EditDataInstanceRequest.Operation;
import com.paxxis.chime.client.widgets.ChimeMessageBox;
import com.paxxis.chime.client.widgets.ChimeMessageBoxEvent;
import com.paxxis.chime.client.widgets.ChimeWindow;

/**
 *
 * @author Robert Englander
 */
public class FileCreatorWindow extends ChimeWindow
{
    public enum FileType {
        Image,
        File
    }

    public interface FileCreationListener {
        public void onFileCreated(DataInstance imageInstance);
    }

    private ButtonBar _buttonBar;
    private Button _okButton;
    private Button _cancelButton;
    private FormPanel _form = new FormPanel();
    private TextArea descriptionField;
    private ServiceManagerListener _serviceManagerListener = null;
    private TextField<String> nameField;
    private DataInstance dataInstance;
    private FileUploadField file;
    private DataInstance licenseInstance = null;
    private ProgressBar progressBar = new ProgressBar();
    private FileCreationListener createListener;
    private FileType fileType;
    private Shape fileShape = null;

    public FileCreatorWindow(FileType type, DataInstance instance, FileCreationListener listener)
    {
        super();
        fileType = type;
        dataInstance = instance;
        createListener = listener;
        setModal(true);
        setHeading("Create New " + fileType.toString());
        setMaximizable(false);
        setMinimizable(false);
        setCollapsible(false);
        setClosable(false);
        setResizable(false);
        setWidth(450);
    }

     protected void init() {
      	postInit();
         AsyncCallback callback = new AsyncCallback() {
             public void onFailure(Throwable result) {
             }

             public void onSuccess(final Object result) {
                 ShapeResponseObject resp = (ShapeResponseObject)result;
                 if (resp.isResponse()) {
                 	fileShape = resp.getResponse().getShape();
                 	validate();
                 }
             }
         };
         
         ShapeRequest req = new ShapeRequest();
         if (fileType == FileType.File) {
             req.setId(Shape.FILE_ID);
         } else {
             req.setId(Shape.IMAGE_ID);
         }
         ServiceManager.getService().sendShapeRequest(req, callback);
     }
     
     protected void postInit() {

        _form.setHeaderVisible(false);
        _form.setBorders(false);
        _form.setBodyBorder(false);
        _form.setStyleAttribute("padding", "5");
        _form.setButtonAlign(HorizontalAlignment.CENTER);
        _form.setAction(GWT.getHostPageBaseURL() + "FileManager");
        _form.setEncoding(Encoding.MULTIPART);
        _form.setMethod(Method.POST);
        _form.setFrame(true);
        _form.setFieldWidth(300);
        _form.setLabelWidth(85);

        LayoutContainer lc = new LayoutContainer();

        file = new FileUploadField();
        file.setName("uploaded." + fileType.toString());
        file.setHideLabel(true);
        file.setEmptyText("Click browse to select file");
        file.setAllowBlank(true);
        file.setWidth(300);
        file.setButtonOffset(3);
        lc.add(file);
        AdapterField af = new AdapterField(lc);
        af.setFieldLabel("File Name");
        file.addListener(Events.Valid,
            new Listener<FieldEvent>() {
                public void handleEvent(FieldEvent evt) {
                    // if the name field is empty, fill it with the file name
                    if (nameField.getRawValue().trim().length() == 0) {
                        nameField.setRawValue(file.getRawValue());
                        nameField.focus();
                        nameField.selectAll();
                    }

                    validate();
                }
            }
        );

        _form.add(af);

        nameField = new TextField<String>();
        nameField.setFieldLabel("Name");
        new KeyNav(nameField) {
            @Override
            public void onKeyPress(final ComponentEvent cd) {
                DeferredCommand.addCommand(
                    new Command() {
                        public void execute() {
                            validate();
                        }
                    }
                );
            }
        };

        _form.add(nameField);

        descriptionField = new TextArea();
        descriptionField.setFieldLabel("Description");
        _form.add(descriptionField);

        _form.addListener(Events.Submit,
            new Listener<FormEvent>() {
                public void handleEvent(FormEvent evt) {
                    progressBar.setVisible(false);
                    _okButton.setEnabled(true);
                    _cancelButton.setEnabled(true);
                    String result = evt.getResultHtml();
                    JSONValue json = JSONParser.parse(result);
                    JSONObject obj = json.isObject();
                    String fileId = obj.get("fileId").isString().stringValue();
                    String mimeType = obj.get("mimeType").isString().stringValue();
                    String extension = obj.get("extension").isString().stringValue();
                    long size = (long)obj.get("size").isNumber().doubleValue();
                    notifyComplete(fileId, mimeType, extension, size);
                }
            }
         );

        DataInputListener listener = new DataInputListener() {

            public void onDataInstance(DataInstance instance) {
                licenseInstance = instance;
                validate();
            }

            public void onStringData(String text) {
            }

        };

        _buttonBar = new ButtonBar();
        _buttonBar.setAlignment(HorizontalAlignment.CENTER);

        _okButton = new Button("Ok");
        _okButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                    progressBar.setVisible(true);
                    progressBar.auto();
                    _cancelButton.setEnabled(false);
                    _okButton.setEnabled(false);
                    _form.submit();
                }
            }
        );

        _buttonBar.add(_okButton);

        _cancelButton = new Button("Cancel");
        _cancelButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                    doCancel();
                }
            }
        );

        _buttonBar.add(_cancelButton);

        _form.add(_buttonBar);

        progressBar.setIncrement(20);
        progressBar.setInterval(100);
        progressBar.updateText("Uploading...");
        progressBar.setVisible(false);
        _form.add(progressBar);

        add(_form);

        _serviceManagerListener = new ServiceManagerAdapter() {
            public void onLoginResponse(LoginResponseObject resp) {
            }
        };

        ServiceManager.addListener(_serviceManagerListener);

        validate();
        layout();
    }

    protected void create(String fileId, String mimeType, String extension, long size) {
        EditDataInstanceRequest req = new EditDataInstanceRequest();
        createInstance(req, fileId, mimeType, extension, size);
    }

    protected void createInstance(final EditDataInstanceRequest req, final String fileId, final String mimeType, final String extension, final long size) {
        AsyncCallback callback = new AsyncCallback() {
            public void onFailure(Throwable result) {
                ChimeMessageBox.alert("Error", result.getMessage(), null);
            }

            public void onSuccess(final Object result) {
                DataInstanceResponseObject resp = (DataInstanceResponseObject)result;
                if (resp.isResponse()) {
                    DataInstanceResponse response = resp.getResponse();
                    List<DataInstance> instances = response.getDataInstances();

                    // if there are others with this name, ask the user to confirm the create.
                    // in the future, we'll show them to the user as part of the confirmation
                    int count = instances.size();
                    if (count > 0) {
                        String msg = "There are already 1 or more instances with this name.  Do you want to create it anyway?";

                        final Listener<ChimeMessageBoxEvent> l = new Listener<ChimeMessageBoxEvent>() {
                            public void handleEvent(ChimeMessageBoxEvent evt) {
                                Button btn = evt.getButtonClicked();
                                if (btn != null) {
                                    if (btn.getText().equalsIgnoreCase("yes")) {
                                        doCreate(req, fileId, mimeType, extension, size);
                                    }
                                }
                            }
                        };

                        ChimeMessageBox.confirm("Create", msg, l);
                    } else {
                        doCreate(req, fileId, mimeType, extension, size);
                    }
                } else {
                    ChimeMessageBox.alert("Error", resp.getError().getMessage(), null);
                }
            }
        };

        DataInstanceRequest request = new DataInstanceRequest();
        request.setUser(ServiceManager.getActiveUser());
        request.addQueryParameter(fileShape, "name", nameField.getValue().trim());
        ServiceManager.getService().sendDataInstanceRequest(request, callback);
    }

    protected void appendRequest(EditDataInstanceRequest req) {

    }

    protected void doCreate(EditDataInstanceRequest req, String fileId, String mimeType, String extension, long size) {
        AsyncCallback callback = new AsyncCallback() {
            public void onFailure(Throwable result) {
            }

            public void onSuccess(final Object result) {
                ServiceResponseObject<EditDataInstanceResponse> resp = (ServiceResponseObject<EditDataInstanceResponse>)result;
                if (resp.isResponse()) {
                    EditDataInstanceResponse response = resp.getResponse();
                    DataInstance imageDataInstance = response.getDataInstance();
                    notifyResults(imageDataInstance);
                    //PageManager.instance().openDetail(true, inst);
                    doCancel();
                } else {
                    ChimeMessageBox.alert("Error", resp.getError().getMessage(), null);
                }
            }
        };

        req.setUser(ServiceManager.getActiveUser());

        // the new instance uses the same scopes as the dataInstance
        for (Scope scope : dataInstance.getSocialContext().getScopes()) {
            req.addScope(scope);
        }

        // add the name and description
        req.setName(nameField.getValue().trim());
        req.setOperation(Operation.Create);
        req.addShape(fileShape);
        req.setDescription(descriptionField.getValue());

        req.addFieldData(fileShape, fileShape.getField("File ID"), fileId);
        req.addFieldData(fileShape, fileShape.getField("Extension"), extension);
        req.addFieldData(fileShape, fileShape.getField("MIME Type"), mimeType);
        req.addFieldData(fileShape, fileShape.getField("Size"), String.valueOf(size));

        // add the license
        //req.addFieldData(dataType, dataType.getField("License"), licenseInstance);

        appendRequest(req);

        ServiceManager.getService().sendEditDataInstanceRequest(req, callback);
    }

    private void validate() {
        String name = nameField.getValue();
        boolean validName = name != null && name.trim().length() > 0 && fileShape != null;

        //boolean canUpload = licenseInstance != null && file.getValue() != null
        boolean canUpload = file.getValue() != null && file.getValue().length() > 0;

        _okButton.setEnabled(validName && canUpload && fileShape != null);
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

    private void notifyResults(DataInstance image) {
        createListener.onFileCreated(image);
    }

    private void notifyComplete(String fileId, String mimeType, String extension, long size) {
        create(fileId, mimeType, extension, size);
    }

    protected void doCancel()
    {
        ServiceManager.removeListener(_serviceManagerListener);
        hide();
    }

}
