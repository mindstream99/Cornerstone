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


/**
 *
 * @author Robert Englander
 */
public interface IDataSet extends CloseableResource {
	
	boolean first() throws DatabaseException;
	boolean next()  throws DatabaseException;
	boolean last() throws DatabaseException;
	boolean previous()  throws DatabaseException;
	boolean absolute(int rowNum)  throws DatabaseException;

	int getRowNumber() throws DatabaseException;
	int rowCount() throws DatabaseException;
	String[] getFieldNames() throws DatabaseException;
	Object[] getRow() throws DatabaseException;
	IDataValue getFieldValue(String fieldName) throws DatabaseException;
	IDataValue getFieldValue(int fieldIndex) throws DatabaseException;
	String getFieldName(int fieldIndex) throws DatabaseException;
	void setFieldValue(String fieldName, IDataValue value) throws DatabaseException;
	void setFieldValue(int fieldIndex, IDataValue value) throws DatabaseException;
    int getFieldIndex(String fieldName) throws DatabaseException;
	void newRow() throws DatabaseException;
    void update() throws DatabaseException;
	void deleteRecord() throws DatabaseException;
	
	int[] getFieldTypes() throws DatabaseException;
	int columnCount();
}
