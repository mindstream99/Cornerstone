package com.paxxis.cornerstone.service;

import com.paxxis.cornerstone.base.RequestMessage;
import com.paxxis.cornerstone.base.ResponseMessage;
import com.paxxis.cornerstone.common.MessagePayload;

/**
 * 
 * @author Robert Englander
 *
 */
public class CornerstoneClient {
     
    private MessagePayload payloadType;
    private long timeout;
    private RequestQueueSender sender;
    
    public CornerstoneClient(MessagePayload payloadType, RequestQueueSender sender, long timeout) {
        this.payloadType = payloadType;
        this.sender = sender;
        this.timeout = timeout;
    }
    
	public <REQ extends RequestMessage, RESP extends ResponseMessage<REQ>> RESP 
    						execute(Class<RESP> clazz, REQ request, ResponseHandler<RESP> handler) {
        
    	ServiceBusMessageProducer<REQ> prod = new ServiceBusMessageProducer<REQ>(request);
        RESP response = sender.send(clazz, prod, handler, timeout, payloadType);
        return response;
    }
}

