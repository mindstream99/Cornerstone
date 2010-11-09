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
import java.util.Date;
import java.util.List;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.cal.DateVariable;
import com.paxxis.chime.client.common.cal.IValue;
import com.paxxis.chime.client.common.cal.Table;
import com.paxxis.chime.client.common.constants.TextConstants;
import com.paxxis.chime.client.widgets.charts.ChimeChart;
import com.paxxis.chime.client.widgets.charts.ChimeChartFactory;
import com.paxxis.chime.client.widgets.charts.ChimeChartFactory.ChartType;


/**
 * 
 * @author Robert Englander
 *
 */
class TableRowModel extends BaseTreeModel implements Serializable {
	private static final long serialVersionUID = 1L;

	public TableRowModel(Table table, int idx) {
        List<IValue> row = table.getRow(idx);

        // the column header index defines the property names
        int cnt = table.columnCount();
        for (int i = 0; i < cnt; i++) {
        	IValue iVal = row.get(i);
        	String displayText;
        	if (iVal instanceof DateVariable) {
                DateTimeFormat dtf = DateTimeFormat.getFormat("MMM d, yyyy");
                displayText = dtf.format((Date)iVal.valueAsObject());
        	} else {
        		displayText = iVal.valueAsString();        	}

        	set(String.valueOf(i), displayText);
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

    public AnalyticDetailPanel(DataInstance instance, int height, boolean showScript) {
        dataInstance = instance;
        setHeight(height);

        BorderLayout layout = new BorderLayout();
        setLayout(layout);

        int resultLeftBorder = 0;

        if (showScript) {
            resultLeftBorder = 2;
            scriptContainer = new LayoutContainer();
            scriptContainer.setStyleAttribute("backgroundColor", "white");
            scriptContainer.setBorders(true);
            scriptContainer.setScrollMode(Scroll.AUTO);
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
            	// replace encoded newlines, and replace all whitespace with non breaking spaces so that
            	// the panel won't wrap the code
            	String script = vals.get(0).getValue().toString()
                	.replaceAll(TextConstants.NEWLINE, "<br>")
                	.replaceAll(" ", "&nbsp;")
                	.trim();
            	if (script.isEmpty()) {
                    scriptHtml.setHtml("No Script Available");
            	} else {
                    scriptHtml.setHtml(script);
            	}
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

    public void showChartResult(final Table table, ChartType type, String title, int axisCol, int valueCol,
    		Integer minValue, Integer maxValue) {
    	
        if (tableResultsContainer.getItemCount() == 0) {
        	final ChimeChart chart = ChimeChartFactory.create(type, title, axisCol - 1, 
        			valueCol - 1, minValue, maxValue);  
            chart.setBorders(true);  
            tableResultsContainer.add(chart, new RowData(1, 1));
            tableResultsContainer.layout();

            DeferredCommand.addCommand(
            	new Command() {
            		public void execute() {
            	        chart.update(table);
            		}
            	}
            );
        } else {
        	ChimeChart chart = (ChimeChart)tableResultsContainer.getItem(0);
            chart.update(table);
        }

        
        ((CardLayout)resultsContainer.getLayout()).setActiveItem(tableResultsContainer);
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
            column.setSortable(false);
            column.setMenuDisabled(true);
            configs.add(column);
        }

        ColumnModel cm = new ColumnModel(configs);
        ChimeGrid<TableRowModel> grid = new ChimeGrid<TableRowModel>(store, cm);
        grid.setStyleAttribute("borderTop", "none");
        grid.setAutoExpandColumn(String.valueOf(colNames.size() - 1));
        grid.getView().setAutoFill(true);
        grid.setBorders(true);

        tableResultsContainer.add(grid, new RowData(1, 1));
        tableResultsContainer.layout();
        ((CardLayout)resultsContainer.getLayout()).setActiveItem(tableResultsContainer);
    }
}