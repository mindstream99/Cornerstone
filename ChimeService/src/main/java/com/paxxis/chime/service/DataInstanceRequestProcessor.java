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

package com.paxxis.chime.service;

import com.paxxis.chime.data.DataInstanceUtils;
import com.mysql.jdbc.CommunicationsException;
import com.paxxis.chime.client.common.Cursor;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceRequest;
import com.paxxis.chime.client.common.DataInstanceRequest.ClauseOperator;
import com.paxxis.chime.client.common.DataInstanceRequest.Depth;
import com.paxxis.chime.client.common.DataInstanceRequest.SortOrder;
import com.paxxis.chime.client.common.DataInstanceRequest.Style;
import com.paxxis.chime.client.common.DataInstanceResponse;
import com.paxxis.chime.client.common.DataInstanceResponse.Reason;
import com.paxxis.chime.client.common.Parameter;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.database.DatabaseConnection;
import com.paxxis.chime.database.DatabaseConnectionPool;
import com.paxxis.chime.client.common.ErrorMessage;
import com.paxxis.chime.client.common.Message;
import com.paxxis.chime.common.MessagePayload;
import com.paxxis.chime.service.MessageProcessor; 
import com.paxxis.chime.data.SearchUtils;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * 
 * @author Robert Englander
 */
public class DataInstanceRequestProcessor extends MessageProcessor {
    private static final Logger _logger = Logger.getLogger(DataInstanceRequestProcessor.class);

    // the database connection pool
    DatabaseConnectionPool _pool;
    
    /**
     * Constructor
     *
     */
    public DataInstanceRequestProcessor(MessagePayload payloadType, DatabaseConnectionPool pool)
    {
        super(payloadType);
        _pool = pool;
    }
    
    protected Message process(boolean ignorePreviousChanges)
    {
        Object payload = getPayload();
        DataInstanceRequest requestMessage = (DataInstanceRequest)new DataInstanceRequest().createInstance(payload);
        
        // build up a response
        Message response = null;
        DatabaseConnection database = _pool.borrowInstance(this);
        
        boolean tryAgain = true;
        boolean retried = false;
        
        while (tryAgain)
        {
            tryAgain = false;
            DataInstanceResponse dtr = new DataInstanceResponse();
            dtr.setRequest(requestMessage);
            response = dtr;
            
            try
            {
                User user = requestMessage.getUser();
                  
                Tools.validateUser(user);
                
                boolean deep = requestMessage.getDepth() == Depth.Deep;

                if (requestMessage.getStyle() == Style.InstanceId)
                {
                    long start = System.currentTimeMillis();
                    DataInstance instance = DataInstanceUtils.getInstance(requestMessage.getInstanceId(), 
                                                requestMessage.getUser(), database, deep, true);
                                                
                    long end = System.currentTimeMillis();
                    _logger.info("Data instance retrieved in " + (end - start) + " msecs");
                    List<DataInstance> instances = new ArrayList<DataInstance>();
                    
                    // currently we're getting the instance and then checking its community scopes against the
                    // user.  would it be more efficient to include the scope check somewhere inside DataInstanceUtils.getInstance()?
                    if (instance == null) {
                        dtr.setReason(Reason.NoSuchData);
                    }
                    else if (Tools.isDataVisible(instance, user)) {
                        instances.add(instance);
                    } else {
                        dtr.setReason(Reason.NotVisible);
                    }
                    
                    dtr.setDataInstances(instances);
                }
                else
                {  
                    List<Parameter> params = requestMessage.getQueryParameters();

                    /*
                    String typeName = requestMessage.getDataShapeName();
                    Class instanceClass;
                    Shape t = null;
                    if (typeName != null) {
                        t = ShapeUtils.getInstance(typeName, database, true);
                        instanceClass = Tools.getClass(t);
                    } else {
                        instanceClass = DataInstance.class;
                    }
                    */

                    long start = System.currentTimeMillis();
                    
                    Cursor cursor = requestMessage.getCursor();
                    SortOrder sortOrder = requestMessage.getSortOrder();
                    
                    InstancesResponse resp;

                    if (requestMessage.getStyle() == Style.ComplexSearch) {
                        resp = SearchUtils.getInstancesByIndex(DataInstance.class, params, requestMessage.getClauseOperator(),
                                                                user, false, false, cursor, sortOrder, database);
                    } else if (requestMessage.getStyle() == Style.ReferenceSearch) {
                        List<Parameter> parameters = new ArrayList<Parameter>();
                        Parameter p = new Parameter();
                        p.clauseOperator = ClauseOperator.MatchAll;
                        p.fieldName = "reference";
                        p.fieldValue = String.valueOf(requestMessage.getInstanceId());
                        parameters.add(p);
                        resp = SearchUtils.getInstancesByIndex(DataInstance.class, parameters, requestMessage.getClauseOperator(),
                                                                user, false, false, cursor, sortOrder, database);
                    } else {
                        boolean shortQuery = requestMessage.getStyle() == Style.KeywordSearchShort;
                        resp = SearchUtils.getInstancesByKeyword(requestMessage.getKeywords(), user, cursor, sortOrder, shortQuery, database);
                    }
                    
                    //InstancesResponse resp = DataInstanceUtils.getInstances(instanceClass, params, requestMessage.getClauseOperator(),
                    //                                        user, t, cursor, database, deep);

                    long end = System.currentTimeMillis();
                    _logger.info(resp.list.size() + " Data instance(s) retrieved in " + (end - start) + " msecs");
                    dtr.setDataInstances(resp.list);
                    dtr.setCursor(resp.cursor);
                }
            }
            catch (SessionValidationException sve)
            {
                ErrorMessage em = new ErrorMessage();
                em.setRequest(requestMessage);
                em.setMessage(sve.getMessage());
                em.setType(ErrorMessage.Type.SessionExpiration);
                response = em;
            }
            catch (Exception e)
            {
                if (e.getCause() instanceof CommunicationsException)
                {
                    if (!retried && !database.isConnected())
                    {
                        retried = true;
                        tryAgain = true;
                        
                        _pool.connect(database);
                    }
                }
                else
                {
                    ErrorMessage em = new ErrorMessage();
                    em.setRequest(requestMessage);
                    em.setMessage(e.getMessage());
                    response = em;
                }
            }
        }

        _pool.returnInstance(database, this);
        
        return response;
    }
    
}
