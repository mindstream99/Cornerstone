package com.paxxis.cornerstone.scripting;

import java.lang.reflect.Method;
import java.util.List;

public class PropertySetter implements PropertyConfigurable {

    private Object target;
    
    private enum PropertyType {
	LIST("add"),
	SINGLE("set");
	
	private String prefix;
	PropertyType(String prefix) {
	    this.prefix = prefix;
	}
	
	String getPrefix() {
	    return prefix;
	}
    }

    public PropertySetter(Object target) {
	this.target = target;
    }
    
    public void setValues(String propName, List<Object> values) {
	for (Object value : values) {
	    setValue(PropertyType.LIST, propName, value);
	}
    }
    
    public void setValue(String propName, Object value) {
	setValue(PropertyType.SINGLE, propName, value);
    }
    
    private void setValue(PropertyType propType, String propName, Object value) {
        Method[] methods = target.getClass().getMethods();
        
        // get the setter
        String firstLetter = propName.substring(0, 1).toUpperCase();
        String setterName = propType.getPrefix() + firstLetter + propName.substring(1);

        boolean foundSetter = false;
        for (Method method : methods) {
            if (!method.getName().equals(setterName)) {
                continue;
            }
            Class<?>[] paramClasses = method.getParameterTypes();
            if (paramClasses.length == 1) {
                // this is the one we want, so convert the value to this type
                try {
                    Object objValue = convert(paramClasses[0], value);
                    method.invoke(target, objValue);
                    foundSetter = true;
                } catch (Exception e) {
                    throw new RuntimeException("Can't convert value '" + value.toString() + "' to " + 
                	    paramClasses[0].getSimpleName() + " for property named '" + propName + "'. ");
                }

                break;
            }
        }
        
        if (!foundSetter) {
            throw new RuntimeException("No validation property named " + propName);
        }
    }
    
    @SuppressWarnings("rawtypes")
    private Object convert(Class cls, Object value) {
        if (value.getClass().isAssignableFrom(cls)) {
            return value;
        }
        
        Object objValue = null;
        if (cls.getName().equals("java.lang.String")) {
            objValue = String.valueOf(value);
        } else if (cls.getName().equals("int")) {
            objValue = Integer.valueOf(value.toString());
        } else if (cls.getName().equals("long")) {
            objValue = Long.valueOf(value.toString());
        } else if (cls.getName().equals("float")) {
            objValue = Float.valueOf(value.toString());
        } else if (cls.getName().equals("double")) {
            objValue = Double.valueOf(value.toString());
        } else if (cls.getName().equals("boolean")) {
            String val = value.toString().toLowerCase();
            if (!(val.equals("true") || val.equals("false"))) {
        	throw new RuntimeException("bad input:" + val);
            }
            objValue = Boolean.valueOf(val);
        } else if (cls.getName().equals("java.util.List")) {
        	objValue = value;                                    
        } else {
            //this covers any class (Enums most importantly) that has
            //a static valueOf(java.lang.String) method
            try {
                @SuppressWarnings("unchecked")
                Method valueOf = cls.getMethod(
                        "valueOf", 
                        String.class);
                objValue = valueOf.invoke(null, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        return objValue;
    }
    
}
