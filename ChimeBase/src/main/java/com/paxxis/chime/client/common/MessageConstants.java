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

import com.paxxis.cornerstone.base.MessagingConstants;

/**
 *
 * @author Robert Englander
 */
public final class MessageConstants extends MessagingConstants {
    // message type values
    public final static int LOGINREQUEST = 2101;
    public final static int LOGINRESPONSE = 2102;
    public final static int LOGOUTREQUEST = 2103;
    public final static int LOGOUTRESPONSE = 2104;

    public final static int PINGREQUEST = 2110;
    public final static int PINGRESPONSE = 2111;

    public final static int ADDCOMMENTREQUEST = 3000;
    public final static int ADDCOMMENTRESPONSE = 3001;
    public final static int APPLYREVIEWREQUEST = 3002;
    public final static int APPLYREVIEWRESPONSE = 3003;
    public final static int APPLYTAGREQUEST = 3004;
    public final static int APPLYTAGRESPONSE = 3005;
    public final static int APPLYVOTEREQUEST = 3006;
    public final static int APPLYVOTERESPONSE = 3007;
    public final static int BUILDINDEXREQUEST = 3008;
    public final static int BUILDINDEXRESPONSE = 3009;
    public final static int COMMENTSREQUEST = 3010;
    public final static int COMMENTSRESPONSE = 3011;
    public final static int CREATEDISCUSSIONREQUEST = 3012;
    public final static int CREATEDISCUSSIONRESPONSE = 3013;
    public final static int DATAINSTANCEEVENT = 3014;
    public final static int DATAINSTANCEREQUEST = 3015;
    public final static int DATAINSTANCERESPONSE = 3016;
    public final static int SHAPEREQUEST = 3017;
    public final static int SHAPERESPONSE = 3018;
    public final static int DISCUSSIONSREQUEST = 3019;
    public final static int DISCUSSIONSRESPONSE = 3020;
    public final static int EDITCOMMUNITYREQUEST = 3021;
    public final static int EDITCOMMUNITYRESPONSE = 3022;
    public final static int EDITDATAINSTANCEREQUEST = 3023;
    public final static int EDITDATAINSTANCERESPONSE = 3024;
    public final static int EDITSHAPEREQUEST = 3025;
    public final static int EDITSHAPERESPONSE = 3026;
    public final static int EDITNAMEDSEARCHREQUEST = 3027;
    public final static int EDITPAGETEMPLATEREQUEST = 3028;
    public final static int EDITPORTALTEMPLATEREQUEST = 3029;
    public final static int EDITUSERREQUEST = 3030;
    public final static int EDITUSERRESPONSE = 3031;
    public final static int FINDINSTANCESREQUEST = 3033;
    public final static int FINDINSTANCESRESPONSE = 3034;
    public final static int FINDTAGSREQUEST = 3035;
    public final static int FINDTAGSRESPONSE = 3036;
    public final static int FINDTYPESREQUEST = 3037;
    public final static int FINDTYPESRESPONSE = 3038;
    public final static int LOCKREQUEST = 3039;
    public final static int LOCKRESPONSE = 3040;
    public final static int REVIEWSREQUEST = 3041;
    public final static int REVIEWSRESPONSE = 3042;
    public final static int RUNCALSCRIPTREQUEST = 3043;
    public final static int RUNCALSCRIPTRESPONSE = 3044;
    public final static int SUBSCRIBEREQUEST = 3045;
    public final static int SUBSCRIBERESPONSE = 3046;
    public final static int UNSUPPORTEDREQUEST = 3047;
    public final static int USERCONTEXTREQUEST = 3048;
    public final static int USERCONTEXTRESPONSE = 3049;
    public final static int USERMESSAGESREQUEST = 3050;
    public final static int USERMESSAGESRESPONSE = 3051;
    public final static int MODIFYSHAPEREQUEST = 3052;
    public final static int MODIFYSHAPERESPONSE = 3053;

    private MessageConstants() {
    }
}
