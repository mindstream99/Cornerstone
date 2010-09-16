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

import com.extjs.gxt.ui.client.core.Template;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.paxxis.chime.client.common.DataInstance;

/**
 *
 * @author Robert Englander
 */
public class SubscribeWindow extends ChimePopup {
    public interface SubscribeChangeListener {
        public void onSubscribeChange(boolean subscribe);
    }

    private static final String TEMPLATE = "<div id='lock-msg'>{text}</div>";

    private static Template _template;

    static
    {
        _template = new Template(TEMPLATE);
        _template.compile();
    }

    private ToolBar toolBar;
    private Button yesItem;
    private Button noItem;
    //private Button closeItem;
    private DataInstance dataInstance;
    private InterceptedHtml htmlPanel;
    private SubscribeChangeListener listener;
    private boolean userSubscribed = false;

    public SubscribeWindow(DataInstance instance, SubscribeChangeListener listener) {
        super();
        this.listener = listener;
        dataInstance = instance;
        userSubscribed = instance.getSocialContext().isRegisteredInterest();

        baseStyle = "x-popup";

        setShadow(false);
        setBorders(true);
        this.setConstrainViewport(true);
        setStyleAttribute("background", "#fbf0d2");
        setStyleAttribute("border", "2px solid #003399");
        setWidth(300);
    }

    public void onRender(Element target, int index) {
    	super.onRender(target, index);
    	init();
    }
    
    protected void init() {
        htmlPanel = new InterceptedHtml(false);
        setLayout(new RowLayout());
        add(htmlPanel, new RowData(1, -1, new Margins(10, 10, 10, 10)));

        LayoutContainer spacer = new LayoutContainer();
        spacer.setHeight(10);
        add(spacer, new RowData(1, -1));

        String msg;
        if (userSubscribed) {
            msg = "You are subscribed for changes to this data.  Do you want to unsubscribe?";
        } else {
            msg = "Do you want to subscribe for changes to this data?";
        }

        Params params = new Params();
        params.set("text", msg);
        String content = _template.applyTemplate(params);
        htmlPanel.setHtml(content);

        toolBar = new ToolBar();
        //toolBar.setStyleAttribute("background", "#fbf0d2");
        toolBar.add(new FillToolItem());

        yesItem = new Button("Yes");
        //yesItem.setIconStyle("yes-icon");
        toolBar.add(yesItem);

        noItem = new Button("No");
        //noItem.setIconStyle("no-icon");
        toolBar.add(noItem);

        //closeItem = new Button("Close");
        //closeItem.setIconStyle("close-icon");
        //toolBar.add(closeItem);

        yesItem.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent ce) {
                    listener.onSubscribeChange(!userSubscribed);
                    hide();
                }
            }
        );

        noItem.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent ce) {
                    hide();
                }
            }
        );

        /*
        closeItem.addSelectionListener(
            new SelectionListener<ComponentEvent>() {
                @Override
                public void componentSelected(ComponentEvent ce) {
                    hide();
                }
            }
        );
        */

        yesItem.setVisible(true);
        noItem.setVisible(true);
        //closeItem.setVisible(closeOnly);

        add(toolBar, new RowData(1, -1, new Margins(10, 0, 0, 0)));
    }

    protected void doCancel()
    {
        hide();
    }

}


