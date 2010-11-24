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

package com.paxxis.chime.service;

import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceHelper;
import com.paxxis.chime.client.common.portal.PortalColumn;
import com.paxxis.chime.client.common.portal.PortalTemplate;
import com.paxxis.chime.client.common.portal.PortletSpecification;
import com.paxxis.chime.client.common.portal.PortletSpecification.PortletType;
import com.paxxis.cornerstone.base.RequestMessage;
import com.paxxis.cornerstone.json.JSONArray;
import com.paxxis.cornerstone.json.JSONObject;
import com.paxxis.cornerstone.json.parser.JSONParser;

import java.util.List;
import java.util.Set;

/**
 *
 * @author Robert Englander
 */
public class PortalTemplateHelper implements DataInstanceHelper
{
    public static void mainZZZZ(String[] args)
    {
        
        PortalTemplate spec = new PortalTemplate();
        PortalColumn col = new PortalColumn(1);

        // the header
        PortletSpecification pspec = new PortletSpecification(PortletSpecification.PortletType.InstanceHeader, 1);
        pspec.setPinned(true);
        pspec.setShowHeader(false);
        pspec.setProperty("showDescription", false);
        col.add(pspec);

        // the field data
        pspec = new PortletSpecification(PortletSpecification.PortletType.InstanceField, 2);
        pspec.setPinned(true);
        pspec.setShowHeader(false);
        col.add(pspec);

        spec.add(col);

        // reviews
        pspec = new PortletSpecification(PortletSpecification.PortletType.InstanceSocialActivity, 3);
        pspec.setPinned(true);
        pspec.setShowHeader(true);
        //pspec.setProperty("height", 350);
        col.add(pspec);


        col = new PortalColumn(300);

        // image gallery
        pspec = new PortletSpecification(PortletSpecification.PortletType.ImageGallery, 4);
        pspec.setPinned(true);
        pspec.setShowHeader(true);
        pspec.setProperty("height", 250);
        col.add(pspec);

        spec.add(col);

        PortalTemplateHelper helper = new PortalTemplateHelper();
        String t = helper.convert(spec);
        
        int x = 1;
    }

    /**
     * Review instance template
     */
    public static void main(String[] args) {
        //createTagTemplate();
        //createShapeTemplate();
        //createUserTemplate();
        //createCommunityTemplate();
        //createDiscussionTemplate();
        //createPortalPageTemplate();
        //createPollTemplate();
        createHome();
    }

    public static void createReviewTemplate() {
        PortalTemplate spec = new PortalTemplate();
        PortalColumn col = new PortalColumn(1);

        // the header
        PortletSpecification pspec = new PortletSpecification(PortletSpecification.PortletType.InstanceHeader, 1);
        pspec.setPinned(true);
        pspec.setShowHeader(false);
        pspec.setProperty("showDescription", false);
        col.add(pspec);

        // the review
        pspec = new PortletSpecification(PortletSpecification.PortletType.ReviewContent, 2);
        pspec.setPinned(true);
        pspec.setShowHeader(false);
        col.add(pspec);

        spec.add(col);

        // social activity
        pspec = new PortletSpecification(PortletSpecification.PortletType.InstanceSocialActivity, 3);
        pspec.setPinned(true);
        pspec.setShowHeader(true);
        //pspec.setProperty("height", 350);
        col.add(pspec);


        col = new PortalColumn(300);

        // image gallery
        pspec = new PortletSpecification(PortletSpecification.PortletType.ImageGallery, 4);
        pspec.setPinned(true);
        pspec.setShowHeader(true);
        pspec.setProperty("height", 250);
        col.add(pspec);

        spec.add(col);

        PortalTemplateHelper helper = new PortalTemplateHelper();
        String t = helper.convert(spec);

        int x = 1;
    }

    public static void createUserTemplate() {
        PortalTemplate spec = new PortalTemplate();
        PortalColumn col = new PortalColumn(1);

        // the header
        PortletSpecification pspec = new PortletSpecification(PortletSpecification.PortletType.InstanceHeader, 1);
        pspec.setPinned(true);
        pspec.setShowHeader(false);
        pspec.setProperty("showDescription", true);
        pspec.setProperty("setCollapsed", true);
        col.add(pspec);

        pspec = new PortletSpecification(PortletSpecification.PortletType.InstanceField, 2);
        pspec.setPinned(true);
        pspec.setShowHeader(false);
        col.add(pspec);

        spec.add(col);

        // social activity
        pspec = new PortletSpecification(PortletSpecification.PortletType.InstanceSocialActivity, 3);
        pspec.setPinned(true);
        pspec.setShowHeader(true);
        //pspec.setProperty("height", 350);
        col.add(pspec);


        col = new PortalColumn(300);

        // image gallery
        pspec = new PortletSpecification(PortletSpecification.PortletType.ImageGallery, 4);
        pspec.setPinned(true);
        pspec.setShowHeader(true);
        pspec.setProperty("height", 250);
        col.add(pspec);

        spec.add(col);

        PortalTemplateHelper helper = new PortalTemplateHelper();
        String t = helper.convert(spec);

        int x = 1;
    }
    public static void createCommunityTemplate() {
        PortalTemplate spec = new PortalTemplate();
        PortalColumn col = new PortalColumn(1);

        // the header
        PortletSpecification pspec = new PortletSpecification(PortletSpecification.PortletType.InstanceHeader, 1);
        pspec.setPinned(true);
        pspec.setShowHeader(false);
        pspec.setProperty("showDescription", true);
        col.add(pspec);

        // the review
        pspec = new PortletSpecification(PortletSpecification.PortletType.CommunityDetail, 2);
        pspec.setPinned(true);
        pspec.setShowHeader(false);
        col.add(pspec);

        spec.add(col);

        // social activity
        pspec = new PortletSpecification(PortletSpecification.PortletType.InstanceSocialActivity, 3);
        pspec.setPinned(true);
        pspec.setShowHeader(true);
        //pspec.setProperty("height", 350);
        col.add(pspec);


        col = new PortalColumn(300);

        // image gallery
        pspec = new PortletSpecification(PortletSpecification.PortletType.ImageGallery, 4);
        pspec.setPinned(true);
        pspec.setShowHeader(true);
        pspec.setProperty("height", 250);
        col.add(pspec);

        spec.add(col);

        PortalTemplateHelper helper = new PortalTemplateHelper();
        String t = helper.convert(spec);

        int x = 1;
    }

    /**
     * Tag instance template
     */
    public static void createTagTemplate() {

        PortalTemplate spec = new PortalTemplate();
        PortalColumn col = new PortalColumn(1);

        // the header
        PortletSpecification pspec = new PortletSpecification(PortletSpecification.PortletType.InstanceHeader, 1);
        pspec.setPinned(true);
        pspec.setShowHeader(false);
        pspec.setProperty("showDescription", true);
        col.add(pspec);

        // the tag detail
        pspec = new PortletSpecification(PortletSpecification.PortletType.TagContent, 2);
        pspec.setPinned(true);
        pspec.setShowHeader(false);
        col.add(pspec);

        spec.add(col);

        // social activity
        pspec = new PortletSpecification(PortletSpecification.PortletType.InstanceSocialActivity, 3);
        pspec.setPinned(true);
        pspec.setShowHeader(true);
        //pspec.setProperty("height", 350);
        col.add(pspec);


        col = new PortalColumn(300);

        // image gallery
        //pspec = new PortletSpecification(PortletSpecification.PortletType.ImageGallery, "4");
        //pspec.setPinned(true);
        //pspec.setShowHeader(true);
        //pspec.setProperty("height", 250);
        //col.add(pspec);

        //spec.add(col);

        PortalTemplateHelper helper = new PortalTemplateHelper();
        String t = helper.convert(spec);

        int x = 1;
    }

    public static void createPortalPageTemplate() {

        PortalTemplate spec = new PortalTemplate();
        PortalColumn col = new PortalColumn(1);

        // the header
        PortletSpecification pspec = new PortletSpecification(PortletSpecification.PortletType.InstanceHeader, 1);
        pspec.setPinned(true);
        pspec.setShowHeader(false);
        pspec.setProperty("showDescription", true);
        pspec.setProperty("showCollapsed", true);
        pspec.setProperty("headerType", "Transparent");
        col.add(pspec);

        
        pspec = new PortletSpecification(PortletSpecification.PortletType.PortalPage, 2);
        pspec.setPinned(true);
        pspec.setShowHeader(false);
        pspec.setProperty("headerType", "None");
        col.add(pspec);
        
        
        spec.add(col);

        // social activity
        pspec = new PortletSpecification(PortletSpecification.PortletType.InstanceSocialActivity, 3);
        pspec.setPinned(true);
        pspec.setShowHeader(true);
        //pspec.setProperty("height", 350);
        col.add(pspec);

        PortalTemplateHelper helper = new PortalTemplateHelper();
        String t = helper.convert(spec);

        int x = 1;
    }

    /**
     * Poll instance template
     */
    public static void createPollTemplate() {

        PortalTemplate spec = new PortalTemplate();
        PortalColumn col = new PortalColumn(1);

        // the header
        PortletSpecification pspec = new PortletSpecification(PortletSpecification.PortletType.InstanceHeader, 1);
        pspec.setPinned(true);
        pspec.setShowHeader(false);
        pspec.setProperty("showDescription", true);
        col.add(pspec);

        // the tag detail
        pspec = new PortletSpecification(PortletSpecification.PortletType.PollContent, 2);
        pspec.setPinned(true);
        pspec.setShowHeader(false);
        col.add(pspec);

        spec.add(col);

        // social activity
        pspec = new PortletSpecification(PortletSpecification.PortletType.InstanceSocialActivity, 3);
        pspec.setPinned(true);
        pspec.setShowHeader(true);
        //pspec.setProperty("height", 350);
        col.add(pspec);


        col = new PortalColumn(300);

        // image gallery
        //pspec = new PortletSpecification(PortletSpecification.PortletType.ImageGallery, 4);
        //pspec.setPinned(true);
        //pspec.setShowHeader(true);
        //pspec.setProperty("height", 250);
        //col.add(pspec);

        //spec.add(col);

        PortalTemplateHelper helper = new PortalTemplateHelper();
        String t = helper.convert(spec);

        int x = 1;
    }

    /**
     * discussion instance template
     */
    public static void createDiscussionTemplate() {

        PortalTemplate spec = new PortalTemplate();
        PortalColumn col = new PortalColumn(1);

        // the header
        PortletSpecification pspec = new PortletSpecification(PortletSpecification.PortletType.InstanceHeader, 1);
        pspec.setPinned(true);
        pspec.setShowHeader(false);
        pspec.setProperty("showDescription", false);
        col.add(pspec);

        // the detail
        pspec = new PortletSpecification(PortletSpecification.PortletType.DiscussionContent, 2);
        pspec.setPinned(true);
        pspec.setShowHeader(false);
        col.add(pspec);

        spec.add(col);

        // social activity
        pspec = new PortletSpecification(PortletSpecification.PortletType.InstanceSocialActivity, 3);
        pspec.setPinned(true);
        pspec.setShowHeader(true);
        pspec.setProperty("showComments", false);
        col.add(pspec);


        col = new PortalColumn(300);

        // image gallery
        //pspec = new PortletSpecification(PortletSpecification.PortletType.ImageGallery, 4);
        //pspec.setPinned(true);
        //pspec.setShowHeader(true);
        //pspec.setProperty("height", 250);
        //col.add(pspec);

        //spec.add(col);

        PortalTemplateHelper helper = new PortalTemplateHelper();
        String t = helper.convert(spec);

        int x = 1;
    }

    public static void createShapeTemplate() {

        PortalTemplate spec = new PortalTemplate();
        PortalColumn col = new PortalColumn(1);

        // the header
        PortletSpecification pspec = new PortletSpecification(PortletSpecification.PortletType.InstanceHeader, 1);
        pspec.setPinned(true);
        pspec.setShowHeader(false);
        pspec.setProperty("showDescription", true);
        col.add(pspec);

        // the detail
        pspec = new PortletSpecification(PortletSpecification.PortletType.DataTypeContent, 2);
        pspec.setPinned(true);
        pspec.setShowHeader(false);
        col.add(pspec);

        spec.add(col);

        // social activity
        pspec = new PortletSpecification(PortletSpecification.PortletType.InstanceSocialActivity, 3);
        pspec.setPinned(true);
        pspec.setShowHeader(true);
        //pspec.setProperty("height", 350);
        col.add(pspec);


        col = new PortalColumn(300);

        // image gallery
        //pspec = new PortletSpecification(PortletSpecification.PortletType.ImageGallery, 4);
        //pspec.setPinned(true);
        //pspec.setShowHeader(true);
        //pspec.setProperty("height", 250);
        //col.add(pspec);

        //spec.add(col);

        PortalTemplateHelper helper = new PortalTemplateHelper();
        String t = helper.convert(spec);

        int x = 1;
    }

    public static void createHome()
    {

        PortalTemplate spec = new PortalTemplate();
        PortalColumn col = new PortalColumn(1.0);

        // the general welcome news story
        PortletSpecification pspec = new PortletSpecification(PortletSpecification.PortletType.DataInstance, 1);
        pspec.setPinned(true);
        pspec.setShowHeader(false);
        //pspec.setProperty("typeId", 41);
        pspec.setProperty("instanceId", 2028);
        pspec.setProperty("fieldName", "Content");
        col.add(pspec);

        pspec = new PortletSpecification(PortletSpecification.PortletType.DataInstance, 2);
        pspec.setPinned(true);
        pspec.setShowHeader(false);
        //pspec.setProperty("typeId", 41);
        pspec.setProperty("instanceId", 2060);
        pspec.setProperty("fieldName", "Content");
        col.add(pspec);

        pspec = new PortletSpecification(PortletSpecification.PortletType.NamedSearch, 3);
        pspec.setPinned(false);
        pspec.setShowHeader(true);
        pspec.setProperty("instanceId", 4089);
        pspec.setProperty("height", 250);
        col.add(pspec);

        spec.add(col);

        /*
        // a showcased instance
        pspec = new PortletSpecification(PortletSpecification.PortletType.DataInstance, 3);
        pspec.setPinned(false);
        pspec.setShowHeader(true);
        pspec.setProperty("typeId", 128647);
        pspec.setProperty("instanceId", 128882);
        pspec.setProperty("fieldName", "Description");
        col.add(pspec);

        // another showcased instance
        pspec = new PortletSpecification(PortletSpecification.PortletType.DataInstance, 4);
        pspec.setPinned(false);
        pspec.setShowHeader(true);
        pspec.setProperty("typeId", 128647);
        pspec.setProperty("instanceId", 128867);
        pspec.setProperty("fieldName", "Description");
        col.add(pspec);

        spec.add(col);

        col = new PortalColumn(0.55);

        // a named search
        pspec = new PortletSpecification(PortletSpecification.PortletType.NamedSearch, 5);
        pspec.setPinned(false);
        pspec.setShowHeader(true);
        pspec.setProperty("typeId", 128408);
        pspec.setProperty("instanceId", 128902);
        pspec.setProperty("height", 250);
        col.add(pspec);

        // a named search
        pspec = new PortletSpecification(PortletSpecification.PortletType.NamedSearch, 6);
        pspec.setPinned(false);
        pspec.setShowHeader(true);
        pspec.setProperty("typeId", 128408);
        pspec.setProperty("instanceId", 128908);
        pspec.setProperty("height", 250);
        col.add(pspec);

        // a named search
        pspec = new PortletSpecification(PortletSpecification.PortletType.NamedSearch, 7);
        pspec.setPinned(false);
        pspec.setShowHeader(true);
        pspec.setProperty("typeId", 128408);
        pspec.setProperty("instanceId", 128914);
        pspec.setProperty("height", 250);
        col.add(pspec);
        spec.add(col);
        */

        PortalTemplateHelper helper = new PortalTemplateHelper();
        String t = helper.convert(spec);

        int x = 1;
    }

    public void processAfterRead(DataInstance instance, Object obj) 
    {
    }

    public void processBeforeWrite(RequestMessage request)
    {
    }
    
    public String convert(PortalTemplate template)
    {
        String text = "";
        
        JSONObject json = new JSONObject();
        JSONArray array = new JSONArray();
        List<PortalColumn> columns = template.getPortalColumns();
        for (PortalColumn column : columns)
        {
            JSONObject obj = columnToJSON(column);
            array.add(obj);
        }

        String id = template.getId();
        if (id == null) {
            id = "-1";
        }
        json.put("id", id);
        json.put("columns", array);

        text = json.toString();
        
        return text;
    }
    
    public PortalTemplate convert(String text)
    {
        PortalTemplate template = new PortalTemplate();
        
        JSONParser parser = JSONParser.create(text);
        
        try
        {
            JSONObject jsonObject = parser.parse();
            
            String id = jsonObject.getString("id");
            template.setId(id);
            
            JSONArray array = jsonObject.getJSONArray("columns");
            int cnt = array.length();
            for (int i = 0; i < cnt; i++)
            {
                PortalColumn column = JSONToColumn(array.getJSONObject(i));
                template.add(column);
            }            
        }
        catch (Exception e)
        {
            // this needs to be reported in some way, not just eaten like this
        }
        
        return template;
    }
    
    private JSONObject columnToJSON(PortalColumn column)
    {
        JSONObject json = new JSONObject();

        json.put("width", column.getWidth());
        
        List<PortletSpecification> specs = column.getPortletSpecifications();
        JSONArray array = new JSONArray();
        
        for (PortletSpecification spec : specs)
        {
            JSONObject obj = portletSpecToJSON(spec);
            array.add(obj);
        }
        
        json.put("portlets", array);
        return json;
    }

    private JSONObject portletSpecToJSON(PortletSpecification spec)
    {
        JSONObject obj = new JSONObject();
        obj.put("id", spec.getId());
        obj.put("type", spec.getType().toString());
        obj.put("pinned", spec.isPinned());
        obj.put("showHeader", spec.isShowHeader());

        JSONArray props = new JSONArray();

        Set<String> names = spec.getPropertyNames();
        for (String name : names)
        {
            String value = spec.getProperty(name).toString();
            JSONObject p = new JSONObject();
            p.put(name, value);
            props.add(p);
        }

        obj.put("properties", props);
        
        return obj;
    }
    
    private PortalColumn JSONToColumn(JSONObject json)
    {
        PortalColumn column = null;
        
        try
        {
            double width = json.getDouble("width");
            column = new PortalColumn(width);
            
            JSONArray portlets = json.getJSONArray("portlets");
            int cnt = portlets.length();
            for (int i = 0; i < cnt; i++)
            {
                JSONObject jsonPortlet = portlets.getJSONObject(i);
                PortletSpecification portletSpec = jsonToPortletSpec(jsonPortlet);
                column.add(portletSpec);
            }
        }
        catch (Exception e)
        {
            // this needs to be reported in some way, not just eaten like this
        }
        
        return column;
    }
    
    private PortletSpecification jsonToPortletSpec(JSONObject jsonPortlet)
    {
        long id = jsonPortlet.getLong("id");
        PortletType type = PortletType.valueOf(jsonPortlet.getString("type"));
        PortletSpecification portletSpec = new PortletSpecification(type, id);
        portletSpec.setPinned(jsonPortlet.getBoolean("pinned"));
        portletSpec.setShowHeader(jsonPortlet.getBoolean("showHeader"));

        JSONArray props = jsonPortlet.getJSONArray("properties");
        int pcnt = props.length();
        for (int j = 0; j < pcnt; j++)
        {
            JSONObject prop = props.getJSONObject(j);
            Set<String> names = prop.getNames();

            // each one has only 1 name and value.  is there a better way?
            String name = names.toArray()[0].toString();
            String value = prop.getString(name);
            portletSpec.setProperty(name, value);
        }
        
        return portletSpec;
    }
}
