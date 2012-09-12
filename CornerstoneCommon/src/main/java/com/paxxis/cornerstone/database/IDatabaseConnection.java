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

import java.util.Properties;

/**
 *
 * @author Robert Englander
 */

public interface IDatabaseConnection extends CloseableResource
{
    // connection
    void connect(String connectionString, Properties props) throws DatabaseException;
    void connect(String connectionString, String user, String password) throws DatabaseException;
    void disconnect(boolean force);
    void disconnect();
    boolean isConnected();
    String getUser();
    String getConnectionURL();

    // transactions
    void startTransaction() throws DatabaseException;
    void commitTransaction() throws DatabaseException;
    void rollbackTransaction() throws DatabaseException;
    boolean inTransaction();

    // data mgmt
    void executeInsert(String table, String[] columns, IDataValue[] values) throws DatabaseException;
    void executeUpdate(String table, String[] columns, IDataValue[] values, String filter) throws DatabaseException;
    void executeDelete(String table, String filter) throws DatabaseException;
    void deleteTableData(String tableName) throws DatabaseException;
    
    // data lookup
    IDataSet getDataSet(String sql, boolean readOnly) throws DatabaseException;
    IDataSet executeQuery(String sql, String table) throws DatabaseException;
    IDataValue getValueFromQuery(String sql) throws DatabaseException;
    
}
