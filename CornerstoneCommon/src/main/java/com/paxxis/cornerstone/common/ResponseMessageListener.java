package com.paxxis.cornerstone.common;

import javax.jms.Message;
import javax.jms.MessageListener;

import com.paxxis.cornerstone.base.RequestMessage;
import com.paxxis.cornerstone.base.ResponseMessage;

public abstract class ResponseMessageListener<RESP extends ResponseMessage<? extends RequestMessage>> 
        implements MessageListener {

    @Override
    public void onMessage(Message message) {
        //do nothing...
    }

    public abstract void onResponseMessage(RESP responseMessage);
    
}
