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

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import com.paxxis.cornerstone.base.InstanceId;

/**
 *
 * @author Robert Englander
 */
public interface IDataValue extends Serializable 
{
    public String asString();
    public Float asFloat();
    public Double asDouble();
    public Integer asInteger();
    public Long asLong();
    public Date asDate();
    public Boolean asBoolean();
    public boolean isNull();
    public InstanceId asInstanceId();
    
    /**
     * Insert the data value into the supplied prepared statement at the
     * specified index.
     */
    public void insert(PreparedStatement stmt, int idx) throws SQLException;

    /**
     * 
     * @deprecated the preferred way to prepare data for SQL inserts and updates is by
     * using prepared statements.
     */
    public String asSQLValue();
}
