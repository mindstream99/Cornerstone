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

import java.sql.SQLException;

/**
 *
 * @author Robert Englander
 */
public class DatabaseException extends Exception
{
    public DatabaseException() 
    {
    }
    
    public DatabaseException(String message) 
    {
        super(message);
    }
    
    public DatabaseException(Throwable originalException) 
    {
        super(originalException);
        dumpDetails(originalException);
    }
    
    public DatabaseException(Throwable originalException, String message) 
    {
        super(message, originalException);
        dumpDetails(originalException);
    }
    
    private void dumpDetails(Throwable ex) 
    {
        if(!(ex instanceof SQLException)) return;
        SQLException sqlEx = (SQLException)ex;
        StringBuffer msgBuffer = new StringBuffer(512);
        msgBuffer.append("DatabaseException: SQL Exception Chain");
        int i = 1;
        while(sqlEx != null) 
        {
            msgBuffer.append("\n" + i + " :\n");
            msgBuffer.append("ErrorCode: " + sqlEx.getErrorCode());
            msgBuffer.append("\nSQL State: " + sqlEx.getSQLState() + "\n");
            msgBuffer.append(sqlEx.getMessage());
            msgBuffer.append("\n------------");
            i++;
            sqlEx = sqlEx.getNextException();
        }
        
        // System.out.println(msgBuffer.toString());
    }
}
