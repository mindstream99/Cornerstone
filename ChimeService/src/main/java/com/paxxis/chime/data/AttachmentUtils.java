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
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.database.DataSet;
import com.paxxis.chime.database.DatabaseConnection;
import com.paxxis.chime.service.Tools;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class AttachmentUtils {
    private enum Type {
        I,
        F
    }

    private AttachmentUtils() {}

    public static List<DataInstance> getFiles(InstanceId instanceId, DatabaseConnection database) throws Exception {
        return getInstances(Type.F, instanceId, database);
    }

    public static List<DataInstance> getImages(InstanceId instanceId, DatabaseConnection database) throws Exception {
        return getInstances(Type.I, instanceId, database);
    }

    private static List<DataInstance> getInstances(Type type, InstanceId instanceId, DatabaseConnection database) throws Exception {
        List<DataInstance> results = new ArrayList<DataInstance>();

        String sql = "select foreign_id from " + Tools.getTableSet() + "_Attachment where instance_id = '" + instanceId +
                "' and filetype = '" + type.toString() + "' order by filenum";
        DataSet dataSet = database.getDataSet(sql, true);
        while (dataSet.next()) {
            DataInstance image = DataInstanceUtils.getInstance(InstanceId.create(dataSet.getFieldValue("foreign_id").asString()), null, database, true, true);
            results.add(image);
        }

        dataSet.close();

        return results;
    }

}
