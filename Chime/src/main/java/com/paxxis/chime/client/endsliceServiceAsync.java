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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.paxxis.chime.client.common.AddCommentRequest;
import com.paxxis.chime.client.common.ApplyReviewRequest;
import com.paxxis.chime.client.common.ApplyTagRequest;
import com.paxxis.chime.client.common.ApplyVoteRequest;
import com.paxxis.chime.client.common.CommentsRequest;
import com.paxxis.chime.client.common.CreateDiscussionRequest;
import com.paxxis.chime.client.common.DataInstanceRequest;
import com.paxxis.chime.client.common.DiscussionsRequest;
import com.paxxis.chime.client.common.EditCommunityRequest;
import com.paxxis.chime.client.common.EditDataInstanceRequest;
import com.paxxis.chime.client.common.EditShapeRequest;
import com.paxxis.chime.client.common.EditUserRequest;
import com.paxxis.chime.client.common.FindInstancesRequest;
import com.paxxis.chime.client.common.LockRequest;
import com.paxxis.chime.client.common.ModifyShapeRequest;
import com.paxxis.chime.client.common.MultiRequest;
import com.paxxis.chime.client.common.PingRequest;
import com.paxxis.chime.client.common.ReviewsRequest;
import com.paxxis.chime.client.common.RunCALScriptRequest;
import com.paxxis.chime.client.common.ShapeRequest;
import com.paxxis.chime.client.common.SubscribeRequest;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.client.common.UserMessagesRequest;


/**
 *
 * @author Robert Englander
 */
public interface endsliceServiceAsync {
    public void sendUserMessagesRequest(UserMessagesRequest request, AsyncCallback callback);
    public void sendEditCommunityRequest(EditCommunityRequest request, AsyncCallback callback);
    public void sendEditUserRequest(EditUserRequest request, AsyncCallback callback);
    public void sendRunCALScriptRequest(RunCALScriptRequest request, AsyncCallback callback);
    public void sendSubscribeRequest(SubscribeRequest request, AsyncCallback callback);
    public void sendLockRequest(LockRequest request, AsyncCallback callback);
    public void sendMultiRequest(MultiRequest request, AsyncCallback callback);
    public void sendPingRequest(PingRequest request, AsyncCallback callback);
    public void sendEditDataInstanceRequest(EditDataInstanceRequest request, AsyncCallback callback);
    public void sendEditDataTypeRequest(EditShapeRequest request, AsyncCallback callback);
    public void sendModifyShapeRequest(ModifyShapeRequest request, AsyncCallback callback);
    public void sendApplyTagRequest(ApplyTagRequest request, AsyncCallback callback);
    public void sendApplyRatingRequest(ApplyReviewRequest request, AsyncCallback callback);
    public void sendCreateDiscussionRequest(CreateDiscussionRequest request, AsyncCallback callback);
    public void sendApplyVoteRequest(ApplyVoteRequest request, AsyncCallback callback);
    public void sendAddCommentRequest(AddCommentRequest request, AsyncCallback callback);
    public void sendRatingsRequest(ReviewsRequest request, AsyncCallback callback);
    public void sendCommentsRequest(CommentsRequest request, AsyncCallback callback);
    public void sendDiscussionsRequest(DiscussionsRequest request, AsyncCallback callback);
    public void sendShapeRequest(ShapeRequest request, AsyncCallback callback);
    public void sendDataInstanceRequest(DataInstanceRequest request, AsyncCallback callback);
    public void sendFindInstancesRequest(FindInstancesRequest request, AsyncCallback callback);
    public void login(String name, String password, AsyncCallback callback);
    public void login(User user, AsyncCallback callback);
    public void logout(User user, AsyncCallback callback);
    public void isReady(AsyncCallback callback);
    public void initialize(AsyncCallback callback);
    public void getBrandingData(AsyncCallback callback);
}
