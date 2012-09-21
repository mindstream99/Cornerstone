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

import com.paxxis.cornerstone.common.CornerstoneConfiguration;

/**
 * @author Robert Englander
 */
public class DatabaseBackedConfiguration extends CornerstoneConfiguration {
    private String dbQuery = null;
    private String keyColumn = null;
    private String valueColumn = null;
    private DatabaseConnectionPool databasePool = null;
    
    public DatabaseBackedConfiguration() {
    }

    public void setup() {
        if (databasePool != null) {
        	DatabaseConnection database = null;
        	IDataSet dataSet = null;
        	try {
                database = databasePool.borrowInstance(this);
                dataSet = database.getDataSet(dbQuery, true);
                
                int keyIdx = dataSet.getFieldIndex(keyColumn);
                int valueIdx = dataSet.getFieldIndex(valueColumn);
                
                while (dataSet.next()) {
                    String name = dataSet.getFieldValue(keyIdx).asString();
                    String value = dataSet.getFieldValue(valueIdx).asString();
                    getLocalPropertyMap().put(name, value);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
            	if (dataSet != null) {
                    dataSet.close();
            	}

            	if (database != null) {
                	databasePool.returnInstance(database, this);
            	}
            }
        }
    }
    
    public void setDatabasePool(DatabaseConnectionPool pool) {
        databasePool = pool;
    }
    
    public void setDbQuery(String query) {
        dbQuery = query;
    }

    public void setValueColumn(String name) {
        valueColumn = name;
    }
    
    public void setKeyColumn(String name) {
        keyColumn = name;
    }
}
