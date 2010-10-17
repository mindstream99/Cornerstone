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

import com.paxxis.chime.client.common.Comment;
import com.paxxis.chime.client.common.CommentsBundle;
import com.paxxis.chime.client.common.Community;
import com.paxxis.chime.client.common.Cursor;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceRequest.Operator;
import com.paxxis.chime.client.common.DataInstanceRequest.SortOrder;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.FieldData;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.Scope;
import com.paxxis.chime.client.common.SearchFilter;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.database.DatabaseConnection;
import com.paxxis.chime.service.Tools;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class CommentUtils 
{
    private CommentUtils()
    {}
    
    public static void addComment(DataInstance instance, User user, String comment,
                                    DatabaseConnection database) throws Exception
    {
        try {
            database.startTransaction();
            InstanceId instanceId = instance.getId();

            Shape commentShape = ShapeUtils.getInstance("Comment", database, true);
            List<Shape> shapes = new ArrayList<Shape>();
            shapes.add(commentShape);

            // create a new comment instance
            String sqlSuffix[] = {"backRef,charVal","'" + instanceId + "', '" + instance.getName() + "'"};

            // the new comment provides for 'read' scope that is consistent with the scopes of the
            // instance being commented on.
            List<Scope> commentScopes = new ArrayList<Scope>();
            List<Scope> scopes = instance.getSocialContext().getScopes();

            // include R scopes that are not specific to this user
            for (Scope scope : scopes) {
                if (!scope.getCommunity().getId().equals(user.getId())) {
                    commentScopes.add(new Scope(scope.getCommunity(), Scope.Permission.R));
                }
            }

            // include RU scope for this user
            commentScopes.add(new Scope(new Community(user.getId()), Scope.Permission.RU));

            DataInstanceUtils.createInstance(shapes, instanceId.getValue(),
                    comment, null, sqlSuffix,
                    new ArrayList<FieldData>(), commentScopes, user, database);

            // update the instance
            String sqlInserts = "expiration = null, commented = CURRENT_TIMESTAMP, commentedBy = '" + user.getId() + "', commentedByName = '" +
                    user.getName() + "', commentCount = (commentCount + 1)";

            DataInstanceUtils.modifyInstance(instance, sqlInserts, user, false, database);

            HistoryUtils.writeEvent(HistoryUtils.HistoryEventType.Comment, "", instance, user, database);

            // the user is automatically subscribed when a comment is added
            //DataInstanceUtils.registerInterest(instance, user, true, database);
            database.commitTransaction();
        } catch (Exception e) {
            database.rollbackTransaction();
            throw e;
        }
    }

    public static CommentsBundle getComments(InstanceId instanceId, SearchFilter filter, Cursor cursor, SortOrder sortOrder, DatabaseConnection database) throws Exception
    {
        Shape commentType = ShapeUtils.getInstance("Comment", database, true);
        String filterSql = "";
        if (filter != null && filter.isEnabled()) {
            InstanceId refId = InstanceId.create(filter.getValue().toString());
            Shape shape = filter.getDataField().getShape();
            List<InstanceId> ids = new ArrayList<InstanceId>();
            if (shape.getName().equals("User")) {
                ids.add(refId);
            } else if (shape.getName().equals("User Collection")) {
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
        }

        String sql = "SELECT A.* FROM "
                + Tools.getTableSet() + " A, " + Tools.getTableSet() + "_Type B where " +
                " A.id = B.instance_id and B.datatype_id = '600' and " + filterSql + " A.backRef = '" + instanceId + "'";

        switch (sortOrder) {
            case ByMostUseful:
                sql += " order by A.ranking desc";
                break;
            default:
                sql += " order by A.updated desc";
                break;
        }

        InstancesBundle bundle = DataInstanceUtils.getInstances(sql, cursor, null, database, false);
        List<Comment> comments = (List<Comment>)bundle.getInstances();
        return new CommentsBundle(comments, bundle.getCursor());
    }
}
