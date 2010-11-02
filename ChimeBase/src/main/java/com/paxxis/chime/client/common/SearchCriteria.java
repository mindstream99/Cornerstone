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

import com.paxxis.chime.client.common.DataInstanceRequest.ClauseOperator;
import com.paxxis.chime.client.common.DataInstanceRequest.Depth;
import com.paxxis.chime.client.common.DataInstanceRequest.Operator;
import com.paxxis.chime.client.common.DataInstanceRequest.SortOrder;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class SearchCriteria implements Serializable {
	private static final long serialVersionUID = 2L;

	private SortOrder _sortOrder = SortOrder.ByName;
    private ClauseOperator _operator = ClauseOperator.MatchAll;
    
    private List<SearchFilter> _filters = new ArrayList<SearchFilter>();
    
    public SearchCriteria()
    {
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

        SearchCriteria inst = (SearchCriteria)other;

        if (hashCode() != inst.hashCode()) {
            return false;
        }

        if (_sortOrder == null) {
            if (inst._sortOrder != null) {
                return false;
            }
        } else {
            if (!_sortOrder.equals(inst._sortOrder)) {
                return false;
            }
        }

        if (_operator == null) {
            if (inst._operator != null) {
                return false;
            }
        } else {
            if (!_operator.equals(inst._operator)) {
                return false;
            }
        }

        if (_filters == null) {
            if (inst._filters != null) {
                return false;
            }
        } else {
            if (!_filters.equals(inst._filters)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this._sortOrder != null ? this._sortOrder.hashCode() : 0);
        hash = 29 * hash + (this._operator != null ? this._operator.hashCode() : 0);
        hash = 29 * hash + (this._filters != null ? this._filters.hashCode() : 0);
        return hash;
    }

    public SearchCriteria copy() {
        SearchCriteria target = new SearchCriteria();
        target._sortOrder = _sortOrder;
        target._operator = _operator;
        target._filters = new ArrayList<SearchFilter>();
        for (SearchFilter filter : _filters) {
            target.addFilter(filter.copy());
        }
        return target;
    }

    public int getEnabledFilterCount()
    {
        int cnt = 0;
        for (SearchFilter filter : _filters)
        {
            if (filter.isEnabled())
            {
                cnt++;
            }
        }
        
        return cnt;
    }
    
    public DataInstanceRequest buildRequest(User user, int cursorSize)
    {
        DataInstanceRequest req = new DataInstanceRequest();
        req.setSortOrder(_sortOrder);
        req.setClauseOperator(_operator);
        req.setCursor(new Cursor(cursorSize));
        req.setDepth(Depth.Shallow);
        req.setUser(user);

        for (SearchFilter filter : _filters)
        {
            if (filter.isEnabled())
            {
                DataField field = filter.getDataField();
                String fieldName = null;
                if (field != null) {
                    fieldName = field.getName();
                }

                Operator op = filter.getOperator();
                Serializable value = filter.getValue();
                 
                DataField subField = filter.getSubField();
                Shape subShape = null;
                if (subField != null) {
                	subShape = field.getShape();
                	fieldName = subField.getName();
                }
                
                req.addQueryParameter(filter.getDataShape(), subShape, fieldName, value, op);
            }
        }
        
        return req;
    }

    public void setOperator(ClauseOperator operator)
    {
        _operator = operator;
    }
    
    public ClauseOperator getOperator()
    {
        return _operator;
    }
    
    public void setSortOrder(SortOrder sortOrder)
    {
        _sortOrder = sortOrder;
    }
    
    public SortOrder getSortOrder()
    {
        return _sortOrder;
    }
    
    public void addFilter(SearchFilter filter)
    {
        _filters.add(filter);
    }
    
    public void removeFilter(SearchFilter filter)
    {
        if (_filters.contains(filter))
        {
            _filters.remove(filter);
        }
    }
    
    public void removeFilter(int idx)
    {
        int cnt = _filters.size();
        if (idx >= 0 && idx < cnt)
        {
            _filters.remove(idx);
        }
    }
    
    public List<SearchFilter> getFilters()
    {
        return _filters;
    }
}
