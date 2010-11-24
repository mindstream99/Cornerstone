/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.paxxis.chime.client;

import com.paxxis.chime.client.common.UserMessagesResponse;
import com.paxxis.cornerstone.common.MessagePayload;
import com.paxxis.cornerstone.service.ResponseListener;
import com.paxxis.cornerstone.service.ResponseProcessor;

/**
 *
 * @author rob
 */
public class UserMessagesResponseProcessor extends ResponseProcessor<UserMessagesResponse>
{
    public UserMessagesResponseProcessor(MessagePayload type, ResponseListener<UserMessagesResponse> listener)
    {
        super(type, listener);
    }

    protected UserMessagesResponse renderMessage(Object payload)
    {
        return (UserMessagesResponse)new UserMessagesResponse().createInstance(payload);
    }
}
 