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

package com.paxxis.chime.client.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class DataSocialContext implements Serializable
{
    private User _user = null;
    private float _averageRating = 0.0f;
    private int _ratingCount = 0;

    private ReviewsBundle _reviewsBundle = null;
    private Review _userReview = null;
    private CommentsBundle _commentsBundle = null;
    private DiscussionsBundle _discussionsBundle = null;
    
    private List<TagContext> _tagContexts = new ArrayList<TagContext>();
    private List<Scope> _scopes = new ArrayList<Scope>();

    private boolean _hasUserVote = false;
    private boolean _userVote = false;

    private boolean registeredInterest = false;

    public void setRegisteredInterest(boolean interest) {
        registeredInterest = interest;
    }

    public boolean isRegisteredInterest() {
        return registeredInterest;
    }
    
    public void setUserVote(boolean vote) {
        _userVote = vote;
        _hasUserVote = true;
    }

    public boolean getUserVote() {
        return _userVote;
    }

    public void setHasUserVote(boolean has) {
        _hasUserVote = has;
    }
    
    public boolean hasUserVote() {
        return _hasUserVote;
    }
    
    public void setAverageRating(float averageRating)
    {
        _averageRating = averageRating;
    }
    
    public float getAverageRating()
    {
        return _averageRating;
    }
    
    public void setRatingCount(int count)
    {
        _ratingCount = count;
    }
    
    public int getRatingCount()
    {
        return _ratingCount;
    }
    
    public void setReviewsBundle(ReviewsBundle ratings)
    {
        _reviewsBundle = ratings;
    }
    
    public ReviewsBundle getReviewsBundle()
    {
        return _reviewsBundle;
    }
    
    public void setUserReview(Review review)
    {
        _userReview = review;
    }
    
    public Review getUserReview()
    {
        return _userReview;
    }
    
    public void setCommentsBundle(CommentsBundle comments)
    {
        _commentsBundle = comments;
    }

    public CommentsBundle getCommentsBundle()
    {
        return _commentsBundle;
    }

    public void setDiscussionsBundle(DiscussionsBundle discussions)
    {
        _discussionsBundle = discussions;
    }

    public DiscussionsBundle getDiscussionsBundle()
    {
        return _discussionsBundle;
    }

    public void addTagContext(TagContext tagContext)
    {
        _tagContexts.add(tagContext);
    }
    
    public void setTagContexts(List<TagContext> contexts)
    {
        _tagContexts.clear();
        _tagContexts.addAll(contexts);
    }
    
    public List<TagContext> getTagContexts()
    {
        return _tagContexts;
    }

    public void addScope(Scope scope)
    {
        _scopes.add(scope);
    }
    
    public List<Scope> getScopes()
    {
        return _scopes;
    }

    public void setUser(User user)
    {
        _user = user;
    }
    
    public User getUser()
    {
        return _user;
    }
}
