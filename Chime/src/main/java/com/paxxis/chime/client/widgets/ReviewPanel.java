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
import com.google.gwt.core.client.GWT;
import com.paxxis.chime.client.ChimeAsyncCallback;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceResponseObject;
import com.paxxis.chime.client.Utils;
import com.paxxis.chime.client.common.ApplyVoteRequest;
import com.paxxis.chime.client.common.ApplyVoteResponse;
import com.paxxis.chime.client.common.Review;
import com.paxxis.chime.client.common.User;

/**
 *
 * @author Robert Englander
 */
public class ReviewPanel extends LayoutContainer
{
    public interface VoteChangeListener {
        public void onVoteChange();
    }

    private static final String STARS = "<img src='resources/images/chime/stars";
    private static final String STARSEND = ".gif' width='55' height='13'/>";

    private static final String HEADERTEMPLATE = "<div id='review'>"  +
        "<hr COLOR=\"#e1e1e1\"/><br>&nbsp;&nbsp;&nbsp;{rating}&nbsp;&nbsp;&nbsp;{heading}<br>" +
        "<span id='review-subheading'>&nbsp;&nbsp;&nbsp;{subHeading}{subHeading2}</span></div>";
    
    private static Template _headerTemplate;
    
    private static final String REVIEWTEMPLATE = "<div id='review'>"  +
        "<span id='review-msg'>{review}</span></div>";
    
    private static Template _reviewTemplate;

    static
    {
        _headerTemplate = new Template(HEADERTEMPLATE);
        _headerTemplate.compile();
        _reviewTemplate = new Template(REVIEWTEMPLATE);
        _reviewTemplate.compile();
    }
    
    private Review _review;
    private InterceptedHtml _htmlHeader;
    private InterceptedHtml _htmlReview;
    private UsefulVoterPanel _voterPanel;
    private VoteChangeListener voteListener;

    public ReviewPanel(Review review, VoteChangeListener listener) {
        _review = review;
        voteListener = listener;
        init();
        update();
    }
    
    private void init()
    {
        LayoutContainer cont = new LayoutContainer();
        //cont.setLayout(new FilledColumnLayout(HorizontalAlignment.LEFT));
        
        _htmlHeader = new InterceptedHtml();
        cont.add(_htmlHeader); //, new FilledColumnLayoutData());

        add(cont, new FlowData(new Margins(2)));

        _htmlReview = new InterceptedHtml();
        add(_htmlReview, new FlowData(new Margins(5, 5, 5, 10)));

        _voterPanel = new UsefulVoterPanel("Was this review helpful to you? ",
            new UsefulVoterPanel.VoteListener() {
                public void onVote(boolean positive) {
                    ApplyVoteRequest req = new ApplyVoteRequest();
                    req.setData(_review);
                    req.setPositive(positive);
                    sendVoteRequest(req);
                }
            }
        );

        add(_voterPanel, new FlowData(new Margins(0, 5, 2, 20)));
    }
    
    public void sendVoteRequest(ApplyVoteRequest request)
    {
        final ChimeAsyncCallback<ServiceResponseObject<ApplyVoteResponse>> callback = 
        		new ChimeAsyncCallback<ServiceResponseObject<ApplyVoteResponse>>() {
            public void onSuccess(ServiceResponseObject<ApplyVoteResponse> response) {
                if (response.isResponse()) {
                    _review = (Review)response.getResponse().getDataInstance();
                    update();

                    User user = response.getResponse().getUpdatedUser();
                    if (user != null) {
                        ServiceManager.updateActiveUser(user);
                    }

                    if (voteListener != null) {
                        voteListener.onVoteChange();
                    }
                } else {
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
        params.set("id", getId(_review));
        params.set("rating", getRating(_review));
        params.set("heading", getHeading(_review));
        params.set("subHeading", getSubHeading(_review));
        params.set("subHeading2", getSubHeading2(_review));
        
        String content = _headerTemplate.applyTemplate(params);
        _htmlHeader.setHtml(content);

        params = new Params();
        params.set("review", getReview(_review));
        
        content = _reviewTemplate.applyTemplate(params);
        _htmlReview.setHtml(content);
        
        _voterPanel.setVisible(ServiceManager.getActiveUser() != null);
    }
    
    private static String getReview(Review rating)
    {
        StringBuffer buffer = new StringBuffer();
        
        String reviewText = rating.getDescription();
        buffer.append(reviewText);
        
        String temp = buffer.toString();
        temp = temp.replaceAll("<ul", "<ul class='forceul'");
        temp = temp.replaceAll("<ol", "<ol class='forceol'");
        return temp;
    }
    
    private static String getHeading(Review rating)
    {
        String heading = "<span class=\"eslink\"><a href=\"" + GWT.getHostPageBaseURL() +
             "#detail:" + rating.getId() + "\" target=\"_self\">" + rating.getName() + "</a></span>";
        return heading;
    }
    
    private static String getSubHeading(Review rating)
    {
        String action = "Created";
        if (rating.getUpdated().after(rating.getCreated()))
        {
            action = "Updated";
        }
        
        return action + " by " + Utils.toHoverUrl(rating.getUpdatedBy()) + " on " +
                rating.getUpdated().toLocaleString();
    }

    private static String getSubHeading2(Review rating)
    {
        String s = "";

        int pos = rating.getPositiveCount();
        int neg = rating.getNegativeCount();
        int cnt = pos + neg;

        if (cnt == 1) {
            s = "<br>&nbsp;&nbsp;&nbsp;" + pos + " of " + cnt + " person found this review helpful";
        } else if (cnt > 1) {
            s = "<br>&nbsp;&nbsp;&nbsp;" + pos + " of " + cnt + " people found this review helpful";
        }

        return s;
    }

    private static String getRating(Review rating)
    {
        float avg = rating.getRating();
        
        int integral = (int)avg;
        double fraction = avg - integral;
        
        String stars = "-" + integral;
        
        if (integral > 0)
        {
            if (fraction > 0.0 && fraction < 0.5)
            {
                stars += "-Q";
            }
            else if (fraction == 0.5)
            {
                stars += "-H";
            }
            else if (fraction > 0.5)
            {
                stars += "-T";
            }
        }
        
        return (STARS + stars + STARSEND);
        
    }
    
    private static String getId(Review rating)
    {
        return rating.getId().getValue();
    }
}
