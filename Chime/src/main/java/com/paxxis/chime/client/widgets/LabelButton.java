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

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.paxxis.chime.client.FilledColumnLayout;
import com.paxxis.chime.client.FilledColumnLayoutData;

/**
 *
 * @author Robert Englander
 */
public class LabelButton extends LayoutContainer
{
    private Button _button;
    private String _iconStyle;
    private String _text;
    
    public LabelButton(String iconStyle, String text)
    {
        _iconStyle = iconStyle;
        _text = text;
    }
    
    public void onRender(Element el, int p)
    {
        super.onRender(el, p);

        setLayout(new FilledColumnLayout(HorizontalAlignment.LEFT));
        setBorders(false);
        
        setStyleAttribute("background", "transparent");
        ToolBar bar = new ToolBar();
        bar.setStyleAttribute("background", "transparent");
        bar.setBorders(false);

        _button = new Button();
        _button.setIconStyle(_iconStyle);
        _button.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent evt) 
                {
                }
            }
        );

        bar.add(_button);
        
        add(bar, new FilledColumnLayoutData());
        
        LayoutContainer htmlPanel = new LayoutContainer();
        htmlPanel.setLayout(new RowLayout());
        
        HtmlContainer htmlTitle = new HtmlContainer();
        htmlTitle.setStyleAttribute("font", "normal 12px arial, tahoma, sans-serif");
        htmlTitle.setStyleAttribute("top", "3px");
        htmlTitle.setStyleAttribute("position", "relative");
        htmlTitle.setBorders(false);
        
        htmlTitle.setHtml(_text);
        htmlPanel.add(htmlTitle, new RowData(1, -1));

        add(htmlPanel, new FilledColumnLayoutData());
    }
}
