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

import com.paxxis.chime.client.common.portal.PortalTemplate;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class DataInstance implements Serializable {
    public enum TagAction
    {
        A, // Applied
        R  // Removed
    }
    
    public enum ReviewAction
    {
        C, // Created
        U  // Updated
    }

    public enum LockType {
        NONE,
        EDIT,
        FLOW
    }

    private boolean isTransient = false;

    private InstanceId _id = InstanceId.create("0");
    private String _name = null;
    private String _description = "";

    private Date expiration = null;

    private User _createdBy = null;
    private Date _created = null;
    private User _updatedBy = null;
    private Date _updated = null;

    private User _reviewedBy = null;
    private Date _reviewed = null;
    private ReviewAction _reviewedAction = null;
    private float _averageRating = 0.0f;
    private int _ratingCount = 0;

    private User _taggedBy = null;
    private Date _tagged = null;
    private int _tagCount = 0;
    private TagAction _taggedAction = null;
    
    private User _commentedBy = null;
    private Date _commented = null;
    private int _commentCount = 0;

    private int _positiveCount = 0;
    private int _negativeCount = 0;
    private int _ranking = 50;

    private LockType lockType = LockType.NONE;
    private User lockedBy = null;
    private Date locked = null;

    private List<Shape> shapes = new ArrayList<Shape>();

    // the field values are a two layer hash map.  The outer layer maps a type id
    // to a hash map.  That inner hashmap maps a field id to a list of data field values
    private HashMap<InstanceId, HashMap<String, List<DataFieldValue>>> _fieldValues =
            new HashMap<InstanceId, HashMap<String, List<DataFieldValue>>>();
    
    // there is no social context if the data type is not socialized
    private DataSocialContext _socialContext = null;

    // attached images
    private List<DataInstance> _images = new ArrayList<DataInstance>();

    // attached files
    private List<DataInstance> _files = new ArrayList<DataInstance>();

    private PortalTemplate _portalTemplate = null;

    public void setTransient(boolean val) {
        isTransient = val;
    }

    public boolean isTransient() {
        return isTransient;
    }
    
    public void finishLoading(DataInstanceHelper helper, Object database)
    {
        if (helper != null)
        {
            helper.processAfterRead(this, database);
        }
    }

    @Override
    public boolean equals(Object other) {
        
        if (this == other) {
            return true;
        }

        if (other == null) {
            return false;
        }

        if (other.getClass() != getClass()) {
            return false;
        }

        DataInstance inst = (DataInstance)other;
        if (_id == null) {
            if (inst._id != null) {
                return false;
            }
        } else {
            if (!_id.equals(inst._id)) {
                return false;
            }
        }

        if (hashCode() != inst.hashCode()) {
            return false;
        }

        // we need to do a field by field comparison
        if (isTransient != inst.isTransient) {
            return false;
        }

        if (_name == null) {
            if (inst._name != null) {
                return false;
            }
        } else {
            if (!_name.equals(inst._name)) {
                return false;
            }
        }

        if (_description == null) {
            if (inst._description != null) {
                return false;
            }
        } else {
            if (!_description.equals(inst._description)) {
                return false;
            }
        }

        if (expiration == null) {
            if (inst.expiration != null) {
                return false;
            }
        } else {
            if (!expiration.equals(inst.expiration)) {
                return false;
            }
        }

        if (_createdBy == null) {
            if (inst._createdBy != null) {
                return false;
            }
        } else {
            if (!_createdBy.equals(inst._createdBy)) {
                return false;
            }
        }

        if (_created == null) {
            if (inst._created != null) {
                return false;
            }
        } else {
            if (!_created.equals(inst._created)) {
                return false;
            }
        }

        if (_updatedBy == null) {
            if (inst._updatedBy != null) {
                return false;
            }
        } else {
            if (!_updatedBy.equals(inst._updatedBy)) {
                return false;
            }
        }

        if (_updated == null) {
            if (inst._updated != null) {
                return false;
            }
        } else {
            if (!_updated.equals(inst._updated)) {
                return false;
            }
        }


        if (_reviewedBy == null) {
            if (inst._reviewedBy != null) {
                return false;
            }
        } else {
            if (!_reviewedBy.equals(inst._reviewedBy)) {
                return false;
            }
        }

        if (_reviewed == null) {
            if (inst._reviewed != null) {
                return false;
            }
        } else {
            if (!_reviewed.equals(inst._reviewed)) {
                return false;
            }
        }

        if (_reviewedAction == null) {
            if (inst._reviewedAction != null) {
                return false;
            }
        } else {
            if (!_reviewedAction.equals(inst._reviewedAction)) {
                return false;
            }
        }

        if (_averageRating != inst._averageRating) {
            return false;
        }

        if (_ratingCount != inst._ratingCount) {
            return false;
        }

        if (_taggedBy == null) {
            if (inst._taggedBy != null) {
                return false;
            }
        } else {
            if (!_taggedBy.equals(inst._taggedBy)) {
                return false;
            }
        }

        if (_tagged == null) {
            if (inst._tagged != null) {
                return false;
            }
        } else {
            if (!_tagged.equals(inst._tagged)) {
                return false;
            }
        }

        if (_tagCount != inst._tagCount) {
            return false;
        }

        if (_taggedAction == null) {
            if (inst._taggedAction != null) {
                return false;
            }
        } else {
            if (!_taggedAction.equals(inst._taggedAction)) {
                return false;
            }
        }

        if (_commentedBy == null) {
            if (inst._commentedBy != null) {
                return false;
            }
        } else {
            if (!_commentedBy.equals(inst._commentedBy)) {
                return false;
            }
        }

        if (_commented == null) {
            if (inst._commented != null) {
                return false;
            }
        } else {
            if (!_commented.equals(inst._commented)) {
                return false;
            }
        }

        if (_commentCount != inst._commentCount) {
            return false;
        }

        if (_positiveCount != inst._positiveCount) {
            return false;
        }

        if (_negativeCount != inst._negativeCount) {
            return false;
        }
        if (_ranking != inst._ranking) {
            return false;
        }

        if (lockType == null) {
            if (inst.lockType != null) {
                return false;
            }
        } else {
            if (!lockType.equals(inst.lockType)) {
                return false;
            }
        }

        if (lockedBy == null) {
            if (inst.lockedBy != null) {
                return false;
            }
        } else {
            if (!lockedBy.equals(inst.lockedBy)) {
                return false;
            }
        }

        if (locked == null) {
            if (inst.locked != null) {
                return false;
            }
        } else {
            if (!locked.equals(inst.locked)) {
                return false;
            }
        }

        if (shapes == null) {
            if (inst.shapes != null) {
                return false;
            }
        } else {
            if (!shapes.equals(inst.shapes)) {
                return false;
            }
        }

        if (_fieldValues == null) {
            if (inst._fieldValues != null) {
                return false;
            }
        } else {
            if (!_fieldValues.equals(inst._fieldValues)) {
                return false;
            }
        }

        if (_socialContext == null) {
            if (inst._socialContext != null) {
                return false;
            }
        } else {
            if (!_socialContext.equals(inst._socialContext)) {
                return false;
            }
        }

        if (_images == null) {
            if (inst._images != null) {
                return false;
            }
        } else {
            if (!_images.equals(inst._images)) {
                return false;
            }
        }

        if (_files == null) {
            if (inst._files != null) {
                return false;
            }
        } else {
            if (!_files.equals(inst._files)) {
                return false;
            }
        }

        if (_portalTemplate == null) {
            if (inst._portalTemplate != null) {
                return false;
            }
        } else {
            if (!_portalTemplate.equals(inst._portalTemplate)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (this._id != null ? this._id.hashCode() : 0);
        return hash;
    }

    public DataInstance copy() {
        return copy(new DataInstance());
    }

    protected <T extends DataInstance> T copy(T target) {
        if (!(target instanceof DataInstance)) {
            throw new RuntimeException();
        }

        target.setName(getName());
        target.setDescription(getDescription());
        target.setId(getId());
        target.setCommentCount(getCommentCount());
        target.setRatingCount(getRatingCount());
        target.setTagCount(getTagCount());
        target.setAverageRating(getAverageRating());
        target.setCreatedBy(getCreatedBy());
        target.setCreated(getCreated());
        target.setUpdatedBy(getUpdatedBy());
        target.setUpdated(getUpdated());
        target.setTaggedBy(getTaggedBy());
        target.setTagged(getTagged());
        target.setReviewedBy(getReviewedBy());
        target.setReviewed(getReviewed());
        target.setCommentedBy(getCommentedBy());
        target.setCommented(getCommented());
        target.setPositiveCount(getPositiveCount());
        target.setNegativeCount(getNegativeCount());
        target.setRanking(getRanking());
        target.setTransient(isTransient());
        target.setExpiration(getExpiration());
        List<Shape> types = getShapes();
        for (Shape type : types) {
            target.addShape(type);
            List<DataField> fields = type.getFields();
            for (DataField field : fields) {
                List<DataFieldValue> copyValues = new ArrayList<DataFieldValue>();
                List<DataFieldValue> fieldValues = getFieldValues(type, field);
                for (DataFieldValue fieldValue : fieldValues) {
                    DataFieldValue v = new DataFieldValue(fieldValue);
                    copyValues.add(v);
                }
                target.setFieldValues(type, field, copyValues);
            }
        }
        return target;
    }

    public boolean isCommentsChanged(DataInstance other) {
        if (getCommentCount() > other.getCommentCount()) {
            return true;
        }

        return false;
    }

    public boolean isReviewsChanged(DataInstance other) {
        if (getRatingCount() > other.getRatingCount()) {
            return true;
        }

        if (getRatingCount() > 0) {
            if (getReviewed().after(other.getReviewed())) {
                return true;
            }
        }

        return false;
    }

    public boolean isTagsChanged(DataInstance other) {
        Date tagged = getTagged();
        Date otherTagged = other.getTagged();

        if (tagged != null) {
            if (otherTagged == null || tagged.after(otherTagged)) {
                return true;
            }
        }

        return false;
    }

    public boolean isChangedSince(DataInstance other) {

        // check the updated timstamp
        if (getUpdated().after(other.getUpdated())) {
            return true;
        }

        // check comments
        if (isCommentsChanged(other)) {
            return true;
        }

        // check reviews
        if (isReviewsChanged(other)) {
            return true;
        }

        // check tags
        if (isTagsChanged(other)) {
            return true;
        }


        // check discussions


        // check polls

        return false;
    }

    public boolean canUpdatePermissions(User user) {
        boolean canUpdate = canUpdate(user);
        if (canUpdate) {
            // there are additional restrictions.
            Shape type = getShapes().get(0);
            if (type.getName().equals("User") ||
                type.getName().equals("Review") ||
                type.getName().equals("Comment") ||
                type.getName().equals("Tag") ||
                type.getName().equals("Community") ||
                type.getName().equals("Discussion"))
            {
                canUpdate = false;
            }
        }

        return canUpdate;
    }

    public boolean canUpdate(User user)
    {
        if (getId().equals(user.getId())) {
            return true;
        }
        
        boolean canUpdate = false;

        // check the lock status first, it's faster
        if (getLockType() != LockType.NONE) {
            if (!user.getId().equals(getLockedBy().getId())) {
                if (!user.isAdmin()) {
                    return false;
                }
            }
        }

        DataSocialContext context = getSocialContext();
        List<Scope> scopes = context.getScopes();

        if (user != null) {
            if (user.isAdmin()) {
                canUpdate = true;
            } else {
                for (Scope scope : scopes) {
                    if (scope.getCommunity().getId().equals(user.getId()) ||
                            scope.isGlobalCommunity()) {
                        if (scope.getPermission() == Scope.Permission.RU ||
                                scope.getPermission() == Scope.Permission.RUC) {
                            canUpdate = true;
                            break;
                        }
                    } else {
                        List<Community> communities = user.getCommunities();
                        for (Community community : communities) {
                            if (community.getId().equals(scope.getCommunity().getId())) {
                                if (scope.getPermission() == Scope.Permission.RU ||
                                        scope.getPermission() == Scope.Permission.RUC) {
                                    canUpdate = true;
                                    break;
                                }
                            }
                        }

                        // check for moderated communities
                        if (!canUpdate) {
                            communities = user.getModeratedCommunities();
                            for (Community community : communities) {
                                if (community.getId().equals(scope.getCommunity().getId())) {
                                    if (scope.getPermission() == Scope.Permission.RM) {
                                        canUpdate = true;
                                        break;
                                    }
                                }
                            }
                        }

                        if (canUpdate) {
                            break;
                        }
                    }
                }
            }
        }

        return canUpdate;
    }

    public void setAverageRating(float rating)
    {
        _averageRating = rating;
    }
    
    public float getAverageRating()
    {
        return _averageRating;
    }
    
    public void setRatingCount(int cnt)
    {
        _ratingCount = cnt;
    }
    
    public int getRatingCount()
    {
        return _ratingCount;
    }
    
    public void setTagCount(int cnt)
    {
        _tagCount = cnt;
    }
    
    public int getTagCount()
    {
        return _tagCount;
    }
    
    public void setCommentCount(int cnt)
    {
        _commentCount = cnt;
    }
    
    public int getCommentCount()
    {
        return _commentCount;
    }

    public void setPositiveCount(int cnt) {
        _positiveCount = cnt;
    }

    public int getPositiveCount() {
        return _positiveCount;
    }

    public void setNegativeCount(int cnt) {
        _negativeCount = cnt;
    }

    public int getNegativeCount() {
        return _negativeCount;
    }

    public void setRanking(int cnt) {
        _ranking = cnt;
    }

    public int getRanking() {
        return _ranking;
    }

    public void setImages(List<DataInstance> images) {
        _images.clear();
        _images.addAll(images);
    }

    public List<DataInstance> getImages() {
        return _images;
    }

    public void setFiles(List<DataInstance> files) {
        _files.clear();
        _files.addAll(files);
    }

    public List<DataInstance> getFiles() {
        return _files;
    }

    public void setFieldValues(Shape type, DataField field, List<DataFieldValue> values)
    {
        HashMap<String, List<DataFieldValue>> outer = _fieldValues.get(type.getId());
        if (outer == null) {
            outer = new HashMap<String, List<DataFieldValue>>();
            _fieldValues.put(type.getId(), outer);
        }

        outer.put(field.getId(), values);
    }
    
    public List<DataFieldValue> getFieldValues(Shape type, DataField field)
    {
        HashMap<String, List<DataFieldValue>> outer = _fieldValues.get(type.getId());
        if (outer == null) {
            setFieldValues(type, field, new ArrayList<DataFieldValue>());
            outer = _fieldValues.get(type.getId());
        }

        List<DataFieldValue> values = outer.get(field.getId());
        if (values == null) {
            values = new ArrayList<DataFieldValue>();
            outer.put(field.getId(), values);
        }

        return values;
    }
    
    public InstanceId getId() {
        return _id;
    }

    public void setId(InstanceId id) {
        _id = id;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getDescription() {
        return _description;
    }

    public void setDescription(String desc) {
        if (desc == null) {
            _description = "";
        } else {
            _description = desc;
        }
    }

    public Date getExpiration() {
        return expiration;
    }

    public void setExpiration(Date exp) {
        expiration = exp;
    }

    public User getCreatedBy() {
        return _createdBy;
    }

    public void setCreatedBy(User createdBy) {
        _createdBy = createdBy;
    }

    public Date getCreated() {
        return _created;
    }

    public void setCreated(Date created) {
        _created = created;
    }

    public User getUpdatedBy() {
        return _updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        _updatedBy = updatedBy;
    }

    public Date getUpdated() {
        return _updated;
    }

    public void setUpdated(Date updated) {
        _updated = updated;
    }

    public User getTaggedBy() {
        return _taggedBy;
    }

    public void setTaggedBy(User taggedBy) {
        _taggedBy = taggedBy;
    }

    public Date getTagged() {
        return _tagged;
    }

    public void setTagged(Date tagged) {
        _tagged = tagged;
    }

    public TagAction getTaggedAction()
    {
        return _taggedAction;
    }
    
    public void setTaggedAction(TagAction action)
    {
        _taggedAction = action;
    }
    
    public User getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(User by) {
        lockedBy = by;
    }

    public Date getLocked() {
        return locked;
    }

    public void setLocked(Date val) {
        locked = val;
    }

    public LockType getLockType()
    {
        return lockType;
    }

    public void setLockType(LockType type)
    {
        lockType = type;
    }

    public User getReviewedBy() {
        return _reviewedBy;
    }

    public void setReviewedBy(User reviewedBy) {
        _reviewedBy = reviewedBy;
    }

    public Date getReviewed() {
        return _reviewed;
    }

    public void setReviewed(Date reviewed) {
        _reviewed = reviewed;
    }

    public ReviewAction getReviewedAction()
    {
        return _reviewedAction;
    }
    
    public void setReviewedAction(ReviewAction action)
    {
        _reviewedAction = action;
    }
    
    public User getCommentedBy() {
        return _commentedBy;
    }

    public void setCommentedBy(User commentedBy) {
        _commentedBy = commentedBy;
    }

    public Date getCommented() {
        return _commented;
    }

    public void setCommented(Date commented) {
        _commented = commented;
    }

    public List<Shape> getShapes() {
        return shapes;
    }

    public void addShape(Shape type) {
        boolean exists = false;
        for (Shape t : shapes) {
            if (t.getId().equals(type.getId())) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            shapes.add(type);
            HashMap<String, List<DataFieldValue>> map = new HashMap<String, List<DataFieldValue>>();
            List<DataField> fields = type.getFields();
            for (DataField field : fields)
            {
                List<DataFieldValue> vals = new ArrayList<DataFieldValue>();
                map.put(field.getId(), vals);
            }

            _fieldValues.put(type.getId(), map);
        }
    }

    public void setShapes(List<Shape> types) {
        setShapes(types, true);
    }

    public void setShapes(List<Shape> types, boolean clearFieldValues) {
        shapes = types;

        if (clearFieldValues) {
            _fieldValues.clear();

            for (Shape type : types) {
                HashMap<String, List<DataFieldValue>> map = new HashMap<String, List<DataFieldValue>>();
                List<DataField> fields = type.getFields();
                for (DataField field : fields)
                {
                    List<DataFieldValue> vals = new ArrayList<DataFieldValue>();
                    map.put(field.getId(), vals);
                }

                _fieldValues.put(type.getId(), map);
            }
        }
    }

    public void setPortalTemplate(PortalTemplate template)
    {
        _portalTemplate = template;
    }
    
    public PortalTemplate getPortalTemplate()
    {
        return _portalTemplate;
    }

    public DataSocialContext createNewSocialContext() {
        return new DataSocialContext();
    }

    public DataSocialContext getSocialContext()
    {
        return _socialContext;
    }
    
    public void setSocialContext(DataSocialContext context)
    {
        _socialContext = context;
    }

    public static DataInstance createInstance(Object source)
    {
        if (source instanceof DataInstance)
        {
            return (DataInstance)source;
        }
        //else if (source instanceof String)
        //{
        //    return createFromJSON((String)source);
        //}
        
        throw new RuntimeException("something something");
    }
    
}
