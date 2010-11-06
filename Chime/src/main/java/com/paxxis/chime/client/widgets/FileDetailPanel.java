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
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.paxxis.chime.client.Utils;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.portal.DataRowModel;

/**
 *
 * @author Robert Englander
 */
public class FileDetailPanel extends LayoutContainer {

    private DataInstance dataInstance;
    private ChimeGrid<DataRowModel> grid;
    private ListStore<DataRowModel> listStore;

    public FileDetailPanel(DataInstance instance) {
        super();
        dataInstance = instance;
        init();
        update();
    }

    private void init() {

    	LayoutContainer cont = new LayoutContainer();
    	cont.setLayout(new RowLayout());
    	
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        ColumnConfig column = new ColumnConfig();
        column.setId(DataRowModel.NAME);
        column.setFixed(true);
        column.setHeader("");
        column.setWidth(150);
        column.setSortable(false);
        column.setMenuDisabled(true);
        column.setRenderer(new FieldDataGridCellRenderer());
        configs.add(column);
        
        column = new ColumnConfig();
        column.setId(DataRowModel.VALUE);
        column.setHeader("");
        column.setWidth(300);
        column.setSortable(false);
        column.setMenuDisabled(true);
        column.setRenderer(new FieldDataGridCellRenderer());
        configs.add(column);
        
        ColumnModel cm = new ColumnModel(configs);
        
        listStore = new ListStore<DataRowModel>();
        grid = new ChimeGrid<DataRowModel>(listStore, cm);
        grid.getView().setAutoFill(true);
        grid.setSelectionModel(
        	new GridSelectionModel<DataRowModel>() {
        		protected boolean isSelectable(int row, int cell, boolean acceptsNav) {		        	
        			return false;
        		}
        	}
        );
        grid.getView().setForceFit(true);
        grid.setHideHeaders(true);
        grid.setTrackMouseOver(false);
        grid.setAutoHeight(true);
        grid.setAutoExpandColumn(DataRowModel.VALUE);
        
        cont.add(grid, new RowData(1, -1, new Margins(0)));

        add(cont, new RowData(1, -1));
        addListener(Events.Resize,
            new Listener<BoxComponentEvent>() {
                public void handleEvent(BoxComponentEvent evt) {
                	DeferredCommand.addCommand(
                		new Command() {
                			public void execute() {
                                layout();
                                grid.setWidth(getWidth());
                                grid.getView().refresh(false);
                			}
                		}
                	);
                }
            }
        );
    }

    public void update() {
        listStore.removeAll();
    	DataRowModel model = new DataRowModel(DataRowModel.NAME, "<b>MIME Type:</b>");
    	model.set(DataRowModel.VALUE, getMimeType());
    	listStore.add(model);
        
    	model = new DataRowModel(DataRowModel.NAME, "<b>File Size:</b>");
    	model.set(DataRowModel.VALUE, getFileSize());
    	listStore.add(model);
        
    	model = new DataRowModel(DataRowModel.NAME, "<b>Download:</b>");
    	model.set(DataRowModel.VALUE, getDownloadLink());
    	listStore.add(model);
    	
        listStore.commitChanges();

        // I don't know why this is necessary, but for some reason the grid is not sizing its
        // width correctly.
        DeferredCommand.addCommand(
        	new Command() {
        		public void execute() {
        	    	grid.setWidth(getWidth());
                	grid.getView().refresh(false);
        		}
        	}
        );
    }

    private String getMimeType() {
        Shape type = dataInstance.getShapes().get(0);
        DataField field = type.getField("MIME Type");
        List<DataFieldValue> values = dataInstance.getFieldValues(type, field);

        String result = "Unknown";
        if (values.size() > 0) {
            result = values.get(0).getValue().toString();
        }

        return result;
    }

    private String getFileSize() {
        Shape type = dataInstance.getShapes().get(0);
        DataField field = type.getField("Size");
        List<DataFieldValue> values = dataInstance.getFieldValues(type, field);

        String result = "Unknown";
        if (values.size() > 0) {
            result = values.get(0).getValue().toString();

            try {
                double sz = Double.valueOf(result);
                result = "" + (long)sz;
            } catch (Exception e) {
                result = e.getMessage();
            }
        }

        return result + " Bytes";
    }

    private String getDownloadLink() {
        Shape type = dataInstance.getShapes().get(0);
        DataField field = type.getField("File ID");
        List<DataFieldValue> values = dataInstance.getFieldValues(type, field);

        String result = "";
        if (values.size() > 0) {
            String fileName = dataInstance.getName();
            if (-1 == fileName.indexOf(".")) {
                List<DataFieldValue> vals = dataInstance.getFieldValues(type, type.getField("Extension"));
                if (vals.size() > 0) {
                    fileName += ("." + vals.get(0).getValue());
                }
            }

            String id = values.get(0).getValue().toString();
            String link = GWT.getHostPageBaseURL().trim();
            if (link.endsWith("/")) {
                link = link.substring(0, link.length() - 1);
            }
            
            link += "/FileManager/" + fileName + "?id=" + id;
            result = Utils.toExternalUrl(link, "Click here...");
        }

        return result;
    }
}
