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

import java.util.List;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.paxxis.chime.client.PaginatedResultsPanel;
import com.paxxis.chime.client.SearchCriteriaSortOrderModel;
import com.paxxis.chime.client.SearchFilterPanel;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.common.SearchCriteria;
import com.paxxis.chime.client.common.SearchFilter;
import com.paxxis.chime.client.common.DataInstanceRequest.ClauseOperator;
import com.paxxis.chime.client.common.portal.PortletSpecification;
import com.paxxis.chime.client.widgets.InterceptedHtml;

/**
 *
 * @author Robert Englander
 */
public class SearchCriteriaPortlet extends PortletContainer 
{
    private SearchCriteria _searchCriteria = null;
    private LayoutContainer criteriaPanel = null;
    private LayoutContainer _resultsPanel = null;
    private PaginatedResultsPanel _resultsContent = null;
    private int _height;
    
    public SearchCriteriaPortlet(PortletSpecification spec, HeaderType type, int height)
    {
        super(spec, type, true);
        _height = height;
    }

    @Override
    public void reset()
    {
        if (_resultsContent != null)
        {
            _resultsContent.reset();
        }
    }

    private void clearResultsPanel()
    {
        if (_resultsPanel != null)
        {
            criteriaPanel.remove(_resultsPanel);
            _resultsPanel = null;
            _resultsContent = null;
            criteriaPanel.layout();
        }
    }
    
    public void setCriteria(final SearchCriteria criteria)
    {
    	Runnable r = new Runnable() {
    		public void run() {
    	        clearResultsPanel();
    	        
    	        _searchCriteria = criteria;
    	        criteriaPanel.removeAll();

    	        LayoutContainer lc = new LayoutContainer();
    	        lc.setStyleAttribute("font", "normal 13px arial, tahoma, sans-serif");
    	        lc.setBorders(false);
    	        InterceptedHtml html = new InterceptedHtml();
    	        String s = "<b>Sort Order:</b> " + SearchCriteriaSortOrderModel.getText(criteria.getSortOrder());
    	        if (criteria.getEnabledFilterCount() > 1)
    	        {
    	            if (criteria.getOperator() == ClauseOperator.MatchAll)
    	            {
    	                s += "<br><b>Match Filters:</b> All";
    	            }
    	            else
    	            {
    	                s += "<br><b>Match Filters:</b> Any";
    	            }
    	        }

    	        html.setHtml(s);
    	        lc.add(html);
    	        criteriaPanel.add(lc, new RowData(1, -1, new Margins(5, 5, 5, 10)));
    	        
    	        List<SearchFilter> filters = criteria.getFilters();
    	        for (SearchFilter filter : filters)
    	        {
    	            SearchFilterPanel panel = new SearchFilterPanel(filter);
    	            criteriaPanel.add(panel, new RowData(1, -1, new Margins(0, 0, 0, 10)));
    	        }

    	        _resultsPanel = new LayoutContainer();
    	        _resultsPanel.setLayout(new FitLayout());
    	        _resultsPanel.setBorders(false);

    	        _resultsContent = new PaginatedResultsPanel(PaginatedResultsPanel.Type.Short);
    	        _resultsPanel.add(_resultsContent);

    	        BorderLayoutData data = new BorderLayoutData(LayoutRegion.CENTER);
    	        data.setMargins(new Margins(0));
    	        data.setCollapsible(false);
    	        data.setSplit(false);
    	        getBody().add(_resultsPanel, data);

    	        _resultsContent.query(_searchCriteria.buildRequest(ServiceManager.getActiveUser(), 100));
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
    	super.init();
        getBody().setHeight(_height);

        BorderLayout layout = new BorderLayout();
        getBody().setLayout(layout);
        
        criteriaPanel = new LayoutContainer();
        criteriaPanel.setStyleAttribute("backgroundColor", "#f1f1f1");
        criteriaPanel.setBorders(true);
        criteriaPanel.setScrollMode(Scroll.AUTOY);
        criteriaPanel.setLayout(new RowLayout());

        BorderLayoutData data = new BorderLayoutData(LayoutRegion.WEST, 275);
        data.setMargins(new Margins(0));
        data.setCollapsible(false);
        data.setSplit(false);

        getBody().add(criteriaPanel, data);
    }
}
