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

import com.paxxis.chime.data.CacheManager;
import com.paxxis.chime.data.ScopeUtils;
import com.paxxis.chime.data.ShapeUtils;
import com.paxxis.chime.data.DataInstanceUtils;
import com.mysql.jdbc.CommunicationsException;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstance.LockType;
import com.paxxis.chime.client.common.DataInstanceEvent;
import com.paxxis.chime.client.common.DataInstanceEvent.EventType;
import com.paxxis.chime.client.common.DataInstanceHelper;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.EditDataInstanceRequest;
import com.paxxis.chime.client.common.EditDataInstanceResponse;
import com.paxxis.chime.database.DatabaseConnection;
import com.paxxis.chime.database.DatabaseConnectionPool;
import com.paxxis.chime.client.common.ErrorMessage;
import com.paxxis.chime.client.common.Message;
import com.paxxis.chime.client.common.Tag;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.common.MessagePayload;
import com.paxxis.chime.data.FieldDataUtils;
import com.paxxis.chime.data.PrimaryDataUtils;
import com.paxxis.chime.data.InstanceShapeUtils;
import java.util.List;
import org.apache.log4j.Logger;
/**
 *
 * @author Robert Englander
 */
public class EditDataRequestProcessor extends MessageProcessor {
    private static final Logger _logger = Logger.getLogger(EditDataRequestProcessor.class);

    // the database connection pool
    private DatabaseConnectionPool _pool;
    
    private NotificationTopicSender _topicSender;

    /**
     * Constructor
     *
     */
    public EditDataRequestProcessor(MessagePayload payloadType, DatabaseConnectionPool pool, NotificationTopicSender sender)
    {
        super(payloadType);
        _pool = pool;
        _topicSender = sender;
    }

    protected Message process(boolean ignorePreviousChanges)
    {
        Object payload = getPayload();
        EditDataInstanceRequest requestMessage = (EditDataInstanceRequest)new EditDataInstanceRequest().createInstance(payload);
        
        // build up a response
        Message response = null;
        DatabaseConnection database = _pool.borrowInstance(this);
        
        boolean tryAgain = true;
        boolean retried = false;
        
        while (tryAgain)
        {
            tryAgain = false;
            EditDataInstanceResponse dtr = new EditDataInstanceResponse();
            dtr.setRequest(requestMessage);
            response = dtr;
            
            try
            { 
                List<Shape> shapes = requestMessage.getShapes();
                User user = requestMessage.getUser();
                Tools.validateUser(user);
                if (shapes.isEmpty())
                {
                    String typeName = requestMessage.getShapeName();
                    if (typeName != null) {
                        Shape dt = ShapeUtils.getInstance(typeName, database, true);
                        requestMessage.addShape(dt);
                        shapes.add(dt);
                    }
                }

                if (!shapes.isEmpty()) {
                    // some requests don't pass a shape
                    DataInstanceHelper helper = DataInstanceHelperFactory.getHelper(Tools.getClass(shapes.get(0)));
                    if (helper != null)
                    {
                        helper.processBeforeWrite(requestMessage);
                    }
                }

                // make sure the data hasn't changed already
                if (requestMessage.getDataInstance() != null && !ignorePreviousChanges && requestMessage.getOperation() != EditDataInstanceRequest.Operation.Create) {
                    DataInstance inst = DataInstanceUtils.getInstance(requestMessage.getDataInstance().getId(),
                            requestMessage.getUser(), database, false, true);
                    if (inst.getUpdated().after(requestMessage.getDataInstance().getUpdated())) {
                        throw new StaleDataEditException("The data has been changed by another user.");
                    } else {
                        // let's see if the lock has changed
                        if (inst.getLockType() != LockType.NONE && !user.getId().equals(inst.getLockedBy().getId())) {
                            throw new LockedDataEditException("This data has been locked by user " + inst.getLockedBy().getName());
                        }
                    }
                }

                DataInstanceEvent event = new DataInstanceEvent();

                DataInstance instance = null;

                if (requestMessage.getOperation() == EditDataInstanceRequest.Operation.Create)
                {
                    long start = System.currentTimeMillis();
                    instance = DataInstanceUtils.createInstance(shapes, requestMessage.getName(),
                                            requestMessage.getDescription(), null, new String[] {null, null},
                                            requestMessage.getFieldData(), requestMessage.getScopes(), user, database);
                    long end = System.currentTimeMillis();
                    _logger.info("Data instance created in " + (end - start) + " msecs");
    
                    event.setEventType(EventType.Create);
                    event.setDataInstance(instance);
                }
                else if (requestMessage.getOperation() == EditDataInstanceRequest.Operation.Delete)
                {
                    long start = System.currentTimeMillis();
                    instance = requestMessage.getDataInstance();
                    DataInstanceUtils.deleteInstance(instance, requestMessage.getUser(), database);
                    long end = System.currentTimeMillis();
                    _logger.info("Data instance deleted in " + (end - start) + " msecs");

                    event.setEventType(EventType.Delete);
                    event.setDataInstance(requestMessage.getDataInstance());
                }
                else if (requestMessage.getOperation() == EditDataInstanceRequest.Operation.Modify)
                {
                    /*
                    long start = System.currentTimeMillis();
                    DataInstance instance = requestMessage.getDataInstance();
                    String newName = requestMessage.getName();
                    instance = DataInstanceUtils.modifyInstance(instance, newName, requestMessage.getUser(), database);
                    long end = System.currentTimeMillis();
                    _logger.info("Data instance modified in " + (end - start) + " msecs");

                    event.setEventType(EventType.NameChange);
                    event.setDataInstance(requestMessage.getDataInstance());
                    
                    dtr.setDataInstance(instance);
                    */
                    throw new Exception("Modify request not implemented");
                }
                else if (requestMessage.getOperation() == EditDataInstanceRequest.Operation.AddFieldData)
                {
                    long start = System.currentTimeMillis();
                    instance = FieldDataUtils.addFieldData(requestMessage.getDataInstance(),
                                            requestMessage.getFieldData(), requestMessage.getUser(), database);
                    long end = System.currentTimeMillis();
                    _logger.info("Data instance properties added in " + (end - start) + " msecs");
    
                    event.setEventType(EventType.Modify);
                    event.setDataInstance(instance);
                }
                else if (requestMessage.getOperation() == EditDataInstanceRequest.Operation.DeleteFieldData)
                {
                    long start = System.currentTimeMillis();
                    instance = FieldDataUtils.removeFieldData(requestMessage.getDataInstance(),
                                            requestMessage.getFieldData(), requestMessage.getUser(), database);
                    long end = System.currentTimeMillis();
                    _logger.info("Data instance properties removed in " + (end - start) + " msecs");
    
                    event.setEventType(EventType.Modify);
                    event.setDataInstance(instance);
                }
                else if (requestMessage.getOperation() == EditDataInstanceRequest.Operation.ModifyFieldData)
                {
                    long start = System.currentTimeMillis();
                    instance = FieldDataUtils.modifyFieldData(requestMessage.getDataInstance(),
                                            requestMessage.getFieldData(), requestMessage.getUser(), database);
                    long end = System.currentTimeMillis();
                    _logger.info("Data instance properties modified in " + (end - start) + " msecs");
    
                    event.setEventType(EventType.Modify);
                    event.setDataInstance(instance);
                }
                else if (requestMessage.getOperation() == EditDataInstanceRequest.Operation.UpdatePrimaryData)
                {
                    long start = System.currentTimeMillis();
                    instance = PrimaryDataUtils.updatePrimaryData(requestMessage.getDataInstance(),
                            requestMessage.getUser(), database);
                    long end = System.currentTimeMillis();
                    _logger.info("Data instance primary data modified in " + (end - start) + " msecs");

                    event.setEventType(EventType.Modify);
                    event.setDataInstance(instance);
                }
                else if (requestMessage.getOperation() == EditDataInstanceRequest.Operation.UpdateTypes)
                {
                    long start = System.currentTimeMillis();
                    instance = InstanceShapeUtils.updateShapes(requestMessage.getDataInstance(),
                            requestMessage.getUser(), database);
                    long end = System.currentTimeMillis();
                    _logger.info("Data instance type data modified in " + (end - start) + " msecs");

                    event.setEventType(EventType.Modify);
                    event.setDataInstance(instance);
                    dtr.setDataInstance(instance);
                }
                else if (requestMessage.getOperation() == EditDataInstanceRequest.Operation.ModifyScopes)
                {
                    long start = System.currentTimeMillis();
                    instance = ScopeUtils.modifyScopes(requestMessage.getDataInstance().getId(),
                            requestMessage.getUser(), requestMessage.getScopes(), database);
                    long end = System.currentTimeMillis();
                    _logger.info("Data instance scopes modified in " + (end - start) + " msecs");

                    event.setEventType(EventType.Modify);
                    event.setDataInstance(instance);
                    dtr.setDataInstance(instance);
                }

                // TODO stop calling this method and instead make every kind of update make these updated header changes directly
                instance = DataInstanceUtils.setUpdated(instance, user, database);
                dtr.setDataInstance(instance);

                if (instance instanceof Tag) {
                    CacheManager.instance().put((Tag)instance);
                } else {
                    CacheManager.instance().put(instance);
                }

                // if the modified instance is the active user, the cached user session needs to be updated.
                if (instance instanceof User) {
                    User updatedUser = (User)instance;
                    if (updatedUser.getId().equals(user.getId())) {
                        updatedUser.setSessionToken(user.getSessionToken());
                        CacheManager.instance().putUserSession(updatedUser);
                    }
                }

                // remove this instance from the cache.  this is kind of lazy.  overall we should
                // be reworking all writes to update the cache with the new data instead of just
                // trashing it.
                //CacheManager.instance().remove(dtr.getDataInstance());

                event.setUser(user);
                _topicSender.send(new ServiceBusMessageProducer(event), getPayloadType());
            }
            catch (SessionValidationException sve)
            {
                ErrorMessage em = new ErrorMessage();
                em.setRequest(requestMessage);
                em.setMessage(sve.getMessage());
                em.setType(ErrorMessage.Type.SessionExpiration);
                response = em;
            }
            catch (StaleDataEditException sve)
            {
                ErrorMessage em = new ErrorMessage();
                em.setRequest(requestMessage);
                em.setMessage(sve.getMessage());
                em.setType(ErrorMessage.Type.StaleDataEdit);
                response = em;
            }
            catch (LockedDataEditException sve)
            {
                ErrorMessage em = new ErrorMessage();
                em.setRequest(requestMessage);
                em.setMessage(sve.getMessage());
                em.setType(ErrorMessage.Type.LockedDataEdit);
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
                    em.setMessage(e.getMessage());
                    response = em;
                }
            }
        }

        _pool.returnInstance(database, this);
        
        return response;
    }
    
}
