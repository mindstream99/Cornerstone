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
import com.google.gwt.user.client.rpc.RemoteService;
import com.paxxis.chime.client.common.AddCommentRequest;
import com.paxxis.chime.client.common.AddCommentResponse;
import com.paxxis.chime.client.common.ApplyReviewRequest;
import com.paxxis.chime.client.common.ApplyReviewResponse;
import com.paxxis.chime.client.common.ApplyTagRequest;
import com.paxxis.chime.client.common.ApplyTagResponse;
import com.paxxis.chime.client.common.ApplyVoteRequest;
import com.paxxis.chime.client.common.ApplyVoteResponse;
import com.paxxis.chime.client.common.CommentsRequest;
import com.paxxis.chime.client.common.CreateDiscussionRequest;
import com.paxxis.chime.client.common.CreateDiscussionResponse;
import com.paxxis.chime.client.common.DataInstanceRequest;
import com.paxxis.chime.client.common.DiscussionsRequest;
import com.paxxis.chime.client.common.EditCommunityRequest;
import com.paxxis.chime.client.common.EditCommunityResponse;
import com.paxxis.chime.client.common.EditDataInstanceRequest;
import com.paxxis.chime.client.common.EditDataInstanceResponse;
import com.paxxis.chime.client.common.EditShapeRequest;
import com.paxxis.chime.client.common.EditShapeResponse;
import com.paxxis.chime.client.common.EditUserRequest;
import com.paxxis.chime.client.common.EditUserResponse;
import com.paxxis.chime.client.common.FindInstancesRequest;
import com.paxxis.chime.client.common.LockRequest;
import com.paxxis.chime.client.common.LockResponse;
import com.paxxis.chime.client.common.ModifyShapeRequest;
import com.paxxis.chime.client.common.ModifyShapeResponse;
import com.paxxis.chime.client.common.MultiRequest;
import com.paxxis.chime.client.common.MultiResponse;
import com.paxxis.chime.client.common.PingRequest;
import com.paxxis.chime.client.common.PingResponse;
import com.paxxis.chime.client.common.ReviewsRequest;
import com.paxxis.chime.client.common.RunCALScriptRequest;
import com.paxxis.chime.client.common.RunCALScriptResponse;
import com.paxxis.chime.client.common.ShapeRequest;
import com.paxxis.chime.client.common.SubscribeRequest;
import com.paxxis.chime.client.common.SubscribeResponse;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.client.common.UserMessagesRequest;
import com.paxxis.chime.client.common.UserMessagesResponse;

/**
 *
 * @author Robert Englander
 */
public interface endsliceService extends RemoteService
{
    public ServiceResponseObject<UserMessagesResponse> sendUserMessagesRequest(UserMessagesRequest request);
    public ServiceResponseObject<EditCommunityResponse> sendEditCommunityRequest(EditCommunityRequest request);
    public ServiceResponseObject<EditUserResponse> sendEditUserRequest(EditUserRequest request);
    public ServiceResponseObject<RunCALScriptResponse> sendRunCALScriptRequest(RunCALScriptRequest request);
    public ServiceResponseObject<SubscribeResponse> sendSubscribeRequest(SubscribeRequest request);
    public ServiceResponseObject<LockResponse> sendLockRequest(LockRequest request);
    public ServiceResponseObject<MultiResponse> sendMultiRequest(MultiRequest request);
    public ServiceResponseObject<PingResponse> sendPingRequest(PingRequest request);
    public ServiceResponseObject<EditDataInstanceResponse> sendEditDataInstanceRequest(EditDataInstanceRequest request);
    public ServiceResponseObject<EditShapeResponse> sendEditDataTypeRequest(EditShapeRequest request);
    public ServiceResponseObject<ModifyShapeResponse> sendModifyShapeRequest(ModifyShapeRequest request);
    public ServiceResponseObject<ApplyTagResponse> sendApplyTagRequest(ApplyTagRequest request);
    public ServiceResponseObject<ApplyReviewResponse> sendApplyRatingRequest(ApplyReviewRequest request);
    public ServiceResponseObject<CreateDiscussionResponse> sendCreateDiscussionRequest(CreateDiscussionRequest request);
    public ServiceResponseObject<ApplyVoteResponse> sendApplyVoteRequest(ApplyVoteRequest request);
    public ServiceResponseObject<AddCommentResponse> sendAddCommentRequest(AddCommentRequest request);
    public RatingsResponseObject sendRatingsRequest(ReviewsRequest request);
    public CommentsResponseObject sendCommentsRequest(CommentsRequest request);
    public DiscussionsResponseObject sendDiscussionsRequest(DiscussionsRequest request);
    public ShapeResponseObject sendShapeRequest(ShapeRequest request);
    public DataInstanceResponseObject sendDataInstanceRequest(DataInstanceRequest request);
    public FindInstancesResponseObject sendFindInstancesRequest(FindInstancesRequest request);
    public LoginResponseObject login(String name, String password);
    public LoginResponseObject login(User user);
    public LogoutResponseObject logout(User user);
    
    public boolean isReady();
    public void initialize();

    public BrandingData getBrandingData();
}
