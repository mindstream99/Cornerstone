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

import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.visualizations.Gauge;
import com.paxxis.chime.client.common.portal.PortletSpecification;

/**
 * A wrapper for the gwt visualizations Gauge Chart.
 * 
 * @author Robert Englander
 *
 */
public class ChimeGaugeChart extends ChimeChart {

	private Gauge chart;
    private Gauge.Options options = Gauge.Options.create();
	
	ChimeGaugeChart(PortletSpecification spec) {
		super(spec);
	}
	
	@Override
	protected void init() {
		super.init();
        setBorders(false);
        setLayout(new RowLayout());

        int min = 0;
        int max = 100;
        
        PortletSpecification spec = getSpecification();
        
        Object object = spec.getProperty("max");
        if (object != null) {
        	try {
        		max = Integer.parseInt(object.toString());
        	} catch (NumberFormatException e) {
        		// there's nothing to do here, as we're running inside the browser.
        		// the spec has already been validated at load time by the ChimeService.
        		// this catch is only here because it's a java checked exception.
        	}
        }

        object = spec.getProperty("min");
        if (object != null) {
        	try {
        		min = Integer.parseInt(object.toString());
        	} catch (NumberFormatException e) {
        		// there's nothing to do here, as we're running inside the browser.
        		// the spec has already been validated at load time by the ChimeService.
        		// this catch is only here because it's a java checked exception.
        	}
        }

        options.setGaugeRange(min, max);
        
        Integer danger = null;
        object = spec.getProperty("danger");
        if (object != null) {
        	try {
        		danger = Integer.parseInt(object.toString());
        	} catch (NumberFormatException e) {
        		// there's nothing to do here, as we're running inside the browser.
        		// the spec has already been validated at load time by the ChimeService.
        		// this catch is only here because it's a java checked exception.
        	}
        }

        Integer warn = null;
        object = spec.getProperty("warn");
        if (object != null) {
        	try {
        		warn = Integer.parseInt(object.toString());
        	} catch (NumberFormatException e) {
        		// there's nothing to do here, as we're running inside the browser.
        		// the spec has already been validated at load time by the ChimeService.
        		// this catch is only here because it's a java checked exception.
        	}
        }
        
        if (danger != null) {
        	options.setRedRange(danger, max);
        }
        
        if (warn != null) {
        	if (danger != null) {
        		options.setYellowRange(warn, danger);
        	} else {
        		options.setYellowRange(warn, max);
        	}
        }

	    options.setHeight(-1);
	    options.setWidth(-1);
	    
	    DataTable data = getData();
	    data.removeColumn(0);
	    data.insertColumn(0, ColumnType.STRING, "LABEL");
	    data.insertColumn(1, ColumnType.NUMBER, "VALUE");
	    data.addRow();
	    data.setValue(0, 0, "Percent");
	    data.setValue(0, 1, 0);
	    chart = new Gauge(data, options);

	    chart.setTitle(spec.getProperty("field").toString());
	    
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

					DataTable data = getData();
					int rows = data.getNumberOfRows();

					if (rows > 0) {
						if (rows > 1) {
							data.removeRows(0, rows - 1);
						}

						//data.setValue(0, 0, "Percent");
					    chart.draw(data, options);
					}
				}
			}
		);
	}
}



