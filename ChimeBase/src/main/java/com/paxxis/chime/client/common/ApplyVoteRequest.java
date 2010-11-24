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

import com.paxxis.cornerstone.base.RequestMessage;

/**
 *
 * @author Robert Englander
 */
public class ApplyVoteRequest extends RequestMessage {
	private static final long serialVersionUID = 1L;
    private final static int VERSION = 1;

    @Override
    public int getMessageType() {
        return messageType();
    }

    public static int messageType() {
        return MessageConstants.APPLYVOTEREQUEST;
    }

    @Override
    public int getMessageVersion() {
        return messageVersion();
    }

    public static int messageVersion() {
        return VERSION;
    }


    private DataInstance _data = null;
    private boolean _positive = true;
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

    public boolean isPositive()
    {
        return _positive;
    }

    public void setPositive(boolean positive)
    {
        _positive = positive;
    }

}
