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
import com.paxxis.chime.client.common.DataSocialContext;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.Tag;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.client.common.portal.PortalTemplate;
import java.util.List;
import javax.transaction.RollbackException;
import javax.transaction.TransactionManager;
import org.apache.log4j.Logger;
import org.jboss.cache.Cache;
import org.jboss.cache.CacheFactory;
import org.jboss.cache.DefaultCacheFactory;
import org.jboss.cache.Fqn;
import org.jboss.cache.Node;
import org.jboss.cache.notifications.annotation.CacheListener;
import org.jboss.cache.notifications.annotation.NodeEvicted;
import org.jboss.cache.notifications.event.Event;
import org.jboss.cache.notifications.event.NodeEvictedEvent;

/** 
 *
 * @author Robert Englander
 */
public class CacheManager {
    private static final Logger _logger = Logger.getLogger(CacheManager.class);

    @CacheListener
    public class SessionEvictionListener
    {
        @NodeEvicted
        public void handleEvent(Event event)
        {
            if (event.getType() == Event.Type.NODE_EVICTED) {
                NodeEvictedEvent evt = (NodeEvictedEvent)event;

                // get the user object and move it to the pending timeout region
                if (evt.getFqn().toString().startsWith("/UserSession/")) {
                    Node node = _cache.getRoot().getChild(evt.getFqn());
                    if (node != null) {
                        Object obj = node.get("object");
                        if (obj != null) {
                            User user = (User)obj;
                            putExpiringUserSession(user);
                        }
                    }
                }
            }
        }
    }

    private static CacheManager _instance = null;
    
    private Cache _cache = null;
    
    private CacheManager()
    {
        try
        {
            CacheFactory factory = new DefaultCacheFactory();
            _cache = factory.createCache("ChimeServiceCache.xml");
            _cache.addCacheListener(new SessionEvictionListener());
        }
        catch (Exception e)
        {
            int x = 1;
        }
    }
    
    public static synchronized CacheManager instance()
    {
        if (_instance == null)
        {
            _instance = new CacheManager();
        }
        
        return _instance;
    }
    
    public void remove(DataInstance instance)
    {
        //*
        TransactionManager mgr = _cache.getConfiguration().getRuntimeConfig().getTransactionManager();
        InstanceId instanceId = instance.getId();

        try
        {
            mgr.begin();
            Fqn fqn = Fqn.fromString("/DataInstance/" + instanceId);
            boolean removed = _cache.removeNode(fqn);
            fqn = Fqn.fromString("/TagList/" + instanceId);
            removed = _cache.getRoot().removeChild(fqn);
            mgr.commit();
        }
        catch (Exception e)
        {
            _logger.error("CacheManager.remove.", e);
            try
            {
                mgr.rollback();
            }
            catch (Exception e2)
            {
            }
        }
        //*/
    }
    
    public void put(DataInstance instance)
    {
        TransactionManager mgr = _cache.getConfiguration().getRuntimeConfig().getTransactionManager();
        
        try
        {
            mgr.begin();
            Fqn fqn = Fqn.fromString("/DataInstance/" + instance.getId());
            Node node = _cache.getRoot().addChild(fqn);
            node.put("object", instance);
            mgr.commit();
        } catch (RollbackException e) {
            _logger.error("CacheManager.put", e);
        } catch (Exception e) {
            _logger.error("CacheManager.put", e);
            try
            {
                mgr.rollback();
            }
            catch (Exception e2)
            {
            }
        }
    }

    public DataSocialContext getSocialContext(DataInstance instance, User user) {
        
        if (user == null) {
            return null;
        }
        
        try {
            String fqnString = "/SocialContext/" +
                    instance.getId() + "/" + user.getId();
            Fqn fqn = Fqn.fromString(fqnString);
            Node node = _cache.getRoot().getChild(fqn);
            if (node == null) {
                return null;
            }

            Object obj = node.get("object");
            if (obj == null) {
                return null;
            }

            return (DataSocialContext)obj;
        }
        catch (Exception e)
        {
            _logger.error("CacheManager.getSocialContext", e);
            return null;
        }
    }

    public void putSocialContext(DataInstance instance) {
        DataSocialContext context = instance.getSocialContext();
        if (context != null) {
            User user = context.getUser();
            if (user != null) {
                TransactionManager mgr = _cache.getConfiguration().getRuntimeConfig().getTransactionManager();

                try {
                    mgr.begin();

                    // the social context is changed, so any user social contexts in the
                    // cache are no longer valid and need to be cleared
                    String fqnString = "/SocialContext/" +
                            instance.getId();
                    Fqn fqn = Fqn.fromString(fqnString);
                    boolean removed = _cache.getRoot().removeChild(fqn);

                    // now the new one
                    fqnString += "/" + user.getId();
                    fqn = Fqn.fromString(fqnString);

                    Node node = _cache.getRoot().addChild(fqn);
                    node.put("object", context);

                    mgr.commit();
                } catch (RollbackException e) {
                    _logger.error("CacheManager.putSocialContext", e);
                } catch (Exception e)
                {
                    _logger.error("CacheManager.putSocialContext", e);
                    try
                    {
                        mgr.rollback();
                    }
                    catch (Exception e2)
                    {
                    }
                }
            }
        }
    }

    public PortalTemplate getPortalTemplate(InstanceId typeId, InstanceId instanceId)
    {
        try
        {
            String fqnString = "/Template/" + typeId + "/" + instanceId;
            Fqn fqn = Fqn.fromString(fqnString);
            Node node = _cache.getRoot().getChild(fqn);
            if (node == null)
            {
                return null;
            }

            Object obj = node.get("object");
            if (obj == null)
            {
                return null;
            }

            return (PortalTemplate)obj;
        }
        catch (Exception e)
        {
            _logger.error("CacheManager.getPortalTemplate", e);
            return null;
        }
    }

    public void putPortalTemplate(InstanceId typeId, InstanceId instanceId, PortalTemplate template)
    {
        TransactionManager mgr = _cache.getConfiguration().getRuntimeConfig().getTransactionManager();
        
        try
        {
            mgr.begin();
            
            String fqnString = "/Template/" + typeId + "/" + instanceId;
            Fqn fqn = Fqn.fromString(fqnString);
            Node node = _cache.getRoot().addChild(fqn);
            node.put("object", template);

            mgr.commit();
        } catch (RollbackException e) {
            _logger.error("CacheManager.putPortalTemplate", e);
        } catch (Exception e) {
            _logger.error("CacheManager.putPortalTemplate", e);
            try
            {
                mgr.rollback();
            }
            catch (Exception e2)
            {
            }
        }
    }
    
    public void putUserSession(User user)
    {
        if (user == null)
        {
            return;
        }
        
        TransactionManager mgr = _cache.getConfiguration().getRuntimeConfig().getTransactionManager();
        
        try
        {
            mgr.begin();
            
            String fqnString = "/UserSession/" + user.getId() + "/" + user.getSessionToken();
            Fqn fqn = Fqn.fromString(fqnString);
            Node node = _cache.getRoot().addChild(fqn);
            node.put("object", user);

            mgr.commit();
        } catch (RollbackException e) {
            _logger.error("CacheManager.putUserSession: " + e);
        } catch (Exception e)
        {
            _logger.error("CacheManager.putUserSession: " + e);
            try
            {
                mgr.rollback();
            }
            catch (Exception e2)
            {
            }
        }

        User u = getUserSession(user);
        int x = 1;
    }

    public User getUserSession(User user)
    {
        if (user == null)
        {
            return null;
        }
        
        try
        {
            String fqnString = "/UserSession/" + user.getId() + "/" + user.getSessionToken();
            Fqn fqn = Fqn.fromString(fqnString);
            Node node = _cache.getRoot().getChild(fqn);
            if (node == null)
            {
                return null;
            }

            Object obj = node.get("object");
            if (obj == null)
            {
                return null;
            }

            return (User)obj;
        }
        catch (Exception e)
        {
            _logger.error("CacheManager.getUserSession: " + e);
            return null;
        }
    }

    public void putExpiringUserSession(User user)
    {
        if (user == null)
        {
            return;
        }

        TransactionManager mgr = _cache.getConfiguration().getRuntimeConfig().getTransactionManager();

        try
        {
            mgr.begin();

            String fqnString = "/ExpiringUserSession/" + user.getId() + "/" + user.getSessionToken();
            Fqn fqn = Fqn.fromString(fqnString);
            Node node = _cache.getRoot().addChild(fqn);
            node.put("object", user);

            mgr.commit();
        } catch (RollbackException e) {
            _logger.error("CacheManager.putExpiringUserSession: " + e);
        } catch (Exception e)
        {
            _logger.error("CacheManager.putExpiringUserSession: " + e);
            try
            {
                mgr.rollback();
            }
            catch (Exception e2)
            {
            }
        }

        User u = getUserSession(user);
        int x = 1;
    }

    public User getExpiringUserSession(User user)
    {
        if (user == null)
        {
            return null;
        }

        try
        {
            String fqnString = "/ExpiringUserSession/" + user.getId() + "/" + user.getSessionToken();
            Fqn fqn = Fqn.fromString(fqnString);
            Node node = _cache.getRoot().getChild(fqn);
            if (node == null)
            {
                return null;
            }

            Object obj = node.get("object");
            if (obj == null)
            {
                return null;
            }

            return (User)obj;
        }
        catch (Exception e)
        {
            _logger.error("CacheManager.getExpiringUserSession: " + e);
            return null;
        }
    }

    public boolean isExpiringUserSession(User user)
    {
        if (user == null) {
            return false;
        }

        try {
            String fqnString = "/ExpiringUserSession/" + user.getId() + "/" + user.getSessionToken();
            Fqn fqn = Fqn.fromString(fqnString);
            boolean has = _cache.getRoot().hasChild(fqn);
            return has;
        }
        catch (Exception e)
        {
            _logger.error("CacheManager.isExpiringUserSession: " + e);
            return false;
        }
    }

    public boolean hasUserSession(User user)
    {
        if (user == null) {
            return false;
        }

        try {
            String fqnString = "/UserSession/" + user.getId() + "/" + user.getSessionToken();
            Fqn fqn = Fqn.fromString(fqnString);
            boolean has = _cache.getRoot().hasChild(fqn);
            return has;
        }
        catch (Exception e)
        {
            _logger.error("CacheManager.hasUserSession: " + e);
            return false;
        }
    }

    public void removeUserSession(User user)
    {
        if (user == null)
        {
            return;
        }

        TransactionManager mgr = _cache.getConfiguration().getRuntimeConfig().getTransactionManager();

        try
        {
            mgr.begin();
            String fqnString = "/UserSession/" + user.getId() + "/" + user.getSessionToken();
            boolean removed = _cache.removeNode(fqnString);
            mgr.commit();
        }
        catch (Exception e)
        {
            _logger.error("CacheManager.removeUserSession: " + e);
            try
            {
                mgr.rollback();
            }
            catch (Exception e2)
            {
            }
        }
    }

    public void removeExpiringUserSession(User user)
    {
        if (user == null)
        {
            return;
        }

        TransactionManager mgr = _cache.getConfiguration().getRuntimeConfig().getTransactionManager();

        try
        {
            mgr.begin();
            String fqnString = "/ExpiringUserSession/" + user.getId() + "/" + user.getSessionToken();
            boolean removed = _cache.removeNode(fqnString);
            mgr.commit();
        }
        catch (Exception e)
        {
            _logger.error("CacheManager.removeExpiringUserSession: " + e);
            try
            {
                mgr.rollback();
            }
            catch (Exception e2)
            {
            }
        }
    }

    public void putShape(DataInstance shape)
    {
        TransactionManager mgr = _cache.getConfiguration().getRuntimeConfig().getTransactionManager();
        
        try
        {
            mgr.begin();
            Fqn fqn = Fqn.fromString("/Shape/" + shape.getId());
            Node node = _cache.getRoot().addChild(fqn);
            node.put("object", shape);

            fqn = Fqn.fromString("/Shape/" + shape.getName());
            node = _cache.getRoot().addChild(fqn);
            node.put("object", shape);

            mgr.commit();
        } catch (Exception e) {
            _logger.error("CacheManager.putShape: " + e);
            try
            {
                mgr.rollback();
            }
            catch (Exception e2)
            {
            }
        }
    }

    public Shape getShape(InstanceId id)
    {
        try
        {
            Fqn fqn = Fqn.fromString("/Shape/" + id);
            Node node = _cache.getRoot().getChild(fqn);
            if (node == null)
            {
                return null;
            }

            Object obj = node.get("object");
            if (obj == null)
            {
                return null;
            }

            Shape instance = (Shape)obj;
            return instance;
        }
        catch (Exception e)
        {
            _logger.error("CacheManager.getShape: " + e);
            return null;
        }
    }

    public Shape getShape(String name)
    {
        try
        {
            Fqn fqn = Fqn.fromString("/Shape/" + name);
            Node node = _cache.getRoot().getChild(fqn);
            if (node == null)
            {
                return null;
            }

            Object obj = node.get("object");
            if (obj == null)
            {
                return null;
            }

            Shape instance = (Shape)obj;
            return instance;
        }
        catch (Exception e)
        {
            _logger.error("CacheManager.getShape: " + e);
            return null;
        }
    }

    public DataInstance get(InstanceId id)
    {
        try
        {
            Fqn fqn = Fqn.fromString("/DataInstance/" + id);
            Node node = _cache.getRoot().getChild(fqn);
            if (node == null)
            {
                return null;
            }

            Object obj = node.get("object");
            if (obj == null)
            {
                return null;
            }

            DataInstance instance = (DataInstance)obj;
            return instance;
        }
        catch (Exception e)
        {
            _logger.error("CacheManager.get: " + e);
            return null;
        }
    }
    
    public void putTags(InstanceId instanceId, InstanceId userId, List<Tag> tags)
    {
        TransactionManager mgr = _cache.getConfiguration().getRuntimeConfig().getTransactionManager();
        
        try
        {
            mgr.begin();
            Fqn fqn = Fqn.fromString("/TagList/" + instanceId + "/" + userId);
            Node node = _cache.getRoot().addChild(fqn);
            node.put("object", tags);
            mgr.commit();
        } catch (RollbackException e) {
            _logger.error("CacheManager.putTags: " + e);
        } catch (Exception e)
        {
            _logger.error("CacheManager.putTags: " + e);
            try
            {
                mgr.rollback();
            }
            catch (Exception e2)
            {
            }
        }
    }
    
    public List<Tag> getTags(InstanceId instanceId, InstanceId userId)
    {
        try
        {
            Fqn fqn = Fqn.fromString("/TagList/" + instanceId + "/" + userId);
            Node node = _cache.getRoot().getChild(fqn);
            if (node == null)
            {
                return null;
            }

            Object obj = node.get("object");
            if (obj == null)
            {
                return null;
            }

            List<Tag> tags = (List<Tag>)obj;
            return tags;
        }
        catch (Exception e)
        {
            _logger.error("CacheManager.get: " + e);
            return null;
        }
    }
}
