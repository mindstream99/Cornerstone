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

package com.paxxis.chime.notification;

import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.database.DataSet;
import com.paxxis.chime.database.DatabaseConnection;
import com.paxxis.chime.database.DatabaseConnectionPool;
import com.paxxis.chime.data.UserUtils;
import com.paxxis.chime.service.ChimeConfiguration;
import java.util.HashMap;
import org.apache.log4j.Logger;

class WatchNotificationProcessor extends EmailNotifier {
    private static final Logger _logger = Logger.getLogger(WatchNotificationProcessor.class);

    private DatabaseConnectionPool dbPool;
    private ChimeConfiguration config;

    WatchNotificationProcessor(DatabaseConnectionPool pool, ChimeConfiguration cfg) {
        super(cfg);
        dbPool = pool;
        config = cfg;
    }

    public void run() {
        DatabaseConnection dbconn = dbPool.borrowInstance(this);
        try {
            try {
                dbconn.startTransaction();

                String sql = "select user_id, instance_id, instance_name from Chime.RegisteredInterest where last_update > last_notification order by user_id";
                DataSet dataSet = dbconn.getDataSet(sql, true);
                String currentUserId = null;
                HashMap<String, StringBuilder> msgMap = new HashMap<String, StringBuilder>();
                StringBuilder msg = new StringBuilder();

                User admin = new User();
                admin.setId(User.SYSTEM);

                while (dataSet.next()) {
                    String userId = dataSet.getFieldValue("user_id").asString();
                    String instanceId = dataSet.getFieldValue("instance_id").asString();
                    String instanceName = dataSet.getFieldValue("instance_name").asString();
                    if (!userId.equals(currentUserId)) {
                        if (currentUserId != null) {
                            User user = UserUtils.getUserById(InstanceId.create(userId), admin, dbconn);
                            String email = user.getEmailAddress();
                            if (email != null && !email.isEmpty()) {
                                // send what we have
                                Pair p = new Pair();
                                p.email = email;
                                p.id = userId;
                                String body = "The following data instances have had recent activity:\n\n" + msg.toString();
                                send(p, dbconn, "Chime Watch Notification", body);
                            }
                        }

                        currentUserId = userId;
                        msg = new StringBuilder();
                    }

                    String link = "";
                    String urlHost = config.getStringValue("chime.notification.urlHost", "");
                    if (!urlHost.isEmpty()) {
                        link = " (" + urlHost + "/#detail:" + instanceId;
                    }

                    msg.append(instanceName).append(link).append("\n");
                }
                dataSet.close();
                sql = "update Chime.RegisteredInterest set last_notification = CURRENT_TIMESTAMP where last_update > last_notification";
                dbconn.executeStatement(sql);

                if (msg.length() > 0) {
                    User user = UserUtils.getUserById(InstanceId.create(currentUserId), admin, dbconn);
                    String email = user.getEmailAddress();
                    if (email != null && !email.isEmpty()) {
                        // send what we have
                        Pair p = new Pair();
                        p.email = email;
                        p.id = currentUserId;
                        String body = "The following data instances have had recent activity:\n\n" + msg.toString();
                        send(p, dbconn, "Chime Watch Notification", body);
                    }
                }

                dbconn.commitTransaction();

            } catch (Exception ee) {
                _logger.error(ee);
                dbconn.rollbackTransaction();
            }

        } catch (Exception e) {
            _logger.error(e);
        }

        dbPool.returnInstance(dbconn, this);
        Notifier.instance().initScheduledWatchNotification();
    }
}
