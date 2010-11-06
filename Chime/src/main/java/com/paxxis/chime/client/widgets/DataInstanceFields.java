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
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.paxxis.chime.client.InstanceUpdateListener;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.portal.DataRowModel;

/**
 *
 * @author Robert Englander
 */
public class DataInstanceFields extends LayoutContainer
{
    private DataInstance _instance = null;
    private InstanceUpdateListener updateListener;
    private boolean pendingData = false;
    private DataInstance pendingInstance; 
    private Shape pendingShape;
    private boolean pendingRefresh;
    private List<String> pendingExclusions = new ArrayList<String>();

    private ChimeGrid<DataFieldValueModel> grid = null;
    private ListStore<DataFieldValueModel> listStore;
    
    public DataInstanceFields(InstanceUpdateListener saveListener) {
        updateListener = saveListener;
    }
    
    public void onRender(Element parent, int index) {
    	super.onRender(parent, index);
        init();
        if (pendingData) {
        	pendingData = false;
        	setDataInstance(pendingInstance, pendingShape, pendingExclusions, pendingRefresh);
        }
    }
    
    private void init()
    {
        setLayout(new RowLayout());
        setStyleAttribute("backgroundColor", "white");
        setBorders(false);
        addListener(Events.Resize,
            new Listener<BoxComponentEvent>() {

                public void handleEvent(BoxComponentEvent evt) {
                    layout(true);
                }

            }
        );

        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        ColumnConfig column = new ColumnConfig();
        column.setId(DataFieldValueModel.EDIT);
        column.setFixed(true);
        column.setHeader("");
        column.setWidth(25);
        column.setSortable(false);
        column.setMenuDisabled(true);
        column.setRenderer(new DataEditGridCellRenderer());
        configs.add(column);
        
        column = new ColumnConfig();
        column.setId(DataFieldValueModel.NAME);
        column.setFixed(true);
        column.setHeader("");
        column.setWidth(125);
        column.setSortable(false);
        column.setMenuDisabled(true);
        column.setRenderer(new FieldDataGridCellRenderer());
        configs.add(column);
        
        column = new ColumnConfig();
        
        column.setId(DataRowModel.VALUE);
        column.setHeader("");
        column.setWidth(150);
        column.setSortable(false);
        column.setMenuDisabled(true);
        column.setRenderer(new FieldDataGridCellRenderer());
        configs.add(column);
        
        ColumnModel cm = new ColumnModel(configs);
        
        listStore = new ListStore<DataFieldValueModel>();
        grid = new ChimeGrid<DataFieldValueModel>(listStore, cm);
        grid.getView().setAutoFill(true);
        grid.setSelectionModel(null); 
        grid.getView().setForceFit(true);
        grid.setHideHeaders(true);
        grid.setTrackMouseOver(false);
        grid.setStripeRows(false);
        grid.setAutoExpandColumn(DataRowModel.VALUE);
        grid.setAutoHeight(true);
        
        add(grid, new RowData(1, -1, new Margins(0)));
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

    public void setDataInstance(DataInstance instance, Shape shape) {
        setDataInstance(instance, shape, null, true);
    }
    
    public void setDataInstance(DataInstance instance, Shape shape, List<String> excludedFields, boolean refresh) {
    	if (!isRendered()) {
    		pendingData = true;
    		pendingInstance = instance;
    		pendingShape = shape;
    		pendingRefresh = refresh;
    		if (excludedFields != null) {
    			pendingExclusions.addAll(excludedFields);
    		}
    		
    		return;
    	}

    	update(instance, _instance, shape, excludedFields, refresh);

        _instance = instance;
    }

    private void update(DataInstance newInstance, DataInstance oldInstance, Shape type, 
    		List<String> excludedFields, boolean refresh) {

    	boolean refreshGrid = refresh;
    	
        // we want to keep the fields that were also in the old instance; remove those
        // that don't exist anymore; and add the new ones.
        
        // TODO for now we aren't handling the case where the data type definition
        // has changed.  so for now there's no reason to remove the fields.  instead,
        // add them if this is the first time.  actually, if this is a different instance
        // then we do remove the fields and start over
        boolean startOver = oldInstance == null || !newInstance.getId().equals(oldInstance.getId());
        if (startOver) {
            listStore.removeAll();

            List<DataField> fields = type.getFields();
            for (DataField field : fields) {
            	
                if (!field.isPrivate() && !excludedFields.contains(field.getName())) {
                	DataFieldValueModel model = new DataFieldValueModel(newInstance, type, field, updateListener);
                	listStore.add(model);
                }
            }
            
            listStore.commitChanges();
            refreshGrid = true;
        } else {
        	for (DataFieldValueModel model : listStore.getModels()) {
        		model.update(newInstance);
        	}
        	listStore.commitChanges();
        }

        
        if (refreshGrid) {
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
        
    }
    
}

