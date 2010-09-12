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
import com.paxxis.chime.client.common.Cursor;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.Discussion;
import com.paxxis.chime.client.common.DiscussionsBundle;
import com.paxxis.chime.client.common.FieldData;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.Scope;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.database.DatabaseConnection;
import com.paxxis.chime.service.Tools;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class DiscussionUtils
{
    private DiscussionUtils()
    {}

    public static DataInstance addDiscussion(DataInstance instance, User user, String name, String initialComment,
                                    DatabaseConnection database) throws Exception
    {
        try {
            database.startTransaction();
            InstanceId instanceId = instance.getId();

            Shape discussionType = ShapeUtils.getInstance("Discussion", database, true);
            List<Shape> types = new ArrayList<Shape>();
            types.add(discussionType);

            // create a new discussion instance
            String sqlSuffix[] = {"backRef,charVal","'" + instanceId + "', '" + instance.getName() + "'"};

            // the new discussion provides for 'read' scope that is consistent with the scopes of the
            // instance being discussed.
            List<Scope> discussionScopes = new ArrayList<Scope>();
            List<Scope> scopes = instance.getSocialContext().getScopes();

            // include R scopes that are not specific to this user
            for (Scope scope : scopes) {
                if (!scope.getCommunity().getId().equals(user.getId())) {
                    discussionScopes.add(new Scope(scope.getCommunity(), Scope.Permission.R));
                }
            }

            // include RU scope for this user
            discussionScopes.add(new Scope(new Community(user.getId()), Scope.Permission.RU));

            DataInstance discussion = DataInstanceUtils.createInstance(types, name, null, null, sqlSuffix,
                    new ArrayList<FieldData>(), discussionScopes, user, database);
            CommentUtils.addComment(discussion, user, initialComment, database);
            discussion = DataInstanceUtils.getInstance(discussion.getId(), user, database, true, false);

            // update the instance
            String sqlInserts = "expiration = null";
            DataInstanceUtils.modifyInstance(instance, sqlInserts, user, false, database);

            DataInstance data = DataInstanceUtils.getInstance(instance.getId(), user, database, true, false);
            database.commitTransaction();
            return data;
        } catch (Exception e) {
            database.rollbackTransaction();
            throw e;
        }
    }

    public static DiscussionsBundle getDiscussions(InstanceId instanceId, Cursor cursor, DatabaseConnection database) throws Exception
    {
        String sql = "SELECT A.* FROM "
                + Tools.getTableSet() + " A, " + Tools.getTableSet() + "_Type B where " +
                " A.id = B.instance_id and B.datatype_id = '1000' and  A.backRef = '" + instanceId + "' order by A.updated desc";

        InstancesBundle bundle = DataInstanceUtils.getInstances(sql, cursor, null, database, false);
        List<Discussion> discussions = (List<Discussion>)bundle.getInstances();
        return new DiscussionsBundle(discussions, bundle.getCursor());
    }
}
