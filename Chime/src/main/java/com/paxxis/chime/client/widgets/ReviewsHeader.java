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
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.paxxis.chime.client.ChimeAsyncCallback;
import com.paxxis.chime.client.SearchFilterModifyListener;
import com.paxxis.chime.client.SearchFilterPanel;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceResponseObject;
import com.paxxis.chime.client.common.ApplyReviewRequest;
import com.paxxis.chime.client.common.ApplyReviewResponse;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.Review;
import com.paxxis.chime.client.common.SearchFilter;
import com.paxxis.chime.client.common.Utils;
import com.paxxis.chime.client.common.DataInstanceRequest.SortOrder;
import com.paxxis.chime.client.editor.ReviewEditorWindow;
import com.paxxis.chime.client.portal.UpdateReason;
import com.paxxis.chime.client.widgets.SocialFilterPopup.SocialFilterListener;

/**
 *
 * @author Robert Englander
 */
public class ReviewsHeader extends LayoutContainer
{
    public interface ReviewsChangedListener {
        public void onReviewsChanged(DataInstance instance);
        public void onFilterChange(SearchFilter filter);
        public void onSortChange(SortOrder order);
        public void onRefresh();
    }
    
    private static final String STARS = "<img src='resources/images/chime/stars";
    private static final String STARSEND = ".gif' width='55' height='13'/>";

    private static final String TEMPLATE = "<div id='reviews-header'>"  +
        "{rating}&nbsp;&nbsp;&nbsp;{heading}</div>";
    
    private static Template _template;

    static
    {
        _template = new Template(TEMPLATE);
        _template.compile();
    }

    private DataInstance _instance;
    private InterceptedHtml _html;
    private Button _reviewButton;
    private Button _filterButton;
    private LayoutContainer _filterContainer;
    private SearchFilter _filter = null;
    private ReviewsChangedListener _reviewsChangedListener;

    private Button sortButton;
    private CheckMenuItem sortByRecentItem;
    private CheckMenuItem sortByRatingItem;
    private CheckMenuItem sortByHelpfulItem;
    
    public ReviewsHeader(ReviewsChangedListener reviewsListener) {
        _reviewsChangedListener = reviewsListener;
        init();
    }
    
    public void sendRequest(ApplyReviewRequest request)
    {
        final ChimeAsyncCallback<ServiceResponseObject<ApplyReviewResponse>> callback = 
        					new ChimeAsyncCallback<ServiceResponseObject<ApplyReviewResponse>>() {
            public void onSuccess(ServiceResponseObject<ApplyReviewResponse> response) {
                if (response.isResponse()) {
                    _reviewsChangedListener.onReviewsChanged(response.getResponse().getDataInstance());
                    sortByRecentItem.setChecked(true);
                    sortButton.setText("Sort by Most Recent");
                } else {
                    ChimeMessageBox.alert("Error", response.getError().getMessage(), null);
                }
            }
        };

        request.setUser(ServiceManager.getActiveUser());
        ServiceManager.getService().sendApplyRatingRequest(request, callback);
    }

    private void init()
    {
        setLayout(new RowLayout());
        setBorders(false);
        setStyleAttribute("backgroundColor", "white");
        
        final ToolBar bar = new ToolBar();
        _reviewButton = new Button("Write a review...");
        bar.add(_reviewButton);
        
        final ReviewEditorWindow.ReviewEditorListener listener = 
            new ReviewEditorWindow.ReviewEditorListener() 
            {
                public void onComplete(Review review)
                {
                    ApplyReviewRequest req = new ApplyReviewRequest();
                    req.setData(_instance);
                    req.setReview(review);
              
                    sendRequest(req);
                }
            };
        
        _reviewButton.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent evt) 
                {
                    Runnable r = new Runnable() {
                        public void run() {
                            ReviewEditorWindow w = new ReviewEditorWindow(_instance.getSocialContext().getUserReview(), listener);
                            w.show();
                        }
                    };

                    if (ServiceManager.isLoggedIn()) {
                        r.run();
                    } else {
                        LoginWindow w = new LoginWindow("You must be logged in to write a review.", r, null);
                        w.show();
                    }
                }
            }
        );
        
        sortButton = new Button("Sort by Most Recent");

        Menu menu = new Menu();

        sortByRecentItem = new CheckMenuItem("Sort by Most Recent");
        sortByRecentItem.setGroup("SortStyleGroup");
        sortByRecentItem.setChecked(true);
        sortByRecentItem.addSelectionListener(
            new SelectionListener<MenuEvent>()
            {
                @Override
                public void componentSelected(MenuEvent evt)
                {
                    sortButton.setText("Sort by Most Recent");
                    _reviewsChangedListener.onSortChange(SortOrder.ByMostRecentCreate);
                }
            }
        );
        menu.add(sortByRecentItem);

        sortByRatingItem = new CheckMenuItem("Sort By Highest Rating");
        sortByRatingItem.setGroup("SortStyleGroup");
        sortByRatingItem.setChecked(false);
        sortByRatingItem.addSelectionListener(
            new SelectionListener<MenuEvent>()
            {
                @Override
                public void componentSelected(MenuEvent evt)
                {
                    sortButton.setText("Sort By Highest Rating");
                    _reviewsChangedListener.onSortChange(SortOrder.ByRating);
                }
            }
        );
        menu.add(sortByRatingItem);

        sortByHelpfulItem = new CheckMenuItem("Sort By Most Helpful");
        sortByHelpfulItem.setGroup("SortStyleGroup");
        sortByHelpfulItem.setChecked(false);
        sortByHelpfulItem.addSelectionListener(
            new SelectionListener<MenuEvent>()
            {
                @Override
                public void componentSelected(MenuEvent evt)
                {
                    sortButton.setText("Sort By Most Helpful");
                    _reviewsChangedListener.onSortChange(SortOrder.ByMostUseful);
                }
            }
        );
        menu.add(sortByHelpfulItem);

        sortButton.setMenu(menu);
        bar.add(sortButton);

        final SocialFilterListener filterListener = new SocialFilterListener() {

            public void onApply(SearchFilter filter) {
                SearchFilterPanel p = new SearchFilterPanel(filter,
                    new SearchFilterModifyListener() {

                        public void onDeleteRequest(SearchFilterPanel panel) {
                            _filterContainer.removeAll();
                            _filter = null;
                            _reviewsChangedListener.onFilterChange(_filter);
                        }

                        public void onEnableRequest(SearchFilterPanel panel, boolean enable) {
                            _filter = panel.getFilter();
                            _filter.setEnabled(enable);
                            panel.updateButtonState();
                            _reviewsChangedListener.onFilterChange(_filter);
                        }

                    }
                );

                _filter = filter;
                _filterContainer.removeAll();

                _filterContainer.add(p, new RowData(1, -1));
                _filterContainer.layout();
                _reviewsChangedListener.onFilterChange(_filter);
            }

        };

        _filterButton = new Button("Set Filter...");
        _filterButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                    Runnable r = new Runnable() {
                        public void run() {
                            SocialFilterPopup popup = new SocialFilterPopup(filterListener);
                            int x = _filterButton.getAbsoluteLeft();
                            int y = 1 + _filterButton.getAbsoluteTop() + _filterButton.getOffsetHeight();
                            popup.setPosition(x, y);
                            popup.show(); //At(x, y);
                        }
                    };

                    if (ServiceManager.isLoggedIn()) {
                        r.run();
                    } else {
                        LoginWindow w = new LoginWindow("You must be logged in to set a review filter.", r, null);
                        w.show();
                    }
                }
            }
        );

        // TODO the social filtering is buggy, so let's not expose it for now
        //bar.add(_filterButton);

        bar.add(new FillToolItem());

        add(bar, new RowData(1, -1, new Margins(0, 0, 5, 0)));
        _html = new InterceptedHtml();
        add(_html, new RowData(1, -1, new Margins(5)));
        
        _filterContainer = new LayoutContainer();
        _filterContainer.setLayout(new RowLayout());
        add(_filterContainer, new RowData(1, -1, new Margins(5)));

        addListener(Events.Resize,
            new Listener<BoxComponentEvent>() {

                public void handleEvent(BoxComponentEvent evt) {
                    layout(true);
                }

            }
        );
    }
    
    public void setDataInstance(DataInstance instance, UpdateReason reason)
    {
        if (!ServiceManager.isLoggedIn()) {
            _filterContainer.removeAll();
            _filter = null;
        }
        
        _instance = instance;
        update();
    }

    public void update()
    {
        Params params = new Params();
        params.set("rating", getRating(_instance.getAverageRating()));
        params.set("heading", getHeading(_instance));
        
        String content = _template.applyTemplate(params);
        _html.setHtml(content);

        if (_instance != null)
        {
            if (_instance.getSocialContext().getUserReview() == null ||
                    ServiceManager.getActiveUser() == null)
            {
                _reviewButton.setText("Write a review...");
            }
            else
            {
                _reviewButton.setText("Edit your review...");
            }
        }
    }

    private static String getHeading(DataInstance instance)
    {
        StringBuffer buf = new StringBuffer();

        int count = instance.getRatingCount();
        float avg = Utils.round(instance.getAverageRating(), 2);
        
        String stars;
        if (avg == 1.0f)
        {
            stars = "1 Star";
        }
        else
        {
            stars = avg + " Stars";
        }
        
        if (count == 0)
        {
            buf.append("No Reviews");
        }
        else if (count == 1)
        {
            buf.append("1 Review with rating of " + stars);
        }
        else
        {
            buf.append(count + " Reviews with average rating of " + stars);
        }
        
        return buf.toString();
    }

    private static String getRating(float avg)
    {
        int integral = (int)avg;
        double fraction = avg - integral;
        
        String stars = "-" + integral;
        
        if (integral > 0)
        {
            if (fraction > 0.0 && fraction < 0.5)
            {
                stars += "-Q";
            }
            else if (fraction == 0.5)
            {
                stars += "-H";
            }
            else if (fraction > 0.5)
            {
                stars += "-T";
            }
        }
        
        return (STARS + stars + STARSEND);
        
    }
}
