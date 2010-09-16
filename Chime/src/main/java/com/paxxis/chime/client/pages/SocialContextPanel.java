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

package com.paxxis.chime.client.pages;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.TabPanel.TabPosition;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.paxxis.chime.client.InstanceUpdateListener;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataSocialContext;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.portal.UpdateReason;
import com.paxxis.chime.client.widgets.CommentsPanel;
import com.paxxis.chime.client.widgets.DiscussionsPanel;
import com.paxxis.chime.client.widgets.ReviewsPanel;
import com.paxxis.chime.client.widgets.TagsPanel;

/**
 *
 * @author Robert Englander
 */
public class SocialContextPanel extends LayoutContainer 
{
    private enum Type
    {
        Reviews,
        Comments,
        Discussions,
        Tags,
        //Polls
    }
    
    private DataInstance _instance = null;
    private ReviewsPanel _reviewsPanel = null;
    private CommentsPanel _commentsPanel = null;
    private TagsPanel _tagsPanel = null;
    //private LayoutContainer _pollsPanel = null;
    private DiscussionsPanel _discussionsPanel = null;
    private ContentPanel _mainPanel = null;
    
    private TabPanel _section1TabPanel;
    private TabPanel _section2TabPanel;
    private Orientation _orientation;

    private TabItem reviewItem;
    private TabItem commentItem;
    private TabItem tagItem;
    //private TabItem pollItem;
    private TabItem discussionItem;

    private boolean showComments = true;
    private InstanceUpdateListener updateListener = null;

    public SocialContextPanel(InstanceUpdateListener listener) {
        this(Orientation.HORIZONTAL, listener);
    }

    public SocialContextPanel(Orientation orientation, InstanceUpdateListener listener)
    {
        _orientation = orientation;
        init(Type.Reviews, listener);
    }

    public void setShowComments(boolean show) {
        showComments = show;
    }
    
    public void reset()
    {
        Type type = Type.Reviews;
        if (_mainPanel != null)
        {
            Component c = _section1TabPanel.getSelectedItem().getItem(0);
            if (c == _reviewsPanel)
            {
                type = Type.Reviews;
            }
            else if (c == _commentsPanel)
            {
                type = Type.Comments;
            }
            else if (c == _discussionsPanel)
            {
                type = Type.Discussions;
            }
            else if (c == _tagsPanel)
            {
                type = Type.Tags;
            }
            else
            {
                //type = Type.Polls;
            }
            
            remove(_mainPanel);
        }
        
        init(type, updateListener);
        setDataInstance(_instance, UpdateReason.InstanceChange);
    }
    
    public void setDataInstance(DataInstance instance, UpdateReason reason)
    {
        _instance = instance;
        _reviewsPanel.setDataInstance(_instance, reason);
        _commentsPanel.setDataInstance(_instance, reason);
        _discussionsPanel.setDataInstance(_instance, reason);
        _tagsPanel.setDataInstance(_instance, reason);

        if (reason == UpdateReason.InstanceChange) {
            _section1TabPanel.removeAll();

            Shape type = instance.getShapes().get(0);

            if (type.getCanReview()) {
                _section1TabPanel.add(reviewItem);
            }

            if (type.getCanComment() && showComments) {
                _section1TabPanel.add(commentItem);
            }

            if (type.getCanDiscuss()) {
                _section1TabPanel.add(discussionItem);
            }

            if (type.getCanPoll()) {
                //_section1TabPanel.add(pollItem);
            }
        }

        /*
        _section2TabPanel.removeAll();
        if (instance.getTypes().getCanTag()) {
            _section2TabPanel.add(tagItem);
        }
        */
    }
    
    public String getSummary()
    {
        StringBuffer buf = new StringBuffer();
        
        DataSocialContext context = _instance.getSocialContext();
        int count = context.getRatingCount();
        float avg = context.getAverageRating();
        
        String spacer = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
        
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
            buf.append(spacer + "[No Reviews]  ");
        }
        else if (count == 1)
        {
            buf.append(spacer + "[1 Review - rating of " + stars + "]");
        }
        else
        {
            buf.append(spacer + "[" + count + " Reviews - average rating of " + stars + "]");
        }
        
        count = context.getCommentsBundle().getCursor().getTotal();
        if (count == 0)
        {
            buf.append(spacer + "[No Comments]");
        }
        else if (count == 1)
        {
            buf.append(spacer + "[1 Comment]");
        }
        else
        {
            buf.append(spacer + "[" + count + " Comments]");
        }
        
        count = context.getTagContexts().size();
        if (count == 0)
        {
            buf.append(spacer + "[No Tags Applied]");
        }
        else if (count == 1)
        {
            buf.append(spacer + "[1 Tag Applied]");
        }
        else
        {
            buf.append(spacer + "[" + count + " Tags Applied]");
        }

        return buf.toString();
    }
    
    private void init(Type initialPanel, InstanceUpdateListener listener)
    {
        updateListener = listener;
        setLayout(new FitLayout()); //new BorderLayout());
        setBorders(false);
        
        _section1TabPanel = new TabPanel();
        //_section1TabPanel.setPlain(true);
        _section1TabPanel.setTabScroll(true);
        _section1TabPanel.setBorders(true);
        _section1TabPanel.setBodyBorder(false);
        _section1TabPanel.setTabPosition(TabPosition.TOP);
        
        _section2TabPanel = new TabPanel();
        //_section2TabPanel.setPlain(true);
        _section2TabPanel.setTabScroll(true);
        _section2TabPanel.setBorders(true);
        _section2TabPanel.setBodyBorder(false);
        _section2TabPanel.setTabPosition(TabPosition.TOP);

        reviewItem = new TabItem();
        reviewItem.setLayout(new FitLayout());
        reviewItem.setText("Reviews");
        reviewItem.setIconStyle("review-icon");
        reviewItem.setClosable(false);
        _reviewsPanel = new ReviewsPanel(listener);
        reviewItem.add(_reviewsPanel);
        
        _section1TabPanel.add(reviewItem);
        if (initialPanel == Type.Reviews)
        {
            //_section1TabPanel.setSelection(reviewItem);
        }
        
        commentItem = new TabItem();
        commentItem.setLayout(new FitLayout());
        commentItem.setText("Comments");
        commentItem.setIconStyle("comment-icon");
        commentItem.setClosable(false);
        _commentsPanel = new CommentsPanel(listener);
        commentItem.add(_commentsPanel);
        
        _section1TabPanel.add(commentItem);
        if (initialPanel == Type.Comments)
        {
            //_section1TabPanel.setSelection(commentItem);
        }

        discussionItem = new TabItem();
        discussionItem.setLayout(new FitLayout());
        discussionItem.setText("Discussions");
        discussionItem.setIconStyle("discussion-icon");
        discussionItem.setClosable(false);
        _discussionsPanel = new DiscussionsPanel(listener);
        discussionItem.add(_discussionsPanel);

        _section1TabPanel.add(discussionItem);

        //pollItem = new TabItem();
        //pollItem.setLayout(new FitLayout());
        //pollItem.setText("Polls");
        //pollItem.setIconStyle("poll-icon");
        //pollItem.setClosable(false);
        //_pollsPanel = new LayoutContainer();
        //pollItem.add(_pollsPanel);

        //_section1TabPanel.add(pollItem);

        tagItem = new TabItem();
        tagItem.setLayout(new FitLayout());
        tagItem.setText("Tags");
        tagItem.setIconStyle("tag-icon");
        tagItem.setClosable(false);
        _tagsPanel = new TagsPanel(listener);
        tagItem.add(_tagsPanel);
        
        _section2TabPanel.add(tagItem);
        if (initialPanel == Type.Tags)
        {
            //_section2TabPanel.setSelection(tagItem);
        }
        
        _mainPanel = new ContentPanel();
        _mainPanel.setHeaderVisible(false);
        _mainPanel.setBorders(false);
        _mainPanel.setLayout(new BorderLayout());

        BorderLayoutData data = new BorderLayoutData(LayoutRegion.CENTER);
        data.setMinSize(100);
        data.setMaxSize(1200);
        data.setMargins(new Margins(0, 2, 0, 0));
        data.setCollapsible(true);
        data.setSplit(true);
        _mainPanel.add(_section1TabPanel, data);

        if (_orientation == Orientation.HORIZONTAL) {
            data = new BorderLayoutData(LayoutRegion.EAST, 0.4f);
        } else {
            data = new BorderLayoutData(LayoutRegion.SOUTH, 0.4f);
        }
        data.setMinSize(100);
        data.setMaxSize(1200);
        data.setMargins(new Margins(0, 0, 0, 2));
        data.setCollapsible(true);
        data.setSplit(true);
        _mainPanel.add(_section2TabPanel, data);

        add(_mainPanel);
    }
}
