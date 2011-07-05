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
import java.util.Date;

/**
 * A value and its creation timestamp.
 * 
 * @author Rob Englander
 *
 */
public class TimestampedValue implements Serializable {

	private static final long serialVersionUID = 1L;

	/** the value */
	private Serializable value;
	
	/** the timestamp */
	private Date timestamp;
	
	public TimestampedValue() {
		this("");
	}
	
	public TimestampedValue(Serializable value) {
		this(value, new Date());
	}
	
	public TimestampedValue(Serializable value, Date timestamp) {
		this.value = value;
		this.timestamp = timestamp;
	}
	
	public Serializable getValue() {
		return value;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
}
