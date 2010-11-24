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

import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.paxxis.chime.client.ChimeAsyncCallback;
import com.paxxis.chime.client.DiscussionsResponseObject;
import com.paxxis.chime.client.InstanceUpdateListener;
import com.paxxis.chime.client.Paginator;
import com.paxxis.chime.client.PaginatorContainer;
import com.paxxis.chime.client.PagingListener;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataSocialContext;
import com.paxxis.chime.client.common.Discussion;
import com.paxxis.chime.client.common.DiscussionsBundle;
import com.paxxis.chime.client.common.DiscussionsRequest;
import com.paxxis.chime.client.portal.UpdateReason;
import com.paxxis.chime.client.widgets.DiscussionsHeader.DiscussionsChangedListener;
import com.paxxis.cornerstone.base.Cursor;

/**
 *
 * @author Robert Englander
 */
public class DiscussionsPanel extends ChimeLayoutContainer implements PagingListener
{
    private DataInstance _instance = null;
    private Paginator _paginator;
    private DiscussionsHeader _header;
    private LayoutContainer _resultsList;
    private ChimeAsyncCallback<DiscussionsResponseObject> callback;
    private InstanceUpdateListener updateListener;

    public DiscussionsPanel(InstanceUpdateListener listener) {
    	super();
    	updateListener = listener;
    }

    protected void init()
    {
        setLayout(new RowLayout());
        setStyleAttribute("backgroundColor", "white");

        _resultsList = new LayoutContainer();
        _resultsList.setBorders(false);
        _resultsList.setStyleName("x-combo-list");
        _resultsList.setStyleAttribute("backgroundColor", "white");

        ContentPanel p = new ContentPanel();
        p.setScrollMode(Scroll.AUTO);
        p.setHeaderVisible(false);
        p.setBorders(false);
        p.setBodyBorder(false);
        p.add(_resultsList);

        _header = new DiscussionsHeader(
            new DiscussionsChangedListener() {
                public void onDiscussionsChanged(DataInstance instance) {
                    setDataInstance(instance, UpdateReason.InstanceChange);
                    if (updateListener != null) {
                        updateListener.onUpdate(instance, InstanceUpdateListener.Type.DiscussionApplied);
                    }
                }
            }
        );

        add(_header, new RowData(1, -1));
        add(p, new RowData(1, 1));

        _paginator = new Paginator(this);
        PaginatorContainer south = new PaginatorContainer(_paginator);
        add(south, new RowData(1, -1));

        callback = new ChimeAsyncCallback<DiscussionsResponseObject>() {
            public void onSuccess(DiscussionsResponseObject response) {
                if (response.isResponse()) {
                    update(response.getResponse().getDiscussions(), response.getResponse().getCursor());
                }
            }
        };
    }

    public void setDataInstance(final DataInstance instance, final UpdateReason reason)
    {
    	Runnable r = new Runnable() {
    		public void run() {
    	        _instance = instance;
    	        _header.setDataInstance(instance, reason);
    	        update(reason);
    		}
    	};
    	
    	if (isRendered()) {
    		r.run();
    	} else {
    		addPostRenderRunnable(r);
    	}
    }

    public void update(UpdateReason reason)
    {
        if (reason == UpdateReason.InstanceChange) {
            _resultsList.removeAll();

            if (_instance != null)
            {
                DataSocialContext context = _instance.getSocialContext();
                if (context != null)
                {
                    DiscussionsBundle bundle = context.getDiscussionsBundle();
                    List<Discussion> discussions = bundle.getDiscussions();
                    for (Discussion discussion : discussions)
                    {
                        DiscussionPanel panel = new DiscussionPanel(discussion);
                        _resultsList.add(panel, new FlowData(new Margins(2, 2, 0, 2)));
                    }

                    _paginator.setCursor(bundle.getCursor());
                }
            }

            _resultsList.layout();
        }
    }

    public void update(List<Discussion> discussions, Cursor cursor)
    {
        _resultsList.removeAll();
        boolean collapsed = discussions.size() > 1;
        for (Discussion discussion : discussions)
        {
            DiscussionPanel panel = new DiscussionPanel(discussion);
            _resultsList.add(panel);
        }

        _paginator.setCursor(cursor);

        _resultsList.layout();
    }

    private void query(Cursor cursor)
    {
        DiscussionsRequest req = new DiscussionsRequest();
        req.setCursor(cursor);
        req.setDataInstance(_instance);

        ServiceManager.getService().sendDiscussionsRequest(req, callback);
    }

    public void onNext()
    {
        Cursor cursor = _paginator.getCursor();
        cursor.prepareNext();
        query(cursor);
    }

    public void onPrevious()
    {
        Cursor cursor = _paginator.getCursor();
        cursor.preparePrevious();
        query(cursor);
    }

    public void onFirst()
    {
        Cursor cursor = _paginator.getCursor();
        cursor.prepareFirst();
        query(cursor);
    }

    public void onLast()
    {
        Cursor cursor = _paginator.getCursor();
        cursor.prepareLast();
        query(cursor);
    }

    public void onRefresh()
    {
    }

    public void onChildCollapseChange()
    {
        _resultsList.layout();
    }
}
