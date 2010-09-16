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
import com.extjs.gxt.ui.client.event.DomEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.paxxis.chime.client.FilledColumnLayout;
import com.paxxis.chime.client.FilledColumnLayoutData;

/**
 *
 * @author Robert Englander
 */
public class RatingPanel extends LayoutContainer
{
    public interface RatingChangeListener
    {
        public void onChange(int rating);
    }
    
    private RatingChangeListener _listener = null;
    
    private IconButton _iconButton;
    private Text _label;
    private int _currentPos = -1;
    private int _rating = 0;
    
    public RatingPanel()
    {
        this(0);
    }
    
    public RatingPanel(int rating)
    {
        _rating = rating;
        setLayout(new FilledColumnLayout(HorizontalAlignment.LEFT));
        setToolTip("Click to set rating");
        _iconButton = new IconButton("stars0-icon");
        
        add(_iconButton, new FilledColumnLayoutData(75));
        
        _label = new Text();
        add(_label, new FilledColumnLayoutData());
        
        _iconButton.addListener(Events.OnClick, 
            new Listener<DomEvent>()
            {
                public void handleEvent(DomEvent evt)
                {
                    int x = evt.getClientX() - _iconButton.getAbsoluteLeft();
                    select(x);
                }
            }
        );

        _iconButton.addListener(Events.OnMouseMove, 
            new Listener<DomEvent>()
            {
                public void handleEvent(DomEvent evt)
                {
                    int x = evt.getClientX() - _iconButton.getAbsoluteLeft();
                    update(x);
                }
            }
        );

        _iconButton.addListener(Events.OnMouseOut, 
            new Listener<DomEvent>()
            {
                public void handleEvent(DomEvent evt)
                {
                    clear();
                }
            }
        );
        
        setStars(rating);
    }
    
    public void setRatingChangeListener(RatingChangeListener listener)
    {
        _listener = listener;
    }
    
    public int getRating()
    {
        return _rating;
    }
    
    private void clear()
    {
        String icon = "stars" + _rating + "-icon";
        _iconButton.changeStyle(icon);
        _currentPos = -1;
    }
    
    private void select(int x)
    {
        _rating = 1 + (x / 15);
            
        if (_listener != null)
        {
            _listener.onChange(_rating);
        }
    }
    
    private void update(int x)
    {
        int pos = 1 + (x / 15);
        
        if (pos != _currentPos)
        {
            _currentPos = pos;
            setStars(pos);
        }
    }
    
    private void setStars(int val)
    {
        String icon = "stars" + val + "-icon";
        _iconButton.changeStyle(icon);
    }
}
