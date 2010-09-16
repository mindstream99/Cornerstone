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
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.paxxis.chime.client.common.portal.PortletSpecification;
import com.paxxis.chime.client.widgets.ChimeLayoutContainer;

/**
 *
 * @author Robert Englander
 */
public abstract class LayoutProxyPortlet extends PortletContainer {

    public interface LayoutProxyListener {
        public void onLayoutProxyDelete(LayoutProxyPortlet portlet);
        public void onLayoutProxyEdit(PortletSpecification pspec);
    }

    private PortletSpecification portletSpec;
    private ToolButton actionsButton = null;
    private LayoutProxyListener listener;
    private ChimeLayoutContainer propertiesContainer;

    public static LayoutProxyPortlet create(PortletSpecification spec, LayoutProxyListener listener) {
        LayoutProxyPortlet portlet = null;

        switch (spec.getType()) {
            case NamedSearch:
                portlet = new NamedSearchPortlet.NamedSearchPortletProxy(spec, listener);
                break;
            case Analytic:
                portlet = new AnalyticPortlet.AnalyticPortletProxy(spec, listener);
                break;
            case DataInstance:
                portlet = new DataInstancePortlet.DataInstancePortletProxy(spec, listener);
                break;
            case ExternalSite:
                break;
            case ImageGallery:
                break;
            case RichText:
                portlet = new RichTextPortlet.RichTextPortletProxy(spec, listener);
                break;
            case ImageRenderer:
                portlet = new ImageRendererPortlet.ImageRendererPortletProxy(spec, listener);
                break;
        }

        return portlet;
    }

    public LayoutProxyPortlet(PortletSpecification spec, LayoutProxyListener listener) {
        super(null, HeaderType.Bordered, false);
        portletSpec = spec;
        this.listener = listener;
    }

    protected void init() {
    	super.init();
        getBody().setHeight(120);

        actionsButton = new ToolButton("x-tool-save");
        addHeaderItem(actionsButton);
        propertiesContainer = new ChimeLayoutContainer() {
        	public void init() {
        	}
        };
        propertiesContainer.setBorders(false);
        propertiesContainer.setLayout(new FlowLayout());
        getBody().add(propertiesContainer);

        setupActionMenu();
    }

    protected ChimeLayoutContainer getPropertiesContainer() {
        return propertiesContainer;
    }

    protected abstract void onEdit();

    private void setupActionMenu() {
        final Menu actionMenu = new Menu();

        MenuItem item  = new MenuItem("Edit");
        actionMenu.add(item);
        item.addSelectionListener(
            new SelectionListener<MenuEvent>() {
                @Override
                public void componentSelected(MenuEvent ce) {
                    onEdit();
                }
            }
        );

        item  = new MenuItem("Delete");
        actionMenu.add(item);
        item.addSelectionListener(
            new SelectionListener<MenuEvent>() {
                @Override
                public void componentSelected(MenuEvent ce) {
                    listener.onLayoutProxyDelete(LayoutProxyPortlet.this);
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

    protected LayoutProxyListener getListener() {
        return listener;
    }

    @Override
    public PortletSpecification getSpecification() {
        return portletSpec;
    }
}
