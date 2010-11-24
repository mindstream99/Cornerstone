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

import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.service.Tools;
import com.paxxis.cornerstone.base.InstanceId;
import com.paxxis.cornerstone.database.DatabaseConnection;
import com.paxxis.cornerstone.database.StringData;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class InstanceShapeUtils {
    private InstanceShapeUtils() {
    }

    public static DataInstance updateShapes(DataInstance instance, User user, DatabaseConnection database) throws Exception {
        database.startTransaction();

        // remove the existing shapes and apply the new ones.  we need to know which ones are actually being removed
        // so that the data associated with the shape can be removed as well

        DataInstance result = null;

        try
        {
            boolean changed = false;
            DataInstance old = DataInstanceUtils.getInstance(instance.getId(), user, database, true, true);

            // find the existing types that should have data removed
            List<Shape> remove = new ArrayList<Shape>();
            for (Shape shape : old.getShapes()) {
                boolean found = false;
                for (DataInstance t : instance.getShapes()) {
                    if (shape.getId().equals(t.getId())) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    remove.add(shape);
                }
            }

            // remove all types from the instance
            String sql = "delete from DataInstance_Type where instance_id = '" + instance.getId() + "'";
            database.executeStatement(sql);

            // add the new types
            int position = 1;
            for (Shape type : instance.getShapes()) {
                InstanceId id = Tools.getNewId(Tools.DEFAULT_EXTID);
                sql = "insert into DataInstance_Type (id,instance_id, name, datatype_id, timestamp,position) values ('" +
                        id + "', '" + instance.getId() + "', " + new StringData(instance.getName()).asSQLValue() +
                        ", '" + type.getId() +
                        "', CURRENT_TIMESTAMP," + position++ + ")";
                database.executeStatement(sql);
            }

            changed = true;

            if (changed) {
                for (Shape type : remove) {
                    FieldDataUtils.deleteAllFieldData(old, type, database);
                }

                sql = "update DataInstance set expiration = null, updated = CURRENT_TIMESTAMP, updatedBy = '" + user.getId() +
                        "', updatedByName = '" + user.getName() +
                        "' where id = '" + instance.getId() + "'";
                database.executeStatement(sql);
            }

            result = DataInstanceUtils.getInstance(instance.getId(), user, database, true, false);
            database.commitTransaction();
        }
        catch (Exception e)
        {
            database.rollbackTransaction();
            throw new Exception(e.getMessage());
        }

        return result;
    }

}
