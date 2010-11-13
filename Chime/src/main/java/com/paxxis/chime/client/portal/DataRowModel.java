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

package com.paxxis.chime.client.portal;

import java.io.Serializable;

import com.extjs.gxt.ui.client.data.BaseTreeModel;

/**
 * This is a simple name/value pair model for use in grids that don't render
 * Chime specific data instances.
 * 
 * @author Robert Englander
 *
 */
public class DataRowModel extends BaseTreeModel implements Serializable {

	public static final String NAME = "name";
	public static final String VALUE = "value";
	public static final String BLANK = "blank";

	private static final long serialVersionUID = 1L;

    public DataRowModel() {
    }

    public DataRowModel(String name, String value) {
        set(name, value);
    }
}