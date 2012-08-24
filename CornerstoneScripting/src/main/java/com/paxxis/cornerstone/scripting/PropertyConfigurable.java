package com.paxxis.cornerstone.scripting;

import java.util.List;

public interface PropertyConfigurable {
    public void setValues(String propName, List<Object> values);
    public void setValue(String propName, Object value);
}
