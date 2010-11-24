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

import com.paxxis.chime.client.common.Comment;
import com.paxxis.chime.client.common.CommentsBundle;
import com.paxxis.chime.client.common.Community;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceRequest.SortOrder;
import com.paxxis.chime.client.common.Discussion;
import com.paxxis.chime.client.common.DiscussionsBundle;
import com.paxxis.chime.client.common.Review;
import com.paxxis.chime.client.common.ReviewsBundle;
import com.paxxis.chime.client.common.Scope;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.service.Tools;
import com.paxxis.cornerstone.base.InstanceId;
import com.paxxis.cornerstone.database.DataSet;
import com.paxxis.cornerstone.database.DatabaseConnection;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class ScopeUtils 
{
    private ScopeUtils()
    {}
    
    private static boolean isApplied(InstanceId instanceId, Scope scope, DatabaseConnection database) throws Exception
    {
        String sql = "select 1 from " + Tools.getTableSet() + "_community where instance_id = '" + instanceId +
                        "' and community_id = '" + scope.getCommunity().getId() + "'";

        DataSet dataSet = database.getDataSet(sql, true);
        boolean isApplied = dataSet.next();
        dataSet.close();
        
        return isApplied;
    }

    public static DataInstance modifyScopes(InstanceId instanceId, User user, List<Scope> scopes, DatabaseConnection database) throws Exception {
        database.startTransaction();

        DataInstance result = null;
        try {
            // first clear all existing scopes
            String sql = "delete from " + Tools.getTableSet() + "_community where instance_id = '" + instanceId + "'";
            database.executeStatement(sql);
            
            // now apply the new scopes
            for (Scope scope : scopes) {
                applyScope(instanceId, scope, database);
            }

            // update the visibility scope for the reviews, comments, and discussions
            ReviewsBundle reviewsBundle = ReviewUtils.getReviews(instanceId, null, null, SortOrder.ByMostRecentEdit, database);
            List<Review> reviews = reviewsBundle.getReviews();
            for (Review review : reviews) {
                DataInstance r = DataInstanceUtils.getInstance(review.getId(), user, database, true, true);
                List<Scope> reviewScopes = r.getSocialContext().getScopes();
                upgradeVisibility(reviewScopes, scopes);
                modifyScopes(r.getId(), user, reviewScopes, database);
            }

            CommentsBundle commentsBundle = CommentUtils.getComments(instanceId, null, null, SortOrder.ByMostRecentEdit, database);
            List<Comment> comments = commentsBundle.getComments();
            for (Comment comment : comments) {
                DataInstance c = DataInstanceUtils.getInstance(comment.getId(), user, database, true, true);
                List<Scope> commentScopes = c.getSocialContext().getScopes();
                upgradeVisibility(commentScopes, scopes);
                modifyScopes(c.getId(), user, commentScopes, database);
            }

            DiscussionsBundle discussionsBundle = DiscussionUtils.getDiscussions(instanceId, null, database);
            List<Discussion> discussions = discussionsBundle.getDiscussions();
            for (Discussion discussion : discussions) {
                DataInstance d = DataInstanceUtils.getInstance(discussion.getId(), user, database, true, true);
                List<Scope> discussionScopes = d.getSocialContext().getScopes();
                upgradeVisibility(discussionScopes, scopes);
                modifyScopes(d.getId(), user, discussionScopes, database);
            }

            result = DataInstanceUtils.getInstance(instanceId, user, database, true, false);
            database.commitTransaction();
        } catch (Exception e) {
            database.rollbackTransaction();
            throw new Exception(e.getMessage());
        }

        return result;
    }
    
    /**
     * the visibility scope in scopes is upgraded to match the visibility in newScopes
     * @param scopes
     * @param newScopes
     */
    private static void upgradeVisibility(List<Scope> scopes, List<Scope> newScopes) {
    	Scope readScope = null;
    	for (Scope scope : scopes) {
            if (scope.getPermission() == Scope.Permission.R) {
                readScope = scope;
                break;
            }
    	}

    	if (readScope == null) {
            for (Scope scope : scopes) {
                if (scope.getPermission() == Scope.Permission.RU || scope.getPermission() == Scope.Permission.RUC) {
                    readScope = scope;
                    break;
                }
            }
    	}

    	Scope editScope = null;
    	for (Scope scope : scopes) {
            if (scope.getPermission() == Scope.Permission.RU || scope.getPermission() == Scope.Permission.RUC) {
                editScope = scope;
                break;
            }
    	}

        Scope newReadScope = null;
    	for (Scope scope : scopes) {
            if (scope.getPermission() == Scope.Permission.R) {
                newReadScope = scope;
                break;
            }
    	}

    	if (newReadScope == null) {
            for (Scope scope : scopes) {
                if (scope.getPermission() == Scope.Permission.RU || scope.getPermission() == Scope.Permission.RUC) {
                    newReadScope = scope;
                    break;
                }
            }
    	}

        if (readScope == editScope) {
            // just add the visibility scope
            scopes.add(newReadScope);
        } else {
            // replace the visibility scope
            int cnt = scopes.size();
            for (int i = 0; i < cnt; i++) {
                if (scopes.get(i) == readScope) {
                    // this is the one
                    scopes.set(i, newReadScope);
                    break;
                }
            }
        }

    }

    public static void applyScope(InstanceId instanceId, Scope scope, DatabaseConnection database) throws Exception
    {
        if (isApplied(instanceId, scope, database))
        {
            String sql = "update " + Tools.getTableSet() + "_community set permissions = '" + scope.getPermission() +
                            "' where instance_id = '" + instanceId + "' and community_id = '" + scope.getCommunity().getId() + "'";

            database.executeStatement(sql);

        }
        else
        {
            InstanceId id = Tools.getNewId(Tools.DEFAULT_EXTID);
            String sql = "insert into " + Tools.getTableSet() + "_community (id, instance_id, community_id, permissions) " +
                    "values ('" + id + "', '" + instanceId + "', '" + scope.getCommunity().getId() + "', '" + scope.getPermission() + "')";
            database.executeStatement(sql);
        }
    }
    
    public static List<Scope> getScopes(InstanceId instanceId, DatabaseConnection database) throws Exception
    {
        String sql = "select community_id, permissions from " + Tools.getTableSet() + "_community where instance_id = '" + instanceId + "'";

        List<Scope> scopes = new ArrayList<Scope>();
        
        DataSet dataSet = database.getDataSet(sql, true);
        while (dataSet.next())
        {
            String id = dataSet.getFieldValue("community_id").asString();
            String permissions = dataSet.getFieldValue("permissions").asString();

            Community community = new Community(InstanceId.create(id));
            Scope.Permission perm = Scope.Permission.valueOf(permissions);
            
            Scope scope = new Scope(community, perm);
            scopes.add(scope);
        }
        
        // if there are no scopes then this instance is scoped Globally
        if (scopes.isEmpty())
        {
            Scope scope = new Scope(Community.Global, Scope.Permission.R);
            scopes.add(scope);
        }

        dataSet.close();
        return scopes;
    }
}
