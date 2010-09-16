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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.charts.client.Chart;
import com.extjs.gxt.charts.client.model.ChartModel;
import com.extjs.gxt.charts.client.model.Legend;
import com.extjs.gxt.charts.client.model.Legend.Position;
import com.extjs.gxt.charts.client.model.axis.XAxis;
import com.extjs.gxt.charts.client.model.axis.YAxis;
import com.extjs.gxt.charts.client.model.charts.BarChart;
import com.extjs.gxt.charts.client.model.charts.FilledBarChart;
import com.extjs.gxt.charts.client.model.charts.PieChart;
import com.extjs.gxt.charts.client.model.charts.BarChart.BarStyle;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.cal.IValue;
import com.paxxis.chime.client.common.cal.Table;
import com.paxxis.chime.client.editor.ChartUtils.ChartType;


/**
 * 
 * @author Robert Englander
 *
 */
class TableRowModel extends BaseTreeModel implements Serializable {

    public TableRowModel(Table table, int idx) {
        List<IValue> row = table.getRow(idx);

        // the column header index defines the property names
        int cnt = table.columnCount();
        for (int i = 0; i < cnt; i++) {
            set(String.valueOf(i), row.get(i).valueAsString());
        }
    }
}

/**
 *
 * @author Robert Englander
 */
public class AnalyticDetailPanel extends LayoutContainer {

    private DataInstance dataInstance;
    private InterceptedHtml scriptHtml;
    private LayoutContainer scriptContainer;
    private LayoutContainer resultsContainer;
    private LayoutContainer tableResultsContainer;
    private LayoutContainer textResultsContainer;
    private InterceptedHtml textResultsHtml;
    private boolean showScript;

    public AnalyticDetailPanel(DataInstance instance, int height, boolean showScript) {
        dataInstance = instance;
        setHeight(height);
        this.showScript = showScript;

        BorderLayout layout = new BorderLayout();
        setLayout(layout);

        int resultLeftBorder = 0;

        if (showScript) {
            resultLeftBorder = 2;
            scriptContainer = new LayoutContainer();
            scriptContainer.setStyleAttribute("backgroundColor", "white");
            scriptContainer.setBorders(true);
            scriptContainer.setScrollMode(Scroll.AUTOY);
            scriptContainer.setLayout(new RowLayout());
            scriptContainer.setStyleAttribute("font", "normal 12px arial, tahoma, sans-serif");

            scriptHtml = new InterceptedHtml();
            scriptContainer.add(scriptHtml, new RowData(1, -1));
            
            BorderLayoutData data = new BorderLayoutData(LayoutRegion.WEST, 350);
            data.setMargins(new Margins(0, 2, 0, 0));
            data.setCollapsible(true);
            data.setSplit(true);

            add(scriptContainer, data);
        }

        resultsContainer = new LayoutContainer();
        resultsContainer.setStyleAttribute("backgroundColor", "#f1f1f1");
        resultsContainer.setBorders(true);
        resultsContainer.setLayout(new CardLayout());

        tableResultsContainer = new LayoutContainer();
        tableResultsContainer.setLayout(new FitLayout());
        resultsContainer.add(tableResultsContainer);

        textResultsContainer = new LayoutContainer();
        resultsContainer.add(textResultsContainer);
        textResultsHtml = new InterceptedHtml();
        textResultsContainer.add(textResultsHtml, new RowData(1, -1));

        ((CardLayout)resultsContainer.getLayout()).setActiveItem(textResultsContainer);

        BorderLayoutData data = new BorderLayoutData(LayoutRegion.CENTER);
        data.setMargins(new Margins(0, 0, 0, resultLeftBorder));
        data.setCollapsible(false);
        data.setSplit(true);

        add(resultsContainer, data);

        if (showScript) {
            DataField field = dataInstance.getShapes().get(0).getField("Script");
            List<DataFieldValue> vals = dataInstance.getFieldValues(dataInstance.getShapes().get(0), field);
            if (vals.size() > 0) {
                String script = vals.get(0).getName();
                scriptHtml.setHtml(script);
            } else {
                scriptHtml.setHtml("No Script Available");
            }
        }
    }

    public void showResult(String text) {
        textResultsHtml.setHtml(text);
        textResultsContainer.layout();
        ((CardLayout)resultsContainer.getLayout()).setActiveItem(textResultsContainer);
    }

    public void showChartResult(Table table, ChartType type, String title, int axisCol, int valueCol,
    		Integer minValue, Integer maxValue) {
        //tableResultsContainer.removeAll();
    	
        String url = "./resources/chart/open-flash-chart.swf";        
        Chart chart;
        if (tableResultsContainer.getItemCount() == 0) {
            chart = new Chart(url);  
            chart.setBorders(true);  
            tableResultsContainer.add(chart);
            tableResultsContainer.layout();
        } else {
        	chart = (Chart)tableResultsContainer.getItem(0);
        }

        int aCol = axisCol - 1;
        int vCol = valueCol - 1;
        switch (type) {
	        case Pie:
	            chart.setChartModel(getPieChartData(table, title, aCol, vCol));  
	            break;
	        case Bar:
	            chart.setChartModel(getBarChartData(table, title, aCol, vCol, minValue, maxValue));  
	            break;
	        case Bar3D:
	            chart.setChartModel(getBar3DChartData(table, title, aCol, vCol, minValue, maxValue));  
	            break;
        }
        
        ((CardLayout)resultsContainer.getLayout()).setActiveItem(tableResultsContainer);
    }
    
    private ChartModel getBar3DChartData(Table table, String title, int axisCol, int valueCol, Integer minVal, Integer maxVal) {  
    	ChartModel cm = new ChartModel(title,  
			"font-size: 14px; font-family: Verdana; color:#ffff00;");  
		cm.setBackgroundColour("#000077");  
    	int rowCount = table.rowCount();
		XAxis xa = new XAxis();  
		YAxis ya = new YAxis();
		double maxValue = 0.0;
		if (maxVal != null) {
			maxValue = maxVal;
		}

		double minValue = 0.0;
		if (minVal != null) {
			minValue = minVal;
		}
		
		List<Number> values = new ArrayList<Number>();
		for (int i = 0; i < rowCount; i++) {
    		String text = table.getRow(i).get(axisCol).valueAsString();
    		xa.addLabels(text);  

    		double value = table.getRow(i).get(valueCol).valueAsDouble();
    		values.add(value);
    		
    		if (maxVal == null && value > maxValue) {
    			maxValue = value;
    		}
    		
    		if (minVal == null && value < minValue) {
    			minValue = value;
    		}
		}  
		
		xa.getLabels().setColour("#ffff00");  
		xa.setGridColour("-1");  
		xa.setColour("#aa5500");  
		xa.setZDepth3D(5);  
		cm.setXAxis(xa);
		
		ya.setMax(maxValue);
		ya.setMin(minValue);
		ya.setGridColour("-1");  
		ya.setColour("#ffff00");  
		cm.setYAxis(ya);  
		
		BarChart bchart = new BarChart(BarStyle.THREED);  
		bchart.setColour("#CC6600");  
		bchart.setTooltip("#val#");
		
		bchart.addValues(values);

		cm.addChartConfig(bchart);  
    	return cm;  
    }      
    
    private ChartModel getBarChartData(Table table, String title, int axisCol, int valueCol, Integer minVal, Integer maxVal) {  
    	ChartModel cm = new ChartModel(title,  
		"font-size: 14px; font-family: Verdana; color:#ffff00;");  
		cm.setBackgroundColour("#000077");  
		int rowCount = table.rowCount();
		XAxis xa = new XAxis();  
		YAxis ya = new YAxis();
		double maxValue = 0.0;
		if (maxVal != null) {
			maxValue = maxVal;
		}
		
		double minValue = 0.0;
		if (minVal != null) {
			minValue = minVal;
		}
	
		List<Number> values = new ArrayList<Number>();
		for (int i = 0; i < rowCount; i++) {
			String text = table.getRow(i).get(axisCol).valueAsString();
			xa.addLabels(text);  
	
			double value = table.getRow(i).get(valueCol).valueAsDouble();
			values.add(value);
    		
    		if (maxVal == null && value > maxValue) {
    			maxValue = value;
    		}
    		
    		if (minVal == null && value < minValue) {
    			minValue = value;
    		}
		}  
		
		xa.getLabels().setColour("#ffff00");  
		xa.setGridColour("-1");  
		xa.setColour("#aa5500");  
		cm.setXAxis(xa);  

		ya.setMax(maxValue);
		ya.setMin(minValue);
		ya.setGridColour("-1");  
		ya.setColour("#ffff00");  
		cm.setYAxis(ya);  
		
		FilledBarChart bchart = new FilledBarChart();  
		bchart.setColour("#CC6600");  
		bchart.setTooltip("#val#");
		
		bchart.addValues(values);
	
		cm.addChartConfig(bchart);  
		return cm;  
    }      
    
    private ChartModel getPieChartData(Table table, String title, int axisCol, int valueCol) {  
    	ChartModel cm = new ChartModel(title,  
    		"font-size: 14px; font-family: Verdana; text-align: center;");  
    	cm.setBackgroundColour("#fffff5");  
    	Legend lg = new Legend(Position.RIGHT, true);  
    	lg.setPadding(10);  
    	//cm.setLegend(lg);  
    	       
    	PieChart pie = new PieChart();  
    	pie.setAlpha(0.5f);  
    	pie.setNoLabels(false);  
    	//pie.setTooltip("#label# $#val#K<br>#percent#");  
    	pie.setTooltip("#label# #val#");  
    	pie.setColours("#ff0000", "#00aa00", "#0000ff", "#ff9900", "#ff00ff");  
    	
    	int rowCount = table.rowCount();
    	for (int i = 0; i < rowCount; i++) {
    		String text = table.getRow(i).get(axisCol).valueAsString();
    		double value = table.getRow(i).get(valueCol).valueAsDouble();
        	pie.addSlices(new PieChart.Slice(value, text, text));  
    	}
    	  
    	cm.addChartConfig(pie);  
    	return cm;  
    }      
    
    public void showResult(Table table) {
        tableResultsContainer.removeAll();

        ListStore<TableRowModel> store = new ListStore<TableRowModel>();

        for (int i = 0; i < table.rowCount(); i++) {
            TableRowModel model = new TableRowModel(table, i);
            store.add(model);
        }

        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        List<IValue> colNames = table.getColumnNames();
        for (int i = 0; i < colNames.size(); i++) {
            ColumnConfig column = new ColumnConfig();
            column.setId(String.valueOf(i));
            column.setHeader(colNames.get(i).valueAsString());
            column.setWidth(150);
            configs.add(column);
        }

        ColumnModel cm = new ColumnModel(configs);
        Grid<TableRowModel> grid = new Grid<TableRowModel>(store, cm);
        grid.setStyleAttribute("borderTop", "none");
        grid.setAutoExpandColumn(String.valueOf(colNames.size() - 1));
        grid.setBorders(true);

        tableResultsContainer.add(grid);
        tableResultsContainer.layout();
        ((CardLayout)resultsContainer.getLayout()).setActiveItem(tableResultsContainer);
    }
}