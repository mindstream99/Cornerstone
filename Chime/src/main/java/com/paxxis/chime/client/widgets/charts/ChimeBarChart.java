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

package com.paxxis.chime.client.widgets.charts;

import com.extjs.gxt.ui.client.util.Size;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.LegendPosition;
import com.google.gwt.visualization.client.visualizations.BarChart;
import com.paxxis.chime.client.common.portal.PortletSpecification;

/**
 * A wrapper for the gwt visualizations Bar Chart.
 * 
 * @author Robert Englander
 *
 */
public class ChimeBarChart extends ChimeChart {

	private BarChart chart;
    private BarChart.Options options = BarChart.Options.create();
	private PortletSpecification spec;
	
	ChimeBarChart(PortletSpecification spec) {
		this.spec = spec;
	}
	
	@Override
	protected void init() {
        setBorders(false);
        setLayout(new RowLayout());

        Object object = spec.getProperty("max");
        if (object != null) {
        	try {
        		int max = Integer.parseInt(object.toString());
        		options.setMax(max);
        	} catch (NumberFormatException e) {
        		// there's nothing to do here, as we're running inside the browser.
        		// the spec has already been validated at load time by the ChimeService.
        		// this catch is only here because it's a java checked exception.
        	}
        }

        object = spec.getProperty("min");
        if (object != null) {
        	try {
        		int min = Integer.parseInt(object.toString());
        		options.setMin(min);
        	} catch (NumberFormatException e) {
        		// there's nothing to do here, as we're running inside the browser.
        		// the spec has already been validated at load time by the ChimeService.
        		// this catch is only here because it's a java checked exception.
        	}
        }

	    options.setHeight(-1);
	    options.setWidth(-1);
	    options.setTitleFontSize(14.0); 
	    options.setTitle(spec.getProperty("field").toString());
	    options.setLegend(LegendPosition.NONE);
	    options.setTitleY(spec.getProperty("labelY").toString());
	    options.setAxisFontSize(12.0);
	    options.setEnableTooltip(true);
	    
	    DataTable data = getData();
	    data.addRow();
	    data.setValue(0, 0, 0);
	    chart = new BarChart(data, options);
	    
	    add(chart, new RowData(1, -1));
	}

	public void redraw() {
	    chart.draw(getData(), options);
	}
	
    @Override
    protected void onResize(int width, int height) {
    	super.onResize(width, height);

    	int aw = width;

    	Size frameWidth = el().getFrameSize();
    	aw -= (15 + frameWidth.width);

		options.setHeight(aw);
		options.setWidth(aw);
		redraw();
    }
}



