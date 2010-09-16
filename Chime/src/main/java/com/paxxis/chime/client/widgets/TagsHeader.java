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
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceResponseObject;
import com.paxxis.chime.client.common.ApplyTagRequest;
import com.paxxis.chime.client.common.ApplyTagResponse;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.Tag;
import com.paxxis.chime.client.common.ApplyTagRequest.ApplyType;
import com.paxxis.chime.client.portal.UpdateReason;

/**
 *
 * @author Robert Englander
 */
public class TagsHeader extends LayoutContainer
{
    public interface TagsChangedListener {
        public void onTagsChanged(DataInstance instance);
    }

    private static final String TEMPLATE = "<div id='tags-header'>"  +
        "{heading}</div>";
    
    private static Template _template;

    static
    {
        _template = new Template(TEMPLATE);
        _template.compile();
    }

    private DataInstance _instance;
    private InterceptedHtml _html;
    private TagsPanel _tagsPanel;
    private Button _sortButton;
    private Button _tagButton;
    private TagsChangedListener _tagsChangedListener;
    
    public TagsHeader(TagsPanel panel, TagsChangedListener tagsChangedListener)
    {
        _tagsPanel = panel;
        _tagsChangedListener = tagsChangedListener;
        init();
    }
    
    public void sendRequest(ApplyTagRequest request)
    {
        final AsyncCallback callback = new AsyncCallback()
        {
            public void onFailure(Throwable arg0)
            {
                ChimeMessageBox.alert("System Error", "Please contact the system administrator.", null);
            }

            public void onSuccess(Object obj)
            {
                ServiceResponseObject<ApplyTagResponse> response = (ServiceResponseObject<ApplyTagResponse>)obj;
                if (response.isResponse())
                {
                    _tagsChangedListener.onTagsChanged(response.getResponse().getDataInstance());
                }
                else
                {
                    ChimeMessageBox.alert("Error", response.getError().getMessage(), null);
                }
            }
        };

        request.setUser(ServiceManager.getActiveUser());
        ServiceManager.getService().sendApplyTagRequest(request, callback);
    }

    private void init()
    {
        setLayout(new RowLayout());
        setBorders(false);
        setStyleAttribute("backgroundColor", "white");
        
        LayoutContainer upper = new LayoutContainer();

        _html = new InterceptedHtml();
        upper.add(_html);
        add(_html, new RowData(1, -1, new Margins(5)));
        
        final ButtonBar bar = new ButtonBar();
        _tagButton = new Button("Apply a tag...");
        _tagButton.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent evt) 
                {
                    ApplyTagWindow tagWindow = new ApplyTagWindow(
                        new ApplyTagWindow.ApplyTagListener()
                        {
                            public void onApply(Tag tag) 
                            {
                                ApplyTagRequest req = new ApplyTagRequest();
                                req.addTag(tag);
                                req.setApplyType(ApplyType.Add);
                                req.setData(_instance);

                                sendRequest(req);
                            }
                        }
                    );
                    
                    /*
                    int left = _tagButton.getAbsoluteLeft();
                    int top = _tagButton.getAbsoluteTop();
                    int height = bar.getSize().height;
                    
                    tagWindow.setPosition(left, top + height);
                    */
                    
                    tagWindow.show();
                }
            }
        );
        
        bar.add(_tagButton);

        _sortButton = new Button();
        
        if (_tagsPanel.getDisplayMode() == TagsPanel.Mode.Cloud)
        {
            _sortButton.setText("Show as Cloud");
        }
        else
        {
            _sortButton.setText("Show as List");
        }
        
        Menu menu = new Menu();
        
        CheckMenuItem item = new CheckMenuItem("Show as List");
        item.setGroup("TagStyleGroup");
        item.setChecked(_tagsPanel.getDisplayMode() == TagsPanel.Mode.List);
        item.addSelectionListener(
            new SelectionListener<MenuEvent>()
            {
                @Override
                public void componentSelected(MenuEvent evt) 
                {
                    _tagsPanel.setDisplayMode(TagsPanel.Mode.List);
                    _sortButton.setText("Show as List");
                }
            }
        );
        menu.add(item);
        
        CheckMenuItem item2 = new CheckMenuItem("Show as Cloud");
        item2.setGroup("TagStyleGroup");
        item2.setChecked(_tagsPanel.getDisplayMode() == TagsPanel.Mode.Cloud);
        item2.addSelectionListener(
            new SelectionListener<MenuEvent>()
            {
                @Override
                public void componentSelected(MenuEvent evt) 
                {
                    _tagsPanel.setDisplayMode(TagsPanel.Mode.Cloud);
                    _sortButton.setText("Show as Cloud");
                }
            }
        );
        menu.add(item2);
        
        _sortButton.setMenu(menu);
        bar.add(_sortButton);

        add(bar, new RowData(1, -1, new Margins(5, 5, 5, 5)));
    }
    
    public void setDataInstance(DataInstance instance, UpdateReason reason)
    {
        _instance = instance;
        update(reason);
    }

    public void update(UpdateReason reason)
    {
        if (reason == UpdateReason.InstanceChange || reason == UpdateReason.LoginLogout) {
            Params params = new Params();
            params.set("heading", getHeading(_instance));

            String content = _template.applyTemplate(params);
            _html.setHtml(content);

            if (ServiceManager.getActiveUser() == null) {
                _tagButton.setEnabled(false);
                //_tagButton.setToolTip("You must be logged in to apply a tag.");
            } else {
                _tagButton.setEnabled(true);
                //_tagButton.setToolTip((String)null);
            }
        }
    }

    private static String getHeading(DataInstance instance)
    {
        StringBuffer buf = new StringBuffer();

        int count = instance.getSocialContext().getTagContexts().size();

        if (count == 0)
        {
            buf.append("No Tags Applied");
        }
        else if (count == 1)
        {
            buf.append("1 Tag Applied");
        }
        else
        {
            buf.append(count + " Tags Applied");
        }
        
        return buf.toString();
    }
}
