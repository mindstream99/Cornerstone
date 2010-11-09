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

package com.paxxis.chime.client.portal;

import java.util.List;

import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.paxxis.chime.client.ChimeAsyncCallback;
import com.paxxis.chime.client.InstanceUpdateListener;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceResponseObject;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.ErrorMessage;
import com.paxxis.chime.client.common.RunCALScriptRequest;
import com.paxxis.chime.client.common.RunCALScriptResponse;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.client.common.cal.IValue;
import com.paxxis.chime.client.common.cal.Table;
import com.paxxis.chime.client.common.portal.PortletSpecification;
import com.paxxis.chime.client.editor.CALScriptEditor;
import com.paxxis.chime.client.editor.CALScriptEditor.TextEditorListener;
import com.paxxis.chime.client.widgets.AnalyticDetailPanel;
import com.paxxis.chime.client.widgets.ChimeMessageBox;

/**
 *
 * @author Robert Englander
 */
public class AnalyticDetailPortlet extends PortletContainer {
    private AnalyticDetailPanel detailPanel;
    private ToolButton editButton = null;
    private ToolButton runButton = null;
    private DataInstance dataInstance = null;
    private String script = "";
    private boolean newScript = true;
    private InstanceUpdateListener updateListener;

    public AnalyticDetailPortlet(PortletSpecification spec, HeaderType type, InstanceUpdateListener listener) {
        super(spec, HeaderType.Shaded, true);
        updateListener = listener;
    }

    public void setDataInstance(final DataInstance instance) {

    	Runnable r = new Runnable() {
    		public void run() {
    	        getBody().removeAll();
    	        script = "";
    	        newScript = true;
    	        if (instance.getShapes().get(0).getId().equals(Shape.ANALYTIC_ID)) {
    	            dataInstance = instance;
    	            detailPanel = new AnalyticDetailPanel(dataInstance, 400, true);
    	            getBody().add(detailPanel, new RowData(1, -1));

    	            DataField field = dataInstance.getShapes().get(0).getField("Script");
    	            List<DataFieldValue> vals = dataInstance.getFieldValues(dataInstance.getShapes().get(0), field);
    	            if (vals.size() > 0) {
    	                script = vals.get(0).getValue().toString();
    	                newScript = false;
    	            }
    	        }

    	        editButton.setVisible(instance.canUpdate(ServiceManager.getActiveUser()));
    	        getBody().layout();

    	        DeferredCommand.addCommand(
    	            new Command() {
    	                public void execute() {
    	                    runScript();
    	                }
    	            }
    	        );
    		}
    	};
    	
    	if (isRendered()) {
    		r.run();
    	} else {
    		addPostRenderRunnable(r);
    	}
    }

    protected void init() {
    	super.init();

    	getBody().setLayout(new RowLayout());
        editButton = new ToolButton("x-tool-save");
        runButton = new ToolButton("x-tool-gear");

        addHeaderItem(editButton);
        addHeaderItem(runButton);

        setHeading("Analytic Details");

        editButton.addSelectionListener(
                new SelectionListener<IconButtonEvent>() {
            @Override
            public void componentSelected(IconButtonEvent ce) {
                CALScriptEditor w = new CALScriptEditor(
                    new TextEditorListener() {
                        public void onComplete(String text) {
                            updateScript(text);
                        }
                    }, script
                );

                w.show();
            }
        });

        runButton.addSelectionListener(
                new SelectionListener<IconButtonEvent>() {
            @Override
            public void componentSelected(IconButtonEvent ce) {
                runScript();
            }
        });

    }

    private void runScript() {
        ChimeAsyncCallback<ServiceResponseObject<RunCALScriptResponse>> callback = 
        			new ChimeAsyncCallback<ServiceResponseObject<RunCALScriptResponse>>() {
            public void onSuccess(ServiceResponseObject<RunCALScriptResponse> response) {
                if (response.isResponse()) {
                    RunCALScriptResponse resp = response.getResponse();
                    IValue result = resp.getResult();
                    if (result instanceof Table) {
                        showTableResult((Table)result);
                    } else {
                        showTextResult("<br>&nbsp;&nbsp;&nbsp;" + result.valueAsString());
                    }
                } else {
                    ErrorMessage msg = response.getError();
                    String text;
                    if (msg.getType() == ErrorMessage.Type.SessionExpiration) {
                        ServiceManager.logout();
                        text = "Your session has expired.  Please login again.";
                        ChimeMessageBox.alert("Error", text, null);
                    } else {
                        text = msg.getMessage();
                        text = text.replaceAll("<", "&lt;");
                        text = text.replaceAll(">", "&gt;");
                        text = text.replaceAll("\n", "<br>");
                        showTextResult(text);
                    }

                }
            }
        };

        if (hasScript()) {
            User user = ServiceManager.getActiveUser();
            RunCALScriptRequest req = new RunCALScriptRequest();
            req.setData(dataInstance);
            req.setUser(user);
            ServiceManager.getService().sendRunCALScriptRequest(req, callback);
        }
    }

    private boolean hasScript() {
        boolean result = false;

        if (dataInstance != null) {
            DataField field = dataInstance.getShapes().get(0).getField("Script");
            List<DataFieldValue> vals = dataInstance.getFieldValues(dataInstance.getShapes().get(0), field);
            if (!vals.isEmpty()) {
            	DataFieldValue value = vals.get(0);
            	String script = value.getValue().toString().trim();
            	result = !script.isEmpty();
            }
        }
        return result;
    }

    private void showTextResult(String text) {
        detailPanel.showResult(text);
    }

    private void showTableResult(Table table) {
        detailPanel.showResult(table);
    }
    
    private void updateScript(String scriptText) {

        DataField field = dataInstance.getShapes().get(0).getField("Script");
        List<DataFieldValue> vals = dataInstance.getFieldValues(dataInstance.getShapes().get(0), field);

        if (newScript) {
            // add this one
            DataFieldValue val = new DataFieldValue();
            val.setValue(scriptText);
            val.setShapeId(field.getShape().getId());
            vals.add(val);
        } else {
            // modify the existing one
            DataFieldValue val = vals.get(0);
            val.setValue(scriptText);
        }

        updateListener.onUpdate(dataInstance, InstanceUpdateListener.Type.FieldData);
    }
}
