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

import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.paxxis.chime.client.common.Cursor;

/**
 *
 * @author Robert Englander
 */
public class Paginator extends ToolBar
{
    private static int _idCounter = 0;
    private static final String ID = "PAG";
    
    private Component extraComponent;
    Cursor _cursor = null;
    PagingListener _listener;
    IconButton _first;
    IconButton _last;
    IconButton _previous;
    IconButton _next;
    LabelField _label;
    String _uniqueId;
    Cursor pendingCursor = null;
    
    private static String nextId()
    {
        _idCounter++;
        return (ID + _idCounter);
    }
    
    public Paginator(PagingListener listener) {
        this(listener, null);
    }

    public Paginator(PagingListener listener, Component extra) {
        super();
        
        _uniqueId = nextId();
        extraComponent = extra;
        _listener = listener;
        setStyleAttribute("background", "transparent");
        setBorders(false);
    }
    
    private void init() {
        // Home
        _first = new IconButton("x-tbar-page-first");
        _first.setWidth(20);
        _first.addSelectionListener(
            new SelectionListener<IconButtonEvent>()
            {
                @Override
                public void componentSelected(IconButtonEvent evt) 
                {
                    first();
                }
            }
        );
        
        _previous = new IconButton("x-tbar-page-prev");
        _previous.setWidth(20);
        _previous.addSelectionListener(
            new SelectionListener<IconButtonEvent>()
            {
                @Override
                public void componentSelected(IconButtonEvent evt) 
                {
                    previous();
                }
            }
        );
        
        _label = new LabelField();
        _label.setText("No Data");
        //_label.setStyleAttribute("color", "#fbf0d2");
        
        _next = new IconButton("x-tbar-page-next");
        _next.setWidth(20);
        _next.addSelectionListener(
            new SelectionListener<IconButtonEvent>()
            {
                @Override
                public void componentSelected(IconButtonEvent evt) 
                {
                    next();
                }
            }
        );
        
        _last = new IconButton("x-tbar-page-last");
        _last.setWidth(20);
        _last.addSelectionListener(
            new SelectionListener<IconButtonEvent>()
            {
                @Override
                public void componentSelected(IconButtonEvent evt) 
                {
                    last();
                }
            }
        );

        add(_first);
        add(_previous);
        add(_label);
        add(_next);
        add(_last);
        
        if (extraComponent != null) {
            add(new FillToolItem());
            add(extraComponent);
        }

        setCursor(null);
    }
    
    public void onRender(Element parent, int index) {
    	super.onRender(parent, index);
    	init();
    	
    	if (pendingCursor != null) {
    		Cursor c = pendingCursor;
    		pendingCursor = null;
    		setCursor(c);
    	}
    }

    public String getUniqueId()
    {
        return _uniqueId;
    }
    
    public Cursor getCursor()
    {
        return _cursor;
    }
    
    public boolean canDoFirst()
    {
        return _first.isEnabled();
    }
    
    public boolean canDoPrevious()
    {
        return _previous.isEnabled();
    }
    
    public boolean canDoNext()
    {
        return _next.isEnabled();
    }
    
    public boolean canDoLast()
    {
        return _last.isEnabled();
    }
    
    public void setCursor(Cursor cursor)
    {
    	if (!isRendered()) {
    		pendingCursor = cursor;
    		return;
    	}
    	
        _cursor = cursor;
        
        if (cursor == null)
        {
            _first.setEnabled(false);
            _previous.setEnabled(false);
            _next.setEnabled(false);
            _last.setEnabled(false);
            _label.setText("No Data");
            
            int fw = _first.getWidth();
            int fh = _first.getHeight();
            boolean fv = _first.isVisible();
            boolean fv1 = _first.isVisible(true);
            
            
            
            int xxx = 1;
        }
        else
        {
            int count = cursor.getCount();
            int total = cursor.getTotal();
            int first = 1 + cursor.getFirst();
            int last = first + count - 1;
            boolean atFirst = first == 1;
            boolean atLast = last == total;

            _first.setEnabled(!atFirst);
            _last.setEnabled(!atLast);
            _previous.setEnabled(!atFirst);
            _next.setEnabled(!atLast);

            String text;

            if (total > 0)
            {
                if (first == last) {
                    text = last + " of " + total;
                } else {
                    text = first + " to " + last + " of " + total;
                }

                if (cursor.isLimited())
                {
                    text += "+";
                }
            }
            else
            {
                text = "No Data";
            }
            
            _label.setText(text);
        }
        
        _label.setEnabled(true);
    }
    
    public void next()
    {
        _listener.onNext();
    }
    
    public void previous()
    {
        _listener.onPrevious();
    }
    
    public void first()
    {
        _listener.onFirst();
    }
    
    public void last()
    {
        _listener.onLast();
    }
    
    public void refresh()
    {
        _listener.onRefresh();
    }
}
