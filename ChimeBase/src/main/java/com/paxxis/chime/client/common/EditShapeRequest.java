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
public class EditShapeRequest extends RequestMessage {
	private static final long serialVersionUID = 1L;

	public enum Operation
    {
        Create,
        AddFields,
        RemoveFields
    }
    
    private final static int VERSION = 1;

    @Override
    public MessageConstants.MessageType getMessageType() {
        return messageType();
    }

    public static MessageConstants.MessageType messageType() {
        return MessageConstants.MessageType.EditShapeRequest;
    }

    @Override
    public int getMessageVersion() {
        return messageVersion();
    }

    public static int messageVersion() {
        return VERSION;
    }


    private InstanceId id = null;
    private String _name = null;
    private String _description = null;
    private User _user = null;
    private List<FieldDefinition> _fieldDefs = new ArrayList<FieldDefinition>();
    private Operation _operation = Operation.Create;
    private List<Scope> _scopes = new ArrayList<Scope>();
    private boolean isTabular = false;
    
    public void addScope(Scope scope)
    {
        _scopes.add(scope);
    }

    public List<Scope> getScopes()
    {
        return _scopes;
    }

    public void setId(InstanceId id) {
        this.id = id;
    }

    public InstanceId getId() {
        return id;
    }

    public boolean hasId() {
        return (id != null);
    }
    
    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }
    
    public String getDescription()
    {
        return _description;
    }

    public void setDescription(String description)
    {
        _description = description;
    }
    
    public User getUser()
    {
        return _user;
    }
    
    public void setUser(User user)
    {
        _user = user;
    }

    public void addFieldDefinition(FieldDefinition def) {
        _fieldDefs.add(def);
    }

    public void addFieldDefinition(String name, String desc, String typeName)
    {
        FieldDefinition def = new FieldDefinition();
        def.name = name;
        def.description = desc;
        def.typeName = typeName;
        _fieldDefs.add(def);
    }
    
    public List<FieldDefinition> getFieldDefinitions()
    {
        return _fieldDefs;
    }
    
    public void setTabular(boolean tabular) {
    	isTabular = tabular;
    }
    
    public boolean isTabular() {
    	return isTabular;
    }
    
    public Operation getOperation()
    {
        return _operation;
    }
    
    public void setOperation(Operation op)
    {
        _operation = op;
    }
}
