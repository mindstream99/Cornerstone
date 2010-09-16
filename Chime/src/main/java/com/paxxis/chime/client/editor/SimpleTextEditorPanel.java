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
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;

/**
 *
 * @author Robert Englander
 */
public class SimpleTextEditorPanel extends LayoutContainer 
{
    public interface EditorPanelChangeListener
    {
        public void onChange();
    }
    
    private EditorPanelChangeListener _listener = null;
    
    private TextArea _editor = null;
    private String _originalText = null;
    
    class SimpleTextField extends LayoutContainer
    {
        public SimpleTextField(TextArea ta)
        {
            init(ta);
        }
        
        private void init(TextArea ta)
        {
            setLayout(new FitLayout());
            setBorders(false);
            setStyleAttribute("background", "white");
            add(ta);
        }
    }
    
    public SimpleTextEditorPanel(String originalText)
    {
        _originalText = originalText;
        init();
    }

    public void setChangeListener(EditorPanelChangeListener listener)
    {
        _listener = listener;
    }

    public String getHtml() {
        return _editor.getValue();
    }

    public String getText()
    {
        return _editor.getRawValue();
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
        
        _editor = new TextArea();
        add(new SimpleTextField(_editor), new RowData(1, 1));

        _editor.setValue(_originalText);
        
        new KeyNav(_editor)
        {
            public void onKeyPress(final ComponentEvent ce)
            {
                notifyChange();
            }
        };
    }

    private void notifyChange() {
        DeferredCommand.addCommand(
            new Command() {
                public void execute() {
                    _listener.onChange();
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
