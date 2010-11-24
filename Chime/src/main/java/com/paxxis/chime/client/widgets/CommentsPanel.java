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
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.paxxis.chime.client.ChimeAsyncCallback;
import com.paxxis.chime.client.CommentsResponseObject;
import com.paxxis.chime.client.InstanceUpdateListener;
import com.paxxis.chime.client.Paginator;
import com.paxxis.chime.client.PaginatorContainer;
import com.paxxis.chime.client.PagingListener;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.common.Comment;
import com.paxxis.chime.client.common.CommentsBundle;
import com.paxxis.chime.client.common.CommentsRequest;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataSocialContext;
import com.paxxis.chime.client.common.SearchFilter;
import com.paxxis.chime.client.common.DataInstanceRequest.SortOrder;
import com.paxxis.chime.client.portal.UpdateReason;
import com.paxxis.chime.client.widgets.CommentPanel.VoteChangeListener;
import com.paxxis.chime.client.widgets.CommentsHeader.CommentsChangedListener;
import com.paxxis.cornerstone.base.Cursor;

/**
 *
 * @author Robert Englander
 */
public class CommentsPanel extends ChimeLayoutContainer implements PagingListener, VoteChangeListener
{
    public enum Type {
        Comment,
        Post
    }
    private DataInstance _instance = null;
    private Paginator _paginator;
    private LayoutContainer _resultsList;
    private CommentsHeader _header;
    private PaginatorContainer _south;
    private ChimeAsyncCallback<CommentsResponseObject> pagedCallback;
    private SearchFilter _filter = null;
    private Type type;
    private SortOrder sortOrder = SortOrder.ByMostRecentEdit;
    private InstanceUpdateListener updateListener;

    public CommentsPanel(InstanceUpdateListener listener) {
        this(Type.Comment, listener);
    }

    public CommentsPanel(Type type, InstanceUpdateListener listener) {
        this.type = type;
        updateListener = listener;
    }

    protected void init()
    {
        setLayout(new RowLayout());
        setStyleAttribute("backgroundColor", "white");

        _header = new CommentsHeader(type,
            new CommentsChangedListener() {
                public void onCommentsChanged(DataInstance instance) {
                    setDataInstance(instance, UpdateReason.InstanceChange);

                    if (updateListener != null) {
                        updateListener.onUpdate(instance, InstanceUpdateListener.Type.CommentApplied);
                    }
                }

                public void onFilterChange(SearchFilter filter) {
                    _filter = filter;
                    onRefresh();
                }

                public void onSortChange(SortOrder order) {
                    sortOrder = order;
                    onRefresh();
                }

                public void onRefresh() {
                    query(new Cursor(_paginator.getCursor().getMax()), pagedCallback);
                    layout(true);
                }
            }
        );

        add(_header, new RowData(1, -1));

        _resultsList = new LayoutContainer();
        _resultsList.setBorders(false);
        _resultsList.setStyleName("x-combo-list");
        _resultsList.setStyleAttribute("backgroundColor", "white");
        _resultsList.setScrollMode(Scroll.AUTO);
    
        ContentPanel p = new ContentPanel();
        p.setScrollMode(Scroll.AUTO);
        p.setHeaderVisible(false);
        p.setBorders(false);
        p.setBodyBorder(false);
        p.add(_resultsList);

        add(p, new RowData(1, 1));

        _paginator = new Paginator(this);
        _south = new PaginatorContainer(_paginator);

        add(_south, new RowData(1, -1));
        
        pagedCallback = new ChimeAsyncCallback<CommentsResponseObject>() {
            public void onSuccess(CommentsResponseObject response) { 
                if (response.isResponse()) {
                    update(response.getResponse().getComments(), response.getResponse().getCursor());
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
        if (reason == UpdateReason.InstanceChange || reason == UpdateReason.LoginLogout) {
            _resultsList.removeAll();

            if (_instance != null)
            {
                DataSocialContext context = _instance.getSocialContext();
                if (context != null)
                {
                    CommentsBundle bundle = context.getCommentsBundle();
                    List<Comment> comments = bundle.getComments();
                    for (Comment comment : comments)
                    {
                        CommentPanel panel = new CommentPanel(comment, this);
                        _resultsList.add(panel);
                    }

                    _paginator.setCursor(bundle.getCursor());
                }
            }

            layout();
        }
    }

    public void update(List<Comment> comments, Cursor cursor)
    {
        _resultsList.removeAll();
        for (Comment comment : comments)
        {
            CommentPanel panel = new CommentPanel(comment, this);
            _resultsList.add(panel);
        }

        _paginator.setCursor(cursor);

        _resultsList.layout();
    }

    private void query(Cursor cursor, AsyncCallback cb)
    {
        CommentsRequest req = new CommentsRequest();
        req.setCursor(cursor);
        req.setDataInstance(_instance);
        req.setUser(ServiceManager.getActiveUser());
        req.setFilter(_filter);
        req.setSortOrder(sortOrder);
        
        ServiceManager.getService().sendCommentsRequest(req, cb);
    }
    
    public void onNext() 
    {
        Cursor cursor = _paginator.getCursor();
        cursor.prepareNext();
        query(cursor, pagedCallback);
    }

    public void onPrevious() 
    {
        Cursor cursor = _paginator.getCursor();
        cursor.preparePrevious();
        query(cursor, pagedCallback);
    }

    public void onFirst() 
    {
        Cursor cursor = _paginator.getCursor();
        cursor.prepareFirst();
        query(cursor, pagedCallback);
    }

    public void onLast() 
    {
        Cursor cursor = _paginator.getCursor();
        cursor.prepareLast();
        query(cursor, pagedCallback);
    }

    public void onRefresh() 
    {
    }

    public void onVoteChange() {
        if (sortOrder == SortOrder.ByMostUseful) {
            Listener<ChimeMessageBoxEvent> l = new Listener<ChimeMessageBoxEvent>() {
                public void handleEvent(ChimeMessageBoxEvent evt) {
                    Button btn = evt.getButtonClicked();
                    if (btn != null) {
                        if (btn.getText().equalsIgnoreCase("yes")) {
                            query(new Cursor(_paginator.getCursor().getMax()), pagedCallback);
                            layout();
                        }
                    }
                }
            };

            String promptText = "Your vote may impact the sort order.<br><br>Would you like to refresh the list?";
            ChimeMessageBox.confirm("Apply Vote", promptText, l);
        }
    }
}
