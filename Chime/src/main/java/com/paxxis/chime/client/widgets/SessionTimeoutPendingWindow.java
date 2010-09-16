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

package com.paxxis.chime.client.widgets;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.AbsoluteData;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.google.gwt.user.client.Timer;
import com.paxxis.chime.client.ChimeDialogManager;
import com.paxxis.chime.client.ServiceManager;

/**
 * @author Robert Englander
 *
 */
public class SessionTimeoutPendingWindow extends ChimeWindow {

	public interface TimeoutPendingListener {
		public void keepAlive();
	}
	
    private Button okButton;
    private FormPanel form;
    private Html html;
    private int counter = 30;
    private boolean active = true;
    private TimeoutPendingListener listener;
    private Timer timer;
    
    public SessionTimeoutPendingWindow(TimeoutPendingListener l) {
    	super();
    	listener = l;
		setModal(true);
        setHeading("Session Expiring");
        setMaximizable(false);
        setMinimizable(false);
        setCollapsible(false);
        setClosable(false);
        setResizable(false);
        setWidth(350);
    }
    
    /* (non-Javadoc)
	 * @see com.paxxis.chime.client.widgets.ChimeWindow#init()
	 */
	@Override
	protected void init() {

		form = new FormPanel();
        form.setHeaderVisible(false);
        form.setBorders(false);
        form.setBodyBorder(false);
        form.setStyleAttribute("padding", "5");
        form.setButtonAlign(HorizontalAlignment.CENTER);
        form.setFrame(true);
        form.setFieldWidth(290);
        form.setHideLabels(true);
      
        LayoutContainer lc = new LayoutContainer();
        lc.setLayout(new CenterLayout());
        lc.setHeight(50);
        html = new Html();
        AbsoluteData data = new AbsoluteData();
        data.setMargins(new Margins(5));
        lc.add(html, data);
        form.add(lc);

        okButton = new Button("Ok");
        okButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                	active = false;
                	timer.cancel();
                	hide();
                	listener.keepAlive();
                }
            }
        );

        form.addButton(okButton);
        add(form);
        
        timer = new Timer() {
			@Override
			public void run() {
				if (active) {
					update();
					
					if (counter == 0) {
						ChimeDialogManager.instance().clear();
						ServiceManager.logout();
					}
				}
			}
        };

        update();
        timer.scheduleRepeating(1000);
	}
	
	private void update() {
		html.setHtml("Session will expire in <b><i>" + counter-- + "</i></b> seconds.<br><br>Click Ok to keep your session alive.");
	}
}
