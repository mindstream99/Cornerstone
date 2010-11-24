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

/**
 *
 * @author Robert Englander
 */
public class PrimaryDataUtils {
    private PrimaryDataUtils() {
    }

    public static DataInstance updatePrimaryData(DataInstance instance, User user, DatabaseConnection database) throws Exception {
        database.startTransaction();

        DataInstance result = null;

        try
        {
            boolean changedFiles = false;
            boolean changedImages = false;
            DataInstance old = DataInstanceUtils.getInstance(instance.getId(), user, database, true, true);

            if (old.getImages().size() > 0) {
                String sql = "delete from DataInstance_Attachment where instance_id = '" + instance.getId() +
                        "' and filetype = 'I'"; 
                database.executeStatement(sql);
                changedImages = true;
            }

            int fileNum = 1;
            if (instance.getImages().size() > 0) {
                for (DataInstance image : instance.getImages()) {
                    InstanceId id = Tools.getNewId(Tools.DEFAULT_EXTID);
                    String sql = "insert into DataInstance_Attachment (id,instance_id, filetype, filenum, foreign_id, timestamp) values ('" +
                            id + "', '" + instance.getId() + "', 'I', " + fileNum + ", '" + image.getId() +
                            "', CURRENT_TIMESTAMP)";
                    database.executeStatement(sql);
                    fileNum++;
                }
                changedImages = true;
            }

            if (old.getFiles().size() > 0) {
                String sql = "delete from DataInstance_Attachment where instance_id = '" + instance.getId() +
                        "' and filetype = 'F'"; 
                database.executeStatement(sql);
                changedFiles = true;
            }

            if (instance.getFiles().size() > 0) {
                for (DataInstance file : instance.getFiles()) {
                    InstanceId id = Tools.getNewId(Tools.DEFAULT_EXTID);
                    String sql = "insert into DataInstance_Attachment (id,instance_id, filetype, filenum, foreign_id, timestamp) values ('" +
                            id + "', '" + instance.getId() + "', 'F', 1, '" + file.getId() +
                            "', CURRENT_TIMESTAMP)";
                    database.executeStatement(sql);
                }
                changedFiles = true;
            }

            // update the name in the primary record
            String oldName = old.getName();
            String newName = instance.getName();

            // update the description in the primary record
            String oldDesc = old.getDescription();
            if (oldDesc == null) {
                oldDesc = "";
            }

            String newDesc = instance.getDescription();
            if (newDesc == null) {
                newDesc = "";
            }

            if (!oldName.equals(newName) || !oldDesc.equals(newDesc)) {
                String sql = "update DataInstance set name = " + new StringData(newName).asSQLValue() + ", description = " + new StringData(newDesc).asSQLValue() +
                        ", expiration = null, updated = CURRENT_TIMESTAMP, updatedBy = '" + user.getId() + "', updatedByName = '" + user.getName() +
                        "' where id = '" + instance.getId() + "'";
                database.executeStatement(sql);
            } else if (changedImages || changedFiles) {
                String sql = "update DataInstance set expiration = null, updated = CURRENT_TIMESTAMP, updatedBy = '" +
                        user.getId() + "', updatedByName = '" + user.getName() +
                        "' where id = '" + instance.getId() + "'";
                database.executeStatement(sql);
            }

            result = DataInstanceUtils.getInstance(instance.getId(), user, database, true, false);

            if (result.getShapes().get(0).getId().equals(Shape.SHAPE_ID)) {
                CacheManager.instance().putShape(result);
            }

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
