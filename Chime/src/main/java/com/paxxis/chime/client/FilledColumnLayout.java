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

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.util.Size;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Container;
import com.extjs.gxt.ui.client.widget.Layout;

/**
 *
 * @author Robert Englander
 */
public class FilledColumnLayout extends Layout 
{

    protected El innerCt;
    protected HorizontalAlignment alignment;
    
    public FilledColumnLayout(HorizontalAlignment align) 
    {
        alignment = align;
        setExtraStyle("x-column");
    }

    @Override
    protected void onLayout(Container container, El target) 
    {
        if (innerCt == null) 
        {
            container.addStyleName("x-column-layout-ct");
            innerCt = target.createChild("<div class='x-column-inner'></div>");
            innerCt.createChild("<div class='x-clear'></div>");
        }

        renderAll(container, innerCt);

        Size size = target.getSize(true);

        int w = size.width;

        int count = container.getItemCount();
        int lastIndex;
        
        if (alignment == HorizontalAlignment.LEFT)
        {
            lastIndex = count - 1;
            // we size all of the columns except the last one based on the
            // layout data width value.  the last column gets the remainder.
            for (int i = 0; i < (count - 1); i++)
            {
                Component c = container.getItem(i);
                FilledColumnLayoutData data = (FilledColumnLayoutData) getLayoutData(c);
                int width = data.getWidth();
                if (width == -1)
                {
                    setSize(c, -1, -1);
                    //int prefW = c.el().getWidth(false);
                    int prefW = c.getOffsetWidth();
                    setSize(c, prefW, -1);
                    w -= prefW;
                }
                else
                {
                    setSize(c, width, -1);
                    w -= width;
                }
            }
        }
        else
        {
            lastIndex = 0;
            // we size all of the columns except the last one based on the
            // layout data width value.  the last column gets the remainder.
            for (int i = (count - 1); i > 0; i--)
            {
                Component c = container.getItem(i);
                FilledColumnLayoutData data = (FilledColumnLayoutData) getLayoutData(c);
                int width = data.getWidth();
                if (width == -1)
                {
                    setSize(c, -1, -1);
                    //int prefW = c.el().getWidth(false);
                    int prefW = c.getOffsetWidth();
                    setSize(c, prefW, -1);
                    w -= prefW;
                }
                else
                {
                    setSize(c, width, -1);
                    w -= width;
                }
            }
        }
        
        // size the last one
        if (count > 0)
        {
            Component c = container.getItem(lastIndex);
            setSize(c, w, -1);
        }
    }

}
