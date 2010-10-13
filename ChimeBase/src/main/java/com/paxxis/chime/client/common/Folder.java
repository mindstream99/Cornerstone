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

/**
 *
 * @author Robert Englander
 */
public class Folder extends DataInstance {
    private List<DataInstance> children = new ArrayList<DataInstance>();

    public void setChildren(List<DataInstance> list) {
        children.clear();
        children.addAll(list);
    }

    public List<DataInstance> getChildren() {
        return children;
    }

    public List<DataInstance> getChildrenReferences() {
        Shape type = getShapes().get(0);
        DataField field = type.getField("Data Items");
        List<DataFieldValue> values = getFieldValues(type, field);

        List<DataInstance> result = new ArrayList<DataInstance>();

        for (DataFieldValue value : values) {
            DataInstance favorite = new DataInstance();
            favorite.setId(value.getReferenceId());
            favorite.addShape(field.getShape());
            favorite.setName(value.getValue().toString());

            result.add(favorite);
        }

        return result;
    }
}
