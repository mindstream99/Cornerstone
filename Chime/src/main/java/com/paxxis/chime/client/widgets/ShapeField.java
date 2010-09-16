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

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.core.Template;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableRowLayout;
import com.paxxis.chime.client.InstanceUpdateListener;
import com.paxxis.chime.client.LoginResponseObject;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceManagerListener;
import com.paxxis.chime.client.Utils;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.portal.PortletContainer;
import com.paxxis.chime.client.portal.PortletContainer.HeaderType;

/**
 *
 * @author Robert Englander
 */
public class ShapeField extends LayoutContainer
{
    private static final String CONTENTTEMPLATE = "<div id='type-field'><span id='type-text-content'>{content}</span></div>";

    private static Template _contentTemplate;

    static
    {
        _contentTemplate = new Template(CONTENTTEMPLATE);
        _contentTemplate.compile();
    }

    private Shape _type;
    private DataField _field;

    private LayoutContainer _contentContainer;
    private InterceptedHtml _content;

    private ToolButton actionsButton;
    private PortletContainer leftSide;

    private InstanceUpdateListener _saveListener;

    public ShapeField(Shape type, DataField field,
            InstanceUpdateListener saveListener) {

        super();
        //super(Style.Orientation.VERTICAL, null, HeaderType.Transparent, false);
        _type = type;
        _field = field;
        _saveListener = saveListener;

        init();
    }

    private void init()
    {
        TableRowLayout tableLayout = new TableRowLayout();
        tableLayout.setColumns(2);
        setLayout(tableLayout);
        leftSide = new PortletContainer(Style.Orientation.VERTICAL, null, HeaderType.Transparent, false);

        TableData toolData = new TableData();
        toolData.setVerticalAlign(VerticalAlignment.TOP);
        toolData.setMargin(3);
        toolData.setWidth("175");
        add(leftSide, toolData);

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

        _contentContainer = new LayoutContainer();
        toolData = new TableData();
        toolData.setVerticalAlign(VerticalAlignment.TOP);
        toolData.setMargin(3);
        toolData.setWidth("-1");
        add(_contentContainer, toolData);

        _content = new InterceptedHtml();
        _contentContainer.add(_content);

        update();
    }
    
    private void setupActionMenu() {
        actionsButton.addSelectionListener(
                new SelectionListener<IconButtonEvent>() {
            @Override
             public void componentSelected(IconButtonEvent ce) {
             }
        });

    }

    private void updateActionsButton() {
        boolean canEdit = (_field.isUserEditable() && _type.canUpdate(ServiceManager.getActiveUser()));
        actionsButton.setVisible(canEdit);
    }

    public void updateDataInstance(Shape type) {
        _type = type;
        update();
    }

    private void update()
    {
        leftSide.setHeading(getName(_field));
        generateContent();
        updateActionsButton();
    }

    private static String getName(DataField field)
    {
        return field.getName();
    }

    private void generateContent() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<b>Description:</b> " + _field.getDescription());
        buffer.append("<br><b>Shape:</b> " + Utils.toHoverUrl(_field.getShape()));
        buffer.append("<br><b>Maximum Values:</b> ");
        if (_field.getMaxValues() == 0) {
            buffer.append("Unlimited");
        } else {
            buffer.append(_field.getMaxValues());
        }

        Params params = new Params();
        params.set("content", buffer.toString());
        String txt = _contentTemplate.applyTemplate(params);
        _content.setHtml(txt);
    }
}
