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
import java.util.List;

import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.database.DatabaseConnection;
import com.paxxis.chime.database.StringData;
import com.paxxis.chime.service.Tools;

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

            // IMAGES -- remove the existing images and then just add the new ones.

            // find the existing images that should be removed
            List<DataInstance> removeImages = new ArrayList<DataInstance>(old.getImages());

            /*
            for (DataInstance img : old.getImages()) {
                boolean found = false;
                for (DataInstance image : instance.getImages()) {
                    if (image.getId().equals(img.getId())) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    removeImages.add(img);
                }
            }
            */

            // removeImages images
            if (removeImages.size() > 0) {
                String sql = "delete from DataInstance_Attachment where instance_id = '" + instance.getId() +
                        "' and foreign_id in (";
                String op = "";
                for (DataInstance img : removeImages) {
                    sql += op + "'" + img.getId() + "'";
                    op = ",";
                }
                sql += ")";
                database.executeStatement(sql);
                changedImages = true;
            }

            // find the new images to add
            List<DataInstance> addImages = new ArrayList<DataInstance>(instance.getImages());
            /*
            for (DataInstance img : instance.getImages()) {
                boolean found = false;
                for (DataInstance image : old.getImages()) {
                    if (image.getId().equals(img.getId())) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    addImages.add(img);
                }
            }
            */

            // add the new images
            int fileNum = 1;
            if (addImages.size() > 0) {
                for (DataInstance image : addImages) {
                    InstanceId id = Tools.getNewId(Tools.DEFAULT_EXTID);
                    String sql = "insert into DataInstance_Attachment (id,instance_id, filetype, filenum, foreign_id, timestamp) values ('" +
                            id + "', '" + instance.getId() + "', 'I', " + fileNum + ", '" + image.getId() +
                            "', CURRENT_TIMESTAMP)";
                    database.executeStatement(sql);
                    fileNum++;
                }
                changedImages = true;
            }

            // FILES

            // find the existing files that should be removed
            List<DataInstance> removeFiles = new ArrayList<DataInstance>(old.getFiles());
            /*
            for (DataInstance file : old.getFiles()) {
                boolean found = false;
                for (DataInstance f : instance.getFiles()) {
                    if (f.getId().equals(file.getId())) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    removeFiles.add(file);
                }
            }
            */

            // remove files
            if (removeFiles.size() > 0) {
                String sql = "delete from DataInstance_Attachment where instance_id = '" + instance.getId() +
                        "' and foreign_id in (";
                String op = "";
                for (DataInstance file : removeFiles) {
                    sql += op + "'" + file.getId() + "'";
                    op = ",";
                }
                sql += ")";
                database.executeStatement(sql);
                changedFiles = true;
            }

            // find the new files to add
            List<DataInstance> addFiles = new ArrayList<DataInstance>(instance.getFiles());
            /*
            for (DataInstance file : instance.getFiles()) {
                boolean found = false;
                for (DataInstance f : old.getFiles()) {
                    if (file.getId().equals(f.getId())) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    addFiles.add(file);
                }
            }
            */

            // add the new files
            if (addFiles.size() > 0) {
                for (DataInstance file : addFiles) {
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
