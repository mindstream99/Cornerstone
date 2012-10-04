package com.paxxis.cornerstone.database;

public class NetezzaProvider extends DatabaseConnectionProvider {

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
		.append("/")
		.append(pool.getDbName());
		String extraParameters = pool.getExtraParameters();
		if (!extraParameters.isEmpty()) {
			builder.append("?").append(extraParameters);
		}

		return builder.toString();
	}

	@Override
	public void postConnect(DatabaseConnectionPool pool, DatabaseConnection database)
	throws DatabaseException {
	}

	@Override
	public void onShutdown(DatabaseConnectionPool pool) {
	}

	@Override
	public String getName() {
		return "netezza";
	}

	@Override
	public int getDefaultPort() {
		return 5480;
	}

}
