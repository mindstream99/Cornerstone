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

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.core.Template;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.DataView;
import com.extjs.gxt.ui.client.widget.DataViewItem;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.InfoConfig;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.Element;
import com.paxxis.chime.client.common.Cursor;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceRequest;
import com.paxxis.chime.client.common.DataInstanceResponse;
import com.paxxis.chime.client.common.FindInstancesRequest;
import com.paxxis.chime.client.common.FindInstancesResponse;
import com.paxxis.chime.client.common.RequestMessage;
import com.paxxis.chime.client.widgets.InterceptedHtml;


/**
 *
 * @author Robert Englander
 */
public class PaginatedResultsPanel extends LayoutContainer implements PagingListener, QueryProvider
{
	public interface InstanceHoverListener {
		public void onHover(DataInstance instance);
	}
	
    public interface ResultsListener {
        public void onDataInstanceResponse(DataInstanceResponse response);
    }

    public enum Type
    {
        ListBox,
        ListBoxExtended,
        Short,
        Long
    }
    
    private Template _template;
    
    private Paginator _paginator;
    private LayoutContainer _resultsList;
    private RequestMessage _priorRequest = null;
    private DataInstancesView view;
    private DataInstanceStore store;
    private boolean _includeLinks;
    private Type type;

    private LayoutContainer noResultsContainer;
    private InterceptedHtml noResultsPanel;
    private String noResultsString;
    private LayoutContainer cardPanel;

    private ResultsListener resultsListener = null;
    private InstanceHoverListener hoverListener = null;

    public PaginatedResultsPanel(Type type) {
        this(type, "No Results");
    }

    public PaginatedResultsPanel(Type type, String str) {
        noResultsString = str;
        this.type = type;
        switch (type)
        {
            case ListBox:
                _includeLinks = false;
                _template = new Template(ListTemplates.TINY);
                break;
                
            case Short:
                _includeLinks = true;
                _template = new Template(ListTemplates.SHORT);
                break;

            case ListBoxExtended:
                _includeLinks = false;
                _template = new Template(ListTemplates.SHORT);
                break;

            case Long:
                _includeLinks = true;
                _template = new Template(ListTemplates.LONG);
                break;
        }
        
        _template.compile();

        init();
    }

    public void setInstanceHoverListener(InstanceHoverListener listener) {
    	hoverListener = listener;
    }
    
    public void setResultsListener(ResultsListener listener) {
        resultsListener = listener;
    }

    public void onRender(Element parent, int index) {
        super.onRender(parent, index);
    }

    public void query(DataInstanceRequest request) 
    {
        view.setSelectedItem(null);
        store.query(request);
    }

    public void query(FindInstancesRequest request) 
    {
        view.setSelectedItem(null);
        store.query(request);
    }

    public DataView getView()
    {
        return view;
    }
    
    public void scrollTo(int idx)
    {
        DataViewItem item = view.getItem(idx);
        
        InfoConfig c = new InfoConfig("scrollTo", "" + (item != null));
        Info.display(c);
        
        view.scrollIntoView(item);
    }
    
    public void reset()
    {
        removeAll();
        init();
        
        if (_priorRequest != null)
        {
            if (_priorRequest instanceof DataInstanceRequest)
            {
                DataInstanceRequest req = (DataInstanceRequest)_priorRequest;
                query(req);
            }
            else if (_priorRequest instanceof FindInstancesRequest)
            {
                FindInstancesRequest req = (FindInstancesRequest)_priorRequest;
                query(req);
            }
        }
    }

    private void initListBox()
    {
        view = new DataInstancesView() 
        {
            @Override
            public void scrollIntoView(DataViewItem item) 
            {
                item.el().scrollIntoView(_resultsList.getElement(), false);
            }
        };
        
        view.setSelectOnOver(true);
        view.setBorders(false);
        view.setStyleAttribute("overflow", "hidden");
       
        view.addListener(Events.SelectionChange, new Listener<ComponentEvent>() 
        {
            public void handleEvent(ComponentEvent ce) 
            {
                if (_includeLinks)
                {
                    view.setSelectedItem(null);
                } else if (hoverListener != null) {
                	DataViewItem item = view.getSelectedItem();
                	DataInstance inst = null;
                	if (item != null) {
                		inst = ((DataInstanceModel)item.getModel()).getDataInstance();
                	}
                	hoverListener.onHover(inst);
                }
            }
        });
        
        createStore();
        
        view.setStore(store);
        
        _resultsList.add(view, new RowData(1, -1, new Margins(3)));

        _resultsList.layout();
        
        view.setBorders(false);
        view.setStyleAttribute("backgroundColor", "white");
        view.setTemplate(getTemplate());
        view.setSelectionMode(SelectionMode.SINGLE);

        view.setItemSelector(".x-combo-list-item");
        
        if (_includeLinks)
        {
            view.setSelectStyle("endslice-view-item-selected-trans");
            view.setOverStyle("endslice-view-item-over-trans");
        }
        else
        {
            view.setSelectStyle("endslice-view-item-selected");
            view.setOverStyle("endslice-view-item-over");
        }
        
    }
    
    private void createStore()
    {
        int truncLength = 0;
        if (type == Type.Short) {
            truncLength = 300;
        } else if (type == Type.ListBoxExtended || type == Type.ListBox) {
            truncLength = 100;
        }

        store = new DataInstanceStore(
            new DataStoreUpdateListener()
            {
                public void onDataInstanceResponse(DataInstanceResponse response) 
                {
                    processDataInstanceResponse(response);

                    if (resultsListener != null) {
                        resultsListener.onDataInstanceResponse(response);
                    }
                }

                public void onFindInstancesResponse(FindInstancesResponse response) 
                {
                    _paginator.setEnabled(true);

                    if (response != null)
                    {
                        _paginator.setCursor(response.getCursor());
                        _priorRequest = response.getRequest();
                    }
                    else
                    {
                        _priorRequest = null;
                        _paginator.setCursor(null);
                    }

                    if (response.getDataInstances().size() == 0) {
                        ((CardLayout)cardPanel.getLayout()).setActiveItem(noResultsContainer);
                    } else {
                        ((CardLayout)cardPanel.getLayout()).setActiveItem(_resultsList);
                    }
                    
                    if (hoverListener != null) {
                    	hoverListener.onHover(null);
                    }
                }
            },
            _includeLinks, truncLength
        );
       
    }
    
    private void processDataInstanceResponse(DataInstanceResponse response)
    {
        _paginator.setEnabled(true);

        if (response != null)
        {
            _paginator.setCursor(response.getCursor());
            _priorRequest = response.getRequest();
        }
        else
        {
            _priorRequest = null;
            _paginator.setCursor(null);
        }

        if (response.getDataInstances().size() == 0) {
            ((CardLayout)cardPanel.getLayout()).setActiveItem(noResultsContainer);
        } else {
            ((CardLayout)cardPanel.getLayout()).setActiveItem(_resultsList);
        }
    }
    
    private void init()
    {
        setLayout(new RowLayout());

        _resultsList = new LayoutContainer();
        RowLayout rl = new RowLayout();
        rl.setAdjustForScroll(true);
        _resultsList.setLayout(rl);
        //_resultsList.setBorders(false);
        _resultsList.setStyleName("x-combo-list");
        _resultsList.setStyleAttribute("backgroundColor", "white");
        _resultsList.setScrollMode(Scroll.AUTO);
        
        //ContentPanel p = new ContentPanel();
        //p.setScrollMode(Scroll.AUTO);
        //p.setHeaderVisible(false);
        //p.add(_resultsList);

        BorderLayoutData data = new BorderLayoutData(LayoutRegion.CENTER);
        data.setMargins(new Margins(0));

        cardPanel = new LayoutContainer();
        cardPanel.setLayout(new CardLayout());

        noResultsContainer = new LayoutContainer();
        noResultsContainer.setBorders(true);
        noResultsContainer.setStyleAttribute("backgroundColor", "white");
        noResultsContainer.setLayout(new CenterLayout());
        noResultsPanel = new InterceptedHtml();
        noResultsPanel.setHtml(noResultsString);
        noResultsContainer.add(noResultsPanel);
        cardPanel.add(noResultsContainer);
        cardPanel.add(_resultsList);

        ((CardLayout)cardPanel.getLayout()).setActiveItem(noResultsContainer);

        add(cardPanel, new RowData(1, 1));

        initListBox();

        _paginator = new Paginator(this);
        PaginatorContainer south = new PaginatorContainer(_paginator);

        add(south, new RowData(1, 32));
    }

    public ListStore getStore()
    {
        return store;
    }
    
    public Template getTemplate() 
    {
        return _template;
    }

    public void queryNext()
    {
        if (_paginator.canDoNext())
        {
            onNext();
        }
    }
    
    public void onNext()
    {
        Cursor cursor = _paginator.getCursor();
        cursor.prepareNext();
        requery(cursor);
    }

    public void queryPrevious()
    {
        if (_paginator.canDoPrevious())
        {
            onPrevious();
        }
    }
    
    public void onPrevious()
    {
        Cursor cursor = _paginator.getCursor();
        cursor.preparePrevious();
        requery(cursor);
    }
    
    public void queryFirst()
    {
        if (_paginator.canDoFirst())
        {
            onPrevious();
        }
    }

    public void onFirst()
    {
        Cursor cursor = _paginator.getCursor();
        cursor.prepareFirst();
        requery(cursor);
    }
    
    public void queryLast()
    {
        if (_paginator.canDoLast())
        {
            onPrevious();
        }
    }

    public void onLast()
    {
        Cursor cursor = _paginator.getCursor();
        cursor.prepareLast();
        requery(cursor);
    }
    
    public void onRefresh()
    {
        requery(new Cursor(100));
    }
    
    public void clear()
    {
        _paginator.setCursor(null);
        view.setSelectedItem(null);
        view.removeAll();
    }
    
    public RequestMessage getPriorRequest()
    {
        return _priorRequest;
    }
    
    private void requery(Cursor cursor)
    {
        // remove any list selection
        view.setSelectedItem(null);
        
        if (_priorRequest != null)
        {
            //_paginator.setEnabled(false);
        
            if (_priorRequest instanceof DataInstanceRequest)
            {
                DataInstanceRequest req = (DataInstanceRequest)_priorRequest;
                req.setCursor(cursor);
                query(req);
            }
            else if (_priorRequest instanceof FindInstancesRequest)
            {
                FindInstancesRequest req = (FindInstancesRequest)_priorRequest;
                req.setCursor(cursor);
                query(req);
            }
        }
    }
    
    DataViewItem selectedItem = null;
    
    public void select(int index) 
    {
        //if (view != null) 
        {
            DataViewItem item = view.getItem(index);

            if (item != null) 
            {
                selectedItem = item;
                //view.scrollIntoView(selectedItem);
                //view.setSelectedItem(item);
                view.getSelectionModel().select(index, false);
                
                DataInstanceModel model = (DataInstanceModel)item.getModel();
                DataInstance instance = model.getDataInstance();
                String text = instance.getName();
            }
        }
    }

    public void selectNext() 
    {
        int count = view.getItemCount();
        if (count > 0) 
        {
            int selectedIndex = view.indexOf(selectedItem);

            if (selectedIndex == -1) 
            {
                select(0);
            } 
            else if (selectedIndex < count - 1) 
            {
                select(selectedIndex + 1);
            }
        }
    }

    public void selectPrev() 
    {
        int count = view.getItemCount();
        if (count > 0) 
        {
            int selectedIndex = view.indexOf(selectedItem);

            if (selectedIndex == -1) 
            {
                select(0);
            } 
            else if (selectedIndex != 0) 
            {
                select(selectedIndex - 1);
            }
        }
    }
}










