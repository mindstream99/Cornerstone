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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.Shape;

/**
 * 
 * @author Robert Englander
 *
 */
public class TabularFieldData implements Serializable{
	private static final long serialVersionUID = 1L;

	private Shape shape;
	private List<DataInstance> instances = new ArrayList<DataInstance>();
	
	public TabularFieldData(Shape shape) {
		this.shape = shape;
	}
	
	public Shape getShape() {
		return shape;
	}
	
	public void add(DataInstance inst) {
		instances.add(inst);
	}
	
	public List<DataInstance> getInstances() {
		return instances;
	}
}
