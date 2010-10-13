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

package com.paxxis.chime.client.common.cal;

import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.extension.ChimeExtension;
import java.util.List;

/**
 * This interface is implemented by services or applications that provide query
 * support to the CAL query object.  CAL objects do not know anything about the
 * environment that they run in.
 * @author Robert Englander
 */
public interface QueryProvider {
    public List<String> getInstances(List<QueryParameter> params);
    public List<DataInstance> getDataInstances(List<QueryParameter> params);
    
    public double getStockPrice(String symbol);

    public DataInstance getDataInstanceById(InstanceId id);
    public Shape getShapeById(InstanceId id);
    public ChimeExtension getExtension(String id);
    public ExtensionHelper createExtensionHelper();
}
