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

import java.util.HashMap;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.HistoryListener;
import com.paxxis.chime.client.pages.PageManager;
import com.paxxis.chime.client.widgets.ChimeMessageBox;

/**
 *
 * @author Robert Englander
 */
public class StateManager implements HistoryListener
{
    private StateChangeListener _listener;
    private HashMap<String, Object> _stateMap = new HashMap<String, Object>();
    private boolean _silent = false;
    private String _silentMessage = null;

    private StateManager()
    {}
    
    private static StateManager _instance = null;
	private static MainContainer mainContainer = null;
	
	public static void setMainContainer(MainContainer main) {
		mainContainer = main;
	}
	
	public static MainContainer getMainContainer() {
		return mainContainer;
	}
	
    public static StateManager instance()
    {
        if (_instance == null)
        {
            _instance = new StateManager();
        }
        
        return _instance;
    }
    
    public void putState(String key, Object value)
    {
        _stateMap.put(key, value);
    }
    
    public Object getState(String key)
    {
        return _stateMap.get(key);
    }
    
    public void backInactive()
    {
        _silent = true;
        back();
    }
    
    public void back()
    {
        History.back();
    }
    
    public void forward()
    {
        History.forward();
    }
    
    public void pushInactiveToken(String token)
    {
        History.newItem(token, false);
    }
    
    public void setListener(StateChangeListener listener)
    {
        _listener = listener;
        init();
    }

    public void goBack(final String msg) {
        _silentMessage = msg;
        StateManager.instance().backInactive();
    }
    
    public void onHistoryChanged(String token) 
    {
    	ChimeDialogManager.instance().clear();
        
        if (ServiceManager.isLoggedIn()) {
        	if (!_silent)
            {
                _listener.onStateChange(token, true);
            }
            else
            {
                _silent = false;
                String msg = _silentMessage;
                _silentMessage = null;
                if (msg != null) {
                    ChimeMessageBox box = ChimeMessageBox.alert("Warning", msg, null);
                    ChimeDialogManager.instance().register(box);
                }
            }
        } else {
            MainContainer.setPendingToken(token);
            PageManager.instance().openNavigator(true);
        }
    }

    private void init()
    {
        History.addHistoryListener(this);
       
        String token = History.getToken();
        onHistoryChanged(token);
    }
}
