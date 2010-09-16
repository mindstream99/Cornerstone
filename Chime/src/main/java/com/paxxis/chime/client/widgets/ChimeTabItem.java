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

import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.core.Template;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.WidgetComponent;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableRowLayout;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Frame;

/**
 * TabItems are added to a {@link ChimeTabPanel}. TabItems can be closable, disabled
 * and support icons.</p>Code snippet:
 *
 * <pre>
   ChimeTabItem ti = new ChimeTabItem("Tab One");
   ti.setClosable(true);
   ti.setEnabled(false);
   tabPanel.add(ti);
 * </pre>
 *
 * <dl>
 * <dt><b>Events:</b></dt>
 *
 * <dd><b>BeforeClose</b> : TabPanelEvent(tabPanel, item)<br>
 * <div>Fires before an item is closed by the user clicking the close icon.
 * Listeners can set the <code>doit</code> field to <code>false</code> to cancel
 * the action.</div>
 * <ul>
 * <li>tabPanel : this</li>
 * <li>item : the item that was closed.</li>
 * </ul>
 * </dd>
 *
 * <dd><b>Close</b> : TabPanelEvent(tabPanel, item)<br>
 * <div>Fires after an item is closed by the user clicking the close icon.</div>
 * <ul>
 * <li>tabPanel : this</li>
 * <li>item : the item that was closed.</li>
 * </ul>
 * </dd>
 *
 * <dd><b>Select</b> : TabPanelEvent(tabPanel, item)<br>
 * <div>Fires after the item is selected.</div>
 * <ul>
 * <li>tabPanel : this</li>
 * <li>item : the item that was closed.</li>
 * </ul>
 * </dd>
 * <dl>
 * 
 * @author Robert Englander
 */
public class ChimeTabItem extends LayoutContainer {

  public class HeaderItem extends LayoutContainer {

    String text, iconStyle;
    boolean isEmpty ;
    public HeaderItem(boolean empty) {
        isEmpty = empty;
        if (empty) {
            init();
        }
    }

    private void init() {
        LayoutContainer header = new LayoutContainer();
        header.setStyleAttribute("backgroundColor", "transparent");

        TableRowLayout layout = new TableRowLayout();
        layout.setCellSpacing(3);
        header.setLayout(layout);

        TableData toolData = new TableData();
        toolData.setVerticalAlign(VerticalAlignment.MIDDLE);
        toolData.setMargin(3);

        LayoutContainer hc = new LayoutContainer();
        hc.setLayout(new RowLayout());

        //hc.setStyleName("portlet-header-transparent");
        hc.setStyleAttribute("backgroundColor", "transparent");
        hc.add(header, new RowData(1, -1, new Margins(4, 3, 3, 3)));

        LayoutContainer cont = new LayoutContainer();
        cont.add(hc);
        cont.setStyleAttribute("backgroundColor", "transparent");

        add(cont, new RowData(1, -1, new Margins(0)));
        ToolButton collapseButton = new ToolButton("x-tool-down");
        header.add(collapseButton, toolData);
        this.add(hc);
    }

    /**
     * Returns the header's icon style
     *
     * @return the icon style
     */
    public String getIconStyle() {
      return iconStyle;
    }

    /**
     * Returns the header's text.
     *
     * @return the text
     */
    public String getText() {
      return text;
    }

    @Override
    public void onComponentEvent(ComponentEvent ce) {
      super.onComponentEvent(ce);
      if (ce.getEventTypeInt() == Event.ONCLICK) {
        onClick(ce);
      }
    }

    public void setDisabledStyle(String style) {
        this.disabledStyle = style;
    }
    /**
     * Sets the item's icon style. The style name should match a CSS style that
     * specifies a background image using the following format:
     *
     * <pre>
     *
     * <code> .my-icon { background: url(images/icons/my-icon.png) no-repeat
     * center left !important; } </code>
     *
     * </pre>
     *
     * @param iconStyle the icon style
     */
    public void setIconStyle(String iconStyle) {
      if (rendered) {
        el().selectNode(".x-tab-strip-text").removeStyleName(this.iconStyle).addStyleName(iconStyle);
      }
      this.iconStyle = iconStyle;
    }

    /**
     * Sets the header's text.
     *
     * @param text the text
     */
    public void setText(String text) {
      this.text = text;
      if (rendered) {
        el().child(".x-tab-strip-text").dom.setInnerHTML(text);
      }
    }

    protected void onClick(ComponentEvent ce) {
      tabPanel.onItemClick(ChimeTabItem.this, ce);
    }

    protected void onMouseOut(ComponentEvent ce) {
      tabPanel.onItemOver(ChimeTabItem.this, false);
    }

    protected void onMouseOver(BaseEvent be) {
      tabPanel.onItemOver(ChimeTabItem.this, true);
    }

    protected void onRender(Element target, int pos) {
      if (isEmpty) {
        super.onRender(target, pos);
      } else {
          tabPanel.onItemRender(ChimeTabItem.this, target, pos);
      }
    }

  }

  Template template;
  ChimeTabPanel tabPanel;
  HeaderItem header;

  private String textStyle;
  private boolean closable;
  private RequestBuilder autoLoad;
  private boolean isEmpty;
  /**
   * Creates a new tab item.
   */
  public ChimeTabItem(boolean empty) {
    header = new HeaderItem(empty);
    isEmpty = empty;
  }

  public boolean isEmpty() {
      return isEmpty;
  }
  /**
   * Creates a new tab item with the given text.
   *
   * @param text the item's text
   */
  public ChimeTabItem(String text, boolean empty) {
    this(empty);
    setText(text);
  }

  /**
   * Closes the tab item.
   */
  public void close() {
    tabPanel.remove(this);
  }

  @Override
  public void disable() {
    super.disable();
    header.disable();
  }

  @Override
  public void enable() {
    super.enable();
    header.enable();
  }

  /**
   * Returns the item's header component.
   *
   * @return the header component
   */
  public HeaderItem getHeader() {
    return header;
  }

  /**
   * Returns the item's icon style.
   *
   * @return the icon style
   */
  public String getIconStyle() {
    return header.getIconStyle();
  }

  /**
   * Returns the item's tab panel.
   *
   * @return the tab panel
   */
  public ChimeTabPanel getTabPanel() {
    return tabPanel;
  }

  /**
   * Returns the item's text.
   *
   * @return the text
   */
  public String getText() {
    return header.getText();
  }

  /**
   * Returns the item's text style name.
   *
   * @return the style name
   */
  public String getTextStyle() {
    return textStyle;
  }

  /**
   * Returns true if the item can be closed.
   *
   * @return the closable the close state
   */
  public boolean isClosable() {
    return closable;
  }

  /**
   * Sends a remote request and sets the item's content using the returned HTML.
   *
   * @param requestBuilder the request builder
   */
  public void setAutoLoad(RequestBuilder requestBuilder) {
    this.autoLoad = requestBuilder;
  }

  /**
   * Sets whether the tab may be closed (defaults to false).
   *
   * @param closable the closabable state
   */
  public void setClosable(boolean closable) {
    this.closable = closable;
  }

  /**
   * Sets the item's icon style. The style name should match a CSS style that
   * specifies a background image using the following format:
   *
   * <pre>
     .my-icon { background: url(images/icons/my-icon.png) no-repeat center left !important; }
   * </pre>
   *
   * @param iconStyle the icon style
   */
  public void setIconStyle(String iconStyle) {
    header.setIconStyle(iconStyle);
  }

  /**
   * Sets the item's text.
   *
   * @param text the new text
   */
  public void setText(String text) {
    header.setText(text);
  }

  /**
   * Sets the style name to be applied to the item's text element.
   *
   * @param textStyle the style name
   */
  public void setTextStyle(String textStyle) {
    this.textStyle = textStyle;
  }

  /**
   * Sets a url for the content area of the item.
   *
   * @param url the url
   * @return the frame widget
   */
  public Frame setUrl(String url) {
    Frame f = new Frame(url);
    fly(f.getElement()).setStyleAttribute("frameBorder", "0");
    f.setSize("100%", "100%");
    removeAll();
    add(new WidgetComponent(f));
    return f;
  }

  @Override
  public String toString() {
    return el() != null ? el().toString() : super.toString();
  }

  @Override
  protected void onRender(Element parent, int index) {
    super.onRender(parent, index);
    if (autoLoad != null) {
      el().load(autoLoad);
    }
  }

}
