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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.Listener;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.paxxis.chime.client.common.AddCommentRequest;
import com.paxxis.chime.client.common.AddCommentResponse;
import com.paxxis.chime.client.common.ApplyReviewRequest;
import com.paxxis.chime.client.common.ApplyReviewResponse;
import com.paxxis.chime.client.common.ApplyTagRequest;
import com.paxxis.chime.client.common.ApplyTagResponse;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.EditDataInstanceRequest;
import com.paxxis.chime.client.common.EditDataInstanceResponse;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.client.widgets.ChimeMessageBox;
import com.paxxis.chime.client.widgets.ChimeMessageBoxEvent;
import com.paxxis.chime.client.widgets.LoginWindow;

/**
 *
 * @author Robert Englander
 */
public class ServiceManager 
{
    private static final String LOGINCOOKIENAME = "ChimeCookie";
    
    private static boolean showingServiceAlert = false;
    
    private static User _user = null;
    private static ArrayList<ServiceManagerListener> _listeners = new ArrayList<ServiceManagerListener>();
    
    private ServiceManager()
    {
    }

    public static boolean isLoggedIn() {
        return _user != null;
    }
    
    public static User getActiveUser()
    {
        return _user;
    }

    public static void updateActiveUser(User user) {
        if (_user != null && _user.getId().equals(user.getId())) {
            user.setSessionToken(_user.getSessionToken());
            _user = user;
        }
    }

    public static boolean isAdminLoggedIn() {
        boolean result = false;
        if (_user != null) {
            result = _user.isAdmin();
        }

        return result;
    }

    public static void addListener(ServiceManagerListener listener)
    {
        synchronized (_listeners)
        {
            if (!_listeners.contains(listener))
            {
                _listeners.add(listener);
            }
        }
    }
    
    public static void removeListener(ServiceManagerListener listener)
    {
        synchronized (_listeners)
        {
            if (_listeners.contains(listener))
            {
                _listeners.remove(listener);
            }
        }
    }
    
    private static void notifyLoginResponse(LoginResponseObject resp)
    {
        List<ServiceManagerListener> listeners;
        synchronized (_listeners)
        {
            listeners = (List<ServiceManagerListener>)_listeners.clone();
        }
        
        for (ServiceManagerListener listener : listeners)
        {
            listener.onLoginResponse(resp);
        }
    }
    
    private static void notifyLogout()
    {
        List<ServiceManagerListener> listeners;
        synchronized (_listeners)
        {
            listeners = (List<ServiceManagerListener>)_listeners.clone();
        }
        
        for (ServiceManagerListener listener : listeners)
        {
            listener.onLogout();
        }
    }

    private static void notifyDataInstanceUpdate(DataInstance instance)
    {
        List<ServiceManagerListener> listeners;
        synchronized (_listeners)
        {
            listeners = (List<ServiceManagerListener>)_listeners.clone();
        }
        
        for (ServiceManagerListener listener : listeners)
        {
            listener.onDataInstanceUpdated(instance);
        }
    }
    
    public static void handleServiceError(Throwable t) {
    	if (t instanceof IncompatibleRemoteServiceException) {
    		ServiceManager.handleIncompatibleService();
    	} else {
    		String msg = "The Chime service may be down, or the version of Chime you are using may not be up to date." +
    					 "<br><br>Please refresh your browser or clear the browser cache and try again." +
    					 "  If this problem persists, please contact your system administrator.";
    		showServiceAlert(msg);
    	}
    }
    
    private static void handleIncompatibleService() {
    	showServiceAlert("The version of Chime you are using is not up to date.<br><br>Please refresh your browser or clear the browser cache and try again.");
    }
    
    public static void clearServiceAlertFlag() {
    	showingServiceAlert = false;
    }
    
    private static void showServiceAlert(String msg) {
    	// the showingServiceAlert flag prevents the alert dialogs
    	// from piling up
    	if (!showingServiceAlert) {
        	showingServiceAlert = true;
        	Listener<ChimeMessageBoxEvent> listener = new Listener<ChimeMessageBoxEvent>() {
    			@Override
    			public void handleEvent(ChimeMessageBoxEvent evt) {
    				logout(true);
    				showingServiceAlert = false;
    			}
        	};
        	
        	ChimeMessageBox.alert("Chime", msg, listener);
    	}
    }
    
    public static void logout() {
    	logout(false);
    }
    
    public static void logout(boolean abandon) {
        final ChimeAsyncCallback<LogoutResponseObject> callback = new ChimeAsyncCallback<LogoutResponseObject>() {
            public void onFailure(Throwable t) {
            	if (t instanceof IncompatibleRemoteServiceException) {
            		// let's not bother the user at this point.  who knows, maybe the browser will be refreshed
            		// before the next attempt to log in.
            	} else {
                    ServiceManager.handleServiceError(t);
            	}
            }

            public void onSuccess(LogoutResponseObject response) {
            	abandonSession();
            }

        };

        if (abandon) {
        	abandonSession();
        } else {
            ServiceManager.getService().logout(_user, callback);
        }
    }

    private static void abandonSession() {
        _user = null;
        updateLoginCookie();
        ServiceManager.notifyLogout();
    }
    
    public static boolean hasLoginCookie() {
        return null != Cookies.getCookie(LOGINCOOKIENAME);
    }
    
    public static void loginFromCookie()
    {
        String cookie = Cookies.getCookie(LOGINCOOKIENAME);
        if (cookie != null)
        {
            String[] parts = cookie.split(",");
            User user = new User();
            user.setId(InstanceId.create(parts[0]));
            user.setSessionToken(parts[1]);

            final ChimeAsyncCallback<LoginResponseObject> callback = new ChimeAsyncCallback<LoginResponseObject>() {
                public void onSuccess(LoginResponseObject response) {
                    if (response.isResponse()) {
                        _user = response.getResponse().getUser();
                    } else {
                        _user = null;
                    }

                    ServiceManager.notifyLoginResponse(response);
                }

            };
            
            ServiceManager.getService().login(user, callback);
        }
    }
    
    private static void updateLoginCookie()
    {
        if (_user == null)
        {
            Cookies.removeCookie(LOGINCOOKIENAME);
        }
        else
        {
            String value = _user.getId() + "," + _user.getSessionToken();
            Cookies.setCookie(LOGINCOOKIENAME, value);
        }
    }
    
    public static void reLogin(Runnable loginTask, Runnable cancelTask)
    {
        updateLoginCookie();
        LoginWindow w = new LoginWindow("Your session has expired.", loginTask, cancelTask);
        w.show();
    }
    
    public static void login()
    {
        LoginWindow w = new LoginWindow();
        w.show();
    }
    
    public static void login(String name, String pw) {
        final ChimeAsyncCallback<LoginResponseObject> callback = new ChimeAsyncCallback<LoginResponseObject>() {
            public void onSuccess(LoginResponseObject response) { 
                if (response.isResponse()) {
                    _user = response.getResponse().getUser();
                } else {
                    _user = null;
                }

                updateLoginCookie();
                ServiceManager.notifyLoginResponse(response);
            }

        };

        ServiceManager.getService().login(name, pw, callback);
    }

    public static void sendRequest(ApplyTagRequest request)
    {
        final ChimeAsyncCallback<ServiceResponseObject<ApplyTagResponse>> callback = 
        		new ChimeAsyncCallback<ServiceResponseObject<ApplyTagResponse>>() {
            public void onSuccess(ServiceResponseObject<ApplyTagResponse> response) { 
                if (response.isResponse()) {
                    ServiceManager.notifyDataInstanceUpdate(response.getResponse().getDataInstance());
                } else {
                    ChimeMessageBox.alert("Error", response.getError().getMessage(), null);
                }
            }
        };

        request.setUser(getActiveUser());
        ServiceManager.getService().sendApplyTagRequest(request, callback);
    }
    
    public static void sendRequest(ApplyReviewRequest request)
    {
        final ChimeAsyncCallback<ServiceResponseObject<ApplyReviewResponse>> callback = 
        		new ChimeAsyncCallback<ServiceResponseObject<ApplyReviewResponse>>() {
            public void onSuccess(ServiceResponseObject<ApplyReviewResponse> response) { 
                if (response.isResponse()) {
                    ServiceManager.notifyDataInstanceUpdate(response.getResponse().getDataInstance());
                } else {
                    ChimeMessageBox.alert("Error", response.getError().getMessage(), null);
                }
            }
        };

        request.setUser(getActiveUser());
        ServiceManager.getService().sendApplyRatingRequest(request, callback);
    }
    
    public static void sendRequest(EditDataInstanceRequest request) {
        final ChimeAsyncCallback<ServiceResponseObject<EditDataInstanceResponse>> callback = 
        		new ChimeAsyncCallback<ServiceResponseObject<EditDataInstanceResponse>>() {
            public void onSuccess(ServiceResponseObject<EditDataInstanceResponse> response) { 
                if (response.isResponse()) {
                    ServiceManager.notifyDataInstanceUpdate(response.getResponse().getDataInstance());
                } else {
                    ChimeMessageBox.alert("Error", response.getError().getMessage(), null);
                }
            }
        };

        ServiceManager.getService().sendEditDataInstanceRequest(request, callback);
    }
    
    public static void sendRequest(AddCommentRequest request)
    {
        final ChimeAsyncCallback<ServiceResponseObject<AddCommentResponse>> callback = 
        		new ChimeAsyncCallback<ServiceResponseObject<AddCommentResponse>>() {
            public void onSuccess(ServiceResponseObject<AddCommentResponse> response) { 
                if (response.isResponse()) {
                    ServiceManager.notifyDataInstanceUpdate(response.getResponse().getDataInstance());
                } else {
                    ChimeMessageBox.alert("Error", response.getError().getMessage(), null);
                }
            }
        };

        request.setUser(getActiveUser());
        ServiceManager.getService().sendAddCommentRequest(request, callback);
    }

    public static endsliceServiceAsync getService()
    {
        // Create the client proxy. Note that although you are creating the
        // service interface proper, you cast the result to the asynchronous
        // version of
        // the interface. The cast is always safe because the generated proxy
        // implements the asynchronous interface automatically.
        endsliceServiceAsync service = (endsliceServiceAsync) GWT.create(endsliceService.class);

        // Specify the URL at which our service implementation is running.
        // Note that the target URL must reside on the same domain and port from
        // which the host page was served.
        //
        ServiceDefTarget endpoint = (ServiceDefTarget) service;
        String moduleRelativeURL = GWT.getModuleBaseURL() + "chimeservice";
        endpoint.setServiceEntryPoint(moduleRelativeURL);
        return service;
    }
    
    /*

    public static extendedEventServiceAsync getEventService()
    {
        // Create the client proxy. Note that although you are creating the
        // service interface proper, you cast the result to the asynchronous
        // version of
        // the interface. The cast is always safe because the generated proxy
        // implements the asynchronous interface automatically.
        extendedEventServiceAsync service = (extendedEventServiceAsync) GWT.create(extendedEventService.class);

        // Specify the URL at which our service implementation is running.
        // Note that the target URL must reside on the same domain and port from
        // which the host page was served.
        //
        ServiceDefTarget endpoint = (ServiceDefTarget) service;
        String moduleRelativeURL = GWT.getModuleBaseURL() + "gwteventservice";
        endpoint.setServiceEntryPoint(moduleRelativeURL);
        return service;
    }
    */
}
