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

package com.paxxis.cornerstone.service;

/**
 * @author Robert Englander
 */
public class ActiveMQInitialContextFactory extends JndiInitialContextFactory {

	private static final String QMARK = "?";
	private static final String AMP = "&";
	private static final String FAILOVER = "failover:";
	private static final String USEASYNC = "jms.useAsyncSend=";
	private static final String TRACKMSGS = "trackMessages=";
	
	private enum ThreeState {
		True,
		False,
		None
	}
	
	private boolean failover = false;
    private ThreeState asyncSend = ThreeState.None;
    private ThreeState trackMessages = ThreeState.None;
    

	public ActiveMQInitialContextFactory() {
		super();
	}
	
    protected String prepareProviderUrl(String url) {
    	StringBuilder fullUrl = new StringBuilder();
    	if (failover) {
    		fullUrl.append(FAILOVER).append("(").append(url).append(")");
    	} else {
    		fullUrl.append(url);
    	}

    	String psep = QMARK;
    	
    	switch (asyncSend) {
	    	case True:
	    	case False:
	    		fullUrl.append(psep).append(USEASYNC).append(asyncSend.name().toLowerCase());
	    		psep = AMP;
	    		break;
	    	case None:
	    		break;
    	}
    	
    	switch (trackMessages) {
	    	case True:
	    	case False:
	    		fullUrl.append(psep).append(TRACKMSGS).append(trackMessages.name().toLowerCase());
	    		break;
	    	case None:
	    		break;
    	}

    	return fullUrl.toString();
    }

    protected ThreeState getThreeStateValue(String val) {
    	ThreeState value = ThreeState.None;
    	if (ThreeState.True.name().equalsIgnoreCase(val)) {
    		value = ThreeState.True;
    	} else if (ThreeState.False.name().equalsIgnoreCase(val)) {
    		value = ThreeState.False;
    	}
    	
    	return value;
    }

    public void setFailover(boolean val) {
    	failover = val;
    }
    
    public boolean getFailover() {
    	return failover;
    }
    
    public void setUseAsyncSend(boolean val) {
		asyncSend = getThreeStateValue(Boolean.toString(val));
    }
    
    public boolean getUseAsyncSend() {
    	return asyncSend.equals(ThreeState.True);
    }
    
    public void setTrackMessages(boolean val) {
		trackMessages = getThreeStateValue(Boolean.toString(val));
    }
    
    public boolean getTrackMessages() {
    	return trackMessages.equals(ThreeState.True);
    }
    
}