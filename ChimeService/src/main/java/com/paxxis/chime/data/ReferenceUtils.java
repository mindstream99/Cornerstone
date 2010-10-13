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

import com.paxxis.chime.client.common.BackReferencingDataInstance;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataSocialContext;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.Tag;
import com.paxxis.chime.client.common.TagContext;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.database.DatabaseConnection;
import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class ReferenceUtils {
    private ReferenceUtils() {
    }

    /**
     * The name of any referenced instance may have been changed and so we need to run through
     * them and get their names again.
     *
     * @param instance
     */
    static void updateReferences(DataInstance instance, User user, DatabaseConnection database) throws Exception {

        // the applied data types
        List<Shape> shapes = ShapeUtils.getInstanceShapes(instance.getId(), database);
        instance.setShapes(shapes, false);

        for (Shape type : shapes) {
           List<DataField> fields = type.getFields();
           for (DataField field : fields) {
               List<DataFieldValue> values = instance.getFieldValues(type, field);
               for (DataFieldValue value : values) {
                   if (!value.isInternal()) {
                       value.setValue(DataInstanceUtils.getInstance(value.getReferenceId(), user, database, false, true).getName());
                   }
               }
           }
        }

        DataSocialContext context = instance.getSocialContext();
        if (context != null) {
            List<TagContext> tagContexts = context.getTagContexts();
            for (TagContext tagContext : tagContexts) {
                Tag tag = tagContext.getTag();
                tag.setName(DataInstanceUtils.getInstance(tag.getId(), user, database, false, true).getName());
            }
        }

        if (instance instanceof BackReferencingDataInstance) {
            BackReferencingDataInstance inst = (BackReferencingDataInstance)instance;
            InstanceId brId = inst.getBackRefId();
            inst.setBackRefName(DataInstanceUtils.getInstance(brId, user, database, false, true).getName());
        }

        List<DataInstance> images = AttachmentUtils.getImages(instance.getId(), database);
        instance.setImages(images);

        List<DataInstance> files = AttachmentUtils.getFiles(instance.getId(), database);
        instance.setFiles(files);
    }

}
