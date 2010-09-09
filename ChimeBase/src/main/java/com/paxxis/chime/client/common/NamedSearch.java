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
 *
 * @author Robert Englander
 */
public class NamedSearch extends DataInstance
{
    private SearchCriteria _searchCriteria = null;
    
    public void setSearchCriteria(SearchCriteria criteria)
    {
        _searchCriteria = criteria;
    }
    
    public SearchCriteria getSearchCriteria()
    {
        return _searchCriteria;
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

        NamedSearch inst = (NamedSearch)other;

        if (hashCode() != inst.hashCode()) {
            return false;
        }

        if (!super.equals(other)) {
            return false;
        }

        if (_searchCriteria == null) {
            if (inst._searchCriteria != null) {
                return false;
            }
        } else {
            if (!_searchCriteria.equals(inst._searchCriteria)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public NamedSearch copy() {
        NamedSearch result = super.copy(new NamedSearch());

        if (_searchCriteria != null) {
            result._searchCriteria = _searchCriteria.copy();
        }
        
        return result;
    }
}
