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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.core.XDOM;
import com.extjs.gxt.ui.client.event.DragEvent;
import com.extjs.gxt.ui.client.event.DragListener;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.fx.Draggable;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Point;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ComponentHelper;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.paxxis.chime.client.common.portal.PortalTemplate;
import com.paxxis.chime.client.widgets.ChimeLayoutContainer;

/**
 * 
 * @author Robert Englander
 *
 */
class PortalColumn extends LayoutContainer {

    public void applyMask() {
        El mask = new El("<div class='chime-el-mask'></div>");

        El me = el();
        me.makePositionable();
        //me.addStyleName("x-masked");

        if (GXT.isIE && !GXT.isStrict) {
            mask.setWidth(me.getWidth());
            mask.setHeight(me.getHeight());
        }

        mask.setDisplayed(true);
        me.appendChild(mask.dom);
    }
}

/**
 *
 * @author Robert Englander
 */
public class PortalContainer extends ChimeLayoutContainer {

  public interface PortletChangeListener {
      public void onPortletMoved(PortletContainer portlet, int oldRow, int oldCol, int newRow, int newCol);
      public void onPortletDeleted(PortletContainer portlet, int row, int col);
  }

  private LayoutContainer _centerContainer;
  
  private List<PortalColumn> columns = new ArrayList<PortalColumn>();
  private DragListener listener;
  private List<Integer> startColumns;
  private int numColumns;
  private int startCol, startRow;
  private int insertCol = -1, insertRow = -1;
  private PortletContainer active;
  private El dummy;
  private ColumnLayout cl;
  private List<Point> _pinnedLocations = new ArrayList<Point>();
  private PortalTemplate _template;
  private HashMap<Long, PortletContainer> _portletMap = new HashMap<Long, PortletContainer>();
  private boolean useFlow;
  private String columnPadding;
  private String columnPadding2;

  /**
   	* Creates a new portal container.
   	* 
   	* @param numColumns the number of columns
   	*/
  	public PortalContainer(int numColumns, boolean flow) {
  		this(numColumns, flow, "10px 0px 0px 5px", "10px 0px 0px 10px");
  	}
  
    public PortalContainer(int numColumns, boolean flow, String padding, String padding2) {
		useFlow = flow;
		this.numColumns = numColumns;
		columnPadding = padding;
		columnPadding2 = padding2;
    }
  
    public boolean hasPortlets() {
    	return !_portletMap.isEmpty();
    }
    
  @Override
  protected void init() {
    BorderLayoutData data = null;
    if (!useFlow) {
        setLayout(new BorderLayout());
        data = new BorderLayoutData(LayoutRegion.CENTER);
        data.setMargins(new Margins(2, 2, 2, 2));
        data.setSplit(true);
    } else {
    	setLayout(new RowLayout());
    }

    setBorders(false);
    
    _centerContainer = new LayoutContainer();
    _centerContainer.setBorders(false);
    _centerContainer.setStyleAttribute("backgroundColor", "white");  
    
    cl = new ColumnLayout();
    cl.setAdjustForScroll(!useFlow);
    _centerContainer.setLayout(cl);
    _centerContainer.setStyleAttribute("overflow", "auto");
    baseStyle = "x-portal";
    enableLayout = true;

    if (useFlow) {
        add(_centerContainer, new RowData(1, -1));
    } else {
        add(_centerContainer, data);
    }

    String columnPad = columnPadding;
    for (int i = 0; i < numColumns; i++) {
      PortalColumn l = new PortalColumn();
      l.addStyleName("x-portal x-portal-column");
      l.setStyleAttribute("minHeight", "20px");
      l.setStyleAttribute("padding", columnPad);
      l.setLayout(new RowLayout());
      l.setLayoutOnChange(true);
      _centerContainer.add(l);
      columns.add(l);
      columnPad = columnPadding2;
    }

    listener = new DragListener() {

      @Override
      public void dragCancel(DragEvent de) {
        onDragCancel(de);
      }

      @Override
      public void dragEnd(DragEvent de) {
        onDragEnd(de);
      }

      @Override
      public void dragMove(DragEvent de) {
        onDragMove(de);
      }

      @Override
      public void dragStart(DragEvent de) {
        onDragStart(de);
      }

    };
  }

    public InstanceHeaderPortlet getInstanceHeaderPortlet() {
        Set<Entry<Long, PortletContainer>> set = _portletMap.entrySet();
        for (Entry<Long, PortletContainer> entry : set) {
            PortletContainer cont = entry.getValue();
            if (cont instanceof InstanceHeaderPortlet) {
                return (InstanceHeaderPortlet)cont;
            }
        }

        return null;
    }

    public PagePortlet getPagePortlet() {
        Set<Entry<Long, PortletContainer>> set = _portletMap.entrySet();
        for (Entry<Long, PortletContainer> entry : set) {
            PortletContainer cont = entry.getValue();
            if (cont instanceof PagePortlet) {
                return (PagePortlet)cont;
            }
        }

        return null;
    }

    public void setLeft(PortletContainer portlet)
    {
        _portletMap.put(portlet.getSpecification().getId(), portlet);

        BorderLayoutData data = new BorderLayoutData(LayoutRegion.WEST, 400, 150, 600);
        data.setMargins(new Margins(2, 2, 2, 2));
        data.setCollapsible(false);
        data.setSplit(true);
        add(portlet, data);
    }
  
    public void setRight(PortletContainer portlet)
    {
        _portletMap.put(portlet.getSpecification().getId(), portlet);

        BorderLayoutData data = new BorderLayoutData(LayoutRegion.EAST, 400, 150, 600);
        data.setMargins(new Margins(2, 2, 2, 2));
        data.setCollapsible(false);
        data.setSplit(true);
        add(portlet, data);
    }

    public void setTop(PortletContainer portlet)
    {
        _portletMap.put(portlet.getSpecification().getId(), portlet);

        BorderLayoutData data = new BorderLayoutData(LayoutRegion.NORTH, 300, 150, 600);
        data.setMargins(new Margins(2, 2, 2, 2));
        data.setCollapsible(false);
        data.setSplit(true);
        add(portlet, data);
    }

    public void setBottom(PortletContainer portlet)
    {
        _portletMap.put(portlet.getSpecification().getId(), portlet);

        BorderLayoutData data = new BorderLayoutData(LayoutRegion.SOUTH, 300, 150, 600);
        data.setMargins(new Margins(2, 2, 2, 2));
        data.setCollapsible(false);
        data.setSplit(true);
        add(portlet, data);
    }

  public void setTemplate(PortalTemplate template)
  {
    _template = template;
  }
  
  public PortalTemplate getTemplate()
  {
      return _template;
  }
  
  public PortletContainer getPortletBySpecId(long id)
  {
      PortletContainer cont = _portletMap.get(id);
      return cont;
  }
  
  public void destroy()
  {
    for (LayoutContainer column : columns)
    {
        List<Component> components = column.getItems();
        for (Component comp : components)
        {
            if (comp instanceof PortletContainer)
            {
                ((PortletContainer)comp).destroy();
            }
        }
    }
  }

    public void addLayoutProxy(final LayoutProxyPortlet portlet, final int column) {
    	
    	Runnable r = new Runnable() {
    		public void run() {
    	        portlet.addPostRenderRunnable(
    	        	new Runnable() {
    	        		public void run() {
    	        	        Draggable d = new Draggable(portlet, portlet.getHeader());
    	        	        d.setUseProxy(true);
    	        	        d.addDragListener(listener);
    	        	        d.setMoveAfterProxyDrag(false);
    	        	        d.setSizeProxyToSource(true);
    	        	        portlet.getHeader().setStyleAttribute("cursor", "pointer");
    	        		}
    	        	}
    	        );
    	        add(portlet, column,false);
    		}
    	};
    	
    	if (isRendered()) {
    		r.run();
    	} else {
    		addPostRenderRunnable(r);
    	}
    }

    public void add(final PortletContainer portlet, final int column) {
    	Runnable r = new Runnable() {
    		public void run() {
    	        add(portlet, column, true);
    		}
    	};
    	
    	if (isRendered()) {
    		r.run();
    	} else {
    		addPostRenderRunnable(r);
    	}
    }

    private void add(PortletContainer portlet, int column, boolean isPinned)
    {
        _portletMap.put(portlet.getSpecification().getId(), portlet);

        if (isPinned) {
            int row = columns.get(column).getItemCount();
            Point pt = new Point(row, column);
            _pinnedLocations.add(pt);
        }

        columns.get(column).add(portlet, new RowData(1, -1, new Margins(5, 0, 0, 0)));
    }

    public void maskPortlets() {
        DeferredCommand.addCommand(
            new Command() {
                public void execute() {
                    for (PortalColumn col : columns) {
                        col.applyMask();
                    }
                }
            }
        );
    }

  /**
   * Removes a portlet from the portal.
   * 
   * @param portlet the porlet to remove
   * @param column the column
   */
  public void remove(PortletContainer portlet, int column) {
    columns.get(column).remove(portlet);
    _portletMap.remove(portlet.getSpecification().getId());
  }

  /**
   * True to adjust the layout for a vertical scroll bar (defaults to true).
   * 
   * @param adjust true to adjust
   */
  public void setAjustForScroll(boolean adjust) {
    cl.setAdjustForScroll(adjust);
  }

  /**
   * Sets the column's width.
   * 
   * @param colIndex the column index
   * @param width the column width
   */
  public void setColumnWidth(final int colIndex, final double width) {
	  Runnable r = new Runnable() {
		  public void run() {
			    ComponentHelper.setLayoutData(columns.get(colIndex), new ColumnData(width));
		  }
	  };
	  
	  if (isRendered()) {
		  r.run();
	  } else {
		  addPostRenderRunnable(r);
	  }
  }

  @Override
  protected void onAttach() {
    super.onAttach();
    DeferredCommand.addCommand(new Command() {
      public void execute() {
        el().setVisible(false);
        el().setVisible(true);
      }
    });
  }

  protected void onDragEnd(DragEvent de) {
    dummy.removeFromParent();

    if (insertCol != -1 && insertRow != -1) {
      if (startCol == insertCol && insertRow > startRow) {
        insertRow--;
      }

      active.setVisible(true);
      
      try
      {
        active.removeFromParent();
      }
      catch (Throwable t)
      {}
      
      active.reset();
      
      columns.get(insertCol).insert(active, insertRow, new RowData(1, -1));
      active.addStyleName("x-repaint");

      fireEvent(Events.Drop,
          new PortalContainerEvent(this, active, startCol, startRow, insertCol, insertRow));
    }

    active.setVisible(true);
    active = null;
    insertCol = -1;
    insertRow = -1;
  }

  protected void onDragMove(DragEvent de) {
    active.setVisible(false);
    
    int col = getColumn(de.getClientX());
    int row = getRowPosition(col, de.getClientY());
    
    if (col != insertCol || row != insertRow) {
      if (!pinned(row, col))
      {
          PortalContainerEvent pe = new PortalContainerEvent(this, active, startCol, startRow, col, row);
          if (fireEvent(Events.ValidateDrop, pe)) {
            addInsert(col, row);
          } else {
            insertCol = startCol;
            insertRow = startRow;
          }
      }
    }
  }

  private boolean pinned(int row, int col)
  {
    boolean pinned = false;
    
    for (Point pt : _pinnedLocations)
    {
        if (pt.x == row && pt.y == col)
        {
            pinned = true;
            break;
        }
    }
    
    return pinned;
  }
  
  @Override
  protected void onRender(Element target, int index) {
    setElement(DOM.createDiv(), target, index);
    super.onRender(target, index);
  }

  private void addInsert(int col, int row) {
    insertCol = col;
    insertRow = row;
    dummy.removeFromParent();
    LayoutContainer lc = columns.get(insertCol);
    lc.el().insertChild(dummy.dom, row);
  }

  private int getColumn(int x) {
    for (int i = startColumns.size() - 1; i >= 0; i--) {
      if (x > startColumns.get(i)) {
        return i;
      }
    }
    return 0;
  }

  private int getRow(int col, int y) {
    LayoutContainer con = columns.get(col);
    int count = con.getItemCount();

    for (int i = 0; i < count; i++) {
      Component c = con.getItem(i);
      int b = c.getAbsoluteTop();
      int t = b + c.getOffsetHeight();

      if (y < t) {
        return i;
      }
    }

    return 0;
  }

  private int getRowPosition(int col, int y) {
    LayoutContainer con = columns.get(col);
    List<Component> list = new ArrayList<Component>(con.getItems());
    list.remove(dummy);
    int count = list.size();

    if (count == 0) {
      return 0;
    }
    for (int i = 0; i < count; i++) {
      Component c = list.get(i);
      int b = c.getAbsoluteTop();
      int t = b + c.getOffsetHeight();
      int m = b + (c.getOffsetHeight() / 2);
      if (y < t) {
        if (y < m) {
          return i;
        } else {
          return i + 1;
        }
      }
    }
    return list.size();
  }

  private void onDragCancel(DragEvent event) {
    active.setVisible(true);
    active = null;
    insertCol = -1;
    insertRow = -1;
    dummy.removeFromParent();
  }

  private void onDragStart(DragEvent de) {
    active = (PortletContainer) de.getComponent();
    
    if (dummy == null) {
      dummy = new El(XDOM.create("<div class='x-portal-insert' style='margin-bottom: 10px'><div></div></div>"));
      dummy.setStyleName("x-portal-insert");
    }

    dummy.setStyleAttribute("padding", active.el().getStyleAttribute("padding"));

    int h = active.el().getHeight() - active.el().getFrameWidth("tb");
    dummy.firstChild().setHeight(h);

    startColumns = new ArrayList<Integer>();
    for (int i = 0; i < numColumns; i++) {
      LayoutContainer con = columns.get(i);
      int x = con.getAbsoluteLeft();
      startColumns.add(x);
    }
    startCol = getColumn(de.getClientX());
    startRow = getRow(startCol, de.getClientY());
    
    
  }
}
