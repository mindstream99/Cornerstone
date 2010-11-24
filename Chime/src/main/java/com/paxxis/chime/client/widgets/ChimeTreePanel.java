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

import java.util.Collection;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanelView;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.paxxis.chime.client.detailpopup.DetailPopupManager;
import com.paxxis.cornerstone.base.InstanceId;

/**
 * @author Robert Englander
 *
 */
public class ChimeTreePanel<M extends BaseTreeModel> extends TreePanel<M> {
    public interface InterceptedLinkListener {
        public boolean onLink(String id);
    }
    private InterceptedLinkListener linkListener = null;

	abstract class HoverTimer extends Timer {
        private String token;
        public HoverTimer(String token) {
            this.token = token;
        }

        protected String getToken() {
            return token;
        }
    }
    
	private String _hovering = "";
	private String hoverToken;
	
	public ChimeTreePanel(String token, TreeStore<M> store, boolean preventSelects) {
		super(store);
		hoverToken = token;
		if (preventSelects) {
	        setView(
            	new TreePanelView<M>() {
            		public boolean isSelectableTarget(M m, com.google.gwt.user.client.Element target) {
            			return false;
            		}
            	}
            );
		}
	}

    public void setLinkListener(InterceptedLinkListener listener) {
        linkListener = listener;
    }
	
	public void onRender(com.google.gwt.user.client.Element parent, int index) {
		super.onRender(parent, index);
        setTrackMouseOver(false);
        sinkEvents(Event.ONCLICK | Event.MOUSEEVENTS);
	}

    @Override
    public void onBrowserEvent(final Event evt)
    {
        Element elem = evt.getTarget();
        String href = elem.getAttribute("href");
        
        String url = GWT.getHostPageBaseURL();
        
        if (evt.getTypeInt() == Event.ONCLICK) {
        	boolean handled = false;
        	/*
        	if (linkListener != null) {
                int idx = href.indexOf("#" + hoverToken);
                if (idx != -1) {
                    String token = href.substring(hoverToken.length() + 2);
                    handled = linkListener.onLink(Long.valueOf(token));
                    if (handled) {
                        evt.stopPropagation();
                    }
                }
            }
            */
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
                    int idx = href.indexOf("#" + hoverToken);
                    if (idx != -1) {
                        _hovering = "";
                        DetailPopupManager.instance().hideImmediately();
                        DOM.eventPreventDefault(evt);
                        String token = href.substring(idx + 1);
                        History.newItem(token);
                        handled = true;
                    }
                }
            }

            if (!handled) {
                super.onBrowserEvent(evt);
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

            if (href.startsWith(url)) {
                int idx = href.indexOf("#" + hoverToken);
                if (idx != -1) {
                	String instId = href.substring(idx + hoverToken.length() + 1);
                	Collection<TreeNode> treeNodes = nodes.values();
                    for (TreeNode treeNode : treeNodes) {
                    	String id = treeNode.getModel().get("id");
                    	if (id.equals(instId)) {
                    		elem = treeNode.getElement();
                    		break;
                    	}
                    }

                    final Element theTarget = elem;
                    
                    if (true) { //elem.getAttribute("chime-hover").equals("true")) {
                        _hovering = href.substring(idx + 1);
                        HoverTimer t = new HoverTimer(_hovering) {
                            @Override
                            public void run() {
                                if (_hovering.equals(getToken())) {
                                	if (GXT.isSafari) {
                                    	int x = 5 + theTarget.getOffsetLeft();
                                        int y = theTarget.getAbsoluteTop() + theTarget.getOffsetHeight() - 6;
                                        DetailPopupManager.instance().show(_hovering, x, y);
                                	} else {
                                        DetailPopupManager.instance().show(_hovering, theTarget);
                                	}
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
