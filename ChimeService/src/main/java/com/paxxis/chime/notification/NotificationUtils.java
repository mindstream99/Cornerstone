
package com.paxxis.chime.notification;

import com.paxxis.chime.client.common.InstanceId;

/**
 *
 * @author rob
 */
public class NotificationUtils {

    private static final String PLUS = "<img src='resources/images/chime/bulletdown.png' width='11' height='11'/>";

    private NotificationUtils() {}

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

    public static String toHoverUrl(String root, InstanceId instanceId, String text) {
        String url = "<span style=\"white-space: normal;\" class=\"eslink\"><a chime-hover=\"true\" hover=\"text-decoration: underline; color: red\" href=\"" + root +
             "#detail:" + instanceId + "\" target=\"_self\">" + buildText(text, false) + "</a></span>";

        return url;
    }

    public static String toChimeUrl(InstanceId instanceId, String text) {
        String url = "<a href=\"chime://#detail:" + instanceId + "\">" + text + "</a>";

        return url;
    }

}
