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
 *vvv
 * @author Robert Englanderert Englander
 */
public class AddCommentRequest extends RequestMessage {
    private final static int VERSION = 1;

    @Override
    public MessageConstants.MessageType getMessageType() {
        return messageType();
    }

    public static MessageConstants.MessageType messageType() {
        return MessageConstants.MessageType.AddCommentRequest;
    }

    @Override
    public int getMessageVersion() {
        return messageVersion();
    }

    public static int messageVersion() {
        return VERSION;
    }

    
    private DataInstance _data = null;
    private Comment _comment = null;
    private User _user = null;

    public User getUser() {
        return _user;
    }

    public void setUser(User user) {
        _user = user;
    }


    public DataInstance getData()
    {
        return _data;
    }

    public void setData(DataInstance data)
    {
        _data = data;
    }

    public Comment getComment()
    {
        return _comment;
    }

    public void setComment(Comment comment)
    {
        _comment = comment;
    }
    
}
