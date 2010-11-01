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

import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.paxxis.chime.client.portal.DataRowModel;

/**
 * 
 * @author Robert Englander
 *
 */
public class DataDeleteGridCellRenderer implements GridCellRenderer<DataRowModel> {

    public DataDeleteGridCellRenderer() {
    }

    @Override
    public Object render(final DataRowModel model, final String property,
                    ColumnData config, final int rowIndex, final int colIndex,
                    final ListStore<DataRowModel> store, final Grid<DataRowModel> grid) {

	    IconButton deleteBtn = new IconButton("delete-icon");
	    deleteBtn.addSelectionListener(
	        new SelectionListener<IconButtonEvent>() {
	            @Override
	            public void componentSelected(IconButtonEvent evt) {
                    store.remove(model);
	            }
	        }
	    );
	
	    LayoutContainer lc = new LayoutContainer();
	    lc.setLayout(new RowLayout());
	    ToolBar bar = new ToolBar();
	    bar.setBorders(false);
	    bar.setSpacing(6);
	    bar.setStyleAttribute("background", "transparent");
	    bar.add(deleteBtn);
	    lc.add(bar, new RowData(1, -1));
        return lc;
    }
}

