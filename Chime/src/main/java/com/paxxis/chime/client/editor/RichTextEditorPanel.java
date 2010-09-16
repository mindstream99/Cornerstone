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

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.RichTextArea.Justification;

/**
 *
 * @author Robert Englander
 */
public class RichTextEditorPanel extends LayoutContainer 
{
    public interface EditorPanelChangeListener
    {
        public void onChange();
    }
    
    private EditorPanelChangeListener _listener = null;
    
    private EditorToolBar _toolbar = null;
    private RichTextArea _editor = null;
    private String _originalText = null;
    private boolean showToolbar = true;
    
    class RichTextField extends LayoutContainer
    {
        public RichTextField(RichTextArea rta)
        {
            init(rta);
        }
        
        private void init(RichTextArea rta)
        {
            setLayout(new FitLayout());
            setBorders(false);
            setStyleAttribute("background", "white");
            add(rta);
        }
    }
    
    public RichTextEditorPanel(String originalText, boolean showToolbar)
    {
        _originalText = originalText;
        this.showToolbar = showToolbar;
        init();
    }

    public void setChangeListener(EditorPanelChangeListener listener)
    {
        _listener = listener;
    }

    public String getHtml()
    {
        return _editor.getHTML();
    }
    
    public String getPlainText()
    {
        return _editor.getText();
    }

    public void setFocus()
    {
        _editor.setFocus(true);
    }
    
    private void init()
    {
        setLayout(new RowLayout());
        setStyleAttribute("background", "transparent");
        setBorders(false);

        if (showToolbar) {
            _toolbar = new EditorToolBar(this);
            add(_toolbar, new RowData(1, -1));
        }
        
        _editor = new RichTextArea();
        add(new RichTextField(_editor), new RowData(1, 1));

        _editor.setHTML(_originalText);
        
        _editor.addMouseListener(
            new MouseListener()
            {
                public void onMouseDown(Widget arg0, int arg1, int arg2) 
                {
                }

                public void onMouseEnter(Widget arg0) 
                {
                }

                public void onMouseLeave(Widget arg0) 
                {
                }

                public void onMouseMove(Widget arg0, int arg1, int arg2) 
                {
                }

                public void onMouseUp(Widget arg0, int arg1, int arg2) 
                {
                    updateFormatState();
                }
            }
        );
        
        _editor.addClickListener(
            new ClickListener()
            {
                public void onClick(Widget w) 
                {
                    updateFormatState();
                }
            }
        );
        
        _editor.addKeyboardListener(
            new KeyboardListener()
            {
                public void onKeyDown(Widget arg0, char arg1, int arg2) 
                {
                }

                public void onKeyPress(Widget arg0, char arg1, int arg2) 
                {
                    updateFormatState();
                }

                public void onKeyUp(Widget arg0, char arg1, int arg2) 
                {
                    updateFormatState();
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
                    updateFormatState();
                }
            }
        );
    }
    
    private void updateFormatState()
    {
        if (showToolbar) {
            boolean set = _editor.getBasicFormatter().isBold();
            _toolbar.setBoldState(set);

            set = _editor.getBasicFormatter().isItalic();
            _toolbar.setItalicState(set);

            set = _editor.getBasicFormatter().isUnderlined();
            _toolbar.setUnderlineState(set);

            /*
            if (_editor.getBasicFormatter().isJustifyCenter())
            {
                _toolbar.setCenterJustifyState();
            }
            else if (_editor.getBasicFormatter().isJustifyRight())
            {
                _toolbar.setRightJustifyState();
            }
            else
            {
                _toolbar.setLeftJustifyState();
            }
			*/
            
            /*
            if (_editor.getExtendedFormatter().isOrderedList())
            {
                _toolbar.setOrderedListState();
            }
            else if (_editor.getExtendedFormatter().isUnorderedList())
            {
                _toolbar.setUnorderedListState();
            }
            else
            {
                _toolbar.setNoListState();
            }
            */
        }

        if (_listener != null)
        {
            _listener.onChange();
        }
    }
    
    public void setLink()
    {
        final LinkPromptWindow w = new LinkPromptWindow();

        Runnable task = new Runnable()
        {
            public void run()
            {
                _editor.getExtendedFormatter().createLink(w.getURL());
            }
        };

        w.show(task);
    }

    public void removeLink()
    {
        _editor.getExtendedFormatter().removeLink();
    }
    
    public void indentLeft()
    {
        _editor.getExtendedFormatter().leftIndent();
    }
    
    public void indentRight()
    {
        _editor.getExtendedFormatter().rightIndent();
    }
    
    public void toggleBold()
    {
        _editor.getBasicFormatter().toggleBold();
    }
    
    public void toggleItalic()
    {
        _editor.getBasicFormatter().toggleItalic();
    }
    
    public void toggleUnderline()
    {
        _editor.getBasicFormatter().toggleUnderline();
    }
    
    public void setLeftJustify()
    {
        _editor.getBasicFormatter().setJustification(Justification.LEFT);
        updateFormatState();
    }
    
    public void setCenterJustify()
    {
        _editor.getBasicFormatter().setJustification(Justification.CENTER);
        updateFormatState();
    }
    
    public void setRightJustify()
    {
        _editor.getBasicFormatter().setJustification(Justification.RIGHT);
        updateFormatState();
    }
    
    public void setOrderedList()
    {
        _editor.getExtendedFormatter().insertOrderedList();
        updateFormatState();
    }
    
    public void setUnorderedList()
    {
        _editor.getExtendedFormatter().insertUnorderedList();
        updateFormatState();
    }
}
