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

import com.paxxis.chime.client.common.DataInstanceRequest.Operator;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.service.InstancesResponse;
import com.paxxis.chime.service.Tools;
import com.paxxis.cornerstone.database.DatabaseConnection;

/**
 *
 * @author Robert Englander
 */
public class SocialActivityUtils {
    private SocialActivityUtils() {
    }

    public static InstancesResponse getMostActivelyTagged(Shape shape, Operator operator, int limit, DatabaseConnection database) throws Exception
    {
        int offset = DataInstanceUtils.operatorToOffset(operator);

        String sql = "select sum(A.id)/A.id as counter, A.* from " + Tools.getTableSet() + " A, " +
                "" + Tools.getTableSet() + "_tag B, " + Tools.getTableSet() + "_Type C where C.datatype_id = '" + shape.getId() +
                "' and A.id = B.instance_id and A.id = C.instance_id and B.datatype_id = '" + shape.getId() + "' and B.timestamp >= (DATE_ADD(CURRENT_TIMESTAMP, interval -" +
                offset + " DAY)) group by A.id order by counter desc limit " + limit;

        return SearchUtils.findInstances(sql, null, database);

    }

    public static InstancesResponse getMostActivelyReviewed(Shape shape, Operator operator, int limit, DatabaseConnection database) throws Exception
    {
        int offset = DataInstanceUtils.operatorToOffset(operator);

        // TODO there is no _rating table anymore.  this needs to be reworked
        String sql = "select sum(A.id)/A.id as counter, A.* from " + Tools.getTableSet() + " A, " +
                "" + Tools.getTableSet() + "_rating B, " + Tools.getTableSet() + "_Type C where C.datatype_id = '" + shape.getId() +
                "' and A.id = B.instance_id and A.id = C.instance_id and B.datatype_id = '" + shape.getId() + "' and B.timestamp >= (DATE_ADD(CURRENT_TIMESTAMP, interval -" +
                offset + " DAY)) group by A.id order by counter desc limit " + limit;

        return SearchUtils.findInstances(sql, null, database);

    }

    public static InstancesResponse getMostActivelyCommented(Shape shape, Operator operator, int limit, DatabaseConnection database) throws Exception
    {
        int offset = DataInstanceUtils.operatorToOffset(operator);
        String name = shape.getName();

        // TODO this should not use the name anymore.  there's only 1 kind of comment now
        String sql = "select sum(A.id)/A.id as counter, A.* from " + Tools.getTableSet() + " A, " +
                "" + Tools.getTableSet() + "_comment B, " + Tools.getTableSet() + "_Type C where A.datatype_id = '" + shape.getId() +
                "' and A.id = B.instance_id and A.id = C.instance_id and B.datatype_id = '" + shape.getId() + "' and B.instanceType != '" + name + "_review' " +
                "and B.timestamp >= (DATE_ADD(CURRENT_TIMESTAMP, interval -" +
                offset + " DAY)) group by A.id order by counter desc limit " + limit;

        return SearchUtils.findInstances(sql, null, database);

    }

}
