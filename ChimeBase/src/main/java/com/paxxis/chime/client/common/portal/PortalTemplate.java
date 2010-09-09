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
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to describe the layout and content of
 * a dashboard.
 * 
 * @author Robert Englander
 */
public class PortalTemplate implements Serializable
{
    private String _id = "-1";
    private List<PortalColumn> _columns = new ArrayList<PortalColumn>();
    private boolean autoUpdate = false;
    
    public PortalTemplate() {
    }

    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    public void setAutoUpdate(boolean auto) {
        autoUpdate = auto;
    }
    
    public long getNextId() {
        long max = 0;
        for (PortalColumn column : _columns) {
            for (PortletSpecification spec : column.getPortletSpecifications()) {
                long specId = spec.getId();
                if (specId > max) {
                    max = specId;
                }
            }
        }

        return max + 1;
    }

    public PortalTemplate copy() {
        PortalTemplate target = new PortalTemplate();
        target.setId(_id);
        for (PortalColumn column : _columns) {
            target.add(column.copy());
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

        PortalTemplate inst = (PortalTemplate)other;

        if (hashCode() != inst.hashCode()) {
            return false;
        }

        if (_id == null) {
            if (inst._id != null) {
                return false;
            }
        } else {
            if (!_id.equals(inst._id)) {
                return false;
            }
        }

        if (_id == null) {
            if (inst._id != null) {
                return false;
            }
        } else {
            if (!_id.equals(inst._id)) {
                return false;
            }
        }

        if (_columns == null) {
            if (inst._columns != null) {
                return false;
            }
        } else {
            if (!_columns.equals(inst._columns)) {
                return false;
            }
        }

        if (autoUpdate != inst.autoUpdate) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this._id != null ? this._id.hashCode() : 0);
        hash = 29 * hash + (this._columns != null ? this._columns.hashCode() : 0);
        return hash;
    }

    public void setId(String id)
    {
        _id = id;
    }
    
    public String getId()
    {
        return _id;
    }
        
    public void add(PortalColumn column)
    {
        _columns.add(column);
    }
    
    public List<PortalColumn> getPortalColumns()
    {
        return _columns;
    }
}
