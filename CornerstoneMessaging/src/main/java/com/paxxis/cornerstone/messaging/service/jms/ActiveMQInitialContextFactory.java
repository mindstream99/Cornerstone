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

package com.paxxis.cornerstone.messaging.service.jms;

import com.paxxis.cornerstone.service.JndiInitialContextFactory;

/**
 * @author Robert Englander
 */
public class ActiveMQInitialContextFactory extends JndiInitialContextFactory {

	private static final String QMARK = "?";
	private static final String AMP = "&";
	private static final String FAILOVER = "failover:";
	private static final String FAILOVERSENDTIMEOUT = "timeout=";
	private static final String USEASYNC = "jms.useAsyncSend=";
	private static final String TRACKMSGS = "trackMessages=";
	private static final String RECONNECTATTEMPTS = "maxReconnectAttempts=";
	private static final int DEFAULTMAXATTEMPTS = 0;
	private static final int DEFAULTFAILOVERSENDTIMEOUT = 1000;
	
	private enum ThreeState {
		True,
		False,
		None
	}
	
	private boolean failover = false;
    private ThreeState asyncSend = ThreeState.None;
    private ThreeState trackMessages = ThreeState.None;
    private int maxConnectionAttempts = DEFAULTMAXATTEMPTS;
    private int failoverSendTimeout = DEFAULTFAILOVERSENDTIMEOUT;

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

    	if (failover) {
    		if (maxConnectionAttempts > 0) {
			   fullUrl.append(psep).append(RECONNECTATTEMPTS).append(maxConnectionAttempts);
			   psep = AMP;
    		}
    		
			if (failoverSendTimeout > -1) {
			   fullUrl.append(psep).append(FAILOVERSENDTIMEOUT).append(failoverSendTimeout);
			   psep = AMP;
    		}
    	}

    	switch (asyncSend) {
	    	case True:
	    	case False:
	    		fullUrl.append(psep).append(USEASYNC).append(asyncSend.name().toLowerCase());
	    		psep = AMP;
	    		break;
	    	case None:
	    		break;
    	}
    	
    	if (failover) {
        	switch (trackMessages) {
		    	case True:
		    	case False:
		    		fullUrl.append(psep).append(TRACKMSGS).append(trackMessages.name().toLowerCase());
		    		break;
		    	case None:
		    		break;
	    	}
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

    public void setMaxConnectionAttempts(int val) {
    	if (val < 0) {
    		throw new RuntimeException("Max Connection Attempts can't be less than 0");
    	}
    	
    	maxConnectionAttempts = val;
    }

    public int getMaxConnectionAttempts() {
    	return maxConnectionAttempts;
    }
    
    public void setFailoverSendTimeout(int val) {
    	if (val < 1000 && val != -1) {
    		throw new RuntimeException("Failover Send Timeout must be either -1 or greater/equal to 1000");
    	}
    	
    	failoverSendTimeout = val;
    }

    public int getFailoverSendTimeout() {
    	return failoverSendTimeout;
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
