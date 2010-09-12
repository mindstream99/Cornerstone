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

package com.paxxis.chime.service;

import com.paxxis.chime.client.common.Tag;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class ApplyTagsResponse 
{
    boolean _instanceModified = false;
    List<Tag> _newTags = new ArrayList<Tag>();
    
    public ApplyTagsResponse(boolean instanceModified, List<Tag> newTags)
    {
        _instanceModified = instanceModified;
        _newTags = newTags;
    }
    
    public ApplyTagsResponse()
    {
    }

    public void setInstanceModified(boolean val)
    {
        _instanceModified = val;
    }
    
    public boolean wasInstanceModified()
    {
        return _instanceModified;
    }
    
    public void setNewTags(List<Tag> tags)
    {
        _newTags = tags;
    }
    
    public List<Tag> getNewTags()
    {
        return _newTags;
    }
}
