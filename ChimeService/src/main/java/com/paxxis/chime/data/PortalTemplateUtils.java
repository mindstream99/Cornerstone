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
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.portal.PortalTemplate;
import com.paxxis.chime.service.PortalTemplateHelper;
import com.paxxis.cornerstone.base.InstanceId;
import com.paxxis.cornerstone.database.DataSet;
import com.paxxis.cornerstone.database.DatabaseConnection;
import com.paxxis.cornerstone.database.IDataValue;

import java.util.HashMap;

/**
 *
 * @author Robert Englander
 */
public class PortalTemplateUtils 
{
    private static HashMap<InstanceId, PortalTemplate> localShapeTemplates = new HashMap<InstanceId, PortalTemplate>();
    private static HashMap<InstanceId, PortalTemplate> localInstanceTemplates = new HashMap<InstanceId, PortalTemplate>();

    private PortalTemplateUtils()
    {}

    public static void addLocalShapeTemplate(String shapeId, PortalTemplate template) {
        localShapeTemplates.put(InstanceId.create(shapeId), template);
    }

    public static void addLocalInstanceTemplate(String id, PortalTemplate template) {
        localInstanceTemplates.put(InstanceId.create(id), template);
    }

    public static PortalTemplate getTemplate(DataInstance instance, DatabaseConnection database) throws Exception
    {
        PortalTemplate template = localInstanceTemplates.get(instance.getId());
        if (template == null) {
            Shape type = instance.getShapes().get(0);
            template = localShapeTemplates.get(type.getId());
            InstanceId zero = InstanceId.create("0");
            if (template == null)
            {
                template = loadTemplate(type.getId(), zero, database);
                if (template == null) {
                    template = loadTemplate(zero, zero, database);
                }
            } else {
                CacheManager.instance().putPortalTemplate(type.getId(), zero, template);
            }
        }
        
        return template;
    }
    
    private static PortalTemplate loadTemplate(InstanceId typeId, InstanceId instanceId, DatabaseConnection database) throws Exception
    {
        PortalTemplate template = CacheManager.instance().getPortalTemplate(typeId, instanceId);
        if (template != null)
        {
            return template;
        }

        String sql = "SELECT id, template FROM Chime.Template where type_id = '" + typeId + "' and instance_id = '" + instanceId + "'";
        
        DataSet dataSet = database.getDataSet(sql, true);
        boolean hasTemplate = dataSet.next();
        if (hasTemplate)
        {
            IDataValue id = dataSet.getFieldValue("id");
            IDataValue text = dataSet.getFieldValue("template");
            
            PortalTemplateHelper helper = new PortalTemplateHelper();
            template = helper.convert(text.asString());
            template.setId(id.asString());
            
            CacheManager.instance().putPortalTemplate(typeId, instanceId, template);
        }

        dataSet.close();
        
        return template;
    }
}
