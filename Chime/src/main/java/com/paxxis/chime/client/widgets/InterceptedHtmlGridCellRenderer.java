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

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.paxxis.chime.client.portal.DataRowModel;

/**
 * A grid cell renderer that uses the Chime InterceptedHtml to render content, allowing internal
 * Chime URLs in the content to provide the proper navigation and hover behavior.
 *  
 * @author Robert Englander
 *
 */
public class InterceptedHtmlGridCellRenderer implements GridCellRenderer<DataRowModel> {

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

		// put the html component inside of a basic layout container so that
		// the margins can be applied.
		final InterceptedHtml html = new InterceptedHtml();
		html.setHtml(model.get(property).toString());
		html.setWordWrap(true);
		LayoutContainer lc = new LayoutContainer();
		lc.setLayout(new RowLayout());
		lc.add(html, new RowData(1, -1, margins));
		return lc;
	}


}
