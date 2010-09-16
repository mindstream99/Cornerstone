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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.Element;

/**
 * 
 * @author Robert Englander
 *
 */
public abstract class ChimeLayoutContainer extends LayoutContainer {

	private List<Runnable> postRenderList = new ArrayList<Runnable>();
	
    public void addPostRenderRunnable(Runnable r) {
    	postRenderList.add(r);
    }

    protected abstract void init();
    
    @Override
    protected void onRender(Element parent, int index) {
    	super.onRender(parent, index);
    	init();
    	postRender();
    }

    protected void postRender() {
    	for (Runnable r : postRenderList) {
    		r.run();
    	}
    	
    	postRenderList.clear();
    }

    public void applyMask() {

    	Runnable r = new Runnable() {
    		public void run() {
    	    	El mask = new El("<div class='chime-el-mask'></div>");

    	        El me = el();
    	        me.makePositionable();
    	        //me.addStyleName("x-masked");

    	        if (GXT.isIE && !GXT.isStrict) {
    	            mask.setWidth(me.getWidth());
    	            mask.setHeight(me.getHeight());
    	        }

    	        mask.setDisplayed(true);
    	        me.appendChild(mask.dom);
    		}
    	};
    	
    	if (isRendered()) {
    		r.run();
    	} else {
    		addPostRenderRunnable(r);
    	}
    }

}
