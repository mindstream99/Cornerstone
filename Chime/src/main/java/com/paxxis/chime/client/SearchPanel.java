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
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import com.paxxis.chime.client.common.Cursor;
import com.paxxis.chime.client.common.DataInstanceRequest;
import com.paxxis.chime.client.common.NamedSearch;
import com.paxxis.chime.client.common.SearchCriteria;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.client.common.DataInstanceRequest.SortOrder;


/**
 *
 * @author Robert Englander
 */
public class SearchPanel extends ContentPanel
{
    private ContentPanel resultPanel;
    private PaginatedResultsPanel results;
    private SearchCriteriaPanel _criteriaPanel;
    private boolean advancedEditor;

    public SearchPanel(boolean editor)
    {
        advancedEditor = editor;
        setHeaderVisible(false);
    }
    
    public void onRender(Element el, int p)
    {
        super.onRender(el, p); 
        
        initialize(); 
    }
 
    public void setKeywords(String keywords) {
        _criteriaPanel.setKeywords(keywords);
    }
    
    public void setCriteria(SearchCriteria criteria, boolean showAdvanced)
    {
        _criteriaPanel.setCriteria(criteria, showAdvanced);
    }
    
    public void setSearch(NamedSearch search) {
    	_criteriaPanel.setSearch(search);
    }

    public SearchCriteria getSearchCriteria() {
        return _criteriaPanel.getCriteria();
    }
    public void doScroll()
    {
        results.scrollTo(50);
    }
    
    private void initialize()
    {
        setBorders(false);
        
        setLayout(new BorderLayout()); 
        
        BorderLayoutData data = new BorderLayoutData(LayoutRegion.CENTER);
        data.setMargins(new Margins(2, 2, 2, 2));
        data.setSplit(true);
        
        resultPanel = new ContentPanel();
        resultPanel.setHeaderVisible(false);
        results = new PaginatedResultsPanel(PaginatedResultsPanel.Type.Long);
        resultPanel.setLayout(new FitLayout());
        resultPanel.add(results);
        add(resultPanel, data);
        
        BorderLayoutData data2 = new BorderLayoutData(LayoutRegion.WEST, 320, 150, 450);
        data2.setMargins(new Margins(2, 2, 2, 2));
        data2.setCollapsible(true);
        data2.setSplit(true);
        ContentPanel criteria = new ContentPanel();
        
        criteria.setHeaderVisible(false);
        //criteria.setHeading("Search Criteria");
        criteria.setCollapsible(false); 
        criteria.setBorders(false);
        criteria.setLayout(new FitLayout());

        _criteriaPanel = new SearchCriteriaPanel(
            new SearchProvider()
            {
                public void onSearchRequest(SearchCriteria criteria)
                {
                    if (criteria == null)
                    {
                        results.clear();
                    }
                    else
                    {
                        results.query(criteria.buildRequest(ServiceManager.getActiveUser(), 100));
                    }
                }

                public void onSearchRequest(String keywords) {
                    results.query(buildKeywordRequest(keywords, ServiceManager.getActiveUser(), 100));
                }
            }, advancedEditor
        );
        
        criteria.add(_criteriaPanel);

        add(criteria, data2); 
        
    }

    private DataInstanceRequest buildKeywordRequest(String keywords, User user, int cursorSize) {
        DataInstanceRequest req = new DataInstanceRequest();
        req.setUser(user);
        req.setCursor(new Cursor(cursorSize));
        req.setKeywords(keywords, false);
        req.setSortOrder(SortOrder.ByRelevance);
        return req;
    }
}

