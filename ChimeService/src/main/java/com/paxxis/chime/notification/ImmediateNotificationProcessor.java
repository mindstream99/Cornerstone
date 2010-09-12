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
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.database.DatabaseConnection;
import com.paxxis.chime.database.DatabaseConnectionPool;
import com.paxxis.chime.data.CommunityUtils;
import com.paxxis.chime.data.DataInstanceUtils;
import com.paxxis.chime.data.UserUtils;
import com.paxxis.chime.service.ChimeConfiguration;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * Notification processor for event types that require immediate notification.
 *
 * @author Robert Englander
 */
class ImmediateNotificationProcessor extends EmailNotifier {
    private static final Logger _logger = Logger.getLogger(ImmediateNotificationProcessor.class);

    private DataInstance instance;
    private User user;
    private DatabaseConnectionPool dbPool;

    ImmediateNotificationProcessor(DataInstance inst, User user, DatabaseConnectionPool pool, ChimeConfiguration cfg) {
        super(cfg);
        instance = inst;
        this.user = user;
        dbPool = pool;
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

                    // find the communities that get immediate notification
                    Shape typeShape = eventType.getShapes().get(0);
                    DataField immediateField = typeShape.getField("Immediate Notification");
                    List<DataFieldValue> immediates = eventType.getFieldValues(typeShape, immediateField);
                    List<InstanceId> ids = new ArrayList<InstanceId>();
                    for (DataFieldValue val : immediates) {
                        ids.add(val.getReferenceId());
                    }

                    List<User> users = CommunityUtils.getCommunityMembers(ids, user, dbconn);
                    List<Pair> pairs = new ArrayList<Pair>();
                    for (User u : users) {
                        if (u.getSocialContext() == null) {
                            u = UserUtils.getUserById(u.getId(), user, dbconn);
                        }

                        String emailAddr = u.getEmailAddress();
                        if (emailAddr != null && !emailAddr.isEmpty()) {
                            Pair p = new Pair();
                            p.email = emailAddr;
                            p.id = u.getId().getValue();
                            pairs.add(p);
                        }
                    }

                    if (!pairs.isEmpty()) {
                        String body = generateEmailContent(instance, user, dbconn, eventShape, eventType);
                        send(pairs, dbconn, "Chime Notification - " + eventType.getName(), body);
                    }
                }
            } catch (Exception e) {
                _logger.error(e);
            }

            dbPool.returnInstance(dbconn, this);
        }
    }

    protected String generateEmailContent(DataInstance instance, User user, DatabaseConnection dbconn,
            Shape eventShape, DataInstance eventType) throws Exception {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Event Name: ").append(instance.getName());
        buffer.append("\nDescription: ").append(instance.getDescription());
        buffer.append("\nCreated: ").append(instance.getUpdated().toLocaleString());
        buffer.append("\nType: ").append(eventType.getName());

        // get the related content
        DataField field = eventShape.getField("Related Content");
        List<DataFieldValue> values = instance.getFieldValues(eventShape, field);
        buffer.append("\nRelated Content: ");
        for (DataFieldValue val : values) {
            DataInstance related = DataInstanceUtils.getInstance(val.getReferenceId(), user, dbconn, false, true);
            buffer.append("\n    ").append(related.getName());
        }

        // summary
        field = eventShape.getField("Summary");
        values = instance.getFieldValues(eventShape, field);
        if (values.size() > 0) {
            buffer.append("\n\nSummary: ").append(values.get(0).getName()).append("\n\n");
        }

        return buffer.toString();
    }

}

