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
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.User;
import java.util.List;

/**
 *
 * @author Robert Englander
 */
public interface ExtensionContext {

    public MemoryIndexer createMemoryIndexer();
    public Shape getShapeById(InstanceId id);
    public DataInstance getInstanceById(InstanceId id, User user);
    public DataInstance createDataInstance(String extId, InstanceId shapeId, String name, String desc,
            User user, Community community);
    public DataInstance publishEvent(String name, String desc, DataInstance eventType, List<DataInstance> related,
            String summary, User user, Community community);
    public void publishUpdate(DataInstance instance);
}
