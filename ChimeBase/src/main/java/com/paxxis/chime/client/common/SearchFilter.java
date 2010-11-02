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

import com.paxxis.chime.client.common.DataInstanceRequest.Operator;

public class SearchFilter implements Serializable {
	private static final long serialVersionUID = 1L;

	private Shape dataShape = null;
    private String _displayValue = null;
    private DataField _field = null;
    private DataField _subField = null;
    private Operator _operator = null;
    private Serializable _value = null;
    private boolean _enabled = true;
    
    public SearchFilter()
    {
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

        SearchFilter inst = (SearchFilter)other;

        if (hashCode() != inst.hashCode()) {
            return false;
        }

        if (dataShape == null) {
            if (inst.dataShape != null) {
                return false;
            }
        } else {
            if (!dataShape.equals(inst.dataShape)) {
                return false;
            }
        }

        if (_displayValue == null) {
            if (inst._displayValue != null) {
                return false;
            }
        } else {
            if (!_displayValue.equals(inst._displayValue)) {
                return false;
            }
        }

        if (_field == null) {
            if (inst._field != null) {
                return false;
            }
        } else {
            if (!_field.equals(inst._field)) {
                return false;
            }
        }

        if (_subField == null) {
            if (inst._subField != null) {
                return false;
            }
        } else {
            if (!_subField.equals(inst._subField)) {
                return false;
            }
        }

        if (_operator == null) {
            if (inst._operator != null) {
                return false;
            }
        } else {
            if (!_operator.equals(inst._operator)) {
                return false;
            }
        }

        if (_value == null) {
            if (inst._value != null) {
                return false;
            }
        } else {
            if (!_value.equals(inst._value)) {
                return false;
            }
        }

        if (_enabled != inst._enabled) {
            return false;
        }
        
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (this.dataShape != null ? this.dataShape.hashCode() : 0);
        hash = 37 * hash + (this._field != null ? this._field.hashCode() : 0);
        hash = 37 * hash + (this._subField != null ? this._subField.hashCode() : 0);
        hash = 37 * hash + (this._operator != null ? this._operator.hashCode() : 0);
        hash = 37 * hash + (this._value != null ? this._value.hashCode() : 0);
        return hash;
    }

    public SearchFilter copy() {
        SearchFilter filter = new SearchFilter();
        filter.setEnabled(isEnabled());
        filter.setDataField(getDataField());
        filter.setSubField(getSubField());
        filter.setOperator(getOperator());
        filter.setValue(getValue(), getDisplayValue());
        filter.setDataShape(getDataShape());
        return filter;
    }
    
    public boolean isValid()
    {
        if (_field != null && _operator != null && _value != null)
        {
            return true;
        }
        else if (_field != null && _operator != null && _value == null)
        {
            if (_operator == Operator.Past24Hours ||
                _operator == Operator.Past3Days ||
                _operator == Operator.Past7Days ||
                _operator == Operator.Past30Days)
            {
                return true;
            }
        }
        
        return false;
    }
    
    public void clear()
    {
        _field = null;
        _operator = null;
        _value = null;
        _displayValue = null;
    }

    public void setEnabled(boolean enabled)
    {
        _enabled = enabled;
    }
    
    public boolean isEnabled()
    {
        return _enabled;
    }

    public void setDataShape(Shape shape) {
        dataShape = shape;
    }

    public Shape getDataShape() {
        return dataShape;
    }

    public void setDataField(DataField field)
    {
        _field = field;
    }

    public DataField getDataField()
    {
        return _field;
    }
    
    public void setSubField(DataField field)
    {
        _subField = field;
    }
    
    public DataField getSubField()
    {
        return _subField;
    }
    
    public void setOperator(Operator operator)
    {
        _operator = operator;
    }
    
    public Operator getOperator() 
    {
        return _operator;
    }

    public void setValue(Serializable value)
    {
        setValue(value, value.toString());
    }
    
    public void setValue(Serializable value, String displayValue)
    {
        _value = value;
        _displayValue = displayValue;
    }
    
    public Serializable getValue() 
    {
        return _value;
    }

    public String getDisplayValue()
    {
        return _displayValue;
    }
}
