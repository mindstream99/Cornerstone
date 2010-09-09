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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class Shape extends DataInstance {

    public static final InstanceId EVENT_ID = InstanceId.create("a97381dce3d44e139823679460d2a5f100");
    public static final InstanceId SHAPE_ID = InstanceId.create("100");
    public static final InstanceId TEXT_ID = InstanceId.create("200");
    public static final InstanceId NUMBER_ID = InstanceId.create("300");
    public static final InstanceId URL_ID = InstanceId.create("400");
    public static final InstanceId REVIEW_ID = InstanceId.create("500");
    public static final InstanceId COMMENT_ID = InstanceId.create("600");
    public static final InstanceId TAG_ID = InstanceId.create("700");
    public static final InstanceId USER_ID = InstanceId.create("800");
    public static final InstanceId COMMUNITY_ID = InstanceId.create("900");
    public static final InstanceId DISCUSSION_ID = InstanceId.create("1000");
    public static final InstanceId RICHTEXT_ID = InstanceId.create("1100");
    public static final InstanceId DASHBOARD_ID = InstanceId.create("1200");
    public static final InstanceId NAMEDSEARCH_ID = InstanceId.create("1300");
    public static final InstanceId IMAGE_ID = InstanceId.create("1400");
    public static final InstanceId FILE_ID = InstanceId.create("1500");
    public static final InstanceId ANALYTIC_ID = InstanceId.create("1900");
    public static final InstanceId REFERENCE_ID = InstanceId.create("2000");
    public static final InstanceId FOLDER_ID = InstanceId.create("2100");

    private boolean _primitive = false;
    private boolean _viewable = false;

    private boolean _canReview = false;
    private boolean _canComment = false;
    private boolean _canDiscuss = false;
    private boolean _canTag = false;
    private boolean _canVote = false;
    private boolean _canPoll = false;
    private boolean canAttachFiles = false;
    private boolean hasImageGallery = false;
    private boolean canMultiType = false;
    private boolean isDirectCreatable = false;

    private List<DataField> _fields = new ArrayList<DataField>();

    @Override
    public Shape copy() {
        Shape result = super.copy(new Shape());
        result._primitive = _primitive;
        result._viewable = _viewable;
        result._canReview = _canReview;
        result._canComment = _canComment;
        result._canDiscuss = _canDiscuss;
        result._canTag = _canTag;
        result._canVote = _canVote;
        result._canPoll = _canPoll;
        result.canAttachFiles = canAttachFiles;
        result.canMultiType = canMultiType;
        result.hasImageGallery = hasImageGallery;
        result.isDirectCreatable = isDirectCreatable;
        for (DataField field : _fields) {
            result._fields.add(field.copy());
        }

        return result;
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

        Shape inst = (Shape)other;

        if (hashCode() != inst.hashCode()) {
            return false;
        }

        if (!super.equals(other)) {
            return false;
        }

        if (_fields == null) {
            if (inst._fields != null) {
                return false;
            }
        } else {
            if (!_fields.equals(inst._fields)) {
                return false;
            }
        }

        if (_primitive != inst._primitive) {
            return false;
        }

        if (_viewable != inst._viewable) {
            return false;
        }

        if (_canReview != inst._canReview) {
            return false;
        }

        if (_canComment != inst._canComment) {
            return false;
        }

        if (_canDiscuss != inst._canDiscuss) {
            return false;
        }

        if (_canTag != inst._canTag) {
            return false;
        }

        if (_canVote != inst._canVote) {
            return false;
        }

        if (_canPoll != inst._canPoll) {
            return false;
        }

        if (canAttachFiles != inst.canAttachFiles) {
            return false;
        }

        if (canMultiType != inst.canMultiType) {
            return false;
        }

        if (hasImageGallery != inst.hasImageGallery) {
            return false;
        }

        if (isDirectCreatable != inst.isDirectCreatable) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    public static Shape createInstance(Object source)
    {
        if (source instanceof Shape)
        {
            return (Shape)source;
        }
        
        throw new RuntimeException("something something");
    }
        
    public boolean isNumeric()
    {
        boolean isNumeric = false;
        
        if (isPrimitive())
        {
            String name = getName();
            isNumeric = (name.equals("Number"));
        }
        
        return isNumeric;
    }

    public boolean canMultiType() {
        return canMultiType;
    }

    public void setCanMultiType(boolean val) {
        canMultiType = val;
    }

    public boolean isDirectCreatable() {
        return isDirectCreatable;
    }

    public void setDirectCreatable(boolean val) {
        isDirectCreatable = val;
    }

    public boolean canAttachFiles() {
        return canAttachFiles;
    }

    public void setCanAttachFiles(boolean val) {
        canAttachFiles = val;
    }

    public boolean hasImageGallery() {
        return hasImageGallery;
    }

    public void setHasImageGallery(boolean val) {
        hasImageGallery = val;
    }

    public boolean getCanComment() {
        return _canComment;
    }

    public void setCanComment(boolean canComment) {
        _canComment = canComment;
    }

    public boolean getCanDiscuss() {
        return _canDiscuss;
    }

    public void setCanDiscuss(boolean canDiscuss) {
        _canDiscuss = canDiscuss;
    }

    public boolean getCanPoll() {
        return _canPoll;
    }

    public void setCanPoll(boolean canPoll) {
        _canPoll = canPoll;
    }

    public boolean getCanReview() {
        return _canReview;
    }

    public void setCanReview(boolean canReview) {
        _canReview = canReview;
    }

    public boolean getCanTag() {
        return _canTag;
    }

    public void setCanTag(boolean canTag) {
        _canTag = canTag;
    }

    public boolean getCanVote() {
        return _canVote;
    }

    public void setCanVote(boolean canVote) {
        _canVote = canVote;
    }

    public boolean isVisible() {
        return _viewable;
    }

    public void setVisible(boolean viewable) {
        _viewable = viewable;
    }

    public boolean isPrimitive()
    {
        return _primitive;
    }

    public void setPrimitive(boolean internal)
    {
        _primitive = internal;
    }

    public DataField getField(String name)
    {
        for (DataField field : _fields)
        {
            if (name.equals(field.getName()))
            {
                return field;
            }
        }
        
        return null;
    }
    
    public List<DataField> getFields()
    {
        return _fields;
    }
    
    public void addField(DataField field)
    {
        _fields.add(field);
    }
}
