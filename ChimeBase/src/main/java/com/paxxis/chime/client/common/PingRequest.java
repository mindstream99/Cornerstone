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

package com.paxxis.chime.client.common;

import com.paxxis.cornerstone.base.InstanceId;
import com.paxxis.cornerstone.base.RequestMessage;

/**
 *
 * @author Robert Englander
 */
public class PingRequest extends RequestMessage {
	private static final long serialVersionUID = 1L;
	private final static int VERSION = 1;

    @Override
    public int getMessageType() {
        return messageType();
    }

    public static int messageType() {
        return MessageConstants.PINGREQUEST;
    }

    @Override
    public int getMessageVersion() {
        return messageVersion();
    }

    public static int messageVersion() {
        return VERSION;
    }

    private User _user = null;
    private InstanceId activeId = InstanceId.UNKNOWN;
    private boolean sessionPing = true;
    private boolean userActivity = false;

    public boolean isSessionPing() {
        return sessionPing;
    }
    
    public void setActiveInstanceId(InstanceId id) {
        activeId = id;
        sessionPing = false;
    }

    public InstanceId getActiveInstanceId() {
        return activeId;
    }

    public void setUserActivity(boolean activity) {
        userActivity = activity;
    }

    public boolean getUserActivity() {
        return userActivity;
    }

    public void setUser(User user) {
        _user = user;
    }
    
    public User getUser() {
        return _user;
    }
}
