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

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.paxxis.chime.client.DataFieldModel;
import com.paxxis.chime.client.InstanceUpdateListener;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.Shape;

/**
 *
 * @author Robert Englander
 */
public class ShapeFields extends ChimeLayoutContainer
{
    private Shape shape = null;
    private InstanceUpdateListener saveListener;
    private ChimeGrid<DataFieldModel> grid;
    private ListStore<DataFieldModel> listStore;
    
    public ShapeFields(InstanceUpdateListener saveListener) {
        this.saveListener = saveListener;
    }

    protected void init() {
        setLayout(new RowLayout());
        setBorders(false);

        listStore = new ListStore<DataFieldModel>();
        
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        String[] cols = {
        	DataFieldModel.NAME,
        	DataFieldModel.DESCRIPTION,
        	DataFieldModel.SHAPE,
        	DataFieldModel.MAXVALUES,
        	DataFieldModel.FORMAT
        };
        
        for (String col : cols) {
            ColumnConfig column = new ColumnConfig();
            column.setId(col);
            column.setFixed(false);
            column.setHeader(col);
            column.setWidth(150);
            column.setSortable(false);
            column.setMenuDisabled(true);
            column.setRenderer(new DataFieldCellRenderer(true));
            configs.add(column);
        }

        ColumnModel cm = new ColumnModel(configs);
        grid = new ChimeGrid<DataFieldModel>(listStore, cm, true, false);
        grid.getView().setAutoFill(false);
        grid.setSelectionModel(null);
        grid.getView().setForceFit(false);
        grid.getView().setShowDirtyCells(false);
        grid.setHideHeaders(false);
        grid.setTrackMouseOver(false);
        grid.setStripeRows(false);
        grid.setBorders(true);
        grid.setHeight(300);
        grid.setAutoExpandColumn(DataFieldModel.FORMAT);
        add(grid, new RowData(1, -1));
    }

    public void setDataInstance(Shape sh) {
        shape = sh;
        Runnable r = new Runnable() {
        	public void run() {
                for (DataField field : shape.getFields()) {
                	DataFieldModel model = new DataFieldModel(field);
                	listStore.add(model);
                }
        	}
        };
        
        if (isRendered()) {
        	r.run();
        } else {
        	addPostRenderRunnable(r);
        }
    }
}





