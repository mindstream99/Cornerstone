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

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;

/**
 * 
 * @author Robert Englander
 *
 */
public class ChimeListStore<M extends ModelData> extends ListStore<M> {

	private boolean filtered = false;
	
	public ChimeListStore() {
		super();
	}
	
	public ChimeListStore(boolean filtered) {
		super();
		this.filtered = filtered;
	}
	
	public void filter(String a) {
		if (filtered) {
			super.filter(a);
		}
	}
	
	public void filter(String a, String b) {
		if (filtered) {
			super.filter(a, b);
		}
	}
}
