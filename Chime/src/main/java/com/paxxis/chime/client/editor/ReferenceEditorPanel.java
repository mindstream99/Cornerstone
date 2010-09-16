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

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.paxxis.chime.client.DataInputListener;
import com.paxxis.chime.client.DataInstanceComboBox;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.Shape;

/**
 *
 * @author Robert Englander
 */
public class ReferenceEditorPanel extends LayoutContainer 
{
    public interface EditorPanelChangeListener
    {
        public void onChange();
        public void onComplete();
    }
    
    private EditorPanelChangeListener _listener = null;
    
    private DataInstanceComboBox _editor = null;
    private String _dataType = null;
    private DataInstance _result = null;
    private String _originalText = null;
    
    class FieldWrapper extends LayoutContainer
    {
        public FieldWrapper(DataInstanceComboBox ta)
        {
            init(ta);
        }
        
        private void init(DataInstanceComboBox ta)
        {
            setLayout(new FitLayout());
            setBorders(false);
            setStyleAttribute("background", "white");
            add(ta);
        }
    }
    
    public ReferenceEditorPanel(Shape type) {
        this(type, null);
    }

    public ReferenceEditorPanel(Shape type, String origText) {
        this(type.getId().getValue(), origText);
    }

    public ReferenceEditorPanel(String type, String origText) {
        _dataType = type;
        _originalText = origText;
    }

    protected void onRender(Element parent, int index) { 
    	super.onRender(parent, index);
    	init();
    }
    
    public void setChangeListener(EditorPanelChangeListener listener)
    {
        _listener = listener;
    }

    public DataInstance getResult()
    {
        return _result;
    }

    public void setFocus()
    {
        _editor.focus();
    }
    
    private void init()
    {
        setLayout(new RowLayout());
        setStyleAttribute("background", "transparent");
        setBorders(false);
        
        DataInputListener inputListener = new DataInputListener() {

            public void onDataInstance(DataInstance instance) {
                _result = instance;
                notifyChange(true);
            }

            public void onStringData(String text) {
            }
            
        };
        
        _editor = new DataInstanceComboBox(inputListener, InstanceId.create(_dataType), true, false, true);
        if (_originalText != null) {
            DeferredCommand.addCommand(
                new Command() {
                    public void execute() {
                        _editor.setRawValue(_originalText);
                    }
                }
            );
        }
        
        add(new FieldWrapper(_editor), new RowData(1, 1));

        
        new KeyNav(_editor)
        {
            public void onKeyPress(final ComponentEvent ce)
            {
                notifyChange(false);
            }

            public void onEnter(final ComponentEvent ce)
            {
                //notifyChange(true);
            }
        };
        
        layout(true);
    }

    private void notifyChange(final boolean complete) {
        DeferredCommand.addCommand(
            new Command() {
                public void execute() {
                    if (complete) {
                        _listener.onComplete();
                    } else {
                        _listener.onChange();
                    }
                }
            }
        );
    }
    
    public void initialize()
    {
        DeferredCommand.addCommand(
            new Command()
            {
                public void execute() 
                {
                    setFocus();
                }
            }
        );
    }
}
