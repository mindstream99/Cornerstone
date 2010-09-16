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

package com.paxxis.chime.client.widgets;

import com.paxxis.chime.client.FilledColumnLayout;
import com.paxxis.chime.client.FilledColumnLayoutData;
import com.paxxis.chime.client.Utils;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.core.Template;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.google.gwt.core.client.GWT;
import com.paxxis.chime.client.common.Discussion;
import com.paxxis.chime.client.common.User;
import java.util.Date;

/**
 *
 * @author Robert Englander
 */
public class DiscussionPanel extends LayoutContainer {

    private static final String HEADERTEMPLATE = "<div id='discussion'>"  +
        "&nbsp;&nbsp;&nbsp;{heading}<br>" +
        "<span id='discussion-subheading'>&nbsp;&nbsp;&nbsp;{subHeading}</span></div>";

    private static Template _headerTemplate;

    private static Template _reviewTemplate;

    static
    {
        _headerTemplate = new Template(HEADERTEMPLATE);
        _headerTemplate.compile();
    }

    private Discussion _discussion;
    private InterceptedHtml _htmlHeader;

    public DiscussionPanel(Discussion discussion)
    {
        _discussion = discussion;
        init();
        update();
    }

    private void init()
    {
        LayoutContainer cont = new LayoutContainer();
        cont.setLayout(new FilledColumnLayout(HorizontalAlignment.LEFT));

        _htmlHeader = new InterceptedHtml();
        cont.add(_htmlHeader, new FilledColumnLayoutData());

        add(cont, new FlowData(new Margins(2)));
    }

    public void update()
    {
        Params params = new Params();
        params.set("heading", getHeading(_discussion));
        params.set("subHeading", getSubHeading(_discussion));

        String content = _headerTemplate.applyTemplate(params);
        _htmlHeader.setHtml(content);
    }

    private static String getHeading(Discussion discussion)
    {
        String heading = "<span class=\"eslink\"><a href=\"" + GWT.getHostPageBaseURL() +
             "#detail:" + discussion.getId() + "\" target=\"_self\">" + discussion.getName() + "</a></span>";
        return heading;
    }

    private static String getSubHeading(Discussion discussion)
    {
        User user = discussion.getCommentedBy();
        Date dt = discussion.getCommented();

        int count = discussion.getCommentCount();
        String txt = count + " post";
        if (count != 1) {
            txt += "s.  Most recent by " + Utils.toHoverUrl(user) + " on " + dt.toLocaleString();
        } else {
            txt += " by " + Utils.toHoverUrl(user) + " on " + dt.toLocaleString();
        }

        return txt;
    }

}
