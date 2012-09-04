package com.paxxis.cornerstone.database;

public class NetezzaProvider implements DatabaseConnectionProvider {

    @Override
    public String getConnectionUrl(DatabaseConnectionPool pool) {
	StringBuilder builder = new StringBuilder();
	builder.append(pool.getDbUrlPrefix())
	       .append("//")
	       .append(pool.getDbHostname())
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

}
