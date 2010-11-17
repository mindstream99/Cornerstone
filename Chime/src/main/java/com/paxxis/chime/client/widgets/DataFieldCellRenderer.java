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

import com.extjs.gxt.ui.client.event.BoxComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.paxxis.chime.client.DataFieldModel;
import com.paxxis.chime.client.Utils;
import com.paxxis.chime.client.common.Shape;

/**
 * 
 * @author Robert Englander
 *
 */
public class DataFieldCellRenderer implements GridCellRenderer<DataFieldModel> {

	private boolean useInterceptedHtml = true;

	public DataFieldCellRenderer(boolean intercepted) {
		useInterceptedHtml = intercepted;
	}
	
	@Override
	public Object render(DataFieldModel model, String property,
			ColumnData config, int rowIndex, int colIndex,
			ListStore<DataFieldModel> store, Grid<DataFieldModel> grid) {

        Object result;
		if (property.equals(DataFieldModel.SHAPE)) {
			Shape shape = (Shape)model.get(property);
			
        	String txt = "";
			if (shape != null) {
	        	String name = shape.getName().replaceAll(" ", "&nbsp;");
	            
	        	if (useInterceptedHtml) {
	                txt = Utils.toHoverUrl(shape.getId(), name);
	            } else {
	                txt = name;
	            }
			}

        	LayoutContainer lc = new LayoutContainer();
			renderHtml(lc, txt);
			result = lc;
		} else {
			result = "";
			Object obj = model.get(property);
			if (obj != null) {
				result = obj.toString();
			}
		}
		
		return result;
	}

	private void renderHtml(final LayoutContainer lc, String content) {
		lc.setLayout(new RowLayout()); 
		final InterceptedHtml html = new InterceptedHtml();
		html.setHtml(content);
		
        lc.add(html, new RowData(1, -1));
        lc.addListener(Events.Resize,
            new Listener<BoxComponentEvent>() {
                public void handleEvent(BoxComponentEvent evt) {
                	DeferredCommand.addCommand(
                		new Command() {
                			public void execute() {
                            	lc.layout();
                            	html.setWidth(lc.getWidth());
                			}
                		}
                	);
                }
            }
        );
	}
}
