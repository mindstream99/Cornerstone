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

package com.paxxis.chime.client;

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

/**
 * 
 * @author Robert Englander
 *
 */
public class PaginatorContainer extends LayoutContainer {

	public PaginatorContainer(Paginator paginator) {
		init(paginator);
	}
	
	private void init(Paginator paginator) {
        setLayout(new RowLayout());
        setStyleAttribute("background", "transparent url(resources/images/gray/panel/white-top-bottom.gif)");
        setStyleAttribute("border", "0pt none");
        setStyleAttribute("padding-left", "3px");
        setStyleAttribute("color", "black");
        add(paginator, new RowData(-1, -1, new Margins(2)));
	}
}
