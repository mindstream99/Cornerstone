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

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.core.Template;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.DataViewItem;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.paxxis.chime.client.DataInstanceModel;
import com.paxxis.chime.client.DataInstancesView;
import com.paxxis.chime.client.ListTemplates;
import com.paxxis.chime.client.common.DataInstance;

/**
 *
 * @author Robert Englander
 */
public class FilesContainer extends LayoutContainer {

    private static Template _template;

    static
    {
        _template = new Template(ListTemplates.FILEGALLERY);
        _template.compile();
    }

    private LayoutContainer _resultsList;
    private DataInstancesView view;
    private ListStore<DataInstanceModel> store;
    private boolean _includeLinks = true;

    public FilesContainer() {
        init();
    }

    private void init() {
        setLayout(new BorderLayout());

        _resultsList = new LayoutContainer();
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
        add(_resultsList, data);

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
                }
            }
        });

        store = new ListStore<DataInstanceModel>();

        view.setStore(store);

        _resultsList.add(view, new FlowData(new Margins(3)));

        _resultsList.layout();

        view.setBorders(false);
        view.setStyleAttribute("backgroundColor", "white");
        view.setTemplate(_template);
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

    /*
    public int getSelectedIndex() {
        DataViewItem item = view.getSelectedItem();
        if (item != null) {
            DataInstanceModel model = (DataInstanceModel)item.getModel();
            return store.indexOf(model);
        }

        return -1;
    }
    */
    
    public void showFiles(DataInstance instance, final int idx) {
        view.setSelectedItem(null);
        store.removeAll();

        boolean topRule = false;
        List<DataInstanceModel> models = new ArrayList<DataInstanceModel>();
        for (DataInstance file : instance.getFiles()) {
            DataInstanceModel model = new DataInstanceModel(file, true, 200, topRule);
            model.setupDownloadLink();
            models.add(model);
            topRule = true;
        }

        store.add(models);
        store.commitChanges();

        DeferredCommand.addCommand(
            new Command() {
                public void execute() {
                    view.scrollIntoView(view.getItem(idx));
                }
            }
        );
    }
}







