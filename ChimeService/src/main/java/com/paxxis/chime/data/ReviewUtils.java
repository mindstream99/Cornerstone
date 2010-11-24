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

import com.paxxis.chime.client.common.Community;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceRequest.Operator;
import com.paxxis.chime.client.common.DataInstanceRequest.SortOrder;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.FieldData;
import com.paxxis.chime.client.common.ReviewsBundle;
import com.paxxis.chime.client.common.Review;
import com.paxxis.chime.client.common.Scope;
import com.paxxis.chime.client.common.SearchFilter;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.service.Tools;
import com.paxxis.cornerstone.base.Cursor;
import com.paxxis.cornerstone.base.InstanceId;
import com.paxxis.cornerstone.database.DataSet;
import com.paxxis.cornerstone.database.DatabaseConnection;
import com.paxxis.cornerstone.database.StringData;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Robert Englander
 */
public class ReviewUtils
{
    private ReviewUtils()
    {}
    
    public static DataInstance applyReview(DataInstance instance, Review review, User user, DatabaseConnection database) throws Exception
    {
        database.startTransaction();
        InstanceId id = instance.getId();
        String tableSet = Tools.getTableSet();
        
        try
        {
            String comment = review.getDescription();

            // is this a new review?  or is this an adjusted review?
            String sql = "select A.id, A.intVal2 rating from " + tableSet +
                            " A, " + Tools.getTableSet() + "_Type B where A.backRef = '" + instance.getId() +
                            "' and A.updatedBy = '" + user.getId() + "' and A.id = B.instance_id and B.datatype_id = '500'";
            
            String ratingId = "0";
            
            DataSet dataSet = database.getDataSet(sql, true);
            boolean adjusted = dataSet.next();
            int oldRating = 0;
            Shape reviewType = ShapeUtils.getInstance("Review", database, true);
            List<Shape> types = new ArrayList<Shape>();
            types.add(reviewType);

            if (adjusted)
            {
                ratingId = dataSet.getFieldValue("id").asString();
                oldRating = dataSet.getFieldValue("rating").asInteger();

                String sqlInserts = "name = " + new StringData(review.getName()).asSQLValue() + ", description = " + new StringData(review.getDescription()).asSQLValue() + ", intVal2 = " + review.getRating()
                        + ", backRef = '" + instance.getId() + "', charVal = '" + instance.getName() + "'";
                DataInstanceUtils.modifyInstance(InstanceId.create(ratingId), sqlInserts, user, true, database);
            }
            else
            {
                // the new review provides for 'read' scope that is consistent with the scopes of the
                // instance being reviewed.  In addition, the new review includes 'update' scope for
                // the reviewer.  ok, so let's build up the appropriate scopes...
                List<Scope> reviewScopes = new ArrayList<Scope>();
                List<Scope> scopes = instance.getSocialContext().getScopes();

                // include R scopes that are not specific to this user
                for (Scope scope : scopes) {
                    if (!scope.getCommunity().getId().equals(user.getId())) {
                        reviewScopes.add(new Scope(scope.getCommunity(), Scope.Permission.R));
                    }
                }

                // include RU scope for this user
                reviewScopes.add(new Scope(new Community(user.getId()), Scope.Permission.RU));

                String sqlInserts[] = {"intVal2,backRef,charVal", "" + review.getRating() + ", '" + instance.getId() + "', '" + instance.getName() + "'"};
                DataInstanceUtils.createInstance(types, review.getName(),
                        comment, null, sqlInserts,
                        new ArrayList<FieldData>(), reviewScopes, user, database);
            }
            
            dataSet.close();
            
            // if the review value hasn't changed and the user hasn't provided an updated review, don't do anything
            if ((oldRating != review.getRating()) || comment != null)
            {
                // compute the new average review and review count and update the instance record
                sql = "select count(A.intVal2) theCount, sum(A.intVal2) theSum from " + tableSet +
                        " A, " + tableSet + "_Type B where A.backRef = '" + instance.getId() +
                        "' and A.id = B.instance_id and B.datatype_id = '500'";

                dataSet = database.getDataSet(sql, true);
                dataSet.next();
                float sum = dataSet.getFieldValue("theSum").asInteger();
                int newCount = dataSet.getFieldValue("theCount").asInteger();
                float newAverage = sum / newCount; 

                String reviewedAction = DataInstance.ReviewAction.C.toString();
                if (adjusted)
                {
                    reviewedAction = DataInstance.ReviewAction.U.toString();
                }

                // update the instance
                String sqlInserts = "expiration = null, averageRating = " + newAverage + ", ratingCount = " + newCount +
                        ", reviewed = CURRENT_TIMESTAMP, reviewedBy = '" + user.getId() +
                        "', reviewedByName = '" + user.getName() + "'" +
                        ", reviewedByAction = '" + reviewedAction + "'";

                DataInstanceUtils.modifyInstance(instance, sqlInserts, false, user, database);
                HistoryUtils.writeEvent(HistoryUtils.HistoryEventType.Review, "", instance, user, database);
                dataSet.close();
            }
            
            database.commitTransaction();
        }
        catch (Exception e)
        {
            database.rollbackTransaction();
            throw new Exception(e.getMessage());
        }
        
        DataInstance data = DataInstanceUtils.getInstance(id, user, database, true, false);
        return data;
    }
    
    public static Review getReview(InstanceId instanceId, User user, DatabaseConnection database) throws Exception {
        String sql = "SELECT A.* FROM "
                + Tools.getTableSet() + " A, " + Tools.getTableSet() + "_Type B where " +
                " A.id = B.instance_id and B.datatype_id = '500' and A.backRef = '" + instanceId + 
                "' and A.updatedBy = '" + user.getId() + "'";

        InstancesBundle bundle = DataInstanceUtils.getInstances(sql, null, user, database, true);
        Review review = null;
        if (bundle.getInstances().size() == 1) {
            review = (Review)bundle.getInstances().get(0);
        }
        
        return review;
    }
    
    public static ReviewsBundle getReviews(InstanceId instanceId, SearchFilter filter, Cursor cursor, SortOrder sortOrder, DatabaseConnection database) throws Exception
    {
        String filterSql = "";
        if (filter != null && filter.isEnabled()) {

            /*
            InstanceId refId = InstanceId.create(filter.getValue().toString());
            Shape shape = filter.getDataField().getShape();
            List<InstanceId> ids = new ArrayList<InstanceId>();
            if (shape.getId().equals(Shape.USER_ID)) {
                ids.add(refId);
            } else if (shape.getId().equals(Shape.USERCOLLECTION_ID)) {
                DataInstance collection = DataInstanceUtils.getInstance(refId, null, database, true, true);
                DataField userField = shape.getField("Users");
                List<DataFieldValue> fieldValues = collection.getFieldValues(shape, userField);
                for (DataFieldValue fieldValue : fieldValues) {
                    ids.add(fieldValue.getReferenceId());
                }
            }

            if (ids.size() > 0) {
                if (filter.getOperator() == Operator.NotReference || filter.getOperator() == Operator.NotContainedIn) {
                    filterSql = "updatedBy not in (";
                } else {
                    filterSql = "updatedBy in (";
                }

                String comma = "";
                for (InstanceId id : ids) {
                    filterSql += (comma + "'" + id + "'");
                    comma = ",";
                }

                filterSql += ") and ";
            }
            */
        }

        String sql = "SELECT A.* FROM "
                + Tools.getTableSet() + " A, " + Tools.getTableSet() + "_Type B where " +
                " A.id = B.instance_id and B.datatype_id = '500' and " + filterSql + " A.backRef = '" + instanceId + "'";

        switch (sortOrder) {
            case ByMostUseful:
                sql += " order by A.ranking desc";
                break;
            case ByRating:
                sql += " order by A.intVal2 desc";
                break;
            default:
                sql += " order by A.updated desc";
                break;
        }


        InstancesBundle bundle = DataInstanceUtils.getInstances(sql, cursor, null, database, false);

        List<Review> reviews = (List<Review>)bundle.getInstances();
        return new ReviewsBundle(reviews, bundle.getCursor());
    }
}
