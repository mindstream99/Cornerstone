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

import com.extjs.gxt.ui.client.widget.Popup;
import com.extjs.gxt.ui.client.widget.Window;
import com.paxxis.chime.client.widgets.ChimeMessageBox;
import com.paxxis.chime.client.widgets.ChimePopup;

/**
 *
 * @author Robert Englander
 */
public class ChimeDialogManager {

    private List<Object> windows = new ArrayList<Object>();
    private static ChimeDialogManager _instance = null;

    private ChimeDialogManager() {

    }

    public static ChimeDialogManager instance() {
        if (_instance == null) {
            _instance = new ChimeDialogManager();
        }

        return _instance;
    }

    public void register(Window w) {
        windows.add(w);
    }

    public void register(ChimeMessageBox box) {
        windows.add(box);
    }

    public void register(ChimePopup popup) {
        windows.add(popup);
    }

    public void unregister(Window w) {
    	windows.remove(w);
    }
    
    public void unregister(ChimeMessageBox box) {
    	windows.remove(box);
    }
    
    public void unregister(ChimePopup popup) {
    	windows.remove(popup);
    }
    
    public boolean isEmpty() {
    	return windows.isEmpty();
    }
    
    public void clear() {
    	
    	// we must work backwards so that the modal stack is maintained
    	int cnt = windows.size();
    	for (int i = (cnt - 1); i >= 0; i--) {
    		Object obj = windows.get(i);
    		if (obj instanceof Window) {
    			Window w = (Window)obj;
    			w.hide();
    		} else if (obj instanceof ChimePopup) {
    			ChimePopup p = (ChimePopup)obj;
    			p.hide();
    		} else {
    			ChimeMessageBox b = (ChimeMessageBox)obj;
    			b.close();
    		}
    	}

        windows.clear();
        ActivityMonitor.instance().pingActive();
    }
}
