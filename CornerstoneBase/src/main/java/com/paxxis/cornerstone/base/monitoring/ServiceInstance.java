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

import com.paxxis.cornerstone.base.InstanceId;

/**
 * ServiceInstance contains properties that represent a running service instance.
 * 
 * @author Rob Englander
 *
 */
public class ServiceInstance implements Serializable {

	private static final long serialVersionUID = 2L;

	private String displayName = null;
	private InstanceId serviceId = null;
	private InstanceId instanceId = null;
	private String hostName = null;
	private String clusterName = null;
	private Date startTime = null;
	private boolean startTimeUTC = false;

	public ServiceInstance() {
	}
	
	public void setStartTimeUTC(boolean val) {
	    this.startTimeUTC = val;
	}
	
	public boolean isStartTimeUTC() {
	    return this.startTimeUTC;
	}
	
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public InstanceId getServiceId() {
		return serviceId;
	}

	public void setServiceId(InstanceId serviceId) {
		this.serviceId = serviceId;
	}

	public InstanceId getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(InstanceId instanceId) {
		this.instanceId = instanceId;
	}

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

	public Date getStartTime() {
		return startTime;
	}
	
	public void setStartTime(Date startTime) {
        int offset = 60000 * startTime.getTimezoneOffset();
        long newTime = startTime.getTime() + offset;
		this.startTime = new Date(newTime);
	}
}
