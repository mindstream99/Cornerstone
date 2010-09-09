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

package com.paxxis.chime.client.common;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Robert Englander
 */
public class DataFieldValue implements Serializable
{
    private InstanceId id = InstanceId.create("-1");
    private InstanceId shapeId = InstanceId.create("-1");
    private InstanceId referenceId = InstanceId.create("-1");
    private String value;
    private boolean isInternal;
    private Date timestamp = null;

    public DataFieldValue()
    {
        
    }
    
    public DataFieldValue(InstanceId refId, String name, InstanceId sId, InstanceId id, Date ts)
    {
        referenceId = refId;
        value = name;
        shapeId = sId;
        this.id = id;
        timestamp = ts;
        isInternal = false;
    }
    
    public DataFieldValue(String name, InstanceId sId, InstanceId id, Date ts)
    {
        value = name;
        shapeId = sId;
        this.id = id;
        timestamp = ts;
        isInternal = true;
    }
    
    public DataFieldValue(DataFieldValue source)
    {
        isInternal = source.isInternal();
        id = source.getId();
        shapeId = source.getShapeId();
        referenceId = source.getReferenceId();
        value = source.getName();
    }
    
    public boolean isInternal()
    {
        return isInternal;
    }
    
    public void setReferenceId(InstanceId id)
    {
        referenceId = id;
    }
    
    public void setName(String name)
    {
        value = name;
    }
    
    public String getName()
    {
        return value;
    }
    
    public InstanceId getReferenceId()
    {
        return referenceId;
    }
    
    public void setShapeId(InstanceId id)
    {
        shapeId = id;
    }
    
    public InstanceId getShapeId()
    {
        return shapeId;
    }
    
    public void setId(InstanceId id)
    {
        this.id = id;
    }
    
    public InstanceId getId()
    {
        return id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

}
