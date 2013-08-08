package com.paxxis.cornerstone.common;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigurationUtils {
    private ConfigurationUtils() {
    }

    @SuppressWarnings("rawtypes")
    public static Object convert(Class cls, Object value, Method method) {
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
            objValue = Boolean.valueOf(value.toString());
        } else if (cls.getName().equals("java.util.List")) {
            objValue = convertToList(value, method.getAnnotation(CollectionProperty.class));
        } else if (cls.getName().equals("java.util.Collection")) {
            objValue = convertToList(value, method.getAnnotation(CollectionProperty.class));
        } else if (cls.getName().equals("java.util.Map")) { 
            objValue = convertToMap(value, method.getAnnotation(MapProperty.class));
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

    @SuppressWarnings("rawtypes")
    public static Map<?,?> convertToMap(Object value, MapProperty annotation) {
        Class<?> keyType = String.class;
        Class<?> valueType = String.class;
        
        if (annotation != null) {
            keyType = annotation.keyType();
            valueType = annotation.valueType();
        }
        
        Map<Object, Object> map = new HashMap<Object, Object>();
        
        // strip out newlines
        value = value.toString().replace('\n',' ');
        value = value.toString().replace('\r',' ');

        // replace all of the slash-comma entries with a \r
        value = value.toString().replace("\\,", "\r");
        String[] entries = value.toString().trim().split(",");
        for (String entry : entries) {
            // put back the commas
            entry = entry.trim().replace("\r", ",");
            
            // replace all of the slash-equals sign with \r
            entry = entry.trim().replace("\\=", "\r");
            String[] pairs = entry.trim().split("=");
            
            // put back the equals signs
            Object key = convert(keyType, pairs[0].replace("\r", "=").trim(), null);
            Object val = convert(valueType, pairs[1].replace("\r", "=").trim(), null);
            map.put(key, val);
        }
        
        return map;
    }
    
    @SuppressWarnings("rawtypes")
    public static List<?> convertToList(Object value, CollectionProperty annotation) {
        Class<?> valueType = String.class;

        if (annotation != null) {
            valueType = annotation.valueType();
        }
        List<Object> list = new ArrayList<Object>();
        
        // strip out newlines
        value = value.toString().replace('\n',' ');
        value = value.toString().replace('\r',' ');

        // replace all of the slash-comma entries with a \r
        value = value.toString().replace("\\,", "\r");
        String[] entries = value.toString().trim().split(",");
        for (String entry : entries) {
            // put back the commas
            entry = entry.trim().replace("\r", ",");

            Object val = convert(valueType, entry.trim(), null);
            list.add(val);
        }
        
        return list;
    }
}
