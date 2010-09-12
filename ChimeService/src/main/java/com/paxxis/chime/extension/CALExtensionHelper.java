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

package com.paxxis.chime.extension;

import com.paxxis.chime.client.common.cal.Array;
import com.paxxis.chime.client.common.cal.ExtensionHelper;
import com.paxxis.chime.client.common.cal.IValue;
import com.paxxis.chime.client.common.cal.StringVariable;
import com.paxxis.chime.client.common.extension.CALExtension;
import com.paxxis.chime.client.common.extension.ChimeExtension;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Robert Englander
 */
public class CALExtensionHelper implements ExtensionHelper {

    private HashMap<String, Method> calMethods = new HashMap<String, Method>();
    private CALExtension calExtension = null;
    private ChimeExtension chimeExtension = null;
    
    public void initialize(ChimeExtension chime, CALExtension ext) {
        try {
            chimeExtension = chime;
            calExtension = ext;
            String calClassName = chimeExtension.getCalClassName();
            Class clazz = Class.forName(calClassName);
            Method[] list = clazz.getMethods();
            for (Method m : list) {
                calMethods.put(m.getName(), m);
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(CALExtensionHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean methodHasReturn(String name)
    {
        boolean result = false;
        Method m = calMethods.get(name);
        if (m != null) {
            result = !m.getReturnType().getName().equals("void");
        }

        return result;
    }

    public int getMethodParameterCount(String name)
    {
        int result = -1;
        Method m = calMethods.get(name);
        if (m != null) {
            result = m.getParameterTypes().length;
        }

        if (result == -1) {
            throw new RuntimeException("Extension " + chimeExtension.getId() + " has no such method: " + name);
        }

        return result;
    }

    public IValue executeMethod(String name, List<IValue> params) {
        Method m = calMethods.get(name);
        Object[] paramArray = new Object[params.size()];
        for (int i = 0; i < params.size(); i++) {
            IValue param = params.get(i);
            if (m.getParameterTypes()[i].getName().equals("java.lang.String")) {
                paramArray[i] = param.valueAsString();
            } else { // assume numbers for now
                paramArray[i] = param.valueAsDouble();
            }
        }

        try {
            Object result = m.invoke(calExtension, paramArray);
            if (result instanceof List) {
                List list = (List)result;
                List<IValue> ivals = new ArrayList<IValue>();
                for (Object o : list) {
                    StringVariable var = new StringVariable(null, o.toString());
                    ivals.add(var);
                }

                Array returnArray = new Array(null);
                returnArray.initialize(ivals);
                return returnArray;
            } else {
                return new StringVariable(null, result.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException("Extension " + chimeExtension.getId() + " invocation failure: " + e.getCause().getLocalizedMessage());
        }
    }
}
