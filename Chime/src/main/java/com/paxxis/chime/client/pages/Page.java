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

import com.extjs.gxt.ui.client.widget.Container;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

/**
 * 
 * @author Robert Englander
 *
 */
public class Page extends LayoutContainer
{
    protected Container _content;
    protected String _name;
    private boolean _closable = true;
    private String _iconStyle;

    public Page(Container content, String name, String iconStyle) 
    {
        this(content, name, true, iconStyle);
    }

    public Page(Container content, String name, boolean closeable, String iconStyle) 
    {
        _content = content;
        _name = name;
        _closable = closeable;
        _iconStyle = iconStyle;
        
        setLayout(new FitLayout());
        setBorders(false);
        add(content);

    }

    public Container getContent()
    {
        return _content;
    }
    
    public String getName()
    {
        return _name;
    }
    
    public String getIconStyle()
    {
        return _iconStyle;
    }
    
    public boolean isClosable() 
    {
        return _closable;
    }
}
