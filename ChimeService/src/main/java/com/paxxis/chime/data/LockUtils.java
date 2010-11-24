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
import com.paxxis.cornerstone.database.DatabaseConnection;

/**
 *
 * @author Robert Englander
 */
public class LockUtils {
    private LockUtils() {
    }

    public static DataInstance applyLock(DataInstance instance, DataInstance.LockType lockType, User user, DatabaseConnection database) throws Exception
    {
        database.startTransaction();
        InstanceId id = instance.getId();
        String tableSet = Tools.getTableSet();

        try
        {
            String sql = "update " + tableSet + " set " +
                    "lockType = '" + lockType.toString() + "', lockedBy = '" + user.getId() +
                    "', lockedByName = '" + user.getName() +
                    "', locked = CURRENT_TIMESTAMP where id = '" + instance.getId() + "'";

            database.executeStatement(sql);

            database.commitTransaction();
        }
        catch (Exception e)
        {
            database.rollbackTransaction();
            throw new Exception(e.getMessage());
        }

        DataInstance data = DataInstanceUtils.getInstance(id, user, database, true, false);
        return data;
    }

}
