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
import com.paxxis.chime.client.Utils;
import com.paxxis.chime.client.common.DataInstance;

/**
 *
 * @author Robert Englander
 */
public class LockPanel extends LayoutContainer {
    public interface LockPanelListener {
        public void onLock(boolean lock);
    }

    //private InterceptedHtml html;
    private IconButton lockButton;
    private DataInstance dataInstance = null;
    private LockPanelListener listener;

    public LockPanel(LockPanelListener listener) {
        this.listener = listener;
        init();
    }

    private void init() {
        setLayout(new RowLayout());
        setBorders(false);
        setStyleAttribute("backgroundColor", "transparent");

        lockButton = new IconButton("unlocked24-icon");
        lockButton.setSize(24, 24);
        LayoutContainer lc = new LayoutContainer();
        lc.setLayout(new CenterLayout());
        lc.add(lockButton);
        lc.setHeight(36);
        add(lc, new RowData(1, -1));

        lockButton.addSelectionListener(
            new SelectionListener<IconButtonEvent>() {
                @Override
                public void componentSelected(IconButtonEvent ce) {
                    if (dataInstance != null) {
                        LockWindow w = new LockWindow(dataInstance,
                            new LockWindow.LockChangeListener() {
                                public void onEditLock(boolean lock) {
                                    listener.onLock(lock);
                                }
                            }
                        );
                        int x = lockButton.getAbsoluteLeft();
                        int y = lockButton.getAbsoluteTop() + lockButton.getOffsetHeight() + 1;
                        w.showAt(x, y);
                    }
                }
            }
        );
    }

    public void setDataInstance(DataInstance instance) {
        dataInstance = instance;
        if (instance.getLockType() == DataInstance.LockType.NONE) {
            //html.setHtml("<div id='data-header'><span id='data-header-msg'>Click to Lock</span></div>");
            lockButton.changeStyle("unlocked24-icon");
        } else {
            //html.setHtml("<div id='data-header'><span id='data-header-msg'>Click to Unlock</span></div>");
            lockButton.changeStyle("locked24-icon");
        }

        lockButton.setEnabled(Utils.isLockVisible(instance));

        layout();
    }
}
