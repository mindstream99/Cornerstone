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

import com.paxxis.chime.data.ShapeUtils;
import com.paxxis.chime.data.DataInstanceUtils;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceHelper;
import com.paxxis.chime.client.common.DataInstanceRequest.ClauseOperator;
import com.paxxis.chime.client.common.DataInstanceRequest.Operator;
import com.paxxis.chime.client.common.DataInstanceRequest.SortOrder;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.EditDataInstanceRequest.Operation;
import com.paxxis.chime.client.common.EditNamedSearchRequest;
import com.paxxis.chime.client.common.NamedSearch;
import com.paxxis.chime.client.common.SearchCriteria;
import com.paxxis.chime.client.common.SearchFilter;
import com.paxxis.cornerstone.base.InstanceId;
import com.paxxis.cornerstone.base.RequestMessage;
import com.paxxis.cornerstone.database.DatabaseConnection;
import com.paxxis.cornerstone.json.JSONArray;
import com.paxxis.cornerstone.json.JSONObject;
import com.paxxis.cornerstone.json.parser.JSONParser;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class NamedSearchHelper implements DataInstanceHelper
{
    public void processAfterRead(DataInstance instance, Object obj) 
    {
        NamedSearch inst = (NamedSearch)instance;
        
        // get the contents of the Search Criteria field, which contains
        // a JSON representation of the search criteria
        Shape type = inst.getShapes().get(0);
        DataField field = type.getField("Search Criteria");
        List<DataFieldValue> values = inst.getFieldValues(type, field);
        if (values.size() > 0) // && typevalues.size() > 0)
        {
            String jsonText = values.get(0).getValue().toString();
            
            SearchCriteria criteria = convert(jsonText, (DatabaseConnection)obj);
            inst.setSearchCriteria(criteria);
        }
    }

    public void processBeforeWrite(RequestMessage request)
    {
        EditNamedSearchRequest req = (EditNamedSearchRequest)request;

        if (req.getDataInstance() == null) {
            req.setOperation(Operation.Create);
        } else {
            req.setOperation(Operation.ModifyFieldData);
        }

        String str = convert(req.getSearchCriteria());

        DataField field = req.getShapes().get(0).getField("Search Criteria");
        if (req.getDataInstance() == null) {
            DataFieldValue value = new DataFieldValue();
            value.setValue(str);
            req.addFieldData(req.getShapes().get(0), field, value);
        } else {
            List<DataFieldValue> values = req.getDataInstance().getFieldValues(req.getShapes().get(0), field);
            DataFieldValue value = values.get(0);
            value.setValue(str);
            req.addFieldData(req.getShapes().get(0), field, value);
        }
    }
    
    private String convert(SearchCriteria criteria)
    {
        String text = "";
        
        JSONObject json = new JSONObject();
        json.put("sortOrder", criteria.getSortOrder().toString());
        json.put("clauseOperator", criteria.getOperator().toString());
        
        JSONArray array = new JSONArray();
        List<SearchFilter> filters = criteria.getFilters();
        for (SearchFilter filter : filters)
        {
            JSONObject obj = filterToJSON(filter);
            array.add(obj);
        }
        
        json.put("filters", array);

        text = json.toString();
        
        return text;
    }
    
    private SearchCriteria convert(String text, DatabaseConnection database)
    {
        SearchCriteria criteria = new SearchCriteria();
        
        JSONParser parser = JSONParser.create(text);
        
        try
        {
            JSONObject jsonObject = parser.parse();
            
            criteria.setSortOrder(SortOrder.valueOf(jsonObject.getString("sortOrder")));
            criteria.setOperator(ClauseOperator.valueOf(jsonObject.getString("clauseOperator")));
            
            JSONArray array = jsonObject.getJSONArray("filters");
            int cnt = array.length();
            for (int i = 0; i < cnt; i++)
            {
                SearchFilter filter = JSONToFilter(array.getJSONObject(i), database);
                criteria.addFilter(filter);
            }
            
        }
        catch (Exception e)
        {
            // this needs to be reported in some way, not just eaten like this
        }
        
        return criteria;
    }
    
    private JSONObject filterToJSON(SearchFilter filter)
    {
        JSONObject json = new JSONObject();

        DataField dataField = filter.getDataField();
        Shape shape = filter.getDataShape();

        json.put("fieldName", dataField.getName());
        
        if (shape != null) {
            json.put("dataType", shape.getId().getValue());
        } else {
            json.put("dataType", "-1");
        }
        
        json.put("operator", filter.getOperator().toString());
        
        Serializable value = filter.getValue();
        if (value != null)
        {
            json.put("value", value.toString());
        }
        else
        {
            json.put("value", "");
        }
        
        json.put("enabled", filter.isEnabled());

        return json;
    }

    private SearchFilter JSONToFilter(JSONObject json, DatabaseConnection database)
    {
        SearchFilter filter = new SearchFilter();
        
        try
        {
            String fieldName = json.getString("fieldName");
            String typeId = json.getString("dataType");
            Operator operator = Operator.valueOf(json.getString("operator"));
            Serializable value = json.getString("value");
        
            filter.setEnabled(json.getBoolean("enabled"));
            filter.setOperator(operator);

            // not all of the filters are based on actual fields.  Many, like name,
            // tag, etc are psuedo fields.  if the typeId is -1 it's a psuedo field
            
            DataField dataField = new DataField();
            dataField.setName(fieldName);
            Shape type = null;
            if (!typeId.equals("-1")) {
                type = ShapeUtils.getInstanceById(InstanceId.create(typeId), database, true);
                filter.setDataShape(type);
            }

            if (operator == Operator.Reference || operator == Operator.NotReference) {
                InstanceId id = InstanceId.create(value.toString());
                DataInstance inst = DataInstanceUtils.getInstance(id, null, database, false, true);
                filter.setValue(id, inst.getName());
            } else {
                filter.setValue(value);
            }
            
            filter.setDataField(dataField);
        }
        catch (Exception e)
        {
            // this needs to be reported in some way, not just eaten like this
        }
        
        return filter;
    }
}
