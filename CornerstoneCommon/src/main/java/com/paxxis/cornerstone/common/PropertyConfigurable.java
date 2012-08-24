package com.paxxis.cornerstone.common;

import java.util.List;

public interface PropertyConfigurable {
    public void setValues(String propName, List<String> values);
    public void setValue(String propName, String value);
}
