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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.paxxis.cornerstone.base.RequestMessage;

/**
 * 
 * @author Robert Englander
 *
 */
public class ConfigurationChangeEvent extends RequestMessage {
	private static final long serialVersionUID = 1L;
    private final static int VERSION = 1;

	public ConfigurationChangeEvent() {
	}

    @Override
    public int getMessageType() {
        return messageType();
    }

    public static int messageType() {
        return ManagementMessageConstants.CONFIGURATION_CHANGE_EVENT;
    }

    @Override
    public int getMessageVersion() {
        return messageVersion();
    }

    public static int messageVersion() {
        return VERSION;
    }

    private List<ConfigurationChange> changes = new ArrayList<ConfigurationChange>();
    
    public void setConfigurationChanges(Collection<ConfigurationChange> changes) {
    	this.changes.clear();
    	this.changes.addAll(changes);
    }
    
    public Collection<ConfigurationChange> getConfigurationChanges() {
    	return this.changes;
    }
    
    public void addConfigurationChange(ConfigurationChange change) {
    	this.changes.add(change);
    }
}
