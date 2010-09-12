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
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.database.DatabaseConnection;
import com.paxxis.chime.service.Tools;

/**
 *
 * @author Robert Englander
 */
public class HistoryUtils {

    public enum HistoryEventType {
        Create,
        Modify,
        Review,
        Comment,
        Tag
    }

    private HistoryUtils() {}

    public static void writeEvent(HistoryEventType type, DataInstance instance, User user, DatabaseConnection database) throws Exception {

        InstanceId id = Tools.getNewId(Tools.DEFAULT_EXTID);
        String sql = "insert into " + Tools.getTableSet() + "_History " +
                "(id, instance_id, user_id, user_name, timestamp, eventType) values ('" +
                id + "', '" + instance.getId() +
                "', '" + user.getId() + "', '" + user.getName() + "', CURRENT_TIMESTAMP" +
                ", '" + type.toString() + "')";
        database.executeStatement(sql);
    }
}
