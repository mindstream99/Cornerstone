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
public class DataField  implements Serializable {
    String _id = "-1";
    String _name = null;
    String _description = null;
    boolean _private = false;
    boolean userEditable = true;
    Shape shape = null;
    int _column = -1;
    int _maxValues = 0;

    public DataField()
    {
        
    }
    
    public DataField copy() {
        DataField copy = new DataField();
        copy.setColumn(getColumn());
        copy.setShape(getShape());
        copy.setDescription(getDescription());
        copy.setId(getId());
        copy.setMaxValues(getMaxValues());
        copy.setName(getName());
        copy.setUserEditable(isUserEditable());
        return copy;
    }
    
    @Override
    public boolean equals(Object other)
    {

        if (this == other) {
            return true;
        }

        if (other == null) {
            return false;
        }

        if (other.getClass() != getClass()) {
            return false;
        }

        DataField inst = (DataField)other;

        if (hashCode() != inst.hashCode()) {
            return false;
        }

        boolean equiv = getName().equals(inst.getName()) &&
                        getDescription().equals(inst.getDescription()) &&
                        getShape().getId().equals(inst.getShape().getId()) &&
                        getMaxValues() == inst.getMaxValues();
        
        return equiv;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + (this._id != null ? this._id.hashCode() : 0);
        hash = 89 * hash + (this.shape != null ? this.shape.hashCode() : 0);
        hash = 89 * hash + this._column;
        return hash;
    }
    
    public boolean isValid()
    {
        return getName().length() > 0 &&
                  getDescription().length() > 0 &&
                  getShape() != null;
    }
    
    public void setColumn(int col)
    {
        _column = col;
    }
    
    public int getColumn()
    {
        return _column;
    }

    public static DataField createInstance(Object source)
    {
        if (source instanceof DataField)
        {
            return (DataField)source;
        }
        //else if (source instanceof String)
        //{
        //    return createFromJSON((String)source);
        //}
        
        throw new RuntimeException("something something");
    }
    
    public void setId(String id)
    {
        _id = id;
    }
    
    public String getId()
    {
        return _id;
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

    public int getMaxValues()
    {
        return _maxValues;
    }

    public void setMaxValues(int maxValues)
    {
        _maxValues = maxValues;
    }

    public boolean isPrivate() 
    {
        return _private;
    }

    public void setPrivate(boolean val) 
    {
        _private = val;
    }

    public void setUserEditable(boolean val) {
        userEditable = val;
    }

    public boolean isUserEditable() {
        return userEditable;
    }
    
    public Shape getShape()
    {
        return shape;
    }
    
    public void setShape(Shape s)
    {
        shape = s;
    }
}
