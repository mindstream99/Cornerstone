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

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.paxxis.chime.client.Constants;
import com.paxxis.chime.client.SearchPanel;
import com.paxxis.chime.client.common.NamedSearch;
import com.paxxis.chime.client.common.SearchCriteria;
import com.paxxis.chime.client.widgets.ChimeWindow;

/**
 *
 * @author Robert Englander
 */
public class NamedSearchEditor extends ChimeWindow {

    public interface NamedSearchEditListener {
        public void onEdit(SearchCriteria criteria);
    }

    private NamedSearch instance;
    private NamedSearch origInstance;
    private ButtonBar saveCancelBar;
    private SearchPanel searchPanel;
    private NamedSearchEditListener listener;

    public NamedSearchEditor(NamedSearch instance, NamedSearchEditListener listener) {
        super();

        this.listener = listener;
        this.instance = instance;
        origInstance = instance.copy();
    }

    protected void init() {
        setShim(true);
        setLayout(new RowLayout());

        searchPanel = new SearchPanel(true);
        add(searchPanel, new RowData(1, 1));

        Button saveButton = new Button("Save");
        saveButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent ce) {
                    listener.onEdit(searchPanel.getSearchCriteria());
                    close();
                }
            }
        );

        Button cancelButton = new Button("Cancel");
        cancelButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent ce) {
                    close();
                }
            }
        );

        saveCancelBar = new ButtonBar();
        saveCancelBar.setAlignment(HorizontalAlignment.CENTER);
        saveCancelBar.add(saveButton);
        saveCancelBar.add(cancelButton);
        add(saveCancelBar, new RowData(1, -1));

        setModal(true);
        setHeading("Edit Search Criteria");
        setIconStyle("search-icon");
        setMaximizable(false);

        Widget panel = RootPanel.get().getWidget(0);
        int h = panel.getOffsetHeight() - 160 - Constants.HEADERHEIGHT - Constants.FOOTERHEIGHT;
        int w = panel.getOffsetWidth() - 160;
        setPosition(80, (80 + Constants.HEADERHEIGHT));
        setSize(w, h);

        DeferredCommand.addCommand(
            new Command() {
                public void execute() {
                    searchPanel.setCriteria(instance.getSearchCriteria(), true);
                }
            }
        );

    }
}
