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

package com.paxxis.chime.client;

import java.util.Date;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.BoxComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout.HBoxLayoutAlign;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Element;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.SearchFilter;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.DataInstanceRequest.Operator;
import com.paxxis.chime.client.common.constants.SearchFieldConstants;
import com.paxxis.chime.client.widgets.InterceptedHtml;

/**
 *
 * @author Robert Englander
 */
public class SearchFilterPanel extends LayoutContainer
{
    private SearchFilter _filter;
    private SearchFilterModifyListener _listener = null;
    private IconButton _deleteButton;
    private IconButton _enableButton;
    private boolean _readOnly;
    
    public SearchFilterPanel(SearchFilter filter, SearchFilterModifyListener listener)
    {
        _filter = filter;
        _listener = listener;
        _readOnly = false;
        init();
    }
    
    public SearchFilterPanel(SearchFilter filter)
    {
        _filter = filter;
        _readOnly = true;
        init();
    }

    private void init() {
        addListener(Events.Resize,
            new Listener<BoxComponentEvent>() {

                public void handleEvent(BoxComponentEvent evt) {
                    layout();
                }

            }
        );
    }
    
    public SearchFilter getFilter()
    {
        return _filter;
    }
    
    public void updateButtonState()
    {
        if (_filter.isEnabled())
        {
            _enableButton.changeStyle("enabled-filter-icon");
        }
        else
        {
            _enableButton.changeStyle("disabled-filter-icon");
        }
    }
    
    public void onRender(Element el, int p)
    {
        super.onRender(el, p);

        setLayout(new FilledColumnLayout(HorizontalAlignment.LEFT));
        setBorders(false);
        LayoutContainer bar = new LayoutContainer();
        HBoxLayout layout = new HBoxLayout();
        layout.setPadding(new Padding(5));
        layout.setHBoxLayoutAlign(HBoxLayoutAlign.TOP);
        bar.setLayout(layout);
        bar.setBorders(false);

        if (!_readOnly)
        {
            _deleteButton = new IconButton("delete-icon");
            _deleteButton.addSelectionListener(
                new SelectionListener<IconButtonEvent>()
                {
                    @Override
                    public void componentSelected(IconButtonEvent evt) 
                    {
                        _listener.onDeleteRequest(SearchFilterPanel.this);
                    }
                }
            );
    
            bar.add(_deleteButton, new HBoxLayoutData(new Margins(2)));
        }
        
        _enableButton = new IconButton("enabled-filter-icon");
        _enableButton.addSelectionListener(
            new SelectionListener<IconButtonEvent>()
            {
                @Override
                public void componentSelected(IconButtonEvent evt) 
                {
                    _listener.onEnableRequest(SearchFilterPanel.this, !_filter.isEnabled());
                }
            }
        );

        _enableButton.setEnabled(!_readOnly);
        bar.add(_enableButton, new HBoxLayoutData(new Margins(2)));
        
        if (_readOnly) {
            add(bar, new FilledColumnLayoutData(30));
        } else {
            add(bar, new FilledColumnLayoutData(55));
        }
        
        LayoutContainer htmlPanel = new LayoutContainer();
        htmlPanel.setLayout(new RowLayout());
        
        InterceptedHtml html = new InterceptedHtml();
        html.setStyleAttribute("font", "normal 12px arial, tahoma, sans-serif");
        html.setStyleAttribute("top", "8px");
        html.setStyleAttribute("position", "relative");
        html.setStyleAttribute("color", "black");
        html.setBorders(false);
        
        Operator op = _filter.getOperator();

        String field;
        boolean isTag = false;
        if (_filter.getDataField().getName().equalsIgnoreCase(SearchFieldConstants.TAG))
        {
            isTag = true;
            
            if (op == Operator.NotReference)
            {
                field = "Not tagged";
            }
            else
            {
                field = "Tagged";
            }
        }
        else if (_filter.getDataField().getName().equalsIgnoreCase(SearchFieldConstants.USER))
        {
            field = SearchFieldConstants.USER;
        }
        else if (_filter.getDataField().getName().equalsIgnoreCase(SearchFieldConstants.COMMUNITY))
        {
            field = SearchFieldConstants.COMMUNITY;
        }
        else
        {
            field = _filter.getDataField().getName();
            DataField subField = _filter.getSubField();

            if (subField != null) {
            	field += " : " + subField.getName();
            }
        }
        
        Shape shape = _filter.getDataShape();
        if (shape != null) {
        	field = shape.getName() + " : " + field;
        }
        
        String text = "<span id='filter-field'>" + field + "</span>";
        switch (op)
        {
            case StartsWith:
                text += " contains word starting with '" + _filter.getDisplayValue() + "'";
                break;
            case Contains:
                String value = _filter.getDisplayValue();
                if (-1 == value.indexOf(" "))
                {
                    text += " contains the word '" + value + "'";
                }
                else
                {
                    text += " contains the phrase '" + value + "'";
                }
                break;
            case Like:
                text += " contains a word like '" + _filter.getDisplayValue() + "'";
                break;
            case Equals:
                text += " equals " + _filter.getDisplayValue();
                break;
            case NotEquals:
                text += " does not equal " + _filter.getDisplayValue();
                break;
            case LessThan:
                text += " less than " + _filter.getDisplayValue();
                break;
            case LessThanOrEquals:
                text += " less than or equal to " + _filter.getDisplayValue();
                break;
            case GreaterThan:
                text += " greater than " + _filter.getDisplayValue();
                break;
            case GreaterThanOrEquals:
                text += " greater than or equal to " + _filter.getDisplayValue();
                break;
            case Reference:
                if (isTag) {
                	InstanceId id = InstanceId.create(_filter.getValue().toString());
                	String link = Utils.toHoverUrl(id, _filter.getDisplayValue());
                    text += " with " + link;
                } else {
                	InstanceId id = InstanceId.create(_filter.getValue().toString());
                	String link = Utils.toHoverUrl(id, _filter.getDisplayValue());
                    text += " is " + link;
                }
                break;
            case ContainedIn:
            	{
	            	InstanceId id = InstanceId.create(_filter.getValue().toString());
	            	String link = Utils.toHoverUrl(id, _filter.getDisplayValue());
	                text += " is in " + link;
            	}
                break;
            case NotReference:
                if (isTag) {
                	InstanceId id = InstanceId.create(_filter.getValue().toString());
                	String link = Utils.toHoverUrl(id, _filter.getDisplayValue());
                    text += " with " + link;
                } else {
                	InstanceId id = InstanceId.create(_filter.getValue().toString());
                	String link = Utils.toHoverUrl(id, _filter.getDisplayValue());
                    text += " is not " + link;
                }
                break;
            case NotContainedIn:
	            {
                	InstanceId id = InstanceId.create(_filter.getValue().toString());
	            	String link = Utils.toHoverUrl(id, _filter.getDisplayValue());
	                text += " is not in " + link;
	            }
                break;
            case Past24Hours:
                text += " within the past 24 hours";
                break;
            case Past3Days:
                text += " within the past 3 days";
                break;
            case Past7Days:
                text += " within the past 7 days";
                break;
            case Past30Days:
                text += " within the past 30 days";
                break;
            case BeforeDate:
	        	{
	                Date dval = (Date)_filter.getValue();
	                DateTimeFormat dtf = DateTimeFormat.getFormat("MMM d, yyyy");
	                String formatted = dtf.format(dval);
	        		text += " is before " + formatted;
	        	}
	            break;
            case OnOrBeforeDate:
	        	{
	                Date dval = (Date)_filter.getValue();
	                DateTimeFormat dtf = DateTimeFormat.getFormat("MMM d, yyyy");
	                String formatted = dtf.format(dval);
	        		text += " is on or before " + formatted;
	        	}
	            break;
            case OnDate:
	        	{
	                Date dval = (Date)_filter.getValue();
	                DateTimeFormat dtf = DateTimeFormat.getFormat("MMM d, yyyy");
	                String formatted = dtf.format(dval);
	        		text += " is " + formatted;
	        	}
                break;
            case AfterDate:
	        	{
	                Date dval = (Date)_filter.getValue();
	                DateTimeFormat dtf = DateTimeFormat.getFormat("MMM d, yyyy");
	                String formatted = dtf.format(dval);
	        		text += " is after " + formatted;
	        	}
	            break;
            case OnOrAfterDate:
	        	{
	                Date dval = (Date)_filter.getValue();
	                DateTimeFormat dtf = DateTimeFormat.getFormat("MMM d, yyyy");
	                String formatted = dtf.format(dval);
	        		text += " is on or after " + formatted;
	        	}
	            break;
            case IsYes:
        		text += " is Yes";
            	break;
            case IsNo:
        		text += " is No";
            	break;
        }

        html.setHtml(text + "<br>&nbsp;");
        htmlPanel.add(html, new RowData(1, -1));
        
        add(htmlPanel, new FilledColumnLayoutData());
        updateButtonState();
    }
}

