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


package com.paxxis.chime.database;

import com.paxxis.chime.common.DataLatch;
import com.paxxis.chime.service.ChimeConfigurable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Manages a pool of DatabaseConnection connections.
 *    
 * @author Robert Englander
 */ 
public class DatabaseConnectionPool extends ChimeConfigurable
{
    private static final long ONEMINUTE = 60000L;
    private static final long DEFAULTIDLETHRESHOLD = 5L * ONEMINUTE;
    private static final long DEFAULTSWEEPCYCLE = ONEMINUTE;
    
    // the database parameters
    String _dbHostname = null;
    String _dbUsername = null;
    String _dbPassword = null;
    String _dbName = null;
    String _dbDriver = null;
    String _dbUrlPrefix = null;

    // the maximum number of instances in the pool
    private int _maximum = 1;
    
    // the minimum number of instances in the pool
    private int _minimum = 1;
    
    private long _idleThreshold = DEFAULTIDLETHRESHOLD;
    
    private long _sweepCycle = DEFAULTSWEEPCYCLE;
    
    class PoolEntry
    {
        long timestamp;
        DatabaseConnection database;
        
        public PoolEntry(DatabaseConnection db)
        {
            database = db;
            timestamp = System.currentTimeMillis();
        }
    }
    
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
                
                sweep();
            }
        }
    }
    
    private Sweeper _sweeper = new Sweeper();
    
    // free pool
    private ArrayList<PoolEntry> _freePool = new ArrayList<PoolEntry>();
    
    // assigned connections
    private HashMap<DatabaseConnection, Object> _activePool = new HashMap<DatabaseConnection, Object>();
    
    // borrowers waiting for instances to free up
    private ArrayList<DataLatch> _borrowersInWaiting = new ArrayList<DataLatch>();
    
    // the semaphore for protected the pools
    private final Object _semaphore = new Object();
    
    public DatabaseConnectionPool()
    {
    }

    private void sweep()
    {
        long now = System.currentTimeMillis();
        
        synchronized (_semaphore)
        {
            int active = _activePool.size();
            int free = _freePool.size();
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
                            PoolEntry entry = _freePool.get(i);
                            if ((now - entry.timestamp) >= _idleThreshold)
                            {
                                entry.database.disconnect();
                                _freePool.remove(i);
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
            for (PoolEntry entry : _freePool)
            {
                try
                {
                    // just send any old statement; essentially like doing a ping
                    entry.database.executeStatement("select 1");
                }
                catch (Exception e)
                {}
                
                // if after the ping above we find the connection is closed,
                // try to reconnect it
                if (!entry.database.isConnected())
                {
                    connect(entry.database);
                }
            }
        }
    }
    
    /**
     * TODO make sure the connection is still open before returning
     * it to the caller.  it may have been closed.  if so, it needs to
     * be reconnected.
     * 
     * @param borrower
     * @return
     */
    public DatabaseConnection borrowInstance(Object borrower)
    {
        DatabaseConnection db = null;

        synchronized (_semaphore)
        {
            if (!_freePool.isEmpty())
            {
                // there's a free entry, give it to this borrower.
                PoolEntry entry = _freePool.remove(0);
                db = entry.database;
                _activePool.put(db, borrower);
            }
            else if (_activePool.size() < _maximum)
            {
                // we can create a new one
                db = create();
                _activePool.put(db, borrower);
            }
        }
        
        if (db == null)
        {
            // the borrower will have to wait until an instance
            // is returned by another borrower
            DataLatch latch = new DataLatch();
            _borrowersInWaiting.add(latch);
            db = (DatabaseConnection)latch.waitForObject();
        }

        return db;
    }
    
    public void returnInstance(DatabaseConnection database, Object borrower)
    {
        DataLatch latch = null;
        
        synchronized (_semaphore)
        {
            // take this one off the active pool
            _activePool.remove(database);

            if (_borrowersInWaiting.size() > 0)
            {
                // give this one to the next borrower
                latch = _borrowersInWaiting.remove(0);
                _activePool.put(database, borrower);
            }
            else
            {
                // put it back into the free pool
                PoolEntry entry = new PoolEntry(database);
                _freePool.add(entry);
            }
        }

        if (latch != null)
        {
            latch.setObject(database);
        }
    }

    @Override
    public void destroy() {
        for (PoolEntry entry : _freePool) {
            entry.database.disconnect();
        }

        for (DatabaseConnection conn : _activePool.keySet()) {
            conn.disconnect();
        }
    }

    @Override
    public void initialize()
    {
        for (int i = 0; i < _minimum; i++)
        {
            DatabaseConnection database = create();
            PoolEntry entry = new PoolEntry(database);
            _freePool.add(entry);
        }
        
        _sweeper.start();
    }
    
    public void connect(DatabaseConnection database)
    {
        try
        {
            database.setDriverName(_dbDriver);

            if (_dbHostname.equals("embedded")) {
                String url = _dbUrlPrefix + _dbName;
                database.connect(url, _dbUsername, _dbPassword);
            } else {
                String url = _dbUrlPrefix + "//" + _dbHostname + "/" + _dbName;
                database.connect(url, _dbUsername, _dbPassword);
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
}
