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

/**
 * 
 * @author Rob Englander
 *
 */
public class OracleProvider implements DatabaseConnectionProvider {

    @Override
    public String getConnectionUrl(DatabaseConnectionPool pool) {
	String port = "";
	Integer portNum = pool.getDbPort();
	if (portNum != null) {
	    port = ":" + portNum;
	}
	StringBuilder builder = new StringBuilder();
	builder.append(pool.getDbUrlPrefix())
	       .append(pool.getDbUsername())
	       .append("/")
	       .append(pool.getDbPassword())
	       .append("@//")
	       .append(pool.getDbHostname())
	       .append(port)
	       .append("/")
	       .append(pool.getDbSid());
        return builder.toString();
    }

    @Override
    public void postConnect(DatabaseConnectionPool pool, DatabaseConnection database)
	    throws DatabaseException {
	database.executeStatement("alter session set nls_timestamp_format='" + pool.getTimestampFormat() + "'");
    }

    @Override
    public void onShutdown(DatabaseConnectionPool pool) {
    }

    @Override
    public String getName() {
	return "oracle";
    }

}
