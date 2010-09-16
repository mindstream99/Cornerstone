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

package com.paxxis.chime.client.portal;

import java.io.Serializable;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.fx.FxConfig;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableRowLayout;
import com.paxxis.chime.client.common.portal.PortletSpecification;
import com.paxxis.chime.client.widgets.InterceptedHtml;

/**
 *
 * @author Robert Englander
 */
public class PortletContainer1 extends LayoutContainer
{
    public enum HeaderType {
        Shaded,
        Bordered,
        Transparent,
        None
    }

    /*
    private static final String LABELTEMPLATE = "<div id='data-field-header'>"  +
        "<div class='data-field-indicator'>" +
        "{name}</div></div>";

    private static Template _labelTemplate;

    static
    {
        _labelTemplate = new Template(LABELTEMPLATE);
        _labelTemplate.compile();
    }
    */

    private ToolButton increaseSize = null;
    private ToolButton decreaseSize = null;
    private PortletSpecification _spec;
    private boolean _autoLayout;

    private HeaderType headerType;
    private LayoutContainer header = null;
    private InterceptedHtml headerLabel = null;
    private boolean isCollapsible;
    private boolean isCollapsed = false;
    private TableData toolData;
    private ToolButton collapseButton;
    private boolean tabStyleHeader = false;

    private LayoutContainer body;

    public PortletContainer1(boolean tabStyle, PortletSpecification spec, HeaderType type, boolean collapsible) {
        this(tabStyle, Style.Orientation.VERTICAL, spec, type, collapsible, false);
    }

    public PortletContainer1(PortletSpecification spec, HeaderType type, boolean collapsible) {
        this(false, Style.Orientation.VERTICAL, spec, type, collapsible, false);
    }

    public PortletContainer1(PortletSpecification spec, HeaderType type, boolean collapsible, boolean collapsed) {
        this(Style.Orientation.VERTICAL, spec, type, collapsible);
    }

    public PortletContainer1(Style.Orientation orientation, PortletSpecification spec, HeaderType type,
            boolean collapsible) {
        this(false, orientation, spec, type, collapsible, false);

    }

    public PortletContainer1(boolean tabStyle, Style.Orientation orientation, PortletSpecification spec, HeaderType type,
            boolean collapsible, boolean collapsed) {
        _spec = spec;
        _autoLayout = true;
        isCollapsible = collapsible;
        headerType = type;
        tabStyleHeader = tabStyle;

        // does the spec override the collapsed setting?
        if (spec != null) {
            Object val = spec.getProperty("showCollapsed");
            if (val != null) {
                if (val.toString().trim().equals("true")) {
                    collapsed = true;
                }
            }
        }

        // does the spec override the header type?
        if (spec != null) {
            Object val = spec.getProperty("headerType");
            if (val != null) {
                try {
                    headerType = HeaderType.valueOf(val.toString());
                } catch (IllegalArgumentException e) {
                    // fine, leave it alone
                }
            }
        }

        isCollapsed = collapsed;
        init(orientation);
    }

    protected HeaderType getHeaderType() {
        return headerType;
    }

    public PortletSpecification getSpecification()
    {
        return _spec;
    }

    public LayoutContainer getHeader() {
        return header;
    }
    
    public void reset()
    {
    }
    
    public void destroy()
    {
    }
    
    private void init(Style.Orientation orientation) {
        setLayout(new RowLayout(orientation));
        body = new LayoutContainer();
        
        if (_spec != null) {
            Serializable s = _spec.getProperty("height");
            if (s != null)
            {
                int height = Integer.parseInt(s.toString());
                body.setHeight(height);
            }
        }


        if (headerType != HeaderType.None) {

            header = new LayoutContainer();

            TableRowLayout layout = new TableRowLayout();
            layout.setCellSpacing(3);
            header.setLayout(layout);

            toolData = new TableData();
            toolData.setVerticalAlign(VerticalAlignment.MIDDLE);
            toolData.setMargin(3);

            LayoutContainer hc = new LayoutContainer();
            hc.setLayout(new RowLayout());

            switch (headerType) {
                case Shaded:
                    hc.setStyleName("portlet-header-shaded");
                    body.addStyleName("portlet-frame");
                    break;
                case Bordered:
                    hc.setStyleName("portlet-header-bordered");
                    body.addStyleName("portlet-frame");
                    break;
                case Transparent:
                    hc.setStyleName("portlet-header-transparent");
                    break;
            }

            hc.add(header, new RowData(1, -1, new Margins(4, 3, 3, 3)));

            LayoutContainer cont = new LayoutContainer();
            if (tabStyleHeader) {
                layout = new TableRowLayout();
                layout.setCellSpacing(0);
                cont.setLayout(layout);
                cont.add(hc, toolData);
            } else {
                cont.add(hc);
            }

            if (orientation == Style.Orientation.VERTICAL) {
                add(cont, new RowData(1, -1, new Margins(0)));
            } else {
                add(cont, new RowData(200, 1, new Margins(0)));
            }

            if (isCollapsible) {
                if (isCollapsed) {
                    collapseButton = new ToolButton("x-tool-down");
                } else {
                    collapseButton = new ToolButton("x-tool-up");
                }

                collapseButton.addSelectionListener(
                    new SelectionListener<IconButtonEvent>() {
                        @Override
                        public void componentSelected(IconButtonEvent evt) {
                            setCollapsed(!isCollapsed);
                        }
                    }
                );

                addHeaderItem(collapseButton);
            }
        }

        body.setVisible(!isCollapsed);

        if (orientation == Style.Orientation.VERTICAL) {
            add(body, new RowData(1, -1));
        } else {
            add(body, new RowData(200, 1));
        }

    }

    private void setCollapsed(boolean collapsed) {
        isCollapsed = collapsed;
        if (isCollapsed) {
            body.el().fadeOut(FxConfig.NONE);
            collapseButton.changeStyle("x-tool-down");
        } else {
            body.el().fadeIn(FxConfig.NONE);
            collapseButton.changeStyle("x-tool-up");
        }
    }

    public void setHeading(String text) {
        setHeading(text, null);
    }

    public void setHeading(String text, String styleName) {
        if (headerType != HeaderType.None) {
            if (headerLabel == null) {
                headerLabel = new InterceptedHtml();
                headerLabel.addStyleName("portlet-header-label");
                if (styleName != null) {
                    headerLabel.addStyleName(styleName);
                }
                
                header.add(headerLabel, toolData);
            }

            /*
            Params params = new Params();
            params.set("name", text);

            String content = _labelTemplate.applyTemplate(params);
            */
            headerLabel.setHtml(text);
            header.layout();
        }
    }

    public void addHeaderItem(Component comp) {
        if (header != null) {
            header.add(comp, toolData);
        }
    }

    protected LayoutContainer getBody() {
        return body;
    }

    /*
    public void setExpandable()
    {
        // not if it's auto layout
        if (_autoLayout)
        {
            return;
        }
        
        increaseSize = new ToolButton("x-tool-down", 
                new SelectionListener<ComponentEvent>() 
        {  
            @Override  
             public void componentSelected(ComponentEvent ce) 
             {  
                int h = 25 + getHeight();
                setHeight(h);
                increaseSize.setEnabled(h < 1000);
                decreaseSize.setEnabled(h > 75);
             }  
        });
        
        getHeader().addTool(increaseSize);  

        addListener(Events.BeforeExpand, 
            new Listener<ComponentEvent>()
            {
                public void handleEvent(ComponentEvent evt) 
                {
                    DeferredCommand.addCommand(
                        new Command()
                        {
                            public void execute()
                            {
                                //portletX.setHeight(250);
                            }
                        }
                    );
                }
            }
        );

        addListener(Events.Collapse, 
            new Listener<ComponentEvent>()
            {
                public void handleEvent(ComponentEvent evt) 
                {
                    DeferredCommand.addCommand(
                        new Command()
                        {
                            public void execute()
                            {
                                increaseSize.setVisible(false);
                                decreaseSize.setVisible(false);
                            }
                        }
                    );
                }
            }
        );

        addListener(Events.Expand, 
            new Listener<ComponentEvent>()
            {
                public void handleEvent(ComponentEvent evt) 
                {
                    DeferredCommand.addCommand(
                        new Command()
                        {
                            public void execute()
                            {
                                increaseSize.setVisible(true);
                                decreaseSize.setVisible(true);
        
                                layout();
                            }
                        }
                    );
                }
            }
        );
        
        decreaseSize = new ToolButton("x-tool-up", 
                new SelectionListener<ComponentEvent>() 
        {  
            @Override  
             public void componentSelected(ComponentEvent ce) 
             {  
                int h = getHeight() - 25;
                setHeight(h);
                increaseSize.setEnabled(h < 1000);
                decreaseSize.setEnabled(h > 75);
             }  
        });
        
        getHeader().addTool(decreaseSize);  
    }
    */
}
