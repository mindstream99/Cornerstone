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

import com.paxxis.chime.client.portal.PortletContainer.HeaderType;
import com.paxxis.chime.client.portal.SearchCriteriaPortlet;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.paxxis.chime.client.common.NamedSearch;

/**
 *
 * @author Robert Englander
 */
public class NamedSearchDetailPanel extends LayoutContainer {

    private SearchCriteriaPortlet criteriaPanel;

    public NamedSearchDetailPanel(NamedSearch instance, int height) {
        setHeight(300);
        setLayout(new FitLayout());
        criteriaPanel = new SearchCriteriaPortlet(null, HeaderType.None, height);
        criteriaPanel.setCriteria(instance.getSearchCriteria());
        add(criteriaPanel);
    }
}
