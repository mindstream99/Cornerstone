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

import com.paxxis.chime.data.CacheManager;
import com.paxxis.chime.client.common.Comment;
import com.paxxis.chime.client.common.Community;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataSocialContext;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.Discussion;
import com.paxxis.chime.client.common.NamedSearch;
import com.paxxis.chime.client.common.Dashboard;
import com.paxxis.chime.client.common.Folder;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.Review;
import com.paxxis.chime.client.common.Scope;
import com.paxxis.chime.client.common.Tag;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.database.DataSet;
import com.paxxis.chime.database.DatabaseConnection;
import java.io.File;
import java.util.List;
import java.util.UUID;
import org.apache.log4j.Logger;

/** 
 *
 * @author Robert Englander
 */
public class Tools {
    private static final Logger _logger = Logger.getLogger(Tools.class);

    public static final String DEFAULT_EXTID = "00";
    
    private static final String TABLESET = "Chime.DataInstance";

    private static final int LONGCHARS = 20;
    
    private Tools()
    {}

    public static String getTableSet() {
        return TABLESET;
    }

    public static String minNumeric()
    {
        return longToNumeric(Long.MIN_VALUE / 1000L);
    }
    
    public static String maxNumeric()
    {
        return longToNumeric(Long.MAX_VALUE / 1000L);
    }

    public static float numericToFloat(String val)
    {
        float v = numericToLong(val, 1L) / 1000.0f;
        return v;
    }
    
    public static String floatToNumeric(float val)
    {
        long v = (long)(val * 1000.0f);
        return longToNumeric(v, 1L);
    }

    public static long numericToLong(String val)
    {
        return numericToLong(val, 1000L);
    }
    
    private static long numericToLong(String val, long factor)
    {
        boolean isNegative = val.charAt(0) == '0';
        String part = val.substring(1);
        long value = Long.valueOf(part);
        if (isNegative)
        {
            value = (Long.MAX_VALUE - value);
        }
        
        value = value / factor;
        
        return value;
    }

    public static String longToNumeric(long val)
    {
        return longToNumeric(val, 1000L);
    }
    
    private static String longToNumeric(long val, long factor)
    {
        String pre = "1";

        val = val * factor;
        boolean isNegative = (val < 0L);
        if (isNegative)
        {
            val = Long.MAX_VALUE + val;
            pre = "0";
        }
        
        String x = Long.toString(val);
        int need = (LONGCHARS - x.length());
        if (need > 0)
        {
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < need; i++)
            {
                buf.append('0');
            }
            
            x = buf.toString() + x;
        }
        
        return (pre + x);
    }

    /**
     * A utility method for deleting directory contents.
     *
     */
    public static void deleteDirectory(File path) {
        if( path.exists() ) {
            File[] files = path.listFiles();
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                }
                else {
                    files[i].delete();
                }
            }
        }
    }

    public static Class getClass(Shape type) {

        if (type.getId().equals(Shape.TAG_ID))
        {
            return Tag.class;
        }
        else if (type.getId().equals(Shape.USER_ID))
        {
            return User.class;
        }
        else if (type.getId().equals(Shape.SHAPE_ID))
        {
            return Shape.class;
        }
        else if (type.getId().equals(Shape.COMMUNITY_ID))
        {
            return Community.class;
        }
        else if (type.getId().equals(Shape.COMMENT_ID))
        {
            return Comment.class;
        }
        else if (type.getId().equals(Shape.DISCUSSION_ID))
        {
            return Discussion.class;
        }
        else if (type.getId().equals(Shape.REVIEW_ID))
        {
            return Review.class;
        }
        else if (type.getId().equals(Shape.NAMEDSEARCH_ID))
        {
            return NamedSearch.class;
        }
        else if (type.getId().equals(Shape.DASHBOARD_ID))
        {
            return Dashboard.class;
        }
        else if (type.getId().equals(Shape.FOLDER_ID))
        {
            return Folder.class;
        }
        else
        {
            return DataInstance.class;
        }
    }

    public static Class getClass(List<Shape> shapes) {

        return getClass(shapes.get(0));
    }
    
    public static boolean isDataVisible(DataInstance instance, User user)
    {
        boolean isVisible = false;
        DataSocialContext context = instance.getSocialContext();
        List<Scope> scopes = context.getScopes();
        
        if (user == null) {
            // nope
        } else if (user.isAdmin()) {
            isVisible = true;
        } else {
            for (Scope scope : scopes) {
                if (scope.isGlobalCommunity() ||
                        (scope.getCommunity().getId().equals(user.getId())) ) {
                    isVisible = true;
                } else {
                    List<Community> communities = user.getCommunities();
                    for (Community community : communities) {
                        if (community.getId().equals(scope.getCommunity().getId())) {
                            isVisible = true;
                            break;
                        }
                    }

                    if (isVisible) {
                        break;
                    }
                }
            }
        }
        
        return isVisible;
    }
    
    public static void validateUser(User user) throws SessionValidationException
    {
        if (user != null)
        {
            _logger.info("Validating user " + user.getName() + " with token: " + user.getSessionToken());
            
            if (null == CacheManager.instance().getUserSession(user))
            {
                // has it already moved into the expiring session cache?
                if (CacheManager.instance().isExpiringUserSession(user)) {
                    CacheManager.instance().putUserSession(user);
                    CacheManager.instance().removeExpiringUserSession(user);
                } else {
                    // this is a bogus or expired session
                    throw new SessionValidationException("Invalid or expired user session");
                }
            }
        }
    }

    public static InstanceId getNewId(String source) {
        UUID uuid = UUID.randomUUID();
        String converted = uuid.toString().replaceAll("-", "");
        return InstanceId.create(converted + source);
    }

    public static String getLimitClause(DatabaseConnection db, long limit) {
        String clause;
        if (db.getConnectionURL().startsWith("jdbc:mysql:")) {
            clause = " limit " + limit;
        } else {
            clause = " fetch first " + limit + " rows only ";
        }

        return clause;
    }
}
