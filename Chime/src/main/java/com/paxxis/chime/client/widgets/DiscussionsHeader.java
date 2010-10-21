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
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.paxxis.chime.client.ChimeAsyncCallback;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceResponseObject;
import com.paxxis.chime.client.common.CreateDiscussionRequest;
import com.paxxis.chime.client.common.CreateDiscussionResponse;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.editor.DiscussionEditorWindow;
import com.paxxis.chime.client.portal.UpdateReason;

/**
 *
 * @author Robert Englander
 */
public class DiscussionsHeader extends LayoutContainer {
    public interface DiscussionsChangedListener {
        public void onDiscussionsChanged(DataInstance instance);
    }

    private static final String TEMPLATE = "<div id='discussions-header'>"  +
        "{heading}</div>";

    private static Template _template;

    static
    {
        _template = new Template(TEMPLATE);
        _template.compile();
    }

    private DataInstance _instance;
    private InterceptedHtml _html;
    private Button _discussionButton;
    private DiscussionsChangedListener _discussionsChangedListener;

    public DiscussionsHeader(DiscussionsChangedListener listener) {
        _discussionsChangedListener = listener;
        init();
    }

    private void init()
    {
        setLayout(new RowLayout());
        setBorders(false);
        setStyleAttribute("backgroundColor", "white");

        final ToolBar bar = new ToolBar();
        _discussionButton = new Button("Create a discussion...");
        bar.add(_discussionButton);

        final DiscussionEditorWindow.DiscussionEditorListener listener =
            new DiscussionEditorWindow.DiscussionEditorListener() {
                public void onComplete(String title, String initialComment) {
                    processRequest(title, initialComment);
                }
            };

        _discussionButton.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent evt) {
                    DiscussionEditorWindow w = new DiscussionEditorWindow(listener);
                    w.show();
                }
            }
        );

        add(bar, new RowData(1, -1, new Margins(5, 5, 5, 5)));
        _html = new InterceptedHtml();
        add(_html, new RowData(1, -1, new Margins(5)));

    }

    private void processRequest(String title, String initialComment) {

        final ChimeAsyncCallback<ServiceResponseObject<CreateDiscussionResponse>> callback = 
        		new ChimeAsyncCallback<ServiceResponseObject<CreateDiscussionResponse>>() {
            public void onSuccess(ServiceResponseObject<CreateDiscussionResponse> response) {
                if (response.isResponse()) {
                    _discussionsChangedListener.onDiscussionsChanged(response.getResponse().getDataInstance());
                } else {
                    ChimeMessageBox.alert("Error", response.getError().getMessage(), null);
                }
            }
        };

        CreateDiscussionRequest request = new CreateDiscussionRequest();
        request.setData(_instance);
        request.setUser(ServiceManager.getActiveUser());
        request.setTitle(title);
        request.setInitialComment(initialComment);
        ServiceManager.getService().sendCreateDiscussionRequest(request, callback);
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

    private static String getHeading(DataInstance instance)
    {
        StringBuffer buf = new StringBuffer();

        int count = instance.getSocialContext().getDiscussionsBundle().getDiscussions().size();
        if (count == 0)
        {
            buf.append("No Discussions");
        }
        else if (count == 1)
        {
            buf.append("1 Discussion");
        }
        else
        {
            buf.append(count + " Discussions");
        }

        return buf.toString();
    }
}
