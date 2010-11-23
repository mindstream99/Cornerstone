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

import java.util.ArrayList;
import java.util.List;

import com.paxxis.chime.client.common.Community;
import com.paxxis.chime.client.common.DataInstanceRequest;
import com.paxxis.chime.client.common.DataInstanceRequest.ClauseOperator;
import com.paxxis.chime.client.common.DataInstanceRequest.SortOrder;
import com.paxxis.chime.client.common.FieldData;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.Parameter;
import com.paxxis.chime.client.common.Scope;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.client.common.UserProfile;
import com.paxxis.chime.client.common.constants.SearchFieldConstants;
import com.paxxis.chime.database.DataSet;
import com.paxxis.chime.database.DatabaseConnection;
import com.paxxis.chime.database.IDataValue;
import com.paxxis.chime.database.StringData;
import com.paxxis.chime.service.InstancesResponse;
import com.paxxis.chime.ldap.LdapContextFactory;
import com.paxxis.chime.service.Tools;
/**
 *
 * @author Robert Englander
 */
public class UserUtils {

    private UserUtils() {
    }

    public static User createUser(String name, String loginId, String description, String password, User user, DatabaseConnection database) throws Exception {
    	return createUser(name, loginId, description, password, null, user, database);
    }
    
    public static User createUser(String name, String loginId, String description, String password, String emailAddress, User user, DatabaseConnection database) throws Exception {

        database.startTransaction();
        User newUser = null;

        try {
            Shape userShape = ShapeUtils.getInstanceById(Shape.USER_ID, database, true);
            List<Shape> shapes = new ArrayList<Shape>();
            shapes.add(userShape);

            // everyone can see the user
            // after creation we need to modify the scope so that the user can edit him/herself
            List<Scope> scopes = new ArrayList<Scope>();
            scopes.add(new Scope(Community.Global, Scope.Permission.R));

            String sqlInserts[] = {"charVal", "'" + password + "'"};

            List<FieldData> dataList = new ArrayList<FieldData>();
            FieldData fieldData = new FieldData();
            fieldData.shape = userShape;
            fieldData.field = userShape.getField(User.LOGINID);
            fieldData.value = loginId;
            dataList.add(fieldData);
            
            if (emailAddress != null && !emailAddress.equals("")){
            	FieldData emailData = new FieldData();
            	emailData.shape = userShape;
            	emailData.field = userShape.getField(User.EMAILADDR_FIELD);
            	emailData.value = emailAddress;
            	dataList.add(emailData);
            }
            
            newUser = (User)DataInstanceUtils.createInstance(shapes, name,
                    description, null, sqlInserts,
                    dataList, scopes, user, database);

            // ok, now modify the scopes
            ScopeUtils.applyScope(newUser.getId(), new Scope(new Community(newUser.getId()), Scope.Permission.RU), database);

            newUser = getUserById(newUser.getId(), user, database);
            database.commitTransaction();
        } catch (Exception e) {
            database.rollbackTransaction();
            throw new Exception(e.getMessage());
        }

        return newUser;
    }

    public static User changePassword(User user, InstanceId userId, String oldPassword, String newPassword, DatabaseConnection database) throws Exception {
        database.startTransaction();
        User result = null;

        try {
            if (!user.isAdmin()) {
                result = (User)DataInstanceUtils.getInstance(userId, user, database, false, true);
                if (!result.getPassword().equals(oldPassword)) {
                    throw new Exception("Old Password is not valid.");
                }
            }

            String sql = "update " + Tools.getTableSet() + " set charVal = " + new StringData(newPassword).asSQLValue() +
                    " where id = '" + userId.getValue() + "'";

            database.executeStatement(sql);
            result = getUserById(userId, user, database);
            HistoryUtils.writeEvent(HistoryUtils.HistoryEventType.Modify, "Password Changed", result, user, database);
            database.commitTransaction();
        } catch (Exception e) {
            database.rollbackTransaction();
            throw new Exception(e.getMessage());
        }

        return result;
    }

    public static User getUserById(InstanceId userId, User user, DatabaseConnection database) throws Exception
    {
        User result = null;

        result = (User)DataInstanceUtils.getInstance(userId, user, database, true, true);

        updateUserProfile(result, database);

        return result;
    }

    public static User getUserByLoginId(String loginId, User user, DatabaseConnection database) throws Exception {

    	// the index doesn't support exact matching, so we use a Contains operator.  login ids can't have spaces,
    	// so this will actually provide the result we want.
    	List<Parameter> params = new ArrayList<Parameter>();
    	Parameter param = new Parameter();
    	param.dataShape = ShapeUtils.getInstanceById(Shape.USER_ID, database, true);

        // if the login id is 'system', we match by name instead of id.  this is done so that
        // therre will always be a way to login.
        if ("system".equals(loginId)) {
            param.fieldName = SearchFieldConstants.NAME;
        } else {
            param.fieldName = User.LOGINID;
        }
    	param.fieldValue = loginId;
    	param.operator = DataInstanceRequest.Operator.Contains;
    	param.subShape = null;
    	params.add(param);
    	InstancesResponse response = SearchUtils.getInstancesByIndex(User.class, params, ClauseOperator.MatchAll, 
    			user, false, false, null, SortOrder.ByName, database);
    	
        User result = null;
        if (!response.list.isEmpty()) {
        	if ("system".equals(loginId)){
        		result = getUserById(((User)response.list.get(0)).getId(), user, database);
        	} else {
	           	for (User aUser : (List<User>)response.list){
	           		aUser = getUserById(aUser.getId(), user, database);
	           		if (loginId.equals(aUser.getLoginId())){
	        			result = aUser;
	        			break;
	        		}
	        	}
        	}
        }
        
        return result;
    }

    public static void updateUserProfile(User user, DatabaseConnection database) throws Exception {
        UserProfile profile = user.getProfile();

        String sql = "select * from Chime.UserProfile where user_id = '" + user.getId() + "'";
        DataSet dataSet = database.getDataSet(sql, true);
        if (dataSet.next()) {
            IDataValue emailAddr = dataSet.getFieldValue("emailAddress");
            IDataValue emailNotif = dataSet.getFieldValue("emailNotification");

            profile.setEmailNotification(emailNotif.asString().equals("Y"));
            profile.setEmailAddress(emailAddr.asString());
        }

        long count = VoteUtils.getCount(user, VoteUtils.Vote.Y, VoteUtils.VoteType.ReviewVotesWritten, database);
        profile.setPositiveReviewVotesWritten(count);

        count = VoteUtils.getCount(user, VoteUtils.Vote.N, VoteUtils.VoteType.ReviewVotesWritten, database);
        profile.setNegativeReviewVotesWritten(count);

        count = VoteUtils.getCount(user, VoteUtils.Vote.Y, VoteUtils.VoteType.CommentVotesWritten, database);
        profile.setPositiveCommentVotesWritten(count);

        count = VoteUtils.getCount(user, VoteUtils.Vote.N, VoteUtils.VoteType.CommentVotesWritten, database);
        profile.setNegativeCommentVotesWritten(count);

        count = VoteUtils.getCount(user, VoteUtils.Vote.Y, VoteUtils.VoteType.ReviewVotesReceived, database);
        profile.setPositiveReviewVotesReceived(count);

        count = VoteUtils.getCount(user, VoteUtils.Vote.N, VoteUtils.VoteType.ReviewVotesReceived, database);
        profile.setNegativeReviewVotesReceived(count);

        count = VoteUtils.getCount(user, VoteUtils.Vote.Y, VoteUtils.VoteType.CommentVotesReceived, database);
        profile.setPositiveCommentVotesReceived(count);

        count = VoteUtils.getCount(user, VoteUtils.Vote.N, VoteUtils.VoteType.CommentVotesReceived, database);
        profile.setNegativeCommentVotesReceived(count);

        dataSet.close();
    }
    
    public static User createLdapUser(String loginId, String password, LdapContextFactory ldap, User user, DatabaseConnection database) throws Exception {
    	User newUser = null;
    	if (ldap.isLdapEnabled() && ldap.isAutoCreate()){
	    	Shape userShape = ShapeUtils.getInstanceById(Shape.USER_ID, database, true);
	    	newUser = ldap.getLdapUser(loginId, password, userShape);
	    	
	    	if (newUser != null){
	    		newUser = createUser(newUser.getName(), loginId, newUser.getName(), "", newUser.getEmailAddress(), user, database);
	    	}
    	}
    	
    	return newUser;
    }

    public static boolean authenticateUser(String loginId, String password, User user, LdapContextFactory ldap){
    	boolean authenticated = false;
    	if (user.isLocalUser() || !ldap.isLdapEnabled()) {
    		authenticated = (password.equals(user.getPassword()));
        } else {
        	if (ldap.getContext(loginId, password)!=null){
	    		authenticated = true;
	    	}    		
        }
    	return authenticated;
    }
}
