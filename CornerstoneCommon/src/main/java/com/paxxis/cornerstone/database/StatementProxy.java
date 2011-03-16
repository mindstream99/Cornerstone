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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


/**
 * StatementProxy is a wrapper class around jdbc Statement objects
 * 
 * @author Philip Kubasov
 *
 */
public class StatementProxy<T extends Statement> implements InvocationHandler {

    private static final Logger logger = Logger.getLogger(StatementProxy.class);
    
    private T wrapped;
    private List<ResultSet> resultSets = new ArrayList<ResultSet>();   
    
    
    /**
     *  
     * @param stmt
     */
    StatementProxy(T stmt) {
        wrapped  = stmt;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
       Object o = method.invoke(wrapped, args); 
       if (o instanceof ResultSet) {
           registerResultSet((ResultSet) o);
       }
       return o;
    }
    
    @SuppressWarnings("unchecked")
    public T createStatementWrapper() {
        Class<? extends Statement> clazz = wrapped.getClass();        
        //we use the thread's context classloader because that is what is appropriate when
        //you maybe running in a multi-classloader environment like a servlet container
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return (T) Proxy.newProxyInstance(classLoader, clazz.getInterfaces(), this);
    }    
   
    private void registerResultSet(ResultSet rs) {
        resultSets.add(rs);
    }
    
    public void cleanUp() {
        for (ResultSet rs : resultSets) {
            disposeResultSet(rs);
        }
        disposeStatement();
        resultSets.clear();
    }      
    
    private void disposeResultSet(ResultSet rs) {
       try {                    
           rs.close();                
       } catch (SQLException e) {
           logger.error(e);
       }            
    }
    
    private void disposeStatement() {
       try {
           wrapped.close();
       } catch(SQLException e) {
           logger.error(e);
       }         
    }    
    
    protected void finalize() {
        cleanUp();
    }

}
