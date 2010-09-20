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

/**
 *
 * @author Robert Englander
 */
public class MessageConstants
{
    private final static int INVALID = -1;

    // header values
    private final static int MESSAGETYPE = 1;
    private final static int MESSAGEVERSION = 2;
    private final static int PAYLOADTYPE = 3;

    // payload type values
    private final static int JAVAOBJECTPAYLOAD = 1;

    // message type values
    private final static int ERRORRESPONSE = 0;

    private final static int CYBERNODE_EVENT = 1001;
    private final static int CYBERNODE_WATCHEVENT = 1002;
    private final static int SERVICEINSTANCE_EVENT = 1003;
    private final static int SERVICEINSTANCE_WATCHEVENT = 1004;
    
    private final static int PUBLISHER_ACTIVITYEVENT = 10001;

    private final static int OPSTRING_EVENT = 2001;

    private final static int MULTIREQUEST = 101;
    private final static int MULTIRESPONSE = 102;

    private final static int LOGINREQUEST = 2101;
    private final static int LOGINRESPONSE = 2102;
    private final static int LOGOUTREQUEST = 2103;
    private final static int LOGOUTRESPONSE = 2104;

    private final static int PINGREQUEST = 2110;
    private final static int PINGRESPONSE = 2111;

    private final static int ADDCOMMENTREQUEST = 3000;
    private final static int ADDCOMMENTRESPONSE = 3001;
    private final static int APPLYREVIEWREQUEST = 3002;
    private final static int APPLYREVIEWRESPONSE = 3003;
    private final static int APPLYTAGREQUEST = 3004;
    private final static int APPLYTAGRESPONSE = 3005;
    private final static int APPLYVOTEREQUEST = 3006;
    private final static int APPLYVOTERESPONSE = 3007;
    private final static int BUILDINDEXREQUEST = 3008;
    private final static int BUILDINDEXRESPONSE = 3009;
    private final static int COMMENTSREQUEST = 3010;
    private final static int COMMENTSRESPONSE = 3011;
    private final static int CREATEDISCUSSIONREQUEST = 3012;
    private final static int CREATEDISCUSSIONRESPONSE = 3013;
    private final static int DATAINSTANCEEVENT = 3014;
    private final static int DATAINSTANCEREQUEST = 3015;
    private final static int DATAINSTANCERESPONSE = 3016;
    private final static int SHAPEREQUEST = 3017;
    private final static int SHAPERESPONSE = 3018;
    private final static int DISCUSSIONSREQUEST = 3019;
    private final static int DISCUSSIONSRESPONSE = 3020;
    private final static int EDITCOMMUNITYREQUEST = 3021;
    private final static int EDITCOMMUNITYRESPONSE = 3022;
    private final static int EDITDATAINSTANCEREQUEST = 3023;
    private final static int EDITDATAINSTANCERESPONSE = 3024;
    private final static int EDITSHAPEREQUEST = 3025;
    private final static int EDITSHAPERESPONSE = 3026;
    private final static int EDITNAMEDSEARCHREQUEST = 3027;
    private final static int EDITPAGETEMPLATEREQUEST = 3028;
    private final static int EDITPORTALTEMPLATEREQUEST = 3029;
    private final static int EDITUSERREQUEST = 3030;
    private final static int EDITUSERRESPONSE = 3031;
    private final static int ERRORMESSAGE = 3032;
    private final static int FINDINSTANCESREQUEST = 3033;
    private final static int FINDINSTANCESRESPONSE = 3034;
    private final static int FINDTAGSREQUEST = 3035;
    private final static int FINDTAGSRESPONSE = 3036;
    private final static int FINDTYPESREQUEST = 3037;
    private final static int FINDTYPESRESPONSE = 3038;
    private final static int LOCKREQUEST = 3039;
    private final static int LOCKRESPONSE = 3040;
    private final static int REVIEWSREQUEST = 3041;
    private final static int REVIEWSRESPONSE = 3042;
    private final static int RUNCALSCRIPTREQUEST = 3043;
    private final static int RUNCALSCRIPTRESPONSE = 3044;
    private final static int SUBSCRIBEREQUEST = 3045;
    private final static int SUBSCRIBERESPONSE = 3046;
    private final static int UNSUPPORTEDREQUEST = 3047;
    private final static int USERCONTEXTREQUEST = 3048;
    private final static int USERCONTEXTRESPONSE = 3049;
    private final static int USERMESSAGESREQUEST = 3050;
    private final static int USERMESSAGESRESPONSE = 3051;

    private MessageConstants() {
    }

    /**
     *
     */
    public enum MessageType {
        Invalid(INVALID),
        ErrorResponse(ERRORRESPONSE),
        CybernodeEvent(CYBERNODE_EVENT),
        CybernodeWatchEvent(CYBERNODE_WATCHEVENT),
        ServiceInstanceEvent(SERVICEINSTANCE_EVENT),
        ServiceInstanceWatchEvent(SERVICEINSTANCE_WATCHEVENT),
        PublisherActivityEvent(PUBLISHER_ACTIVITYEVENT),
        OpStringEvent(OPSTRING_EVENT),
        MultiRequest(MULTIREQUEST),
        MultiResponse(MULTIRESPONSE),
        LoginRequest(LOGINREQUEST),
        LoginResponse(LOGINRESPONSE),
        LogoutRequest(LOGOUTREQUEST),
        LogoutResponse(LOGOUTRESPONSE),
        PingRequest(PINGREQUEST),
        PingResponse(PINGRESPONSE),
        AddCommentRequest(ADDCOMMENTREQUEST),
        AddCommentResponse(ADDCOMMENTRESPONSE),
        ApplyReviewRequest(APPLYREVIEWREQUEST),
        ApplyReviewResponse(APPLYREVIEWRESPONSE),
        ApplyTagRequest(APPLYTAGREQUEST),
        ApplyTagResponse(APPLYTAGRESPONSE),
        ApplyVoteRequest(APPLYVOTEREQUEST),
        ApplyVoteResponse(APPLYVOTERESPONSE),
        BuildIndexRequest(BUILDINDEXREQUEST),
        BuildIndexResponse(BUILDINDEXRESPONSE),
        CommentsRequest(COMMENTSREQUEST),
        CommentsResponse(COMMENTSRESPONSE),
        CreateDiscussionRequest(CREATEDISCUSSIONREQUEST),
        CreateDiscussionResponse(CREATEDISCUSSIONRESPONSE),
        DataInstanceEvent(DATAINSTANCEEVENT),
        DataInstanceRequest(DATAINSTANCEREQUEST),
        DataInstanceResponse(DATAINSTANCERESPONSE),
        ShapeRequest(SHAPEREQUEST),
        ShapeResponse(SHAPERESPONSE),
        DiscussionsRequest(DISCUSSIONSREQUEST),
        DiscussionsResponse(DISCUSSIONSRESPONSE),
        EditCommunityRequest(EDITCOMMUNITYREQUEST),
        EditCommunityResponse(EDITCOMMUNITYRESPONSE),
        EditDataInstanceRequest(EDITDATAINSTANCEREQUEST),
        EditDataInstanceResponse(EDITDATAINSTANCERESPONSE),
        EditShapeRequest(EDITSHAPEREQUEST),
        EditShapeResponse(EDITSHAPERESPONSE),
        EditNamedSearchRequest(EDITNAMEDSEARCHREQUEST),
        EditPageTemplateRequest(EDITPAGETEMPLATEREQUEST),
        EditPortalTemplateRequest(EDITPORTALTEMPLATEREQUEST),
        EditUserRequest(EDITUSERREQUEST),
        EditUserResponse(EDITUSERRESPONSE),
        ErrorMessage(ERRORMESSAGE),
        FindInstancesRequest(FINDINSTANCESREQUEST),
        FindInstancesResponse(FINDINSTANCESRESPONSE),
        FindTagsRequest(FINDTAGSREQUEST),
        FindTagsResponse(FINDTAGSRESPONSE),
        FindTypesRequest(FINDTYPESREQUEST),
        FindTypesResponse(FINDTYPESRESPONSE),
        LockRequest(LOCKREQUEST),
        LockResponse(LOCKRESPONSE),
        ReviewsRequest(REVIEWSREQUEST),
        ReviewsResponse(REVIEWSRESPONSE),
        RunCALScriptRequest(RUNCALSCRIPTREQUEST),
        RunCALScriptResponse(RUNCALSCRIPTRESPONSE),
        SubscribeRequest(SUBSCRIBEREQUEST),
        SubscribeResponse(SUBSCRIBERESPONSE),
        UnsupportedRequest(UNSUPPORTEDREQUEST),
        UserContextRequest(USERCONTEXTREQUEST),
        UserContextResponse(USERCONTEXTRESPONSE),
        UserMessagesRequest(USERMESSAGESREQUEST),
        UserMessagesResponse(USERMESSAGESRESPONSE);

        private int value;

        private MessageType(int val) {
            value = val;
        }

        public int getValue() {
            return value;
        }

        public static MessageType valueOf(int val) {
            MessageType result = Invalid;
            switch (val) {
                case ERRORRESPONSE:
                    result = ErrorResponse;
                    break;
                case CYBERNODE_WATCHEVENT:
                    result = CybernodeWatchEvent;
                    break;
                case CYBERNODE_EVENT:
                    result = CybernodeEvent;
                    break;
                case SERVICEINSTANCE_EVENT:
                    result = ServiceInstanceEvent;
                    break;
                case SERVICEINSTANCE_WATCHEVENT:
                    result = ServiceInstanceWatchEvent;
                    break;
                case PUBLISHER_ACTIVITYEVENT:
                    result = PublisherActivityEvent;
                    break;
                case OPSTRING_EVENT:
                    result = OpStringEvent;
                    break;
                case MULTIREQUEST:
                    result = MultiRequest;
                    break;
                case MULTIRESPONSE:
                    result = MultiResponse;
                    break;
                case LOGINREQUEST:
                    result = LoginRequest;
                    break;
                case LOGINRESPONSE:
                    result = LoginResponse;
                    break;
                case LOGOUTREQUEST:
                    result = LogoutRequest;
                    break;
                case LOGOUTRESPONSE:
                    result = LogoutResponse;
                    break;
                case PINGREQUEST:
                    result = PingRequest;
                    break;
                case PINGRESPONSE:
                    result = PingResponse;
                    break;
                case ADDCOMMENTREQUEST:
                    result = AddCommentRequest;
                    break;
                case ADDCOMMENTRESPONSE:
                    result = AddCommentResponse;
                    break;
                case APPLYREVIEWREQUEST:
                    result = ApplyReviewRequest;
                    break;
                case APPLYREVIEWRESPONSE:
                    result = ApplyReviewResponse;
                    break;
                case APPLYTAGREQUEST:
                    result = ApplyTagRequest;
                    break;
                case APPLYTAGRESPONSE:
                    result = ApplyTagResponse;
                    break;
                case APPLYVOTEREQUEST:
                    result = ApplyVoteRequest;
                    break;
                case APPLYVOTERESPONSE:
                    result = ApplyVoteResponse;
                    break;
                case BUILDINDEXREQUEST:
                    result = BuildIndexRequest;
                    break;
                case BUILDINDEXRESPONSE:
                    result = BuildIndexResponse;
                    break;
                case COMMENTSREQUEST:
                    result = CommentsRequest;
                    break;
                case COMMENTSRESPONSE:
                    result = CommentsResponse;
                    break;
                case CREATEDISCUSSIONREQUEST:
                    result = CreateDiscussionRequest;
                    break;
                case CREATEDISCUSSIONRESPONSE:
                    result = CreateDiscussionResponse;
                    break;
                case DATAINSTANCEEVENT:
                    result = DataInstanceEvent;
                    break;
                case DATAINSTANCEREQUEST:
                    result = DataInstanceRequest;
                    break;
                case DATAINSTANCERESPONSE:
                    result = DataInstanceResponse;
                    break;
                case SHAPEREQUEST:
                    result = ShapeRequest;
                    break;
                case SHAPERESPONSE:
                    result = ShapeResponse;
                    break;
                case DISCUSSIONSREQUEST:
                    result = DiscussionsRequest;
                    break;
                case DISCUSSIONSRESPONSE:
                    result = DiscussionsResponse;
                    break;
                case EDITCOMMUNITYREQUEST:
                    result = EditCommunityRequest;
                    break;
                case EDITCOMMUNITYRESPONSE:
                    result = EditCommunityResponse;
                    break;
                case EDITDATAINSTANCEREQUEST:
                    result = EditDataInstanceRequest;
                    break;
                case EDITDATAINSTANCERESPONSE:
                    result = EditDataInstanceResponse;
                    break;
                case EDITSHAPEREQUEST:
                    result = EditShapeRequest;
                    break;
                case EDITSHAPERESPONSE:
                    result = EditShapeResponse;
                    break;
                case EDITNAMEDSEARCHREQUEST:
                    result = EditNamedSearchRequest;
                    break;
                case EDITPAGETEMPLATEREQUEST:
                    result = EditPageTemplateRequest;
                    break;
                case EDITPORTALTEMPLATEREQUEST:
                    result = EditPortalTemplateRequest;
                    break;
                case EDITUSERREQUEST:
                    result = EditUserRequest;
                    break;
                case EDITUSERRESPONSE:
                    result = EditUserResponse;
                    break;
                case ERRORMESSAGE:
                    result = ErrorMessage;
                    break;
                case FINDINSTANCESREQUEST:
                    result = FindInstancesRequest;
                    break;
                case FINDINSTANCESRESPONSE:
                    result = FindInstancesResponse;
                    break;
                case FINDTAGSREQUEST:
                    result = FindTagsRequest;
                    break;
                case FINDTAGSRESPONSE:
                    result = FindTagsResponse;
                    break;
                case FINDTYPESREQUEST:
                    result = FindTypesRequest;
                    break;
                case FINDTYPESRESPONSE:
                    result = FindTypesResponse;
                    break;
                case LOCKREQUEST:
                    result = LockRequest;
                    break;
                case LOCKRESPONSE:
                    result = LockResponse;
                    break;
                case REVIEWSREQUEST:
                    result = ReviewsRequest;
                    break;
                case REVIEWSRESPONSE:
                    result = ReviewsResponse;
                    break;
                case RUNCALSCRIPTREQUEST:
                    result = RunCALScriptRequest;
                    break;
                case RUNCALSCRIPTRESPONSE:
                    result = RunCALScriptResponse;
                    break;
                case SUBSCRIBEREQUEST:
                    result = SubscribeRequest;
                    break;
                case SUBSCRIBERESPONSE:
                    result = SubscribeResponse;
                    break;
                case UNSUPPORTEDREQUEST:
                    result = UnsupportedRequest;
                    break;
                case USERCONTEXTREQUEST:
                    result = UserContextRequest;
                    break;
                case USERCONTEXTRESPONSE:
                    result = UserContextResponse;
                    break;
                case USERMESSAGESREQUEST:
                    result = UserMessagesRequest;
                    break;
                case USERMESSAGESRESPONSE:
                    result = UserMessagesResponse;
                    break;

            }

            return result;
        }
    }

    public enum PayloadType {
        Invalid(INVALID),
        JavaObjectPayload(JAVAOBJECTPAYLOAD);

        private int value;

        private PayloadType(int val) {
            value = val;
        }

        public int getValue() {
            return value;
        }

        public static PayloadType valueOf(int val) {
            PayloadType result = Invalid;
            switch (val) {
                case JAVAOBJECTPAYLOAD:
                    result = JavaObjectPayload;
                    break;
            }

            return result;
        }
    }

    public enum HeaderConstant {
        Invalid(INVALID),
        MessageType(MESSAGETYPE),
        MessageVersion(MESSAGEVERSION),
        PayloadType(PAYLOADTYPE);

        private int value;

        private HeaderConstant(int val) {
            value = val;
        }

        public int getValue() {
            return value;
        }

        public static HeaderConstant valueOf(int val) {
            HeaderConstant result = Invalid;
            switch (val) {
                case MESSAGETYPE:
                    result = MessageType;
                    break;
                case MESSAGEVERSION:
                    result = MessageVersion;
                    break;
                case PAYLOADTYPE:
                    result = PayloadType;
                    break;
            }

            return result;
        }
    }
}
