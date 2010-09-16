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

import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.core.XDOM;
import com.extjs.gxt.ui.client.util.Size;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Container;
import com.extjs.gxt.ui.client.widget.Layout;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.LayoutData;

/**
 * This layout positions and sizes the container's children in columns
 * horizontally. Each component may specify its width in pixels or as percentage
 * of the parent remaining width.
 * 
 * <p />
 * Child Widgets are:
 * <ul>
 * <li><b>Sized</b> : Yes - the width of a widget is adjusted by the ColumnData
 * hint</li>
 * <li><b>Positioned</b> : Yes - widgets are placed in columns</li>
 * </ul>
 * 
 * <p />
 * Code snippet:
 * 
 * <code><pre>
   LayoutContainer container = new LayoutContainer();
   container.setLayout(new ColumnLayout());
   container.add(new Button("100px"), new ColumnData(100));
   container.add(new Button("30%"), new ColumnData(.3));
   container.add(new Button("50px"), new ColumnData(50));
 * </pre></code>
 * 
 * @see ColumnData
 * @author Robert Englander
 */
public class ChimeColumnLayout extends Layout {

  protected El innerCt;
  protected boolean adjustForScroll = false;

  /**
   * Creates a new column layout.
   */
  public ChimeColumnLayout() {
    setExtraStyle("x-column");
    monitorResize = true;
  }

  /**
   * Returns true if adjust for scroll is enabled.
   * 
   * @return the adjust for scroll state
   */
  public boolean isAdjustForScroll() {
    return adjustForScroll;
  }

  /**
   * True to adjust the container width calculations to account for the scroll
   * bar (defaults to false).
   * 
   * @param adjustForScroll the adjust for scroll state
   */
  public void setAdjustForScroll(boolean adjustForScroll) {
    this.adjustForScroll = adjustForScroll;
  }

  @Override
  protected void onLayout(Container<?> container, El target) {
    if (innerCt == null) {
      container.addStyleName("x-column-layout-ct");
      innerCt = target.createChild("<div class='x-column-inner'></div>");
      innerCt.createChild("<div class='x-clear'></div>");
    }

    renderAll(container, innerCt);

    Size size = target.getStyleSize();

    int w = size.width - (adjustForScroll ? Math.max(19, XDOM.getScrollBarWidth()) : 0);
    int pw = w;

    int count = container.getItemCount();

    // some columns can be percentages while others are fixed
    // so we need to make 2 passes
    for (int i = 0; i < count; i++) {
      Component c = container.getItem(i);

      ColumnData layoutData = null;
      LayoutData d = getLayoutData(c);
      if (d != null && d instanceof ColumnData) {
        layoutData = (ColumnData) d;
      } else {
        layoutData = new ColumnData();
      }

      if (layoutData.getWidth() > 1) {
        pw -= layoutData.getWidth();
      }

      pw -= getSideMargins(c);
    }

    pw = pw < 0 ? 0 : pw;

    for (int i = 0; i < count; i++) {
      Component c = container.getItem(i);

      ColumnData layoutData = null;
      LayoutData d = getLayoutData(c);
      if (d != null && d instanceof ColumnData) {
        layoutData = (ColumnData) d;
      } else {
        layoutData = new ColumnData();
      }

      int width = -1;
      if (layoutData.getWidth() > 0 && layoutData.getWidth() <= 1) {
        width = (int) (layoutData.getWidth() * pw);
      } else {
        width = (int) layoutData.getWidth();
      }
      setSize(c, width, -1);
    }
  }
}
