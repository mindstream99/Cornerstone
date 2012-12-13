/*
 * Copyright 2012 the original author or authors.
 * Copyright 2012 Paxxis Technology LLC
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
public class HiveProvider extends DatabaseConnectionProvider {

	@Override
	public String getConnectionUrl(DatabaseConnectionPool pool) {
		String port = "";
		Integer portNum = pool.getDbPort();
		if (portNum != null) {
			port = ":" + portNum;
		}
		StringBuilder builder = new StringBuilder();
		builder.append(pool.getDbUrlPrefix())
		.append("//")
		.append(pool.getDbHostname())
		.append(port)
		.append("/default");

		return builder.toString();
	}

	@Override
	public void postConnect(DatabaseConnectionPool pool, DatabaseConnection database)
			throws DatabaseException {
		database.executeStatement("USE " + pool.getDbName());
	}

	@Override
	public void onShutdown(DatabaseConnectionPool pool) {
	}

	@Override
	public String getName() {
		return "hive";
	}

	@Override
	public int getDefaultPort() {
		return 10000;
	}

}
