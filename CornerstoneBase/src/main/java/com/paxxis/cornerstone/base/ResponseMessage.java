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

package com.paxxis.cornerstone.base;


/**
 * All Response messages should extend ResponseMessage.
 * @author Robert Englander
 */
public abstract class ResponseMessage<D extends RequestMessage> extends Message {
	private static final long serialVersionUID = 2L;
	
	// the associated request message
    private D request = null;

    private ErrorMessage error = null;
    
    private long responseSentOn;
    private long responseReceivedOn;
    
    
    public void setRequest(D request) {
        this.request = request;
    }

    public D getRequest() {
        return request;
    }

	public boolean isError() {
		return error != null;
	}
    
	public void setErrorMessage(ErrorMessage em) {
    	error = em;
    }
    
	public ErrorMessage getErrorMessage() {
    	return error;
    }

    public void setResponseSentOn(long responseSentOn) {
        this.responseSentOn = responseSentOn;
    }

    public long getResponseSentOn() {
        return responseSentOn;
    }

    public void setResponseReceivedOn(long responseReceivedOn) {
        this.responseReceivedOn = responseReceivedOn;
    }

    public long getResponseReceivedOn() {
        return responseReceivedOn;
    }
    
    public long getResponseLatency() {
        return this.responseReceivedOn - this.responseSentOn;
    }
    
    public long getProcessorTime() {
        return this.responseSentOn - this.request.getRequestReceivedOn();
    }
    
    public long getTotalRoundTripTime() {
        return this.responseReceivedOn - this.request.getRequestSentOn();
    }
    
    public String reportTimings() {
        return new StringBuilder()
                        .append(this.request.getRequestLatency())
                        .append(", ")
                        .append(this.getProcessorTime())
                        .append(", ")
                        .append(this.getResponseLatency())
                        .append(", ")
                        .append(this.getTotalRoundTripTime())
                        .toString();
    }
}
