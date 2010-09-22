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

import java.io.Serializable;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.paxxis.chime.client.common.UserMessage;

/**
 * A data model wrapper around a Chime UserMessage.
 * 
 * @author Robert Englander
 *
 */
public class UserMessageModel extends BaseTreeModel implements Serializable {

	public static final String ID = "id";
	public static final String SUBJECT = "subject";
	public static final String BODY = "body";
	public static final String TIMESTAMP = "timestamp";

	private static final long serialVersionUID = 1L;
	private UserMessage userMessage;

    public UserMessageModel() {
    }

    public UserMessageModel(UserMessage msg) {
        userMessage = msg;
        set(ID, userMessage.getId().getValue());
        set(SUBJECT, userMessage.getSubject());
        set(BODY, userMessage.getBody().replaceAll("\n", "<br>"));
        set(TIMESTAMP, userMessage.getTimestamp().toLocaleString());
    }

    public UserMessage getuserMessage() {
    	return userMessage;
    }
}
