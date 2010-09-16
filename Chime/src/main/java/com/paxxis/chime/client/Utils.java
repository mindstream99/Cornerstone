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

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.paxxis.chime.client.common.Community;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.Scope;
import com.paxxis.chime.client.common.User;

/**
 *
 * @author Robert Englander
 */
public class Utils {

    private static final String PLUS = "<img src='resources/images/chime/bulletdown.png' width='11' height='11'/>";

    private Utils() {}

    private static String buildText(String txt, boolean usePlus) {
    	txt = txt.trim();
    	int lastSpace = txt.lastIndexOf(' ');
    	String part1;
    	String part2;
    	
    	if (lastSpace == -1) {
    		part1 = "";
    		part2 = txt;
    	} else {
    		part1 = txt.substring(0, lastSpace);
    		part2 = txt.substring(lastSpace);
    	}

    	if (usePlus) {
    		part2 += PLUS;
    	}

    	String result = part1 + "<span style=\"white-space: nowrap;\">" + part2 + "</span>";
    	return result;
    }
    
    public static String toHoverUrlPrivate(InstanceId id, String text) {
        String url;
        if (!ServiceManager.isLoggedIn()) {
            url = "<span>" + text + "</span>";
        } else {
            url = "<span style=\"white-space: normal;\" class=\"eslink2\"><a chime-hover=\"true\" hover=\"text-decoration: underline; color: red\" href=\"" + GWT.getHostPageBaseURL() +
                 "#detail:" + id + "\" target=\"_self\">" + buildText(text, true) + "</a></span>";
        }

        return url;
    }

    public static String toHoverUrlPrivate(DataInstance instance) {
        return toHoverUrlPrivate(instance.getId(), instance.getName());
    }

    public static String toHoverUrl(DataInstance instance) {
        return toHoverUrl(instance, instance.getName());
    }

    public static String toExternalUrl(String url, String text) {
        String newUrl;
        if (url.startsWith("https:") || url.startsWith("http:") || url.startsWith("ftp:") || url.startsWith("mailto:")) {
            newUrl = url;
        } else if (url.contains("@")) {
        	newUrl = "mailto:" + url;
        } else {
            newUrl = "http://" + url;
        }

        String target = "_blank";
        if (newUrl.startsWith("mailto")) {
        	target = "_self";
        }
        
        String result = "<a hover=\"text-decoration: underline; color: red\" href=\"" + newUrl +
             "\" target=\"" + target + "\">" + text.replaceAll(" ", "&nbsp;") + "</a>";

        return result;
    }

    public static String toUrl(InstanceId id, String text) {
        String url;
        if (!ServiceManager.isLoggedIn()) {
            url = "<span>" + text + "</span>";
        } else {
            url = "<span style=\"white-space: normal;\" class=\"eslink\"><a chime-hover=\"true\" hover=\"text-decoration: underline; color: red\" href=\"" + GWT.getHostPageBaseURL() +
        	"#detail:" + id + "\" target=\"_self\">" + buildText(text, false) + "</a></span>";
        }

        return url;
    }

    public static String toUrl(DataInstance instance) {
        boolean isUserType = (instance instanceof User);
        return toUrl(isUserType, instance);
    }

    private static String toUrl(boolean isUserType, DataInstance instance) {
        String url;
        if (isUserType && !ServiceManager.isLoggedIn()) {
            url = "<span>" + instance.getName() + "</span>";
        } else {
            url = "<span style=\"white-space: normal;\" class=\"eslink\"><a chime-hover=\"true\" hover=\"text-decoration: underline; color: red\" href=\"" + GWT.getHostPageBaseURL() +
            	"#detail:" + instance.getId() + "\" target=\"_self\">" + buildText(instance.getName(), false) + "</a></span>";
        }

        return url;
    }


    public static String toHoverUrl(DataInstance instance, String text) {
        boolean isUserType = (instance instanceof User);
        return toHoverUrl(isUserType, instance.getId(), text, "detail");
    }

    public static String toHoverUrl(InstanceId instanceId, String text) {
        return toHoverUrl(false, instanceId, text, "detail");
    }

    public static String toHoverSearchUrl(InstanceId instanceId, String text) {
        return toHoverUrl(false, instanceId, text, "search");
    }

    private static String toHoverUrl(boolean isUserType, InstanceId instanceId, String text, String kind) {
        String url;
        if (isUserType && !ServiceManager.isLoggedIn()) {
            url = "<span>" + text + "</span>";
        } else {
            url = "<span style=\"white-space: normal;\" class=\"eslink\"><a chime-hover=\"true\" hover=\"text-decoration: underline; color: red\" href=\"" + GWT.getHostPageBaseURL() +
                 "#" + kind + ":" + instanceId + "\" target=\"_self\">" + buildText(text, true) + "</a></span>";
        }

        return url;
    }

    /*
    public static boolean isEditable(DataInstance instance) {
        User user = ServiceManager.getActiveUser();
        if (instance == null || user == null) {
            return false;
        }

        // users need to be able to edit their own data
        if (instance.getId().equals(user.getId())) {
        	return true;
        }
        
        return instance.canUpdate(user);
    }
	*/
    
    public static boolean isLockVisible(DataInstance instance) {
        User user = ServiceManager.getActiveUser();
        if (instance == null || user == null) {
            return false;
        }

        List<Community> communities = user.getCommunities();
        communities.add(Community.Global);
        communities.add(new Community(user.getId()));
        boolean isCommunity = instance instanceof Community;

        for (Community comm : communities) {
            if (isMatch(comm, isCommunity, instance.getSocialContext().getScopes())) {
                return true;
            }
        }

        return false;
    }
    
    private static boolean isMatch(Community community, boolean isCommunity, List<Scope> scopes) {
        boolean isMember = false;
        for (Scope scope : scopes) {
        	if ( (isCommunity && scope.getPermission() == Scope.Permission.RM) ||
        		 (!isCommunity && (scope.getPermission() == Scope.Permission.RU || scope.getPermission() == Scope.Permission.RUC))) {
        		
                if (scope.isGlobalCommunity()) {
                    isMember = true;
                    break;
                } else if (scope.getCommunity().getId().equals(community.getId())) {
                    isMember = true;
                    break;
                }
            }
        }

        return isMember;
    }
}
