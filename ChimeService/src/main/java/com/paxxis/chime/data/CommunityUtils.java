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
import com.paxxis.chime.client.common.DataInstanceRequest;
import com.paxxis.chime.client.common.DataInstanceRequest.ClauseOperator;
import com.paxxis.chime.client.common.DataInstanceRequest.Operator;
import com.paxxis.chime.client.common.DataInstanceRequest.SortOrder;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.FieldData;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.Parameter;
import com.paxxis.chime.client.common.Scope;
import com.paxxis.chime.client.common.SearchCriteria;
import com.paxxis.chime.client.common.SearchFilter;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.database.DatabaseConnection;
import com.paxxis.chime.service.InstancesResponse;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class CommunityUtils {

    private CommunityUtils() {
    }

    public static List<User> getCommunityMembers(List<InstanceId> communityIds, User user, DatabaseConnection database) throws Exception {

        if (communityIds.isEmpty()) {
            return new ArrayList<User>();
        }

        SearchCriteria criteria = new SearchCriteria();

        Shape userShape = ShapeUtils.getInstance("User", database, true);
        DataField communityField = userShape.getField("Community");
        DataField moderatorField = userShape.getField("Community Moderator");

        for (InstanceId id : communityIds) {
            SearchFilter filter = new SearchFilter();
            filter.setDataShape(userShape);
            filter.setDataField(communityField);
            filter.setOperator(Operator.Reference);
            filter.setValue(id);
            criteria.addFilter(filter);

            filter = new SearchFilter();
            filter.setDataShape(userShape);
            filter.setDataField(moderatorField);
            filter.setOperator(Operator.Reference);
            filter.setValue(id);
            criteria.addFilter(filter);
        }

        DataInstanceRequest req = criteria.buildRequest(user, 1000);
        List<Parameter> params = req.getQueryParameters();

        InstancesResponse resp = SearchUtils.getInstancesByIndex(User.class, params, ClauseOperator.MatchAny, user, false, false, null, SortOrder.ByName, database);
        return new ArrayList<User>(resp.list);
    }

    public static Community createCommunity(String name, String description, User user, DatabaseConnection database) throws Exception {

        database.startTransaction();
        Community newCommunity = null;

        try {
            Shape communityShape = ShapeUtils.getInstance("Community", database, true);
            List<Shape> shapes = new ArrayList<Shape>();
            shapes.add(communityShape);

            // everyone can see the community
            List<Scope> scopes = new ArrayList<Scope>();
            scopes.add(new Scope(Community.Global, Scope.Permission.R));

            String sqlInserts[] = {null, null};

            newCommunity = (Community)DataInstanceUtils.createInstance(shapes, name,
                    description, null, sqlInserts,
                    new ArrayList<FieldData>(), scopes, user, database);

            // ok, now modify the scopes
            ScopeUtils.applyScope(newCommunity.getId(), new Scope(new Community(newCommunity.getId()), Scope.Permission.RM), database);

            database.commitTransaction();
        } catch (Exception e) {
            database.rollbackTransaction();
            throw new Exception(e);
        }

        return newCommunity;
    }
}
