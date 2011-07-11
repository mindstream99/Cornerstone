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

import java.util.ArrayList;
import java.util.List;

import com.paxxis.cornerstone.base.RequestMessage;


public class ServiceMetricsEvent extends RequestMessage {
	private static final long serialVersionUID = 2L;
    private final static int VERSION = 1;

	public ServiceMetricsEvent() {
	}

    @Override
    public int getMessageType() {
        return messageType();
    }

    public static int messageType() {
        return MonitoringMessageConstants.METRICS_EVENT;
    }

    @Override
    public int getMessageVersion() {
        return messageVersion();
    }

    public static int messageVersion() {
        return VERSION;
    }

    private ServiceInstance serviceInstance = null;

    private List<ServiceMetrics> metrics = new ArrayList<ServiceMetrics>();
    
    public List<ServiceMetrics> getMetrics() {
		return metrics;
	}

	public void setMetrics(List<ServiceMetrics> metrics) {
		this.metrics.clear();
		this.metrics.addAll(metrics);
	}

	public void addMetrics(ServiceMetrics metrics) {
		this.metrics.add(metrics);
	}

	public ServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    public void setServiceInstance(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }
    
}
