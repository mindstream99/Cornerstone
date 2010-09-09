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

/**
 *
 * @author Robert Englander
 */
public class ShapeTagContext implements Serializable
{
    public enum Type
    {
        User,
        Community
    }
    
    private Type _type = null;
    private Shape shape = null;
    private long _usageCount = 0;
    
    public ShapeTagContext() {
    }
    
    public ShapeTagContext(Type type, Shape s, long usageCount) {
        _type = type;
        shape = s;
        _usageCount = usageCount;
    }

    public ShapeTagContext copy() {
        ShapeTagContext result = new ShapeTagContext();
        result._type = _type;
        result._usageCount = _usageCount;
        if (shape != null) {
            result.shape = shape.copy();
        }
        return result;
    }

    @Override
    public boolean equals(Object other) {

        if (this == other) {
            return true;
        }

        if (other == null) {
            return false;
        }

        if (other.getClass() != getClass()) {
            return false;
        }

        ShapeTagContext inst = (ShapeTagContext)other;

        if (hashCode() != inst.hashCode()) {
            return false;
        }

        if (!super.equals(other)) {
            return false;
        }

        if (_type != inst._type) {
            return false;
        }

        if (_usageCount != inst._usageCount) {
            return false;
        }

        if (shape == null) {
            if (inst.shape != null) {
                return false;
            }
        } else {
            if (!shape.equals(inst.shape)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (this._type != null ? this._type.hashCode() : 0);
        hash = 41 * hash + (this.shape != null ? this.shape.hashCode() : 0);
        hash = 41 * hash + (int) (this._usageCount ^ (this._usageCount >>> 32));
        return hash;
    }

    public Type getType()
    {
        return _type;
    }
    
    public Shape getShape()
    {
        return shape;
    }
    
    public long getUsageCount()
    {
        return _usageCount;
    }
}
