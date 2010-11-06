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

import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.dnd.GridDragSource;
import com.extjs.gxt.ui.client.dnd.GridDropTarget;
import com.extjs.gxt.ui.client.dnd.DND.Feedback;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridView;

/**
 * 
 * @author Robert Englander
 *
 */
public class ChimeGrid<M extends ModelData> extends Grid<M> {

	// the standard Grid doesn't provide a selection model that prohibits
	// selection.  Passing null to setSelectionModel of the grid class
	// will result in null pointer exceptions in its afterRenderView method.
	// To provide for null selection, ChimeGrid overrides the afterRenderView method.
	// Due to the fact that we can't call the GridView methods directly, the ChimeGridView
	// class is used in order to make those calls possible.
	static class ChimeGridView extends GridView {
		public void afterRender() {
			super.afterRender();
		}
		
		public void onRowSelect(int idx) {
			super.onRowSelect(idx);
		}
	}
	
	public ChimeGrid(ListStore<M> store, ColumnModel cm) {
		this(store, cm, true, false);
	}
	
	public ChimeGrid(ListStore<M> store, ColumnModel cm, boolean autoCellHeight, boolean canReorder) {
		super(store, cm);
		if (autoCellHeight) {
		    addStyleName("chimeGrid");
		}
	    
	    if (canReorder) {
            GridDropTarget target = new GridDropTarget(this);
            target.setFeedback(Feedback.INSERT);
            target.setAllowSelfAsSource(true);
            new GridDragSource(this);
        }
	    
	    setView(new ChimeGridView());
	}

	/**
	 * Overridden so that a null selection model gives us the behavior
	 * we want.
	 */
	protected void afterRenderView() {
		ChimeGridView vw = (ChimeGridView)getView();
		viewReady = true;
	    vw.afterRender();
	    onAfterRenderView();

	    if (sm != null) {
		    List<M> list = sm.getSelectedItems();
		    for (M m : list) {
		      vw.onRowSelect(store.indexOf(m));
		    }
	    }

	    fireEvent(Events.ViewReady);
	  }	
}
