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
import java.util.Date;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.google.gwt.core.client.GWT;
import com.paxxis.chime.client.common.BackReferencingDataInstance;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.DataInstance.ReviewAction;
import com.paxxis.chime.client.common.DataInstance.TagAction;

/**
 *
 * @author Robert Englander
 */
public class DataInstanceModel extends BaseTreeModel implements Serializable
{
    private static final String STARS = "<img src='resources/images/chime/stars";
    private static final String STARSEND = ".gif' width='55' height='13'/>";
    
    DataInstance _instance;
    boolean _includeLinks;
    int truncateDescriptions = 0;
    boolean topRule;
 
    public DataInstanceModel()
    {
    }
    
    public DataInstanceModel(DataInstance instance, boolean includeLinks, int truncDescriptions, boolean topRule)
    {
        setId(instance.getId().getValue());

        _instance = instance;
        _includeLinks = includeLinks;
        truncateDescriptions = truncDescriptions;
        
        String name;
        if (includeLinks)
        {
            name = Utils.toUrl(instance);
        }
        else
        {
            name = instance.getName();
        }
        
        setName(name);

        String desc = instance.getDescription();
        if (truncateDescriptions > 0) {
            if (desc != null && desc.length() > truncateDescriptions) {
                desc = desc.substring(0, (truncateDescriptions - 1)) + "&nbsp;...";
            }
        }

        setDesc(desc);
        
        String topRuleString = "";
        if (topRule) {
        	topRuleString = "<hr COLOR=\"#f1f1f1\"/>";
        }

        setTopRule(topRuleString);
        
        List<Shape> types = instance.getShapes();
        String txt = "Applied Shapes:";
        for (Shape type : types) {
            if (includeLinks) {
                txt += "&nbsp;&nbsp;&nbsp;" + Utils.toUrl(type);
            } else {
                txt += "&nbsp;&nbsp;&nbsp;" + type.getName();
            }
        }
        setTypes(txt);

        if (instance instanceof BackReferencingDataInstance) {
            txt = "<br>Applied To:";
            BackReferencingDataInstance br = (BackReferencingDataInstance)instance;
            if (includeLinks) {
                txt += "&nbsp;&nbsp;&nbsp;" + Utils.toUrl(br.getBackRefId(), br.getBackRefName());
            } else {
                txt += "&nbsp;&nbsp;&nbsp;" + br.getBackRefName();
            }
            setBackref(txt);
        }

        int cnt = instance.getRatingCount();
        if (cnt == 0)
        {
            txt = "There are no reviews of this data.";
        }
        else if (cnt == 1)
        {
            if (instance.getReviewedAction() == ReviewAction.C)
            {
                txt = "There is 1 review created by " + instance.getReviewedBy().getName() + " on " + instance.getReviewed().toLocaleString();
            }
            else
            {
                txt = "There is 1 review updated by " + instance.getReviewedBy().getName() + " on " + instance.getReviewed().toLocaleString();
            }
        }
        else
        {
            if (instance.getReviewedAction() == ReviewAction.C)
            {
                txt = "There are " + cnt + " reviews, the most recent created by " + instance.getReviewedBy().getName() + " on " + instance.getReviewed().toLocaleString();
            }
            else
            {
                txt = "There are " + cnt + " reviews, the most recent updated by " + instance.getReviewedBy().getName() + " on " + instance.getReviewed().toLocaleString();
            }
        }
        setRatingCount(txt);
        
        if (instance.isTransient()) {
        	setCommentCount("");
        } else {
            cnt = instance.getCommentCount();
            if (cnt == 0)
            {
                //txt = "There are no comments on this data.";
                txt = "No&nbsp;Comments";
            }
            else if (cnt == 1)
            {
                //txt = "There is 1 comment created by " + instance.getCommentedBy().getName() + " on " + instance.getCommented().toLocaleString();
                txt = "1&nbsp;Comment";
            }
            else
            {
                //txt = "There are " + cnt + " comments, the most recent created by " + instance.getCommentedBy().getName() + " on " + instance.getCommented().toLocaleString();
                txt = cnt + "&nbsp;Comments";
            }
            setCommentCount(txt);
        }
        
        cnt = instance.getTagCount();
        if (cnt == 0)
        {
            txt = "There are no tags applied to this data.";
        }
        else if (cnt == 1)
        {
            if (instance.getTaggedAction() == TagAction.A)
            {
                txt = "There is 1 tag applied.  Most recently a tag was applied by " + instance.getTaggedBy().getName() + " on " + instance.getTagged().toLocaleString();
            }
            else
            {
                txt = "There is 1 tag applied.  Most recently a tag was removed by " + instance.getTaggedBy().getName() + " on " + instance.getTagged().toLocaleString();
            }
        }
        else
        {
            if (instance.getTaggedAction() == TagAction.A)
            {
                txt = "There are " + cnt + " tags applied.  Most recently a tag was applied by " + instance.getTaggedBy().getName() + " on " + instance.getTagged().toLocaleString();
            }
            else
            {
                txt = "There are " + cnt + " tags applied.  Most recently a tag was removed by " + instance.getTaggedBy().getName() + " on " + instance.getTagged().toLocaleString();
            }
        }
        setTagCount(txt);

        Date created = instance.getCreated();
        Date updated = instance.getUpdated();
        if (created.equals(updated))
        {
            txt = "Created by " + Utils.toUrl(instance.getCreatedBy()) + " on " + created.toLocaleString();
        }
        else
        {
            txt = "Updated by " + Utils.toUrl(instance.getUpdatedBy()) + " on " + updated.toLocaleString();
        }
        setTimestamp(txt);

        int pos = instance.getPositiveCount();
        int neg = instance.getNegativeCount();
        int count = pos + neg;

        if (count == 1) {
            txt = "<br>" + pos + " of " + count + " person found this data useful";
        } else if (count > 1) {
            txt = "<br>" + pos + " of " + count + " people found this data useful";
        } else {
            txt = "";
        }
        setUsefulness(txt);
        
        float avg = instance.getAverageRating();
        int integral = (int)avg;
        double fraction = avg - integral;

        if (instance.isTransient()) {
        	setRating("");
        	setShortRating("Transient");
        } else if (instance.getShapes().get(0).getCanReview()) {
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

            int ratings = instance.getRatingCount();
            String ratingCount = "No&nbsp;Reviews";
            if (ratings == 1) {
            	ratingCount = "1 Review";
            } else if (ratings > 1) {
            	ratingCount = "" + ratings + "&nbsp;Reviews";
            }
            
            setShortRating(ratingCount);
            
            if (ratings == 0) {
                setRating(""); 
            } else {
                setRating(STARS + stars + STARSEND); 
            }
        } else {
            setRating("");
        }
    }
 
    public String getId() {
    	return get("id");
    }
    
    public void setId(String id) {
    	set("id", id);
    }
    
    public String getName()
    {
        return get("name");
    }
    
    public void setName(String name) 
    {
        set("name", name);
    }

    public String getDesc() {
        return get("desc");
    }

    public void setDesc(String desc) {
        set("desc", desc);
    }

    public String getBackref() {
        return get("backref");
    }

    public void setBackref(String br) {
        set("backref", br);
    }
    
    public String getTypes() {
        return get("types");
    }

    public void setTypes(String types) {
        set("types", types);
    }
        
    public String getDiffs()
    {
        return get("diffs");
    }
    
    public void setDiffs(String diffs)
    {
        set("diffs", diffs);
    }
    
    public String getRating()
    {
        return get("rating");
    }
    
    public void setRating(String rating)
    {
        set("rating", rating);
    }

    public void setShortRating(String rating)
    {
        set("shortRating", rating);
    }

    public String getRatingCount()
    {
        return get("ratingCount");
    }
    
    public void setRatingCount(String count)
    {
        set("ratingCount", count);
    }
    
    public String getCommentCount()
    {
        return get("commentCount");
    }
    
    public void setCommentCount(String count)
    {
        set("commentCount", count);
    }
    
    public String getTagCount()
    {
        return get("tagCount");
    }
    
    public void setTagCount(String count)
    {
        set("tagCount", count);
    }
    
    public String getTimestamp()
    {
        return get("timestamp");
    }

    public void setTimestamp(String stamp)
    {
        set("timestamp", stamp);
    }

    public String getUsefulness()
    {
        return get("usefulness");
    }

    public void setUsefulness(String usefulness)
    {
        set("usefulness", usefulness);
    }

    public void setTopRule(String txt) {
    	set("topRule", txt);
    }
    
    public String getTopRule() {
    	return get("topRule");
    }
    
    public String getDetailsLink()
    {
        return get("detailsLink");
    }
    
    public void setDetailsLink(String link)
    {
        set("detailsLink", link);
    }

    public DataInstance getDataInstance()
    {
        return _instance;
    }

    public void setupDownloadLink() {
        Shape type = _instance.getShapes().get(0);
        if (type.getName().equals("File")) {
            String fileName = _instance.getName();
            if (-1 == fileName.indexOf(".")) {
                List<DataFieldValue> vals = _instance.getFieldValues(type, type.getField("Extension"));
                if (vals.size() > 0) {
                    fileName += ("." + vals.get(0).getName());
                }
            }
            String id = _instance.getFieldValues(type, type.getField("File ID")).get(0).getName();

            String link = GWT.getHostPageBaseURL().trim();
            if (link.endsWith("/")) {
                link = link.substring(0, link.length() - 1);
            }

            link += "/FileManager/" + fileName + "?id=" + id;
            String url = Utils.toExternalUrl(link, "Download...");
            set("downloadLink", url);
        }
    }

    public String getDownloadLink() {
        return get("downloadLink");
    }
    
}

