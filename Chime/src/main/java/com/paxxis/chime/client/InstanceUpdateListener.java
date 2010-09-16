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

package com.paxxis.chime.client;

import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.NamedSearch;
import com.paxxis.chime.client.common.SearchCriteria;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.portal.PortalTemplate;


/**
 *
 * @author Robert Englander
 */
public interface InstanceUpdateListener {
    public enum Type {
        FieldData,
        ImageAdd,
        ImageRemove,
        FileAdd,
        FileRemove,
        Name,
        Description,
        Type,
        AddFieldDefinition,
        PageLayout,
        Refresh,
        NamedSearchChange,
        CommentApplied,
        ReviewApplied,
        DiscussionApplied,
        TagApplied,
        ScopeChange,
        Silent
    }

    public void onUpdate(DataInstance instance, Type type);
    public void onUpdate(NamedSearch instance, SearchCriteria criteria);
    public void onUpdate(DataInstance instance, PortalTemplate template);
    public void onUpdate(Shape instance, DataField field, Type type);
    public void onUpdateLock(DataInstance instance, DataInstance.LockType lockType);
    public void onUpdateSubscription(DataInstance instance, boolean subscribe);
    public void onUpdateFavorite(DataInstance instance, boolean favorite);
}
