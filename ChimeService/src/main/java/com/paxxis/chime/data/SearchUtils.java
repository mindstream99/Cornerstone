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

package com.paxxis.chime.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.store.RAMDirectory;

import com.paxxis.chime.client.common.Community;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstance.ReviewAction;
import com.paxxis.chime.client.common.DataInstance.TagAction;
import com.paxxis.chime.client.common.DataInstanceRequest.ClauseOperator;
import com.paxxis.chime.client.common.DataInstanceRequest.Operator;
import com.paxxis.chime.client.common.DataInstanceRequest.SortOrder;
import com.paxxis.chime.client.common.Parameter;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.client.common.constants.SearchFieldConstants;
import com.paxxis.chime.client.common.extension.ChimeExtension;
import com.paxxis.chime.client.common.extension.MemoryIndexer;
import com.paxxis.chime.extension.ChimeExtensionManager;
import com.paxxis.chime.extension.ChimeMemoryIndexer;
import com.paxxis.chime.indexing.ChimeAnalyzer;
import com.paxxis.chime.indexing.Indexer;
import com.paxxis.chime.service.InstancesResponse;
import com.paxxis.chime.service.Tools;
import com.paxxis.cornerstone.base.Cursor;
import com.paxxis.cornerstone.base.InstanceId;
import com.paxxis.cornerstone.database.DataSet;
import com.paxxis.cornerstone.database.DatabaseConnection;
import com.paxxis.cornerstone.database.StringData;

/**
 *
 * @author Robert Englander
 */
public class SearchUtils {
    private static final Logger _logger = Logger.getLogger(SearchUtils.class);

    private static final int ROWLIMIT = 1000;
    private static final long MSEC_24HOURS = (24L * 60L * 60L * 1000L);
    private static final long MSEC_3DAYS = (3L * 24L * 60L * 60L * 1000L);
    private static final long MSEC_7DAYS = (7L * 24L * 60L * 60L * 1000L);
    private static final long MSEC_30DAYS = (30L * 24L * 60L * 60L * 1000L);
    private static final long MAXDATE = Date.UTC(2999, 1, 1, 0, 0, 0);

    private SearchUtils() {
    }

    public static InstancesResponse findInstances(Shape shape, boolean excludeInternals, String text, Cursor cursor, DatabaseConnection database) throws Exception
    {
        if (excludeInternals)
        {
            String sql = "select A.id, A.name, averageRating from " + Tools.getTableSet()
                    + " A, " + Tools.getTableSet() + "_Type B where A.id = B.instance_id and B.datatype_id = '" + shape.getId()
                    + "' and A.name like '"
                    + new StringData(text, false).asSQLValue() + "%' and A.internal = 'N' order by A.name limit " + (ROWLIMIT + 1);

            return findInstances(sql, cursor, database);
        }
        else
        {
            return findInstances(shape, text, cursor, database);
        }
    }

    public static InstancesResponse findInstances(Shape shape, String text, Cursor cursor, DatabaseConnection database) throws Exception
    {
        String sql = "select A.id, A.name, A.averageRating from " + Tools.getTableSet()
                + " A, " + Tools.getTableSet() + "_Type B where A.id = B.instance_id and B.datatype_id = '" + shape.getId()
                + "' and A.name like '"
                + new StringData(text, false).asSQLValue() + "%'" + " order by A.name limit " + (ROWLIMIT + 1);

        return findInstances(sql, cursor, database);
    }

    static InstancesResponse findInstances(String sql, Cursor cursor, DatabaseConnection database) throws Exception
    {
        List<DataInstance> instances = new ArrayList<DataInstance>();

        int start = 1;
        int end = 999999;
        int total = 0;

        if (cursor != null)
        {
            start = cursor.getFirst() + 1;
            end = start + cursor.getMax() - 1;
        }

        int fetchNumber = start;

        boolean limited = false;
        DataSet dataSet = database.getDataSet(sql, true);
        boolean fetchAgain = dataSet.absolute(start);
        while (fetchAgain)
        {
            String id = dataSet.getFieldValue("id").asString();
            String name = dataSet.getFieldValue("name").asString();
            float averageRating = dataSet.getFieldValue("averageRating").asFloat();
            int ratingCount = dataSet.getFieldValue("ratingCount").asInteger();
            int commentCount = dataSet.getFieldValue("commentCount").asInteger();
            int tagCount = dataSet.getFieldValue("tagCount").asInteger();


            DataInstance instance = new DataInstance();
            instance.setId(InstanceId.create(id));
            instance.setName(name);
            instance.setAverageRating(averageRating);
            instance.setRatingCount(ratingCount);
            instance.setCommentCount(commentCount);
            instance.setTagCount(tagCount);

            instances.add(instance);

            if (fetchNumber == end)
            {
                fetchAgain = false;
            }
            else
            {
                fetchAgain = dataSet.next();
            }

            if (!fetchAgain)
            {
                dataSet.last();
                total = dataSet.getRowNumber();

                if (total > ROWLIMIT)
                {
                    total = ROWLIMIT;
                    limited = true;
                }
            }

            fetchNumber++;
        }

        dataSet.close();

        // create the return cursor
        Cursor newCursor = null;

        if (cursor != null)
        {
            newCursor = new Cursor(cursor.getFirst(), instances.size(), cursor.getMax(), total, limited);
        }

        InstancesResponse response = new InstancesResponse();
        response.list = instances;
        response.cursor = newCursor;

        return response;
    }

    public static InstancesResponse findAppliedTagInstances(Shape shape, String text, Cursor cursor, DatabaseConnection database) throws Exception
    {
        List<DataInstance> instances = new ArrayList<DataInstance>();

        String tagSql = "select distinct(tag_id) from Chime.User_tag_usage "
                            + "where type_id = '" + shape.getId() + "' and usageCount > 0";

        String sql = "select id, name, averageRating from " +  Tools.getTableSet() + " where name like '"
                            + new StringData(text, false).asSQLValue() + "%'" + " and id in (" + tagSql + ") order by name limit " + (ROWLIMIT + 1);

        int start = 1;
        int end = 999999;
        int total = 0;

        if (cursor != null)
        {
            start = cursor.getFirst() + 1;
            end = start + cursor.getMax() - 1;
        }

        int fetchNumber = start;

        boolean limited = false;
        DataSet dataSet = database.getDataSet(sql, true);
        boolean fetchAgain = dataSet.absolute(start);
        while (fetchAgain)
        {
            String id = dataSet.getFieldValue("id").asString();
            String name = dataSet.getFieldValue("name").asString();
            float averageRating = dataSet.getFieldValue("averageRating").asFloat();
            int ratingCount = dataSet.getFieldValue("ratingCount").asInteger();
            int commentCount = dataSet.getFieldValue("commentCount").asInteger();
            int tagCount = dataSet.getFieldValue("tagCount").asInteger();

            DataInstance instance = new DataInstance();
            instance.setId(InstanceId.create(id));
            instance.setName(name);
            instance.setAverageRating(averageRating);
            instance.setRatingCount(ratingCount);
            instance.setCommentCount(commentCount);
            instance.setTagCount(tagCount);

            instances.add(instance);

            if (fetchNumber == end)
            {
                fetchAgain = false;
            }
            else
            {
                fetchAgain = dataSet.next();
            }

            if (!fetchAgain)
            {
                dataSet.last();
                total = dataSet.getRowNumber();

                if (total > ROWLIMIT)
                {
                    total = ROWLIMIT;
                    limited = true;
                }
            }

            fetchNumber++;
        }

        dataSet.close();

        // create the return cursor
        Cursor newCursor = null;

        if (cursor != null)
        {
            newCursor = new Cursor(cursor.getFirst(), instances.size(), cursor.getMax(), total, limited);
        }

        InstancesResponse response = new InstancesResponse();
        response.list = instances;
        response.cursor = newCursor;

        return response;
    }

    public static InstancesResponse findInstancesByIndex(Class instanceClass, Shape shape, String text,
            User user, boolean userCreatedOnly, boolean directCreatable, Cursor cursor, SortOrder sortOrder, DatabaseConnection database) throws Exception
    {
        ClauseOperator op = ClauseOperator.MatchAll;
        Parameter p = new Parameter();
        p.fieldName = SearchFieldConstants.NAME;
        p.fieldValue = text;
        p.operator = Operator.StartsWith;
        p.dataShape = shape;
        List<Parameter> params = new ArrayList<Parameter>();
        params.add(p);

        return getInstancesByIndex(instanceClass, params, op, user, userCreatedOnly, directCreatable, cursor, sortOrder, database);
    }

    public static InstancesResponse findInstancesByIndex(Class instanceClass, Shape shape, String text,
            User user, boolean userCreatedOnly, boolean directCreatable, Cursor cursor, SortOrder sortOrder, boolean excludeInternals, DatabaseConnection database) throws Exception
    {
        ClauseOperator op = ClauseOperator.MatchAll;
        List<Parameter> params = new ArrayList<Parameter>();

        Parameter p = new Parameter();
        p.fieldName = SearchFieldConstants.NAME;
        p.fieldValue = text;
        p.operator = Operator.StartsWith;
        p.dataShape = shape;
        params.add(p);

        if (excludeInternals)
        {
            p = new Parameter();
            p.fieldName = SearchFieldConstants.IS_INTERNAL;
            p.operator = Operator.Equals;
            p.fieldValue = "0";
            params.add(p);
        }

        return getInstancesByIndex(instanceClass, params, op, user, userCreatedOnly, directCreatable, cursor, sortOrder, database);
    }

    private static String buildQueryExpression(List<Parameter> params, ClauseOperator clauseOperator,
                                            User user, boolean userCreatedOnly, boolean directCreatable) {

        List<String> terms = new ArrayList<String>();
        String finalTerm = " AND !dataTypeId:600";
        if (directCreatable) {
            finalTerm += " AND direct:1";
        }

        for (Parameter param : params)
        {
            Shape shape = param.dataShape;
            String typeTerm = "";
            if (shape != null) {
                if (!shape.getId().equals(Shape.REFERENCE_ID)) {
                    typeTerm = " AND dataTypeId:" + shape.getId();
                }
            }

            if (param.fieldName.equalsIgnoreCase(SearchFieldConstants.REFERENCE)) {
                String term = "refId:" + param.fieldValue + " AND !dataTypeId:500 AND !dataTypeId:600 AND !dataTypeId:1000";
                terms.add(term);
                finalTerm = "";
            } else if (param.fieldName.equalsIgnoreCase(SearchFieldConstants.ID)) {
                String term = "instanceid:" + param.fieldValue;
                terms.add(term);
                finalTerm = "";
            } else if (param.fieldName.equalsIgnoreCase(SearchFieldConstants.SHAPE)) {
                String op = "";
                if (param.operator == Operator.NotReference) {
                    op = "marker:marker AND !";
                }

                String term;
                if (shape != null && shape.getName().equals("Shape") && directCreatable) {
                    term = "direct:1 AND " + op + "dataTypeId:" + param.fieldValue;
                } else {
                    term = op + "dataTypeId:" + param.fieldValue;
                }

                terms.add(term);
            } else if (param.fieldName.equalsIgnoreCase(SearchFieldConstants.NAME)) {
                String name = param.fieldValue.toString().trim().toLowerCase();
                String term = "marker:marker";
                if (name.length() > 0)
                {
                    term = "name:";

                    switch (param.operator)
                    {
                        case Contains:
                            term += "\"" + name + "\"";
                            break;

                        case StartsWith:
                            term += name + "*";
                            break;

                        case Like:
                            term += name + "~0.75";
                            break;
                    }
                }

                terms.add("(" + term + typeTerm + ")");

            }
            else if (param.fieldName.equalsIgnoreCase(SearchFieldConstants.DESCRIPTION))
            {
                String name = param.fieldValue.toString().trim().toLowerCase();
                String term = "marker:marker";
                if (name.length() > 0)
                {
                    term = "description:";

                    switch (param.operator)
                    {
                        case Contains:
                            term += "\"" + name + "\"";
                            break;

                        case StartsWith:
                            term += name + "*";
                            break;

                        case Like:
                            term += name + "~0.75";
                            break;
                    }
                }

                terms.add("(" + term + typeTerm + ")");

            }
            else if (param.fieldName.equalsIgnoreCase(SearchFieldConstants.TAG))
            {
                String term = "";
                switch (param.operator)
                {
                    case Reference:
                        term = "tagId:" + param.fieldValue.toString();
                        break;
                    case NotReference:
                        term = "(marker:marker AND !tagId:" + param.fieldValue.toString() + ")";
                        break;
                }
                terms.add("(" + term + typeTerm + ")");
            }
            else if (param.fieldName.equalsIgnoreCase(SearchFieldConstants.TAG_USER))
            {
                String term = "";
                switch (param.operator)
                {
                    case Reference:
                        term = "tagUser" + param.fieldValue.toString() + "Id:\"" + user.getId() + "\"";
                        break;
                }
                terms.add("(" + term + typeTerm + ")");
            }

            else if (param.fieldName.equalsIgnoreCase(SearchFieldConstants.PARENT))
            {
                String term = "";
                switch (param.operator)
                {
                    case Reference:
                        term = "parentId:" + param.fieldValue.toString();
                        break;
                    case NotReference:
                        term = "(marker:marker AND !parentId:" + param.fieldValue.toString() + ")";
                        break;
                }
                terms.add("(" + term + typeTerm + ")");
            }
            else if (param.fieldName.equalsIgnoreCase(SearchFieldConstants.USER))
            {
                String term = "";
                String sval = param.fieldValue.toString();
                switch (param.operator)
                {
                    case Reference:
                        term = "updatedById:" + param.fieldValue.toString();
                        break;
                    case NotReference:
                        term = "(marker:marker AND !updatedById:" + param.fieldValue.toString() + ")";
                        break;
                    case ContainedIn:
                        term = "(";
                        String[] list = sval.split(",");
                        String op = "";
                        for (String id : list) {
                            term += op + "updatedById:" + id;
                            op = " OR ";
                        }
                        term += ")";
                        break;
                    case NotContainedIn:
                        term = "(marker:marker AND (";
                        list = sval.split(",");
                        op = "";
                        for (String id : list) {
                            term += op + "!updatedById:" + id;
                            op = " AND ";
                        }
                        term += "))";
                        break;
                }
                terms.add("(" + term + typeTerm + ")");
            }
            else if (param.fieldName.equalsIgnoreCase(SearchFieldConstants.COMMUNITY))
            {
                String term = "";
                switch (param.operator)
                {
                    case Reference:
                        term = "updatedByCommunityId:" + param.fieldValue.toString();
                        break;
                    case NotReference:
                        term = "(marker:marker AND !updatedByCommunityId:" + param.fieldValue.toString() + ")";
                        break;
                }
                terms.add("(" + term + typeTerm + ")");
            }
            else if (param.fieldName.equalsIgnoreCase(SearchFieldConstants.IS_INTERNAL))
            {
                String term = "internalType:";
                term += param.fieldValue.toString();
                terms.add("(" + term + typeTerm + ")");
            }
            else if (param.fieldName.equalsIgnoreCase(SearchFieldConstants.ACTIVITY))
            {
                String term = "latestActivity:";
                long end = new Date().getTime();
                long start = end;
                Date value = (Date)param.fieldValue;

                switch (param.operator)
                {
                    case Past24Hours:
                        start = end - MSEC_24HOURS;
                        break;
                    case Past3Days:
                        start = end - MSEC_3DAYS;
                        break;
                    case Past7Days:
                        start = end - MSEC_7DAYS;
                        break;
                    case Past30Days:
                        start = end - MSEC_30DAYS;
                        break;
                    case BeforeDate:
                        value.setHours(23);
                        value.setMinutes(59);
                        value.setSeconds(59);
                    	start = 0;
                    	end = value.getTime() - MSEC_24HOURS;
                    	break;
                    case OnOrBeforeDate:
                        value.setHours(23);
                        value.setMinutes(59);
                        value.setSeconds(59);
                    	start = 0;
                    	end = value.getTime();
                    	break;
                    case OnDate:
                        {
                            Date dt = new Date();
                            dt.setTime(value.getTime());
                            dt.setHours(0);
                            dt.setMinutes(0);
                            dt.setSeconds(0);
                            start = dt.getTime();
                            dt = new Date();
                            dt.setTime(value.getTime());
                            dt.setHours(23);
                            dt.setMinutes(59);
                            dt.setSeconds(59);
                            end = dt.getTime();
                        }
                    	break;
                    case AfterDate:
                        value.setHours(0);
                        value.setMinutes(0);
                        value.setSeconds(0);
                    	start = value.getTime() + MSEC_24HOURS;
                    	end = MAXDATE;
                    	break;
                    case OnOrAfterDate:
                        value.setHours(0);
                        value.setMinutes(0);
                        value.setSeconds(0);
                    	start = value.getTime();
                    	end = MAXDATE;
                    	break;
                }

                term += "[" + Tools.longToNumeric(start) + " TO " + Tools.longToNumeric(end) + "]";
                terms.add("(" + term + typeTerm + ")");
            }
            else if (param.fieldName.equalsIgnoreCase(SearchFieldConstants.CREATED))
            {
                String term = "updated:";
                long end = new Date().getTime();
                long start = end;
                Date value = (Date)param.fieldValue;

                switch (param.operator)
                {
                    case Past24Hours:
                        start = end - MSEC_24HOURS;
                        break;
                    case Past3Days:
                        start = end - MSEC_3DAYS;
                        break;
                    case Past7Days:
                        start = end - MSEC_7DAYS;
                        break;
                    case Past30Days:
                        start = end - MSEC_30DAYS;
                        break;
                    case BeforeDate:
                        value.setHours(23);
                        value.setMinutes(59);
                        value.setSeconds(59);
                    	start = 0;
                    	end = value.getTime() - MSEC_24HOURS;
                    	break;
                    case OnOrBeforeDate:
                        value.setHours(23);
                        value.setMinutes(59);
                        value.setSeconds(59);
                    	start = 0;
                    	end = value.getTime();
                    	break;
                    case OnDate:
                        {
                            Date dt = new Date();
                            dt.setTime(value.getTime());
                            dt.setHours(0);
                            dt.setMinutes(0);
                            dt.setSeconds(0);
                            start = dt.getTime();
                            dt = new Date();
                            dt.setTime(value.getTime());
                            dt.setHours(23);
                            dt.setMinutes(59);
                            dt.setSeconds(59);
                            end = dt.getTime();
                        }
                    	break;
                    case AfterDate:
                        value.setHours(0);
                        value.setMinutes(0);
                        value.setSeconds(0);
                    	start = value.getTime() + MSEC_24HOURS;
                    	end = MAXDATE;
                    	break;
                    case OnOrAfterDate:
                        value.setHours(0);
                        value.setMinutes(0);
                        value.setSeconds(0);
                    	start = value.getTime();
                    	end = MAXDATE;
                    	break;
                }

                term += "[" + Tools.longToNumeric(start) + " TO " + Tools.longToNumeric(end) + "]";
                terms.add("(" + term + typeTerm + ")");
            }
            else if (param.fieldName.equalsIgnoreCase(SearchFieldConstants.EXPIRED))
            {
                String term = "expiration:";
                String minString = Tools.minNumeric();
                long now = new Date().getTime();
                String valueString = Tools.longToNumeric(now);
                term += "{" + minString + " TO " + valueString + "}";
                terms.add("(" + term + typeTerm + ")");
            }
            else if (param.fieldName.equalsIgnoreCase(SearchFieldConstants.AVG_RATING))
            {
                float val = Float.parseFloat(param.fieldValue.toString());
                String value = Tools.floatToNumeric(val);
                String zero = Tools.floatToNumeric(0.0f);
                String five = Tools.floatToNumeric(5.0f);
                String fiveplus = Tools.floatToNumeric(6.0f);

                String term = "averageRating:";

                switch(param.operator)
                {
                    case Equals:
                        term += "\"" + value + "\"";
                        break;
                    case LessThan:
                        term += "{\"" + zero + "\" TO \"" + value + "\"}";
                        break;
                    case LessThanOrEquals:
                        term += "[\"" + zero + "\" TO \"" + value + "\"]";
                        break;
                    case GreaterThan:
                        term += "{\"" + value + "\" TO \"" + fiveplus + "\"}";
                        break;
                    case NotEquals:
                        term = "(marker:marker AND !" + term + "\"" + value + "\")";
                        break;
                    case GreaterThanOrEquals:
                        term += "[\"" + value + "\" TO \"" + five + "\"]";
                        break;
                }

                terms.add("(" + term + typeTerm + ")");
            } else if (shape != null) {
                DataField field;
                Shape localShape = shape;
                if (param.subShape != null) {
                    localShape = param.subShape;
                }

                field = localShape.getField(param.fieldName);

                String fieldName = "field" + field.getId();
                String term = "";

                if (field.getShape().isPrimitive())
                {
                    term = fieldName + ":";

                    Shape fieldShape = field.getShape();
                    if (fieldShape.isDate()) {
                        Date value = (Date)param.fieldValue;
                        
                        // start and end begin their life as the current date
                        Date dt = new Date();
                        dt.setHours(0);
                        dt.setMinutes(0);
                        dt.setSeconds(0);
                        long end = dt.getTime();
                        long start = end;

                        switch (param.operator)
                        {
                            case Past24Hours:
                                start = end - MSEC_24HOURS;
                                break;
                            case Past3Days:
                                start = end - MSEC_3DAYS;
                                break;
                            case Past7Days:
                                start = end - MSEC_7DAYS;
                                break;
                            case Past30Days:
                                start = end - MSEC_30DAYS;
                                break;
                            case BeforeDate:
                                value.setHours(23);
                                value.setMinutes(59);
                                value.setSeconds(59);
                                start = 0;
                                end = value.getTime() - MSEC_24HOURS;
                                break;
                            case OnOrBeforeDate:
                                value.setHours(23);
                                value.setMinutes(59);
                                value.setSeconds(59);
                                start = 0;
                                end = value.getTime();
                                break;
                            case OnDate:
                                {
                                    Date startDate = new Date();
                                    startDate.setTime(value.getTime());
                                    startDate.setHours(0);
                                    startDate.setMinutes(0);
                                    startDate.setSeconds(0);
                                    start = startDate.getTime();
                                    Date endDate = new Date();
                                    endDate.setTime(value.getTime());
                                    endDate.setHours(23);
                                    endDate.setMinutes(59);
                                    endDate.setSeconds(59);
                                    end = endDate.getTime();
                                }
                                break;
                            case AfterDate:
                                value.setHours(0);
                                value.setMinutes(0);
                                value.setSeconds(0);
                                start = value.getTime() + MSEC_24HOURS;
                                end = MAXDATE;
                                break;
                            case OnOrAfterDate:
                                value.setHours(0);
                                value.setMinutes(0);
                                value.setSeconds(0);
                                start = value.getTime();
                                end = MAXDATE;
                                break;
                        }

                        term += "[" + Tools.longToNumeric(start) + " TO " + Tools.longToNumeric(end) + "]";
                        terms.add("(" + term + typeTerm + ")");
                    }
                    else if (fieldShape.isBoolean())
                    {
                    	float value = (float)(param.operator == Operator.IsYes ? 1.0 : 0.0);
                        String valString = Tools.floatToNumeric(value);
                        term += "\"" + valString + "\"";
                    }
                    else if (fieldShape.isNumeric())
                    {
                        String value = param.fieldValue.toString().trim().toLowerCase();
                        String valString = Tools.floatToNumeric(Float.parseFloat(value));
                        String minString = Tools.minNumeric();
                        String maxString = Tools.maxNumeric();
                        switch (param.operator)
                        {
                            case Equals:
                                term += "\"" + valString + "\"";
                                break;
                            case LessThan:
                                term += "{" + minString + " TO " + valString + "}";
                                break;
                            case LessThanOrEquals:
                                term += "[" + minString + " TO " + valString + "]";
                                break;
                            case GreaterThan:
                                term += "{" + valString + " TO " + maxString + "}";
                                break;
                            case NotEquals:
                                term = "(marker:marker AND !" + term + "\"" + valString + "\")";
                                break;
                            case GreaterThanOrEquals:
                                term += "[" + valString + " TO " + maxString + "]";
                                break;
                        }
                    }
                    else if (fieldShape.getId().equals(Shape.TEXT_ID) || fieldShape.getId().equals(Shape.RICHTEXT_ID))
                    {
                        String value = param.fieldValue.toString().trim().toLowerCase();
                        if (value.length() > 0) {
                            switch (param.operator)
                            {
                                case Contains:
                                    term += "\"" + value + "\"";
                                    break;

                                case StartsWith:
                                    term += value + "*";
                                    break;

                                case Like:
                                    term += value + "~0.75";
                                    break;
                            }
                        }
                    }
                }
                else
                {

                    switch (param.operator)
                    {
                        case Reference:
                            term = fieldName + "Id:\"" + param.fieldValue.toString() + "\"";
                            break;
                        case NotReference:
                            term = "(marker:marker AND !" + fieldName + "Id:\"" + param.fieldValue.toString() + "\")";
                            break;
                    }
                }

                terms.add("(" + term + typeTerm + ")");
            }
        }

        String op = " AND ";
        if (clauseOperator == ClauseOperator.MatchAny)
        {
            op = " OR ";
        }

        String operator = "";

        StringBuilder buffer = new StringBuilder();
        for (String term : terms)
        {
            buffer.append(operator).append(term);
            operator = op;
        }

        String expression = buffer.toString();

        // apply the visibility check for the user
        if (user == null)
        {
            // no data for you!
            String visCheck = "(communityId:-1)";
            if (expression.length() == 0)
            {
                expression = visCheck;
            }
            else
            {
                expression = "(" + expression + ") AND " + visCheck;
            }
        }
        else if (!user.isAdmin())
        {
            // a user can see data that is scoped Globally, data that is not scoped at all,
            // scoped to them privately, and scoped to communities the user belongs to
            String visCheck = "(communityId:" + Community.Global.getId() +
                    " or communityId:" + user.getId();
            List<Community> communities = user.getCommunities();
            for (Community community : communities) {
                visCheck += " or communityId:" + community.getId();
            }

            visCheck += ")";

            if (expression.length() == 0)
            {
                expression = visCheck;
            }
            else
            {
                expression = "(" + expression + ") AND " + visCheck;
            }

            if (userCreatedOnly) {
                expression += " AND createdById:" + user.getId();
            }

        }

        if (expression.length() == 0) {
            expression = "marker:marker";
        }

        expression = "(" + expression + ")" + finalTerm;

        return expression;
    }

    private static Hits getInstanceHitsByIndex(MultiSearcher searcher, List<Parameter> params, ClauseOperator clauseOperator,
                                            User user, boolean userCreatedOnly, boolean directCreatable) throws Exception
    {
        try
        {
            ChimeAnalyzer analyzer = new ChimeAnalyzer();

            String expression = buildQueryExpression(params, clauseOperator, user, userCreatedOnly, directCreatable);

            Logger.getLogger(DataInstanceUtils.class).info("Hits Query Expression:");
            Logger.getLogger(DataInstanceUtils.class).info(expression);

            QueryParser parser = new QueryParser("name", analyzer);
            Query query = parser.parse(expression);

            Sort sort = Sort.RELEVANCE;

            Hits hits = searcher.search(query, sort);
            return hits;
        }
        catch (Exception e)
        {
            throw e;
        }

    }

    private static List<String> getInstanceHitIds(final MultiSearcher searcher, List<Parameter> params, ClauseOperator clauseOperator,
                                            User user, boolean userCreatedOnly, boolean directCreatable) throws Exception
    {
        try
        {
            final List<String> ids = new ArrayList<String>();

            ChimeAnalyzer analyzer = new ChimeAnalyzer();

            String expression = buildQueryExpression(params, clauseOperator, user, userCreatedOnly, directCreatable);

            _logger.info("Hits Query Expression:");
            _logger.info(expression);

            QueryParser parser = new QueryParser("name", analyzer);
            Query query = parser.parse(expression);

            Sort sort = Sort.RELEVANCE;

            searcher.search(query,
                new HitCollector() {

                    @Override
                    public void collect(int doc, float score) {
                        try {
                            ids.add(searcher.doc(doc).get("instanceid"));
                        } catch (Exception e) {

                        }
                    }

                }
            );

            return ids;
        }
        catch (Exception e)
        {
            throw e;
        }

    }

    private static TopFieldDocs getInstanceHitsByIndex(IndexReader[] readers, MultiSearcher searcher, List<Parameter> params, ClauseOperator clauseOperator,
                                            User user, boolean userCreatedOnly, boolean directCreatable,  SortOrder sortOrder, int maxHits) throws Exception
    {
        try
        {
            ChimeAnalyzer analyzer = new ChimeAnalyzer();

            String expression = buildQueryExpression(params, clauseOperator, user, userCreatedOnly, directCreatable);

            _logger.info("Hits Query Expression:");
            _logger.info(expression);

            QueryParser parser = new QueryParser("name", analyzer);
            Query query = parser.parse(expression);

            Sort sort = Sort.RELEVANCE;

            if (sortOrder != SortOrder.ByRelevance) {
                sort = new Sort();
                switch (sortOrder)
                {
                    case ByName:
                        sort.setSort(new SortField("fullName", SortField.STRING));
                        break;
                    case ByRating:
                        sort.setSort(new SortField("averageRating", SortField.STRING, true));
                        break;
                    case ByMostActive:
                        sort.setSort(new SortField("latestActivity", SortField.STRING, true));
                        break;
                    case ByMostUseful:
                        sort.setSort(new SortField("ranking", SortField.STRING, true));
                        break;
                    case ByMostRecentEdit:
                        sort.setSort(new SortField("updated", SortField.STRING, true));
                        break;
                    case ByMostRecentCreate:
                        sort.setSort(new SortField("created", SortField.STRING, true));
                        break;
                }
            }


            TopFieldDocs hits = searcher.search(query, null, maxHits, sort);
            //Hits hits = searcher.search(query, sort);
            return hits;
        }
        catch (Exception e)
        {
            throw e;
        }

    }

    static boolean firstTime = true;
    static boolean loadExtensions = true;
    static IndexReader[] readers;

    public static List<String> getInstanceHitIds(Class instanceClass, List<Parameter> params, ClauseOperator clauseOperator,
                       User user, boolean userCreatedOnly, boolean directCreatable) throws Exception {

        MultiSearcher searcher = getSearcher();
        List<String> hits = getInstanceHitIds(searcher, params, clauseOperator, user, userCreatedOnly, directCreatable);
        searcher.close();

        return hits;
    }

    public static Hits getInstanceHits(Class instanceClass, List<Parameter> params, ClauseOperator clauseOperator,
                       User user, boolean userCreatedOnly, boolean directCreatable) throws Exception {

        MultiSearcher searcher = getSearcher();
        Hits hits = getInstanceHitsByIndex(searcher, params, clauseOperator, user, userCreatedOnly, directCreatable);
        searcher.close();

        return hits;
    }

    private static MultiSearcher getSearcher() throws Exception {
        String[] indexNames = Indexer.getIndexNames();

        if (loadExtensions) {
            List<IndexReader> extReaders = new ArrayList<IndexReader>();
            ChimeExtensionManager extMgr = ChimeExtensionManager.instance();
            if (extMgr != null){
	            for (ChimeExtension ext : extMgr.getExtensions()) {
	                MemoryIndexer indexer = ext.getMemoryIndexer();
	                if (indexer != null) {
	                    if (indexer instanceof ChimeMemoryIndexer) {
	                        ChimeMemoryIndexer memIndexer = (ChimeMemoryIndexer)indexer;
	                        RAMDirectory ramdir = memIndexer.getRamDirectory();
	                        IndexReader reader = IndexReader.open(ramdir);
	                        extReaders.add(reader);
	                    }
	                }
	            }
	            loadExtensions = false;
            }

            readers = new IndexReader[indexNames.length + extReaders.size()];
            int j = 0;
            for (int i = indexNames.length; i < readers.length; i++) {
                readers[i] = extReaders.get(j++);
            }
        }

        Searcher[] searchers = new Searcher[readers.length];
        MultiSearcher searcher = null;
        for (int i = 0; i < searchers.length; i++) {
            if (firstTime && i < indexNames.length) {
                readers[i] = IndexReader.open("index/" + indexNames[i]);
            } else {
                IndexReader newReader = readers[i].reopen();
                if (newReader != readers[i]) {
                    readers[i].close();
                    readers[i] = newReader;
                }
            }

            Searcher s = new IndexSearcher(readers[i]);
            searchers[i] = s;
        }


        searcher = new MultiSearcher(searchers);
        if (!loadExtensions) firstTime = false;
        return searcher;
    }

    public static InstancesResponse getInstancesByIndex(Class instanceClass, List<Parameter> params, ClauseOperator clauseOperator,
                                            User user, boolean userCreatedOnly, boolean directCreatable, Cursor cursor, SortOrder sortOrder, DatabaseConnection database) throws Exception
    {
        int total = 0;
        boolean limited = false;
        List<DataInstance> results = new ArrayList<DataInstance>();

        try {
            MultiSearcher searcher = getSearcher();
            long t1 = System.currentTimeMillis();
            TopFieldDocs hits = getInstanceHitsByIndex(readers, searcher, params, clauseOperator, user, userCreatedOnly, directCreatable, sortOrder, 1001);
            long elapsed = System.currentTimeMillis() - t1;
            _logger.info("Initial Query Time: " + elapsed + " msecs");

            //t1 = System.currentTimeMillis();
            //hits = getInstanceHitsByIndex(readers, searcher, params, clauseOperator, user, userCreatedOnly, dataType, sortOrder);
            int cnt = hits.scoreDocs.length;
            //elapsed = System.currentTimeMillis() - t1;
            //_logger.info("Secondary Query Time: " + elapsed + " msecs");

            _logger.info("Found " + cnt + " hit(s).");
            limited = cnt > ROWLIMIT;

            total = cnt;
            if (limited)
            {
                total = ROWLIMIT;
            }

            int start = 0;
            int end = cnt;

            if (cursor != null)
            {
                start = cursor.getFirst();
                end = start + cursor.getMax();
            }

            for (int i = start; i < end; i++)
            {
                Document doc = searcher.doc(hits.scoreDocs[i].doc);
                String id = doc.get("instanceid");
                String name = doc.get("name");
                String description = doc.get("description");
                String ratingCount = doc.get("ratingCount");
                String commentCount = doc.get("commentCount");
                String tagCount = doc.get("tagCount");
                String averageRating = doc.get("averageRating");
                String ranking = doc.get("ranking");
                String positiveCount = doc.get("positiveCount");
                String negativeCount = doc.get("negativeCount");
                String created = doc.get("created");
                String createdBy = doc.get("createdById");
                String createdByName = doc.get("createdByName");
                String updated = doc.get("updated");
                String updatedBy = doc.get("updatedById");
                String updatedByName = doc.get("updatedByName");
                String expiration = doc.get("expiration");
                String tagged = doc.get("tagged");
                String taggedBy = doc.get("taggedById");
                String taggedByName = doc.get("taggedByName");
                String taggedAction = doc.get("taggedAction");
                String reviewed = doc.get("reviewed");
                String reviewedBy = doc.get("reviewedById");
                String reviewedByName = doc.get("reviewedByName");
                String reviewedAction = doc.get("reviewedAction");
                String commented = doc.get("commented");
                String commentedBy = doc.get("commentedById");
                String commentedByName = doc.get("commentedByName");

                boolean isTransient = doc.get("transient").equals("Y");

                String[] typeIds = doc.getValues("dataTypeId");

                DataInstance instance = (DataInstance)instanceClass.newInstance();
                instance.setId(InstanceId.create(id));
                instance.setTransient(isTransient);
                instance.setName(name);
                instance.setDescription(description);

                for (int j = 0; j < typeIds.length; j++) {
                    String tid = typeIds[j];
                    Shape t = ShapeUtils.getInstanceById(InstanceId.create(tid), database, true);
                    instance.addShape(t);
                }

                instance.setAverageRating(Tools.numericToFloat(averageRating));
                instance.setRatingCount(Integer.parseInt(ratingCount));
                instance.setRanking((int)Tools.numericToLong(ranking));
                instance.setPositiveCount(Integer.parseInt(positiveCount));
                instance.setNegativeCount(Integer.parseInt(negativeCount));
                instance.setCommentCount(Integer.parseInt(commentCount));
                instance.setTagCount(Integer.parseInt(tagCount));

                instance.setCreated(new Date(Tools.numericToLong(created)));
                User u = new User();
                u.setId(InstanceId.create(createdBy));
                u.setName(createdByName);
                instance.setCreatedBy(u);

                instance.setUpdated(new Date(Tools.numericToLong(updated)));

                if (expiration != null) {
                    instance.setExpiration(new Date(Tools.numericToLong(expiration)));
                }

                u = new User();
                u.setId(InstanceId.create(updatedBy));
                u.setName(updatedByName);
                instance.setUpdatedBy(u);

                if (tagged != null)
                {
                    instance.setTagged(new Date(Tools.numericToLong(tagged)));
                    u = new User();
                    u.setId(InstanceId.create(taggedBy));
                    u.setName(taggedByName);
                    instance.setTaggedBy(u);
                    instance.setTaggedAction(TagAction.valueOf(taggedAction));
                }

                if (reviewed != null)
                {
                    instance.setReviewed(new Date(Tools.numericToLong(reviewed)));
                    u = new User();
                    u.setId(InstanceId.create(reviewedBy));
                    u.setName(reviewedByName);
                    instance.setReviewedBy(u);
                    instance.setReviewedAction(ReviewAction.valueOf(reviewedAction));
                }

                if (commented != null)
                {
                    instance.setCommented(new Date(Tools.numericToLong(commented)));
                    u = new User();
                    u.setId(InstanceId.create(commentedBy));
                    u.setName(commentedByName);
                    instance.setCommentedBy(u);
                }

                results.add(instance);
            }

            searcher.close();
        }
        catch (Exception e)
        {
            int x = 1;
        }

        // create the return cursor
        Cursor newCursor = null;

        if (cursor != null)
        {
            newCursor = new Cursor(cursor.getFirst(), results.size(), cursor.getMax(), total, limited);
        }

        InstancesResponse response = new InstancesResponse();
        response.list = results;
        response.cursor = newCursor;

        return response;
    }

    public static InstancesResponse getInstancesByKeyword(String keywords, User user, Cursor cursor, SortOrder sortOrder, boolean smallQuery, DatabaseConnection database) throws Exception
    {
        // first we do a name search to get the top 100 hits by relevance.  These are the ids
        // that we'll use in the search term for the refId field.
        //
        // if the phrase starts with [xxxxx], then the xxxxx is the name of the data shape of all instances to find
        String kwords = keywords.trim();
        String[] words = new String[] { };
        Shape searchType = null;
        if (kwords.startsWith("[") && kwords.length() > 2) {
            int last = kwords.indexOf("]");
            if (last != -1) {

                String tname = kwords.substring(1, last);
                try {
                    searchType = ShapeUtils.getInstance(tname, database, true);
                    words = kwords.substring(last + 1).trim().split(" ");
                } catch (Exception e) {
                    // bad data shape?  that's ok, just ignore it
                }
            }
        } else {
            words = kwords.split(" ");
        }

        int cnt = words.length;
        for (int i = 0; i < cnt; i++) {
            words[i] = words[i].toLowerCase();
        }

        List<Parameter> params = new ArrayList<Parameter>();
        StringBuilder buf = new StringBuilder();
        String op = "";
        for (String word : words) {
            if (!word.equals("")) {
                Parameter param = new Parameter();
                if (word.startsWith("[") && word.length() > 2) {
                } else {
                    param.fieldName = SearchFieldConstants.NAME;
                    param.operator = Operator.Contains;
                    param.fieldValue = word;
                    param.dataShape = searchType;
                    params.add(param);

                    buf.append(op).append(word);
                    op = "+";
                }

            }
        }

        String combinedWords = buf.toString().toLowerCase();
        Parameter param = new Parameter();
        param.fieldName = SearchFieldConstants.NAME;
        param.operator = Operator.Contains;
        param.fieldValue = combinedWords + "^10";
        params.add(0, param);

        List<DataInstance> results = new ArrayList<DataInstance>();
        cnt = 0;
        int total = 0;
        boolean limited = false;

        try
        {
            MultiSearcher searcher = getSearcher();

            TopFieldDocs referenceHits = getInstanceHitsByIndex(readers, searcher, params, ClauseOperator.MatchAny, user, false, false, sortOrder, 101);
            int len = referenceHits.scoreDocs.length;
            if (len > 100) {
                len = 100;
            }
            String[] referenceIds = new String[len];
            for (int i = 0; i < len; i++) {
                Document doc = searcher.doc(referenceHits.scoreDocs[i].doc);
                referenceIds[i] = doc.get("instanceid");
            }

            ChimeAnalyzer analyzer = new ChimeAnalyzer();

            List<String> terms = new ArrayList<String>();

            terms.add("name:" + combinedWords + "^40");
            for (String word : words) {
                if (!word.startsWith("#")) {
                    terms.add("name:" + word + "^25");
                }
            }

            terms.add("description:" + combinedWords + "^20");
            for (String word : words) {
                if (!word.startsWith("#")) {
                    terms.add("description:" + word + "^15");
                }
            }

            terms.add("content:" + combinedWords + "^10");
            for (String word : words) {
                if (!word.startsWith("#")) {
                    terms.add("content:" + word + "^5");
                }
            }

            for (String id : referenceIds) {
               terms.add("refId:" + id);
            }

            op = " OR ";

            String operator = "";

            StringBuilder buffer = new StringBuilder();
            for (String term : terms)
            {
                buffer.append(operator).append(term);
                operator = op;
            }

            QueryParser parser = new QueryParser("name", analyzer);
            String expression = buffer.toString();

            // apply the visibility check for the user
            if (user == null)
            {
                // no data for you
                String visCheck = "(communityId:-1)";
                if (expression.length() == 0)
                {
                    expression = visCheck;
                }
                else
                {
                    expression = "(" + expression + ") AND " + visCheck;
                }
            }
            else
            {
                if (!user.isAdmin()) {
                    // a user can see data that is scoped Globally, data that is not scoped at all,
                    // scoped to them privately, and scoped to communities the user belongs to
                    String visCheck = "(communityId:" + Community.Global.getId() +
                            " or communityId:" + user.getId();
                    List<Community> communities = user.getCommunities();
                    for (Community community : communities) {
                        visCheck += " or communityId:" + community.getId();
                    }

                    visCheck += ")";

                    if (expression.length() == 0)
                    {
                        expression = visCheck;
                    }
                    else
                    {
                        expression = "(" + expression + ") AND " + visCheck;
                    }

                }
            }

            if (expression.length() == 0)
            {
                expression = "marker:marker AND !dataTypeId:600";
            }
            else
            {
                //expression = "marker:marker AND !(dataTypeId:500 OR dataTypeId:600 OR dataTypeId:1000) AND (" + expression + ")";
                expression = "!dataTypeId:600 AND (" + expression + ")";
            }

            if (searchType != null) {
                expression = expression + " AND dataTypeId:" + searchType.getId();
            }

            _logger.info("Query Expression:");
            _logger.info(expression);

            Query query = parser.parse(expression);


            Sort sort = Sort.RELEVANCE;

            if (sortOrder != SortOrder.ByRelevance) {
                sort = new Sort();
                switch (sortOrder)
                {
                    case ByName:
                        sort.setSort(new SortField("fullName", SortField.STRING));
                        break;
                    case ByRating:
                        sort.setSort(new SortField("averageRating", SortField.STRING, true));
                        break;
                    case ByMostActive:
                        sort.setSort(new SortField("latestActivity", SortField.STRING, true));
                        break;
                    case ByMostUseful:
                        sort.setSort(new SortField("ranking", SortField.STRING, true));
                        break;
                }
            }

            //Hits hits = searcher.search(query, sort);
            int limit = ROWLIMIT;
            int maxHits = ROWLIMIT + 1;
            if (smallQuery) {
                limit = 100;
                maxHits = 101;
            }

            TopFieldDocs hits = searcher.search(query, null, maxHits, sort);

            cnt = hits.scoreDocs.length;

            _logger.info("Found " + cnt + " hit(s).");
            limited = cnt > limit;

            total = cnt;
            if (limited)
            {
                total = limit;
            }

            int start = 0;
            int end = cnt;

            if (cursor != null)
            {
                start = cursor.getFirst();
                end = start + cursor.getMax();
            }

            for (int i = start; i < end; i++)
            {
                Document doc = searcher.doc(hits.scoreDocs[i].doc);
                String id = doc.get("instanceid");
                String name = doc.get("name");
                String description = doc.get("description");
                String ratingCount = doc.get("ratingCount");
                String commentCount = doc.get("commentCount");
                String tagCount = doc.get("tagCount");
                String averageRating = doc.get("averageRating");
                String ranking = doc.get("ranking");
                String positiveCount = doc.get("positiveCount");
                String negativeCount = doc.get("negativeCount");
                String created = doc.get("created");
                String createdBy = doc.get("createdById");
                String createdByName = doc.get("createdByName");
                String updated = doc.get("updated");
                String updatedBy = doc.get("updatedById");
                String updatedByName = doc.get("updatedByName");
                String tagged = doc.get("tagged");
                String taggedBy = doc.get("taggedById");
                String taggedByName = doc.get("taggedByName");
                String taggedAction = doc.get("taggedAction");
                String reviewed = doc.get("reviewed");
                String reviewedBy = doc.get("reviewedById");
                String reviewedByName = doc.get("reviewedByName");
                String reviewedAction = doc.get("reviewedAction");
                String commented = doc.get("commented");
                String commentedBy = doc.get("commentedById");
                String commentedByName = doc.get("commentedByName");
                boolean isTransient = doc.get("transient").equals("Y");


                String[] typeIds = doc.getValues("dataTypeId");

                DataInstance instance = (DataInstance)DataInstance.class.newInstance();
                instance.setId(InstanceId.create(id));
                instance.setTransient(isTransient);
                instance.setName(name);
                instance.setDescription(description);

                for (int j = 0; j < typeIds.length; j++) {
                    String tid = typeIds[j];
                    Shape t = ShapeUtils.getInstanceById(InstanceId.create(tid), database, true);
                    instance.addShape(t);
                }

                instance.setAverageRating(Tools.numericToFloat(averageRating));
                instance.setRatingCount(Integer.parseInt(ratingCount));
                instance.setRanking((int)Tools.numericToLong(ranking));
                instance.setPositiveCount(Integer.parseInt(positiveCount));
                instance.setNegativeCount(Integer.parseInt(negativeCount));
                instance.setCommentCount(Integer.parseInt(commentCount));
                instance.setTagCount(Integer.parseInt(tagCount));

                instance.setCreated(new Date(Tools.numericToLong(created)));
                User u = new User();
                u.setId(InstanceId.create(createdBy));
                u.setName(createdByName);
                instance.setCreatedBy(u);

                instance.setUpdated(new Date(Tools.numericToLong(updated)));
                u = new User();
                u.setId(InstanceId.create(updatedBy));
                u.setName(updatedByName);
                instance.setUpdatedBy(u);

                if (tagged != null)
                {
                    instance.setTagged(new Date(Tools.numericToLong(tagged)));
                    u = new User();
                    u.setId(InstanceId.create(taggedBy));
                    u.setName(taggedByName);
                    instance.setTaggedBy(u);
                    instance.setTaggedAction(TagAction.valueOf(taggedAction));
                }

                if (reviewed != null)
                {
                    instance.setReviewed(new Date(Tools.numericToLong(reviewed)));
                    u = new User();
                    u.setId(InstanceId.create(reviewedBy));
                    u.setName(reviewedByName);
                    instance.setReviewedBy(u);
                    instance.setReviewedAction(ReviewAction.valueOf(reviewedAction));
                }

                if (commented != null)
                {
                    instance.setCommented(new Date(Tools.numericToLong(commented)));
                    u = new User();
                    u.setId(InstanceId.create(commentedBy));
                    u.setName(commentedByName);
                    instance.setCommentedBy(u);
                }

                results.add(instance);
            }

            //for (IndexReader reader : readers) {
            //    reader.close();
            //}
        }
        catch (Exception e)
        {
            int x = 1;
        }

        // create the return cursor
        Cursor newCursor = null;

        if (cursor != null)
        {
            newCursor = new Cursor(cursor.getFirst(), results.size(), cursor.getMax(), total, limited);
        }

        InstancesResponse response = new InstancesResponse();
        response.list = results;
        response.cursor = newCursor;

        return response;
    }

}
