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

import com.extjs.gxt.ui.client.util.Size;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.LegendPosition;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.visualizations.AreaChart;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.portal.PortletSpecification;

/**
 * 
 * @author Robert Englander
 *
 */
public class FieldDataChartPortlet extends PortletContainer {
	private AreaChart chart;
    private AreaChart.Options options = AreaChart.Options.create();
    private DataTable data = DataTable.create();

    private LayoutContainer container;

    public FieldDataChartPortlet(PortletSpecification spec) {
        super(spec, HeaderType.None, false);
    }

    protected void init() {
    	super.init();
    	getBody().setLayout(new RowLayout());
        container = new LayoutContainer();
        container.setBorders(false);
        container.setLayout(new RowLayout());
        getBody().add(container, new RowData(1, -1));

        options.setMax(100);
	    options.setMin(0);
	    options.setHeight(-1);
	    options.setWidth(-1);
	    options.setTitleFontSize(14.0); 
	    options.setTitle(getSpecification().getProperty("field").toString());
	    options.setLegend(LegendPosition.NONE);
	    options.setTitleY(getSpecification().getProperty("labelY").toString());
	    options.setAxisFontSize(12.0);
	    options.setEnableTooltip(true);
	    data.addColumn(ColumnType.NUMBER, "VALUE");
	    data.addRow();
	    data.setValue(0, 0, 0);
	    chart = new AreaChart(data, options);
	    
	    container.add(chart, new RowData(1, -1));
    }

    @Override
    protected void onResize(int width, int height) {
    	super.onResize(width, height);

    	int aw = width;

    	Size frameWidth = el().getFrameSize();
    	aw -= (15 + frameWidth.width);

		options.setHeight(aw);
		options.setWidth(aw);
	    chart.draw(data, options);
   }

    public void setDataInstance(final DataInstance instance, final UpdateReason reason) {

        Runnable r = new Runnable() {
        	public void run() {
				data = DataTable.create();
				String specField = getSpecification().getProperty("field").toString();
				
			    data.addColumn(ColumnType.NUMBER, specField);

			    DataField field = instance.getShapes().get(0).getField(specField);
			    List<DataFieldValue> values = instance.getFieldValues(instance.getShapes().get(0), field);
			    int row = 0;
			    for (DataFieldValue value : values) {
				    data.addRow();
				    double dval = Double.parseDouble(value.getName());
				    data.setValue(row, 0, dval);
				    row++;
			    }
			    
			    chart.draw(data, options);
        		
        		//container.layout();
         	}
        };
    	
    	if (isRendered()) {
    		r.run();
    	} else {
    		addPostRenderRunnable(r);
    	}
   }
}
