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

import com.extjs.gxt.ui.client.event.BoxComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.portal.DataRowModel;

/**
 * A grid cell renderer that uses the Chime InterceptedHtml to render content, allowing internal
 * Chime URLs in the content to provide the proper navigation and hover behavior.
 *  
 * @author Robert Englander
 *
 */
public class InterceptedHtmlGridCellRenderer implements GridCellRenderer<DataRowModel> {
	private static final int MINGRIDHEIGHT = 40;
	private static final int MAXGRIDHEIGHT = 240;
	private static final int GRIDROWHEIGHT = 20;
	
	/** the default margins for placing the renderer in the cell */
	private Margins margins = new Margins(3, 3, 3, 0);
	
	public InterceptedHtmlGridCellRenderer() {
	}
	
	/**
	 * Constructor
	 * @param m the margins to use when placing the renderer in the cell.
	 */
	public InterceptedHtmlGridCellRenderer(Margins m) {
		margins = m;
	}
	
	@Override
	public Object render(DataRowModel model, String property,
			ColumnData config, int rowIndex, int colIndex,
			ListStore<DataRowModel> store, Grid<DataRowModel> grid) {

		// put the component inside of a basic layout container so that
		// the margins can be applied.
		final LayoutContainer lc = new LayoutContainer();
		//lc.setLayout(new RowLayout());

		Object obj = model.get(property);
		if (obj instanceof TabularFieldData) {
			TabularFieldData tabData = (TabularFieldData)obj;
			lc.setLayout(new RowLayout());
	        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

	        Shape shape = tabData.getShape();
	        String lastColId = "";
	        List<DataField> dataFields = shape.getFields();
	        for (DataField field : dataFields) {
		        ColumnConfig column = new ColumnConfig();
		        column.setId(field.getName());
		        column.setFixed(false);
		        column.setHeader(field.getName());
		        column.setWidth(150);
		        column.setSortable(false);
		        column.setMenuDisabled(true);
		        column.setRenderer(new InterceptedHtmlGridCellRenderer());
		        configs.add(column);
		        lastColId = field.getName();
	        }

	        ColumnModel cm = new ColumnModel(configs);
	        ListStore<DataFieldModel> listStore = new ListStore<DataFieldModel>();
	        final Grid<DataFieldModel> fieldGrid = new Grid<DataFieldModel>(listStore, cm);
	        fieldGrid.getView().setAutoFill(false);
	        fieldGrid.setSelectionModel(null);
	        fieldGrid.getView().setForceFit(false);
	        fieldGrid.setHideHeaders(false);
	        fieldGrid.setTrackMouseOver(false);
	        fieldGrid.setStripeRows(true);
	        fieldGrid.setAutoExpandColumn(lastColId);
	        fieldGrid.setAutoHeight(false);
	        fieldGrid.setBorders(true);
	        
	        lc.add(fieldGrid, new RowData(1, -1, margins));
	        lc.addListener(Events.Resize,
	            new Listener<BoxComponentEvent>() {
	                public void handleEvent(BoxComponentEvent evt) {
	                	DeferredCommand.addCommand(
	                		new Command() {
	                			public void execute() {
	                            	lc.layout();
	                            	fieldGrid.setWidth(lc.getWidth());
	                            	fieldGrid.getView().refresh(false);
	                			}
	                		}
	                	);
	                }
	            }
	        );

	        List<DataInstance> instances = tabData.getInstances();
	        int gridHeight = 0;
	        for (DataInstance instance : instances) {
		        for (DataField field : dataFields) {
		        	DataFieldModel fieldModel = new DataFieldModel(instance, shape, field, null);
		        	listStore.add(fieldModel);
		        }
	        	
	        	gridHeight += GRIDROWHEIGHT;
	        }
	        
	        if (gridHeight < MINGRIDHEIGHT) {
	        	gridHeight = MINGRIDHEIGHT;
	        } else if (gridHeight > MAXGRIDHEIGHT) {
	        	gridHeight = MAXGRIDHEIGHT;
	        }
	        
	        fieldGrid.setHeight(gridHeight);
	        
		} else {
			InterceptedHtml html = new InterceptedHtml();
			html.setHtml(obj.toString());
			html.setWordWrap(true);
			lc.add(html, new FlowData(margins));
		}
		
		return lc;
	}


}
