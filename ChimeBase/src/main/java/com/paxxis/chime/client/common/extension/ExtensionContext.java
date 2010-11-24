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

package com.paxxis.chime.client.common.extension;

import com.paxxis.chime.client.common.Community;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceRequest;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.User;
import com.paxxis.cornerstone.base.InstanceId;
import com.paxxis.cornerstone.base.Message;

import java.util.List;

/**
 *
 * @author Robert Englander
 */
public interface ExtensionContext {

    public enum LogLevel {
        INFO,
        WARN,
        ERROR,
        DEBUG
    }

    /**
     * Creates an in memory indexer for use by the extension.
     *
     */
    public MemoryIndexer createMemoryIndexer();

    /**
     * Retrieves a shape instance.
     * @param id the id of the shape to retrieve
    */
    public Shape getShapeById(InstanceId id);

    /**
     * Retrieves a data instance.
     * @param id the id of the instance to retrieve
     * @param user the user requesting the data
     */
    public DataInstance getInstanceById(InstanceId id, User user);

    /**
     * Creates a data instance.  This does not store the instance.  That is up to the caller to
     * do separately.
     * 
     * @param extId the id of the extension making the request.
     * @param shapeId the id of the shape to apply
     * @param name the name of the new data
     * @param desc the description for the new data
     * @param user the user creating the data
     * @param community the community that has visibility of the data
     */
    public DataInstance createDataInstance(String extId, InstanceId shapeId, String name, String desc,
            User user, Community community);

    /**
     * Publishes and Event instance.  The event instance is created and stored in the Chime primary data
     * store.
     * 
     * @param name the name of the event
     * @param desc the description for the event
     * @param eventType the event type
     * @param related a list of related data instances.
     * @param summary a summary description of the event
     * @param user the user creating the event
     * @param community the community that has visibility
     */
    public DataInstance publishEvent(String name, String desc, DataInstance eventType, List<DataInstance> related,
            String summary, User user, Community community);

    /**
     * Publishes an update event indicating that a data instance has been changed
     * @param instance the changed instance.
     */
    public void publishUpdate(DataInstance instance);

    /**
     * Logs a message into the ChimeService log.
     *
     * @param level the log level
     * @param message the message to log
     */
    public void log(LogLevel level, String className, String message);

    /**
     * Processes a DataInstanceRequest and returns the resulting message.
     * 
     * @param request The request to process
     * @return Either a DataInstanceResponse, or an ErrorMessage
     */
    public Message processRequest(DataInstanceRequest request);

}







