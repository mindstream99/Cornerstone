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

import com.paxxis.chime.client.common.Cursor;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceRequest;
import com.paxxis.chime.client.common.DataInstanceRequest.ClauseOperator;
import com.paxxis.chime.client.common.DataInstanceRequest.Operator;
import com.paxxis.chime.client.common.DataInstanceRequest.SortOrder;
import com.paxxis.chime.client.common.constants.SearchFieldConstants;
import com.paxxis.chime.client.common.Parameter;
import com.paxxis.chime.client.common.SearchCriteria;
import com.paxxis.chime.client.common.SearchFilter;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.database.DatabaseConnection;
import com.paxxis.chime.database.DatabaseConnectionPool;
import com.paxxis.chime.data.DataInstanceUtils;
import com.paxxis.chime.service.InstancesResponse;
import com.paxxis.chime.data.ShapeUtils;
import com.paxxis.chime.indexing.Indexer;
import com.paxxis.chime.data.SearchUtils;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * The event purger looks for Event instances whose retention days have past and
 * have not had any socialization:  that is the combined criteria for deletion.
 * They are found by index query, and those id values are used to delete from the
 * database and from the index, and the cache is cleared.  Bye Bye event instances ;)
 *
 * @author Robert Englander
 */
class EventPurger implements Runnable {
    private static final Logger _logger = Logger.getLogger(EventPurger.class);
    private static final int PURGEMAX = 100;

    private DatabaseConnectionPool dbPool;

    EventPurger(DatabaseConnectionPool pool) {
        dbPool = pool;
    }

    public void run() {
        DatabaseConnection database = dbPool.borrowInstance(this);

        try {
            long start = System.currentTimeMillis();

            int purgeCount = 0;

            User admin = new User();
            admin.setId(User.SYSTEM); 

            Shape eventShape = ShapeUtils.getInstanceById(Shape.EVENT_ID, database, true);

            SearchCriteria criteria = new SearchCriteria();

            // event shape
            SearchFilter filter = new SearchFilter();
            DataField field = new DataField();
            field.setName(SearchFieldConstants.SHAPE);
            filter.setDataField(field);
            filter.setOperator(Operator.Reference);
            filter.setValue(eventShape.getId(), eventShape.getName());
            criteria.addFilter(filter);

            // that has expired
            filter = new SearchFilter();
            field = new DataField();
            field.setName(SearchFieldConstants.EXPIRED);
            filter.setDataField(field);
            criteria.addFilter(filter);

            DataInstanceRequest req = criteria.buildRequest(admin, PURGEMAX);
            List<Parameter> params = req.getQueryParameters();

            InstancesResponse resp = SearchUtils.getInstancesByIndex(User.class, params, ClauseOperator.MatchAll, admin, false, false, new Cursor(100), SortOrder.ByName, database);
            List<DataInstance> instances = (List<DataInstance>)resp.list;

            // remove them from the db
            for (DataInstance inst : instances) {
                DataInstanceUtils.deleteInstance(inst, admin, database);
            }

            // remove them from the index
            Indexer.instance().dataDeleted(instances, dbPool);
            long end = System.currentTimeMillis();

            purgeCount = instances.size();

            _logger.info("Purged " +purgeCount+ " events in "  + (end - start) + " msecs");
        } catch (Exception e) {
            _logger.error(e);
        }

        dbPool.returnInstance(database, this);
        Notifier.instance().initScheduledPurge();
    }
}
