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
import com.paxxis.chime.data.DataInstanceUtils;
import com.paxxis.chime.service.Tools;
import com.paxxis.cornerstone.base.InstanceId;
import com.paxxis.cornerstone.database.DatabaseConnection;
import com.paxxis.cornerstone.database.DatabaseConnectionPool;
import com.paxxis.cornerstone.service.CornerstoneConfiguration;

import java.util.List;
import org.apache.log4j.Logger;

/**
 * Writes events into the event journal.
 *
 * @author Robert Englander
 */
class JournalProcessor implements Runnable {
    private static final Logger _logger = Logger.getLogger(JournalProcessor.class);

    private DataInstance instance;
    private User user;
    private DatabaseConnectionPool dbPool;
    private CornerstoneConfiguration config;

    JournalProcessor(DataInstance inst, User user, DatabaseConnectionPool pool, CornerstoneConfiguration cfg) {
        instance = inst;
        this.user = user;
        dbPool = pool;
        config = cfg;
    }

    public void run() {

        Shape eventShape = instance.getShapes().get(0);
        if (eventShape.getId().equals(Shape.EVENT_ID)) {

            DatabaseConnection dbconn = dbPool.borrowInstance(this);
            try {
                // get the type
                DataField field = eventShape.getField("Type");
                List<DataFieldValue> values = instance.getFieldValues(eventShape, field);
                if (values.size() > 0) {
                    InstanceId id = values.get(0).getReferenceId();
                    DataInstance eventType = DataInstanceUtils.getInstance(id, user, dbconn, true, true);

                    // find the communities that get periodic notification
                    Shape typeShape = eventType.getShapes().get(0);
                    DataField immediateField = typeShape.getField("Periodic Notification");
                    List<DataFieldValue> periodics = eventType.getFieldValues(typeShape, immediateField);
                    if (periodics.size() > 0) {
                        // this one needs to be journaled
                        StringBuilder buffer = new StringBuilder();
                        buffer.append("Event Name: ").append(instance.getName());
                        buffer.append("\nDescription: ").append(instance.getDescription());
                        buffer.append("\nCreated: ").append(instance.getUpdated().toLocaleString());
                        buffer.append("\nType: ").append(eventType.getName());

                        // get the related content
                        field = eventShape.getField("Related Content");
                        values = instance.getFieldValues(eventShape, field);
                        buffer.append("\nRelated Content: ");
                        for (DataFieldValue val : values) {
                            DataInstance related = DataInstanceUtils.getInstance(val.getReferenceId(), user, dbconn, false, true);
                            buffer.append("\n    ").append(related.getName());
                        }

                        // summary
                        field = eventShape.getField("Summary");
                        values = instance.getFieldValues(eventShape, field);
                        if (values.size() > 0) {
                            buffer.append("\n\nSummary: ").append(values.get(0).getValue()).append("\n\n");
                        }

                        // add this to the journal
                        try {
                            dbconn.startTransaction();
                            InstanceId newId = com.paxxis.chime.service.Tools.getNewId(Tools.DEFAULT_EXTID);
                            String sql = "insert into Chime.EventJournal set id = '" + newId +
                                    "', type_id = '" + eventType.getId() +
                                    "', message = '" + buffer.toString() + "', timestamp = CURRENT_TIMESTAMP";
                            dbconn.executeStatement(sql);

                            dbconn.commitTransaction();

                            _logger.info("Logged event to journal");
                        } catch (Exception ee) {
                            _logger.error(ee);
                            dbconn.rollbackTransaction();
                        }
                    }

                }
            } catch (Exception e) {
                _logger.error(e);
            }

            dbPool.returnInstance(dbconn, this);
        }
    }
}

