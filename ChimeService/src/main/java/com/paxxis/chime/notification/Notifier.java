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

import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.User;
import com.paxxis.cornerstone.database.DatabaseConnectionPool;
import com.paxxis.cornerstone.service.CornerstoneConfiguration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manages the various kinds of user notifications.
 * 
 * @author Robert Englander
 */
public class Notifier {

    private static Notifier INSTANCE = null;

    public static Notifier instance() {
        return INSTANCE;
    }

    public Notifier() {
    }

    private ExecutorService notificationExecutor = null;
    private ScheduledExecutorService scheduledExecutor = null;
    private CornerstoneConfiguration config = null;
    private DatabaseConnectionPool dbPool;
    private int purgeFreq = 1440; // defaults to 1 day
    private int periodicEventNotificationFreq = 120; // defaults to 2 hours
    private boolean periodicSummarize = false;
    private int watchNotificationFreq = 1440; // defaults to 24 hours

    public void setConnectionPool(DatabaseConnectionPool pool) {
        dbPool = pool;
    }

    public void initialize() {
        notificationExecutor = Executors.newFixedThreadPool(2);
        scheduledExecutor = Executors.newScheduledThreadPool(1);
        purgeFreq = config.getIntValue("chime.notification.eventPurge", purgeFreq);
        periodicEventNotificationFreq = config.getIntValue("chime.notification.periodicNotification", periodicEventNotificationFreq);
        periodicSummarize = config.getBooleanValue("chime.notification.periodicSummarize", periodicSummarize);
        watchNotificationFreq = config.getIntValue("chime.notification.watchNotification", watchNotificationFreq);
        
        INSTANCE = this;

        initScheduledPurge();
        initScheduledJournalNotification();
        initScheduledWatchNotification();
    }

    public void destroy() {
        notificationExecutor.shutdown();
        scheduledExecutor.shutdown();
    }

    public void setCornerstoneConfiguration(CornerstoneConfiguration config) {
        this.config = config;
    }

    boolean isPeriodicSummarize() {
        return periodicSummarize;
    }
    
    void initScheduledPurge() {
        scheduledExecutor.schedule(new EventPurger(dbPool), purgeFreq, TimeUnit.MINUTES);
    }

    void initScheduledJournalNotification() {
        scheduledExecutor.schedule(new JournalNotificationProcessor(dbPool, config), periodicEventNotificationFreq, TimeUnit.MINUTES);
    }

    void initScheduledWatchNotification() {
        scheduledExecutor.schedule(new WatchNotificationProcessor(dbPool, config), watchNotificationFreq, TimeUnit.MINUTES);
    }

    public void process(DataInstance inst, User user) {
        notificationExecutor.submit(new ImmediateNotificationProcessor(inst, user, dbPool, config));
        notificationExecutor.submit(new JournalProcessor(inst, user, dbPool, config));
    }
}
