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
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableRowLayout;
import com.paxxis.chime.client.Utils;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.portal.PortletSpecification;
import com.paxxis.chime.client.widgets.ChimeLayoutContainer;
import com.paxxis.chime.client.widgets.ChimeTabItem;
import com.paxxis.chime.client.widgets.ChimeTabPanel;
import com.paxxis.chime.client.widgets.InterceptedHtml;

/**
 *
 * @author Robert Englander
 */
public class PortletContainer extends ChimeLayoutContainer
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

    private LayoutContainer headerContainer;
    private HeaderType headerType;
    private LayoutContainer header = null;
    private InterceptedHtml headerLabel = null;
    private boolean isCollapsible;
    private boolean isCollapsed = false;
    private TableData leftSideTableDataPadded;
    private TableData leftSideTableDataUnpadded;
    private ToolButton collapseButton;
    private boolean tabStyleHeader = false; //changes
    private ChimeTabItem tabItem;
    private ChimeTabPanel tabPanel;
    private Style.Orientation orientation;
    
    private ChimeLayoutContainer body;
    private boolean useRightSideToolbar = false;
    private LayoutContainer rightSideToolbar = null;
    
    // a hack to overcome an apparent bug in the fade in/fade out
    private boolean avoidFade = false;

    //public PortletContainer(boolean tabStyle, PortletSpecification spec, HeaderType type, boolean collapsible) {
    //    this(tabStyle, Style.Orientation.VERTICAL, spec, type, collapsible, false);
    //}

    public PortletContainer(PortletSpecification spec, HeaderType type, boolean collapsible) {
        this(false, Style.Orientation.VERTICAL, spec, type, collapsible, false);
    }

    public PortletContainer(PortletSpecification spec, HeaderType type, boolean collapsible, boolean collapsed) {
        this(Style.Orientation.VERTICAL, spec, type, collapsible);
    }

    public PortletContainer(Style.Orientation orientation, PortletSpecification spec, HeaderType type,
            boolean collapsible) {
        this(false, orientation, spec, type, collapsible, false);

    }

    public PortletContainer(boolean tabStyle, Style.Orientation orientation, PortletSpecification spec, HeaderType type,
            boolean collapsible, boolean collapsed) {
        _spec = spec;
        _autoLayout = true;
        isCollapsible = collapsible;
        headerType = type;
        tabStyleHeader = false; //headerType == HeaderType.Shaded;

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
                    tabStyleHeader = false; //headerType == HeaderType.Shaded;
                } catch (IllegalArgumentException e) {
                    // fine, leave it alone
                }
            }
        }

        isCollapsed = collapsed;
        this.orientation = orientation;
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
    
    protected void init() {
        setBorders(false);
        setLayout(new RowLayout(orientation));

        if (tabStyleHeader) {
            tabPanel = new ChimeTabPanel();
            tabPanel.setPlain(true);
            tabPanel.setAutoHeight(true);
            tabItem = new ChimeTabItem(false);
            tabItem.setLayout(new RowLayout(orientation));
            tabPanel.add(tabItem);
            //tabPanel.add(new ChimeTabItem("", true));
            add(tabPanel, new RowData(1, -1));
        }

        body = new ChimeLayoutContainer() {
        	protected void init() {
        	}
        };
        
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

            leftSideTableDataPadded = new TableData();
            leftSideTableDataPadded.setVerticalAlign(VerticalAlignment.MIDDLE);
            leftSideTableDataPadded.setMargin(3);
            leftSideTableDataPadded.setPadding(4);
            
            leftSideTableDataUnpadded = new TableData();
            leftSideTableDataUnpadded.setVerticalAlign(VerticalAlignment.MIDDLE);
            leftSideTableDataUnpadded.setMargin(3);

            headerContainer = new LayoutContainer();
            //headerContainer.setLayout(new FlowLayout());
            headerContainer.setLayout(new ColumnLayout());

            switch (headerType) {
                case Shaded:
                    headerContainer.setStyleName("portlet-header-shaded");
                    body.addStyleName("portlet-frame");
                    break;
                case Bordered:
                    headerContainer.setStyleName("portlet-header-bordered");
                    body.addStyleName("portlet-frame");
                    break;
                case Transparent:
                    headerContainer.setStyleName("portlet-header-transparent");
                    break;
            }

            //headerContainer.add(header, new FlowData(4, 3, 3, 3));
            LayoutContainer hc = new LayoutContainer();
            hc.setLayout(new RowLayout());
            hc.add(header, new RowData(1, -1, new Margins(4, 3, 3, 3)));
            headerContainer.add(hc, new ColumnData(1.0));

            LayoutContainer cont = new LayoutContainer();
            cont.setLayout(new RowLayout());
            cont.add(headerContainer, new RowData(1, -1));

            if (orientation == Style.Orientation.VERTICAL) {
                if (tabStyleHeader) {
                    tabItem.add(cont, new RowData(1, -1, new Margins(0)));
                } else {
                    add(cont, new RowData(1, -1, new Margins(0)));
                }
            } else {
                if (tabStyleHeader) {
                    tabItem.add(cont, new RowData(200, 1, new Margins(0)));
                } else {
                    add(cont, new RowData(200, 1, new Margins(0)));
                }
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

        if (orientation == Style.Orientation.VERTICAL) {
            if (tabStyleHeader) {
                tabItem.add(body, new RowData(1, -1));
            } else {
                add(body, new RowData(1, -1));
            }
        } else {
            if (tabStyleHeader) {
                tabItem.add(body, new RowData(200, 1));
            } else {
                add(body, new RowData(200, 1));
            }
        }

        avoidFade = isCollapsed;
        body.setVisible(!isCollapsed);
    }

    private void setCollapsed(boolean collapsed) {
    	
        isCollapsed = collapsed;
        
        if (isCollapsed) {
            body.el().fadeOut(FxConfig.NONE);
            collapseButton.changeStyle("x-tool-down");
        } else {
        	if (avoidFade) {
        		avoidFade = false;
                body.setVisible(true);
        	} else {
            	body.el().fadeIn(FxConfig.NONE);
        	}

        	collapseButton.changeStyle("x-tool-up");
            body.layout(true);
        }
    }

    public void setHeading(String text) {
        setHeading(InstanceId.UNKNOWN, text, null);
    }

    public void setHeadingById(InstanceId id, String text) {
        setHeading(id, text, null);
    }

    public void setHeading(String text, String styleName) {
        setHeading(InstanceId.UNKNOWN, text, styleName);
    }

    public void setHeading(final InstanceId id, final String text, final String styleName) {
    	
    	Runnable r = new Runnable() {
    		public void run() {
    	        if (headerType != HeaderType.None) {
    	            if (headerLabel == null) {
    	                headerLabel = new InterceptedHtml();
    	                headerLabel.addStyleName("portlet-header-label");
    	                if (styleName != null) {
    	                    headerLabel.addStyleName(styleName);
    	                }
    	                
    	                header.add(headerLabel, leftSideTableDataPadded);
    	            }

    	            /*
    	            Params params = new Params();
    	            params.set("name", text);

    	            String content = _labelTemplate.applyTemplate(params);
    	            */
    	            if (tabStyleHeader) {
    	                if (styleName != null) {
    	                    tabItem.setTextStyle(styleName);
    	                }
    	                tabItem.setText(text);
    	            } else {
    	                if (id.equals(InstanceId.UNKNOWN)) {
    	                    headerLabel.setHtml(text);
    	                } else {
    	                    String txt = Utils.toHoverUrl(id, text);
    	                    headerLabel.setHtml(txt);
    	                }

    	                header.layout();
    	            }
    	        }
    		}
    	};
    	
    	if (isRendered()) {
    		r.run();
    	} else {
    		addPostRenderRunnable(r);
    	}
    }

    public void addToolbarItem(Component comp, int width) {
        if (rightSideToolbar == null) {
            LayoutContainer lc = new LayoutContainer();
            lc.add(comp, new FlowData(new Margins(0, 2, 0, 0)));
            headerContainer.add(lc, new ColumnData(width));
        }
    }

    public void addHeaderItem(final Component comp) {
    	Runnable r = new Runnable() {
    		public void run() {
    	        if (header != null) {
    	            header.add(comp, leftSideTableDataUnpadded);
    	        }
    		}
    	};
    	
    	if (isRendered()) {
    		r.run();
    	} else {
    		addPostRenderRunnable(r);
    	}
    }

    protected ChimeLayoutContainer getBody() {
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
