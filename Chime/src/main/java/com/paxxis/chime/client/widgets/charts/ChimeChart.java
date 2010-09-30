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

import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.paxxis.chime.client.widgets.ChimeLayoutContainer;

/**
 * This is the base class for all Chime gwt-visualization wrappers.  It provides the
 * standard chime layout container functionality, and provides the base mgmt of the
 * chart's data table.
 * 
 * @author Robert Englander
 *
 */
public abstract class ChimeChart extends ChimeLayoutContainer {
	private DataTable data = null;

	public ChimeChart() {
		resetData();
	}
	
	/**
	 * Each chart wrapper must implement its own redraw behavior.
	 */
	public abstract void redraw();
	
	/**
	 * Gets the column index into the data table that contains the
	 * actual value.  Other columns are used by the individual chart
	 * types for their own purpose, and not all charts use column 0 for
	 * the data.  Those that use something other than 0 should override
	 * this method.
	 */
	public int getValueColumn() {
		return 0;
	}

	/**
	 * Resets the data table to its default empty state.
	 */
	public DataTable resetData() {
		data = DataTable.create();
	    data.addColumn(ColumnType.NUMBER, "VALUE");
		return getData();
	}

	public DataTable getData() {
		return data;
	}
}
