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

import com.paxxis.cornerstone.base.Cursor;
import com.paxxis.cornerstone.base.RequestMessage;

/**
 *
 * @author Robert Englander
 */
public class FindInstancesRequest extends RequestMessage {
	private static final long serialVersionUID = 1L;
    private final static int VERSION = 1;

    @Override
    public int getMessageType() {
        return messageType();
    }

    public static int messageType() {
        return MessageConstants.FINDINSTANCESREQUEST;
    }

    @Override
    public int getMessageVersion() {
        return messageVersion();
    }

    public static int messageVersion() {
        return VERSION;
    }

    
    // if _shape is Tag, the appliedType can be specified.  This instructs the
    // service to give back only instances of Tag that have been applied to
    // _appliedShape and meet the other criteria
    Shape _shape = null;
    Shape _appliedShape = null;
    
    // the data type name can be given instead of the type.  if both are given,
    // the _shape will be used
    String _shapeName = null;
    
    // if the type is Shape, the excludeInternals flag can be used as well
    boolean _excludeInternals = false;

    boolean directCreatable = false;
    
    User _user = null;
    
    String _string = null;
    Cursor _cursor = null;
    boolean userCreated = false;


    public void setDirectCreatable(boolean val) {
        directCreatable = val;
    }

    public boolean getDirectCreatable() {
        return directCreatable;
    }

    public void setUserCreated(boolean val) {
        userCreated = val;
    }

    public boolean getUserCreated() {
        return userCreated;
    }

    public void setUser(User user)
    {
        _user = user;
    }
    
    public User getUser()
    {
        return _user;
    }
    
    public void setCursor(Cursor cursor)
    {
        _cursor = cursor;
    }
    
    public Cursor getCursor()
    {
        return _cursor;
    }
    
    public void setExcludeInternals(boolean exclude)
    {
        _excludeInternals = exclude;
    }
    
    public boolean getExcludeInternals()
    {
        return _excludeInternals;
    }
    
    public void setShapeName(String shapeName)
    {
        _shapeName = shapeName;
    }
    
    public String getShapeName()
    {
        return _shapeName;
    }
    
    public void setShape(Shape type)
    {
        _shape = type;
    }
    
    public Shape getShape()
    {
        return _shape;
    }

    public void setAppliedShape(Shape shape)
    {
        _appliedShape = shape;
    }
    
    public Shape getAppliedShape()
    {
        return _appliedShape;
    }
    
    public void setString(String string)
    {
        _string = string;
    }
    
    public String getString()
    {
        return _string;
    }
}
