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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class UserMessagesRequest extends RequestMessage {
    public enum Type {
        Query,
        Delete
    }

    private final static int VERSION = 1;

    @Override
    public MessageConstants.MessageType getMessageType() {
        return messageType();
    }

    public static MessageConstants.MessageType messageType() {
        return MessageConstants.MessageType.UserMessagesRequest;
    }

    @Override
    public int getMessageVersion() {
        return messageVersion();
    }

    public static int messageVersion() {
        return VERSION;
    }

    private Type type = Type.Query;
    private User instance = null;
    private Cursor cursor = null;
    private User user = null;
    private List<UserMessage> deleteList = new ArrayList<UserMessage>();

    public void setType(Type t) {
        type = t;
    }

    public Type getType() {
        return type;
    }

    public void setDeleteList(List<UserMessage> list) {
        deleteList.clear();
        deleteList.addAll(list);
    }

    public List<UserMessage> getDeleteList() {
        return deleteList;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
    }

    public Cursor getCursor() {
        return cursor;
    }

    public void setDataInstance(User instance) {
        this.instance = instance;
    }

    public User getDataInstance() {
        return instance;
    }

}
