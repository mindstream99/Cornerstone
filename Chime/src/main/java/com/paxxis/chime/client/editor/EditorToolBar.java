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

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;

/**
 *
 * @author Robert Englander
 */
public class EditorToolBar extends ButtonBar
{
    private RichTextEditorPanel _editor;
    private ToggleButton _boldItem;
    private ToggleButton _italicItem;
    private ToggleButton _underlineItem;
    private ToggleButton _leftJustifyItem;
    private ToggleButton _centerJustifyItem;
    private ToggleButton _rightJustifyItem;
    private ToggleButton _orderedListItem;
    private ToggleButton _unorderedListItem;
    private Button _leftIndentItem;
    private Button _rightIndentItem;
    private Button _createLinkItem;
    private Button _removeLinkItem;
    
    public EditorToolBar(RichTextEditorPanel editor)
    {
        _editor = editor;
        init();
    }
    
    private void init()
    {
        setBorders(false);
        setStyleAttribute("background", "transparent");
        setMinButtonWidth(22);

        _boldItem = new ToggleButton();
        _boldItem.setToolTip("Bold");
        _boldItem.setIconStyle("bold16-icon");
        _boldItem.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent evt) 
                {
                    _editor.toggleBold();
                }
            
            }
        );
        
        add(_boldItem);

        _italicItem = new ToggleButton();
        _italicItem.setToolTip("Italic");
        _italicItem.setIconStyle("italic16-icon");
        _italicItem.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent evt) 
                {
                    _editor.toggleItalic();
                }
            
            }
        );
        
        add(_italicItem);

        _underlineItem = new ToggleButton();
        _underlineItem.setToolTip("Underline");
        _underlineItem.setIconStyle("underline16-icon");
        _underlineItem.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent evt) 
                {
                    _editor.toggleUnderline();
                }
            
            }
        );
        
        add(_underlineItem);

        add(new SeparatorToolItem());
        
        _leftJustifyItem = new ToggleButton();
        _leftJustifyItem.setToolTip("Left Justify");
        _leftJustifyItem.setIconStyle("alignLeft16-icon");
        _leftJustifyItem.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent evt) 
                {
                    _editor.setLeftJustify();
                }
            
            }
        );
        
        add(_leftJustifyItem);

        _centerJustifyItem = new ToggleButton();
        _centerJustifyItem.setToolTip("Center Justify");
        _centerJustifyItem.setIconStyle("alignCenter16-icon");
        _centerJustifyItem.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent evt) 
                {
                    _editor.setCenterJustify();
                }
            
            }
        );
        
        add(_centerJustifyItem);

        _rightJustifyItem = new ToggleButton();
        _rightJustifyItem.setToolTip("Right Justify");
        _rightJustifyItem.setIconStyle("alignRight16-icon");
        _rightJustifyItem.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent evt) 
                {
                    _editor.setRightJustify();
                }
            
            }
        );
        
        add(_rightJustifyItem);

        add(new SeparatorToolItem());

        _orderedListItem = new ToggleButton();
        _orderedListItem.setToolTip("Numbered List");
        _orderedListItem.setText("N");
        _orderedListItem.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent evt) 
                {
                    _editor.setOrderedList();
                }
            
            }
        );
        
        add(_orderedListItem);

        _unorderedListItem = new ToggleButton();
        _unorderedListItem.setToolTip("Bullet List");
        _unorderedListItem.setText("*");
        _unorderedListItem.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent evt) 
                {
                    _editor.setUnorderedList();
                }
            
            }
        );
        
        add(_unorderedListItem);

        add(new SeparatorToolItem());

        _leftIndentItem = new Button();
        _leftIndentItem.setToolTip("Indent Left");
        _leftIndentItem.setText("<");
        _leftIndentItem.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent evt) 
                {
                    _editor.indentLeft();
                }
            
            }
        );
        
        add(_leftIndentItem);

        _rightIndentItem = new Button();
        _rightIndentItem.setToolTip("Indent Right");
        _rightIndentItem.setText(">");
        _rightIndentItem.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent evt) 
                {
                    _editor.indentRight();
                }
            
            }
        );
        
        add(_rightIndentItem);

        add(new SeparatorToolItem());

        _createLinkItem = new Button();
        _createLinkItem.setToolTip("Create Link");
        _createLinkItem.setIconStyle("addLink16-icon");
        _createLinkItem.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent evt) 
                {
                    _editor.setLink();
                }
            
            }
        );

        add(_createLinkItem);

        _removeLinkItem = new Button();
        _removeLinkItem.setToolTip("Remove Link");
        _removeLinkItem.setIconStyle("removeLink16-icon");
        _removeLinkItem.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent evt) 
                {
                    _editor.removeLink();
                }
            
            }
        );

        add(_removeLinkItem);
    }
    
    public void setBoldState(boolean isBold)
    {
        _boldItem.toggle(isBold);
    }
    
    public void setItalicState(boolean isItalic)
    {
        _italicItem.toggle(isItalic);
    }
    
    public void setUnderlineState(boolean isUnderline)
    {
        _underlineItem.toggle(isUnderline);
    }
    
    public void setLeftJustifyState()
    {
        _leftJustifyItem.toggle(true);
        _centerJustifyItem.toggle(false);
        _rightJustifyItem.toggle(false);
    }
    
    public void setCenterJustifyState()
    {
        _leftJustifyItem.toggle(false);
        _centerJustifyItem.toggle(true);
        _rightJustifyItem.toggle(false);
    }
    
    public void setRightJustifyState()
    {
        _leftJustifyItem.toggle(false);
        _centerJustifyItem.toggle(false);
        _rightJustifyItem.toggle(true);
    }

    
    public void setOrderedListState()
    {
        _orderedListItem.toggle(true);
        _unorderedListItem.toggle(false);
    }
    
    public void setUnorderedListState()
    {
        _orderedListItem.toggle(false);
        _unorderedListItem.toggle(true);
    }
    
    public void setNoListState()
    {
        _orderedListItem.toggle(false);
        _unorderedListItem.toggle(false);
    }
}
