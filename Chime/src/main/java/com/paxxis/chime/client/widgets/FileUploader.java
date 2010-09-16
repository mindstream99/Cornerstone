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
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ProgressBar;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.editor.FileEditorListener;

/**
 *
 * @author Robert Englander
 */
public class FileUploader extends ChimeWindow {
    public enum FileType {
        Image,
        File
    }

    private FileEditorListener _listener;

    private Button _okButton;
    private Button _cancelButton;
    private FormPanel _form = new FormPanel();
    private ProgressBar progressBar = new ProgressBar();
    private DataField _field;
    private DataFieldValue _value;
    private FileUploadField file;
    private FileType fileType;
    //private DataInstance licenseInstance = null;

    public FileUploader(FileType type, DataField field, DataFieldValue value, FileEditorListener listener) {
        super();
        fileType = type;
        _field = field;
        _value = value;
        _listener = listener;
    }

    private void updateButtonState() {
        //boolean canUpload = licenseInstance != null && file.getValue() != null
        boolean canUpload = file.getValue() != null && file.getValue().length() > 0;
        _okButton.setEnabled(canUpload);
    }

    protected void init() {
        setModal(true);

        if (fileType == FileType.Image) {
            setHeading("Upload Image");
        } else {
            setHeading("Upload File");
        }

        setMaximizable(false);
        setMinimizable(false);
        setCollapsible(false);
        setClosable(true);
        setResizable(false);
        setWidth(450);

        _form.setHeaderVisible(false);
        _form.setBorders(false);
        _form.setBodyBorder(false);
        _form.setStyleAttribute("padding", "5");
        _form.setAction(GWT.getHostPageBaseURL() + "FileManager");
        _form.setEncoding(Encoding.MULTIPART);
        _form.setMethod(Method.POST);
        _form.setButtonAlign(HorizontalAlignment.CENTER);
        _form.setFrame(true);
        _form.setFieldWidth(300);
        _form.setLabelWidth(85);

        LayoutContainer lc = new LayoutContainer();

        file = new FileUploadField();

        if (fileType == FileType.File) {
            file.setName("uploaded.File");
        } else {
            file.setName("uploaded.Image");
        }

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
                    updateButtonState();
                }
            }
        );

        _form.add(af);

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

         _cancelButton = new Button("Cancel");
        _cancelButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					hide();
				}
            }
        );

        _form.addButton(_cancelButton);

        _okButton = new Button("Upload");
        _okButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                    progressBar.setVisible(true);
                    progressBar.auto();
                    _cancelButton.setEnabled(false);
                    _okButton.setEnabled(false);

                    if (fileType == FileType.File) {
                        file.setName("uploaded.File");
                    } else {
                        file.setName("uploaded.Image");
                    }
                    _form.submit();
                }
            }
        );

        _form.addButton(_okButton);
        
        progressBar.setIncrement(20);
        progressBar.setInterval(100);
        progressBar.updateText("Uploading...");
        progressBar.setVisible(false);
        _form.add(progressBar);

        add(_form);
        updateButtonState();
    }

    private void notifyComplete(String fileId, String mimeType, String extension, long size) {
        hide();
        _listener.onEdit(fileId, mimeType, extension, size);
    }
}
