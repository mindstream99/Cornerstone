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

import com.paxxis.chime.client.common.DataInstanceRequest.SortOrder;

/**
 *
 * @author Robert Englander
 */
public class ReviewsRequest extends RequestMessage {
    private final static int VERSION = 1;

    @Override
    public MessageConstants.MessageType getMessageType() {
        return messageType();
    }

    public static MessageConstants.MessageType messageType() {
        return MessageConstants.MessageType.ReviewsRequest;
    }

    @Override
    public int getMessageVersion() {
        return messageVersion();
    }

    public static int messageVersion() {
        return VERSION;
    }

    
    private DataInstance _dataInstance = null;
    private Cursor _cursor = null;
    private User _user = null;
    private SearchFilter _filter = null;
    private SortOrder sortOrder = SortOrder.ByMostRecentEdit;

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder order) {
        sortOrder = order;
    }
    
    public SearchFilter getFilter() {
        return _filter;
    }

    public void setFilter(SearchFilter filter) {
        _filter = filter;
    }
    
    public User getUser()
    {
        return _user;
    }

    public void setUser(User user)
    {
        _user = user;
    }
    
    public void setCursor(Cursor cursor)
    {
        _cursor = cursor;
    }
    
    public Cursor getCursor()
    {
        return _cursor;
    }
    
    public void setDataInstance(DataInstance instance)
    {
        _dataInstance = instance;
    }
    
    public DataInstance getDataInstance()
    {
        return _dataInstance;
    }

}
