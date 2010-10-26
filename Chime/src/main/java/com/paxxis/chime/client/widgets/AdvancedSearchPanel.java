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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BoxComponentEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.paxxis.chime.client.ChimeAsyncCallback;
import com.paxxis.chime.client.ChimeListStore;
import com.paxxis.chime.client.SearchCriteriaSortOrderModel;
import com.paxxis.chime.client.SearchFilterModifyListener;
import com.paxxis.chime.client.SearchFilterPanel;
import com.paxxis.chime.client.SearchProvider;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ShapeResponseObject;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceRequest.ClauseOperator;
import com.paxxis.chime.client.common.DataInstanceRequest.Operator;
import com.paxxis.chime.client.common.DataInstanceRequest.SortOrder;
import com.paxxis.chime.client.common.SearchCriteria;
import com.paxxis.chime.client.common.SearchFilter;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.ShapeRequest;
import com.paxxis.chime.client.common.constants.SearchFieldConstants;
import com.paxxis.chime.client.editor.ComplexSearchFilterEditor;
import com.paxxis.chime.client.editor.NamedSearchSaver;
import com.paxxis.chime.client.editor.SearchFilterEditor;
import com.paxxis.chime.client.editor.SearchFilterEditor.FilterListener;

/**
 *
 * @author Robert Englander
 */
public class AdvancedSearchPanel extends ChimeLayoutContainer
{
    private SearchProvider _searchProvider;
    
    // the filters panel
    private LayoutContainer _filtersPanel;
    
    private ComboBox<SearchCriteriaSortOrderModel> _sortOrderComboBox;
    private ChimeListStore<SearchCriteriaSortOrderModel> _sortOrderStore;
    private List<SearchCriteriaSortOrderModel> _sortOrderList;

    private Button _addFilterButton;   
    private Button _clearFiltersButton;   
    private Button _refreshSearchButton;
    private Button _saveButton;
    private Button _matchAllAnyToggle;
    
    private Html _sortOrderLabel;
    //private SearchFilter _activeFilter;
    private SearchFilterModifyListener _modifyListener;
    
    // the search criteria
    private SearchCriteria _criteria = null;
    private List<SearchFilterPanel> _searchFilterPanels = new ArrayList<SearchFilterPanel>();
    
    private boolean _blockQuery = false;
    
    private Menu filterMenu;
    private boolean advancedEditor;
    
    public AdvancedSearchPanel(SearchProvider provider, boolean advancedEditor)
    {
        _searchProvider = provider;
        this.advancedEditor = advancedEditor;
    }

    public SearchCriteria getCriteria() {
        return _criteria;
    }
    
    public void setCriteria(final SearchCriteria criteria)
    {
    	Runnable r = new Runnable() {
    		public void run() {
    	        _blockQuery = true;
    	        setupCriteria(null, false, false, true);
    	        
    	        // this has to be set AFTER the call to setupCriteria above
    	        _criteria = criteria;

    	        //_dataTypeComboBox.setRawValue(criteria.getDataType().getName());
    	        
    	        int cnt = _sortOrderStore.getCount();
    	        for (int i = 0; i < cnt; i++)
    	        {
    	            SearchCriteriaSortOrderModel model = _sortOrderStore.getAt(i);
    	            if (criteria.getSortOrder() == model.getSortOrder())
    	            {
    	                _sortOrderComboBox.setValue(model);
    	                break;
    	            }
    	        }
    	        
    	        if (_criteria.getOperator() == ClauseOperator.MatchAll)
    	        {
    	            _matchAllAnyToggle.setText("Matching All");
    	        }
    	        else
    	        {
    	            _matchAllAnyToggle.setText("Matching Any");
    	        }
    	        
    	        List<SearchFilter> filters = criteria.getFilters();
    	        for (SearchFilter filter : filters)
    	        {
    	            SearchFilterPanel p = new SearchFilterPanel(filter, _modifyListener);
    	            _filtersPanel.add(p, new RowData(1, -1, new Margins(0, 2, 0, 2)));
    	            _searchFilterPanels.add(p);
    	        }

    	        _filtersPanel.layout();
    	        
    	        _blockQuery = false;
    	        refreshQuery();
    		}
    	};
    	
    	if (isRendered()) {
    		r.run();
    	} else {
    		addPostRenderRunnable(r);
    	}
    }
    
    protected void init()
    {
        _modifyListener = new SearchFilterModifyListener()
        {
            public void onDeleteRequest(SearchFilterPanel panel) 
            {
                SearchFilter filter = panel.getFilter();
                _criteria.removeFilter(filter);
                _filtersPanel.remove(panel);
                updateState();
                _filtersPanel.layout();
                refreshQuery();
                if (_searchFilterPanels.contains(panel))
                {
                    _searchFilterPanels.remove(panel);
                }
            }

            public void onEnableRequest(SearchFilterPanel panel, boolean enable) 
            {
                SearchFilter filter = panel.getFilter();
                filter.setEnabled(enable);
                panel.updateButtonState();
                updateState();
                refreshQuery();
            }
        };

        init2();
    }
    
    private void init2()
    {
        _sortOrderStore = new ChimeListStore<SearchCriteriaSortOrderModel>();
        _sortOrderList = new ArrayList<SearchCriteriaSortOrderModel>();
        _sortOrderList.add(new SearchCriteriaSortOrderModel(SortOrder.ByName));
        _sortOrderList.add(new SearchCriteriaSortOrderModel(SortOrder.ByRating));
        _sortOrderList.add(new SearchCriteriaSortOrderModel(SortOrder.ByMostActive));
        _sortOrderList.add(new SearchCriteriaSortOrderModel(SortOrder.ByMostRecentCreate));
        _sortOrderList.add(new SearchCriteriaSortOrderModel(SortOrder.ByMostRecentEdit));
        
        RowLayout layout = new RowLayout(Orientation.VERTICAL);
        setLayout(layout);
        
        _sortOrderComboBox = new ComboBox<SearchCriteriaSortOrderModel>();
        _sortOrderStore = new ChimeListStore<SearchCriteriaSortOrderModel>();
        _sortOrderStore.add(_sortOrderList);
        _sortOrderComboBox.setStore(_sortOrderStore);
        _sortOrderComboBox.setEditable(false);
        _sortOrderComboBox.setDisplayField("description");
        
        _sortOrderComboBox.addSelectionChangedListener(
            new SelectionChangedListener<SearchCriteriaSortOrderModel>()
            {
                @Override
                public void selectionChanged(final SelectionChangedEvent evt) 
                {
                    SearchCriteriaSortOrderModel model = (SearchCriteriaSortOrderModel)evt.getSelectedItem();

                    if (model != null && _criteria != null)
                    {
                        SortOrder sortOrder = model.getSortOrder();
                        _criteria.setSortOrder(sortOrder);
                        notifyQueryProvider();
                    }
                }
            }
        );
        
        ToolBar bar = new ToolBar();
        _addFilterButton = new Button("Filter");
        _clearFiltersButton = new Button("Clear");
        _refreshSearchButton = new Button("Refresh");
        _saveButton = new Button("Save");
        _matchAllAnyToggle = new Button("Matching All");
        _matchAllAnyToggle.setIconStyle("refresh-icon");
        
        bar.add(_addFilterButton);
        bar.add(_clearFiltersButton);
        bar.add(new SeparatorToolItem());
        bar.add(_matchAllAnyToggle);
        bar.add(new FillToolItem());

        if (!advancedEditor) {
            bar.add(_saveButton);
        }

        bar.add(_refreshSearchButton);
        add(bar, new RowData(1, -1, new Margins(5, 0, 5, 0)));
 
        _sortOrderLabel = new Html("<div id='endslice-form-label'>Sort Order:</div>");
        add(_sortOrderLabel, new RowData(-1, -1, new Margins(5, 5, 2, 5)));
        
        add(_sortOrderComboBox, new RowData(1, -1, new Margins(5, 5, 5, 5)));
        _sortOrderComboBox.setValue(_sortOrderStore.getAt(0));

        
        buildFilterMenu();
        
        _clearFiltersButton.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent arg0) 
                {
                    clearFilters();
                }
            }
        );

        _refreshSearchButton.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent arg0) 
                {
                    refreshQuery();
                }
            }
        );

        _saveButton.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent arg0) 
                {
                    save();
                }
            }
        );

        _matchAllAnyToggle.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent evt) 
                {
                    if (_criteria.getOperator() == ClauseOperator.MatchAll)
                    {
                        _matchAllAnyToggle.setText("Matching Any");
                        _criteria.setOperator(ClauseOperator.MatchAny);
                    }
                    else
                    {
                        _matchAllAnyToggle.setText("Matching All");
                        _criteria.setOperator(ClauseOperator.MatchAll);
                    }
                    
                    refreshQuery();
                }
            }
        );

        LayoutContainer panel = new LayoutContainer();
        panel.setScrollMode(Scroll.AUTO);
        panel.setBorders(false);
        panel.setStyleAttribute("backgroundColor", "transparent");
        
        _filtersPanel = new LayoutContainer();
        _filtersPanel.setLayout(new RowLayout());
        _filtersPanel.setBorders(false);
        _filtersPanel.setStyleAttribute("backgroundColor", "transparent");
        panel.addListener(Events.Resize,
            new Listener<BoxComponentEvent>() {

                public void handleEvent(BoxComponentEvent evt) {
                    _filtersPanel.layout(true);
                }

            }
        );
        
        panel.add(_filtersPanel);
        add(panel, new RowData(1, 1, new Margins(4, 2, 2, 2)));

        updateState();
        _criteria = new SearchCriteria();
        
        if (!advancedEditor) {
            doSearch();
        }
    }

    protected void addFilter(SearchFilter filter) {
    	_criteria.addFilter(filter);
        SearchFilterPanel p = new SearchFilterPanel(filter, _modifyListener);
        _filtersPanel.add(p, new RowData(1, -1, new Margins(0, 2, 0, 2)));
        _searchFilterPanels.add(p);
        _filtersPanel.layout();
        
        doSearch();
    }

    protected void buildFilterMenu() {
    	filterMenu = new Menu();
    	_addFilterButton.setMenu(filterMenu);
    	
    	MenuItem item = new MenuItem(SearchFieldConstants.NAME);
    	filterMenu.add(item);
        item.addSelectionListener(
            new SelectionListener<MenuEvent>() {
                @Override
                public void componentSelected(MenuEvent ce) {
                	SearchFilter filter = new SearchFilter();
                	DataField field = new DataField();
                	field.setName(SearchFieldConstants.NAME);
                    filter.setDataField(field);
                    filter.setValue("");
                    SearchFilterEditor w = new SearchFilterEditor(SearchFilterEditor.Type.Text, filter,
                        new FilterListener() {
                            public void onSave(SearchFilter filter) {
                                addFilter(filter);
                            }
                        }
                    );

                    w.show(_addFilterButton);
                }
            }
        );

    	item = new MenuItem(SearchFieldConstants.DESCRIPTION);
    	filterMenu.add(item);
        item.addSelectionListener(
            new SelectionListener<MenuEvent>() {
                @Override
                public void componentSelected(MenuEvent ce) {
                	SearchFilter filter = new SearchFilter();
                	DataField field = new DataField();
                	field.setName(SearchFieldConstants.DESCRIPTION);
                    filter.setDataField(field);
                    filter.setValue("");
                    SearchFilterEditor w = new SearchFilterEditor(SearchFilterEditor.Type.Text, filter,
                        new FilterListener() {
                            public void onSave(SearchFilter filter) {
                                addFilter(filter);
                            }
                        }
                    );

                    w.show(_addFilterButton);
                }
            }
        );

    	item = new MenuItem(SearchFieldConstants.AVG_RATING);
    	filterMenu.add(item);
        item.addSelectionListener(
            new SelectionListener<MenuEvent>() {
                @Override
                public void componentSelected(MenuEvent ce) {
                	SearchFilter filter = new SearchFilter();
                	DataField field = new DataField();
                	field.setName(SearchFieldConstants.AVG_RATING);
                    filter.setDataField(field);
                    filter.setValue("");
                    SearchFilterEditor w = new SearchFilterEditor(SearchFilterEditor.Type.Number, filter,
                        new FilterListener() {
                            public void onSave(SearchFilter filter) {
                                addFilter(filter);
                            }
                        }
                    );

                    w.show(_addFilterButton);
                }
            }
        );

        item = new MenuItem(SearchFieldConstants.TAG);
    	filterMenu.add(item);
        item.addSelectionListener(
            new SelectionListener<MenuEvent>() {
                @Override
                public void componentSelected(MenuEvent ce) {
                	SearchFilter filter = new SearchFilter();
                	DataField field = new DataField();
                	field.setName(SearchFieldConstants.TAG);
                    filter.setDataField(field);
                    filter.setValue("-1");
                    SearchFilterEditor w = new SearchFilterEditor(SearchFilterEditor.Type.Reference, filter, Shape.TAG_ID,
                        new FilterListener() {
                            public void onSave(SearchFilter filter) {
                                addFilter(filter);
                            }
                        }
                    );

                    w.show(_addFilterButton);
                }
            }
        );

        item = new MenuItem(SearchFieldConstants.USER);
    	filterMenu.add(item);
        item.addSelectionListener(
            new SelectionListener<MenuEvent>() {
                @Override
                public void componentSelected(MenuEvent ce) {
                	SearchFilter filter = new SearchFilter();
                	DataField field = new DataField();
                	field.setName(SearchFieldConstants.USER);
                    filter.setDataField(field);
                    filter.setValue("-1");
                    SearchFilterEditor w = new SearchFilterEditor(SearchFilterEditor.Type.Reference, filter, Shape.USER_ID,
                        new FilterListener() {
                            public void onSave(SearchFilter filter) {
                                addFilter(filter);
                            }
                        }
                    );

                    w.show(_addFilterButton);
                }
            }
        );

        item = new MenuItem(SearchFieldConstants.COMMUNITY);
    	filterMenu.add(item);
        item.addSelectionListener(
            new SelectionListener<MenuEvent>() {
                @Override
                public void componentSelected(MenuEvent ce) {
                	SearchFilter filter = new SearchFilter();
                	DataField field = new DataField();
                	field.setName(SearchFieldConstants.COMMUNITY);
                    filter.setDataField(field);
                    filter.setValue("-1");
                    SearchFilterEditor w = new SearchFilterEditor(SearchFilterEditor.Type.Reference, filter, Shape.COMMUNITY_ID,
                        new FilterListener() {
                            public void onSave(SearchFilter filter) {
                                addFilter(filter);
                            }
                        }
                    );

                    w.show(_addFilterButton);
                }
            }
        );

        item = new MenuItem(SearchFieldConstants.ACTIVITY);
    	filterMenu.add(item);
        item.addSelectionListener(
            new SelectionListener<MenuEvent>() {
                @Override
                public void componentSelected(MenuEvent ce) {
                	SearchFilter filter = new SearchFilter();
                	DataField field = new DataField();
                	field.setName(SearchFieldConstants.ACTIVITY);
                    filter.setDataField(field);
                    filter.setValue("");
                    SearchFilterEditor w = new SearchFilterEditor(SearchFilterEditor.Type.Activity, filter,
                        new FilterListener() {
                            public void onSave(SearchFilter filter) {
                                addFilter(filter);
                            }
                        }
                    );

                    w.show(_addFilterButton);
                }
            }
        );

        item = new MenuItem(SearchFieldConstants.CREATED);
    	filterMenu.add(item);
        item.addSelectionListener(
            new SelectionListener<MenuEvent>() {
                @Override
                public void componentSelected(MenuEvent ce) {
                	SearchFilter filter = new SearchFilter();
                	DataField field = new DataField();
                	field.setName(SearchFieldConstants.CREATED);
                    filter.setDataField(field);
                    filter.setValue("");
                    SearchFilterEditor w = new SearchFilterEditor(SearchFilterEditor.Type.Activity, filter,
                        new FilterListener() {
                            public void onSave(SearchFilter filter) {
                                addFilter(filter);
                            }
                        }
                    );

                    w.show(_addFilterButton);
                }
            }
        );

        item = new MenuItem(SearchFieldConstants.SHAPE);
    	filterMenu.add(item);
        item.addSelectionListener(
            new SelectionListener<MenuEvent>() {
                @Override
                public void componentSelected(MenuEvent ce) {
                	SearchFilter filter = new SearchFilter();
                	DataField field = new DataField();
                	field.setName(SearchFieldConstants.SHAPE);
                    filter.setDataField(field);
                    filter.setValue("-1");
                    SearchFilterEditor w = new SearchFilterEditor(SearchFilterEditor.Type.Reference, filter, Shape.SHAPE_ID,
                        new FilterListener() {
                            public void onSave(SearchFilter filter) {
                                addFilter(filter);
                            }
                        }
                    );

                    w.show(_addFilterButton);
                }
            }
        );

    	item = new MenuItem("Shape : Field");
    	filterMenu.add(item);
        item.addSelectionListener(
            new SelectionListener<MenuEvent>() {
                @Override
                public void componentSelected(MenuEvent ce) {
                	SearchFilter filter = new SearchFilter();
                	DataField field = new DataField();
                	field.setName(SearchFieldConstants.SHAPE);
                    filter.setDataField(field);
                    filter.setValue("-1");
                    ComplexSearchFilterEditor w = new ComplexSearchFilterEditor(
                        new FilterListener() {
                            public void onSave(SearchFilter filter) {
                                addFilter(filter);
                            }
                        }
                    );

                    w.show(_addFilterButton);
                }
            }
        );
    }
    
    protected void save()
    {
        NamedSearchSaver saver = new NamedSearchSaver(null, _criteria);
        saver.show();
    }
    
    public void refreshQuery()
    {
        notifyQueryProvider();
        updateState();
    }
    
    protected void reset()
    {
        _criteria = null;

        clearFiltersPanel();
        updateState();
        notifyQueryProvider();
    }
    
    protected void clearFilters()
    {
        _criteria.getFilters().clear();
        _matchAllAnyToggle.setText("Matching All");
        _criteria.setOperator(ClauseOperator.MatchAll);
        
        notifyQueryProvider();
        updateState();
        clearFiltersPanel();
    }
    
    protected void clearFiltersPanel()
    {
        _filtersPanel.removeAll();
        _searchFilterPanels.clear();
        
        _filtersPanel.layout();
    }
    
    protected void doSearch()
    {
        notifyQueryProvider();
        updateState();
    }
    
    protected void updateState()
    {
        if (_criteria == null)
        {
            _clearFiltersButton.setEnabled(false);
            _refreshSearchButton.setEnabled(false);
            _matchAllAnyToggle.setVisible(false);
        }
        else
        {
            _clearFiltersButton.setEnabled(_criteria.getFilters().size() > 0);
            _refreshSearchButton.setEnabled(true);
            _matchAllAnyToggle.setVisible(_criteria.getEnabledFilterCount() >= 2);
        }
    }
    
    protected void initCriteria(final DataInstance shape)
    {
        final ChimeAsyncCallback<ShapeResponseObject> callback = new ChimeAsyncCallback<ShapeResponseObject>() {
            public void onSuccess(ShapeResponseObject resp) {
                Shape shape = resp.getResponse().getShape();

                // we don't want Internals
                if (!shape.isPrimitive()) {
                    _criteria = new SearchCriteria();
                    
                    // is this the permanent way to deal with this?
                    SearchFilter filter = new SearchFilter();
                    filter.setValue(shape.getId(), shape.getName());
                    filter.setEnabled(true);
                    filter.setOperator(Operator.Reference);
                    DataField f = new DataField();
                    f.setName(SearchFieldConstants.SHAPE);
                    filter.setDataField(f);
                    //_activeFilter = filter;

                    setupCriteria(shape, true, true, true);
                }
            }
        };

        // we've been handed a shallow instance, so we need to go get the
        // full shape instance
        ShapeRequest request = new ShapeRequest();
        request.setId(shape.getId());

        ServiceManager.getService().sendShapeRequest(request, callback);
    }

    protected void setupCriteria(Shape type, boolean newCriteria, boolean doQuery, boolean clearFiltersPanel)
    {
    	if (clearFiltersPanel) {
            clearFiltersPanel();
    	}
        
        if (newCriteria)
        {
            _criteria = new SearchCriteria();
        }
        
        _blockQuery = true;
        //_dataFieldComboBox.setValue(_dataFieldStore.getAt(0));
        _sortOrderComboBox.setValue(_sortOrderStore.getAt(0));
        _blockQuery = false;

        updateState();
        
        if (doQuery)
        {
            notifyQueryProvider();
        }
    }
    
    protected void notifyQueryProvider()
    {
        if (!_blockQuery)
        {
            _searchProvider.onSearchRequest(_criteria);
        }
    }
}
