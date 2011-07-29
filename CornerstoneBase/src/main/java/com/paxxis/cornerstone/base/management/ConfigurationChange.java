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

package com.paxxis.cornerstone.base.management;

import java.io.Serializable;

/**
 * 
 * @author Robert Englander
 *
 */
public class ConfigurationChange implements Serializable {
	private static final long serialVersionUID = 1L;

	private String name;
	private Serializable oldValue;
	private Serializable newValue;
	
	public ConfigurationChange() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Serializable getOldValue() {
		return oldValue;
	}

	public void setOldValue(Serializable oldValue) {
		this.oldValue = oldValue;
	}

	public Serializable getNewValue() {
		return newValue;
	}

	public void setNewValue(Serializable newValue) {
		this.newValue = newValue;
	}

}
