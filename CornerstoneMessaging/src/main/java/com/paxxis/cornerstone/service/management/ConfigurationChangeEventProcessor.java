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

package com.paxxis.cornerstone.service.management;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.paxxis.cornerstone.base.management.ConfigurationChange;
import com.paxxis.cornerstone.base.management.ConfigurationChangeEvent;
import com.paxxis.cornerstone.database.DatabaseBackedConfiguration;
import com.paxxis.cornerstone.service.EventMessageProcessor;

/**
 * 
 * @author Rob Englander
 *
 */
public class ConfigurationChangeEventProcessor extends EventMessageProcessor<ConfigurationChangeEvent> {
	
	private List<DatabaseBackedConfiguration> configurations = new ArrayList<DatabaseBackedConfiguration>();
	
	public void setConfigurations(List<DatabaseBackedConfiguration> configurations) {
		this.configurations.clear();
		this.configurations.addAll(configurations);
	}
	
	public void initialize() {
	} 
	
	@Override
	protected void process(ConfigurationChangeEvent eventMessage) throws Exception {
		Collection<ConfigurationChange> changes = eventMessage.getConfigurationChanges();
		for (DatabaseBackedConfiguration configuration : configurations) {
			for (ConfigurationChange change : changes) {
				configuration.modifyParameter(change);
			}
		}
	}
	
	@Override
	public Class<ConfigurationChangeEvent> getRequestMessageClass() {
        return ConfigurationChangeEvent.class;
	}
}
