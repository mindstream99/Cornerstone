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

package com.paxxis.chime.client;

import com.paxxis.chime.client.detailpopup.DetailPopupManager;
import com.extjs.gxt.ui.client.widget.DataView;
import com.extjs.gxt.ui.client.widget.DataViewItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;

/**
 *
 * @author Robert Englander
 */
public class DataInstancesView extends DataView
{
    private String _hovering = "";

    abstract class HoverTimer extends Timer {
        private String token;
        public HoverTimer(String token) {
            this.token = token;
        }

        protected String getToken() {
            return token;
        }
    }

    public DataInstancesView()
    {
        super();
        sinkEvents(Event.ONCLICK);
    }

	public boolean insert(final DataViewItem item, final int index) {

        DeferredCommand.addCommand(
            new Command() {
                public void execute() {
                
                    boolean added = DataInstancesView.super.insert(item, index);
                }
            }
        );

        return true;
    }

    public void setSelectedItem(DataViewItem item)
    {
        super.setSelectedItem(item);
    }

    @Override
    public void onBrowserEvent(final Event evt)
    {
        String href = evt.getTarget().getAttribute("href");
        String url = GWT.getHostPageBaseURL();

        if (evt.getTypeInt() == Event.ONCLICK) {
            if (href.startsWith(url)) {
                // this is ours...
                int idx = href.indexOf("#detail");
                if (idx != -1) {
                    _hovering = "";
                    DetailPopupManager.instance().hideImmediately();
                    DOM.eventPreventDefault(evt);
                    String token = href.substring(idx + 1);
                    History.newItem(token);
                }
            }
        } else if (evt.getTypeInt() == Event.ONMOUSEOVER) {
            Element target = evt.getTarget();

            if (!href.startsWith(url)) {
                // maybe it's the parent that has the href
                target = evt.getTarget().getParentElement();
                href = target.getAttribute("href");
            }

            final Element theTarget = target;

            if (href.startsWith(url)) {
                int idx = href.indexOf("#detail");
                if (idx != -1) {
                    if (target.getAttribute("chime-hover").equals("true")) {
                        _hovering = href.substring(idx + 1);
                        HoverTimer t = new HoverTimer(_hovering) {
                            @Override
                            public void run() {
                                if (_hovering.equals(getToken())) {
                                    DetailPopupManager.instance().show(_hovering, theTarget);
                                }
                            }
                        };

                        if (!DetailPopupManager.instance().keepShowing(_hovering)) {
                            t.schedule(1000);
                        }
                    }
                }
            }
            super.onBrowserEvent(evt);
        } else if (evt.getTypeInt() == Event.ONMOUSEOUT) {
            _hovering = "";
            DetailPopupManager.instance().hidePopup();
            super.onBrowserEvent(evt);
        } else {
            super.onBrowserEvent(evt);
        }
    }
}
