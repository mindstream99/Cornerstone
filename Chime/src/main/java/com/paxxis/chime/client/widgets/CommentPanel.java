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

import com.extjs.gxt.ui.client.core.Template;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceResponseObject;
import com.paxxis.chime.client.Utils;
import com.paxxis.chime.client.common.ApplyVoteRequest;
import com.paxxis.chime.client.common.ApplyVoteResponse;
import com.paxxis.chime.client.common.Comment;
import com.paxxis.chime.client.common.User;

/**
 *
 * @author Robert Englander
 */
public class CommentPanel extends LayoutContainer
{
    public interface VoteChangeListener {
        public void onVoteChange();
    }

    private static final String HEADERTEMPLATE = "<div id='review'>"  +
                "<span id='review-subheading'><hr COLOR=\"#e1e1e1\"/><br>{heading}{subHeading}</span></div>";
    
    private static Template _headerTemplate;
    
    private static final String REVIEWTEMPLATE = "<div id='review'>"  +
        "<span id='review-msg'>{comment}</span></div>";
    
    private static Template _reviewTemplate;

    static
    {
        _headerTemplate = new Template(HEADERTEMPLATE);
        _headerTemplate.compile();
        _reviewTemplate = new Template(REVIEWTEMPLATE);
        _reviewTemplate.compile();
    }

    private Comment _comment;
    private InterceptedHtml _htmlHeader;
    private InterceptedHtml _htmlReview;
    private UsefulVoterPanel _voterPanel;
    private VoteChangeListener voteListener;

    public CommentPanel(Comment comment, VoteChangeListener listener)
    {
        _comment = comment;
        voteListener = listener;
        init();
        update();
    }
    
    private void init()
    {
        _htmlHeader = new InterceptedHtml();
        add(_htmlHeader, new FlowData(new Margins(5)));

        _htmlReview = new InterceptedHtml();
        add(_htmlReview, new FlowData(new Margins(5)));

        _voterPanel = new UsefulVoterPanel("Was this helpful to you? ",
            new UsefulVoterPanel.VoteListener() {
                public void onVote(boolean positive) {
                    ApplyVoteRequest req = new ApplyVoteRequest();
                    req.setData(_comment);
                    req.setPositive(positive);

                    sendVoteRequest(req);
                }
            }
        );

        add(_voterPanel, new FlowData(new Margins(0, 5, 2, 20)));
        _voterPanel.setVisible(ServiceManager.getActiveUser() != null);
    }
    
    public void sendVoteRequest(ApplyVoteRequest request)
    {
        final AsyncCallback callback = new AsyncCallback()
        {
            public void onFailure(Throwable arg0)
            {
                ChimeMessageBox.alert("System Error", "Please contact the system administrator.", null);
            }

            public void onSuccess(Object obj)
            {
                ServiceResponseObject<ApplyVoteResponse> response = (ServiceResponseObject<ApplyVoteResponse>)obj;
                if (response.isResponse())
                {
                    _comment = (Comment)response.getResponse().getDataInstance();
                    update();

                    User user = response.getResponse().getUpdatedUser();
                    if (user != null) {
                        ServiceManager.updateActiveUser(user);
                    }

                    if (voteListener != null) {
                        voteListener.onVoteChange();
                    }
                }
                else
                {
                    ChimeMessageBox.alert("Error", response.getError().getMessage(), null);
                }
            }
        };

        request.setUser(ServiceManager.getActiveUser());
        ServiceManager.getService().sendApplyVoteRequest(request, callback);
    }

    public void update()
    {
        Params params = new Params();
        params.set("heading", getHeading(_comment));
        params.set("subHeading", getSubHeading(_comment));
        
        String content = _headerTemplate.applyTemplate(params);
        _htmlHeader.setHtml(content);

        params = new Params();
        params.set("comment", getComment(_comment));
        
        content = _reviewTemplate.applyTemplate(params);
        _htmlReview.setHtml(content);

        _voterPanel.setVisible(ServiceManager.getActiveUser() != null);
    }
    
    private static String getHeading(Comment comment)
    {
        return "Posted by " + Utils.toHoverUrl(comment.getUpdatedBy()) + " on " +
                comment.getUpdated().toLocaleString();
    }

    private static String getSubHeading(Comment comment)
    {
        String s = "";

        int pos = comment.getPositiveCount();
        int neg = comment.getNegativeCount();
        int cnt = pos + neg;

        if (cnt == 1) {
            s = "<br>" + pos + " of " + cnt + " person found this helpful";
        } else if (cnt > 1) {
            s = "<br>" + pos + " of " + cnt + " people found this helpful";
        }

        return s;
    }

    private static String getComment(Comment comment)
    {
        StringBuffer buffer = new StringBuffer();
        
        buffer.append(comment.getDescription() + "<br>");
        
        String temp = buffer.toString();
        temp = temp.replaceAll("<ul", "<ul class='forceul'");
        temp = temp.replaceAll("<ol", "<ol class='forceol'");
        return temp;
    }
    
}
