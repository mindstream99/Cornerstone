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

import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.data.CommunityUtils;
import com.paxxis.chime.data.DataInstanceUtils;
import com.paxxis.chime.data.UserUtils;
import com.paxxis.cornerstone.base.InstanceId;
import com.paxxis.cornerstone.database.DataSet;
import com.paxxis.cornerstone.database.DatabaseConnection;
import com.paxxis.cornerstone.database.DatabaseConnectionPool;
import com.paxxis.cornerstone.service.CornerstoneConfiguration;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * Sends notifications based on event journal entries.
 *
 * @author Robert Englander
 */
class JournalNotificationProcessor extends MessageNotifier {
    private static final Logger _logger = Logger.getLogger(JournalNotificationProcessor.class);

    private DatabaseConnectionPool dbPool;
    private CornerstoneConfiguration config;

    JournalNotificationProcessor(DatabaseConnectionPool pool, CornerstoneConfiguration cfg) {
        super(cfg);
        dbPool = pool;
        config = cfg; 
    }

    public void run() {
        DatabaseConnection dbconn = dbPool.borrowInstance(this);
        try {
            // a separate email is generated for each event type in the journal, so the 1st step is
            // to find all of the unique type_id's
            User admin = new User();
            admin.setId(User.SYSTEM);

            try {
                dbconn.startTransaction();

                String sql = "select distinct type_id from Chime.EventJournal";
                
                // important to set readOnly parameter on the dataset to false so that another instance of
                // ChimeService doesn't attempt to accomplish the same thing
                DataSet dataSet = dbconn.getDataSet(sql, false);
                List<String> typeIds = new ArrayList<String>();
                while (dataSet.next()) {
                    String id = dataSet.getFieldValue("type_id").asString();
                    typeIds.add(id);
                }
                dataSet.close();

                for (String tId : typeIds) {
                    InstanceId typeId = InstanceId.create(tId);
                    StringBuilder body = new StringBuilder();
                    DataInstance eventType = DataInstanceUtils.getInstance(typeId, admin, dbconn, true, true);
                    if (Notifier.instance().isPeriodicSummarize()) {
                        sql = "select count(*) from Chime.EventJournal where type_id = '" + typeId + "'";
                        dataSet = dbconn.getDataSet(sql, true);
                        dataSet.next();
                        int count = dataSet.getFieldValue("count(*)").asInteger();
                        body.append("\nThere are ").append(count).append(" new ").append(eventType.getName()).append(" instances.\n");
                        dataSet.close();
                    } else {
                        sql = "select message from Chime.EventJournal where type_id = '" + typeId + "' order by timestamp";
                        dataSet = dbconn.getDataSet(sql, true);
                        while (dataSet.next()) {
                            String msg = dataSet.getFieldValue("message").asString();
                            body.append(msg).append("\n");
                        }
                        body.append("\n");
                        dataSet.close();
                    }

                    Shape eventTypeShape = eventType.getShapes().get(0);
                    DataField field = eventTypeShape.getField("Periodic Notification");
                    List<DataFieldValue> periodics = eventType.getFieldValues(eventTypeShape, field);
                    List<InstanceId> ids = new ArrayList<InstanceId>();
                    for (DataFieldValue val : periodics) {
                        ids.add(val.getReferenceId());
                    }

                    List<User> users = CommunityUtils.getCommunityMembers(ids, admin, dbconn);
                    List<Pair> pairs = new ArrayList<Pair>();
                    for (User u : users) {
                        if (u.getSocialContext() == null) {
                            u = UserUtils.getUserById(u.getId(), admin, dbconn);
                        }

                        String emailAddr = u.getEmailAddress();
                        Pair p = new Pair();
                        if (emailAddr != null && !emailAddr.isEmpty()) {
                            p.email = emailAddr;
                        }

                        p.id = u.getId();
                        pairs.add(p);
                    }

                    if (!pairs.isEmpty()) {
                        send(pairs, dbconn, "Chime Notification - " + eventType.getName(), body.toString());
                    }

                    sql = "delete from Chime.EventJournal where type_id = '" + typeId + "'";
                    dbconn.executeStatement(sql);
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
        Notifier.instance().initScheduledJournalNotification();
    }
}

