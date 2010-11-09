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
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class User extends DataInstance {
    private static final long serialVersionUID = 1L;

    public static final String COMMUNITY_FIELD = "Community";
    public static final String COMMUNITYMODERATOR_FIELD = "Community Moderator";
    public static final String EMAILADDR_FIELD = "Email Address";
    public static final String HOME_FIELD = "Home";
    public static final String FAVORITES_FIELD = "Favorites";
    public static final String LOGINID = "Login ID";

    public static final InstanceId SYSTEM = InstanceId.create("19900");
    public static final InstanceId ADMIN = InstanceId.create("19800");


    transient String _password = "";

    private UserProfile profile = new UserProfile();

    private String _sessionToken = null;

    private boolean isIndexing = false;

    private List<DataInstance> favorites = new ArrayList<DataInstance>();

    private UserMessagesBundle messagesBundle = null;

    @Override
    public User copy() {
        User result = super.copy(new User());
        result._password = _password;
        result._sessionToken = _sessionToken;
        result.isIndexing = isIndexing;
        result.favorites = favorites;

        // TBD should this be copied?  Maybe not worth it since
        // profile may go away.
        result.profile = profile;
        return result;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
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

        User inst = (User)other;

        if (hashCode() != inst.hashCode()) {
            return false;
        }

        if (!super.equals(other)) {
            return false;
        }

        if (_password == null) {
            if (inst._password != null) {
                return false;
            }
        } else {
            if (!_password.equals(inst._password)) {
                return false;
            }
        }

        if (favorites == null) {
            if (inst.favorites != null) {
                return false;
            }
        } else {
            if (!favorites.equals(inst.favorites)) {
                return false;
            }
        }

        if (profile == null) {
            if (inst.profile != null) {
                return false;
            }
        } else {
            if (!profile.equals(inst.profile)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public DataSocialContext createNewSocialContext() {
        return new UserSocialContext();
    }

    public UserProfile getProfile() {
        return profile;
    }

    public void setIndexing(boolean val) {
        isIndexing = val;
    }

    public boolean isIndexing() {
        return isIndexing;
    }

    public boolean isAdmin() {
        if (getId().equals(SYSTEM) || getId().equals(ADMIN)) {
            return true;
        }

        // check if this user is a member of the Chime Administrators community
        Shape type = getShapes().get(0);
        DataField field = type.getField(User.COMMUNITY_FIELD);
        List<DataFieldValue> values = getFieldValues(type, field);
        for (DataFieldValue value : values) {
            if (value.getReferenceId().equals(Community.ChimeAdministrators.getId())) {
                return true;
            }
        }

        return false;
    }

    public void setPassword(String password) {
        _password = password;
    }

    public String getPassword() {
        return _password;
    }
    
    public void setSessionToken(String token)
    {
        _sessionToken = token;
    }
    
    public String getSessionToken()
    {
        return _sessionToken;
    }

    public InstanceId getHomePageId() {
        InstanceId id = Dashboard.USERDEFAULT;

        DataInstance home = getHome();
        if (home != null) {
            id = home.getId();
        }

        return id;
    }

    /**
     * This is just a convenience method for retrieving the communities
     * from the Community field.
     * 
     * @return the communities that this user belongs to
     */
    public List<Community> getCommunities() {
        Shape type = getShapes().get(0);
        DataField field = type.getField(User.COMMUNITY_FIELD);
        List<DataFieldValue> values = getFieldValues(type, field);

        HashMap<InstanceId, Community> map = new HashMap<InstanceId, Community>();

        for (DataFieldValue value : values) {
            Community community = new Community();
            community.setId(value.getReferenceId());
            community.addShape(field.getShape());
            community.setName(value.getValue().toString());

            map.put(community.getId(), community);
        }

        List<Community> results = new ArrayList<Community>();
        List<Community> moderated = getModeratedCommunities();
        for (Community comm : moderated) {
            if (!map.containsKey(comm.getId())) {
                results.add(comm);
            }
        }

        results.addAll(map.values());

        return results;
    }

    public List<Community> getModeratedCommunities() {
        Shape type = getShapes().get(0);
        DataField field = type.getField(User.COMMUNITYMODERATOR_FIELD);
        List<DataFieldValue> values = getFieldValues(type, field);

        List<Community> results = new ArrayList<Community>();

        for (DataFieldValue value : values) {
            Community community = new Community();
            community.setId(value.getReferenceId());
            community.addShape(field.getShape());
            community.setName(value.getValue().toString());

            results.add(community);
        }

        return results;
    }

    public String getEmailAddress() {
        Shape type = getShapes().get(0);
        DataField field = type.getField(User.EMAILADDR_FIELD);
        List<DataFieldValue> values = getFieldValues(type, field);
        String email = "";
        if (values.size() > 0) {
            email = values.get(0).getValue().toString();
        }

        return email;
    }

    public String getLoginId() {
        Shape type = getShapes().get(0);
        DataField field = type.getField(User.LOGINID);
        List<DataFieldValue> values = getFieldValues(type, field);
        String id = "";
        if (values.size() > 0) {
            id = values.get(0).getValue().toString();
        }

        return id;
    }

    public DataInstance getHome() {
        Shape type = getShapes().get(0);
        DataField field = type.getField(User.HOME_FIELD);
        List<DataFieldValue> values = getFieldValues(type, field);

        DataInstance home = null;
        if (values.size() > 0) {
            DataFieldValue value = values.get(0);
            home = new DataInstance();
            home.setId(value.getReferenceId());
            home.addShape(field.getShape());
            home.setName(value.getValue().toString());
        }

        return home;
    }

    public void setFavorites(List<DataInstance> favs) {
        favorites.clear();
        favorites.addAll(favs);
    }

    public List<DataInstance> getFavorites() {
        return favorites;
    }
    
    /**
     * This is just a convenience method for retrieving the Favorites.
     *
     */
    public List<DataInstance> getFavoriteReferences() {
        Shape type = getShapes().get(0);
        DataField field = type.getField(User.FAVORITES_FIELD);
        List<DataFieldValue> values = getFieldValues(type, field);

        List<DataInstance> result = new ArrayList<DataInstance>();

        for (DataFieldValue value : values) {
            DataInstance favorite = new DataInstance();
            favorite.setId(value.getReferenceId());
            favorite.addShape(field.getShape());
            favorite.setName(value.getValue().toString());

            result.add(favorite);
        }

        return result;
    }

    public UserMessagesBundle getUserMessagesBundle() {
        return messagesBundle;
    }

    public void setUserMessagesBundle(UserMessagesBundle bundle) {
        messagesBundle = bundle;
    }

    public static User createInstance(Object source)
    {
        if (source instanceof User)
        {
            return (User)source;
        }
        //else if (source instanceof String)
        //{
        //    return createFromJSON((String)source);
        //}
        
        throw new RuntimeException("something something");
    }
    
}
