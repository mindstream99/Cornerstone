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

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceEvent;
import com.paxxis.chime.client.common.Message;
import com.paxxis.chime.client.common.PingRequest;
import com.paxxis.chime.client.common.PingResponse;
import com.paxxis.chime.client.pages.PageManager;
import com.paxxis.chime.client.pages.PortalViewPage;
import com.paxxis.chime.client.widgets.SessionTimeoutPendingWindow;
import com.paxxis.chime.client.widgets.SessionTimeoutPendingWindow.TimeoutPendingListener;

import de.novanic.eventservice.client.event.Event;
import de.novanic.eventservice.client.event.RemoteEventService;
import de.novanic.eventservice.client.event.RemoteEventServiceFactory;
import de.novanic.eventservice.client.event.listener.RemoteEventListener;

/**
 * The activity monitor is responsible for sending pings to the
 * service periodically.  This is done to indicate to the service
 * that the user session is active on the client side.
 * 
 * @author Robert Englander
 */
public class ActivityMonitor {
    private static ActivityMonitor INSTANCE = null; 
    private static final int SESSIONFREQUENCY = 300000;
    private static final int STALEFREQUENCY = 60000;
    
    private boolean _isActive = false;
    private RemoteEventService theRemoteEventService = null;
    private RemoteEventListener eventListener = null;
    
    public static ActivityMonitor instance() {
        if (INSTANCE == null) {
        	INSTANCE = new ActivityMonitor();
        }
        
        return INSTANCE;
    }
    
    private ActivityMonitor() {
        ServiceManager.addListener(
            new ServiceManagerAdapter() {
                public void onLogout() {
                    _isActive = false;
                }
                
                public void onLoginResponse(LoginResponseObject resp) {
            		_isActive = resp.isResponse();
                }
            }
        );
        
        Timer t = new Timer() {
           public void run() {
               processSession();
           } 
        };
        
        t.scheduleRepeating(SESSIONFREQUENCY);

        Timer t2 = new Timer() {
           public void run() {
               processStale();  
           }
        };

        t2.scheduleRepeating(STALEFREQUENCY);
        registerForEvents();
    }

    public void registerForEvents() {
        theRemoteEventService = RemoteEventServiceFactory.getInstance().getRemoteEventService();
    	eventListener = new RemoteEventListener() {
			public void apply(Event eventWrapper) {
				try {
					if (eventWrapper instanceof EventWrapper) {
		            	final Message event = ((EventWrapper)eventWrapper).getMessage();
		            	DeferredCommand.addCommand(
		            		new Command() {
		            			public void execute() {
		                        	if (event instanceof DataInstanceEvent) {
		                        		DataInstance instance = ((DataInstanceEvent)event).getDataInstance();
		                        		PortalViewPage panel = PageManager.instance().getActiveNavigatorPage();            		
		                        		if (panel != null) { 
		                                    panel.compareForRefresh(instance);
		                        		}
		                        	}
		            			}
		            		}
		            	);
					}
				} catch (Exception e) {
				}
            }
        };

        theRemoteEventService.addListener(EventWrapper.DOMAIN, eventListener);
    }
    
    public void setActive() {
        if (ServiceManager.getActiveUser() != null) {
            _isActive = true;
        }
    }
    
    public void pingActive() {
    	setActive();
    	processSession();
    }
    
    private void processSession() {
        final AsyncCallback<ServiceResponseObject<PingResponse>> callback = new AsyncCallback<ServiceResponseObject<PingResponse>>()
        {
            public void onFailure(Throwable arg0)
            {
            }

            @SuppressWarnings("unchecked")
			public void onSuccess(ServiceResponseObject<PingResponse> response)
            {
                if (response.isResponse()) {
                    PingResponse resp = response.getResponse();
                    if (resp.isPendingTimeout()) {
                    	sessionTimeoutPending();
                    } else if (resp.isExpired()) {
						ChimeDialogManager.instance().clear();
						ServiceManager.logout();
                    }
                }
            }
 
        };

        boolean active = _isActive;
        _isActive = false;
        
        // send a ping to the service
        PingRequest request = new PingRequest();
        request.setUser(ServiceManager.getActiveUser());
        request.setUserActivity(active);
        ServiceManager.getService().sendPingRequest(request, callback);
    }

    private void sessionTimeoutPending() {
    	SessionTimeoutPendingWindow w = new SessionTimeoutPendingWindow(
    		new TimeoutPendingListener() {
    			public void keepAlive() {
    				_isActive = true;
    				processSession();
    			}
    		}
    	);
    	w.show();
    }
    
    private void processStale() {
        final AsyncCallback<ServiceResponseObject<PingResponse>> callback = new AsyncCallback<ServiceResponseObject<PingResponse>>()
        {
            public void onFailure(Throwable arg0)
            {
                //ChimeMessageBox.alert("System Error", "Please contact the system administrator.", null);
            }

            public void onSuccess(ServiceResponseObject<PingResponse> response)
            {
                if (response.isResponse())
                {
                    PingResponse resp = response.getResponse();

                    DataInstance inst = resp.getActivePortalInstance();
                    if (inst != null) {
                        // tell the portal page about this instance
                        PageManager.instance().getActiveNavigatorPage().compareForRefresh(inst);
                    }
                    
                    // update the user
                    ServiceManager.updateActiveUser(resp.getUser());
                }
                else 
                {
                }
            }
 
        };

        // send a stale data ping to the service
        PingRequest request = new PingRequest();
        request.setUser(ServiceManager.getActiveUser());

        DataInstance inst = PageManager.instance().getActiveNavigatorInstance();
        if (inst != null) {
            request.setActivePortalInstanceId(inst.getId());
        }

        ServiceManager.getService().sendPingRequest(request, callback);
    }
}














