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
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.service.Tools;
import com.paxxis.cornerstone.base.InstanceId;
import com.paxxis.cornerstone.database.DataSet;
import com.paxxis.cornerstone.database.DatabaseConnection;

/**
 *
 * @author Robert Englander
 */
public class RegisteredInterestUtils {
    private RegisteredInterestUtils() {
    }

    public static void registerInterest(DataInstance inst, User user, boolean subscribe, DatabaseConnection database) throws Exception {

        boolean isSubscribed = isRegisteredInterest(inst, user, database);

        database.startTransaction();
        try {
            if (!isSubscribed && subscribe) {
                InstanceId id = Tools.getNewId(Tools.DEFAULT_EXTID);
                String sql = "insert into Chime.RegisteredInterest (id,instance_id, instance_name,user_id, last_update, last_notification) values ('" + id + "', '" + inst.getId() +
                        "', '" + inst.getName() + "', '" + user.getId() + "', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

                database.executeStatement(sql);
            } else if (isSubscribed && !subscribe) {
                String sql = "delete from RegisteredInterest where instance_id = '" + inst.getId() +
                        "' AND user_id = '" + user.getId() + "'";

                database.executeStatement(sql);
            }

            database.commitTransaction();
        } catch (Exception e) {
            database.rollbackTransaction();
        }
    }

    static boolean isRegisteredInterest(DataInstance inst, User user, DatabaseConnection database) throws Exception {
        boolean reg = false;

        if (user != null) {
            String sql = "select 1 from Chime.RegisteredInterest where instance_id = '" + inst.getId() +
                    "' and user_id = '" + user.getId() + "'";
            DataSet dataSet = database.getDataSet(sql, true);
            reg = dataSet.next();
            dataSet.close();
        }

        return reg;
    }

}
