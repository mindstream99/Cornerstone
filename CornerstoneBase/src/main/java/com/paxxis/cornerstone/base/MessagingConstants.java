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

package com.paxxis.cornerstone.base;

/**
 *
 * @author Robert Englander
 */
public class MessagingConstants {
    private final static int INVALID = -1;

    public final static int ERRORRESPONSE = 0;
    public final static int ERRORMESSAGE = 1;
    public final static int MULTIREQUEST = 2;
    public final static int MULTIRESPONSE = 3;

    // header values
    private final static int MESSAGETYPE = 1;
    private final static int MESSAGEVERSION = 2;
    private final static int PAYLOADTYPE = 3;
    private final static int GROUPID = 4;
    private final static int GROUPVERSION = 5;
    private final static int REPLYTONAME = 6;

    // payload type values
    private final static int JAVAOBJECTPAYLOAD = 1;
	private final static int JSONOBJECTPAYLOAD = 2;

    protected MessagingConstants() {
    }

    public enum PayloadType {
        Invalid(INVALID),
		JavaObjectPayload(JAVAOBJECTPAYLOAD),
		JsonObjectPayload(JSONOBJECTPAYLOAD);

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
				case JSONOBJECTPAYLOAD:
					result = JsonObjectPayload;
					break;
            }

            return result;
        }
    }

    public enum HeaderConstant {
        Invalid(INVALID),
        MessageType(MESSAGETYPE),
        MessageVersion(MESSAGEVERSION),
        PayloadType(PAYLOADTYPE),
        GroupId(GROUPID),
        GroupVersion(GROUPVERSION),
        ReplyToName(REPLYTONAME);
        
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
                case GROUPID:
                    result = GroupId;
                    break;
                case GROUPVERSION:
                    result = GroupVersion;
                    break;
                case REPLYTONAME:
                    result = ReplyToName;
                    break;
            }

            return result;
        }
    }
}
