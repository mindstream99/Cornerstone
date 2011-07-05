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

package com.paxxis.cornerstone.base.monitoring;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Associates a metric name with a bounded list of timestamped values.
 * 
 * @author Rob Englander
 *
 */
public class ServiceMetrics implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	private List<TimestampedValue> values = new ArrayList<TimestampedValue>();
	private int maxValues;
	
	public ServiceMetrics() {
		this("unknown");
	}
	
	public ServiceMetrics(String name) {
		this(name, 1);
	}

	public ServiceMetrics(String name, int maxValues) {
		this.name = name;
		
		if (maxValues < 0) {
			throw new IllegalArgumentException("maxValues must be greater than 0.");
		}
		
		this.maxValues = maxValues;
	}
	
	public String getName() {
		return name;
	}
	
	public List<TimestampedValue> getValues() {
		return values;
	}
	
	public void addValue(TimestampedValue value) {
		if (values.size() == maxValues) {
			values.remove(0);
		}
		
		values.add(value);
	}
	
	public void setValues(List<TimestampedValue> vals) {
		values.clear();
		int last = vals.size() - 1;
		if (last >= maxValues) {
			last = maxValues - 1;
		}
		
		values.addAll(vals.subList(0, last));
	}
}
