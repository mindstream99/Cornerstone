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
import com.paxxis.cornerstone.base.InstanceId;

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
        set(TIMESTAMP, userMessage.getTimestamp().toLocaleString());

        // convert chime: protocol urls

        // TODO find out if the replacement of "\n" is needed.  I don't think so.
        // I think they don't exist.
        String html = userMessage.getBody().replaceAll("\n", "<br>");
    	int idx;
    	while (-1 != (idx = html.indexOf("<a href=\"chime://#detail:"))) {
    		// find the instance id
    		int idx2 = idx + 25;
    		int idx3 = idx2 - 1;
    		while (html.charAt(idx3) != '"') {
    			idx3++;
    		}
    		String instStr = html.substring(idx2, idx3);
    		InstanceId instId = InstanceId.create(instStr);
    		idx2 = html.indexOf("</a>",idx3 + 1) + 4;
    		
			String text = html.substring(idx3 + 2, idx2 - 4);
    		
    		String part1 = html.substring(0, idx);
    		String part2 = Utils.toHoverUrl(instId, text);
    		String part3 = html.substring(idx2);
    		html = part1 + part2 + part3;
    	}

        set(BODY, html);
    }

    public UserMessage getuserMessage() {
    	return userMessage;
    }
}
