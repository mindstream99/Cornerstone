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

import org.apache.log4j.Logger;

/**
 *
 * @author Robert Englander
 */
public class DatabaseConnection implements IDatabaseConnection {
    private static final Logger logger = Logger.getLogger(DatabaseConnection.class);
    
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
    private String driverName = null;
    private Connection connection;
    private int transactionCount;
    private List<CloseableResource> closeables = new ArrayList<CloseableResource>();
   

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

    public void setDriverName(String name) {
        driverName = name;
        initDriver();
    }
    
    private void initDriver() {
        try {
            Class.forName(driverName);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public void connect(String url, Properties props) throws DatabaseException {
        if (isConnected()) {
            disconnect();
        }
    
        try {
            connection = DriverManager.getConnection(url, props);
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            
            checkForWarning(connection.getWarnings());
        } catch (Exception ex) {
            throw new DatabaseException(ex, "Invalid Server Connection. Please Modify Parameters");
        }
    }
    
    private <T extends Statement> T createStatement(T statement) {
        StatementProxy<T> proxy = new StatementProxy<T>(statement);
        closeables.add(proxy);
        return proxy.createStatementWrapper();
    }
    
    private Statement createStatement(int resultSetType, int resultSetConcurrency) 
            throws DatabaseException {
        validateConnection();

        try {
            return createStatement(connection.createStatement(resultSetType, resultSetConcurrency));
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
    
    public Statement createStatement() throws DatabaseException {
        //create a statement with the default ResultSet type and concurrency (according to docs)
        return createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }
    
    public void connect(String url, String user, String password) throws DatabaseException {
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);
        connect(url, props);
        
        dbUrl = url;
        dbUser = user;
    }

    public void disconnect() {
        if (isConnected()) {   
            try {   
                close();
                connection.close();
            } catch (SQLException ex) {   
                // do not propagate this exception
            }
            
            connection = null;
        }
    }
    
    public void close() {
        //we reverse the list because it is nice to close resources in reverse order of opening them...
        Collections.reverse(closeables);
        for (CloseableResource closeable : closeables) {
            closeable.close();
        }
        closeables.clear();
    }

    public boolean isConnected() {   
        try {
            validateConnection();
        } catch (DatabaseException de) {
            return false;
        }
        return true;
    }

    public IDataValue getValueFromQuery(String sql) throws DatabaseException {
        IDataSet dataset = null;
        try {
            dataset = getDataSet(sql, true);
            dataset.first();
            IDataValue val = dataset.getFieldValue(1);
            return val;
        } finally {
            if (dataset != null) {
                dataset.close();
            }
        }
    }
    
    
    public void executeStatement(String sql) throws DatabaseException {
        Statement stmt = createStatement();

        try {
            stmt.execute(sql);
        } catch (SQLException ex) {   
            throw new DatabaseException(ex);
        } finally {
            close(stmt);
        }
    }

    public IDataSet executeQuery(String sql, String table) throws DatabaseException {
        Statement stmt = createStatement();
        IDataSet resultSet = new DataSet(stmt, sql, table, false);
        closeables.add(resultSet);

        //we purposefully do not close the statement before exiting as the dataset (which wraps
        //a ResultSet) is long lived and closing a statement closes its associated ResultSets...
        return resultSet;
    }

    public void startTransaction() throws DatabaseException {
        validateConnection();
        transactionCount++;
    }

    public void commitTransaction() throws DatabaseException {
        validateConnection();

        if (!inTransaction()) {
            return;
        }

        if (transactionCount > 1) {
            transactionCount--;
            return;
        }

        try {
            connection.commit();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }

        transactionCount = 0;
    }

    public void rollbackTransaction() throws DatabaseException {
        validateConnection();

        if (!inTransaction()) {
            return;
        }

        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
        
        transactionCount = 0;
    }

    public boolean inTransaction() {
        return transactionCount > 0;
    }
    

    public void deleteTableData(String tableName) throws DatabaseException {
        try {
            String sqlStr = "delete from " + tableName;
            executeStatement(sqlStr);
        } catch (DatabaseException ex) {
            throw new DatabaseException(ex, "Couldn't delete data from table: " + tableName + ". An error occured.");
        }
    }


    public IDataSet getDataSet(String query, boolean readOnly) throws DatabaseException {
        int resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
        int resultSetConcurrency = (readOnly) ? ResultSet.CONCUR_READ_ONLY : ResultSet.CONCUR_UPDATABLE;
        String tableName = "";
        DataSet result = null;
        
        Statement stmt = createStatement(resultSetType, resultSetConcurrency);
        try {
            stmt.setFetchSize(1000);
            result = new DataSet(stmt, query, tableName, false);
            result.setTableName(tableName);
            result.setDatabase(this);
        } catch (SQLException ex) {
            //statement is useless at this point...
            close(stmt);
            throw new DatabaseException(ex);
        }
        
        //we purposefully do not close the statement before exiting as the dataset (which wraps
        //a ResultSet) is long lived and closing a statement closes its associated ResultSets...
        return result;
    }
    
    public PreparedStatement getPreparedStatement(String query) throws DatabaseException {
        validateConnection();
        
        try {
            return createStatement(connection.prepareStatement(query));
        } catch (SQLException sqle) {
            throw new DatabaseException(sqle);
        }
    }

    protected void finalize() throws Throwable {
        disconnect();
        super.finalize();
    }

    private static void checkForWarning(SQLWarning warn) throws SQLException {
        if (warn == null) {
            return;
        }
        
        // If a SQLWarning object was given, display the
        // warning messages.  Note that there could be
        // multiple warnings chained together
        
        //just to keep it all in the same log statement...
        StringBuilder builder = new StringBuilder();
        builder.append("\n *** Warning ***\n");
        do {
            builder
                .append("SQLState: ")
                .append(warn.getSQLState())
                .append("\n")
                .append("Message:  ")
                .append(warn.getMessage())
                .append("\n")
                .append("Vendor:   ")
                .append(warn.getErrorCode())
                .append("\n");
            warn = warn.getNextWarning();
        } while (warn != null);
        
        logger.warn(builder.toString());
    }

    public void executeUpdate(String table, String[] columns, IDataValue[] values, String filter) 
            throws DatabaseException {
        
        // build up the SQL statement
        StringBuffer sql = new StringBuffer("update " + table + " set ");

        int count = columns.length;
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append(columns[i]);
            sql.append(" = ");
            sql.append(values[i].asSQLValue());
        }

        sql.append(" where " + filter);

        executeStatement(sql.toString());
    }

    public void executeDelete(String table, String filter) throws DatabaseException {
        String sql = "delete from " + table + " where " + filter;
        executeStatement(sql);
    }

    public void executeInsert(String table, String[] columns, IDataValue[] values) 
            throws DatabaseException {
        
        // build up the SQL statement
        StringBuffer sql = new StringBuffer("insert into " + table + " set ");

        int count = values.length;
        for (int i = 0; i < count; i++) {
            if (i > 0) {
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

    public String getUser() {
        return dbUser;
    }

    public String getConnectionURL() {
        return dbUrl;
    }
    

    private void close(Statement statement) {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException sqle) {
            //ignore
        }
    }
    
    private void validateConnection() throws DatabaseException {
        try {
            if (connection == null || connection.isClosed()) {
                throw new DatabaseException("Database connection is closed");
            }
        } catch (SQLException sqle) {
            connection = null;
            throw new DatabaseException(sqle);
        }
    }
}
