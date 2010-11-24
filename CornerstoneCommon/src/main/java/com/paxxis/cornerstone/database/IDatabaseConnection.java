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

import java.util.Date;
import java.util.Properties;

/**
 *
 * @author Robert Englander
 */

public interface IDatabaseConnection 
{
    // connection
    public void connect(String connectionString, Properties props) throws DatabaseException;
    public void connect(String connectionString, String user, String password) throws DatabaseException;
    public void disconnect();
    public boolean isConnected();
    public String getUser();
    public String getConnectionURL();

    // transactions
    public void startTransaction() throws DatabaseException;
    public void commitTransaction() throws DatabaseException;
    public void rollbackTransaction() throws DatabaseException;
    public boolean inTransaction();

    // locking
    public void lock(String tableName, String[] keyFields, IDataValue[] keyValues) throws DatabaseException;

    // data mgmt
    public void executeInsert(String table, String[] columns, IDataValue[] values) throws DatabaseException;
    public void executeUpdate(String table, String[] columns, IDataValue[] values, String filter) throws DatabaseException;
    public void executeDelete(String table, String filter) throws DatabaseException;
    public void deleteTableData(String tableName) throws DatabaseException;
    
    // data lookup
    public IDataSet executeQuery(String sql, String table) throws DatabaseException;
    public IDataValue getValueFromQuery(String sql) throws DatabaseException;
    
    // system functions
    public Date getSystemDateTime() throws DatabaseException;
    
    // meta data
    //public String[] getTableNames() throws DatabaseException;
    //public String[] getColumnNames( String tableName ) throws DatabaseException;
    
}
