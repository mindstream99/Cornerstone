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

/**
 *
 * @author Robert Englander
 */
public class Utils {
    
    private Utils() {
    }

    public static float round(float Rval, int Rpl) {
        float p = (float)Math.pow(10,Rpl);
        Rval = Rval * p;
        float tmp = Math.round(Rval);
        return (float)tmp/p;
    }
    
    /*
    public static String encode(String source) {
        byte[] bytes = source.getBytes();
        StringBuffer buffer = new StringBuffer();
        for (byte b : bytes) {
            int i = ((int)b * 3) / 2;
            String hex = Integer.toHexString(i).toUpperCase();
            buffer.append(hex);
        }
        
        return buffer.toString();
    }
    
    public static String decode(String source) {
        byte[] bytes = new byte[source.length() / 2];
        for (int i = 0, j = 0; i < source.length(); i += 2, j++) {
            String hex = source.substring(i, i + 2);
            byte b = (byte)((2 * Integer.parseInt(hex, 16)) / 3);
            bytes[j] = b;
        }

        return new String(bytes);
    }
    */
}
