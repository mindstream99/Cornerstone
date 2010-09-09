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

package com.paxxis.chime.client.common.portal;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author Robert Englander
 */
public class PortletSpecification implements Serializable
{
    public enum PortletType
    {
        PortalPage,
        NamedSearch,
        Analytic,
        DataInstance,
        ExternalSite,
        InstanceField,
        InstanceHeader,
        InstanceSocialActivity,
        InstanceReviews,
        InstanceComments,
        InstanceTags,
        SearchCriteria,
        ImageGallery,
        FileGallery,
        ReviewContent,
        NamedSearchDetail,
        AnalyticDetail,
        FileContent,
        ImageContent,
        TagContent,
        DataTypeContent,
        DiscussionContent,
        PollContent,
        UserDetail,
        CommunityDetail,
        RichText,
        ImageRenderer,
        InstanceReferrers,
        DetailPage,
        FieldDataChart,
        MultiChart,
        ShapeFields,
        UserMessages
    }
    
    private PortletType _type;
    private boolean _showHeader = true;
    private boolean _pinned = false;
    private long _id;
    
    private HashMap<String, Serializable> _properties = new HashMap<String, Serializable>();
    
    public PortletSpecification()
    {
    }

    public PortletSpecification(PortletType type, long id)
    {
        _type = type;
        _id = id;
    }

    public PortletSpecification copy() {
        PortletSpecification target = new PortletSpecification();
        target._type = _type;
        target._id = _id;
        target.setShowHeader(_showHeader);
        target.setPinned(_pinned);
        Set<String> keys = _properties.keySet();
        for (String key : keys) {
            target.setProperty(key, _properties.get(key));
        }
        return target;
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

        PortletSpecification inst = (PortletSpecification)other;

        if (hashCode() != inst.hashCode()) {
            return false;
        }

        if (_type == null) {
            if (inst._type != null) {
                return false;
            }
        } else {
            if (!_type.equals(inst._type)) {
                return false;
            }
        }

        if (_showHeader != inst._showHeader) {
            return false;
        }

        if (_pinned != inst._pinned) {
            return false;
        }

        if (_id != inst._id) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (int) (this._id ^ (this._id >>> 32));
        hash = 97 * hash + (this._properties != null ? this._properties.hashCode() : 0);
        return hash;
    }

    public PortletType getType()
    {
        return _type;
    }
    
    public long getId()
    {
        return _id;
    }
    
    public Set<String> getPropertyNames()
    {
        return _properties.keySet();
    }
    
    public void setProperty(String name, Serializable value)
    {
        _properties.put(name, value);
    }
    
    public Serializable getProperty(String name)
    {
        return _properties.get(name);
    }

    public void removeProperty(String name) {
        _properties.remove(name);
    }
    
    public void setShowHeader(boolean showHeader)
    {
        _showHeader = showHeader;
    }
    
    public boolean isShowHeader()
    {
        return _showHeader;
    }
    
    public void setPinned(boolean pinned)
    {
        _pinned = pinned;
    }
    
    public boolean isPinned()
    {
        return _pinned;
    }
}
