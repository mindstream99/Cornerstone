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

package com.paxxis.chime.client.portal;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.util.Size;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.AreaChart;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.portal.PortletSpecification;

/**
 * 
 * @author Robert Englander
 *
 */
public class MultiChartPortlet extends PortletContainer {
	
	private List<AreaChart> chart = new ArrayList<AreaChart>();
    private List<AreaChart.Options> options = new ArrayList<AreaChart.Options>(); //AreaChart.Options.create();
    private List<DataTable> data = new ArrayList<DataTable>(); //DataTable.create();

    private LayoutContainer container;
    private PortalContainer portal = null;

    public MultiChartPortlet(PortletSpecification spec) {
        super(spec, HeaderType.Shaded, true);
    }

    protected void init() {
    	super.init();
    }

    @Override
    protected void onResize(int width, int height) {
    	super.onResize(width, height);

    	int aw = width;

    	Size frameWidth = el().getFrameSize();
    	aw -= (15 + frameWidth.width);
   }

    public void setDataInstance(final DataInstance instance, final UpdateReason reason) {

        Runnable r = new Runnable() {
        	public void run() {
    	    	Object obj = getSpecification().getProperty("title");
    	    	if (obj != null) {
    	    		String title = obj.toString();
    	    		setHeading(title);
    	    	}

				String specField = getSpecification().getProperty("field").toString();
				
         	}
        };
    	
    	if (isRendered()) {
    		r.run();
    	} else {
    		addPostRenderRunnable(r);
    	}
   }
}
