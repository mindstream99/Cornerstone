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

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.paxxis.chime.client.FilledColumnLayout;
import com.paxxis.chime.client.FilledColumnLayoutData;
import com.paxxis.chime.client.InstanceUpdateListener;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceResponseObject;
import com.paxxis.chime.client.Utils;
import com.paxxis.chime.client.common.ApplyTagRequest;
import com.paxxis.chime.client.common.ApplyTagResponse;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.Tag;
import com.paxxis.chime.client.common.TagContext;
import com.paxxis.chime.client.common.ApplyTagRequest.ApplyType;
import com.paxxis.chime.client.portal.UpdateReason;
import com.paxxis.chime.client.widgets.TagsHeader.TagsChangedListener;

/**
 *
 * @author Robert Englander
 */
public class TagsPanel extends ChimeLayoutContainer
{
    /** the min and max point size */
    private static final int MINSIZE = 12;
    private static final int MAXSIZE = 36;
    private static final double PTRANGE = MAXSIZE - MINSIZE;

    public enum Mode
    {
        List,
        Cloud
    }
    
    private Mode _mode = Mode.List;
    
    private DataInstance _instance = null;
    private TagsHeader _header;
    private LayoutContainer _resultsList;
    private Listener<FieldEvent> _checkboxListener;
    private TagsChangedListener _tagsChangedListener;
    private InstanceUpdateListener updateListener;

    public TagsPanel(InstanceUpdateListener listener) {
    	super();
    	updateListener = listener;
    }
    
    protected void init()
    {
        setLayout(new RowLayout());
        setStyleAttribute("backgroundColor", "white");

        _resultsList = new LayoutContainer();
        
        _resultsList.setBorders(false);
        _resultsList.setStyleAttribute("backgroundColor", "white");
        
        _tagsChangedListener = new TagsChangedListener() {
            public void onTagsChanged(DataInstance instance) {
                setDataInstance(instance, UpdateReason.InstanceChange);
                if (updateListener != null) {
                    updateListener.onUpdate(instance, InstanceUpdateListener.Type.TagApplied);
                }
            }
        };
   
        _header = new TagsHeader(this, _tagsChangedListener);

        add(_header, new RowData(1, -1));

        ContentPanel p = new ContentPanel();
        p.setScrollMode(Scroll.AUTO);
        p.setHeaderVisible(false);
        p.setBorders(false);
        p.setBodyBorder(false);
        p.add(_resultsList);

        add(p, new RowData(1, 1, new Margins(5)));

        _checkboxListener = new Listener<FieldEvent>()
        {
            public void handleEvent(FieldEvent evt)
            {
                CheckBox box = (CheckBox)evt.getField();
                Tag tag = (Tag)box.getData("TAG");
                applyTag(tag, box.getValue());
            }
        };
    }

    private void applyTag(Tag tag, boolean apply)
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

        ApplyTagRequest req = new ApplyTagRequest();
        
        if (apply)
        {
            req.setApplyType(ApplyType.Add);
        }
        else
        {
            req.setApplyType(ApplyType.Remove);
        }
        
        req.addTag(tag);
        req.setData(_instance);
        req.setUser(ServiceManager.getActiveUser());

        ServiceManager.getService().sendApplyTagRequest(req, callback);
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
    
    public void setDisplayMode(Mode mode)
    {
        _mode = mode;
        update(UpdateReason.InstanceChange);
    }
    
    public Mode getDisplayMode()
    {
        return _mode;
    }
    
    public void update(UpdateReason reason)
    {
        if (reason == UpdateReason.InstanceChange || reason == UpdateReason.LoginLogout) {
            _resultsList.removeAll();

            if (_mode == Mode.List)
            {
                updateListMode();
            }
            else
            {
                updateCloudMode();
            }

            _resultsList.layout();
        }
    }
    
    private void updateListMode()
    {
        _resultsList.setLayout(new ColumnLayout());

        List<TagContext> tagContexts = _instance.getSocialContext().getTagContexts();
        for (TagContext tagContext : tagContexts)
        {
            Tag tag = tagContext.getTag();
            long usageCount = tagContext.getUsageCount();
            boolean userTagged = tagContext.isUserTagged();

            String tooltip = tag.getDescription();
            
            if (null == ServiceManager.getActiveUser())
            {
                InterceptedHtml element = new InterceptedHtml();
                
                element.addStyleName("tagLink12");
                String tagName = tag.getName(); //.replaceAll(" ", "&nbsp;");
                String link = Utils.toHoverUrl(tag, "&nbsp;" + tagName + "&nbsp;(" + usageCount + ")");
                
                element.setHtml(link);
                
                if (tooltip != null && tooltip.trim().length() > 0)
                {
                    //element.setToolTip(tooltip);
                }
                
                LayoutContainer c = new LayoutContainer();
                c.setLayout(new FlowLayout());
                c.add(element, new FlowData(new Margins(5)));
                _resultsList.add(c, new ColumnData(-1));
            }
            else
            {
                CheckBox element = new CheckBox();
                
                element.setFireChangeEventOnSetValue(false);
                element.addListener(Events.Change, _checkboxListener);
                element.setData("TAG", tag);
                element.setValue(userTagged);
                element.setFireChangeEventOnSetValue(true);
                String tagName = tag.getName().replaceAll(" ", "&nbsp;");

                InterceptedHtml label = new InterceptedHtml();
                label.addStyleName("tagLink12");
                element.setLabelSeparator("");

                String link;
                if (tag.isPrivate()) {
                    link = Utils.toHoverUrlPrivate(tag.getId(), "&nbsp;" + tagName + "&nbsp;(" + usageCount + ")");
                } else {
                    link = Utils.toHoverUrl(tag, "&nbsp;" + tagName + "&nbsp;(" + usageCount + ")");
                }
                
                label.setHtml(link);

                LayoutContainer c = new LayoutContainer();
                c.setBorders(false);
                c.setLayout(new FilledColumnLayout(HorizontalAlignment.LEFT));
                c.add(element, new FilledColumnLayoutData(-1));
                c.add(label, new FilledColumnLayoutData(-1));
                c.add(new LayoutContainer(), new FilledColumnLayoutData());
                
                LayoutContainer c2 = new LayoutContainer();
                c2.add(c, new FlowData(new Margins(5, 5, 0, 5)));
                c2.setBorders(false);
                _resultsList.add(c2, new ColumnData(-1));
            }
        }
    }
    
    private void updateCloudMode()
    {
        _resultsList.setLayout(new ColumnLayout());
        
        List<TagContext> tagContexts = _instance.getSocialContext().getTagContexts();
        
        long min = 99999;
        long max = 0;

        for (TagContext context : tagContexts)
        {
            if (context.getUsageCount() > max)
            {
                max = context.getUsageCount();
            }

            if (context.getUsageCount() < min)
            {
                min = context.getUsageCount();
            }
        }

        long range = max - min + 1;

        for (TagContext tagContext : tagContexts)
        {
            Tag tag = tagContext.getTag();
            long usageCount = tagContext.getUsageCount();
            boolean userTagged = tagContext.isUserTagged();

            String tooltip = tag.getDescription();

            // derive the pt size
            int weight = (int)usageCount;
            double factor = ((double)weight - (double)min) / (double)range;
            int size = (int)(PTRANGE * factor + MINSIZE);
            
            CheckBox checkBox = new CheckBox();

            checkBox.setFireChangeEventOnSetValue(false);
            checkBox.addListener(Events.Change, _checkboxListener);
            checkBox.setData("TAG", tag);
            checkBox.setValue(userTagged);
            checkBox.setFireChangeEventOnSetValue(true);
            
            InterceptedHtml element = new InterceptedHtml();
            element.addStyleName("tagLink" + size);
            
            String tagName = tag.getName().replaceAll(" ", "&nbsp;");

            if (tag.isPrivate())
            {
                String link = Utils.toHoverUrlPrivate(tag.getId(), "&nbsp;" + tagName);
                element.setHtml(link);
            }
            else
            {
                String link = Utils.toHoverUrl(tag, "&nbsp;" + tagName);
                element.setHtml(link);
            }
            
            if (tooltip != null && tooltip.trim().length() > 0)
            {
                //element.setToolTip(tooltip);
            }

            LayoutContainer c = new LayoutContainer();
            c.setBorders(false);
            c.setLayout(new FilledColumnLayout(HorizontalAlignment.LEFT));

            if (null != ServiceManager.getActiveUser())
            {
                c.add(checkBox, new FilledColumnLayoutData(-1));
            }
            
            c.add(element, new FilledColumnLayoutData(-1));
            c.add(new LayoutContainer(), new FilledColumnLayoutData());

            //LayoutContainer c2 = new LayoutContainer();
            //c2.setHeight(40);
            //c2.setLayout(new FitLayout());
            //c2.add(c, new FitData(new Margins(5, 5, 0, 5)));
            //c2.setBorders(false);
            //_resultsList.add(c2, new ColumnData(-1));
            
            LayoutContainer c2 = new LayoutContainer();
            c2.setHeight(40);
            c2.add(c, new FlowData(new Margins(5, 5, 0, 5)));
            c2.setBorders(false);
            _resultsList.add(c2, new ColumnData(-1));
            //LayoutContainer c = new LayoutContainer();
            //c.setLayout(new FlowLayout());
            //c.add(element, new FlowData(new Margins(5)));
            //_resultsList.add(c, new ColumnData(-1));
        }
    }
}
