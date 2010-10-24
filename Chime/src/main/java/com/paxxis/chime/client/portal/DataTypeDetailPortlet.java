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

import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.paxxis.chime.client.InstanceUpdateListener;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.portal.PortletSpecification;
import com.paxxis.chime.client.editor.FieldDefinitionEditListener;
import com.paxxis.chime.client.editor.TypeFieldEditorWindow;

/**
 *
 * @author Robert Englander
 */
public class DataTypeDetailPortlet extends PortletContainer {
    private InstanceUpdateListener updateListener;
    private Menu actionMenu;
    private MenuItem addFieldMenuItem;
    private ToolButton actionsButton;
    private Shape dataType = null;
    
    public DataTypeDetailPortlet(PortletSpecification spec, HeaderType type, InstanceUpdateListener listener) {
        super(spec, HeaderType.Shaded, true);
        updateListener = listener;
    }

    public void setDataInstance(final DataInstance instance) {
    	
    	Runnable r = new Runnable() {
    		public void run() {
    	        dataType = (Shape)instance;

    	        getBody().removeAll();

    	        final ShapeFieldsPortlet portlet = new ShapeFieldsPortlet(null, updateListener);

    	        getBody().add(portlet, new RowData(1, -1, new Margins(5, 0, 5, 0)));
    	        getBody().layout();

    	        DeferredCommand.addCommand(
    	            new Command() {
    	                public void execute() {
    	                    portlet.setDataInstance(dataType);
    	                }
    	            }
    	        );

    	        updateActions();
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
        actionsButton = new ToolButton("x-tool-save");

        addHeaderItem(actionsButton);

        actionsButton.setVisible(false);
        setupActionMenu();

        setHeading("Field Definitions");
    }

    private void updateActions() {
        boolean canEdit = dataType.canUpdate(ServiceManager.getActiveUser());
        actionsButton.setVisible(canEdit);
    }

    private void setupActionMenu() {
        actionMenu = new Menu();

        addFieldMenuItem = new MenuItem("Add Field");
        actionMenu.add(addFieldMenuItem);
        addFieldMenuItem.addSelectionListener(
            new SelectionListener<MenuEvent>() {
                @Override
                public void componentSelected(MenuEvent ce) {
                    TypeFieldEditorWindow w = new TypeFieldEditorWindow(dataType,
                        new FieldDefinitionEditListener() {
                            public void onEdit(DataField field, Type type) {
                                updateListener.onUpdate(dataType, field, InstanceUpdateListener.Type.AddFieldDefinition);
                            }
                        }
                    );
                    w.show();
                }
            }
        );

        actionsButton.addSelectionListener(
                new SelectionListener<IconButtonEvent>() {
            @Override
            public void componentSelected(IconButtonEvent ce) {
               actionMenu.show(actionsButton);
            }
        });
    }
}
