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

import com.paxxis.cornerstone.base.MessagingConstants;

public final class MonitoringMessageConstants extends MessagingConstants {
    // message groups
    public final static int METRICS_EVENT_MESSAGE_GROUP = 13800;

    // message type values
    public final static int METRICS_EVENT = 13801;

    private MonitoringMessageConstants() {
    }
}
