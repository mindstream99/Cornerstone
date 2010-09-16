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
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.paxxis.chime.client.InstanceUpdateListener;
import com.paxxis.chime.client.Paginator;
import com.paxxis.chime.client.PaginatorContainer;
import com.paxxis.chime.client.PagingListener;
import com.paxxis.chime.client.RatingsResponseObject;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.common.Cursor;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataSocialContext;
import com.paxxis.chime.client.common.Review;
import com.paxxis.chime.client.common.ReviewsBundle;
import com.paxxis.chime.client.common.ReviewsRequest;
import com.paxxis.chime.client.common.SearchFilter;
import com.paxxis.chime.client.common.DataInstanceRequest.SortOrder;
import com.paxxis.chime.client.portal.UpdateReason;
import com.paxxis.chime.client.widgets.ReviewPanel.VoteChangeListener;
import com.paxxis.chime.client.widgets.ReviewsHeader.ReviewsChangedListener;

/**
 *
 * @author Robert Englander
 */
public class ReviewsPanel extends ChimeLayoutContainer implements PagingListener, VoteChangeListener
{
    private DataInstance _instance = null;
    private Paginator _paginator;
    private ReviewsHeader _header;
    private LayoutContainer _resultsList;
    private AsyncCallback callback;
    private SearchFilter _filter = null;
    private SortOrder sortOrder = SortOrder.ByMostRecentEdit;
    private InstanceUpdateListener updateListener;
    
    public ReviewsPanel(InstanceUpdateListener updater) {
    	super();
    	updateListener = updater;
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

        _header = new ReviewsHeader(
            new ReviewsChangedListener() {
                public void onReviewsChanged(DataInstance instance) {
                    setDataInstance(instance, UpdateReason.InstanceChange);

                    if (updateListener != null) {
                        updateListener.onUpdate(instance, InstanceUpdateListener.Type.ReviewApplied);
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
                    query(new Cursor(_paginator.getCursor().getMax()));
                    layout(true);
                }
            }
        );

        add(_header, new RowData(1, -1));
        add(p, new RowData(1, 1));
                
        _paginator = new Paginator(this);
        PaginatorContainer south = new PaginatorContainer(_paginator);

        add(south, new RowData(1, -1));
        
        callback = new AsyncCallback()
        {
            public void onFailure(Throwable arg0) 
            {
            }

            public void onSuccess(Object obj) 
            {
                RatingsResponseObject response = (RatingsResponseObject)obj;
                if (response.isResponse())
                {
                    update(response.getResponse().getRatings(), response.getResponse().getCursor());
                }
                else
                { 
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
                    ReviewsBundle bundle = context.getReviewsBundle();
                    List<Review> reviews = bundle.getReviews();
                    for (Review review : reviews)
                    {
                        ReviewPanel panel = new ReviewPanel(review, this);
                        _resultsList.add(panel, new FlowData(new Margins(2, 2, 0, 2)));
                    }

                    _paginator.setCursor(bundle.getCursor());
                }
            }

            layout();
        }
    }

    public void update(List<Review> reviews, Cursor cursor)
    {
        _resultsList.removeAll();
        for (Review review : reviews)
        {
            ReviewPanel panel = new ReviewPanel(review, this);
            _resultsList.add(panel);
        }

        _paginator.setCursor(cursor);

        _resultsList.layout();
    }

    private void query(Cursor cursor)
    {
        ReviewsRequest req = new ReviewsRequest();
        req.setCursor(cursor);
        req.setDataInstance(_instance);
        req.setUser(ServiceManager.getActiveUser());
        req.setFilter(_filter);
        req.setSortOrder(sortOrder);
        
        ServiceManager.getService().sendRatingsRequest(req, callback);
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

    public void onVoteChange() {
        if (sortOrder == SortOrder.ByMostUseful) {
            Listener<ChimeMessageBoxEvent> l = new Listener<ChimeMessageBoxEvent>() {
                public void handleEvent(ChimeMessageBoxEvent evt) {
                    Button btn = evt.getButtonClicked();
                    if (btn != null) {
                        if (btn.getText().equalsIgnoreCase("yes")) {
                            query(new Cursor(_paginator.getCursor().getMax()));
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
