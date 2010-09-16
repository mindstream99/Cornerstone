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

package com.paxxis.chime.client.portal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.InfoConfig;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Timer;
import com.paxxis.chime.client.InstanceUpdateListener;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.NamedSearch;
import com.paxxis.chime.client.common.SearchCriteria;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.client.common.portal.PortalColumn;
import com.paxxis.chime.client.common.portal.PortalTemplate;
import com.paxxis.chime.client.common.portal.PortletSpecification;
import com.paxxis.chime.client.common.portal.PortletSpecification.PortletType;
import com.paxxis.chime.client.portal.PortletContainer.HeaderType;



/**
 *
 * @author Robert Englander
 */
public class PortalUtils {
	
	private static List<Timer> activeTimers = new ArrayList<Timer>();
	
    private PortalUtils()
    {
    }

    public static void registerTimer(Timer timer) {
    	activeTimers.add(timer);
    }
    
    public static void clearTimers() {
    	for (Timer t : activeTimers) {
    		t.cancel();
    	}
    	
    	activeTimers.clear();
    }
    
    public static boolean updatePortal(PortalContainer portal, PortalTemplate template,
    									DataInstance instance, UpdateReason reason) {
        if (portal == null)
        {
            return false;
        }
        else if (!portal.getTemplate().getId().equals(template.getId()))
        {
            return false;
        }
        
        boolean updated = true;
        
        try
        {
            // run through each portlet in the template and update its corresponding
            // portlet in the portal
            List<PortalColumn> columns = template.getPortalColumns();
            for (PortalColumn column : columns)
            {
                List<PortletSpecification> specs = column.getPortletSpecifications();
                for (PortletSpecification spec : specs)
                {
                    updatePortlet(spec, portal, instance, reason);
                }
            }
        }
        catch (Exception e)
        {
        	InfoConfig cfg = new InfoConfig("Update Portal 1 Exception", e.getLocalizedMessage());
        	Info.display(cfg);
            updated = false;
        }
        
        return updated;
    }
    
    public static boolean updatePortal(PortalContainer portal, DataInstance instance, UpdateReason reason)
    {
        if (portal == null) {
            return false;
        } else if (!portal.hasPortlets()) {
        	return false;
        } else if (!portal.getTemplate().getId().equals(instance.getPortalTemplate().getId())) {
            return false;
        }
        
        boolean updated = true;
        
        // run through each portlet in the template and update its corresponding
        // portlet in the portal
        PortalTemplate template = instance.getPortalTemplate();
        List<PortalColumn> columns = template.getPortalColumns();
        for (PortalColumn column : columns)
        {
            List<PortletSpecification> specs = column.getPortletSpecifications();
            for (PortletSpecification spec : specs)
            {
            	try {
            		updatePortlet(spec, portal, instance, reason);
                } catch (Exception e) {
                	InfoConfig cfg = new InfoConfig("Update Portal 2 Exception", e.getLocalizedMessage());
                	Info.display(cfg);
                    updated = false;
                    break;
                }
            }
        }
        
        return updated;
    }
    
    public static void updatePortlet(PortletSpecification spec, PortalContainer portal, 
            DataInstance instance, UpdateReason reason)
    {
        long id = spec.getId();  
        PortletContainer portlet = portal.getPortletBySpecId(id);

        //InfoConfig cfg = new InfoConfig("updating portlet", "" + spec.getType());
        //Info.display(cfg);
        switch (spec.getType())
        {
            case PortalPage:
                ((PagePortlet)portlet).setDataInstance(instance, reason);
                break;

            case DetailPage:
                ((DetailPagePortlet)portlet).setDataInstance(instance, reason);
            	break;

            case MultiChart:
                ((MultiChartPortlet)portlet).setDataInstance(instance, reason);
            	break;
            	
            case FieldDataChart:
                ((FieldDataChartPortlet)portlet).setDataInstance(instance, reason);
            	break;
            	
            case ShapeFields:
            	{
                	Object obj = spec.getProperty("shapeId");
                	if (obj != null) {
                        ((TypeFieldsPortlet)portlet).setDataInstance(instance, obj.toString());
                	} else {
                		((TypeFieldsPortlet)portlet).setDataInstance(instance, instance.getShapes().get(0));
                	}
            	}

            	break;
            	
            case DataInstance:
                {
                    InstanceId instanceId = InstanceId.create(spec.getProperty("instanceId").toString());
                    String fieldName = spec.getProperty("fieldName").toString();
                    ((DataInstancePortlet)portlet).setDataInstance(instanceId, fieldName);
                }
                break;

            case NamedSearch:
                {
                    InstanceId instanceId = InstanceId.create(spec.getProperty("instanceId").toString());
                    ((NamedSearchPortlet)portlet).execute(instanceId);
                }
                break;

            case Analytic:
                {
                    InstanceId instanceId = InstanceId.create(spec.getProperty("instanceId").toString());
                    ((AnalyticPortlet)portlet).execute(instanceId);
                }
                break;

            case ExternalSite:
                break;


            case UserMessages:
	        	{
	        		User user = ServiceManager.getActiveUser();
	        		boolean isUserInstance = instance.getShapes().get(0).getId().equals(Shape.USER_ID);
	        		if (isUserInstance) {
	        			if (user.isAdmin() || user.getId().equals(instance.getId())) {
	                        ((UserMessagesPortlet)portlet).setUser((User)instance, reason);
	                        portlet.setVisible(true);
	        			} else {
	        				portlet.setVisible(false);
	        			}
	        		}
	        	}
	            break;
            	
            case InstanceField:
	        	{
	        		User user = ServiceManager.getActiveUser();
	        		boolean isUserInstance = instance.getShapes().get(0).getId().equals(Shape.USER_ID);
	        		if (isUserInstance) {
	        			if (user.isAdmin() || user.getId().equals(instance.getId())) {
	                        ((TypeFieldGroupsPortlet)portlet).setDataInstance(instance, reason);
	                        portlet.setVisible(true);
	        			} else {
	        				portlet.setVisible(false);
	        			}
	        		} else {
	                    ((TypeFieldGroupsPortlet)portlet).setDataInstance(instance, reason);
	        		}
	        	}
                break;

            case ImageGallery:
            	if (!instance.isTransient()) {
                    ((ImageGalleryPortlet)portlet).setDataInstance(instance, reason);
            	}
                break;

            case FileGallery:
            	if (!instance.isTransient()) {
                    ((FileGalleryPortlet)portlet).setDataInstance(instance, reason);
            	}
                break;

            case InstanceHeader:
                ((InstanceHeaderPortlet)portlet).setDataInstance(instance, reason);
                break;

            case InstanceSocialActivity:
            	if (!instance.isTransient()) {
            		((InstanceSocialActivityPortlet)portlet).setDataInstance(instance, reason);
            	}
                break;

            case ReviewContent:
                ((ReviewDetailPortlet)portlet).setDataInstance(instance);
                break;

            case NamedSearchDetail:
                ((NamedSearchDetailPortlet)portlet).setDataInstance(instance);
                break;

            case AnalyticDetail:
                ((AnalyticDetailPortlet)portlet).setDataInstance(instance);
                break;

            case FileContent:
                ((FileDetailPortlet)portlet).setDataInstance(instance);
                break;

            case ImageContent:
                ((ImageDetailPortlet)portlet).setDataInstance(instance);
                break;

            case ImageRenderer:
                {
                    InstanceId instanceId = InstanceId.create(spec.getProperty("instanceId").toString());
                    ((ImageRendererPortlet)portlet).execute(instanceId);
                }
                break;

            case InstanceReferrers:
                ((InstanceReferrersPortlet)portlet).setDataInstance(instance, reason);
                break;

            case TagContent:
                ((TagDetailPortlet)portlet).setDataInstance(instance);
                break;

            case DataTypeContent:
                ((DataTypeDetailPortlet)portlet).setDataInstance(instance);
                break;

            case DiscussionContent:
                ((DiscussionDetailPortlet)portlet).setDataInstance(instance, reason);
                break;

            case SearchCriteria:
                ((SearchCriteriaPortlet)portlet).setCriteria(((NamedSearch)instance).getSearchCriteria());
                break;

            case UserDetail:
                ((UserDetailPortlet)portlet).setDataInstance(instance);
                break;

            case RichText:
                break;

            default:
                break;
        }
    }
    
    public static PortalContainer buildPortal(DataInstance instance, InstanceUpdateListener listener)
    {
        PortalTemplate portalTemplate = instance.getPortalTemplate();
        return build(instance, portalTemplate, listener);
    }

    public static PortalContainer buildPortal(DataInstance instance, PortalTemplate template) {
        return build(instance, template, null);
    }

    private static PortalContainer build(DataInstance instance, PortalTemplate portalTemplate, InstanceUpdateListener listener) {

    	clearTimers();
    	
        List<PortalColumn> columns = portalTemplate.getPortalColumns();
        PortalContainer portal = new PortalContainer(columns.size(), listener == null);
        portal.setTemplate(portalTemplate);
        
        portal.setBorders(listener != null);

        int cnt = columns.size();
        for (int i = 0; i < cnt; i++)
        {
            PortalColumn column = columns.get(i);

            double width = column.getWidth();
            portal.setColumnWidth(i, width);
            List<PortletSpecification> specs = column.getPortletSpecifications();
            for (PortletSpecification spec : specs)
            {
                PortletContainer portlet = buildPortlet(spec, instance, width, false, listener);
                if (portlet != null) {
                    portal.add(portlet, i);
                }
            }
        }
        
        return portal;
    }
    
    private static PortletContainer buildPortlet(PortletSpecification spec, DataInstance instance, double width, boolean autoLayout,
            InstanceUpdateListener listener)
    {
        PortletContainer portlet = null;
        PortletType type = spec.getType();
        switch (type)
        {
            case PortalPage:
                portlet = createPagePortlet(spec, instance);
                break;

            case DetailPage:
            	portlet = createDetailPagePortlet(spec, instance);
            	break;

            case MultiChart:
            	portlet = createMultiChartPortlet(spec, instance);
            	break;
            	
            case FieldDataChart:
            	portlet = createFieldDataChartPortlet(spec, instance);
            	break;
            	
            case DataInstance:
                portlet = createDataInstancePortlet(spec, autoLayout);
                break;

            case NamedSearch:
                portlet = createNamedSearchPortlet(spec, autoLayout);
                break;

            case ShapeFields:
            	portlet = createShapeFieldsPortlet(spec, instance, listener);
            	break;
            	
            case Analytic:
                portlet = createAnalyticPortlet(spec, autoLayout);
                break;

            case ExternalSite:
                //ExternalSitePortlet portlet0 = new ExternalSitePortlet(true, false, "http://www.ebay.com");
                //portlet0.setHeading("eBay");
                //portlet = createExternalSitePortlet(spec);
                break;

            case UserMessages:
	        	{
	        		User user = ServiceManager.getActiveUser();
	        		boolean isUserInstance = instance.getShapes().get(0).getId().equals(Shape.USER_ID);
	        		if (isUserInstance) {
	        			if (user.isAdmin() || user.getId().equals(instance.getId())) {
	                        portlet = createUserMessagesPortlet(spec, (User)instance, autoLayout, listener);
	        			}
	        		}
	        	}
	            break;
            	
            case InstanceField:
            	{
            		User user = ServiceManager.getActiveUser();
            		boolean isUserInstance = instance.getShapes().get(0).getId().equals(Shape.USER_ID);
            		if (isUserInstance) {
            			if (user.isAdmin() || user.getId().equals(instance.getId())) {
                            portlet = createInstanceFieldPortlet(spec, instance, autoLayout, listener);
            			}
            		} else {
                        portlet = createInstanceFieldPortlet(spec, instance, autoLayout, listener);
            		}
            	}
                break;

            case ImageGallery:
            	if (!instance.isTransient()) {
                    portlet = createImageGalleryPortlet(spec, instance, autoLayout, width, listener);
            	}
                break;

            case FileGallery:
            	if (!instance.isTransient()) {
                    portlet = createFileGalleryPortlet(spec, instance, autoLayout, width, listener);
            	}
                break;

            case InstanceReferrers:
                portlet = createInstanceReferrersPortlet(spec, instance, width);
                break;

            case InstanceHeader:
                portlet = createInstanceHeaderPortlet(spec, instance, autoLayout, listener);
                break;

            case ReviewContent:
                portlet = createReviewDetailPortlet(spec, instance, listener);
                break;

            case NamedSearchDetail:
                portlet = createNamedSearchDetailPortlet(spec, instance, listener);
                break;

            case AnalyticDetail:
                portlet = createAnalyticDetailPortlet(spec, instance, listener);
                break;

            case FileContent:
                portlet = createFileDetailPortlet(spec, instance, listener);
                break;

            case ImageContent:
                portlet = createImageDetailPortlet(spec, instance, listener);
                break;

            case ImageRenderer:
                portlet = createImageRendererPortlet(spec);
                break;
                
            case DiscussionContent:
                portlet = createDiscussionDetailPortlet(spec, instance, listener);
                break;

            case TagContent:
                portlet = createTagDetailPortlet(spec, instance, listener);
                break;

            case DataTypeContent:
                portlet = createDataTypeDetailPortlet(spec, instance, listener);
                break;

            case UserDetail:
                portlet = createUserDetailPortlet(spec, instance, listener);
                break;

            case CommunityDetail:
                portlet = createCommunityDetailPortlet(spec, instance, listener);
                break;

            case InstanceSocialActivity:
            	if (!instance.isTransient()) {
                    portlet = createInstanceSocialActivityPortlet(spec, instance, listener);
            	}
                break;

            case SearchCriteria:
                portlet = createSearchCriteriaPortlet(spec, instance, autoLayout);
                break;

            case RichText:
                portlet = createRichTextPortlet(spec);
                break;

            default:
                break;
        }
        
        return portlet;
    }

    private static PortletContainer createRichTextPortlet(final PortletSpecification spec) {
        RichTextPortlet portlet = new RichTextPortlet(spec);
        return portlet;
    }

    private static PortletContainer createSearchCriteriaPortlet(final PortletSpecification spec, final DataInstance instance, boolean autoLayout)
    {
        final SearchCriteriaPortlet portlet;

        HeaderType type = HeaderType.None;
        if (spec.isShowHeader()) {
            type = HeaderType.Shaded;
        }

        portlet = new SearchCriteriaPortlet(spec, type, 300);
                    
        Serializable s = spec.getProperty("height");
        if (s != null)
        {
            int height = Integer.parseInt(s.toString());
            portlet.setHeight(height);
        }
        
        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    try
                    {
                        SearchCriteria criteria = ((NamedSearch)instance).getSearchCriteria();
                        portlet.setCriteria(criteria);
                    }
                    catch (Exception e)
                    {
                        
                    }
                }
            }
        );

        return portlet;
    }
    
    private static PortletContainer createUserMessagesPortlet(final PortletSpecification spec, final User user,
    		boolean autoLayout, InstanceUpdateListener listener) {

    	final UserMessagesPortlet portlet;

        portlet = new UserMessagesPortlet(spec, listener);

        Serializable s = spec.getProperty("height");
        if (s != null)
        {
            int height = Integer.parseInt(s.toString());
            portlet.setHeight(height);
        }

        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    portlet.setUser(user, UpdateReason.InstanceChange);
                }
            }
        );

        return portlet;
    }
    
    private static PortletContainer createInstanceFieldPortlet(final PortletSpecification spec, final DataInstance instance,
            boolean autoLayout, InstanceUpdateListener listener)
    {
        final TypeFieldGroupsPortlet portlet;

        HeaderType type = HeaderType.None;
        if (spec.isShowHeader()) {
            type = HeaderType.Shaded;
        }

        portlet = new TypeFieldGroupsPortlet(spec, listener);

        Serializable s = spec.getProperty("height");
        if (s != null)
        {
            int height = Integer.parseInt(s.toString());
            portlet.setHeight(height);
        }

        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    portlet.setDataInstance(instance, UpdateReason.InstanceChange);
                }
            }
        );

        return portlet;
    }

    private static PortletContainer createImageGalleryPortlet(final PortletSpecification spec, final DataInstance instance,
            boolean autoLayout, double width, InstanceUpdateListener listener)
    {
        final ImageGalleryPortlet portlet;

        if (autoLayout)
        {
            portlet = new ImageGalleryPortlet(spec, spec.isShowHeader(), (int)width, listener);
        }
        else
        {
            portlet = new ImageGalleryPortlet(spec, spec.isShowHeader(), (int)width, listener);
        }

        //portlet.collapse();

        Serializable s = spec.getProperty("height");
        if (s != null)
        {
            int height = Integer.parseInt(s.toString());
            //portlet.setHeight(height);
        }

        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    portlet.setDataInstance(instance, UpdateReason.InstanceChange);
                }
            }
        );

        return portlet;
    }

    private static PortletContainer createFileGalleryPortlet(final PortletSpecification spec, final DataInstance instance,
            boolean autoLayout, double width, InstanceUpdateListener listener)
    {
        final FileGalleryPortlet portlet;

        if (autoLayout)
        {
            portlet = new FileGalleryPortlet(spec, spec.isShowHeader(), (int)width, listener);
        }
        else
        {
            portlet = new FileGalleryPortlet(spec, spec.isShowHeader(), (int)width, listener);
        }

        //portlet.collapse();

        Serializable s = spec.getProperty("height");
        if (s != null)
        {
            int height = Integer.parseInt(s.toString());
            //portlet.setHeight(height);
        }

        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    portlet.setDataInstance(instance, UpdateReason.InstanceChange);
                }
            }
        );

        return portlet;
    }

    private static PortletContainer createInstanceReferrersPortlet(final PortletSpecification spec, final DataInstance instance,
            double width)
    {
        final InstanceReferrersPortlet portlet;

        portlet = new InstanceReferrersPortlet(spec, (int)width);

        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    portlet.setDataInstance(instance, UpdateReason.InstanceChange);
                }
            }
        );

        return portlet;
    }

    private static PortletContainer createInstanceHeaderPortlet(final PortletSpecification spec, final DataInstance instance, 
            boolean autoLayout, InstanceUpdateListener listener)
    {
        final InstanceHeaderPortlet portlet;
        
        HeaderType type = HeaderType.Bordered;

        portlet = new InstanceHeaderPortlet(spec, type, listener);
                    
        Serializable s = spec.getProperty("height");
        if (s != null)
        {
            int height = Integer.parseInt(s.toString());
            //portlet.setHeight(height);
        }

        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    portlet.setDataInstance(instance, UpdateReason.InstanceChange);
                }
            }
        );

        return portlet;
    }
    
    private static PortletContainer createInstanceSocialActivityPortlet(final PortletSpecification spec, final DataInstance instance,
            InstanceUpdateListener listener)
    {
        final InstanceSocialActivityPortlet portlet;
        
        HeaderType type = HeaderType.None;
        if (spec.isShowHeader()) {
            type = HeaderType.Shaded;
        }

        portlet = new InstanceSocialActivityPortlet(spec, type, listener);

        Serializable s = spec.getProperty("height");
        if (s != null)
        {
            int height = Integer.parseInt(s.toString());
            //portlet.setHeight(height);
        }

        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    portlet.setDataInstance(instance, UpdateReason.InstanceChange);
                }
            }
        );

        return portlet;
    }

    private static PortletContainer createReviewDetailPortlet(final PortletSpecification spec, final DataInstance instance,
            InstanceUpdateListener listener) {

        final ReviewDetailPortlet portlet;

        HeaderType type = HeaderType.None;
        if (spec.isShowHeader()) {
            type = HeaderType.Shaded;
        }

        portlet = new ReviewDetailPortlet(spec, type, listener);

        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    portlet.setDataInstance(instance);
                }
            }
        );

        return portlet;
    }

    private static PortletContainer createNamedSearchDetailPortlet(final PortletSpecification spec, final DataInstance instance,
            InstanceUpdateListener listener) {

        final NamedSearchDetailPortlet portlet;

        HeaderType type = HeaderType.None;
        if (spec.isShowHeader()) {
            type = HeaderType.Shaded;
        }

        portlet = new NamedSearchDetailPortlet(spec, type, listener);

        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    portlet.setDataInstance(instance);
                }
            }
        );

        return portlet;
    }

    private static PortletContainer createAnalyticDetailPortlet(final PortletSpecification spec, final DataInstance instance,
            InstanceUpdateListener listener) {

        final AnalyticDetailPortlet portlet;

        HeaderType type = HeaderType.None;
        if (spec.isShowHeader()) {
            type = HeaderType.Shaded;
        }

        portlet = new AnalyticDetailPortlet(spec, type, listener);

        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    portlet.setDataInstance(instance);
                }
            }
        );

        return portlet;
    }

    private static PortletContainer createFileDetailPortlet(final PortletSpecification spec, final DataInstance instance,
            InstanceUpdateListener listener) {

        final FileDetailPortlet portlet;

        HeaderType type = HeaderType.None;
        if (spec.isShowHeader()) {
            type = HeaderType.Shaded;
        }

        portlet = new FileDetailPortlet(spec, type, listener);

        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    portlet.setDataInstance(instance);
                }
            }
        );

        return portlet;
    }

    private static PortletContainer createImageDetailPortlet(final PortletSpecification spec, final DataInstance instance,
            InstanceUpdateListener listener) {

        final ImageDetailPortlet portlet;

        HeaderType type = HeaderType.None;
        if (spec.isShowHeader()) {
            type = HeaderType.Shaded;
        }

        portlet = new ImageDetailPortlet(spec, type, listener);

        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    portlet.setDataInstance(instance);
                }
            }
        );

        return portlet;
    }

    private static PortletContainer createImageRendererPortlet(final PortletSpecification spec) {

        final ImageRendererPortlet portlet;

        portlet = new ImageRendererPortlet(spec);

        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    InstanceId instanceId = InstanceId.create(spec.getProperty("instanceId").toString());
                    portlet.execute(instanceId);
                }
            }
        );

        return portlet;
    }

    private static PortletContainer createDiscussionDetailPortlet(final PortletSpecification spec, final DataInstance instance,
            InstanceUpdateListener listener) {

        final DiscussionDetailPortlet portlet;

        HeaderType type = HeaderType.None;
        if (spec.isShowHeader()) {
            type = HeaderType.Shaded;
        }

        portlet = new DiscussionDetailPortlet(spec, type, listener);

        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    portlet.setDataInstance(instance, UpdateReason.InstanceChange);
                }
            }
        );

        return portlet;
    }

    private static PortletContainer createTagDetailPortlet(final PortletSpecification spec, final DataInstance instance,
            InstanceUpdateListener listener) {

        final TagDetailPortlet portlet;

        HeaderType type = HeaderType.None;
        if (spec.isShowHeader()) {
            type = HeaderType.Shaded;
        }

        portlet = new TagDetailPortlet(spec, type, listener);

        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    portlet.setDataInstance(instance);
                }
            }
        );

        return portlet;
    }

    private static PortletContainer createDataTypeDetailPortlet(final PortletSpecification spec, final DataInstance instance,
            InstanceUpdateListener listener) {

        final DataTypeDetailPortlet portlet;

        HeaderType type = HeaderType.None;
        if (spec.isShowHeader()) {
            type = HeaderType.Shaded;
        }

        portlet = new DataTypeDetailPortlet(spec, type, listener);

        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    portlet.setDataInstance(instance);
                }
            }
        );

        return portlet;
    }

    private static PortletContainer createUserDetailPortlet(final PortletSpecification spec, final DataInstance instance,
            InstanceUpdateListener listener) {

        final UserDetailPortlet portlet;

        HeaderType type = HeaderType.None;
        if (spec.isShowHeader()) {
            type = HeaderType.Shaded;
        }

        portlet = new UserDetailPortlet(spec, type, listener);

        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    portlet.setDataInstance(instance);
                }
            }
        );

        return portlet;
    }

    private static PortletContainer createCommunityDetailPortlet(final PortletSpecification spec, final DataInstance instance,
            InstanceUpdateListener listener) {

        final CommunityDetailPortlet portlet;

        HeaderType type = HeaderType.None;
        if (spec.isShowHeader()) {
            type = HeaderType.Shaded;
        }

        portlet = new CommunityDetailPortlet(spec, type, listener);

        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    portlet.setDataInstance(instance);
                }
            }
        );

        return portlet;
    }

    private static PortletContainer createDetailPagePortlet(final PortletSpecification spec, final DataInstance instance) {
        HeaderType type = HeaderType.None;
        if (spec.isShowHeader()) {
            type = HeaderType.Shaded;
        }
 
        final DetailPagePortlet portlet = new DetailPagePortlet(spec);

        DeferredCommand.addCommand(
            new Command() {
                public void execute() {
                    portlet.setDataInstance(instance, UpdateReason.InstanceChange);
                }
            }
        );

        return portlet;
    }
    
    private static PortletContainer createShapeFieldsPortlet(final PortletSpecification spec, final DataInstance instance,
    		InstanceUpdateListener listener) {
        HeaderType type = HeaderType.None;
        if (spec.isShowHeader()) {
            type = HeaderType.Shaded;
        }
 
        final TypeFieldsPortlet portlet = new TypeFieldsPortlet(spec, listener);

        DeferredCommand.addCommand(
            new Command() {
                public void execute() {
                	Object obj = spec.getProperty("shapeId");
                	if (obj != null) {
                        portlet.setDataInstance(instance, obj.toString());
                	} else {
                		portlet.setDataInstance(instance, instance.getShapes().get(0));
                	}
                }
            }
        );

        return portlet;
    }

    private static PortletContainer createFieldDataChartPortlet(final PortletSpecification spec, final DataInstance instance) {
        HeaderType type = HeaderType.None;
        if (spec.isShowHeader()) {
            type = HeaderType.Shaded;
        }
 
        final FieldDataChartPortlet portlet = new FieldDataChartPortlet(spec);

        DeferredCommand.addCommand(
            new Command() {
                public void execute() {
                    portlet.setDataInstance(instance, UpdateReason.InstanceChange);
                }
            }
        );

        return portlet;
    }
    
    private static PortletContainer createMultiChartPortlet(final PortletSpecification spec, final DataInstance instance) {
        HeaderType type = HeaderType.None;
        if (spec.isShowHeader()) {
            type = HeaderType.Shaded;
        }
 
        final MultiChartPortlet portlet = new MultiChartPortlet(spec);

        DeferredCommand.addCommand(
            new Command() {
                public void execute() {
                    portlet.setDataInstance(instance, UpdateReason.InstanceChange);
                }
            }
        );

        return portlet;
    }
    
    private static PortletContainer createPagePortlet(final PortletSpecification spec, final DataInstance instance)
    {
        final PagePortlet portlet;

        HeaderType type = HeaderType.None;
        if (spec.isShowHeader()) {
            type = HeaderType.Shaded;
        }
 
        portlet = new PagePortlet(spec);

        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    portlet.setDataInstance(instance, UpdateReason.InstanceChange);

                    Serializable s = spec.getProperty("height");
                    if (s != null)
                    {
                        int height = Integer.parseInt(s.toString());
                        portlet.setHeight(height);
                    }

                }
            }
        );

        return portlet;
    }

    private static PortletContainer createDataInstancePortlet(final PortletSpecification spec, boolean autoLayout)
    {
        final DataInstancePortlet portlet;
        
        HeaderType type = HeaderType.None;
        if (spec.isShowHeader()) {
            type = HeaderType.Shaded;
        }

        portlet = new DataInstancePortlet(spec, type);
        
        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    InstanceId instanceId = InstanceId.create(spec.getProperty("instanceId").toString());
                    String fieldName = spec.getProperty("fieldName").toString();
                    portlet.setDataInstance(instanceId, fieldName);
                    
                    Serializable s = spec.getProperty("height");
                    if (s != null)
                    {
                        int height = Integer.parseInt(s.toString());
                        portlet.setHeight(height);
                    }
                    
                }
            }
        );
        
        return portlet;
    }
    
    private static PortletContainer createNamedSearchPortlet(final PortletSpecification spec, boolean autoLayout)
    {
        final NamedSearchPortlet portlet;

        portlet = new NamedSearchPortlet(spec);

        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    InstanceId instanceId = InstanceId.create(spec.getProperty("instanceId").toString());
                    portlet.execute(instanceId);
                }
            }
        );

        return portlet;
    }

    private static PortletContainer createAnalyticPortlet(final PortletSpecification spec, boolean autoLayout)
    {
        final AnalyticPortlet portlet;

        portlet = new AnalyticPortlet(spec);

        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    InstanceId instanceId = InstanceId.create(spec.getProperty("instanceId").toString());
                    portlet.execute(instanceId);
                }
            }
        );

        return portlet;
    }
}
