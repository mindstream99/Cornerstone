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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class DataInstanceRequest extends RequestMessage {
    public enum Style {
        InstanceId,
        KeywordSearch,
        KeywordSearchShort,
        ComplexSearch,
        ReferenceSearch
    }

    public enum ClauseOperator
    {
        MatchAll,
        MatchAny
    }
    
    public enum Operator
    {
        StartsWith,
        Contains,
        Like,
        Equals,
        NotEquals,
        LessThan,
        LessThanOrEquals,
        GreaterThan,
        GreaterThanOrEquals,
        Reference,
        NotReference,
        ContainedIn,
        NotContainedIn,
        Past24Hours,
        Past3Days,
        Past7Days,
        Past30Days,
        BeforeDate,
        OnOrBeforeDate,
        OnDate,
        AfterDate,
        OnOrAfterDate
    }
    
    public enum Depth
    {
        Deep,
        Shallow
    }
    
    public enum SortOrder
    {
        ByRelevance,
        ByName,
        ByRating,
        ByMostActive,
        ByMostUseful,
        ByMostRecentEdit,
        ByMostRecentCreate
    }
    
    private final static int VERSION = 1;

    @Override
    public MessageConstants.MessageType getMessageType() {
        return messageType();
    }

    public static MessageConstants.MessageType messageType() {
        return MessageConstants.MessageType.DataInstanceRequest;
    }

    @Override
    public int getMessageVersion() {
        return messageVersion();
    }

    public static int messageVersion() {
        return VERSION;
    }

    
    //private boolean _isById = false;
    private Style style = Style.ComplexSearch;
    private SortOrder _sortOrder = SortOrder.ByName;
    private Depth _depth = Depth.Shallow;
    private ClauseOperator _clauseOperator = ClauseOperator.MatchAll;

    private String keywords = null;

    // always use the shape if it's not null, otherwise use the shape name
    //private Shape dataShape = null;
    //private String dataShapeName = null;

    private User _user = null;
    private InstanceId _instanceId = InstanceId.create("0");
    private Cursor _cursor = null;
    
    
    private List<Parameter> _queryParameters = new ArrayList<Parameter>();
    
    public void setCursor(Cursor cursor)
    {
        _cursor = cursor;
    }
    
    public Cursor getCursor()
    {
        return _cursor;
    }
    
    public SortOrder getSortOrder()
    {
        return _sortOrder;
    }
    
    public void setSortOrder(SortOrder order)
    {
        _sortOrder = order;
    }
    
    public Depth getDepth()
    {
        return _depth;
    }
    
    public void setDepth(Depth depth)
    {
        _depth = depth;
    }

    public void setClauseOperator(ClauseOperator op)
    {
        _clauseOperator = op;
    }
    
    public ClauseOperator getClauseOperator()
    {
        return _clauseOperator;
    }
    
    public List<Parameter> getQueryParameters()
    {
        return _queryParameters;
    }
    
    public void setIds(InstanceId instanceId)
    {
        _instanceId = instanceId;
        style = Style.InstanceId;
    }

    public void setKeywords(String words, boolean shortQuery) {
        if (shortQuery) {
            style = Style.KeywordSearchShort;
        } else {
            style = Style.KeywordSearch;
        }
        keywords = words;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setStyle(Style st) {
        style = st;
    }
    
    public Style getStyle() {
        return style;
    }

    public InstanceId getInstanceId()
    {
        return _instanceId;
    }
    
    public void addQueryParameter(Shape type, String fieldName, Serializable fieldValue)
    {
        addQueryParameter(type, fieldName, fieldValue, Operator.Contains);
    }

    public void addQueryParameter(Shape type, String fieldName, Serializable fieldValue, Operator operator) {
    	addQueryParameter(type, null, fieldName, fieldValue, operator);
    }
    
    public void addQueryParameter(Shape type, Shape subShape, String fieldName, Serializable fieldValue, Operator operator) {
        addQueryParam(type, subShape, fieldName, fieldValue, operator, ClauseOperator.MatchAll);
    }
    
    private void addQueryParam(Shape type, Shape subShape, String fieldName, Serializable fieldValue, Operator operator, ClauseOperator clauseOperator) {
        Parameter param = new Parameter();
        param.dataShape = type;
        param.subShape = subShape;
        param.fieldName = fieldName;
        param.fieldValue = fieldValue;
        param.operator = operator;
        param.clauseOperator = clauseOperator;
        
        _queryParameters.add(param);
    }
    
    public User getUser()
    {
        return _user;
    }
    
    public void setUser(User user)
    {
        _user = user;
    }
}
