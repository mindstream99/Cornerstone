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

import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.paxxis.chime.client.common.DataInstance;

/**
 *
 * @author Robert Englander
 */
public class SubscribePanel extends LayoutContainer {
    public interface SubscribePanelListener {
        public void onSubscribe(boolean subscribe);
    }

    //private InterceptedHtml html;
    private IconButton subscribeButton;
    private DataInstance dataInstance = null;
    private SubscribePanelListener listener;

    public SubscribePanel(SubscribePanelListener listener) {
        this.listener = listener;
        init();
    }

    private void init() {
        setLayout(new RowLayout());
        setBorders(false);
        setStyleAttribute("backgroundColor", "transparent");

        subscribeButton = new IconButton("subscribe-icon");
        subscribeButton.setSize(24, 24);
        LayoutContainer lc = new LayoutContainer();
        lc.setLayout(new CenterLayout());
        lc.add(subscribeButton);
        lc.setHeight(36);
        add(lc, new RowData(1, -1));

        //subscribeButton.setVisible(false);

        subscribeButton.addSelectionListener(
            new SelectionListener<IconButtonEvent>() {
                @Override
                public void componentSelected(IconButtonEvent ce) {
                    if (dataInstance != null) {
                        SubscribeWindow w = new SubscribeWindow(dataInstance,
                            new SubscribeWindow.SubscribeChangeListener() {
                                public void onSubscribeChange(boolean subscribe) {
                                    listener.onSubscribe(subscribe);
                                }
                            }
                        );
                        int x = subscribeButton.getAbsoluteLeft();
                        int y = subscribeButton.getAbsoluteTop() + subscribeButton.getOffsetHeight() + 1;
                        w.showAt(x, y);
                        //w.setPosition(x, y);
                        //w.show();
                    }
                }
            }
        );
    }

    public void setDataInstance(DataInstance instance) {
        dataInstance = instance;
        if (instance.getSocialContext().isRegisteredInterest()) {
            subscribeButton.changeStyle("subscribed-icon");
        } else {
            subscribeButton.changeStyle("subscribe-icon");
        }

        subscribeButton.setVisible(!instance.isTransient());

        layout();
    }
}
