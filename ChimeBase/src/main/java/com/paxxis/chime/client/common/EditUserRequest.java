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
public class EditUserRequest extends RequestMessage {
    private final static int VERSION = 1;

    @Override
    public MessageConstants.MessageType getMessageType() {
        return messageType();
    }

    public static MessageConstants.MessageType messageType() {
        return MessageConstants.MessageType.EditUserRequest;
    }

    @Override
    public int getMessageVersion() {
        return messageVersion();
    }

    public static int messageVersion() {
        return VERSION;
    }

    public enum Operation {
        Create,
        Modify
    }


    private User _data = null;
    private User _user = null;
    private Operation _op = Operation.Create;
    private String _name = null;
    private String _password = null;
    private String _oldPassword = null;
    private String _description = null;
    private boolean _enabled = true;

    public String getDescription() {
        return _description;
    }

    public void setDescription(String description) {
        _description = description;
    }

    public boolean isEnabled() {
        return _enabled;
    }

    public void setEnabled(boolean enabled) {
        _enabled = enabled;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getPassword() {
        return _password;
    }

    public void setPassword(String pw) {
        _password = pw;
    }

    public String getOldPassword() {
        return _oldPassword;
    }

    public void setOldPassword(String pw) {
        _oldPassword = pw;
    }

    public User getUser() {
        return _user;
    }

    public void setUser(User user) {
        _user = user;
    }

    public User getData()
    {
        return _data;
    }

    public void setData(User data)
    {
        _data = data;
    }

    public void setOperation(Operation op) {
        _op = op;
    }

    public Operation getOperation() {
        return _op;
    }
}
