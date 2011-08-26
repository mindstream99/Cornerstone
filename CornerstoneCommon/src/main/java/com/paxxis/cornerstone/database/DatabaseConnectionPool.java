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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.paxxis.cornerstone.common.AbstractBlockingObjectPool;
import com.paxxis.cornerstone.common.PasswordGenerator;
import com.paxxis.cornerstone.database.DatabaseConnection.Type;

/**
 * Manages a pool of DatabaseConnection connections.
 *    
 * @author Robert Englander
 */ 
public class DatabaseConnectionPool extends AbstractBlockingObjectPool<DatabaseConnection> {
	private static final Logger LOGGER = Logger.getLogger(DatabaseConnectionPool.class);
	
	private static final String DERBY = "derby";
	private static final String MYSQL = "mysql";
	private static final String ORACLE = "oracle";
    private static final long ONEMINUTE = 60000L;
    private static final long DEFAULTIDLETHRESHOLD = 5L * ONEMINUTE;
    private static final long DEFAULTSWEEPCYCLE = ONEMINUTE;
    private static final String DERBYDRIVEREMBEDDED = "org.apache.derby.jdbc.EmbeddedDriver";
    
    // the database parameters
    private String dbType = null;
    private String dbSid = null;
    private String _dbHostname = null;
    private String _dbUsername = null;
    private String _dbPassword = null;
    private String _dbName = null;
    private String _dbDriver = null;
    private String _dbUrlPrefix = null;
    private String timestampFormat = null;
    private String catalog = "Chime";
    private String extraParameters = "";
    
    private boolean autoCommit = false;
    private DatabaseConnection.TransactionIsolation transactionIsolation = 
            DatabaseConnection.TransactionIsolation.TRANSACTION_READ_UNCOMMITTED;
    
    // the maximum number of instances in the pool
    private int _maximum = 1;
    
    // the minimum number of instances in the pool
    private int _minimum = 1;
    
    private long _idleThreshold = DEFAULTIDLETHRESHOLD;
    
    private long _sweepCycle = DEFAULTSWEEPCYCLE;
    
    private PasswordGenerator passwordGenerator = null;
    
    private boolean ensureConnected = true;
    private String ensureConnectedStatment = "select 1";
    
    //FIXME this is a work around to keep the api for borrowing connections the same in Chime
    //will be removed on future refactorings...
    private Map<DatabaseConnection, PoolEntry> activeConnections = 
        new HashMap<DatabaseConnection, PoolEntry>();
    
    public static class PoolEntry extends AbstractBlockingObjectPool.PoolEntry<DatabaseConnection>
    {
        private long timestamp;
        
        public PoolEntry(DatabaseConnection db) {
            super(db);
            this.timestamp = System.currentTimeMillis();
        }
        
        public long getTimestamp() {
            return this.timestamp;
        }
        
        @Override
        public void shutdown() {
            getObject().disconnect();
            super.shutdown();
        }

        @Override
        public void onReturn() {
            getObject().close();
        }       
        
    }
    
    //FIXME this should probably be pushed down into a more generic object pool...
    private static int _sweeperCounter = 0;
    class Sweeper extends Thread
    {
        boolean terminate = false;
        
        public Sweeper()
        {
            setName("Cornerstone Database Connection Pool Sweeper - " + _sweeperCounter++);
        }
        
        public synchronized void terminate()
        {
            terminate = true;
            interrupt();
        }
        
        @Override
        public void run()
        {
            while (!terminate)
            {
                try
                {
                    Thread.sleep(_sweepCycle);
                }
                catch (InterruptedException e)
                {
                }
                
                if (!terminate) {
                    sweep();
                }
            }
        }
    }
    
    private Sweeper _sweeper = new Sweeper();
    
    
    private void sweep()
    {
        long now = System.currentTimeMillis();
        
        synchronized (getSemaphore())
        {
            Map<AbstractBlockingObjectPool.PoolEntry<DatabaseConnection>, Object> activePool = getActivePool();
            int active = activePool.size();
            
            List<AbstractBlockingObjectPool.PoolEntry<DatabaseConnection>> freePool = getFreePool();
            int free = freePool.size();
            
            int total = active + free;
            
            if (free > 0 && total > _minimum)
            {
                // how many should we remove?
                int over = total - _minimum;
                int target = free - over;
                if (free > target)
                {
                    int count = free - target;

                    if (count > 0)
                    {
                        int removed = 0;
                        int last = free - 1;
                        for (int i = last; i >= 0; i--)
                        {
                            PoolEntry entry = (PoolEntry) freePool.get(i);
                            if ((now - entry.timestamp) >= _idleThreshold)
                            {
                                entry.getObject().disconnect();
                                freePool.remove(i);
                                removed++;

                                if (removed == count)
                                {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            
            // reconnect any closed connections in the free pool
            for (AbstractBlockingObjectPool.PoolEntry<DatabaseConnection> entry : freePool)
            {
                ensureConnection(entry.getObject());
            }
        }
    }
    
    public void ensureConnection(DatabaseConnection connection) {
        try
        {
            // just send any old statement; essentially like doing a ping
            connection.executeStatement(getEnsureConnectedStatment());
        }
        catch (Exception e)
        {}
        
        // if after the ping above we find the connection is closed,
        // try to reconnect it
        if (!connection.isConnected())
        {
            connect(connection);
        }
    }
    
    /**
     * Return a database connection
     * 
     * @param borrower
     * @return
     */
    public DatabaseConnection borrowInstance(Object borrower)
    {
        PoolEntry entry = borrow(borrower);
        activeConnections.put(entry.getObject(), entry);
        return entry.getObject();
    }
    
    public void returnInstance(DatabaseConnection connection, Object borrower) {
        returnInstance(activeConnections.remove(connection));
    }

    /**
     * Hook to validate the pool entry before returning it to borrower
     * @param entry
     * @return a valid pool entry
     */
    @Override
    protected <P extends AbstractBlockingObjectPool.PoolEntry<DatabaseConnection>> P validatePoolEntry(P entry) {
        if (isEnsureConnected()) {
            ensureConnection(entry.getObject());
        }
        return super.validatePoolEntry(entry);
    }
    
    public void shutdown() {
        try {
            super.shutdown();
        } finally {
	        try {
	        	if (isDerby()) {
	        		// if we're using Derby embedded then we need to shutdown the database
	        		if (DERBYDRIVEREMBEDDED.equals(_dbDriver)) {
	        			DriverManager.getConnection("jdbc:derby:;shutdown=true");
	        		}
	        	}
			} catch (SQLException e) {
				LOGGER.info(e.getLocalizedMessage());
			}
            _sweeper.terminate();
            activeConnections.clear();
        }
    }
    

    @Override
    public void initialize()
    {
    	_dbPassword = passwordGenerator.encryptPassword(_dbPassword);
    	setPoolSize(_minimum);
    	super.initialize();     	
        _sweeper.start();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected PoolEntry createPoolEntry() {
	    return new PoolEntry(create());
    }
    
    public void setPasswordGenerator(PasswordGenerator generator) {
    	passwordGenerator = generator;
    }
    
    public void setCatalog(String cat) {
    	catalog = cat;
    }

    public String getCatalog() {
    	return catalog;
    }
    
    public boolean isDerby() {
    	return DERBY.equalsIgnoreCase(dbType);
    }
    
    public boolean isMySQL() {
    	return MYSQL.equalsIgnoreCase(dbType);
    }
    
    public boolean isOracle() {
    	return ORACLE.equalsIgnoreCase(dbType);
    }
    
    public void connect(DatabaseConnection database)
    {
        try
        {
            database.setDriverName(_dbDriver);
            database.setCatalog(catalog);
            database.setAutoCommit(autoCommit);
            database.setTransactionIsolation(transactionIsolation);
            
            if (isDerby()) {
                String url = _dbUrlPrefix + _dbName;
                database.connect(url, _dbUsername, _dbPassword);
                database.setDatabaseType(Type.Derby);
            } else if (isOracle()) {
                String url = _dbUrlPrefix + _dbUsername + "/" + _dbPassword + "@//" +
                					_dbHostname + "/" + dbSid;
                database.connect(url, _dbUsername, _dbPassword);
                database.setDatabaseType(Type.Oracle);
                database.executeStatement("alter session set nls_timestamp_format='" + timestampFormat + "'");
                
            } else if (isMySQL()){
                String url = _dbUrlPrefix + "//" + _dbHostname + "/" + _dbName;
                if (!extraParameters.isEmpty()) {
                	url += "?" + extraParameters;
                }
                
                database.connect(url, _dbUsername, _dbPassword);
                database.setDatabaseType(Type.MySQL);
            } else {
            	throw new RuntimeException("Unsupport Database Type: " + dbType);
            }
        }
        catch (DatabaseException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    private DatabaseConnection create()
    {
        DatabaseConnection database = new DatabaseConnection();
        connect(database);
        return database;
    }
    
    public void setExtraParameters(String params) {
    	extraParameters = params;
    }
    
    public String getExtraParameters() {
    	return extraParameters;
    }
    
    public void setTimestampFormat(String fmt) {
    	timestampFormat = fmt;
    }
    
    public String getTimestampFormat() {
    	return timestampFormat;
    }
    
    public void setDbType(String type) {
    	dbType = type;
    }
    
    public String getDbType() {
    	return dbType;
    }
    
    public void setDbSid(String sid) {
    	dbSid = sid;
    }
    
    public String getDbSid() {
    	return dbSid;
    }
    
    public void setDbDriver(String driver)
    {
        _dbDriver = driver;
    }
    
    public String getDbDriver()
    {
        return _dbDriver;
    }
    
    public void setDbUrlPrefix(String prefix)
    {
        _dbUrlPrefix = prefix;
    }
    
    public String getDbUrlPrefix()
    {
        return _dbUrlPrefix;
    }
    
    /**
     * Set the host name for the configuration database.
     *
     * @param name the host name
     */
    public void setDbHostname(String name)
    {
        _dbHostname = name;
    }
    
    public String getDbHostname()
    {
        return _dbHostname;
    }
    
    /**
     * Set the user name to use for logging in to the configuration database.
     *
     * @param name the user name
     */
    public void setDbUsername(String name)
    {
        _dbUsername = name;
    }
    
    public String getDbUsername()
    {
        return _dbUsername;
    }
    /**
     * Set the user password for logging in to the configuration database
     *
     * @param pw the password
     */
    public void setDbPassword(String pw)
    {
    	_dbPassword = pw;
    }
    
    public String getDbPassword()
    {
        return _dbPassword;
    }
    
    /**
     * Set the name of the configuration database
     *
     * @param name the configuration database name
     */
    public void setDbName(String name)
    {
        _dbName = name;
    }

    public String getDbName()
    {
        return _dbName;
    }
    
    public void setMinPoolSize(int minimum)
    {
        _minimum = minimum;
    }
    
    public int getMinPoolSize()
    {
        return _minimum;
    }
    
    public void setMaxPoolSize(int maximum)
    {
        _maximum = maximum;
    }
    
    public int getMaxPoolSize()
    {
        return _maximum;
    }
    
    public void setIdleThreshold(float minutes)
    {
        _idleThreshold = (long)(minutes * (float)ONEMINUTE);
    }
    
    public void setSweepCycle(float minutes)
    {
        _sweepCycle = (long)(minutes * (float)ONEMINUTE);
    }
    
    public void setEnsureConnected(boolean ensureConnected) {
        this.ensureConnected = ensureConnected;
    }
    
    public boolean isEnsureConnected() {
        return this.ensureConnected;
    }

    public void setEnsureConnectedStatment(String ensureConnectedStatment) {
        this.ensureConnectedStatment = ensureConnectedStatment;
    }

    public String getEnsureConnectedStatment() {
        return ensureConnectedStatment;
    }

    public DatabaseConnection.TransactionIsolation getTransactionIsolation() {
        return transactionIsolation;
    }

    public void setTransactionIsolation(DatabaseConnection.TransactionIsolation transactionIsolation) {
        this.transactionIsolation = transactionIsolation;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }
}
