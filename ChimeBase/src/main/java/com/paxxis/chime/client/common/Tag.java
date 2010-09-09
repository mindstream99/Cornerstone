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
public class Tag extends DataInstance
{
    private long _usageCount = 0;
    private boolean _private = false;

    private List<ShapeTagContext> _communityContext = null;
    private List<ShapeTagContext> _userContext = null;

    @Override
    public Tag copy() {
        Tag tag = super.copy(new Tag());
        tag._usageCount = _usageCount;
        tag._private = _private;
        if (_communityContext != null) {
            List<ShapeTagContext> list = new ArrayList<ShapeTagContext>();
            for (ShapeTagContext ctx : _communityContext) {
                list.add(ctx.copy());
            }

            tag.setCommunityContext(list);
        }

        if (_userContext != null) {
            List<ShapeTagContext> list = new ArrayList<ShapeTagContext>();
            for (ShapeTagContext ctx : _userContext) {
                list.add(ctx.copy());
            }

            tag.setUserContext(list);
        }

        return tag;
    }

    @Override
    public boolean equals(Object other) {

        if (this == other) {
            return true;
        }

        if (other == null) {
            return false;
        }

        if (other.getClass() != getClass()) {
            return false;
        }

        Tag inst = (Tag)other;

        if (hashCode() != inst.hashCode()) {
            return false;
        }

        if (!super.equals(other)) {
            return false;
        }

        if (_usageCount != inst._usageCount) {
            return false;
        }

        if (_private != inst._private) {
            return false;
        }

        if (_communityContext == null) {
            if (inst._communityContext != null) {
                return false;
            }
        } else {
            if (!_communityContext.equals(inst._communityContext)) {
                return false;
            }
        }

        if (_userContext == null) {
            if (inst._userContext != null) {
                return false;
            }
        } else {
            if (!_userContext.equals(inst._userContext)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public boolean isPrivate()
    {
        return _private;
    }

    public void setPrivate(boolean value)
    {
        _private = value;
    }

    public long getUsageCount()
    {
        return _usageCount;
    }

    public void setUsageCount(long usageCount)
    {
        _usageCount = usageCount;
    }

    public List<ShapeTagContext> getCommunityContext()
    {
        return _communityContext;
    }

    public void setCommunityContext(List<ShapeTagContext> communityContext)
    {
        _communityContext = communityContext;
    }

    public List<ShapeTagContext> getUserContext()
    {
        return _userContext;
    }

    public void setUserContext(List<ShapeTagContext> userContext)
    {
        _userContext = userContext;
    }

}
