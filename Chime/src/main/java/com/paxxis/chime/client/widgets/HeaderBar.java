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
import com.extjs.gxt.ui.client.event.BoxComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;

/**
 *
 * @author Robert Englander
 */
public class HeaderBar extends LayoutContainer {
    private static int id = 1;
    private static final String TEMPLATE = "<div id='endslice-paginator'><div id='{id}' style='float: left; padding-top: 2px; padding-right: 2px;'></div><div id='endslice-paginator-title'>&nbsp;&nbsp;</div></div>";
    private static Template template;

    static
    {
        template = new Template(TEMPLATE);
        template.compile();
    }

    private ToolBar toolbar;
    private HtmlContainer container;

    public HeaderBar() {
        init();
    }

    private void init() {
        container = new HtmlContainer();
        Params params = new Params();
        int divid = id++;

        toolbar = new ToolBar();
        toolbar.setBorders(false);
        toolbar.setStyleAttribute("background", "transparent");

        params.set("id", divid);
        container.setHtml(template.applyTemplate(params));
        container.addListener(Events.Resize,
            new Listener<BoxComponentEvent>() {

                public void handleEvent(BoxComponentEvent evt) {
                    sizeToolbar();
                }

            }
        );

        container.add(toolbar, "#" + divid);

        add(container);

        DeferredCommand.addCommand(
            new Command() {
                public void execute() {
                    sizeToolbar();
                }
            }
        );
    }

    private void sizeToolbar() {
        toolbar.setWidth(getWidth() - 6);

    }

    //public void addTool(Component comp) {
    //
    //}

    public void addTool(Component item) {
        toolbar.add(item);
    }
}
