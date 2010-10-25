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

import com.extjs.gxt.ui.client.widget.Html;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.paxxis.chime.client.Utils;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.detailpopup.DetailPopupManager;

/**
 *
 * @author Robert Englander
 */
public class InterceptedHtml extends Html {

    public interface InterceptedLinkListener {
        public boolean onLink(String url);
    }

    private InterceptedLinkListener linkListener = null;
    private String _hovering = "";
    private boolean hoverEnabled = true;

    abstract class HoverTimer extends Timer {
        private String token;
        public HoverTimer(String token) {
            this.token = token;
        }

        protected String getToken() {
            return token;
        }
    }

    public InterceptedHtml(boolean hover) {
        hoverEnabled = hover;
        sinkEvents(Event.ONCLICK | Event.MOUSEEVENTS);
    }

    public InterceptedHtml() {
        this(true);
    }

    public void setLinkListener(InterceptedLinkListener listener) {
        linkListener = listener;
    }

    public void setHtml(String html)
    {
        if (hoverEnabled) {
            // convert any chime://#detail urls with appropriate urls
        	int idx;
        	while (-1 != (idx = html.indexOf("<a href=\"chime://#detail:"))) {
        		// find the instance id
        		int idx2 = idx + 25;
        		int idx3 = idx2 - 1;
        		while (html.charAt(idx3) != '"') {
        			idx3++;
        		}
        		String instStr = html.substring(idx2, idx3);
        		InstanceId instId = InstanceId.create(instStr);
        		idx2 = html.indexOf("</a>",idx3 + 1) + 4;
        		
				String text = html.substring(idx3 + 2, idx2 - 4);
        		
        		String part1 = html.substring(0, idx);
        		String part2 = Utils.toHoverUrl(instId, text);
        		String part3 = html.substring(idx2);
        		html = part1 + part2 + part3;
        	}

        	super.setHtml(html);
            //String newHtml = html.replaceAll("chime://#detail-", GWT.getHostPageBaseURL() + "#detail-");
        } else {
            super.setHtml(html);
        }
    }
    
    @Override
    public void onBrowserEvent(final Event evt)
    {
        Element elem = evt.getTarget();
        String href = elem.getAttribute("href");
        
        String url = GWT.getHostPageBaseURL();
        
        if (evt.getTypeInt() == Event.ONCLICK) {
            boolean handled = false;
            if (linkListener != null) {
                handled = linkListener.onLink(href);
                if (handled) {
                    evt.stopPropagation();
                }
            }

            if (!handled) {
                boolean keepLooking = true;
                while (keepLooking) {
                    if (href.equals("")) {
                        elem = elem.getParentElement();
                        if (elem == null) {
                            keepLooking = false;
                        } else {
                            href = elem.getAttribute("href");
                        }
                    } else {
                        keepLooking = false;
                    }

                }

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
            }
        } else if (evt.getTypeInt() == Event.ONMOUSEOVER) {
            boolean keepLooking = true;
            while (keepLooking) {
                if (href.equals("")) {
                    elem = elem.getParentElement();
                    if (elem == null) {
                        keepLooking = false;
                    } else {
                        href = elem.getAttribute("href");
                    }
                } else {
                    keepLooking = false;
                }

            }

            final Element theTarget = elem;

            if (hoverEnabled && href.startsWith(url)) {
                int idx = href.indexOf("#detail");
                if (idx != -1) {
                    if (true) { //elem.getAttribute("chime-hover").equals("true")) {
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
            if (hoverEnabled) {
                _hovering = "";
                DetailPopupManager.instance().hidePopup();
            }

            super.onBrowserEvent(evt);
        } else {
            super.onBrowserEvent(evt);
        }
    }
}
