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

import com.extjs.gxt.ui.client.Style.Direction;
import com.extjs.gxt.ui.client.core.CompositeElement;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.core.XDOM;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FxEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.PreviewEvent;
import com.extjs.gxt.ui.client.fx.FxConfig;
import com.extjs.gxt.ui.client.util.BaseEventPreview;
import com.extjs.gxt.ui.client.util.Point;
import com.extjs.gxt.ui.client.util.Rectangle;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.paxxis.chime.client.ChimeDialogManager;
import com.paxxis.chime.client.widgets.InstancePreviewPopup.Location;

/**
 * A panel that can be displayed over other widgets.
 * 
 * <dl>
 * <dt><b>Events:</b></dt>
 * 
 * <dd><b>BeforeShow</b> : ComponentEvent(component)<br>
 * <div>Fires before the popup is displayed. Listeners can cancel the action by
 * calling {@link BaseEvent#setCancelled(boolean)}.</div>
 * <ul>
 * <li>component : this</li>
 * </ul>
 * </dd>
 * 
 * <dd><b>Show</b> : ComponentEvent(component)<br>
 * <div>Fires after a popup is displayed.</div>
 * <ul>
 * <li>component : this</li>
 * </ul>
 * </dd>
 * 
 * <dd><b>BeforeHide</b> : ComponentEvent(component)<br>
 * <div>Fires before the popup is hidden. Listeners can cancel the action by
 * calling {@link BaseEvent#setCancelled(boolean)}.</div>
 * <ul>
 * <li>component : this</li>
 * </ul>
 * </dd>
 * 
 * <dd><b>Hide</b> : ComponentEvent(component)<br>
 * <div>Fires after a popup is hidden.</div>
 * <ul>
 * <li>component : this</li>
 * </ul>
 * </dd>
 * </dl>
 * 
 * <dl>
 * <dt>Inherited Events:</dt>
 * <dd>LayoutContainer AfterLayout</dd>
 * <dd>ScrollContainer Scroll</dd>
 * <dd>Container BeforeAdd</dd>
 * <dd>Container Add</dd>
 * <dd>Container BeforeRemove</dd>
 * <dd>Container Remove</dd>
 * <dd>BoxComponent Move</dd>
 * <dd>BoxComponent Resize</dd>
 * <dd>Component Enable</dd>
 * <dd>Component Disable</dd>
 * <dd>Component BeforeHide</dd>
 * <dd>Component Hide</dd>
 * <dd>Component BeforeShow</dd>
 * <dd>Component Show</dd>
 * <dd>Component Attach</dd>
 * <dd>Component Detach</dd>
 * <dd>Component BeforeRender</dd>
 * <dd>Component Render</dd>
 * <dd>Component BrowserEvent</dd>
 * <dd>Component BeforeStateRestore</dd>
 * <dd>Component StateRestore</dd>
 * <dd>Component BeforeStateSave</dd>
 * <dd>Component SaveState</dd>
 * </dl>
 * 
 * <dl>
 * <dt><b>CSS:</b></dt>
 * <dd>.x-popup (the popup itself)</dd>
 * </dl>
 * 
 * @author Robert Englander
 */
public class ChimePopup extends LayoutContainer {
  private Location slideLocation = Location.None;
  private int yOffset = 15;
  private int xOffset = 10;
  private boolean animate;
  private boolean autoFocus = true;
  private boolean autoHide = true;
  private boolean constrainViewport = true;
  private String defaultAlign = "tl-bl?";

  private Element alignElem;
  private String alignPos;
  private int[] alignOffsets;
  private Point alignPoint;
  private BaseEventPreview preview = new BaseEventPreview() {
    @Override
    protected boolean onAutoHide(PreviewEvent ce) {
      if (ChimePopup.this.onAutoHide(ce.getEvent())) {
        hide();
        return true;
      }
      return false;
    }

  };

  /**
   * Creates a new popup panel.
   */
  public ChimePopup() {
    baseStyle = "x-popup";
    shim = true;
    enableLayout = true;
  }
  
  public void setSlideLocation(Location loc) {
	  slideLocation = loc;
  }

  /**
   * Centers the panel within the viewport.
   */
  public void center() {
    if (rendered) {
      el().center();
    }
  }

  /**
   * Returns the default alignment.
   * 
   * @return the default align
   */
  public String getDefaultAlign() {
    return defaultAlign;
  }

  /**
   * Any elements added to this list will be ignored when auto close is enabled.
   * 
   * @return the list of ignored elements
   */
  public CompositeElement getIgnoreList() {
    return preview.getIgnoreList();
  }

  /**
   * Returns the x offset.
   * 
   * @return the offset
   */
  public int getXOffset() {
    return xOffset;
  }

  /**
   * Returns the y offsets.
   * 
   * @return the offset
   */
  public int getYOffset() {
    return yOffset;
  }

  /**
   * Hides the popup.
   */
  public void hide() {
    if (!fireEvent(Events.BeforeHide, new ComponentEvent(this))) {
      return;
    }
    if (autoHide) {
      preview.remove();
    }
    if (isAnimate()) {
    	FxConfig fxConfig = new FxConfig(new Listener<FxEvent>() {
            public void handleEvent(FxEvent fe) {
              afterHide();
            }
        });
    	switch (slideLocation) {
	    	case None:
	            el().fadeOut(fxConfig);
	    		break;
	    	case Left:
	            el().slideOut(Direction.RIGHT, fxConfig);
	    		break;
	    	case Top:
	            el().slideOut(Direction.DOWN, fxConfig);
	    		break;
	    	case Right:
	            el().slideOut(Direction.LEFT, fxConfig);
	    		break;
	    	case Bottom:
	            el().slideIn(Direction.UP, fxConfig);
	    		break;
    	}
    } else {
      afterHide();
    }
    
    ChimeDialogManager.instance().unregister(this);
  }

  /**
   * Returns true if animations are enabled.
   * 
   * @return the animation state
   */
  public boolean isAnimate() {
    return animate;
  }

  /**
   * Returns true if auto focus is enabled.
   * 
   * @return the auto focus state
   */
  public boolean isAutoFocus() {
    return autoFocus;
  }

  /**
   * Returns true if auto hide is enabled.
   * 
   * @return the auto hide state
   */
  public boolean isAutoHide() {
    return autoHide;
  }

  /**
   * Returns true if contrain to viewport is enabled.
   * 
   * @return the constrain viewport state
   */
  public boolean isConstrainViewport() {
    return constrainViewport;
  }

  /**
   * True to enable animations when showing and hiding (defaults to false).
   * 
   * @param animate true to enable animations
   */
  public void setAnimate(boolean animate) {
    this.animate = animate;
  }

  /**
   * True to move focus to the popup when being opened (defaults to true).
   * 
   * @param autoFocus true for auto focus
   */
  public void setAutoFocus(boolean autoFocus) {
    this.autoFocus = autoFocus;
  }

  /**
   * True to close the popup when the user clicks outside of the menu (default
   * to true).
   * 
   * @param autoHide true for auto hide
   */
  public void setAutoHide(boolean autoHide) {
    this.autoHide = autoHide;
  }

  /**
   * True to ensure popup is dislayed within the browser's viewport.
   * 
   * @param constrainViewport true to constrain
   */
  public void setConstrainViewport(boolean constrainViewport) {
    this.constrainViewport = constrainViewport;
  }

  /**
   * The default {@link El#alignTo} anchor position value for this menu relative
   * to its element of origin (defaults to "tl-bl?").
   * 
   * @param defaultAlign the default alignment
   */
  public void setDefaultAlign(String defaultAlign) {
    this.defaultAlign = defaultAlign;
  }

  /**
   * Sets the xOffset when constrainViewport == true (defaults to 10).
   * 
   * @param xOffset the x offset
   */
  public void setXOffset(int xOffset) {
    this.xOffset = xOffset;
  }

  /**
   * Sets the yOffset when constrainViewport == true (defaults to 15).
   * 
   * @param yOffset the offset
   */
  public void setYOffset(int yOffset) {
    this.yOffset = yOffset;
  }

  /**
   * Displays the popup.
   */
  public void show() {
    if (!fireEvent(Events.BeforeShow, new ComponentEvent(this))) return;
    Point p = new Point((int) Window.getClientWidth() / 2, (int) Window.getClientHeight() / 2);
    showAt(p.x, p.y);
  }

  /**
   * Displays the popup aligned to the bottom left of the widget. For exact
   * control of popup position see {@link #show(Element, String, int[])}.
   * 
   * @param widget the widget to use for alignment
   */
  public void show(Component widget) {
    if (!fireEvent(Events.BeforeShow, new ComponentEvent(this))) {
      return;
    }
    alignElem = widget.getElement();
    onShowPopup();
  }

  /**
   * Displays the popup.
   * 
   * @param elem the element to align to
   * @param pos the position
   */
  public void show(Element elem, String pos) {
    if (!fireEvent(Events.BeforeShow, new ComponentEvent(this))) return;
    alignElem = elem;
    alignPos = pos;
    onShowPopup();
  }

  /**
   * Displays the popup.
   * 
   * @param elem the element to align to
   * @param pos the postion
   * @param offsets the offsets
   */
  public void show(Element elem, String pos, int[] offsets) {
    if (!fireEvent(Events.BeforeShow, new ComponentEvent(this))) {
      return;
    }
    alignElem = elem;
    alignPos = pos;
    alignOffsets = offsets;
    onShowPopup();
  }

  /**
   * Shows the popup at the specified location.
   * 
   * @param x the x coordinate
   * @param y the y coordinate
   */
  public void showAt(int x, int y) {
    if (!fireEvent(Events.BeforeShow, new ComponentEvent(this))) {
      return;
    }
    alignPoint = new Point(x, y);
    onShowPopup();
  }

  protected void afterHide() {
    RootPanel.get().remove(this);
    hidden = true;
    hideShadow();
    el().setVisible(false);
    fireEvent(Events.Hide, new ComponentEvent(this));
  }

  protected void afterShow() {
    el().setVisible(true);

    if (layer != null) {
      layer.sync(true);
    }
    if (isAutoFocus()) {
      focus();
    }

    el().setZIndex(XDOM.getTopZIndex());

    fireEvent(Events.Open, new ComponentEvent(this));
  }

  protected void handleKeyUp(ComponentEvent ce) {
    int code = ce.getKeyCode();
    ce.setComponent(this);
    onKeyPress(ce);
    switch (code) {
      case KeyCodes.KEY_ESCAPE:
        onAutoHide(ce.getEvent());
    }
  }

  /**
   * Subclasses may override to cancel the hide from an auto hide.
   * 
   * @param event the current event
   * @return true to close, false to cancel
   */
  protected boolean onAutoHide(Event event) {
    return true;
  }

  @Override
  protected void onDetach() {
    super.onDetach();
    if (preview != null) {
      preview.remove();
    }
  }

  protected void onKeyPress(BaseEvent be) {

  }

  @Override
  protected void onRender(Element target, int index) {
    super.onRender(target, index);
    el().makePositionable(true);

    preview.getIgnoreList().add(getElement());
    ChimeDialogManager.instance().register(this);
  }

  protected ChimePopup onShowPopup() {
    RootPanel.get().add(this);

    hidden = false;
    Point p = null;

    if (alignElem != null) {
      alignPos = alignPos != null ? alignPos : getDefaultAlign();
      alignOffsets = alignOffsets != null ? alignOffsets : new int[] {0, 2};
      p = el().getAlignToXY(alignElem, alignPos, alignOffsets);
    } else if (alignPoint != null) {
      p = alignPoint;
    }

    el().setLeftTop(p.x, p.y);

    alignElem = null;
    alignPos = null;
    alignOffsets = null;
    alignPoint = null;

    el().makePositionable(true).setVisibility(false);

    if (constrainViewport) {
      int clientHeight = Window.getClientHeight() + XDOM.getBodyScrollTop();
      int clientWidth = Window.getClientWidth() + XDOM.getBodyScrollLeft();

      Rectangle r = el().getBounds();

      int x = r.x;
      int y = r.y;

      if (y + r.height > clientHeight) {
        y = clientHeight - r.height - getYOffset();
        el().setTop(y);
      }
      if (x + r.width > clientWidth) {
        x = clientWidth - r.width - getXOffset();
        el().setLeft(x);
      }
    }

    el().setVisibility(true);

    if (autoHide) {
      preview.add();
    }

    if (animate) {
    	FxConfig fxConfig = new FxConfig(new Listener<FxEvent>() {
            public void handleEvent(FxEvent fe) {
              afterShow();
            }
        });
    	switch (slideLocation) {
	    	case None:
	            el().fadeIn(fxConfig);
	    		break;
	    	case Left:
	            el().slideIn(Direction.LEFT, fxConfig);
	    		break;
	    	case Top:
	            el().slideIn(Direction.UP, fxConfig);
	    		break;
	    	case Right:
	            el().slideIn(Direction.RIGHT, fxConfig);
	    		break;
	    	case Bottom:
	            el().slideIn(Direction.DOWN, fxConfig);
	    		break;
    	}
    } else {
      afterShow();
    }

    return this;
  }
}
