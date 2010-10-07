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
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.LegendPosition;
import com.google.gwt.visualization.client.visualizations.AreaChart;
import com.paxxis.chime.client.common.portal.PortletSpecification;

/**
 * A wrapper for GWT area chart.
 * 
 * @author Robert Englander
 *
 */
public class ChimeAreaChart extends ChimeChart {

	private AreaChart chart;
    private AreaChart.Options options = AreaChart.Options.create();
	
	ChimeAreaChart(PortletSpecification spec) {
		super(spec);
	}
	
	@Override
	protected void init() {
		super.init();
        setBorders(false);
        setLayout(new RowLayout());

        PortletSpecification spec = getSpecification();
        
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
	    data.setValue(0, 0, "");
	    data.setValue(0, 1, 0);
	    chart = new AreaChart(data, options);
	    
	    add(chart, new RowData(1, -1));
	}
	
	public void redraw() {
		DeferredCommand.addCommand(
			new Command() {
				public void execute() {
			    	int val = getWidth() - 2;
					options.setWidth(val);
			    	
					if (!makeSquare()) {
						val = getHeight();
					}
					
					options.setHeight(val);

					chart.draw(getData(), options);
				}
			}
		);
	}
}



