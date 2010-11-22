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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class DataInstanceResponse extends ResponseMessage<DataInstanceRequest> {
	private static final long serialVersionUID = 1L;
    public enum Reason {
        NoSuchData,
        NotVisible,
        NoReason
    }
    
    private final static int VERSION = 1;

    @Override
    public int getMessageType() {
        return messageType();
    }

    public static int messageType() {
        return MessageConstants.DATAINSTANCERESPONSE;
    }

    @Override
    public int getMessageVersion() {
        return messageVersion();
    }

    public static int messageVersion() {
        return VERSION;
    }

    
    // the resulting data instance objects
    List<DataInstance> _dataInstances = new ArrayList<DataInstance>();
    
    Cursor _cursor = null;

    private Reason _reason = Reason.NoReason;

    public void setReason(Reason reason) {
        _reason = reason;
    }

    public Reason getReason() {
        return _reason;
    }

    public void setCursor(Cursor cursor)
    {
        _cursor = cursor;
    }
    
    public Cursor getCursor()
    {
        return _cursor;
    }
    
    public List<DataInstance> getDataInstances()
    {
        return _dataInstances;
    }
    
    public void addDataInstance(DataInstance instance)
    {
        _dataInstances.add(instance);
    }
    
    public void setDataInstances(List<DataInstance> instances)
    {
        _dataInstances.clear();
        _dataInstances.addAll(instances);
    }
}

