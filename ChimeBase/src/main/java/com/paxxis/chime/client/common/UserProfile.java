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

/**
 *
 * @author Robert Englander
 */
public class UserProfile implements Serializable {
    private boolean emailNotification = true;
    private String emailAddress = null;

    private long positiveReviewVotesWritten = 0;
    private long negativeReviewVotesWritten = 0;
    private long positiveReviewVotesReceived = 0;
    private long negativeReviewVotesReceived = 0;
    private long positiveCommentVotesWritten = 0;
    private long negativeCommentVotesWritten = 0;
    private long positiveCommentVotesReceived = 0;
    private long negativeCommentVotesReceived = 0;

    public long getNegativeCommentVotesReceived() {
        return negativeCommentVotesReceived;
    }

    public void setNegativeCommentVotesReceived(long negativeCommentVotesReceived) {
        this.negativeCommentVotesReceived = negativeCommentVotesReceived;
    }

    public long getNegativeCommentVotesWritten() {
        return negativeCommentVotesWritten;
    }

    public void setNegativeCommentVotesWritten(long negativeCommentVotesWritten) {
        this.negativeCommentVotesWritten = negativeCommentVotesWritten;
    }

    public long getNegativeReviewVotesReceived() {
        return negativeReviewVotesReceived;
    }

    public void setNegativeReviewVotesReceived(long negativeReviewVotesReceived) {
        this.negativeReviewVotesReceived = negativeReviewVotesReceived;
    }

    public long getNegativeReviewVotesWritten() {
        return negativeReviewVotesWritten;
    }

    public void setNegativeReviewVotesWritten(long negativeReviewVotesWritten) {
        this.negativeReviewVotesWritten = negativeReviewVotesWritten;
    }

    public long getPositiveCommentVotesReceived() {
        return positiveCommentVotesReceived;
    }

    public void setPositiveCommentVotesReceived(long positiveCommentVotesReceived) {
        this.positiveCommentVotesReceived = positiveCommentVotesReceived;
    }

    public long getPositiveCommentVotesWritten() {
        return positiveCommentVotesWritten;
    }

    public void setPositiveCommentVotesWritten(long positiveCommentVotesWritten) {
        this.positiveCommentVotesWritten = positiveCommentVotesWritten;
    }

    public long getPositiveReviewVotesReceived() {
        return positiveReviewVotesReceived;
    }

    public void setPositiveReviewVotesReceived(long positiveReviewVotesReceived) {
        this.positiveReviewVotesReceived = positiveReviewVotesReceived;
    }

    public long getPositiveReviewVotesWritten() {
        return positiveReviewVotesWritten;
    }

    public void setPositiveReviewVotesWritten(long positiveReviewVotesWritten) {
        this.positiveReviewVotesWritten = positiveReviewVotesWritten;
    }


    public UserProfile() {
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public boolean isEmailNotification() {
        return emailNotification;
    }

    public void setEmailNotification(boolean emailNotification) {
        this.emailNotification = emailNotification;
    }
}

