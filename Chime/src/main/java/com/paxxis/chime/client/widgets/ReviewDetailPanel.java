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
import com.paxxis.chime.client.ChimeAsyncCallback;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceResponseObject;
import com.paxxis.chime.client.common.ApplyVoteRequest;
import com.paxxis.chime.client.common.ApplyVoteResponse;
import com.paxxis.chime.client.common.Review;
import com.paxxis.chime.client.common.User;

/**
 *
 * @author Robert Englander
 */
public class ReviewDetailPanel extends LayoutContainer
{
    private static final String STARS = "<img src='resources/images/chime/stars";
    private static final String STARSEND = ".gif' width='55' height='13'/>";

    private static final String HEADERTEMPLATE = "<div id='review'>"  +
        "{rating}&nbsp;&nbsp;&nbsp;<span id='review-detail-msg'>{review}</span></div>";

    private static Template _headerTemplate;

    static
    {
        _headerTemplate = new Template(HEADERTEMPLATE);
        _headerTemplate.compile();
    }

    private Review _review;
    private InterceptedHtml _htmlHeader;

    public ReviewDetailPanel(Review review) {
        _review = review;
        init();
        update();
    }

    private void init()
    {
        LayoutContainer cont = new LayoutContainer();

        _htmlHeader = new InterceptedHtml();
        cont.add(_htmlHeader, new FlowData(0, 5, 0, 5));

        add(cont, new FlowData(new Margins(2)));
    }

    public void sendVoteRequest(ApplyVoteRequest request)
    {
        final ChimeAsyncCallback<ServiceResponseObject<ApplyVoteResponse>> callback = 
        		new ChimeAsyncCallback<ServiceResponseObject<ApplyVoteResponse>>() {
            public void onSuccess(ServiceResponseObject<ApplyVoteResponse> response) {
                if (response.isResponse()) {
                    _review = (Review)response.getResponse().getDataInstance();

                    User user = response.getResponse().getUpdatedUser();
                    if (user != null) {
                        ServiceManager.updateActiveUser(user);
                    }

                    update();
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
        params.set("rating", getRating(_review));
        params.set("review", getReview(_review));

        String content = _headerTemplate.applyTemplate(params);
        _htmlHeader.setHtml(content);
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
}
