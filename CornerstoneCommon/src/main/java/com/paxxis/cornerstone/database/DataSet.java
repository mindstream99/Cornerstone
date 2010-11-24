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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Hashtable;

/**
 *
 * @author Robert Englander
 */

public class DataSet implements IDataSet 
{
    private Statement _stmt = null;
    private ResultSet _resultSet = null;
    private int _columnCount = 0;
    private boolean _doingInsert = false;
    private Hashtable<String, Integer> _columnNameIndex = new Hashtable<String, Integer>();
    private DataSetColumn[] _columns = null;
    private String _tableName = "";
    private String[] _fieldNames;
    private DatabaseConnection _database;
    private boolean _isAudited = false;

    public DataSet(Statement stmt, String query, String tableName, boolean isAudited) throws DatabaseException 
    {
        _stmt = stmt;
        _tableName = tableName;
        _isAudited = isAudited;

        try 
        {
            //long start = System.currentTimeMillis();
            _resultSet = stmt.executeQuery(query);
            //long end = System.currentTimeMillis();
            //System.out.println("Query time: " + (end - start) + " msecs");
            
            ResultSetMetaData metaData = _resultSet.getMetaData();
            
            _columnCount = metaData.getColumnCount();

            _columns = new DataSetColumn[_columnCount];

            for (int i = 1; i <= _columnCount; i++)	
            {
                DataSetColumn info = new DataSetColumn(metaData, i, tableName);
                _columnNameIndex.put(info.getColumnName().toLowerCase(), i);
                _columns[i - 1] = info;
            }
        }
        catch (Exception ex) {

            throw new DatabaseException(ex);
        }
    }

    public void close() 
    {
        if (_resultSet != null)
        {   
            try
            {   
                _resultSet.close();
            }
            catch (SQLException ex) 
            {   
                // do not propagate errors during closing
            }
            
            _resultSet = null;
        }

        if (_stmt != null)
        {   
            try
            {   
                _stmt.close();
            }
            catch (SQLException ex) 
            {   
                // do not propagate errors during closing
            }
            
            _stmt = null;
        }
        
        resetUpdatedColumns();
    }

    private void checkOpen(String name) throws DatabaseException
    {
        if (_resultSet == null)
        {
            throw new DatabaseException("Dataset." + name + ":  DataSet not open");
        }
    }
    
    public boolean first() throws DatabaseException 
    {
        checkOpen("first");
        
        boolean status = false;
        _doingInsert = false;
        resetUpdatedColumns();
        
        try 
        {
            return _resultSet.first();
        }
        catch (SQLException ex) 
        {
        }
        
        return status;
    }

    public boolean next()  throws DatabaseException 
    {
        checkOpen("next");
        
        boolean status = false;
        try
        {   
            if (_doingInsert) 
            {
                _resultSet.moveToCurrentRow();
                resetUpdatedColumns();
                _doingInsert = false;
            }
            
            status = _resultSet.next();
        }
        catch (SQLException ex) 
        {
        }
        
        return status;
    }

    public boolean last() throws DatabaseException 
    {
        checkOpen("last");
        
        boolean status = false;
        _doingInsert = false;
        resetUpdatedColumns();

        try 
        {
            return _resultSet.last();
        }
        catch (SQLException ex) 
        {
        }
        
        return status;
    }

    public boolean previous()  throws DatabaseException 
    {
        checkOpen("previous");
        
        boolean status = false;
        try
        {   
            if (_doingInsert) 
            {
                _resultSet.moveToCurrentRow();
                _doingInsert = false;
                resetUpdatedColumns();
            }
            
            status = _resultSet.previous();
        }
        catch (SQLException ex) 
        {
        }
        
        return status;
    }

    public boolean absolute(int rowNumber) throws DatabaseException 
    {
        checkOpen("absolute");
        
        boolean status = false;
        _doingInsert = false;

        try 
        {
            return _resultSet.absolute(rowNumber);
        }
        catch (SQLException ex) 
        {
        }
        
        return status;
    }

    public int getRowNumber() throws DatabaseException 
    {
        checkOpen("getRowNumber");
        
        int rowNum = 0;
        try 
        {
            rowNum = _resultSet.getRow();
        }
        catch (SQLException ex) 
        {
            throw new DatabaseException(ex);
        }
        
        return rowNum;
    }

    //   -THIS IS VERY EXPENSIVE IF YOU HAVE A LARGE DATASET RETURNED
    public int rowCount() throws DatabaseException 
    {
        checkOpen("rowCount");

        int rowCount = 0;

        try 
        {
            int prevRow = _resultSet.getRow();
            _resultSet.last();
            rowCount = _resultSet.getRow();
            if (prevRow == 0)
            {
                _resultSet.beforeFirst();
            }
            else
            {
                _resultSet.absolute(prevRow);
            }
        }
        catch (SQLException ex) 
        {
            throw new DatabaseException(ex);
        }
        
        return rowCount;
    }
    
    public String[] getFieldNames() throws DatabaseException 
    {
        checkOpen("getFieldNames");
        
        if (_fieldNames == null) 
        {
            _fieldNames = new String[columnCount()];
            for (int i = 0; i < _fieldNames.length; i++)
            {
                _fieldNames[i] = getColumnByIndex(i+1).getColumnName();
            }
        }
        
        return _fieldNames;
    }

    public Object[] getRow() throws DatabaseException 
    {
        checkOpen("getRow");

        Object[] values = new Object[columnCount()];
        for (int i = 0; i < columnCount(); i++)
        {   
            values[i] = getFieldValue(i+1);
        }

        return values;
    }

    public IDataValue getFieldValue(String fieldName) throws DatabaseException 
    {
        checkOpen("");
        return getColumnByName(fieldName).getFieldValue();
    }

    public int getFieldIndex(String fieldName) throws DatabaseException
    {
        checkOpen("");

        return getColumnIndexByName(fieldName);
    }
    
    public String getFieldName(int colNumber) throws DatabaseException 
    {
        checkOpen("");
        DataSetColumn info = getColumnByIndex(colNumber);
        return info.getColumnName();
    }

    public IDataValue getFieldValue(int colNumber) throws DatabaseException 
    {
        checkOpen("");
        return getColumnByIndex(colNumber).getFieldValue();
    }

    public void setFieldValue(String fieldName, IDataValue value) throws DatabaseException 
    {
        checkOpen("");

        getColumnByName(fieldName).setField(value);
    }

    public void setFieldValue(int colNumber, IDataValue value) throws DatabaseException 
    {
        checkOpen("");

        getColumnByIndex(colNumber).setField(value);
    }
    public void newRow() throws DatabaseException 
    {
        checkOpen("");

        try 
        {
            _resultSet.moveToInsertRow();
            _doingInsert = true;
            resetUpdatedColumns();
        }
        catch (SQLException ex) 
        {
        }
    }

    public void update() throws DatabaseException 
    {
        checkOpen("");

        if (_doingInsert) 
        {
            sendInsertCommand();
        }
        else 
        {
            sendUpdateCommand();
        }

        resetUpdatedColumns();
    }


    public void deleteRecord() throws DatabaseException 
    {
        checkOpen("");

        try 
        {
            _resultSet.deleteRow();
        }
        catch (SQLException ex) 
        {
            throw new DatabaseException(ex);
        }
    }
    
    protected void finalize() throws Throwable 
    {
        close();
        super.finalize();
    }

    public int[] getFieldTypes() throws DatabaseException 
    {
        checkOpen("");

        int[] types = new int[columnCount()];

        for (int i = 0; i < columnCount(); i++) 
        {   
            // The column numbers in the meta data start with 1
            types[i] = getColumnByIndex(i+1).getColumnType();
        }
        
        return types;
    }

    public int columnCount() 
    {
        return _columnCount;
    }

    public void setTableName(String tableName) 
    {
        _tableName = tableName;
    }
    
    public String getTableName() 
    {
        return _tableName;
    }
    
    private int getColumnIndexByName(String fieldName) throws DatabaseException
    {
        return _columnNameIndex.get(fieldName.toLowerCase());
    }
    
    private DataSetColumn getColumnByName(String fieldName)  throws DatabaseException 
    {
        DataSetColumn column = _columns[_columnNameIndex.get(fieldName.toLowerCase()) - 1];
        if (column == null)
        {
            throw new DatabaseException("Invalid field name:"+ fieldName);
        }
        
        return column;
    }
    
    private DataSetColumn getColumnByIndex(int idx) 
    {
        return _columns[idx - 1];
    }
    
    private void sendUpdateCommand() throws DatabaseException 
    {
        StringBuffer query = new StringBuffer();

        if (updatedColumns.size() > 0) 
        {
            String table = getTableName();
            String[] fieldNames = getFieldNames();
            query.append("UPDATE ");
            query.append(table);
            query.append(" SET ");
            boolean firstTime = true;
            boolean fieldsUpdated = false;

            for (int i = 0; i < fieldNames.length; i++) 
            {
                if (updatedColumns.containsKey(fieldNames[i]))
                {
                    if (!firstTime) 
                    {
                        query.append(',');
                    }

                    firstTime = false;
                    fieldsUpdated = true;
                    query.append(table + "." + fieldNames[i] + " = ");

                    // get the updated value from the updatedColumns
                    // table.  going directly to the _resultSet at
                    // this point doesn't work
                    IDataValue valueTemp = updatedColumns.get(fieldNames[i]);
                    query.append(updatedColumns.get(valueTemp.asSQLValue()));
                }
            }
            
            if (!fieldsUpdated) 
            {
                return;
            }
        }
        
        if (query.length() > 0) 
        {
            _database.executeStatement(query.toString());
        }
        
        try 
        {
            _resultSet.cancelRowUpdates();
        }
        catch (SQLException ex) 
        {
            throw new DatabaseException(ex);
        }
    }
    
    private void sendInsertCommand() throws DatabaseException 
    {
        String[] fields = getFieldNames();
        StringBuffer query = new StringBuffer();

        String table = getTableName();
        
        query.append("INSERT INTO ");
        query.append(table);
        query.append(" SET ");
        
        if (_isAudited)
        {
            query.append("created = NOW() ");
        }
        
        for (int i = 0; i < fields.length; i++) 
        {
            // for reasons with change below, only process sql if the column is in the updatedColumn list
            // otherwise may get null pointer exception if a field isn't in the hash table
            if (updatedColumns.containsKey(fields[i]))
            {
                if (i > 0) 
                {
                    query.append(',');
                }

                // get the updated value from the updatedColumns
                // table.  going directly to the _resultSet at
                // this point is not a legal operation on the insert
                // row.
                
                query.append(table + "." + fields[i] + " = ");
                IDataValue valueTemp = updatedColumns.get(fields[i]);

                query.append(valueTemp.asSQLValue());
            }
        }
        
        _database.executeStatement(query.toString());
        
        try 
        {
            _resultSet.cancelRowUpdates();
        }
        catch (SQLException ex) 
        {
            throw new DatabaseException(ex);
        }
    }
    
    // this hash table maps the names of columns with
    // updated data with their updated values
    private Hashtable<String, IDataValue> updatedColumns = new Hashtable<String, IDataValue>();

    private void resetUpdatedColumns() 
    {
        updatedColumns.clear();
        for (int i = 0; i < _columns.length; i++)
        {
            _columns[i].updatedValue = null;
        }
    }
    
    public void setDatabase(DatabaseConnection database) 
    {
        _database = database;
    }

    public boolean isAudited() 
    {
        return _isAudited;
    }
    
    private class DataSetColumn 
    {
        public DataSetColumn(ResultSetMetaData meta, int colNumber, String tableName)
        {	
            try 
            {
                columnName = meta.getColumnName(colNumber);
                columnNumber = colNumber;

                columnType = meta.getColumnType(colNumber);
            }
            catch (SQLException ex)
            {	
                columnName = "";
                columnNumber = -1;
                columnType = Types.NULL;
            }
        }

        public String getColumnName()
        {	
            return columnName;
        }

        public int getColumnNumber()
        {	
            return columnNumber;
        }

        public int getColumnType()
        {	
            return columnType;
        }

        public void setField(IDataValue dataValue) throws DatabaseException 
        {
            try 
            {
                if (dataValue == null)
                {
                    _resultSet.updateNull(columnNumber);
                }
                else
                {
                    switch (getColumnType()) 
                    {
                        case Types.LONGVARCHAR:
                        case Types.CHAR:
                        case Types.VARCHAR:
                        case Types.CLOB:
                            _resultSet.updateString(columnNumber, dataValue.asString());
                            break;

                        case Types.BIGINT:
                            _resultSet.updateLong(columnNumber, dataValue.asLong());
                            break;

                        case Types.INTEGER:
                        case Types.SMALLINT:
                        case Types.TINYINT:
                        case Types.BIT:
                            _resultSet.updateInt(columnNumber, dataValue.asInteger());
                            break;
                            
                        case Types.DECIMAL:
                        case Types.DOUBLE:
                        case Types.NUMERIC:
                        case Types.REAL:
                            _resultSet.updateDouble(columnNumber, dataValue.asDouble());
                            break;
                            
                        case Types.FLOAT:
                            _resultSet.updateFloat(columnNumber, dataValue.asFloat());
                            break;
                            
                        case Types.DATE:
                        case Types.TIMESTAMP:
                            _resultSet.updateDate(columnNumber, Tools.toSqlDate(dataValue.asDate()));
                            break;
                        case Types.OTHER:
                            break;
                        default:
                            throw new DatabaseException("columnName="+columnName+"  unknown data type"+getColumnType());
                    }
                }
                
                // we want to save the updated value for the column.  this is
                // used when an insert statement is built instead of using
                // the _resultSet.update() method, because it is not legal
                // to ask the _resultSet for column values when positioned
                // on the insert row until AFTER the update is called.
                updatedColumns.put(getColumnName(), dataValue);
            }
            catch (SQLException ex) 
            {
                throw new DatabaseException(ex,"Error setting field: "+getColumnName());
            }
        }
        
        public IDataValue getFieldValue() throws DatabaseException 
        {
            if (updatedValue != null) 
            {
                return updatedValue;
            }
            
            IDataValue valObj = null;

            try {
                switch (getColumnType()) {
                    case Types.LONGVARCHAR:
                    case Types.CHAR:
                    case Types.VARCHAR:
                    case Types.CLOB:
                        valObj = new StringData(_resultSet.getString(columnNumber));
                        break;

                    case Types.BIGINT:
                    case Types.INTEGER:
                        valObj = new LongData(_resultSet.getLong(columnNumber));
                        break;
                        
                    case Types.SMALLINT:
                    case Types.TINYINT:
                        valObj = new IntegerData(_resultSet.getInt(columnNumber));
                        break;
                        
                    case Types.DECIMAL:
                    case Types.DOUBLE:
                    case Types.NUMERIC:
                    case Types.REAL:
                        valObj = new DoubleData(_resultSet.getDouble(columnNumber));
                        break;
                    case Types.FLOAT:
                        valObj = new FloatData(_resultSet.getFloat(columnNumber));
                        break;
                    case Types.DATE:
                        valObj = new DateValue(_resultSet.getDate(columnNumber));
                        break;
                        
                    case Types.TIMESTAMP:
                        valObj = new DateValue(_resultSet.getTimestamp(columnNumber));
                        break;
                    default: {
                        throw new DatabaseException("columnName="+columnName+"  unknown data type"+getColumnType());
                    }
                }
            }
            catch (SQLException ex)
            {
                throw new DatabaseException(ex);
            }
            return valObj;
        }
        private String	columnName;
        private int		columnNumber;
        private int		columnType;
        private IDataValue updatedValue  = null;

    }

}
