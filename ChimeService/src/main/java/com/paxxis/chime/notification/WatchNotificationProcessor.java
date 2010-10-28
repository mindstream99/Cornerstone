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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.data.UserUtils;
import com.paxxis.chime.database.DataSet;
import com.paxxis.chime.database.DatabaseConnection;
import com.paxxis.chime.database.DatabaseConnectionPool;
import com.paxxis.chime.service.ChimeConfiguration;
import com.paxxis.chime.service.Tools;

/**
 * 
 * @author Robert Englander
 *
 */
class WatchNotificationProcessor extends MessageNotifier {
    private static final Logger _logger = Logger.getLogger(WatchNotificationProcessor.class);

    /** the default numbers of records in the RegisteredInterest table to process at once */
    private static final int CHUNKSIZE = 25;
    private static final int MAXCHUNKSIZE = 100;
    private static final String CHUNKSIZE_PROPERTY = "chime.notification.watchChunkSize";
    
    private DatabaseConnectionPool dbPool;
    private ChimeConfiguration config;
    
    
    WatchNotificationProcessor(DatabaseConnectionPool pool, ChimeConfiguration cfg) {
        super(cfg);
        dbPool = pool;
        config = cfg;
    }

    public void run() {
    	int chunkSize = config.getIntValue(CHUNKSIZE_PROPERTY, CHUNKSIZE);
    	if (chunkSize > MAXCHUNKSIZE) {
    		chunkSize = MAXCHUNKSIZE;
    	}
    	
    	DatabaseConnection dbconn = dbPool.borrowInstance(this);
        try {
            try {
            	boolean moreRecords = true;
            	while (moreRecords) {
                	dbconn.startTransaction();

                    StringBuilder sql = new StringBuilder();
                    sql.append("select id, user_id, instance_id, instance_name, last_update from Chime.RegisteredInterest where last_update > last_notification order by user_id ")
                    				.append(Tools.getLimitClause(dbconn, chunkSize));
                    
                    // the data set must not be read only.  this is what ensures that only 1 instance of the ChimeService
                    // will process a chunk of records.  the 2nd parameter to dbconn.getDataSet specifies if the data set is read only.
                    DataSet dataSet = dbconn.getDataSet(sql.toString(), false);
                    InstanceId currentUserId = null;
                    StringBuilder msg = new StringBuilder();

                    User admin = new User();
                    admin.setId(User.SYSTEM);
                    List<InstanceId> updatedIds = new ArrayList<InstanceId>();
                    
                    while (dataSet.next()) {
                        InstanceId id = InstanceId.create(dataSet.getFieldValue("id").asString());
                        updatedIds.add(id);
                        
                        InstanceId userId = InstanceId.create(dataSet.getFieldValue("user_id").asString());
                        String instanceId = dataSet.getFieldValue("instance_id").asString();
                        String instanceName = dataSet.getFieldValue("instance_name").asString();
                        Date lastUpdate = dataSet.getFieldValue("last_update").asDate();
                        if (!userId.equals(currentUserId)) {
                            if (currentUserId != null) {
                                User user = UserUtils.getUserById(currentUserId, admin, dbconn);
                                String email = user.getEmailAddress();
                                Pair p = new Pair();
                                if (email != null && !email.isEmpty()) {
                                    // send what we have
                                    p.email = email;
                                }

                                p.id = currentUserId;
                                String body = "The following data instances have had recent activity:<br><br>" + msg.toString();
                                send(p, dbconn, "Chime Watch Notification", body);
                            }

                            currentUserId = userId;
                            msg = new StringBuilder();
                        }

                        String link = NotificationUtils.toChimeUrl(InstanceId.create(instanceId), instanceName);
                        String entry = "The data named " + link + " was updated on " + lastUpdate.toLocaleString();
                        msg.append(entry).append("<br>");
                    }
                    dataSet.close();
                    
                    if (updatedIds.size() > 0) {
                    	sql = new StringBuilder();
                        sql.append("update Chime.RegisteredInterest set last_notification = CURRENT_TIMESTAMP where last_update > last_notification and id in (");
                    	String sep = "";
                    	for (InstanceId id : updatedIds) {
                    		sql.append(sep).append("'").append(id.getValue()).append("'");
                    		sep = ",";
                    	}
                    	
                    	sql.append(")");
                        dbconn.executeStatement(sql.toString());
                    }
                    
                    dbconn.commitTransaction();
                    
                    if (msg.length() > 0) {
                        User user = UserUtils.getUserById(currentUserId, admin, dbconn);
                        String email = user.getEmailAddress();
                        Pair p = new Pair();
                        if (email != null && !email.isEmpty()) {
                            // send what we have
                            p.email = email;
                        }

                        p.id = currentUserId;
                        String body = "The following data instances have had recent activity:<br><br>" + msg.toString();
                        send(p, dbconn, "Chime Watch Notification", body);
                    }

                    moreRecords = (updatedIds.size() > 0);
            	}
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
