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
import com.paxxis.chime.client.common.Cursor;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstance.LockType;
import com.paxxis.chime.client.common.DataInstance.ReviewAction;
import com.paxxis.chime.client.common.DataInstance.TagAction;
import com.paxxis.chime.client.common.DataInstanceRequest.Operator;
import com.paxxis.chime.client.common.DataInstanceRequest.SortOrder;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.DataSocialContext;
import com.paxxis.chime.client.common.Discussion;
import com.paxxis.chime.client.common.DiscussionsBundle;
import com.paxxis.chime.client.common.FieldData;
import com.paxxis.chime.client.common.Folder;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.ReviewsBundle;
import com.paxxis.chime.client.common.Review;
import com.paxxis.chime.client.common.Scope;
import com.paxxis.chime.client.common.Tag;
import com.paxxis.chime.client.common.TagContext;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.client.common.UserMessagesBundle;
import com.paxxis.chime.client.common.UserSocialContext;
import com.paxxis.chime.client.common.portal.PortalTemplate;
import com.paxxis.chime.database.DataSet;
import com.paxxis.chime.database.DatabaseConnection;
import com.paxxis.chime.database.IDataValue;
import com.paxxis.chime.database.StringData;
import com.paxxis.chime.service.DataInstanceHelperFactory;
import com.paxxis.chime.service.Tools;
import com.paxxis.chime.data.VoteUtils.UserVote;
import com.paxxis.chime.extension.ChimeExtensionManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;


/**
 *
 * @author Robert Englanderert Englander
 */
public class DataInstanceUtils {
    private static final Logger _logger = Logger.getLogger(DataInstanceUtils.class);

    private static final String DIFFSEP = "{SEP}";
    private static final String PATTERNDIFFSEP = "\\{SEP\\}";
    private static final int ROWLIMIT = 1000;
    private static final int REVIEWLIMIT = 20;
    private static final int DISCUSSIONLIMIT = 20;
    
    public static final int USRMSGLIMIT = 20;
    
    
    private DataInstanceUtils()
    {}
    

    public static DataInstance setUpdated(DataInstance instance, User user, DatabaseConnection database) throws Exception {
        database.startTransaction();

        DataInstance result = null;

        try {
            String sql = "update Chime.DataInstance set expiration = null, updated = CURRENT_TIMESTAMP, updatedBy = '" +
                    user.getId() + "', updatedByName = '" + user.getName() +
                    "' where id = '" + instance.getId() + "'";
            database.executeStatement(sql);
            result = DataInstanceUtils.getInstance(instance.getId(), user, database, true, false);

            sql = "update Chime.RegisteredInterest set instance_name = '" +
                    instance.getName() + "', last_update = CURRENT_TIMESTAMP where instance_id ='"
                    + instance.getId() + "' and user_id <> '" + user.getId() + "'";
            database.executeStatement(sql);

            database.commitTransaction();
        } catch (Exception e) {
            database.rollbackTransaction();
            throw new Exception(e.getMessage());
        }

        return result;
    }

    public static void deleteInstance(DataInstance instance, User user, DatabaseConnection database) throws Exception
    {
        database.startTransaction();

        try
        {
            String tableName = Tools.getTableSet();
            InstanceId instanceId = instance.getId();
            String quotedInstanceId = "'" + instanceId + "'";

            /*
            DataSocialContext context = instance.getSocialContext();
            if (context == null)
            {
                instance = DataInstanceUtils.getInstance(instanceId, user, database, true, true);
                context = instance.getSocialContext();
            }
            */

            // delete the primary record
            String sql = "delete from " + tableName + " where id = " + quotedInstanceId;
            database.executeStatement(sql);

            // delete the vote record
            sql = "delete from " + tableName + "_positive where id = " + quotedInstanceId;
            database.executeStatement(sql);

            // delete the field data
            sql = "delete from " + tableName + "_Text where instance_id = " + quotedInstanceId;
            database.executeStatement(sql);

            sql = "delete from " + tableName + "_Number where instance_id = " + quotedInstanceId;
            database.executeStatement(sql);

            sql = "delete from " + tableName + "_Reference where instance_id = " + quotedInstanceId;
            database.executeStatement(sql);

            // delete history
            sql = "delete from " + tableName + "_History where instance_id = " + quotedInstanceId;
            database.executeStatement(sql);

            // delete attachments
            sql = "delete from " + tableName + "_Attachment where instance_id = " + quotedInstanceId;
            database.executeStatement(sql);

            // delete the community scope records
            sql = "delete from " + tableName + "_community where instance_id = " + quotedInstanceId;
            database.executeStatement(sql);

            // delete the tag and tag metrics records
            sql = "delete from " + tableName + "_tag where instance_id = " + quotedInstanceId;
            database.executeStatement(sql);

            sql = "delete from " + tableName + "_tag_metrics where instance_id = " + quotedInstanceId;
            database.executeStatement(sql);

            // the types
            sql = "delete from " + tableName + "_Type where instance_id = " + quotedInstanceId;
            database.executeStatement(sql);

            // the usage count for each tag needs to be reduced.  The amount to
            // decrement for each tag is the number of times the tag has been applied to the deleted data instance.
            /*
            List<TagContext> tagContexts = context.getTagContexts();
            for (TagContext tagContext : tagContexts)
            {
                String tagId = tagContext.getTag().getId();
                long count = tagContext.getUsageCount();
                sql = "update " + tableName + " set intVal = (intVal - " + count + ") where id = '" + tagId + "'";
                database.executeStatement(sql);

                List<Shape> types = instance.getShapes();
                for (Shape shape : types) {
                    // the User_tag_usage table has a usage count for each user/tag/types combination.  Each one must be decremented
                    // by 1 where the tag and types match each tag and the types of the deleted instance
                    sql = "update Chime.User_tag_usage set usageCount = (usageCount - 1) where tag_id = '" + tagId + "' and type_id = '" + shape.getId() + "'";
                    database.executeStatement(sql);

                    // the Type_tag_usage table next
                    sql = "update Chime.Type_tag_usage set usageCount = (usageCount - 1) where tag_id = '" + tagId + "' and type_id = '" + shape.getId() + "'";
                    database.executeStatement(sql);
                }
            }
            */
            
            // delete all field references to the deleted instance
            //sql = "delete from " + tableName + "_Reference where foreign_id = " + quotedInstanceId;
            //database.executeStatement(sql);

            // delete the reviews
            /*
            if (instance.getShapes().get(0).getCanReview()) {
                List<Review> reviews = ReviewUtils.getReviews(instanceId, null, null, SortOrder.ByMostRecentEdit, database).getReviews();
                for (Review review : reviews) {
                    DataInstanceUtils.deleteInstance(review, user, database);
                }
            }

            // delete the comments
            if (instance.getShapes().get(0).getCanComment()) {
                List<Comment> comments = CommentUtils.getComments(instanceId, null, null, SortOrder.ByMostRecentEdit, database).getComments();
                for (Comment comment : comments) {
                    DataInstanceUtils.deleteInstance(comment, user, database);
                }
            }

            // delete the discussions
            if (instance.getShapes().get(0).getCanDiscuss()) {
                List<Discussion> discussions = DiscussionUtils.getDiscussions(instanceId, null, database).getDiscussions();
                for (Discussion discussion : discussions) {
                    DataInstanceUtils.deleteInstance(discussion, user, database);
                }
            }
            */
            
            // delete the polls
            
            database.commitTransaction();
        }
        catch (Exception e)
        {
            database.rollbackTransaction();
            throw new Exception(e.getMessage());
        }
    }

    public static DataInstance modifyInstance(InstanceId id, String sqlInserts, User user,
            boolean includeUpdateFields, DatabaseConnection database) throws Exception {

        DataInstance inst = DataInstanceUtils.getInstance(id, user, database, true, true);
        return modifyInstance(inst, sqlInserts, user, includeUpdateFields, database);
    }

    public static DataInstance modifyInstance(DataInstance instance, String sqlInserts, User user,
            boolean includeUpdateFields, DatabaseConnection database) throws Exception {
        database.startTransaction();

        InstanceId instanceId = instance.getId();
        DataInstance result = null;

        try
        {
            String sql = "update " + Tools.getTableSet() + " set " + sqlInserts;
            
            if (includeUpdateFields) {
                sql += ", expiration = null, updated = CURRENT_TIMESTAMP, " +
                    "updatedBy = '" + user.getId() + "', updatedByName = '" + user.getName() + "'";
            }

            sql += " where id = '" + instanceId + "'";

            database.executeStatement(sql);

            sql = "update Chime.RegisteredInterest set instance_name = '" +
                    instance.getName() + "', last_update = CURRENT_TIMESTAMP where instance_id ='"
                    + instanceId + "' and user_id <> '" + user.getId() + "'";
            database.executeStatement(sql);

            result = getInstance(instanceId, user, database, true, false);
            database.commitTransaction();

        }
        catch (Exception e)
        {
            database.rollbackTransaction();
            throw new Exception(e.getMessage());
        }

        return result;
    }

    public static DataInstance modifyInstance(DataInstance instance, String sqlInserts, boolean includeUpdateFields, User user, DatabaseConnection database) throws Exception
    {
        return modifyInstance(instance, sqlInserts, user, includeUpdateFields, database);
    }
    
    public static DataInstance createInstance(List<Shape> types, String name, String description, Date expiration, String[] sqlInserts, List<FieldData> fieldData,
            List<Scope> scopes, User user, DatabaseConnection database) throws Exception
    {
        database.startTransaction();
        
        DataInstance result = null;
        
        try
        {
            InstanceId id = Tools.getNewId(Tools.DEFAULT_EXTID);
            String sql = "insert into " + Tools.getTableSet() +
                    " (id,name,description,created,updated,expiration,createdBy,createdByName,updatedBy,updatedByName,averageRating,ratingCount,tagCount,commentCount";
            if (sqlInserts[0] != null) {
                sql += "," + sqlInserts[0];
            }

            String expirationTime = "null";
            if (expiration != null) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                expirationTime = "'" + formatter.format(expiration) + "'";
            }

            sql += ") values ('" + id + "'," +
                new StringData(name).asSQLValue() +
                ", " + new StringData(description).asSQLValue() +
                ", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, " + expirationTime +
                ",'" + user.getId() + "', '" + user.getName() + "', '" + user.getId() + "', '" + user.getName() +
                "', 0, 0, 0, 0";
            
            if (sqlInserts[1] != null) {
                sql += ", " + sqlInserts[1];
            }

            sql += ")";
            
            database.executeStatement(sql);

            //String id = Tools.getPreviousId(database, Tools.getTableSet());

            // set the initial data types for the new instance
            int position = 1;
            for (Shape type : types) {
                InstanceId newid = Tools.getNewId(Tools.DEFAULT_EXTID);
                sql = "insert into " + Tools.getTableSet() + "_Type (id,instance_id, datatype_id, name, position) values ('" + newid + "', '" + id +
                        "', '" + type.getId() + "', " + new StringData(name).asSQLValue() + ", " + position++ + ")";
                database.executeStatement(sql);
            }

            DataInstance instance = getInstance(id, null, database, false, false);

            position = 1;
            for (FieldData field : fieldData)
            {
                FieldDataUtils.createFieldData(instance, field, position++, database);
            }
            
            for (Scope scope : scopes)
            {
                ScopeUtils.applyScope(id, scope, database);
            }
            
            HistoryUtils.writeEvent(HistoryUtils.HistoryEventType.Create, instance, user, database);
            result = getInstance(id, user, database, true, false);
            database.commitTransaction();
        }
        catch (Exception e)
        {
            database.rollbackTransaction();
            throw new Exception(e.getMessage());
        }
        
        return result;
    }
 
    public static DataInstance getInstance(InstanceId id, User user, DatabaseConnection database, boolean deep, boolean useCache) throws Exception
    {
        if (!id.getValue().endsWith(Tools.DEFAULT_EXTID)) {
            // the data must be sourced by an extension
            return ChimeExtensionManager.instance().getDataInstance(id);
        }

        List<Tag> userTags = null;

        if (useCache) {
            DataInstance cachedInstance = CacheManager.instance().get(id);
            if (cachedInstance != null) {
                if (deep) {

                    DataSocialContext social = cachedInstance.getSocialContext();
                    if (social != null) {
                        // maybe there's an appropriate social context in the cache
                        DataSocialContext context = null; //CacheManager.instance().getSocialContext(cachedInstance, user);
                        if (false) {//context != null) {
                            cachedInstance.setSocialContext(context);
                        } else {
                            List<TagContext> tagContexts = TagUtils.getTagContexts(id, user, database);
                            social.setTagContexts(tagContexts);
                            social.setHasUserVote(false);

                            if (user != null && !user.isIndexing()) {
                                Review userReview = ReviewUtils.getReview(id, user, database);
                                social.setUserReview(userReview);

                                UserVote userVote = VoteUtils.getVote(id, user, database);
                                if (userVote.hasVote) {
                                    social.setUserVote(userVote.vote);
                                }

                                userTags = TagUtils.getUserTags(id, user, database, true);
                                for (TagContext tagContext : tagContexts)
                                {
                                    tagContext.setUserTagged(false);

                                    Tag tag = tagContext.getTag();
                                    for (Tag t : userTags)
                                    {
                                        if (t.getId().equals(tag.getId()))
                                        {
                                            tagContext.setUserTagged(true);
                                            break;
                                        }
                                    }
                                }



                                if (cachedInstance instanceof Tag)
                                {
                                    Tag tag = (Tag)cachedInstance;

                                    ShapeUtils.getCommunityShapeContexts(tag, user, database);

                                    if (user != null && !user.isIndexing())
                                    {
                                        ShapeUtils.getUserShapeContexts(user, tag, database);
                                    }
                                } else if (cachedInstance instanceof User) {
                                    User u = (User)cachedInstance;
                                    UserMessagesBundle bundle = UserMessageUtils.getMessages(u, new Cursor(DataInstanceUtils.USRMSGLIMIT), database);
                                    u.setUserMessagesBundle(bundle);
                                }

                                boolean regInterest = RegisteredInterestUtils.isRegisteredInterest(cachedInstance, user, database);
                                social.setRegisteredInterest(regInterest);
                                social.setUser(user);
                                CacheManager.instance().putSocialContext(cachedInstance);
                            }
                        }

                        // get updated references
                        ReferenceUtils.updateReferences(cachedInstance, user, database);

                        return cachedInstance;
                    }
                }
                else
                {
                    return cachedInstance;
                }
            }
        }

        String sql = "SELECT * FROM "
                + Tools.getTableSet() + " where id = '" + id + "'";

        long start = System.currentTimeMillis();
        List<? extends DataInstance> results = getInstances(sql, null, user, database, deep).getInstances();
        long elapsed = System.currentTimeMillis() - start;

        if (results.isEmpty()) {
            return null;
        } else {
            return results.get(0);
        }
    }

    public static InstancesBundle getInstances(String sql, Cursor cursor, User user, DatabaseConnection database, boolean deep) throws Exception {

        List<Tag> userTags = null;
        List<DataInstance> results = new ArrayList<DataInstance>();

        int start = 1;
        int end = 999999;
        int total = 0;

        if (cursor != null)
        {
            start = cursor.getFirst() + 1;
            end = start + cursor.getMax() - 1;
            sql += Tools.getLimitClause(database, ROWLIMIT);
        }

        int fetchNumber = start;
        boolean limited = false;
        DataSet dataSet = database.getDataSet(sql, true);
        boolean fetchAgain = dataSet.absolute(start);
        while (fetchAgain) {
            IDataValue id = dataSet.getFieldValue("id");
            IDataValue name = dataSet.getFieldValue("name");
            IDataValue description = dataSet.getFieldValue("description");

            IDataValue created = dataSet.getFieldValue("created");
            IDataValue createdBy = dataSet.getFieldValue("createdBy");
            IDataValue createdByName = dataSet.getFieldValue("createdByName");

            IDataValue updated = dataSet.getFieldValue("updated");
            IDataValue updatedBy = dataSet.getFieldValue("updatedBy");
            IDataValue updatedByName = dataSet.getFieldValue("updatedByName");

            IDataValue expiration = dataSet.getFieldValue("expiration");

            IDataValue averageRating = dataSet.getFieldValue("averageRating");
            IDataValue ratingCount = dataSet.getFieldValue("ratingCount");
            IDataValue reviewed = dataSet.getFieldValue("reviewed");
            IDataValue reviewedBy = dataSet.getFieldValue("reviewedBy");
            IDataValue reviewedByName = dataSet.getFieldValue("reviewedByName");
            IDataValue reviewedAction = dataSet.getFieldValue("reviewedByAction");

            IDataValue commentCount = dataSet.getFieldValue("commentCount");
            IDataValue commented = dataSet.getFieldValue("commented");
            IDataValue commentedBy = dataSet.getFieldValue("commentedBy");
            IDataValue commentedByName = dataSet.getFieldValue("commentedByName");

            IDataValue lockType = dataSet.getFieldValue("lockType");
            IDataValue locked = dataSet.getFieldValue("locked");
            IDataValue lockedBy = dataSet.getFieldValue("lockedBy");
            IDataValue lockedByName = dataSet.getFieldValue("lockedByName");

            IDataValue tagCount = dataSet.getFieldValue("tagCount");
            IDataValue tagged = dataSet.getFieldValue("tagged");
            IDataValue taggedBy = dataSet.getFieldValue("taggedBy");
            IDataValue taggedByName = dataSet.getFieldValue("taggedByName");
            IDataValue taggedAction = dataSet.getFieldValue("taggedAction");

            IDataValue positiveCount = dataSet.getFieldValue("positiveCount");
            IDataValue negativeCount = dataSet.getFieldValue("negativeCount");
            IDataValue ranking = dataSet.getFieldValue("ranking");

            // we can create the instance based on the first types in the list because
            // if the instance has multiple types then the class is always DataInstance.class.
            List<Shape> shapes = ShapeUtils.getInstanceShapes(InstanceId.create(id.asString()), database);
            Class instanceClass = Tools.getClass(shapes);

            DataInstance instance = (DataInstance)instanceClass.newInstance();
            instance.setId(InstanceId.create(id.asString()));

            instance.setPositiveCount(positiveCount.asInteger());
            instance.setNegativeCount(negativeCount.asInteger());
            instance.setRanking(ranking.asInteger());

            instance.setName(name.asString());
            instance.setDescription(description.asString());
            instance.setShapes(shapes);

            instance.setCreated(created.asDate());
            User u = new User();
            u.setId(InstanceId.create(createdBy.asString()));
            u.setName(createdByName.asString());
            instance.setCreatedBy(u);

            instance.setUpdated(updated.asDate());
            u = new User();
            u.setId(InstanceId.create(updatedBy.asString()));
            u.setName(updatedByName.asString());
            instance.setUpdatedBy(u);

            if (!expiration.isNull()) {
                instance.setExpiration(expiration.asDate());
            }

            instance.setAverageRating(averageRating.asFloat());
            instance.setRatingCount(ratingCount.asInteger());
            if (!reviewed.isNull())
            {
                instance.setReviewed(reviewed.asDate());
                u = new User();
                u.setId(InstanceId.create(reviewedBy.asString()));
                u.setName(reviewedByName.asString());
                instance.setReviewedBy(u);
                instance.setReviewedAction(ReviewAction.valueOf(reviewedAction.asString()));
            }

            instance.setCommentCount(commentCount.asInteger());
            if (!commented.isNull())
            {
                instance.setCommented(commented.asDate());
                u = new User();
                u.setId(InstanceId.create(commentedBy.asString()));
                u.setName(commentedByName.asString());
                instance.setCommentedBy(u);
            }

            LockType lock = LockType.valueOf(lockType.asString());
            instance.setLockType(lock);
            if (lock != LockType.NONE)
            {
                instance.setLocked(locked.asDate());
                u = new User();
                u.setId(InstanceId.create(lockedBy.asString()));
                u.setName(lockedByName.asString());
                instance.setLockedBy(u);
            }

            instance.setTagCount(tagCount.asInteger());
            if (!tagged.isNull())
            {
                instance.setTagged(tagged.asDate());
                u = new User();
                u.setId(InstanceId.create(taggedBy.asString()));
                u.setName(taggedByName.asString());
                instance.setTaggedBy(u);
                instance.setTaggedAction(TagAction.valueOf(taggedAction.asString()));
            }

            if (instance instanceof Tag) {
                Tag tag = (Tag)instance;
                IDataValue usageCount = dataSet.getFieldValue("intVal");
                tag.setUsageCount(usageCount.asLong());
            } else if (instance instanceof Review) {
                Review review = (Review)instance;
                IDataValue rating = dataSet.getFieldValue("intVal2");
                InstanceId brId = InstanceId.create(dataSet.getFieldValue("backRef").asString());
                review.setBackRefId(brId);
                review.setBackRefName(DataInstanceUtils.getInstance(brId, user, database, false, true).getName());
                review.setRating(rating.asInteger());
            } else if (instance instanceof Comment) {
                Comment comment = (Comment)instance;
                InstanceId brId = InstanceId.create(dataSet.getFieldValue("backRef").asString());
                comment.setBackRefId(brId);
                comment.setBackRefName(DataInstanceUtils.getInstance(brId, user, database, false, true).getName());
            } else if (instance instanceof Discussion) {
                Discussion discussion = (Discussion)instance;
                InstanceId brId = InstanceId.create(dataSet.getFieldValue("backRef").asString());
                discussion.setBackRefId(brId);
                discussion.setBackRefName(DataInstanceUtils.getInstance(brId, user, database, false, true).getName());
            } else if (instance instanceof User) {
                User userData = (User)instance;
                IDataValue pw = dataSet.getFieldValue("charVal");
                userData.setPassword(pw.asString());
            } else if (instance instanceof Shape) {
                Shape type = (Shape)instance;

                String m = dataSet.getFieldValue("charVal").asString();
                char[] mask = m.toCharArray();
                type.setPrimitive(mask[0] == 'Y');
                type.setVisible(mask[1] == 'Y');
                type.setDirectCreatable(mask[2] == 'Y');
                type.setCanVote(mask[3] == 'Y');
                type.setCanReview(mask[4] == 'Y');
                type.setCanComment(mask[5] == 'Y');
                type.setCanTag(mask[6] == 'Y');
                type.setCanPoll(mask[7] == 'Y');
                type.setCanAttachFiles(mask[8] == 'Y');
                type.setHasImageGallery(mask[9] == 'Y');
                type.setCanDiscuss(mask[10] == 'Y');
                type.setCanMultiType(mask[11] == 'Y');
            }

            if (deep)
            {
                DataSocialContext context = instance.createNewSocialContext();
                context.setUser(user);
                context.setAverageRating(averageRating.asFloat());
                context.setRatingCount(ratingCount.asInteger());

                InstanceId instId = InstanceId.create(id.asString());

                if (true) { //user == null || !user.isIndexing()) {
                    ReviewsBundle ratings = ReviewUtils.getReviews(instId, null, new Cursor(REVIEWLIMIT), SortOrder.ByMostRecentEdit, database);
                    context.setReviewsBundle(ratings);
                }

                context.setHasUserVote(false);

                if (user != null)
                {
                    Review userReview = ReviewUtils.getReview(instId, user, database);
                    context.setUserReview(userReview);

                    UserVote userVote = VoteUtils.getVote(instId, user, database);
                    if (userVote.hasVote) {
                        context.setUserVote(userVote.vote);
                    }
                }

                List<DataInstance> images = AttachmentUtils.getImages(instId, database);
                instance.setImages(images);

                List<DataInstance> files = AttachmentUtils.getFiles(instId, database);
                instance.setFiles(files);

                if (true) { //user == null || !user.isIndexing()) {
                    CommentsBundle comments = CommentUtils.getComments(instId, null, new Cursor(REVIEWLIMIT), SortOrder.ByMostRecentEdit, database);
                    context.setCommentsBundle(comments);
                }

                if (true) { //user == null || !user.isIndexing()) {
                    DiscussionsBundle discussions = DiscussionUtils.getDiscussions(instId, new Cursor(DISCUSSIONLIMIT), database);
                    context.setDiscussionsBundle(discussions);
                }

                List<TagContext> tagList = TagUtils.getTagContexts(instId, user, database);
                HashMap<InstanceId, TagContext> tagMap = new HashMap<InstanceId, TagContext>();
                for (TagContext tagContext : tagList)
                {
                    context.addTagContext(tagContext);
                    tagMap.put(tagContext.getTag().getId(), tagContext);
                }

                if (user != null && !user.isIndexing())
                {
                    userTags = TagUtils.getUserTags(instId, user, database, false);
                }

                List<Scope> scopeList = ScopeUtils.getScopes(instId, database);
                for (Scope scope : scopeList)
                {
                    context.addScope(scope);
                }

                if (user != null && !user.isIndexing()) {
                    boolean regInterest = RegisteredInterestUtils.isRegisteredInterest(instance, user, database);
                    context.setRegisteredInterest(regInterest);
                }

                instance.setSocialContext(context);

                // the column data comes next
                for (Shape shape : instance.getShapes()) {
                    List<DataField> fields = shape.getFields();
                    for (DataField field : fields)
                    {
                        int colName = field.getColumn();

                        Shape dt = field.getShape();

                        // get the display values
                        List<DataFieldValue> values = null;
                        if (dt.isPrimitive())
                        {
                            values = FieldDataUtils.getInternalFieldValues(database, shape, colName, dt, instance.getId());
                        }
                        else
                        {
                            values = FieldDataUtils.getExternalFieldValues(database, shape, colName, dt, instance.getId());
                        }

                        instance.setFieldValues(shape, field, values);
                    }
                }

                if (instance instanceof User) {
                    // grab the favorites
                    User userData = (User)instance;
                    List<DataInstance> refs = userData.getFavoriteReferences();
                    List<DataInstance> favorites = new ArrayList<DataInstance>();
                    for (DataInstance ref : refs) {
                        DataInstance fav = DataInstanceUtils.getInstance(ref.getId(), user, database, false, true);
                        favorites.add(fav);
                    }

                    userData.setFavorites(favorites);

                    UserMessagesBundle msgsBundle = UserMessageUtils.getMessages(userData, new Cursor(USRMSGLIMIT), database);
                    userData.setUserMessagesBundle(msgsBundle);
                }

                if (instance instanceof Community) {
                    // grab the favorites
                }

                if (instance instanceof Folder) {
                    // grab the favorites
                    Folder userData = (Folder)instance;
                    List<DataInstance> refs = userData.getChildrenReferences();
                    List<DataInstance> kids = new ArrayList<DataInstance>();
                    for (DataInstance ref : refs) {
                        DataInstance fav = DataInstanceUtils.getInstance(ref.getId(), user, database, false, true);
                        kids.add(fav);
                    }

                    userData.setChildren(kids);
                }

                if (instance instanceof Tag)
                {
                    Tag tag = (Tag)instance;

                    ShapeUtils.getCommunityShapeContexts(tag, user, database);

                    if (user != null)
                    {
                        ShapeUtils.getUserShapeContexts(user, tag, database);
                    }

                    boolean isPrivate = true;
                    for (Scope scope : context.getScopes()) {
                        if (scope.isGlobalCommunity()) {
                            isPrivate = false;
                            break;
                        }
                    }
                    tag.setPrivate(isPrivate);
                } else if (instance instanceof Shape) {
                    ShapeUtils.getFields((Shape)instance, database, true);
                } else if (instance instanceof User) {
                    UserUtils.updateUserProfile((User)instance, database);
                    UserSocialContext ctx = (UserSocialContext)context;
                    //ctx.setUserTagContexts(TagUtils.getTagContexts((User)instance, database));
                }
            }

            instance.finishLoading(DataInstanceHelperFactory.getHelper(instanceClass), database);

            PortalTemplate template = PortalTemplateUtils.getTemplate(instance, database);
            instance.setPortalTemplate(template);

            CacheManager.instance().put(instance);
            CacheManager.instance().putSocialContext(instance);

            // update the tag contexts for the given user
            if (userTags != null)
            {
                DataSocialContext social = instance.getSocialContext();
                List<TagContext> tagContexts = social.getTagContexts();
                for (TagContext tagContext : tagContexts)
                {
                    Tag tag = tagContext.getTag();
                    for (Tag t : userTags)
                    {
                        if (t.getId().equals(tag.getId()))
                        {
                            tagContext.setUserTagged(true);
                        }
                    }
                }
            }

            results.add(instance);

            if (fetchNumber == end)
            {
                fetchAgain = false;
            }
            else
            {
                fetchAgain = dataSet.next();
            }

            if (!fetchAgain && cursor != null)
            {
                dataSet.last();
                total = dataSet.getRowNumber();

                if (total > ROWLIMIT)
                {
                    total = ROWLIMIT;
                    limited = true;
                }
            }

            fetchNumber++;
        }

        dataSet.close();


        // create the return cursor
        Cursor newCursor = null;

        if (cursor != null)
        {
            newCursor = new Cursor(cursor.getFirst(), results.size(), cursor.getMax(), total, limited);
        }

        return new InstancesBundle(results, newCursor);
    }

    static int operatorToOffset(Operator operator)
    {
        int offset = 0;
        switch (operator)
        {
            case Past24Hours:
                offset = 1;
                break;
            case Past3Days:
                offset = 3;
                break;
            case Past7Days:
                offset = 7;
                break;
            case Past30Days:
                offset = 30;
                break;
        }
        
        return offset;
    }
    
}
