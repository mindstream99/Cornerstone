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
public class ApplyTagRequest extends RequestMessage {
    public enum ApplyType
    {
        Add,
        Remove
    }
    
    private final static int VERSION = 1;

    @Override
    public MessageConstants.MessageType getMessageType() {
        return messageType();
    }

    public static MessageConstants.MessageType messageType() {
        return MessageConstants.MessageType.ApplyTagRequest;
    }

    @Override
    public int getMessageVersion() {
        return messageVersion();
    }

    public static int messageVersion() {
        return VERSION;
    }

    
    private ApplyType _applyType = ApplyType.Add;
    private DataInstance _data = null;
    private List<Tag> _tags = new ArrayList<Tag>();
    private User _user = null;
    
    public ApplyType getApplyType()
    {
        return _applyType;
    }
    
    public void setApplyType(ApplyType type)
    {
        _applyType = type;
    }
    
    public DataInstance getData()
    {
        return _data;
    }

    public void setData(DataInstance data)
    {
        _data = data;
    }

    public User getUser()
    {
        return _user;
    }
    
    public void setUser(User user)
    {
        _user = user;
    }
    
    public List<Tag> getTags()
    {
        return _tags;
    }

    public void addTag(Tag tag)
    {
        _tags.add(tag);
    }
}
