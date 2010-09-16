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

package com.paxxis.chime.client.editor;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.paxxis.chime.client.DataInputListener;
import com.paxxis.chime.client.SearchComboBox;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.widgets.ChimeWindow;

/**
 *
 * @author Robert Englander
 */
public class LinkPromptWindow extends ChimeWindow
{
    private Html _info;
    
    private Button _okButton;
    private Button _cancelButton;
    private TextField<String> _url;
    private SearchComboBox searchCombo;
    private CheckBox checkBox;
    
    private FormPanel _form = new FormPanel();
    private Runnable _task;
    private String _resultURL = null;
    private DataInstance resultDataInstance = null;
    
    public LinkPromptWindow()
    {
        super();
    }
    
    protected void init()
    {
        setModal(true);
        setHeading("Create Link");
        setLayout(new FlowLayout());
        setMaximizable(false);
        setMinimizable(false);
        setClosable(false);
        setResizable(false);
        setWidth(375);
        
        _form.setHeaderVisible(false);
        _form.setBorders(false);
        _form.setBodyBorder(false);
        _form.setStyleAttribute("padding", "5");
        _form.setButtonAlign(HorizontalAlignment.CENTER);
        _form.setFrame(false);
        _form.setFieldWidth(320);
        _form.setLabelWidth(35);
        _form.setHideLabels(true);
        
        checkBox = new CheckBox();
        checkBox.setBoxLabel("Link to Chime content");
        checkBox.addListener(Events.Change, 
        	new Listener<FieldEvent>() {
				public void handleEvent(FieldEvent evt) {
					updateState();
				}
        	}
        );
        
        checkBox.setValue(false);
        _form.add(checkBox);
        
        _url = new TextField<String>();
        _url.setFieldLabel("URL");
        
        new KeyNav(_url)
        {
            public void onKeyPress(final ComponentEvent ce)
            {
                updateState();
            }

            public void onEnter(ComponentEvent ce) 
            {
                doSave();
            }
        };
        
        _form.add(_url);
        
        searchCombo = new SearchComboBox(
        	new DataInputListener() {
				public void onDataInstance(DataInstance instance) {
					resultDataInstance = instance;
					updateState();
				}

				public void onStringData(String text) {
					resultDataInstance = null;
					updateState();
				}
        	}, true
        );
    
        _form.add(searchCombo);
        
        _okButton = new Button("Ok");
        _okButton.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent evt) 
                {
                    doSave();
                }
            }
        );
        
        _form.addButton(_okButton);
        
        _cancelButton = new Button("Cancel");
        _cancelButton.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent evt) 
                {
                    doCancel();
                }
            }
        );
        
        _form.addButton(_cancelButton);
        
        add(_form);
        updateState();
    }
    
    public void show(Runnable task)
    {
        super.show();

        _task = task;

        DeferredCommand.addCommand(
            new Command()
            {
                public void execute() 
                {
                    _url.setValue("http://");
                    _url.setCursorPos(7);
                    _url.focus();
                }
            }
        );
        
        updateState();
    }
    
    protected void updateState()
    {
        DeferredCommand.addCommand(
            new Command()
            {
                public void execute() 
                {
                	validate();
                }
            }
        );
    }

    private void validate() {
    	boolean canSave = false;
    	
    	if (checkBox.getValue() == true) {
    		// use quick search
    		searchCombo.setVisible(true);
    		_url.setVisible(false);
    		canSave = (resultDataInstance != null);
    	} else {
    		// use basic url
    		searchCombo.setVisible(false);
    		_url.setVisible(true);
    		canSave = canSave();
    	}
    	
        _okButton.setEnabled(canSave);
    }
    
    protected boolean canSave()
    {
        String name = _url.getRawValue();
        boolean can = false;
        
        if (name != null)
        {
            can = name.trim().length() > 0;
        }
        
        return can;
    }
    
    public String getURL()
    {
        return URL.encode(_resultURL);
    }
    
    protected void doSave()
    {
    	if (checkBox.getValue() == true) {
    		_resultURL = "chime://#detail:" + resultDataInstance.getId();
    	} else {
            _resultURL = _url.getValue().toString();
    	}

    	hide();
        _task.run();
    }
    
    protected void doCancel()
    {
        hide();
    }
    
}
