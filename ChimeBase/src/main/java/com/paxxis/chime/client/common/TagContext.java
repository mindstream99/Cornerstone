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

import java.io.Serializable;

/**
 *
 * @author Robert Englander
 */
public class TagContext implements Serializable
{
    private Tag _tag = null;
    private long _usageCount = 0;
    private boolean _userTagged = false;
    
    public Tag getTag()
    {
        return _tag;
    }

    public void setTag(Tag tag)
    {
        _tag = tag;
    }

    public long getUsageCount()
    {
        return _usageCount;
    }

    public void setUsageCount(long usageCount)
    {
        _usageCount = usageCount;
    }
    
    public boolean isUserTagged()
    {
        return _userTagged;
    }
    
    public void setUserTagged(boolean userTagged)
    {
        _userTagged = userTagged;
    }
}
