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

package com.paxxis.chime.data;

import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.cornerstone.base.Cursor;

import java.util.List;

/**
 * A combination of a list of data instances and a cursor.
 * 
 * @author Robert Englander
 */
public class InstancesBundle {
    
    private List<? extends DataInstance> instances;
    private Cursor cursor;

    public InstancesBundle(List<? extends DataInstance> instances, Cursor cursor) {
        this.instances = instances;
        this.cursor = cursor;
    }

    public List<? extends DataInstance> getInstances() {
        return instances;
    }

    public Cursor getCursor() {
        return cursor;
    }
}
