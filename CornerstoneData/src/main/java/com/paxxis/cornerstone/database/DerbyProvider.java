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
package com.paxxis.cornerstone.database;

import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

/**
 * 
 * @author Rob Englander
 *
 */
public class DerbyProvider implements DatabaseConnectionProvider {
    private static final Logger LOGGER = Logger.getLogger(DerbyProvider.class);
    private static final String DERBYDRIVEREMBEDDED = "org.apache.derby.jdbc.EmbeddedDriver";
    
    @Override
    public String getConnectionUrl(DatabaseConnectionPool pool) {
	StringBuilder builder = new StringBuilder();
	builder.append(pool.getDbUrlPrefix())
	.append(pool.getDbName());

	return builder.toString();
    }

    @Override
    public void postConnect(DatabaseConnectionPool pool, DatabaseConnection database)
	    throws DatabaseException {
    }

    @Override
    public void onShutdown(DatabaseConnectionPool pool) {
	try {
	    // if we're using Derby embedded then we need to shutdown the database
	    if (DERBYDRIVEREMBEDDED.equals(pool.getDbDriver())) {
		DriverManager.getConnection("jdbc:derby:;shutdown=true");
	    }
	} catch (SQLException e) {
	    LOGGER.info(e.getLocalizedMessage());
	}
    }

    @Override
    public String getName() {
	return "derby";
    }

    @Override
    public int getDefaultPort() {
        return 1527;
    }
}
