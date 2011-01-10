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

import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

/**
 *
 * @author Robert Englander
 */
public class DatabaseConnection implements IDatabaseConnection {
	public enum Type {
		Oracle,
		MySQL,
		Derby,
		Unknown
	}
	
	private Type type = Type.Unknown;
	private String catalog = "Chime";
    private String dbUrl;
    private String dbUser;
    private boolean driverInitialized = false;
    private String driverName = null;
    private Connection connection;
    private int transactionCount;

    public void setCatalog(String cat) {
    	catalog = cat;
    }

    public String getCatalog() {
    	return catalog;
    }
    
    public void setDatabaseType(Type type) {
    	this.type = type;
    }
    
    public boolean isDerby() {
    	return type == Type.Derby;
    }
    
    public boolean isMySQL() {
    	return type == Type.MySQL;
    }
    
    public boolean isOracle() {
    	return type == Type.Oracle;
    }

    public void setDriverName(String name)
    {
        driverName = name;
        initDriver();
    }
    
    private void initDriver()
    {
        try 
        {
            Class clazz = Class.forName(driverName);
        }
        catch (ClassNotFoundException ex) 
        {
            throw new RuntimeException(ex);
        }
    }
    
    public void connect(String url, Properties props) throws DatabaseException 
    {
        if (isConnected())
        {
            disconnect();
        }
    
        try 
        {
            connection = DriverManager.getConnection(url, props);
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(connection.TRANSACTION_READ_COMMITTED);
            
            checkForWarning(connection.getWarnings());
        }
        catch (Exception ex) 
        {
            throw new DatabaseException(ex, "Invalid Server Connection. Please Modify Parameters");
        }
    }

    public Statement createStatement() throws DatabaseException
    {
        try
        {
            return connection.createStatement();
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
    }
    
    public void connect(String url, String user, String password) throws DatabaseException 
    {
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);
        connect(url, props);
        
        dbUrl = url;
        dbUser = user;
    }

    public void disconnect() 
    {
        if (isConnected())
        {   
            try
            {   
                connection.close();
            }
            catch (SQLException ex) 
            {   
                // do not propagate this exception
            }
            
            connection = null;
        }
    }
    public boolean isConnected()
    {   boolean connected = false;
        
        try
        {   
            connected = (connection != null) && (!connection.isClosed());
        }
        catch (SQLException ex) 
        {   
            // the connection is no good
            connection = null;
            connected = false;
        }
        
        return connected;
    }

    public IDataValue getValueFromQuery(String sql) throws DatabaseException 
    {
        DataSet dataset = null;
        try{
            dataset = getDataSet(sql, true);
            dataset.first();
            IDataValue val = dataset.getFieldValue(1);
            dataset.close();
            return val;
        }
        finally {
            if (dataset!=null) dataset.close();
        }
    }
    
    public Date getSystemDateTime() throws DatabaseException 
    {
        
        return null;
    }
    
    public void executeStatement(String sql) throws DatabaseException 
    {
        //sql = databaseSpecific.processSQLBeforeSending(sql.trim());
        Statement stmt = null;
        boolean hasResultSet = false;
        boolean hasResultCount = false;
        Exception exception = null;

        if (!isConnected())
            throw new DatabaseException("connection is not open");

        try {
            stmt = connection.createStatement();
        }
        catch (SQLException ex)
        {   throw new DatabaseException(ex);
        }

        // at this point, we have a valid statement.
        // we should make sure that it gets closed

        try {
            hasResultSet = stmt.execute(sql);
        }
        catch (SQLException ex)
        {   exception = ex; // this is the only
            // message our callers care about
        }

        try {    // make sure that we close all result sets
            // The java doc says that we need to check
            // getUpdateCount() until we get -1, but,
            // the Oracle driver seems to return only 0's
            while (hasResultSet)
            {   if (hasResultSet)
                {   stmt.getResultSet().close();
                }
                hasResultSet = stmt.getMoreResults();
            }
        }
        catch (SQLException ex) {   // ignore messages while closing things up
        }

        try
        {   stmt.close();
        }
        catch (SQLException ex) {   // ignore messages while closing things up
        }

        if (exception != null)
            throw new DatabaseException(exception);
    }

    public IDataSet executeQuery(String sql, String table) throws DatabaseException 
    {
        //sql = databaseSpecific.processSQLBeforeSending(sql.trim());
        Statement stmt = null;
        boolean hasResultSet = false;
        boolean hasResultCount = false;
        Exception exception = null;

        IDataSet resultSet = null;
        
        if (!isConnected())
            throw new DatabaseException("connection is not open");

        try {
            stmt = connection.createStatement();
        }
        catch (SQLException ex)
        {   throw new DatabaseException(ex);
        }

        // at this point, we have a valid statement.
        // we should make sure that it gets closed

        resultSet = new DataSet(stmt, sql, table, false);

        try {    // make sure that we close all result sets
            // The java doc says that we need to check
            // getUpdateCount() until we get -1, but,
            // the Oracle driver seems to return only 0's
            while (hasResultSet)
            {   if (hasResultSet)
                {   stmt.getResultSet().close();
                }
                hasResultSet = stmt.getMoreResults();
            }
        }
        catch (SQLException ex) {   // ignore messages while closing things up
        }

        try
        {   stmt.close();
        }
        catch (SQLException ex) {   // ignore messages while closing things up
        }

        if (exception != null)
            throw new DatabaseException(exception);
        
        return resultSet;
    }

    public void startTransaction() throws DatabaseException {
        if (!isConnected()) 
        {
            throw new DatabaseException("connection is not open");
        }
        
        if (transactionCount == 0) 
        {
            //executeStatement("start transaction");
        }
        
        transactionCount++;
    }

    public void commitTransaction() throws DatabaseException 
    {
        if (!isConnected())
            throw new DatabaseException("connection is not open");

        if (!inTransaction()) 
        {
            return;
        }

        if (transactionCount > 1) 
        {
            transactionCount--;
            return;
        }

        //executeStatement("commit");
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }

        transactionCount = 0;
    }

    public void rollbackTransaction() throws DatabaseException {
        if (!isConnected())
            throw new DatabaseException("connection is not open");

        if (!inTransaction()) 
        {
            return;
        }

        //executeStatement("rollback");
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
        
        transactionCount = 0;
    }

    public boolean inTransaction() 
    {
        return transactionCount > 0;
    }
    

    public void lock(String tableName, String[] keyFields, IDataValue[] keyValues) throws DatabaseException 
    {
    }


    public void deleteTableData(String tableName) throws DatabaseException 
    {
        try
        {
            String sqlStr = "delete from " + tableName;
            executeStatement(sqlStr);
        }
        catch (DatabaseException ex)
        {
            throw new DatabaseException(ex,"Couldn't delete data from table: " +tableName+". An error occured.");
        }
    }


    public DataSet getDataSet(String query, boolean readOnly) throws DatabaseException {
        int resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
        int resultSetConcurrency = (readOnly) ?  ResultSet.CONCUR_READ_ONLY : ResultSet.CONCUR_UPDATABLE;
        String tableName = "";
        String newQuery = query;

        boolean isAudited = false;
        try 
        {
            Statement stmt = connection.createStatement(resultSetType, resultSetConcurrency);
            stmt.setFetchSize(1000);

            //query = databaseSpecific.processSQLBeforeSending(query.trim());

            // we only need to lock the rows if we're
            // inside a transaction and it's not read-only
            if (inTransaction() && !readOnly)
            {
                //lockRowFromQuery(query);
            }

            DataSet result = new DataSet(stmt, query, tableName, isAudited);
            result.setTableName(tableName);
            result.setDatabase(this);
            return result;
        }
        catch (SQLException ex) {
            throw new DatabaseException(ex);
        }
    }
    
    public PreparedStatement getPreparedStatement(String query) throws SQLException {
        return connection.prepareStatement(query);       
    }

    protected void finalize() throws Throwable {
        disconnect();
        super.finalize();
    }

    private static boolean checkForWarning(SQLWarning warn)
    throws SQLException {
        boolean rc = false;

        // If a SQLWarning object was given, display the
        // warning messages.  Note that there could be
        // multiple warnings chained together

        if (warn != null)
        {	System.out.println("\n *** Warning ***\n");
                rc = true;
                while (warn != null) {
                    System.out.println("SQLState: " + warn.getSQLState());
                    System.out.println("Message:  " + warn.getMessage());
                    System.out.println("Vendor:   " + warn.getErrorCode());
                    System.out.println("");
                    warn = warn.getNextWarning();
                }
        }
        return rc;
    }

    public void executeUpdate(String table, String[] columns, IDataValue[] values, String filter) throws DatabaseException
    {
        // lock the rows if that's the rule
        
        // build up the SQL statement
        StringBuffer sql = new StringBuffer("update " + table + " set ");

        int count = columns.length;
        for (int i = 0; i < count; i++)
        {
            if (i > 0)
            {
                sql.append(", ");
            }
            sql.append(columns[i]);
            sql.append(" = ");
            sql.append(values[i].asSQLValue());
        }

        sql.append(" where " + filter);

        executeStatement(sql.toString());
    }

    public void executeDelete(String table, String filter) throws DatabaseException
    {
        // locking...
        
        String sql = "delete from " + table + " where " + filter;
        executeStatement(sql);
    }

    public void executeInsert(String table, String[] columns, IDataValue[] values) throws DatabaseException
    {
        // build up the SQL statement
        StringBuffer sql = new StringBuffer("insert into " + table + " set ");

        // AUDITABLE????
        
        int count = values.length;
        for (int i = 0; i < count; i++)
        {
            if (i > 0) 
            {
                sql.append(',');
            }

            // get the updated value from the updatedColumns
            // table.  going directly to the _resultSet at
            // this point is not a legal operation on the insert
            // row.

            sql.append(table + "." + columns[i] + " = ");
            IDataValue valueTemp = values[i];

            sql.append(valueTemp.asSQLValue());
        }
        
        executeStatement(sql.toString());
    }

    public String getUser()
    {
        return dbUser;
    }

    public String getConnectionURL()
    {
        return dbUrl;
    }

}
