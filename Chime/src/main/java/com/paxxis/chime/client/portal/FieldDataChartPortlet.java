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

import java.util.List;

import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.visualization.client.DataTable;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.portal.PortletSpecification;
import com.paxxis.chime.client.widgets.charts.ChimeChart;
import com.paxxis.chime.client.widgets.charts.ChimeChartFactory;

/**
 * 
 * @author Robert Englander
 *
 */
public class FieldDataChartPortlet extends PortletContainer {
	private ChimeChart chart;
	
    public FieldDataChartPortlet(PortletSpecification spec) {
        super(spec, HeaderType.None, false);
    }

    protected void init() {
    	super.init();
    	getBody().setLayout(new RowLayout());
    	chart = ChimeChartFactory.create(getSpecification());
    	getBody().add(chart, new RowData(1, -1));
    }

    public void setDataInstance(final DataInstance instance, final UpdateReason reason) {

        Runnable r = new Runnable() {
        	public void run() {
        		DataTable data = chart.resetData();
				
				String specField = getSpecification().getProperty("field").toString();
				
			    DataField field = instance.getShapes().get(0).getField(specField);
			    List<DataFieldValue> values = instance.getFieldValues(instance.getShapes().get(0), field);
			    int row = 0;
			    int col = chart.getValueColumn();
			    for (DataFieldValue value : values) {
				    data.addRow();
				    double dval = Double.parseDouble(value.getName());
				    data.setValue(row, col, round(dval, 2));
				    row++;
			    }
			    
			    chart.redraw();
         	}
        };
    	
    	if (isRendered()) {
    		r.run();
    	} else {
    		addPostRenderRunnable(r);
    	}
    }
    
	private double round(double input, int digits) {
		int precision = 10;
		for (int i = 1; i < digits; i++) {
			precision *= 10;
		}
		double result = Math.floor(input * precision +.5)/precision;
		return result;
	}
	
}
