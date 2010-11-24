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

import java.util.ArrayList;
import java.util.List;

import com.paxxis.cornerstone.base.InstanceId;

/** 
 *
 * @author Robert Englander
 */
public class Community extends DataInstance {
	private static final long serialVersionUID = 1L;

	public static final String FAVORITES_FIELD = "Favorites";

    public static final Community Global = new Community(InstanceId.create("100"));
    public static final Community ChimeAdministrators = new Community(InstanceId.create("10200"));

    static {
        Global.setName("Everyone");
    }

    public Community()
    {
        this(InstanceId.create("0"));
    }

    public Community(InstanceId id, String name) {
        super();
        setId(id);
        setName(name);
    }

    public Community(InstanceId id) {
        this(id, "");
    }

    @Override
    public Community copy() {
        return super.copy(new Community());
    }

    public List<DataInstance> getFavorites() {
        Shape type = getShapes().get(0);
        DataField field = type.getField(Community.FAVORITES_FIELD);
        List<DataFieldValue> values = getFieldValues(type, field);

        List<DataInstance> results = new ArrayList<DataInstance>();

        for (DataFieldValue value : values) {
            DataInstance instance = new DataInstance();
            instance.setId(value.getReferenceId());
            instance.addShape(field.getShape());
            instance.setName(value.getValue().toString());

            results.add(instance);
        }

        return results;
    }
}
