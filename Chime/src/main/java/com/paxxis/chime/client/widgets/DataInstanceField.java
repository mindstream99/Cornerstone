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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.core.Template;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Element;
import com.paxxis.chime.client.FilledColumnLayout;
import com.paxxis.chime.client.FilledColumnLayoutData;
import com.paxxis.chime.client.InstanceUpdateListener;
import com.paxxis.chime.client.LoginResponseObject;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceManagerListener;
import com.paxxis.chime.client.Utils;
import com.paxxis.chime.client.InstanceUpdateListener.Type;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.editor.FieldEditListener;
import com.paxxis.chime.client.editor.FieldEditorListener;
import com.paxxis.chime.client.editor.MultiReferenceEditorWindow;
import com.paxxis.chime.client.editor.MultiTextEditorWindow;
import com.paxxis.chime.client.editor.NumberDataEditorWindow;
import com.paxxis.chime.client.editor.ReferenceDataEditorWindow;
import com.paxxis.chime.client.editor.TextFieldEditorWindow;
import com.paxxis.chime.client.portal.PortletContainer;
import com.paxxis.chime.client.portal.PortletContainer.HeaderType;

/**
 *
 * @author Robert Englander
 */
public class DataInstanceField extends LayoutContainer
{
    class FieldContent {
        private DataFieldValue _value;
        private Shape _type;
        private String stringContent = null;
        
        public FieldContent(Shape type, DataFieldValue value) {
            _type = type;
            _value = value;
        }
        
        public String getContentString() {
        	if (stringContent == null) {
        		generateContent();
        	}
        	
        	return stringContent;
        }
        
        private boolean generateContent() {
            StringBuffer buffer = new StringBuffer();
            
            String valueName = _value.getName().trim();
            boolean isInternal = _field.getShape().isPrimitive();
            stringContent = "";

            boolean isImageReference = false;

            if (isInternal)
            {
                if (_type.getId().equals(Shape.IMAGE_ID) &&
                        _field.getId().equals(Shape.FILE_ID)) {
                    isImageReference = true;
                    stringContent = _value.getName();
                }
                else if (_field.getShape().getId().equals(Shape.URL_ID))
                {
                    String name = Utils.toExternalUrl(valueName, valueName);
                    buffer.append(name);
                }
                else if (_field.getShape().isNumeric())
                {
                    // TODO when formatting is added to the field definition, we'll
                    // apply whatever is specified
                    Double dval = Double.valueOf(_value.getName());
                    NumberFormat fmt = NumberFormat.getDecimalFormat();
                    String formatted = fmt.format(dval);

                    buffer.append(formatted);
                }
                else
                {
                    buffer.append(_value.getName());
                }

                if (!isImageReference) {
                    String temp = buffer.toString();
                    temp = temp.replaceAll("<ul", "<ul class='forceul'");
                    temp = temp.replaceAll("<ol", "<ol class='forceol'");
                    stringContent = temp;
                }
            }
            else
            {
                String name = Utils.toHoverUrl(_value.getReferenceId(), valueName);
                buffer.append(name);
                stringContent = buffer.toString();
            }
            
            return isImageReference;
        }
    }
    
    private static final String CONTENTTEMPLATE = "<div id='data-field'><span id='data-text-content'>{content}</span></div>";
    
    private static Template _contentTemplate;

    static
    {
        _contentTemplate = new Template(CONTENTTEMPLATE);
        _contentTemplate.compile();
    }

    private DataInstance _instance;
    private Shape _type;
    private DataField _field;
    
    private Menu actionMenu;
    private MenuItem createMenuItem;
    private MenuItem editMenuItem;
    private MenuItem removeMenuItem;
    private MenuItem removeAllMenuItem;
    private ToolButton actionsButton;
    private PortletContainer leftSide;
    private LayoutContainer rightSide;
    private InterceptedHtml rightSideHtml;

    private FieldEditListener fieldEditListener;

    private FieldEditorListener _textListener;
    private InstanceUpdateListener _saveListener;
    
    private List<Runnable> postRenderList = new ArrayList<Runnable>();
    
    public DataInstanceField(DataInstance instance, Shape type, DataField field, 
            InstanceUpdateListener saveListener) {

        super();
        _instance = instance;
        _type = type;
        _field = field;
        _saveListener = saveListener;
        
        _textListener  = new FieldEditorListener() {
            public void onSave(DataField field, DataFieldValue value, String text) {
                sendEdit(field, value, text);
            }
        };

        fieldEditListener = new FieldEditListener() {
            public void onEdit(DataInstance instance, Shape type, DataField field) {
                sendEdit(instance, type, field);
            }
        };
    }

    public void onRender(Element parent, int index) {
    	super.onRender(parent, index);
        init();
        
        for (Runnable r : postRenderList) {
        	r.run();
        }
        
        postRenderList.clear();
    }

    private void init()
    {
    	setLayout(new FilledColumnLayout(HorizontalAlignment.LEFT));
        leftSide = new PortletContainer(Style.Orientation.VERTICAL, null, HeaderType.Transparent, false);
        //leftSide.setWidth(175);
        add(leftSide, new FilledColumnLayoutData(175));
        
        actionsButton = new ToolButton("x-tool-save");
        actionsButton.setVisible(false);
        leftSide.addHeaderItem(actionsButton);

        setupActionMenu();

        ServiceManager.addListener(
            new ServiceManagerListener() {
                public void onLoginResponse(LoginResponseObject resp) {
                    updateActionsButton();
                }

                public void onLogout() {
                    updateActionsButton();
                }

                public void onDataInstanceUpdated(DataInstance instance) {
                }
            }
        );

        rightSide = new LayoutContainer();
        rightSide.setLayout(new RowLayout());
        add(rightSide, new FilledColumnLayoutData());
        rightSideHtml = new InterceptedHtml();
        rightSide.add(rightSideHtml, new RowData(1, -1, new Margins(10, 0, 0, 0)));
        
        update();
    }

    private void addNewData() {
        Shape fieldType = _field.getShape();
        if (_field.getMaxValues() != 0 && _field.getMaxValues() == _instance.getFieldValues(fieldType, _field).size()) {
            String msg = _field.getName() + " can't have more than " + _field.getMaxValues();
            if (_field.getMaxValues() == 1) {
                msg += " value.";
            } else {
                msg += " values.";
            }
            ChimeMessageBox.alert("EndSlice", msg, null);
        }
        else if (_field.getShape().isPrimitive()) {
            String typeName = _field.getShape().getName();
            if (typeName.equals("Text") || typeName.equals("Rich Text")) {
                if (_type.getName().equals("Image") &&
                        _field.getName().equals("File ID")) {
                    //FileUploader editor = new FileUploader(FileType.Image, _field, null, _textListener);
                    //editor.show();
                } else {
                    TextFieldEditorWindow editor = new TextFieldEditorWindow(_type, _field, null, _textListener);
                    editor.show();
                }
            } else if (typeName.equals("Number")) {
                NumberDataEditorWindow editor = new NumberDataEditorWindow(_field, null, _textListener);
                editor.show();
            }
        } else {
            ReferenceDataEditorWindow editor = new ReferenceDataEditorWindow(_field, null, _textListener);
            editor.show();
        }
    }

    private void setupActionMenu() {
        actionsButton.addSelectionListener(
                new SelectionListener<IconButtonEvent>() {
            @Override
             public void componentSelected(IconButtonEvent ce) {
                //actionMenu.show(actionsButton);
                if (!_field.getShape().isPrimitive()) {
                    MultiReferenceEditorWindow w = new MultiReferenceEditorWindow(_instance, _type, _field, fieldEditListener);
                    w.show();
                } else {
                    String typeName = _field.getShape().getName();
                    if (typeName.equals("Rich Text")) {
                        List<DataFieldValue> list = _instance.getFieldValues(_type, _field);
                        DataFieldValue val = null;
                        if (list.size() == 1) {
                            val = list.get(0);
                        }

                        TextFieldEditorWindow editor = new TextFieldEditorWindow(_type, _field, val, _textListener);
                        editor.show();
                    } else { // if (typeName.equals("Text")) {
                        MultiTextEditorWindow w = new MultiTextEditorWindow(_instance, _type, _field, fieldEditListener);

                        int x = actionsButton.getAbsoluteLeft();
                        int y = actionsButton.getAbsoluteTop() + actionsButton.getOffsetHeight() + 1;
                        w.show(); //At(x, y);
                    }
                }
             }
        });

    }

    private void sendDelete(DataField field, DataFieldValue value) {
        if (value != null) {
            _instance.getFieldValues(_type, field).remove(value);
        } else {
            _instance.getFieldValues(_type, field).clear();
        }
        
        _saveListener.onUpdate(_instance, Type.FieldData);
    }
    
    private void sendEdit(DataField field, DataFieldValue value, String text) {
        // if the value is null, then this is new data, otherwise this is modified data
        if (value != null) {
            if (field.getShape().isPrimitive()) {
                value.setName(text);
            } else {
                value.setReferenceId(InstanceId.create(text));
            }
        } else {
            if (field.getShape().isPrimitive()) {
                value = new DataFieldValue(text, field.getShape().getId(), InstanceId.UNKNOWN, null);
            } else {
                value = new DataFieldValue(InstanceId.create(text), text, 
                		field.getShape().getId(), InstanceId.UNKNOWN, null);
            }
            List<DataFieldValue> list = _instance.getFieldValues(_type, field);
         
            list.add(value);
        }
        
        _saveListener.onUpdate(_instance, Type.FieldData);
    }

    private void sendEdit(DataInstance instance, Shape type, DataField field) {
        _saveListener.onUpdate(instance, Type.FieldData);
    }

    private void updateActionsButton() {
        boolean canEdit = ((_field.isUserEditable() && _instance.canUpdate(ServiceManager.getActiveUser())) ||
        		(!_field.isUserEditable() && ServiceManager.getActiveUser().isAdmin()));
        actionsButton.setVisible(canEdit);
    }

    public void updateDataInstance(final DataInstance instance, final boolean refresh) {
    	
    	Runnable r = new Runnable() {
    		public void run() {
    	        _instance = instance;
    	        if (refresh) {
    	            update();
    	        }
    		}
    	};
    	
    	if (isRendered()) {
    		r.run();
    	} else {
    		postRenderList.add(r);
    	}
    }
    
    private void update() {
        leftSide.setHeading(getName(_field));

        List<DataFieldValue> values = _instance.getFieldValues(_type, _field);
        boolean isInternal = _field.getShape().isPrimitive();

        StringBuffer buffer = new StringBuffer();
        
        boolean notFirst = false;
        for (DataFieldValue value : values) {
            FieldContent fieldContent = new FieldContent(_type, value);
            if (isInternal && !_field.getShape().getId().equals(Shape.NUMBER_ID)) {
            	if (notFirst) {
            		buffer.append("<br><br>");
            	} else {
            		notFirst = true;
            	}
            	
            	buffer.append(fieldContent.getContentString());
            } else {
            	buffer.append(fieldContent.getContentString() + "&nbsp;&nbsp;&nbsp;&nbsp; ");
            }
        }
        
        Params params = new Params();
        String temp = buffer.toString();
        params.set("content", temp);
        temp = _contentTemplate.applyTemplate(params);
        rightSideHtml.setHtml(temp);

        updateActionsButton();
        layout(true);
    }
    
    private static String getName(DataField field) {
        return field.getName();
    }
    
    private static String getDescription(DataField field)
    {
        String desc = field.getDescription();
        
        if (desc == null || desc.length() == 0)
        {
            return "";
        }
        else
        {
            return " (" + desc + ")";
        }
    }
    

}
