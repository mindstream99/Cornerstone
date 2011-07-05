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

import com.paxxis.cornerstone.base.MessageGroup;

/**
 * Metrics events message group.
 * 
 * @author Rob Englander
 *
 */
public class MetricsEventMessageGroupV1 extends MessageGroup {

    private static final long serialVersionUID = 1L;
    
    public static final int VERSION = 1;
    
    public MetricsEventMessageGroupV1() {
        register(new ServiceMetricsEvent());
    }
    
    @Override
    public int getId() {
        return MonitoringMessageConstants.METRICS_EVENT_MESSAGE_GROUP;
    }

    @Override
    public int getVersion() {
        return VERSION;
    }

}
