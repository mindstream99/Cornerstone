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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class EditDataInstanceRequest extends RequestMessage {
    public enum Operation
    {
        Create,
        Delete,
        Modify,
        AddFieldData,
        DeleteFieldData,
        ModifyFieldData,
        UpdatePrimaryData,
        UpdateTypes,
        ModifyScopes
    }
    
    private final static int VERSION = 1;

    @Override
    public MessageConstants.MessageType getMessageType() {
        return messageType();
    }

    public static MessageConstants.MessageType messageType() {
        return MessageConstants.MessageType.EditDataInstanceRequest;
    }

    @Override
    public int getMessageVersion() {
        return messageVersion();
    }

    public static int messageVersion() {
        return VERSION;
    }

    
    private DataInstance _dataInstance = null;
    private String _name = null;
    private String _description = null;
    private List<Shape> _type = new ArrayList<Shape>();
    private String _typeName = null;
    private User _user = null;
    private Operation _operation = Operation.Create;
    private List<FieldData> _fieldData = new ArrayList<FieldData>();
    private List<Scope> _scopes = new ArrayList<Scope>();
    
    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }

    public void setDescription(String desc) {
        _description = desc;
    }

    public String getDescription() {
        return _description;
    }
    
    public void setDataInstance(DataInstance instance)
    {
        _dataInstance = instance;
    }
    
    public DataInstance getDataInstance()
    {
        return _dataInstance;
    }
    
    public List<Shape> getShapes()
    {
        return _type;
    }
    
    public void addShape(Shape type)
    {
        _type.add(type);
    }
    
    public String getShapeName()
    {
        return _typeName;
    }
    
    public void setShapeName(String typeName)
    {
        _typeName = typeName;
    }
    
    public User getUser()
    {
        return _user;
    }
    
    public void setUser(User user)
    {
        _user = user;
    }
    
    public Operation getOperation()
    {
        return _operation;
    }
    
    public void setOperation(Operation op)
    {
        _operation = op;
    }
    
    public void addScope(Scope scope)
    {
        _scopes.add(scope);
    }
    
    public List<Scope> getScopes()
    {
        return _scopes;
    }
    
    public List<FieldData> getFieldData()
    {
        return _fieldData;
    }

    /*
    public void setFieldData(Shape shape, DataField field, Serializable value)
    {
        if (value != null) {
            // clear out any field data for this field
            List<FieldData> fieldData = new ArrayList<FieldData>();
            for (FieldData fd : _fieldData)
            {
                if (fd.field != field)
                {
                    fieldData.add(fd);
                }
            }

            _fieldData.clear();
            _fieldData.addAll(fieldData);

            addFieldData(shape, field, value);
        }
    }
    */
    
    public void addFieldData(Shape type, DataField field, Serializable value)
    {
        if (value != null) {
            FieldData data = new FieldData();
            data.shape = type;
            data.field = field;
            data.value = value;
            _fieldData.add(data);
        } else {
            int x = 1;
        }
    }
}
