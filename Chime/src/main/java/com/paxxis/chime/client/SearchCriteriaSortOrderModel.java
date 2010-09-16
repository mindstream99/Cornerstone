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

package com.paxxis.chime.client;

import com.paxxis.chime.client.common.DataInstanceRequest.SortOrder;
import com.extjs.gxt.ui.client.data.BaseTreeModel;
import java.io.Serializable;

/**
 *
 * @author Robert Englander
 */
public class SearchCriteriaSortOrderModel extends BaseTreeModel implements Serializable
{
    private SortOrder _sortOrder;

    public SearchCriteriaSortOrderModel()
    {

    }

    public static String getText(SortOrder order)
    {
        String text = "";
        
        switch (order)
        {
            case ByName:
                text = "By Name";
                break;
            case ByRating:
                text = "By Rating";
                break;
            case ByMostActive:
                text = "By Most Recent Activity";
                break;
            case ByMostRecentCreate:
                text = "By Most Recently Created";
                break;
            case ByMostRecentEdit:
                text = "By Most Recently Edited";
                break;
        }
        
        return text;
    }
    
    public SearchCriteriaSortOrderModel(SortOrder operator)
    {
        _sortOrder = operator;
        setDescription(getText(operator));
    }

    public String getDescription()
    {
        return get("description");
    }

    public void setDescription(String text)
    {
        set("description", text);
    }

    public void setSortOrder(String order) 
    {
        set("order", order);
    }

    public SortOrder getSortOrder()
    {
        return _sortOrder;
    }
}
