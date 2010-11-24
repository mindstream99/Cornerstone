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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataSocialContext;
import com.paxxis.chime.client.common.FieldData;
import com.paxxis.chime.client.common.FieldDefinition;
import com.paxxis.chime.client.common.Scope;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.ShapeTagContext;
import com.paxxis.chime.client.common.Tag;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.service.Tools;
import com.paxxis.cornerstone.base.InstanceId;
import com.paxxis.cornerstone.database.DataSet;
import com.paxxis.cornerstone.database.DatabaseConnection;
import com.paxxis.cornerstone.database.IDataValue;
import com.paxxis.cornerstone.database.StringData;

 
/** 
 * 
 * @author Robert Englander
 */
public class ShapeUtils {
	private static final String DEFAULT_MASK = "'NYYYYYYYYYYYN'";
	private static final String TABULAR_MASK = "'NYNNNNNNNNNNY'";

	private ShapeUtils()
    {}
    
    public static Shape createInstanceColumn(Shape shape, Shape columnType, String name,
                                                    String desc, int maxValues, String format, int displayCol, DatabaseConnection database) throws Exception
    {
        database.startTransaction();
        
        try
        {
            // get the next column to use
            String sql = "select max(col) theMax from Chime.ChimeSchema where dataType_id = '" + shape.getId() + "'";
            DataSet dataSet = database.getDataSet(sql, true);
            dataSet.next();
            IDataValue cval = dataSet.getFieldValue("theMax");
            int col = 1;
            if (!cval.isNull())
            {
                col = cval.asInteger() + 1;
            }
            
            dataSet.close();
            
            // insert schema record
            InstanceId id = Tools.getNewId(Tools.DEFAULT_EXTID);
            sql = "insert into Chime.ChimeSchema " +
                    "(id,datatype_id, col, displayCol, fieldName, field_typeid, maxValues, fieldDescription, private) values ('" +
                    id + "', '" + shape.getId() +
                    "', " + col + ", " + displayCol + ", " + new StringData(name).asSQLValue() +
                    ", '" + columnType.getId() + "', " + maxValues +
                    ", " + new StringData(desc).asSQLValue() +
                    ", 'N')";

            database.executeStatement(sql);
            
            database.commitTransaction();
        }
        catch (Exception e)
        {
            database.rollbackTransaction();
            throw new Exception(e.getMessage());
        }
        
        Shape newType = getInstance(shape.getName(), database, false);
        
        return newType;
        
    } 
    
    public static void getCommunityShapeContexts(Tag tag, User user, DatabaseConnection database) throws Exception
    {
        List<ShapeTagContext> result = new ArrayList<ShapeTagContext>();

        String sql = "select distinct(A.type_id) typeId, B.name typeName, A.usageCount usageCount from Chime.Type_tag_usage A, Chime.DataInstance B where A.tag_id = '" +
                tag.getId() + "' and A.usageCount > 0 and A.type_id = B.ID order by B.name";
        
        DataSet dataSet = database.getDataSet(sql, true);
        
        long totalCount = 0;
        while (dataSet.next())
        {
            InstanceId id = InstanceId.create(dataSet.getFieldValue("typeId").asString());
            DataInstance type = DataInstanceUtils.getInstance(id, user, database, true, true);
            if (Tools.isDataVisible(type, user)) {
                String name = dataSet.getFieldValue("typeName").asString();
                long usageCount = dataSet.getFieldValue("usageCount").asLong();
                totalCount += usageCount;

                Shape t = new Shape();
                t.setId(id);
                t.setName(name);

                ShapeTagContext context = new ShapeTagContext(ShapeTagContext.Type.Community, t, usageCount);
                result.add(context);
            }
        }

        tag.setCommunityContext(result);
        tag.setUsageCount(totalCount);
        
        dataSet.close();
    }
    
    public static void getUserShapeContexts(User user, Tag tag, DatabaseConnection database) throws Exception
    {
        List<ShapeTagContext> result = new ArrayList<ShapeTagContext>();

        String sql = "select distinct(A.type_id) typeId, B.name typeName, A.usageCount usageCount from Chime.User_tag_usage A, Chime.DataInstance B where A.tag_id = '" +
                tag.getId() + "' and A.usageCount > 0 and A.type_id = B.ID and A.user_id = '" + user.getId() + "' order by B.name";

        DataSet dataSet = database.getDataSet(sql, true);

        long totalCount = 0;
        while (dataSet.next())
        {
            InstanceId id = InstanceId.create(dataSet.getFieldValue("typeId").asString());
            String name = dataSet.getFieldValue("typeName").asString();
            long usageCount = dataSet.getFieldValue("usageCount").asLong();
            totalCount += usageCount;

            Shape t = new Shape();
            t.setId(id);
            t.setName(name);

            ShapeTagContext context = new ShapeTagContext(ShapeTagContext.Type.Community, t, usageCount);
            result.add(context);
        }

        tag.setUserContext(result);
        tag.setUsageCount(totalCount);

        dataSet.close();
    }

    public static List<Shape> findShapes(String string, boolean includeInternals, DatabaseConnection database) throws Exception {
        // TODO add the 1000 row limiting code
        
        List<Shape> types = new ArrayList<Shape>();

        String sql = "select A.id, A.name, B.value description from DataInstance A, DataInstance_Text B where A.name like '" + string + "%'";
        
        if (!includeInternals)
        {
            sql += " and A.primitive = 'N'";
        }
        
        sql += " and A.id = B.instance_id and B.column = 1 and order by A.name";
        
        DataSet dataSet = database.getDataSet(sql, true);
        while (dataSet.next())
        {
            InstanceId id = InstanceId.create(dataSet.getFieldValue("id").asString());
            String name = dataSet.getFieldValue("name").asString();
            String description = dataSet.getFieldValue("description").asString();

            Shape t = new Shape();
            t.setId(id);
            t.setName(name);
            t.setDescription(description);
            types.add(t);
        }

        dataSet.close();
        
        return types;
    }

    public static Shape createInstance(String shapeName, String description, User user,
            List<FieldDefinition> fieldDefs, boolean tabular, List<Scope> scopes, DatabaseConnection database) throws Exception
    {
        Shape result = null;
        
        database.startTransaction();
        
        try
        {
            Shape shape = ShapeUtils.getInstance("Shape", database, true);
            List<Shape> shapes = new ArrayList<Shape>();
            shapes.add(shape);

            List<FieldData> fieldData = new ArrayList<FieldData>();

            String mask = DEFAULT_MASK;
            if (tabular) {
            	mask = TABULAR_MASK;
            }
            
            DataInstance inst = DataInstanceUtils.createInstance(shapes, shapeName, description, null,
                    new String[] {"charVal", mask}, fieldData, scopes, user, database);

            result = addFields(inst.getId(), fieldDefs, database);
            
            
            result = ShapeUtils.getInstanceById(inst.getId(), database, false);
            database.commitTransaction();
        }
        catch (Exception e)
        {
            database.rollbackTransaction();
            throw new Exception(e.getMessage());
        }
        
        return result;
    }

    public static Shape updateFields(Shape shape, User user, DatabaseConnection database) throws Exception {
    	Shape result = null;
    	database.startTransaction();

        Shape original = ShapeUtils.getInstanceById(shape.getId(), database, true);
        try {
        	// update the existing fields, and add the new ones.  existing fields allow for changes to description,
        	// format, and position.
        	int idx = 1;
        	for (DataField field : shape.getFields()) {
        		if (field.getId().equals(InstanceId.UNKNOWN.getValue())) {
        			// this is a new one
                    checkFieldNameAvailable(original, field.getName());
                    shape = createInstanceColumn(shape, field.getShape(), field.getName(), 
                    		field.getDescription(), field.getMaxValues(), field.getFormat(), idx, database);
        		} else {
        			String sql = "update Chime.ChimeSchema set " +
							"fielddescription = " + new StringData(field.getDescription()).asSQLValue() +
        					", displaycol = " + idx +
        					", displayformat = " + new StringData(field.getFormat()).asSQLValue() +
        					" where id = '" + field.getId() + "'";
        			database.executeStatement(sql);
        		}
        		
        		idx++;
        	}

            DataInstanceUtils.setUpdated(shape, user, database);
            database.commitTransaction();
            result = getInstanceById(shape.getId(), database, false);
        } catch (Exception e) {
            database.rollbackTransaction();
            throw new Exception(e.getMessage());
        }
    	
    	return result;
    }
    
    public static Shape addFields(InstanceId shapeId, List<FieldDefinition> fieldDefs, DatabaseConnection database) throws Exception
    {
        Shape shape = getInstanceById(shapeId, database, false);
        int col = 1;
        for (FieldDefinition def : fieldDefs)
        {
            Shape colType = getInstance(def.typeName, database, true);
            checkFieldNameAvailable(shape, def.name);
            shape = createInstanceColumn(shape, colType, def.name, def.description, def.maxValues, "", col++, database);
        }
        
        return shape;
    }

    private static void checkFieldNameAvailable(Shape shape, String name) throws Exception {
        for (DataField field : shape.getFields()) {
            if (field.getName().equalsIgnoreCase(name)) {
                throw new Exception("Duplicate Field Name Not Allowed: " + name);
            }
        }
    }

    public static Shape removeFields(InstanceId shapeId, List<FieldDefinition> fieldDefs, DatabaseConnection database) throws Exception
    {
        Shape shape = getInstanceById(shapeId, database, false);
        for (FieldDefinition def : fieldDefs)
        {
            shape = removeInstanceColumn(shape, def.name, database);
        }
        
        return shape;
    }

    public static Shape removeInstanceColumn(Shape shape, String fieldName, DatabaseConnection database) throws Exception
    {
        database.startTransaction();
        
        try
        {
            // get the column name
            String sql = "select id, field_typeid, col from Chime.ChimeSchema where datatype_id = '" + shape.getId() +
                           "' and fieldName = " + new StringData(fieldName).asSQLValue();
            DataSet dataSet = database.getDataSet(sql, true);
            dataSet.next();
            String id = dataSet.getFieldValue("id").asString();
            String typeid = dataSet.getFieldValue("field_typeid").asString();
            int col = dataSet.getFieldValue("col").asInteger();

            dataSet.close();

            Shape fieldType = ShapeUtils.getInstanceById(InstanceId.create(typeid), database, true);

            // remove schema record
            sql = "delete from Chime.ChimeSchema where id = '" + id + "'";
            database.executeStatement(sql);
            
            // remove all of the column data
            String ext = "_Reference";
            if (fieldType.isPrimitive()) {
                if (fieldType.isNumeric() || fieldType.isBoolean()) {
                    ext = "_Number";
                } else if (fieldType.isDate()) {
                	ext = "_Timestamp";
                } else {
                    ext = "_Text";
                }
            }

            sql = "delete from " + Tools.getTableSet() + ext + " where datatype_id = '" + shape.getId()
                    + "' and datatype_column = " + col;
            database.executeStatement(sql);
            
            database.commitTransaction();
        }
        catch (Exception e)
        {
            database.rollbackTransaction();
            throw new Exception(e.getMessage());
        }
        
        Shape newType = getInstance(shape.getName(), database, false);
        
        return newType;
        
    } 

    public static List<Shape> getInstanceShapes(InstanceId instanceId, DatabaseConnection database) throws Exception {

        List<Shape> results = new ArrayList<Shape>();

        String sql = "select datatype_id from " + Tools.getTableSet() + "_Type where instance_id = '" + instanceId + "' order by position";
        DataSet dataSet = database.getDataSet(sql, true);
        while (dataSet.next()) {
            Shape shape = getInstanceById(InstanceId.create(dataSet.getFieldValue("datatype_id").asString()), database, true);

            // grab the scopes
            DataSocialContext context = shape.createNewSocialContext();
            List<Scope> scopeList = ScopeUtils.getScopes(shape.getId(), database);
            for (Scope scope : scopeList) {
                context.addScope(scope);
            }

            shape.setSocialContext(context);
            results.add(shape);
        }

        dataSet.close();
        
        return results;
    }

    private static Shape getInstanceById(HashMap<InstanceId, Shape> cache, InstanceId shapeId, DatabaseConnection database, boolean useCache) throws Exception
    {
        Shape type = null;

        if (useCache) {
            type = CacheManager.instance().getShape(shapeId);
        }

        if (type == null)
        {
            String sql = "SELECT A.* FROM "
                      + "Chime.DataInstance A where A.id = '" + shapeId
                      + "' order by id";

            type = getInstance(sql, cache, database);
        }
        
        return type;
    }
    
    private static Shape getInstanceByName(HashMap<InstanceId, Shape> cache, String shapeName, DatabaseConnection database, boolean useCache) throws Exception
    {
        Shape type = null;
        
        if (useCache)
        {
            type = CacheManager.instance().getShape(shapeName);
        }
        
        if (type == null)
        {
            String sql = "SELECT A.* FROM "
                      + "Chime.DataInstance A where A.name = '" + shapeName
                      + "' order by id";

            type = getInstance(sql, cache, database);
        }
        
        return type;
    }
    
    private static Shape getInstance(String sql, HashMap<InstanceId, Shape> cache, DatabaseConnection database) throws Exception
    {
        DataSet dataSet = database.getDataSet(sql, true);
        boolean found = dataSet.next();
        if (!found)
        {
            dataSet.close();
            throw new Exception("Unknown data type");
        }

        IDataValue id = dataSet.getFieldValue("id");
        String theTypeId = id.asString();
        IDataValue name = dataSet.getFieldValue("name");
        IDataValue description = dataSet.getFieldValue("description");

        Shape shape = new Shape();

        String m = dataSet.getFieldValue("charVal").asString();
        char[] mask = m.toCharArray();
        shape.setPrimitive(mask[0] == 'Y');
        shape.setVisible(mask[1] == 'Y');
        shape.setDirectCreatable(mask[2] == 'Y');
        shape.setCanVote(mask[3] == 'Y');
        shape.setCanReview(mask[4] == 'Y');
        shape.setCanComment(mask[5] == 'Y');
        shape.setCanTag(mask[6] == 'Y');
        shape.setCanPoll(mask[7] == 'Y');
        shape.setCanAttachFiles(mask[8] == 'Y');
        shape.setHasImageGallery(mask[9] == 'Y');
        shape.setCanDiscuss(mask[10] == 'Y');
        shape.setCanMultiType(mask[11] == 'Y');
        shape.setTabular(mask[12] == 'Y');


        shape.setName(name.asString());
        shape.setDescription(description.asString());
        shape.setId(InstanceId.create(id.asString()));

        // this gets the data fields
        dataSet.close();

        cache.put(shape.getId(), shape);
        
        sql = "SELECT A.*, B.id, A.id theId, B.name Bname FROM Chime.ChimeSchema A, Chime.DataInstance B where "
                + "A.datatype_id = '" + theTypeId + "' and A.field_typeid = B.id order by A.displayCol";
        dataSet = database.getDataSet(sql, true);
        found = dataSet.next();
        while (found)
        {
            DataField field = new DataField();
            field.setName(dataSet.getFieldValue("fieldName").asString());
            field.setDescription(dataSet.getFieldValue("fieldDescription").asString());
            field.setId(dataSet.getFieldValue("theId").asString());
            field.setPrivate(dataSet.getFieldValue("private").asString().equals("Y"));
            field.setUserEditable(dataSet.getFieldValue("userEditable").asString().equals("Y"));
            field.setMaxValues(dataSet.getFieldValue("maxValues").asInteger());
            field.setColumn(dataSet.getFieldValue("col").asInteger());
            
            Shape t;
            InstanceId tid = InstanceId.create(dataSet.getFieldValue("field_typeid").asString());
            
            if (cache.containsKey(tid))
            {
                t = cache.get(tid);
            }
            else
            {
                t = getInstanceById(cache, tid, database, true);
            }
            
            field.setShape(t);
            
            shape.addField(field);
            found = dataSet.next();
        }

        CacheManager.instance().putShape(shape);

        dataSet.close();
        return shape;
    }
    
    public static Shape getInstance(String shapeName, DatabaseConnection database, boolean useCache) throws Exception
    {
        HashMap<InstanceId, Shape> cache = new HashMap<InstanceId, Shape>();
        return getInstanceByName(cache, shapeName, database, useCache);
    }
    
    public static Shape getInstanceById(InstanceId shapeId, DatabaseConnection database, boolean useCache) throws Exception
    {
        HashMap<InstanceId, Shape> cache = new HashMap<InstanceId, Shape>();
        return getInstanceById(cache, shapeId, database, false);
    }

    public static void getFields(Shape shape, DatabaseConnection database, boolean useCache) throws Exception {

        HashMap<InstanceId, Shape> cache = new HashMap<InstanceId, Shape>();
        cache.put(shape.getId(), shape);

        String sql = "SELECT A.*, A.id theId, B.name DataTypename FROM Chime.ChimeSchema A, Chime.DataInstance B where "
                + "A.datatype_id = '" + shape.getId() + "' and A.field_typeid = B.id order by A.displayCol";
        DataSet dataSet = database.getDataSet(sql, true);
        boolean found = dataSet.next();
        while (found)
        {
            DataField field = new DataField();
            field.setName(dataSet.getFieldValue("fieldName").asString());
            field.setDescription(dataSet.getFieldValue("fieldDescription").asString());
            field.setId(dataSet.getFieldValue("theId").asString());
            field.setPrivate(dataSet.getFieldValue("private").asString().equals("Y"));
            field.setUserEditable(dataSet.getFieldValue("userEditable").asString().equals("Y"));
            field.setMaxValues(dataSet.getFieldValue("maxValues").asInteger());
            field.setColumn(dataSet.getFieldValue("col").asInteger());

            Shape t;
            InstanceId tid = InstanceId.create(dataSet.getFieldValue("field_typeid").asString());

            if (cache.containsKey(tid))
            {
                t = cache.get(tid);
            }
            else
            {
                t = getInstanceById(cache, tid, database, true);
            }

            field.setShape(t);

            shape.addField(field);
            found = dataSet.next();
        }

        dataSet.close();
    }
}
