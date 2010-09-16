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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Size;
import com.extjs.gxt.ui.client.widget.BoxComponent;
import com.extjs.gxt.ui.client.widget.ColorPalette;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ComponentHelper;
import com.extjs.gxt.ui.client.widget.WidgetComponent;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.menu.ColorMenu;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RichTextArea.BasicFormatter;
import com.google.gwt.user.client.ui.RichTextArea.ExtendedFormatter;
import com.google.gwt.user.client.ui.RichTextArea.FontSize;
import com.google.gwt.user.client.ui.RichTextArea.Justification;
import com.google.gwt.user.client.ui.impl.RichTextAreaImpl;

/**
 * 
 * @author Robert Englander
 *
 */
@SuppressWarnings("deprecation")
public class ChimeRichTextEditor extends Field<String> {

	public interface RichTextChangeListener {
		public void onChange();
	}
	
    public static EditToolbarImages IMAGES = (EditToolbarImages) GWT.create(EditToolbarImages.class);

    public class HtmlEditorImages extends FieldImages {
	    private AbstractImagePrototype source = IMAGES.getSource();
	    private AbstractImagePrototype bold = IMAGES.getBold();
	    private AbstractImagePrototype underline = IMAGES.getUnderline();
	    private AbstractImagePrototype italic = IMAGES.getItalic();
	    private AbstractImagePrototype fontColor = IMAGES.getFontColor();
	    private AbstractImagePrototype fontDecrease = IMAGES.getDecreaseSize();
	    private AbstractImagePrototype fontIncrease = IMAGES.getIncreaseSize();
	    private AbstractImagePrototype fontHighlight = IMAGES.getBackgroundColor();
	    private AbstractImagePrototype justifyCenter = IMAGES.getAlignCenter();
	    private AbstractImagePrototype justifyLeft = IMAGES.getAlignLeft();
	    private AbstractImagePrototype justifyRight = IMAGES.getAlignRight();
	    private AbstractImagePrototype ul = IMAGES.getBulletList();
	    private AbstractImagePrototype ol = IMAGES.getNumberList();
	    private AbstractImagePrototype link = IMAGES.getLink();
	    private AbstractImagePrototype linkBreak = IMAGES.getLinkBreak();

    public AbstractImagePrototype getBold() {
      return bold;
    }

    public AbstractImagePrototype getFontColor() {
      return fontColor;
    }

    public AbstractImagePrototype getFontDecrease() {
      return fontDecrease;
    }

    public AbstractImagePrototype getFontHighlight() {
      return fontHighlight;
    }

    public AbstractImagePrototype getFontIncrease() {
      return fontIncrease;
    }

    public AbstractImagePrototype getItalic() {
      return italic;
    }

    public AbstractImagePrototype getJustifyCenter() {
      return justifyCenter;
    }

    public AbstractImagePrototype getJustifyLeft() {
      return justifyLeft;
    }

    public AbstractImagePrototype getJustifyRight() {
      return justifyRight;
    }

    public AbstractImagePrototype getLink() {
        return link;
      }

    public AbstractImagePrototype getLinkBreak() {
        return linkBreak;
      }

    public AbstractImagePrototype getOl() {
      return ol;
    }

    public AbstractImagePrototype getUl() {
      return ul;
    }

    public AbstractImagePrototype getUnderline() {
      return underline;
    }

    public AbstractImagePrototype getSource() {
      return source;
    }

    public void setBold(AbstractImagePrototype bold) {
      this.bold = bold;
    }

    public void setFontColor(AbstractImagePrototype fontColor) {
      this.fontColor = fontColor;
    }

    public void setFontDecrease(AbstractImagePrototype fontDecrease) {
      this.fontDecrease = fontDecrease;
    }

    public void setFontHighlight(AbstractImagePrototype fontHighlight) {
      this.fontHighlight = fontHighlight;
    }

    public void setFontIncrease(AbstractImagePrototype fontIncrease) {
      this.fontIncrease = fontIncrease;
    }

    public void setItalic(AbstractImagePrototype italic) {
      this.italic = italic;
    }

    public void setJustifyCenter(AbstractImagePrototype justifyCenter) {
      this.justifyCenter = justifyCenter;
    }

    public void setJustifyLeft(AbstractImagePrototype justifyLeft) {
      this.justifyLeft = justifyLeft;
    }

    public void setJustifyRight(AbstractImagePrototype justifyRight) {
      this.justifyRight = justifyRight;
    }

    public void setLink(AbstractImagePrototype link) {
      this.link = link;
    }

    public void setUnderline(AbstractImagePrototype underline) {
      this.underline = underline;
    }

    public void setOl(AbstractImagePrototype ol) {
      this.ol = ol;
    }

    public void setUl(AbstractImagePrototype ul) {
      this.ul = ul;
    }

    public void setSource(AbstractImagePrototype source) {
      this.source = source;
    }
  }

  protected class EventHandler implements ClickHandler, KeyUpHandler, FocusHandler, BlurHandler {

    public void onClick(ClickEvent event) {
      updateStatus();
      syncValue();
    }

    public void onKeyUp(KeyUpEvent event) {
      updateStatus();
      syncValue();
    }

    public void onFocus(FocusEvent event) {

    }

    public void onBlur(BlurEvent event) {

    }
  }

  protected class rte extends BoxComponent {
    protected void onRender(Element target, int index) {
      Element e = impl.getElement();
      e.setPropertyInt("frameBorder", 0);
      setElement(e, target, index);

      addDomHandler(handler, ClickEvent.getType());
      addDomHandler(handler, FocusEvent.getType());
      addDomHandler(handler, BlurEvent.getType());
      addDomHandler(handler, KeyUpEvent.getType());
    }

    @Override
    protected void onAttach() {
      super.onAttach();
      impl.initElement();
    }

    @Override
    protected void onDetach() {
      super.onDetach();
      impl.uninitElement();
    }
  }

  public class HtmlEditorMessages extends FieldMessages {
    private String backColorTipText = GXT.MESSAGES.htmlEditor_backColorTipText();
    private String backColorTipTitle = GXT.MESSAGES.htmlEditor_backColorTipTitle();
    private String boldTipText = GXT.MESSAGES.htmlEditor_boldTipText();
    private String boldTipTitle = GXT.MESSAGES.htmlEditor_boldTipTitle();
    private String createLinkText = GXT.MESSAGES.htmlEditor_createLinkText();
    private String decreaseFontSizeTipText = GXT.MESSAGES.htmlEditor_decreaseFontSizeTipText();
    private String decreaseFontSizeTipTitle = GXT.MESSAGES.htmlEditor_decreaseFontSizeTipTitle();
    private String foreColorTipText = GXT.MESSAGES.htmlEditor_foreColorTipText();
    private String foreColorTipTitle = GXT.MESSAGES.htmlEditor_foreColorTipTitle();
    private String increaseFontSizeTipText = GXT.MESSAGES.htmlEditor_increaseFontSizeTipText();
    private String increaseFontSizeTipTitle = GXT.MESSAGES.htmlEditor_increaseFontSizeTipTitle();
    private String italicTipText = GXT.MESSAGES.htmlEditor_italicTipText();
    private String italicTipTitle = GXT.MESSAGES.htmlEditor_italicTipTitle();
    private String justifyCenterTipText = GXT.MESSAGES.htmlEditor_justifyCenterTipText();
    private String justifyCenterTipTitle = GXT.MESSAGES.htmlEditor_justifyCenterTipTitle();
    private String justifyLeftTipText = GXT.MESSAGES.htmlEditor_justifyLeftTipText();
    private String justifyLeftTipTitle = GXT.MESSAGES.htmlEditor_justifyLeftTipTitle();
    private String justifyRightTipText = GXT.MESSAGES.htmlEditor_justifyRightTipText();
    private String justifyRightTipTitle = GXT.MESSAGES.htmlEditor_justifyRightTipTitle();
    private String linkTipText = GXT.MESSAGES.htmlEditor_linkTipText();
    private String linkTipTitle = GXT.MESSAGES.htmlEditor_linkTipTitle();
    private String olTipText = GXT.MESSAGES.htmlEditor_olTipText();
    private String olTipTitle = GXT.MESSAGES.htmlEditor_olTipTitle();
    private String ulTipText = GXT.MESSAGES.htmlEditor_ulTipText();
    private String ulTipTitle = GXT.MESSAGES.htmlEditor_ulTipTitle();
    private String underlineTipText = GXT.MESSAGES.htmlEditor_underlineTipText();
    private String underlineTipTitle = GXT.MESSAGES.htmlEditor_underlineTipTitle();
    private String sourceEditTipText = GXT.MESSAGES.htmlEditor_sourceEditTipText();
    private String sourceEditTipTitle = GXT.MESSAGES.htmlEditor_sourceEditTipTitle();

    public String getBackColorTipText() {
      return backColorTipText;
    }

    public String getBackColorTipTitle() {
      return backColorTipTitle;
    }

    public String getBoldTipText() {
      return boldTipText;
    }

    public String getBoldTipTitle() {
      return boldTipTitle;
    }

    public String getCreateLinkText() {
      return createLinkText;
    }

    public String getDecreaseFontSizeTipText() {
      return decreaseFontSizeTipText;
    }

    public String getDecreaseFontSizeTipTitle() {
      return decreaseFontSizeTipTitle;
    }

    public String getForColorTipTitle() {
      return foreColorTipTitle;
    }

    public String getForeColorTipText() {
      return foreColorTipText;
    }

    public String getIncreaseFontSizeTipText() {
      return increaseFontSizeTipText;
    }

    public String getIncreaseFontSizeTipTitle() {
      return increaseFontSizeTipTitle;
    }

    public String getItalicTipText() {
      return italicTipText;
    }

    public String getItalicTipTitle() {
      return italicTipTitle;
    }

    public String getJustifyCenterTipText() {
      return justifyCenterTipText;
    }

    public String getJustifyCenterTipTitle() {
      return justifyCenterTipTitle;
    }

    public String getJustifyLeftTipText() {
      return justifyLeftTipText;
    }

    public String getJustifyLeftTipTitle() {
      return justifyLeftTipTitle;
    }

    public String getJustifyRightTipText() {
      return justifyRightTipText;
    }

    public String getJustifyRightTipTitle() {
      return justifyRightTipTitle;
    }

    public String getLinkTipText() {
        return linkTipText;
      }

      public String getLinkTipTitle() {
        return linkTipTitle;
      }

      public String getLinkBreakTipText() {
          return "Remove the link";
        }

        public String getLinkBreakTipTitle() {
          return "Remove Link";
        }

    public String getOlTipText() {
      return olTipText;
    }

    public String getOlTipTitle() {
      return olTipTitle;
    }

    public String getUlTipTitle() {
      return ulTipTitle;
    }

    public String getUlTipText() {
      return ulTipText;
    }

    public String getUnderlineTipText() {
      return underlineTipText;
    }

    public String getUnderlineTipTitle() {
      return underlineTipTitle;
    }

    public String getSourceEditTipText() {
      return sourceEditTipText;
    }

    public String getSourceEditTipTitle() {
      return sourceEditTipTitle;
    }

    public void setBackColorTipText(String backColorTipText) {
      this.backColorTipText = backColorTipText;
    }

    public void setBackColorTipTitle(String backColorTipTitle) {
      this.backColorTipTitle = backColorTipTitle;
    }

    public void setBoldTipText(String boldTipText) {
      this.boldTipText = boldTipText;
    }

    public void setBoldTipTitle(String boldTipTitle) {
      this.boldTipTitle = boldTipTitle;
    }

    public void setCreateLinkText(String createLinkText) {
      this.createLinkText = createLinkText;
    }

    public void setDecreaseFontSizeTipText(String decreaseFontSizeTipText) {
      this.decreaseFontSizeTipText = decreaseFontSizeTipText;
    }

    public void setDecreaseFontSizeTipTitle(String decreaseFontSizeTipTitle) {
      this.decreaseFontSizeTipTitle = decreaseFontSizeTipTitle;
    }

    public void setForeColorTipText(String foreColorTipText) {
      this.foreColorTipText = foreColorTipText;
    }

    public void setForeColorTipTitle(String foreColorTipTitle) {
      this.foreColorTipTitle = foreColorTipTitle;
    }

    public void setIncreaseFontSizeTipText(String increaseFontSizeTipText) {
      this.increaseFontSizeTipText = increaseFontSizeTipText;
    }

    public void setIncreaseFontSizeTipTitle(String increaseFontSizeTipTitle) {
      this.increaseFontSizeTipTitle = increaseFontSizeTipTitle;
    }

    public void setItalicTipText(String italicTipText) {
      this.italicTipText = italicTipText;
    }

    public void setItalicTipTitle(String italicTipTitle) {
      this.italicTipTitle = italicTipTitle;
    }

    public void setJustifyCenterTipText(String justifyCenterTipText) {
      this.justifyCenterTipText = justifyCenterTipText;
    }

    public void setJustifyCenterTipTitle(String justifyCenterTipTitle) {
      this.justifyCenterTipTitle = justifyCenterTipTitle;
    }

    public void setJustifyLeftTipText(String justifyLeftTipText) {
      this.justifyLeftTipText = justifyLeftTipText;
    }

    public void setJustifyLeftTipTitle(String justifyLeftTipTitle) {
      this.justifyLeftTipTitle = justifyLeftTipTitle;
    }

    public void setJustifyRightTipText(String justifyRightTipText) {
      this.justifyRightTipText = justifyRightTipText;
    }

    public void setJustifyRightTipTitle(String justifyRightTipTitle) {
      this.justifyRightTipTitle = justifyRightTipTitle;
    }

    public void setLinkTipText(String linkTipText) {
      this.linkTipText = linkTipText;
    }

    public void setLinkTipTitle(String linkTipTitle) {
      this.linkTipTitle = linkTipTitle;
    }

    public void setOlTipText(String olTipText) {
      this.olTipText = olTipText;
    }

    public void setOlTipTitle(String olTipTitle) {
      this.olTipTitle = olTipTitle;
    }

    public void setUlTipText(String ulTipText) {
      this.ulTipText = ulTipText;
    }

    public void setUlTipTitle(String ulTipTitle) {
      this.ulTipTitle = ulTipTitle;
    }

    public void setUnderlineTipText(String underlineTipText) {
      this.underlineTipText = underlineTipText;
    }

    public void setUnderlineTipTitle(String underlineTipTitle) {
      this.underlineTipTitle = underlineTipTitle;
    }

    public void setSourceEditTipText(String sourceEditTipText) {
      this.sourceEditTipText = sourceEditTipText;
    }

    public void setSourceEditTipTitle(String sourceEditTipTitle) {
      this.sourceEditTipTitle = sourceEditTipTitle;
    }

  }

  protected RichTextAreaImpl impl = GWT.create(RichTextAreaImpl.class);

  protected El textarea;
  protected ToolBar toolbar;

  // the toolbar buttons
  protected ToggleButton bold;
  protected ToggleButton italic;
  protected ToggleButton underline;
  protected Button justifyLeft;
  protected Button justifyRight;
  protected Button justifyCenter;
  protected ToggleButton sourceEdit;
  protected Button ol;
  protected Button ul;
  protected Button link;
  protected Button linkBreak;
  protected Button increasefontsize;
  protected Button decreasefontsize;
  protected Button forecolor;
  protected Button backcolor;
  protected SelectionListener<ButtonEvent> btnListener;
  protected FontSize activeFontSize = FontSize.SMALL;
  protected List<FontSize> fontSizesConstants = new ArrayList<FontSize>();

  protected EventHandler handler = new EventHandler();
  protected rte rte;

  private boolean sourceEditMode = true;
  private boolean showToolbar = true;
  private boolean enableFormat = true;
  private boolean enableFontSize = true;
  private boolean enableColors = true;
  private boolean enableAlignments = true;
  private boolean enableLists = true;
  private boolean enableLinks = true;
  private boolean enableFont = true;
  
  private RichTextChangeListener changeListener = null;
  
  public ChimeRichTextEditor(String origContent, boolean showToolbar) {
    super();
    disabledStyle = null;
    fontSizesConstants.add(FontSize.XX_SMALL);
    fontSizesConstants.add(FontSize.X_SMALL);
    fontSizesConstants.add(FontSize.SMALL);
    fontSizesConstants.add(FontSize.MEDIUM);
    fontSizesConstants.add(FontSize.LARGE);
    fontSizesConstants.add(FontSize.X_LARGE);
    fontSizesConstants.add(FontSize.XX_LARGE);
    setSize(500, 300);

    setValue(origContent);
    showToolbar = false;
    messages = new HtmlEditorMessages();
  }

  public void setChangeListener(RichTextChangeListener listener) {
	  changeListener = listener;
  }
  
  public void setHTML(String html) {
	  impl.setHTML(html);
  }
  
  public String getHTML() {
	  return impl.getHTML();
  }
  
  public String getText() {
	  return impl.getText();
  }
  
  @Override
  public void blur() {

  }

  @Override
  public void focus() {

  }

  /**
   * Gets the basic rich text formatting interface.
   * 
   * @return <code>null</code> if basic formatting is not supported
   */
  public BasicFormatter getBasicFormatter() {
    if (impl instanceof BasicFormatter) {
      return (BasicFormatter) impl;
    }
    return null;
  }

  /**
   * Gets the full rich text formatting interface.
   * 
   * @return <code>null</code> if full formatting is not supported
   */
  public ExtendedFormatter getExtendedFormatter() {
    if (impl instanceof ExtendedFormatter) {
      return (ExtendedFormatter) impl;
    }
    return null;
  }

  @Override
  public HtmlEditorMessages getMessages() {
    return (HtmlEditorMessages) messages;
  }

  @Override
  public String getRawValue() {
    if (sourceEdit != null && sourceEdit.isPressed()) {
      pushValue();
    } else {
      syncValue();
    }

    String value = super.getRawValue();
    if ("&#8203;".equals(value)) {
      return "";
    } else {
      return value;
    }
  }

  @Override
  public HtmlEditorImages getImages() {
    if (images == null) {
      images = new HtmlEditorImages();
    }
    return (HtmlEditorImages) images;
  }

  public boolean isEnableAlignments() {
    return enableAlignments;
  }

  public boolean isEnableColors() {
    return enableColors;
  }

  public boolean isEnableFont() {
    return enableFont;
  }

  public boolean isEnableFontSize() {
    return enableFontSize;
  }

  public boolean isEnableFormat() {
    return enableFormat;
  }

  public boolean isEnableLinks() {
    return enableLinks;
  }

  public boolean isEnableLists() {
    return enableLists;
  }

  public boolean isShowToolbar() {
    return showToolbar;
  }

  public boolean isSourceEditMode() {
    return sourceEditMode;
  }

  public void pushValue() {
    impl.setHTML(getInputEl().getValue());
  }

  public void setEnableAlignments(boolean enableAlignments) {
    this.enableAlignments = enableAlignments;
  }

  public void setEnableColors(boolean enableColors) {
    this.enableColors = enableColors;
  }

  public void setEnableFont(boolean enableFont) {
    this.enableFont = enableFont;
  }

  public void setEnableFontSize(boolean enableFontSize) {
    this.enableFontSize = enableFontSize;
  }

  public void setEnableFormat(boolean enableFormat) {
    this.enableFormat = enableFormat;
  }

  public void setEnableLinks(boolean enableLinks) {
    this.enableLinks = enableLinks;
  }

  public void setEnableLists(boolean enableLists) {
    this.enableLists = enableLists;
  }

  @Override
  public void setRawValue(String value) {
    impl.setHTML(value);
    super.setRawValue(value);
  }

  public void setShowToolbar(boolean showToolbar) {
    this.showToolbar = showToolbar;
  }

  public void setSourceEditMode(boolean mode) {
    assertPreRender();
    sourceEditMode = mode;
  }

  @Override
  public void setValue(String value) {
    impl.setHTML(value);
    super.setValue(value);
  }

  public void syncValue() {
    getInputEl().setValue(impl.getHTML());
  }

  @Override
  public boolean validate(boolean silent) {
    return true;
  }

  protected Button createColorButton(AbstractImagePrototype icon, String toolTip, String toolTipTitle,
      Listener<ComponentEvent> listener) {
    Button item = new Button();
    item.setIcon(icon);
    item.setTabIndex(-1);

    ToolTipConfig cfg = new ToolTipConfig(toolTipTitle, toolTip);
    item.setToolTip(cfg);

    ColorMenu menu = new ColorMenu();
    menu.getColorPalette().addListener(Events.Select, listener);
    item.setMenu(menu);
    return item;
  }

  protected ToggleButton createToggleButton(AbstractImagePrototype icon, String toolTip, String toolTipTitle) {
    ToggleButton item = new ToggleButton();
    item.setTabIndex(-1);

    ToolTipConfig cfg = new ToolTipConfig(toolTipTitle, toolTip);
    item.setToolTip(cfg);

    item.setIcon(icon);
    item.addSelectionListener(btnListener);
    return item;
  }

  protected Button createButton(AbstractImagePrototype icon, String toolTip, String toolTipTitle) {
    Button item = new Button();
    item.setIcon(icon);
    item.setTabIndex(-1);

    ToolTipConfig cfg = new ToolTipConfig(toolTipTitle, toolTip);
    item.setToolTip(cfg);

    item.addSelectionListener(btnListener);
    return item;
  }

  protected void doAttachChildren() {
    super.doAttachChildren();
    ComponentHelper.doAttach(toolbar);
    ComponentHelper.doAttach(rte);
  }

  protected void doDetachChildren() {
    super.doAttachChildren();
    ComponentHelper.doDetach(toolbar);
    ComponentHelper.doDetach(rte);
  }

  @Override
  protected El getInputEl() {
    return textarea;
  }

  @Override
  protected void onBlur(ComponentEvent ce) {

  }

  @Override
  protected void onDisable() {
    super.onDisable();
    if (toolbar != null) {
      toolbar.disable();
    }
    mask();
  }

  @Override
  protected void onEnable() {
    super.onEnable();
    if (toolbar != null) {
      toolbar.enable();
    }
    unmask();
  }

  @Override
  protected void onFocus(ComponentEvent ce) {

  }

  @Override
  protected void onRender(Element target, int index) {
    setElement(DOM.createDiv(), target, index);
    addStyleName("x-html-editor-wrap");
    rte = new rte();

    textarea = new El(DOM.createTextArea());
    textarea.addStyleName("x-hidden");
    textarea.addStyleName("x-form-textarea");
    textarea.addStyleName("x-form-field");
    textarea.setStyleAttribute("border", "0 none");
    el().appendChild(textarea.dom);
    rte.render(el().dom);
    setupToolbar();
    setupChangeListener();
    super.onRender(target, index);
  }

  protected void setupChangeListener() {
	  
  }
  
  @Override
  protected void onResize(int width, int height) {
    super.onResize(width, height);

    int aw = width;
    int ah = height;

    Size frameWidth = el().getFrameSize();
    aw -= frameWidth.width;
    ah -= frameWidth.height;

    if (showToolbar) {
      el().down(".x-html-editor-tb").setWidth(aw, true);
      toolbar.setWidth(aw);
      ah -= toolbar.getHeight();
    }
    rte.setSize(aw, ah);

    textarea.setSize(aw, ah, true);
  }

  protected void setupToolbar() {
    if (showToolbar) {
      btnListener = new SelectionListener<ButtonEvent>() {

        @Override
        public void componentSelected(ButtonEvent ce) {
          Component item = ce.getComponent();
          if (item == bold) {
            getBasicFormatter().toggleBold();
          } else if (item == italic) {
            getBasicFormatter().toggleItalic();
          } else if (item == underline) {
            getBasicFormatter().toggleUnderline();
          } else if (item == sourceEdit) {
            toggleSourceEditMode();
            focus();
          } else if (item == ol) {
            getExtendedFormatter().insertOrderedList();
          } else if (item == ul) {
            getExtendedFormatter().insertUnorderedList();
          } else if (item == link) {
        	  promptForLink();
          } else if (item == linkBreak) {
        	  clearLink();
          } else if (item == justifyLeft) {
            getBasicFormatter().setJustification(Justification.LEFT);
          } else if (item == justifyCenter) {
            getBasicFormatter().setJustification(Justification.CENTER);
          } else if (item == justifyRight) {
            getBasicFormatter().setJustification(Justification.RIGHT);
          } else if (item == increasefontsize) {
            int i = fontSizesConstants.indexOf(activeFontSize);
            if (i < (fontSizesConstants.size() - 1)) {
              i++;
              activeFontSize = fontSizesConstants.get(i);
              getBasicFormatter().setFontSize(activeFontSize);
            } else {
              // brings focus back to the editor
              focus();
            }
          } else if (item == decreasefontsize) {
            int i = fontSizesConstants.indexOf(activeFontSize);
            if (i > 0) {
              i--;
              activeFontSize = fontSizesConstants.get(i);
              getBasicFormatter().setFontSize(activeFontSize);
            } else {
              // brings focus back to the editor
              focus();
            }
          }
        }
      };

      toolbar = new ToolBar();
      if (sourceEditMode) {
        toolbar.add(sourceEdit = createToggleButton(getImages().getSource(), getMessages().getSourceEditTipText(),
            getMessages().getSourceEditTipTitle()));
        toolbar.add(new SeparatorToolItem());
      }

      if (getBasicFormatter() != null) {
        if (enableFont) {

          final ListBox listBox = new ListBox();
          listBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
              ListBox listBox = (ListBox) event.getSource();
              getBasicFormatter().setFontName(listBox.getValue(listBox.getSelectedIndex()));
            }
          });
          listBox.addItem("Arial");

          listBox.addItem("Times New Roman");
          listBox.addItem("Verdana");

          toolbar.add(new WidgetComponent(listBox));
          toolbar.add(new SeparatorToolItem());
        }
        if (enableFontSize) {
          toolbar.add(increasefontsize = createButton(getImages().getFontIncrease(),
              getMessages().getIncreaseFontSizeTipText(), getMessages().getIncreaseFontSizeTipTitle()));
          toolbar.add(decreasefontsize = createButton(getImages().getFontDecrease(),
              getMessages().getDecreaseFontSizeTipText(), getMessages().getDecreaseFontSizeTipTitle()));

          toolbar.add(new SeparatorToolItem());
        }
        if (enableFormat) {
          toolbar.add(bold = createToggleButton(getImages().getBold(), getMessages().getBoldTipText(),
              getMessages().getBackColorTipTitle()));
          toolbar.add(italic = createToggleButton(getImages().getItalic(), getMessages().getItalicTipText(),
              getMessages().getItalicTipTitle()));
          toolbar.add(underline = createToggleButton(getImages().getUnderline(), getMessages().getUnderlineTipText(),
              getMessages().getUnderlineTipTitle()));
          toolbar.add(new SeparatorToolItem());
        }
        if (enableAlignments) {
          toolbar.add(justifyLeft = createButton(getImages().getJustifyLeft(), getMessages().getJustifyLeftTipText(),
              getMessages().getJustifyLeftTipTitle()));
          toolbar.add(justifyCenter = createButton(getImages().getJustifyCenter(),
              getMessages().getJustifyCenterTipText(), getMessages().getJustifyCenterTipTitle()));
          toolbar.add(justifyRight = createButton(getImages().getJustifyRight(),
              getMessages().getJustifyRightTipText(), getMessages().getJustifyRightTipTitle()));
          toolbar.add(new SeparatorToolItem());
        }
      }

      if (getExtendedFormatter() != null) {
        if (enableLists) {
          toolbar.add(ol = createButton(getImages().getOl(), getMessages().getOlTipText(),
              getMessages().getOlTipTitle()));
          toolbar.add(ul = createButton(getImages().getUl(), getMessages().getUlTipText(),
              getMessages().getUlTipTitle()));
          toolbar.add(new SeparatorToolItem());
        }
        if (enableLinks) {
            toolbar.add(link = createButton(getImages().getLink(), getMessages().getLinkTipText(),
                    getMessages().getLinkTipTitle()));
            toolbar.add(linkBreak = createButton(getImages().getLinkBreak(), getMessages().getLinkBreakTipText(),
                    getMessages().getLinkBreakTipTitle()));
          toolbar.add(new SeparatorToolItem());
        }
        if (enableColors) {
          toolbar.add(forecolor = createColorButton(getImages().getFontColor(), getMessages().getForeColorTipText(),
              getMessages().getForColorTipTitle(), new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent be) {
                  getBasicFormatter().setForeColor(((ColorPalette) be.getComponent()).getValue());
                }
              }));
          toolbar.add(backcolor = createColorButton(getImages().getFontHighlight(),
              getMessages().getBackColorTipText(), getMessages().getBackColorTipTitle(),
              new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent be) {
                  getBasicFormatter().setBackColor(((ColorPalette) be.getComponent()).getValue());
                }
              }));
        }
      }
      Element e = DOM.createDiv();
      e.setClassName("x-html-editor-tb");
      el().insertFirst(e);
      toolbar.render(e);
    }
  }

    protected void promptForLink() {
    	final LinkPromptWindow w = new LinkPromptWindow();
    	w.show(
    		new Runnable() {
    			public void run() {
    				String url = w.getURL();
    		        if (url != null && url.length() > 0) {
    		            getExtendedFormatter().createLink(url);
    		        } else {
    		            getExtendedFormatter().removeLink();
    		        }
    			}
    		}
    	);
    }
    
    protected void clearLink() {
    	getExtendedFormatter().removeLink();
    }
    
  protected void toggleSourceEditMode() {
    if (sourceEdit.isPressed()) {
      syncValue();
      rte.el().addStyleName("x-hidden");
      textarea.removeStyleName("x-hidden");
    } else {
      pushValue();
      rte.el().removeStyleName("x-hidden");
      textarea.addStyleName("x-hidden");
    }
    for (Component item : toolbar.getItems()) {
      if (item != sourceEdit) {
        item.setEnabled(!item.isEnabled());
      }
    }
  }

  protected void updateStatus() {
    if (showToolbar && rendered) {
      BasicFormatter basic = getBasicFormatter();
      if (basic != null) {
        bold.toggle(basic.isBold());
        underline.toggle(basic.isUnderlined());
        italic.toggle(basic.isItalic());
      }
    }
    
    if (changeListener != null) {
    	changeListener.onChange();
    }
  }
}
