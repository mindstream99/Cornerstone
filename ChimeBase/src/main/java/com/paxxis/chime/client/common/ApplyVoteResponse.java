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
public class ApplyVoteResponse extends ResponseMessage<ApplyVoteRequest> {
    private final static int VERSION = 1;

    @Override
    public MessageConstants.MessageType getMessageType() {
        return messageType();
    }

    public static MessageConstants.MessageType messageType() {
        return MessageConstants.MessageType.ApplyVoteResponse;
    }

    @Override
    public int getMessageVersion() {
        return messageVersion();
    }

    public static int messageVersion() {
        return VERSION;
    }


    // the resulting data object
    DataInstance _data = null;

    // the update user (because voting info has changed)
    User updatedUser = null;

    public void setUpdatedUser(User user) {
        updatedUser = user;
    }

    public User getUpdatedUser() {
        return updatedUser;
    }

    public DataInstance getDataInstance()
    {
        return _data;
    }

    public void setDataInstance(DataInstance data)
    {
        _data = data;
    }
}

