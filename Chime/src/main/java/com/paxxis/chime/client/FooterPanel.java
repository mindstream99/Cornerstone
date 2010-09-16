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

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.Element;

/**
 *
 * @author Robert Englander
 */
public class FooterPanel extends HtmlContainer
{
    public FooterPanel()
    {
    }
    
    @Override
    public void onRender(Element element, int p)
    {
        super.onRender(element, p);
        
        StringBuffer sb = new StringBuffer();
        sb.append("<div id='endslice-footer' class='endslice-footer'><div id='endslice-footer-toolbar'></div><div id='endslice-footer-title'>Copyright 2009 Paxxis Technology LLC</div></div>");

        setHtml(sb.toString());
        //setEnabled(false);
        
        //ContextToolBar bar = new ContextToolBar();

        if (GXT.isIE)
        {
            LayoutContainer cont = new LayoutContainer();
            cont.setLayout(new FilledColumnLayout(HorizontalAlignment.RIGHT));

            LayoutContainer filler = new LayoutContainer();
            filler.setStyleAttribute("backgroundColor", "transparent");

            cont.add(filler, new FilledColumnLayoutData(-1));
            //cont.add(bar, new FilledColumnLayoutData(-1));
            cont.setWidth(250);

            add(cont, "#endslice-footer-toolbar");
        }
        else
        {
            //add(bar, "#endslice-footer-toolbar");
        }
    }
    
    public int getPanelHeight()
    {
        return Constants.FOOTERHEIGHT;
    }
}
