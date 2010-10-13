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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.binder.DataViewBinder;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.core.Template;
import com.extjs.gxt.ui.client.core.XDOM;
import com.extjs.gxt.ui.client.data.BaseModelStringProvider;
import com.extjs.gxt.ui.client.data.ModelStringProvider;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.PreviewEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionProvider;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreListener;
import com.extjs.gxt.ui.client.util.BaseEventPreview;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.util.Size;
import com.extjs.gxt.ui.client.widget.DataView;
import com.extjs.gxt.ui.client.widget.DataViewItem;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.ListModelPropertyEditor;
import com.extjs.gxt.ui.client.widget.form.PropertyEditor;
import com.extjs.gxt.ui.client.widget.form.TwinTriggerField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.RootPanel;
import com.paxxis.chime.client.common.Cursor;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceRequest;
import com.paxxis.chime.client.common.DataInstanceResponse;
import com.paxxis.chime.client.common.FindInstancesRequest;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.ShapeRequest;
import com.paxxis.chime.client.common.DataInstanceRequest.ClauseOperator;
import com.paxxis.chime.client.common.DataInstanceRequest.Depth;
import com.paxxis.chime.client.common.DataInstanceRequest.Operator;
import com.paxxis.chime.client.widgets.InstancePreviewPopup;

/**
 * 
 * @author Robert Englander
 *
 */
public class DataInstanceComboBox extends TwinTriggerField<DataInstanceModel> implements SelectionProvider<DataInstanceModel>
{

    public class DataInstanceValidator implements Validator
    {
        private String _error;
        
        public DataInstanceValidator(String error)
        {
            _error = error;
        }
        
		public String validate(Field<?> field, String value) {
            return _error;
		}
        
    }
  /**
   * ComboBox error messages.
   */
  public class ComboBoxMessages extends TextFieldMessages {

    private String valueNoutFoundText;

    /**
     * Returns the value not found error text.
     * 
     * @return the error text
     */
    public String getValueNoutFoundText() {
      return valueNoutFoundText;
    }

    /**
     * When using a name/value combo, if the value passed to setValue is not
     * found in the store, valueNotFoundText will be displayed as the field text
     * if defined.
     * 
     * @param valueNoutFoundText
     */
    public void setValueNoutFoundText(String valueNoutFoundText) {
      this.valueNoutFoundText = valueNoutFoundText;
    }

  }

  /**
   * CSS style name to apply to the selected item in the dropdown list (defaults
   * to 'x-combo-selected')
   */
  protected String selectedStyle = "endslice-view-item-selected";

  private ModelStringProvider modelStringProvider;
  private String valueField;
  private boolean forceSelection;
  private String listAlign = "tl-bl?";
  private int maxHeight = 300;
  private int minListWidth = 70;
  private boolean editable = true;
  private BaseEventPreview eventPreview;
  private boolean expanded = false;
  private DataViewItem selectedItem;
  private DataInstanceStore store;
  private StoreListener storeListener;
  private Template template;
  private DataView view;
  private DataViewBinder<DataInstanceModel> binder;
  private DataInstanceModel lastSelectionText = null;
  private String lastStartsWith = null;
  private String lastKeyedRawValue = "";
  private boolean adjustWidth = false;
  private boolean _excludeInternals = true;
  private InstanceId dataShapeId = InstanceId.UNKNOWN;
  private Shape dataShape = null;
  private boolean _searchable = false;
  private boolean _useValidation = true;
  private boolean _showTrigger = false;
  private boolean returnFullInstances = false;

  private PaginatedResultsPanel list;
  private DataInputListener _dataInstanceListener;

  private boolean userCreatedOnly = false;
  private boolean directCreatableOnly = false;
  
  private InstancePreviewPopup previewPopup;

  private boolean ignoreNext;
  
  //private List<Runnable> postShapeRetrieve = new ArrayList<Runnable>();

    /**
    * Creates a combo box.
    */
    public DataInstanceComboBox(DataInputListener listener) 
    {
        this(listener, InstanceId.UNKNOWN, false, false, true, false);
    }

    public DataInstanceComboBox(DataInputListener listener, InstanceId shapeId, boolean searchable, boolean directCreatableOnly, boolean useValidation) {
        this(listener, shapeId, searchable, directCreatableOnly, useValidation, false);
    }

    public DataInstanceComboBox(DataInputListener listener, Shape dataType, boolean searchable, boolean directCreatableOnly, boolean useValidation) {
        this(listener, dataType.getId(), searchable, directCreatableOnly, useValidation, false);
        dataShape = dataType;
    }

    public DataInstanceComboBox(DataInputListener listener, InstanceId shapeId, 
            boolean searchable, boolean directCreatableOnly, boolean useValidation, boolean showTrigger)
    {
        _dataInstanceListener = listener;
        dataShapeId = shapeId;
        _searchable = searchable;
        _useValidation = useValidation;
        _showTrigger = showTrigger;
        this.directCreatableOnly = directCreatableOnly;
        
        this.adjustSize = false;
       
        messages = new ComboBoxMessages();
        modelStringProvider = new BaseModelStringProvider();
        setTriggerStyle("x-form-clear-trigger");
        setPropertyEditor(new ListModelPropertyEditor<DataInstanceModel>());
        initComponent();
    }

    public void markInvalid(String msg) {
    	getInputEl().addStyleName(invalidStyle);
    }

    public void setAdjustWidth(boolean adjust) {
    	adjustWidth = adjust;
    }
    
    public void setReturnFullInstances(boolean value) {
        returnFullInstances = value;
    }
    
  public void addSelectionChangedListener(SelectionChangedListener listener) {
    addListener(Events.SelectionChange, listener);
  }

    public void setUserCreatedOnly(boolean val) {
        userCreatedOnly = val;
    }

  /**
   * Clears any text/value currently set in the field.
   */
  public void clearSelections() 
  {
    setRawValue("");
    lastSelectionText = null;
    applyEmptyText();
    value = null;
  }

  /**
   * Hides the dropdown list if it is currently expanded. Fires the <i>Collapse</i>
   * event on completion.
   */
  public void collapse() {
    if (!isExpanded()) {
      return;
    }
    expanded = false;
    store.setActive(false);
    list.setHeight("auto");
    
    list.hide();
    //list.el().slideOut(Direction.UP, FxConfig.NONE);
    eventPreview.remove();
	previewPopup.hide();
    
    //view.getSelectionModel().deselectAll();
  
    //fireEvent(Events.Collapse, new FieldEvent(this));

  }

    /**
    * Expands the dropdown list if it is currently hidden. Fires the <i>expand</i>
    * event on completion.
    */
    public void expand() 
    {
        if (expanded) 
        {
            return;
        }

        if (_searchable)
        {
            expanded = true;
            store.setActive(true);

            DataInstanceModel r = findModel(getDisplayField(), getRawValue());
            if (r != null) {
              binder.setSelection(r);
            }
            list.el().setVisibility(false);
            list.el().updateZIndex(0);
            list.show();
            restrict();
            list.el().setVisibility(true);
            //list.el().slideIn(Direction.DOWN, FxConfig.NONE);

            eventPreview.add();
            fireEvent(Events.Expand, new FieldEvent(this));

            list.layout();
            view.setVisible(true);
            // don't query if there's already a keystroke timer going
            if (strokeCounter == 0)
            {
                findInstances(getRawValue());
            }
        }
    }

  /**
   * Returns the combos data view.
   * 
   * @return the view
   */
  public DataView getDataView() {
    return view;
  }

  /**
   * Returns the display field.
   * 
   * @return the display field
   */
  public String getDisplayField() {
    return getPropertyEditor().getDisplayProperty();
  }

  /**
   * Returns true if the field's value is forced to one of the value in the
   * list.
   * 
   * @return the force selection state
   */
  public boolean getForceSelection() {
    return forceSelection;
  }

  /**
   * Returns the list's list align value.
   * 
   * @return the list align valu
   */
  public String getListAlign() {
    return listAlign;
  }

  /**
   * Returns the dropdown list's max height.
   * 
   * @return the max height
   */
  public int getMaxHeight() {
    return maxHeight;
  }

  @Override
  public ComboBoxMessages getMessages() {
    return (ComboBoxMessages) messages;
  }

  /**
   * Returns the dropdown list's min width.
   * 
   * @return the min width
   */
  public int getMinListWidth() {
    return minListWidth;
  }

  /**
   * Returns the model string provider.
   * 
   * @return the model string provider
   */
  public ModelStringProvider<DataInstanceModel> getModelStringProvider() {
    return modelStringProvider;
  }

  @Override
  public ListModelPropertyEditor<DataInstanceModel> getPropertyEditor() {
    return (ListModelPropertyEditor<DataInstanceModel>) propertyEditor;
  }

  /**
   * Returns the selected style.
   * 
   * @return the selected style
   */
  public String getSelectedStyle() {
    return selectedStyle;
  }

  public List<DataInstanceModel> getSelection() {
    List<DataInstanceModel> sel = new ArrayList<DataInstanceModel>();
    DataInstanceModel v = getValue();
    if (v != null) {
      sel.add(v);
    }
    return sel;
  }

  /**
   * Returns the combo's store.
   * 
   * @return the store
   */
  public ListStore<DataInstanceModel> getStore() {
    return store;
  }

  /**
   * Returns the custom template.
   * 
   * @return the template
   */
  public Template getTemplate() {
    if (template == null) {
      String t = "<div id='{id}' class='x-combo-list-item'>{" + getDisplayField() + "}</div>";
      template = new Template(t);
    }
    return template;
  }

  @Override
  public DataInstanceModel getValue() {
    if (store != null) {
      getPropertyEditor().setList(store.getModels());
    }
    return super.getValue();
  }

  /**
   * Returns the combo's value field.
   * 
   * @return the value field
   */
  public String getValueField() {
    return valueField;
  }

  /**
   * Returns <code>true</code> if the panel is expanded.
   * 
   * @return the expand state
   */
  public boolean isExpanded() {
    return expanded;
  }

  public void removeSelectionListener(SelectionChangedListener listener) {
    removeListener(Events.SelectionChange, listener);
  }

    /**
    * Select an item in the dropdown list by its numeric index in the list. This
    * function does NOT cause the select event to fire. The list must expanded
    * for this function to work, otherwise use #setValue.
    * 
    * @param index the index of the item to select
    */
    public void select(int index) 
    {
        if (view != null) 
        {
            DataViewItem item = view.getItem(index);
            
            if (item != null) 
            {
                DataInstanceModel model = (DataInstanceModel)item.getModel();
                binder.setSelection(model);
                setValue(model);
                selectedItem = item;
                view.setSelectedItem(item);
                view.scrollIntoView(item);
            }
        }
    }

  /**
   * The underlying data field name to bind to this ComboBox (defaults to
   * 'text').
   * 
   * @param displayField the display field
   */
  public void setDisplayField(String displayField) {
    getPropertyEditor().setDisplayProperty(displayField);
  }

  /**
   * Allow or prevent the user from directly editing the field text. If false is
   * passed, the user will only be able to select from the items defined in the
   * dropdown list.
   * 
   * @param value true to allow the user to directly edit the field text
   */
  public void setEditable(boolean value) {
    if (value == this.editable) {
      return;
    }
    this.editable = value;
    if (rendered) {
      El fromEl = getInputEl();
      if (!value) {
        fromEl.dom.setPropertyBoolean("readOnly", true);
        fromEl.addStyleName("x-combo-noedit");
      } else {
        fromEl.dom.setPropertyBoolean("readOnly", false);
        fromEl.removeStyleName("x-combo-noedit");
      }
    }
  }

  /**
   * Sets the panel's expand state.
   * 
   * @param expand <code>true<code> true to expand
   */
  public void setExpanded(boolean expand) {
    this.expanded = expand;
    if (isRendered()) {
      if (expand) {
        expand();
      } else {
        collapse();
      }
    }
  }

  /**
   * Sets whether the combo's value is restricted to one of the values in the
   * list, false to allow the user to set arbitrary text into the field
   * (defaults to false).
   * 
   * @param forceSelection true to force selection
   */
  public void setForceSelection(boolean forceSelection) {
    this.forceSelection = forceSelection;
  }

  /**
   * Sets a valid anchor position value. See {@link El#alignTo} for details on
   * supported anchor positions (defaults to 'tl-bl?').
   * 
   * @param listAlign the new list align value
   */
  public void setListAlign(String listAlign) {
    this.listAlign = listAlign;
  }

  /**
   * Sets the maximum height in pixels of the dropdown list before scrollbars
   * are shown (defaults to 300).
   * 
   * @param maxHeight the max hieght
   */
  public void setMaxHeight(int maxHeight) {
    this.maxHeight = maxHeight;
  }

  /**
   * Sets the minimum width of the dropdown list in pixels (defaults to 70, will
   * be ignored if listWidth has a higher value).
   * 
   * @param minListWidth the min width
   */
  public void setMinListWidth(int minListWidth) {
    this.minListWidth = minListWidth;
  }

  /**
   * Sets the model string provider (defaults to {@link BaseModelStringProvider}.
   * 
   * @param modelStringProvider the string provider
   */
  public void setModelStringProvider(ModelStringProvider<DataInstanceModel> modelStringProvider) {
    this.modelStringProvider = modelStringProvider;
  }

  @Override
  public void setPropertyEditor(PropertyEditor<DataInstanceModel> propertyEditor) {
    assert propertyEditor instanceof ListModelPropertyEditor : "PropertyEditor must be a ModelPropertyEditor instance";
    super.setPropertyEditor(propertyEditor);
  }

    @Override
    public void setRawValue(String text) 
    {
        if (text == null) 
        {
            String msg = getMessages().getValueNoutFoundText();
            text = msg != null ? msg : "";
        }

        getInputEl().setValue(text);
    }

    
    
  /**
   * Sets the CSS style name to apply to the selected item in the dropdown list
   * (defaults to 'x-combo-selected').
   * 
   * @param selectedStyle the selected style
   */
  public void setSelectedStyle(String selectedStyle) {
    this.selectedStyle = selectedStyle;
  }

  public void setSelection(List<DataInstanceModel> selection) {
    if (selection.size() > 0) {
      setValue(selection.get(0));
    } else {
      setValue((DataInstanceModel)null);
    }
  }

  /**
   * Sets the combo's store.
   * 
   * @param store the store
   */
  public void setStore(DataInstanceStore store) {
    this.store = store;
  }

  /**
   * Sets the template to be used to render each item in the drop down.
   * 
   * @param template the template
   */
  public void setTemplate(Template template) {
    assertPreRender();
    this.template = template;
  }

    public void applyInput(final String txt) {
    	DeferredCommand.addCommand(
			new Command() {
				public void execute() {
			        setRawValue(txt);
			        processKeyboardSelection(false);
				}
			}
    	);
    }

    public void setExcludeInternals(boolean val) {
        _excludeInternals = val;
    }
    
  @Override
  public void setValue(DataInstanceModel value) {
    super.setValue(value);
    this.lastSelectionText = getValue();
    
    if (value == null)
    {
        setRawValue("");
    }
    else
    {
        setRawValue(value.getDataInstance().getName());
    }
    
    SelectionChangedEvent se = new SelectionChangedEvent(this, getSelection());
    fireEvent(Events.SelectionChange, se);
  }

  
  /**
   * The underlying data value name to bind to this ComboBox.
   * 
   * @param valueField the value field
   */
  public void setValueField(String valueField) {
    this.valueField = valueField;
  }

  protected void doForce() {
    if (getValue() == null) 
    {
        setValue(lastSelectionText);
      //setRawValue(lastSelectionText != null ? lastSelectionText : "");
    }
  }

  protected DataInstanceModel findModel(String property, String value) {
    if (value == null) return null;
    for (DataInstanceModel model : store.getModels()) {
      if (value.equals(getStringValue(model, property))) {
        return model;
      }
    }
    return null;
  }

  @Override
  protected El getFocusEl() 
  {
    return input;
  }

  protected String getStringValue(DataInstanceModel model, String propertyName) {
    return getModelStringProvider().getStringValue(model, propertyName);
  }

  protected void initComponent() 
  {
      storeListener = new StoreListener() {

      @Override
      public void storeBeforeDataChanged(StoreEvent se) {
        onBeforeLoad(se);
      }

      @Override
      public void storeDataChanged(StoreEvent se) {
        onLoad(se);
      }

    };

    eventPreview = new BaseEventPreview() 
    {
        protected boolean onAutoHide(PreviewEvent ce) 
        {
            //if (list.getElement() == ce.getTarget()) 
            Element target = DOM.eventGetTarget(ce.getEvent());
            if (DOM.isOrHasChild(list.getElement(), target)) 
            {
                return false;
            }

            collapse();
            return true;
        }

        protected void onClick(PreviewEvent pe) 
        {
            Element target = DOM.eventGetTarget(pe.getEvent());
            if (DOM.isOrHasChild(view.getElement(), target)) 
            {
                processMouseSelection();
            }
        }
    };

    
    
    new KeyNav(this) 
    {
    	public void onBackspace(final ComponentEvent evt) {
            clearValidation();
            
            DeferredCommand.addCommand(
                new Command()
                {
                    public void execute()
                    {
                        String txt = getRawValue();
                        startKeystrokeTimer(txt);

                        if (!isExpanded())
                        {
                            onTwinTriggerClick(evt);
                        }
                    }
                }
            );
    	}
    	
    	public void onDelete(final ComponentEvent evt) {
            clearValidation();
            
            DeferredCommand.addCommand(
                new Command()
                {
                    public void execute()
                    {
                        String txt = getRawValue();
                        startKeystrokeTimer(txt);

                        if (!isExpanded())
                        {
                            onTwinTriggerClick(evt);
                        }
                    }
                }
            );
    	}
    	
        public void onKeyPress(final ComponentEvent cd)
        {
            if (!cd.isSpecialKey())
            {
                clearValidation();
                
                DeferredCommand.addCommand(
                    new Command()
                    {
                        public void execute()
                        {
                            String txt = getRawValue();
                            startKeystrokeTimer(txt);

                            if (!isExpanded())
                            {
                                onTwinTriggerClick(cd);
                            }
                        }
                    }
                );
            }
        }
        
        public void onDown(ComponentEvent ce) 
        {
            if (!isExpanded()) 
            {
            	lastStartsWith = null;
            	expand();
            } 
            else 
            {
                selectNext();
                ce.stopEvent();
            }
        }

        public void onRight(ComponentEvent ce)
        {
            if (ce.isControlKey())
            {
                list.queryNext();
                ce.stopEvent();
            }
        }
        
        public void onLeft(ComponentEvent ce)
        {
            if (ce.isControlKey())
            {
                list.queryPrevious();
                ce.stopEvent();
            }
        }
        
        public void onEsc(ComponentEvent ce)
        {
            if (isExpanded()) 
            {
                onTwinTriggerClick(ce);
            } 
        }
        
        public void onEnter(ComponentEvent ce) 
        {
            // ot sure why the stopEvent is required, but without it
            // this event gets fired a second time.  this didn't happen until
            // GXT 1.1
            ce.stopEvent();
            processKeyboardSelection(true);
        }

        public void onTab(ComponentEvent ce) 
        {
            processKeyboardSelection(false);
            //ce.stopEvent();
        }

        public void onUp(ComponentEvent ce) 
        {
            if (isExpanded())
            {
                selectPrev();
                ce.stopEvent();
            }
        }

    };
  }
  
    protected void clearValidation()
    {
        setValidator(null);
        validate();
    }

    public Shape getDataShape() {
    	return dataShape;
    }
    
    public InstanceId getDataShapeId() {
        return dataShapeId;
    }

    public void setShape(InstanceId shapeId, boolean searchable)
    {
        dataShapeId = shapeId;
        _searchable = searchable;
        lastStartsWith = null;

        AsyncCallback callback = new AsyncCallback() {
            public void onFailure(Throwable result) {
            }

            public void onSuccess(final Object result) {
                ShapeResponseObject resp = (ShapeResponseObject)result;
                if (resp.isResponse()) {
                	dataShape = resp.getResponse().getShape();
                    if (getValue() != null)
                    {
                        setValue((DataInstanceModel)null);
                    }
                    else
                    {
                        setRawValue("");
                    }
                    
                    if (_searchable) {
                        //addTrigger();
                    } else {
                        //removeTrigger();
                    }
                }
            }
        };
        
        ShapeRequest req = new ShapeRequest();
        req.setId(dataShapeId);
        ServiceManager.getService().sendShapeRequest(req, callback);
    } 
    
    public void findInstances(String startsWith)
    {
        if (_searchable)
        {
            boolean proceed = true;

            if (lastStartsWith != null)
            {
                proceed = !lastStartsWith.equals(startsWith);
            }

            if (proceed)
            {
                lastStartsWith = startsWith;
                view.setSelectedItem(null);
                store.removeAll();

                FindInstancesRequest request = new FindInstancesRequest();
                request.setCursor(new Cursor(20));
                request.setShape(dataShape);
                request.setExcludeInternals(_excludeInternals);
                request.setString(startsWith);
                request.setUserCreated(userCreatedOnly);
                request.setDirectCreatable(directCreatableOnly);
                request.setUser(ServiceManager.getActiveUser());

                list.query(request);
            }
        }
    }
  
    protected void initList() 
    {
        //if (!_showTrigger) {
        //    trigger.disable();
       // }
        
        if (view == null) 
        {

            list = new PaginatedResultsPanel(PaginatedResultsPanel.Type.ListBox);

            previewPopup = new InstancePreviewPopup(InstancePreviewPopup.Location.Right, true);
        	list.setInstanceHoverListener(
        		new PaginatedResultsPanel.InstanceHoverListener() {
					public void onHover(DataInstance instance) {
						startInstanceHoverTimer(instance);
					}
        		}
        	);
            
            /*
            String style = "x-combo-list";

            list = new LayoutContainer();
            list.setShim(true);
            list.setShadow(true);
            list.setBorders(true);
            list.setStyleName(style);
            list.setScrollMode(Scroll.AUTO);
            */
            
            list.hide();

            //assert store != null;
            store = (DataInstanceStore)list.getStore();
            store.setActive(false);
            
            //view = new DataView();
            view = list.getView();
            
            
            /*
            view.setSelectOnOver(true);
            view.setBorders(false);
            view.setStyleAttribute("overflow", "hidden");
            */
            
            view.addListener(Events.SelectionChange, new Listener<ComponentEvent>() 
            {
                public void handleEvent(ComponentEvent ce) 
                {
                    selectedItem = view.getSelectedItem();
                }
            });
            
            //list.add(view);

            RootPanel.get().add(list);
            list.layout();

            /*
            view.setStyleAttribute("backgroundColor", "white");
            view.setTemplate(getTemplate());
            view.setSelectionMode(SelectionMode.SINGLE);
            view.setSelectStyle(selectedStyle);
            view.setItemSelector("." + style + "-item");
            */
        }

        bindStore(store, true);
    }

  protected void onBeforeLoad(StoreEvent se) {

  }

  @Override
  protected void onBlur(ComponentEvent ce) {
    if (ignoreNext) {
      ignoreNext = false;
      return;
    }
    super.onBlur(ce);
    if (forceSelection) {
      doForce();
    }
  }

    @Override
    protected void onClick(ComponentEvent ce) {
        if (!editable && ce.getTarget() == getInputEl().dom) {
          onTwinTriggerClick(ce);
          return;
        }
        super.onClick(ce);
    }

    @Override
    protected void onKeyDown(FieldEvent fe) {
        if (fe.getKeyCode() == KeyboardListener.KEY_TAB) {
          if (expanded) {
            collapse();
          }
        }
    }
  
    public void invalidate(String message)
    {
        setValidator(new DataInstanceValidator(message));
        validate();
        //setValue(null);
    }
    
    private int strokeCounter = 0;
    private int hoverCounter = 0;
    
    protected void startInstanceHoverTimer(final DataInstance instance) {
        hoverCounter++;

        Timer t = new Timer() {
            @Override
            public void run() {
                hoverCounter--;
                if (hoverCounter == 0) {
            		previewPopup.showInstance(list, instance);
                }
            }
        };

        t.schedule(500);
    }
    
    protected void startKeystrokeTimer(String startsWith) 
    {
        boolean proceed = true;
        
        if (lastStartsWith != null)
        {
            proceed = !lastStartsWith.equals(startsWith);
        }
        
        if (proceed)
        {
            lastKeyedRawValue = startsWith;

            strokeCounter++;

            Timer t = new Timer()
            {
                @Override
                public void run() 
                {
                    strokeCounter--;
                    if (strokeCounter == 0)
                    {
                        String txt = getRawValue();
                        findInstances(txt);
                    }
                }
            };

            t.schedule(500);
        }
    }

  protected void onLoad(StoreEvent se) {

  }

  protected void onRender(Element parent, int index) {
    super.onRender(parent, index);
    input.addStyleName(fieldStyle);
    el().addEventsSunk(Event.KEYEVENTS);

    if (!this.editable) {
      this.editable = true;
      this.setEditable(false);
    }

    if (value != null) {
      setRawValue(getPropertyEditor().getStringValue(value));
    }

    AsyncCallback callback = new AsyncCallback() {
        public void onFailure(Throwable result) {
        }

        public void onSuccess(final Object result) {
            ShapeResponseObject resp = (ShapeResponseObject)result;
            if (resp.isResponse()) {
            	dataShape = resp.getResponse().getShape();
                eventPreview.getIgnoreList().add(getElement());
                eventPreview.getIgnoreList().add(view.getElement());

            }
        }
    };
    
    initList();
    
    if (dataShape == null) {
        ShapeRequest req = new ShapeRequest();
        req.setId(dataShapeId);
        ServiceManager.getService().sendShapeRequest(req, callback);
    }
    
  }

protected void onSelect(DataInstanceModel model) 
{
    DataInstance instance = model.getDataInstance();

    FieldEvent fe = new FieldEvent(this);
    if (fireEvent(Events.BeforeSelect, fe)) 
    {
        focusValue = getValue();
        setValue(model);
        collapse();
        DataInstanceModel v = model;

        /*
        if ((focusValue == null && v != null) || (focusValue != null && !focusValue.equals(v))) 
        {
            fireChangeEvent(focusValue, getValue());
        }
        */
        fireEvent(Events.Select, fe);
    }
    
}

   protected void notifyQueryProvider(DataInstance instance)
   {
       if (returnFullInstances) {
            notifyFullInstance(instance);
       } else {
           _dataInstanceListener.onDataInstance(instance);

       }
   }

    protected void notifyFullInstance(DataInstance instance) {
        AsyncCallback callback = new AsyncCallback() {
            public void onFailure(Throwable result) {
            }

            public void onSuccess(final Object result) {
                DataInstanceResponseObject resp = (DataInstanceResponseObject)result;
                final DataInstanceResponse response = resp.getResponse();
                List<DataInstance> instances = response.getDataInstances();
                _dataInstanceListener.onDataInstance(instances.get(0));
            }
        };
        
        DataInstanceRequest req = new DataInstanceRequest();
        req.setIds(instance.getId());
        req.setDepth(Depth.Deep);
        req.setUser(ServiceManager.getActiveUser());
        ServiceManager.getService().sendDataInstanceRequest(req, callback);
    }

    private void processClearTrigger(ComponentEvent ce) {
        setValue((DataInstanceModel)null);
        findInstances("");

        if (!expanded) {
            onTwinTriggerClick(ce);
        }
    }
    
    private void processExpandTrigger(ComponentEvent ce) {
        if (disabled || isReadOnly()) 
        {
            return;
        }

        if (expanded) 
        {
            collapse();
        } 
        else 
        {
            expand();
        }

        if (GXT.isIE) 
        {
            ignoreNext = true;
        }

        getInputEl().focus();
    }
    
    @Override
    protected void onTwinTriggerClick(ComponentEvent ce) {
    	processExpandTrigger(ce);
    }

    @Override
    protected void onTriggerClick(ComponentEvent ce) 
    {
    	processClearTrigger(ce);
    }

    protected void processMouseSelection() 
    {
        List<DataInstanceModel> selection = binder.getSelection();

        if (selection.size() > 0) 
        {
            DataInstanceModel r = (DataInstanceModel)  selection.get(0);

            if (r != null) 
            {
                processSelection(r.getId(), true, true);
            }
        }

        focus();
    }

    /**
     * There's a bug in the way a twin trigger fields width is calculated, so this adjustment compensates
     */
	protected Size adjustSize(Size size) {
	    if (adjustWidth) {
			size.width -= 9;
	    }
	    return size;
	}
    
    public void processKeyboardSelection(final boolean keepFocus) 
    {
        String text = getRawValue().trim();
     
        if (_searchable)
        {
        	// when the user presses the enter key, there are 2 scenarios that need to
        	// be considered.  the first is that the intent is to accept the currently
        	// selected item in the dropdown, and the second is that the item is meant to
        	// be searched.  we determine which it is by comparing the text in the input
        	// field to the name of the selected item if there is one.  if it's a match,
        	// we use the id, otherwise we use the text.
        	List<DataInstanceModel> selections = binder.getSelection();
        	boolean useId = false;
        	if (selections.size() > 0) {
        		String selText = selections.get(0).getName();
        		if (selText.equals(text)) {
        			text = selections.get(0).getId();
        			useId = true;
        		}
        	}
        	
            processSelection(text, useId, keepFocus);
        }
        else if (text.length() > 0)
        {
            _dataInstanceListener.onStringData(text);
        }
    }
    
    protected void processSelection(final String rawData, final boolean dataIsId, final boolean keepFocus)
    {
        // it's very possible that the user has made a selection that is not the currently
        // selected item in the view.  so go query the server for user's selection.
        AsyncCallback callback = new AsyncCallback()
        {
            public void onFailure(Throwable result) 
            {
            }

            public void onSuccess(final Object result) 
            {
                if (true) //result instanceof DataInstanceResponseObject)
                {
                    DataInstanceResponseObject resp = (DataInstanceResponseObject)result;
                    final DataInstanceResponse response = resp.getResponse();
                    List<DataInstance> instances = response.getDataInstances();

                    // multiple hits, let's see if there's any exact matches in there
                    String name = response.getRequest().getQueryParameters().get(0).fieldValue.toString();

                    List<DataInstance> exactMatches = new ArrayList<DataInstance>();
                    if (dataIsId) {
                    	exactMatches.add(instances.get(0));
                    } else {
                        for (DataInstance instance : instances)
                        {
                            if (name.equalsIgnoreCase(instance.getName()))
                            {
                                exactMatches.add(instance);
                            }
                        }
                    }

                    int ct = exactMatches.size();
                    if (ct == 1) 
                    {
                        // got it!
                    	DataInstance inst = exactMatches.get(0);
                        notifyQueryProvider(inst);
                        
                        // it's possible that the store is empty, which can happen if the applyInput method was called
                        // without the find instances being needed.  if this is the case, we add a new model to the store
                        // so that it can be the selected one in the view
                        DataInstanceModel model;
                        List<DataInstanceModel> models = store.getModels();
                        if (models.size() == 0) {
                        	model = new DataInstanceModel(inst, false, 1, false);
                        	store.add(model);
                        } else {
                            model = store.findModel("id", exactMatches.get(0).getId().getValue());
                        }
                        
                        onSelect(model);
                        clearValidation();
                    }
                    else if (ct == 0)
                    {
                        if (_useValidation)
                        {
                            setValidator(new DataInstanceValidator(""));
                            _dataInstanceListener.onDataInstance(null);
                            validate();
                            //setValidator(null);

                            if (keepFocus)
                            {
                                focus();
                            }
                        }
                        else
                        {
                            if (isExpanded()) 
                            {
                                collapse();
                            } 
                
                            _dataInstanceListener.onStringData(rawData);
                        }
                        
                    }
                    else
                    {
                        setValidator(new DataInstanceValidator(""));
                        validate();
                        //setValidator(null);
                        if (keepFocus)
                        {
                            focus();
                        }
                    }
                }
            }
        };

        DataInstanceRequest req = new DataInstanceRequest();
        req.setCursor(new Cursor(100));
        //req.setDataShape(dataShape);
        req.setClauseOperator(ClauseOperator.MatchAll);
        req.setDepth(Depth.Shallow);
        req.setUser(ServiceManager.getActiveUser());
        if (dataIsId) {
            req.addQueryParameter(dataShape, "id", rawData, Operator.Equals);
        } else {
            req.addQueryParameter(dataShape, "name", rawData, Operator.Contains);
        }
        ServiceManager.getService().sendDataInstanceRequest(req, callback);
                
        if (keepFocus)
        {
            focus();
        }
        
    }

    private void bindStore(DataInstanceStore store, boolean initial)
    {
        if (this.store != null && !initial) 
        {
            this.store.removeStoreListener(storeListener);
            if (store == null) 
            {
                this.store = null;
                if (view != null) 
                {
                    //view.setStore(null);
                }
            }
        }
        
        if (store != null) 
        {
            this.store = store;
            store.addStoreListener(storeListener);
            if (view != null) 
            {
                //view.setStore(store);
                binder = (DataViewBinder<DataInstanceModel>) view.getBinder();
            }
        }
    }

  private void restrict() {
    int w = Math.max(getWidth(), minListWidth);
    list.setWidth(w);

    //int fw = list.el().getFrameWidth("tb");
    //int h = view.el().getHeight() + fw;
    
    //h = Math.min(h, maxHeight - fw);
    int h = maxHeight;
    
    list.setHeight(h);
    list.el().makePositionable(true);
    list.el().alignTo(getElement(), listAlign, new int[] {0, -1});

    int y = list.el().getY();
    int b = y + h;
    int vh = XDOM.getViewportSize().height + XDOM.getBodyScrollTop();
    if (b > vh) {
      y = y - (b - vh) - 5;
      list.el().setTop(y);
    }
    
  }

  private void selectNext() {
    int count = view.getItemCount();
    if (count > 0) {
      int selectedIndex = view.indexOf(selectedItem);
      if (selectedIndex == -1) {
        select(0);
      } else if (selectedIndex < count - 1) {
        select(selectedIndex + 1);
      }
    }
  }

  private void selectPrev() {
    int count = view.getItemCount();
    if (count > 0) {
      int selectedIndex = view.indexOf(selectedItem);
      if (selectedIndex == -1) {
        select(0);
      } else if (selectedIndex != 0) {
        select(selectedIndex - 1);
      }
    }
  }

}
