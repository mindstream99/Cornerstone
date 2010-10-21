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
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.paxxis.chime.client.ChimeAsyncCallback;
import com.paxxis.chime.client.SearchFilterModifyListener;
import com.paxxis.chime.client.SearchFilterPanel;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceResponseObject;
import com.paxxis.chime.client.common.AddCommentRequest;
import com.paxxis.chime.client.common.AddCommentResponse;
import com.paxxis.chime.client.common.Comment;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.SearchFilter;
import com.paxxis.chime.client.common.DataInstanceRequest.SortOrder;
import com.paxxis.chime.client.portal.UpdateReason;
import com.paxxis.chime.client.widgets.CommentsPanel.Type;
import com.paxxis.chime.client.widgets.SocialFilterPopup.SocialFilterListener;

/**
 *
 * @author Robert Englander
 */
public class CommentsHeader extends LayoutContainer {
    private static final String ADDBUTTON_COMMENT = "Add a comment...";
    private static final String ADDBUTTON_POST = "Add a post...";
    private static final String LOGINMSG_COMMENT = "You must be logged in to add a comment.";
    private static final String LOGINMSG_POST = "You must be logged in to add a post.";
    private static final String TYPENAME_COMMENT = "Comment";
    private static final String TYPENAME_COMMENTPLURAL = "Comments";
    private static final String TYPENAME_POST = "Post";
    private static final String TYPENAME_POSTPLURAL = "Posts";

    public interface CommentsChangedListener {
        public void onCommentsChanged(DataInstance instance);
        public void onFilterChange(SearchFilter filter);
        public void onSortChange(SortOrder order);
        public void onRefresh();
    }

    private static final String TEMPLATE = "<div id='comments-header'>"  +
        "{heading}</div>";
    
    private static Template _template;

    static
    {
        _template = new Template(TEMPLATE);
        _template.compile();
    }

    private DataInstance _instance;
    private InterceptedHtml _html;
    private Button _commentButton;
    private Button _filterButton;
    private LayoutContainer _filterContainer = new LayoutContainer();
    private SearchFilter _filter = null;
    private CommentsChangedListener _commentsChangedListener;
    private CommentsPanel.Type _type;
    private String addButtonText;
    private String loginMsgText;
    private String typeName;
    private String typeNamePlural;

    private Button sortButton;
    private CheckMenuItem sortByRecentItem;
    private CheckMenuItem sortByHelpfulItem;

    public CommentsHeader(CommentsPanel.Type type, CommentsChangedListener commentsListener) {
        if (type == Type.Comment) {
            addButtonText = ADDBUTTON_COMMENT;
            loginMsgText = LOGINMSG_COMMENT;
            typeName = TYPENAME_COMMENT;
            typeNamePlural = TYPENAME_COMMENTPLURAL;
        } else {
            addButtonText = ADDBUTTON_POST;
            loginMsgText = LOGINMSG_POST;
            typeName = TYPENAME_POST;
            typeNamePlural = TYPENAME_POSTPLURAL;
        }

        _commentsChangedListener = commentsListener;
        init();
    }
    
    public void sendRequest(AddCommentRequest request)
    {
        final ChimeAsyncCallback<ServiceResponseObject<AddCommentResponse>> callback = 
        		new ChimeAsyncCallback<ServiceResponseObject<AddCommentResponse>>() {
            public void onSuccess(ServiceResponseObject<AddCommentResponse> response) {
                if (response.isResponse()) {
                    _commentsChangedListener.onCommentsChanged(response.getResponse().getDataInstance());
                } else {
                    ChimeMessageBox.alert("Error", response.getError().getMessage(), null);
                }
            }
        };

        request.setUser(ServiceManager.getActiveUser());
        ServiceManager.getService().sendAddCommentRequest(request, callback);
    }

    private void init()
    {
        setLayout(new RowLayout());
        setBorders(false);
        setStyleAttribute("backgroundColor", "white");
        
        final ToolBar bar = new ToolBar();
        _commentButton = new Button(addButtonText);

        bar.add(_commentButton);
        
        final CommentEditorWindow.CommentEditorListener listener = 
            new CommentEditorWindow.CommentEditorListener() 
            {
                public void onComplete(Comment comment) 
                {
                    AddCommentRequest req = new AddCommentRequest();
                    req.setData(_instance);
                    req.setComment(comment);
              
                    sendRequest(req);
                }
            };
        
        _commentButton.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent evt) 
                {
                    Runnable r = new Runnable() {
                        public void run() {
                            CommentEditorWindow w = new CommentEditorWindow(typeName, listener);
                            w.show();
                        }
                    };

                    if (ServiceManager.isLoggedIn()) {
                        r.run();
                    } else {
                        LoginWindow w = new LoginWindow(loginMsgText, r, null);
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
                    _commentsChangedListener.onSortChange(SortOrder.ByMostRecentCreate);
                }
            }
        );
        menu.add(sortByRecentItem);

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
                    _commentsChangedListener.onSortChange(SortOrder.ByMostUseful);
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
                            _commentsChangedListener.onFilterChange(_filter);
                        }

                        public void onEnableRequest(SearchFilterPanel panel, boolean enable) {
                            _filter = panel.getFilter();
                            _filter.setEnabled(enable);
                            panel.updateButtonState();
                            _commentsChangedListener.onFilterChange(_filter);
                        }

                    }
                );

                _filter = filter;
                _filterContainer.removeAll();

                _filterContainer.add(p);
                _filterContainer.layout();
                
                DeferredCommand.addCommand(
                	new Command() {
                		public void execute() {
                            _commentsChangedListener.onFilterChange(_filter);
                		}
                	}
                );
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
                        LoginWindow w = new LoginWindow("You must be logged in to set a filter.", r, null);
                        w.show();
                    }
                }
            }
        );

        // TODO the social filtering is buggy, so let's not expose it for now
        //bar.add(_filterButton);


        add(bar, new RowData(1, -1, new Margins(5, 5, 5, 5)));
        _html = new InterceptedHtml();
        add(_html, new RowData(1, -1, new Margins(5)));
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
        _instance = instance;
        update();
    }

    public void update()
    {
        Params params = new Params();
        params.set("heading", getHeading(_instance));
        
        String content = _template.applyTemplate(params);
        _html.setHtml(content);
    }

    private String getHeading(DataInstance instance)
    {
        StringBuffer buf = new StringBuffer();

        int count = instance.getCommentCount();
        
        if (count == 0)
        {
            buf.append("No " + typeNamePlural);
        }
        else if (count == 1)
        {
            buf.append("1 " + typeName);
        }
        else
        {
            buf.append(count + " " + typeNamePlural);
        }
        
        return buf.toString();
    }
}
