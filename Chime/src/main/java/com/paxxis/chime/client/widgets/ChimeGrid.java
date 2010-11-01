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

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.dnd.GridDragSource;
import com.extjs.gxt.ui.client.dnd.GridDropTarget;
import com.extjs.gxt.ui.client.dnd.DND.Feedback;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;

/**
 * 
 * @author Robert Englander
 *
 */
public class ChimeGrid<M extends ModelData> extends Grid<M> {

	public ChimeGrid(ListStore<M> store, ColumnModel cm) {
		this(store, cm, false);
	}
	
	public ChimeGrid(ListStore<M> store, ColumnModel cm, boolean canReorder) {
		super(store, cm);
	    addStyleName("chimeGrid");
	    
	    if (canReorder) {
            GridDropTarget target = new GridDropTarget(this);
            target.setFeedback(Feedback.INSERT);
            target.setAllowSelfAsSource(true);
            GridDragSource source = new GridDragSource(this);
        }
	}

}
