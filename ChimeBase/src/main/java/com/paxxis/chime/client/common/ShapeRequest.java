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

/**
 *
 * @author Robert Englander
 */
public class ShapeRequest extends RequestMessage {
    private final static int VERSION = 1;

    @Override
    public MessageConstants.MessageType getMessageType() {
        return messageType();
    }

    public static MessageConstants.MessageType messageType() {
        return MessageConstants.MessageType.ShapeRequest;
    }

    @Override
    public int getMessageVersion() {
        return messageVersion();
    }

    public static int messageVersion() {
        return VERSION;
    }

    // the preferred way to request a shape is by id, but there are
    // situations in the client app that still request by name
    private String name = null;
    private InstanceId id = null;

    public boolean isById() {
        return id != null;
    }

    public boolean isByName() {
        return name != null;
    }

    public void setName(String name) {
        this.name = name;
        id = null;
    }
    
    public String getName() {
        if (isById()) {
            return null;
        }

        return name;
    }

    public void setId(InstanceId id) {
        this.id = id;
        name = null;
    }
    
    public InstanceId getId() {
        if (!isById()) {
            return null;
        }
        
        return id;
    }
}
