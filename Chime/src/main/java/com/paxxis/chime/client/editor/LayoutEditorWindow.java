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

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.TabPanel.TabPosition;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.paxxis.chime.client.Constants;
import com.paxxis.chime.client.common.Dashboard;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.portal.PortalColumn;
import com.paxxis.chime.client.common.portal.PortalTemplate;
import com.paxxis.chime.client.common.portal.PortletSpecification;
import com.paxxis.chime.client.common.portal.PortletSpecification.PortletType;
import com.paxxis.chime.client.editor.RichTextEditorWindow.TextEditorListener;
import com.paxxis.chime.client.portal.LayoutProxyPortlet;
import com.paxxis.chime.client.portal.PortalContainer;
import com.paxxis.chime.client.portal.PortalContainerEvent;
import com.paxxis.chime.client.portal.PortalUtils;
import com.paxxis.chime.client.portal.LayoutProxyPortlet.LayoutProxyListener;
import com.paxxis.chime.client.widgets.ChimeWindow;
import com.paxxis.chime.client.widgets.charts.ChimeChartFactory.ChartType;

/**
 *
 * @author Robert Englander
 */
public class LayoutEditorWindow extends ChimeWindow implements LayoutProxyListener {

    public interface LayoutEditorListener {
        public void onChange(PortalTemplate newTemplate);
    }

    class AddPortletListener extends SelectionListener<MenuEvent> {
        private int columnIdx;
        private PortletSpecification.PortletType portletType;

        public AddPortletListener(int idx, PortletSpecification.PortletType type) {
            columnIdx = idx;
            portletType = type;
        }

        @Override
        public void componentSelected(MenuEvent ce) {
            addPortlet(columnIdx, portletType);
        }
    }

    class DeleteColumnListener extends SelectionListener<MenuEvent> {

        private int columnIdx;

        public DeleteColumnListener(int idx) {
            columnIdx = idx;
        }

        @Override
        public void componentSelected(MenuEvent ce) {
            deleteColumn(columnIdx);
        }

    }

    private Dashboard portalPage;
    private PortalTemplate portalTemplate;
    private ToolBar designToolBar;
    private PortalContainer portalContainer;
    private TabPanel tabPanel;
    private TabItem designModeItem;
    private TabItem previewModeItem;
    private List<Button> columnMenus = new ArrayList<Button>();
    private List<MenuItem> columnDeleteItems = new ArrayList<MenuItem>();
    private ButtonBar saveCancelBar;
    private LayoutContainer cont;
    private PortalContainer previewPortal;
    private LayoutContainer previewContainer;

    private Button addColumnItem;
    private LayoutEditorListener editListener;

    public LayoutEditorWindow(Dashboard page, LayoutEditorListener listener) {
        super();
        portalPage = page;
        portalTemplate = page.getPageTemplate().copy();

        editListener = listener;
    }

    protected void init() {
        setShim(true);
        setLayout(new RowLayout());

        tabPanel = new TabPanel();
        tabPanel.setTabScroll(true);
        tabPanel.setBorders(false);
        tabPanel.setBodyBorder(false);
        tabPanel.setTabPosition(TabPosition.TOP);
        tabPanel.addListener(Events.Select,
            new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent be) {
                    if (tabPanel.getSelectedItem() == previewModeItem) {
                        updatePreview();
                        previewPortal.maskPortlets();
                    }
                }
            }
        );

        designModeItem = new TabItem();
        designModeItem.setLayout(new FitLayout());
        designModeItem.setText("Design");
        designModeItem.setClosable(false);
        tabPanel.add(designModeItem);

        previewModeItem = new TabItem();
        previewModeItem.setLayout(new FitLayout());
        previewModeItem.setText("Preview");
        previewModeItem.setClosable(false);
        tabPanel.add(previewModeItem);

        previewContainer = new LayoutContainer();
        RowLayout rl = new RowLayout();
        rl.setAdjustForScroll(true);
        previewContainer.setLayout(rl);
        previewContainer.setScrollMode(Scroll.AUTO);
        previewModeItem.add(previewContainer, new FitData(5, 5, 5, 10));
        
        add(tabPanel, new RowData(1, 1));

        Button saveButton = new Button("Save");
        saveButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent ce) {
                    doSave();
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
        setHeading("Edit Page Layout - " + portalPage.getName());
        setIconStyle("page-icon");
        setMaximizable(true);
        
        Widget panel = RootPanel.get().getWidget(0);
        int h = panel.getOffsetHeight() - 10 - Constants.HEADERHEIGHT - Constants.FOOTERHEIGHT;
        int w = panel.getOffsetWidth() - 10;
        setPosition(5, (5 + Constants.HEADERHEIGHT));
        setSize(w, h);

        LayoutContainer mainContainer = new LayoutContainer();
        mainContainer.setLayout(new RowLayout());
        designModeItem.add(mainContainer);

        designToolBar = new ToolBar();
        addColumnItem = new Button();
        addColumnItem.setText("Add Column");
        addColumnItem.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent ce) {
                    addColumn();
                }
            }
        );

        designToolBar.add(addColumnItem);
        designToolBar.add(new SeparatorToolItem());

        for (int i = 1; i <= 3; i++) {
            Button colItem = new Button("Column " + i);
            columnMenus.add(colItem);
            designToolBar.add(colItem);
            colItem.setVisible(false);

            Menu menu = new Menu();

            MenuItem item = new MenuItem("Delete");
            columnDeleteItems.add(item);
            item.setData("column", i);
            item.addSelectionListener(new DeleteColumnListener(i));
            menu.add(item);

            menu.add(new SeparatorMenuItem());

            item = new MenuItem("Add Named Search Gadget");
            item.setData("column", i);
            item.addSelectionListener(new AddPortletListener(i, PortletSpecification.PortletType.NamedSearch));
            menu.add(item);

            item = new MenuItem("Add Rich Text Gadget");
            item.setData("column", i);
            item.addSelectionListener(new AddPortletListener(i, PortletSpecification.PortletType.RichText));
            menu.add(item);

            item = new MenuItem("Add Image Renderer Gadget");
            item.setData("column", i);
            item.addSelectionListener(new AddPortletListener(i, PortletSpecification.PortletType.ImageRenderer));
            menu.add(item);

            item = new MenuItem("Add Analytic Gadget");
            item.setData("column", i);
            item.addSelectionListener(new AddPortletListener(i, PortletSpecification.PortletType.Analytic));
            menu.add(item);

            colItem.setMenu(menu);
        }

        designToolBar.add(new SeparatorToolItem());
        Button reset = new Button("Reset");
        designToolBar.add(reset);
        reset.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent ce) {
                    reset();
                }
            }
        );
        
        mainContainer.add(designToolBar, new RowData(1, -1));

        cont = new LayoutContainer();
        cont.setLayout(new FitLayout());
        mainContainer.add(cont, new RowData(1, 1));

        PortalTemplate template = portalPage.getPageTemplate();
        List<PortalColumn> columns = template.getPortalColumns();

        int colIdx = 0;
        portalContainer = new PortalContainer(columns.size(), false);
        for (PortalColumn column : columns) {
            portalContainer.setColumnWidth(colIdx, column.getWidth());

            List<PortletSpecification> specs = column.getPortletSpecifications();
            for (PortletSpecification spec : specs) {
                LayoutProxyPortlet p = LayoutProxyPortlet.create(spec, this);
                portalContainer.addLayoutProxy(p, colIdx);
            }

            colIdx++;
        }

        cont.add(portalContainer);
        tabPanel.setSelection(designModeItem);

        portalContainer.addListener(Events.Drop,
            new Listener<PortalContainerEvent>() {
                public void handleEvent(PortalContainerEvent be) {
                    movePortlet(be.startRow, be.startColumn, be.row, be.column);
                }
            }
        );

        layout();

        // for some reason the resize isn't being handled by column layout containers within
        // a free floating window, so this takes care of it
        addListener(Events.Resize,
            new Listener<WindowEvent>() {
                public void handleEvent(WindowEvent be) {
                    layout();

                    tabPanel.getSelectedItem().layout();
                }
            }
        );

        /*
        tabPanel.addListener(Events.Select,
            new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent evt) {
                    if (tabPanel.getSelectedItem() == previewModeItem) {
                        previewPortal.maskPortlets();
                    }
                }
            }
        );
        */
        
        updateColumnMenus();
        updateButtons();
    }

    private void addPortlet(int idx, PortletType type) {

        switch (type) {
            case NamedSearch:
                addNamedSearchPortlet(idx);
                break;

            case Analytic:
                addAnalyticPortlet(idx);
                break;

            case RichText:
                addRichTextPortlet(idx);
                break;

            case ImageRenderer:
                addImageRendererPortlet(idx);
                break;
        }
    }

    private void addRichTextPortlet(final int idx) {
        RichTextEditorWindow w = new RichTextEditorWindow(
            new TextEditorListener() {
                public void onComplete(String content) {
                    long id = portalTemplate.getNextId(); 
                    PortletSpecification pspec = new PortletSpecification(PortletSpecification.PortletType.RichText, id);
                    pspec.setPinned(false);
                    pspec.setShowHeader(true);

                    // escape the quotes
                    String txt = content.replaceAll("\"", "##QUOTE##");
                    pspec.setProperty("content", txt);

                    portalTemplate.getPortalColumns().get(idx - 1).add(pspec);
                    LayoutProxyPortlet p = LayoutProxyPortlet.create(pspec, LayoutEditorWindow.this);
                    portalContainer.addLayoutProxy(p, idx - 1);
                }
            }
        );

        w.show();
    }

    private void addImageRendererPortlet(final int idx) {
        DataInstanceSelectionWindow w = new DataInstanceSelectionWindow(Shape.IMAGE_ID,
            new DataInstanceSelectionListener() {
                public void onSave(DataInstance instance) {
                    long id = portalTemplate.getNextId();
                    PortletSpecification pspec = new PortletSpecification(PortletSpecification.PortletType.ImageRenderer, id);
                    pspec.setPinned(false);
                    pspec.setShowHeader(true);
                    pspec.setProperty("instanceId", instance.getId().getValue());

                    portalTemplate.getPortalColumns().get(idx - 1).add(pspec);
                    LayoutProxyPortlet p = LayoutProxyPortlet.create(pspec, LayoutEditorWindow.this);
                    portalContainer.addLayoutProxy(p, idx - 1);
                }
            }
        );

        w.show();
    }

    private void addNamedSearchPortlet(final int idx) {

        DataInstanceSelectionWindow w = new DataInstanceSelectionWindow(Shape.NAMEDSEARCH_ID,
            new DataInstanceSelectionListener() {
                public void onSave(DataInstance instance) {
                    long id = portalTemplate.getNextId();
                    PortletSpecification pspec = new PortletSpecification(PortletSpecification.PortletType.NamedSearch, id);
                    pspec.setPinned(false);
                    pspec.setShowHeader(true);
                    pspec.setProperty("instanceId", instance.getId().getValue());
                    pspec.setProperty("height", 250);

                    portalTemplate.getPortalColumns().get(idx - 1).add(pspec);
                    LayoutProxyPortlet p = LayoutProxyPortlet.create(pspec, LayoutEditorWindow.this);
                    portalContainer.addLayoutProxy(p, idx - 1);
                }
            }
        );

        w.show();
    }

    private void addAnalyticPortlet(final int idx) {

        AnalyticPortletEditorWindow w = new AnalyticPortletEditorWindow(Shape.ANALYTIC_ID,
            new AnalyticPortletEditorWindow.AnalyticPortletEditListener() {
                public void onSave(DataInstance analytic, boolean autoUpdate, int updateFreq) {
                    long id = portalTemplate.getNextId();
                    PortletSpecification pspec = new PortletSpecification(PortletSpecification.PortletType.Analytic, id);
                    pspec.setPinned(false);
                    pspec.setShowHeader(true);
                    if (autoUpdate) {
                    	pspec.setProperty("autoUpdate", true);
                    	pspec.setProperty("updateFreq", updateFreq);
                    } else {
                    	pspec.setProperty("autoUpdate", false);
                    }
                    pspec.setProperty("instanceId", analytic.getId().getValue());
                    pspec.setProperty("height", 250);
                    portalTemplate.getPortalColumns().get(idx - 1).add(pspec);
                    LayoutProxyPortlet p = LayoutProxyPortlet.create(pspec, LayoutEditorWindow.this);
                    portalContainer.addLayoutProxy(p, idx - 1);
                }

				public void onSave(DataInstance analytic, boolean autoUpdate, int updateFreq, ChartType chartType,
						String chartTitle, int axisColumn, int valueColumn, Integer minValue, Integer maxValue) {

                    long id = portalTemplate.getNextId();
                    PortletSpecification pspec = new PortletSpecification(PortletSpecification.PortletType.Analytic, id);
                    pspec.setPinned(false);
                    pspec.setShowHeader(true);
                    if (autoUpdate) {
                    	pspec.setProperty("autoUpdate", true);
                    	pspec.setProperty("updateFreq", updateFreq);
                    } else {
                    	pspec.setProperty("autoUpdate", false);
                    }
                    pspec.setProperty("instanceId", analytic.getId().getValue());
                    pspec.setProperty("height", 250);
                	pspec.setProperty("chartType", chartType.toString());
                	pspec.setProperty("chartTitle", chartTitle);
                	pspec.setProperty("chartAxisColumn", axisColumn);
                	pspec.setProperty("chartValueColumn", valueColumn);
                	
                	if (minValue != null) {
                    	pspec.setProperty("minValue", minValue.toString());
                	}

                	if (maxValue != null) {
                    	pspec.setProperty("maxValue", maxValue.toString());
                	}
                    
                	portalTemplate.getPortalColumns().get(idx - 1).add(pspec);
                    LayoutProxyPortlet p = LayoutProxyPortlet.create(pspec, LayoutEditorWindow.this);
                    portalContainer.addLayoutProxy(p, idx - 1);
				}
            }
        );

        w.show();
    }

    public void onLayoutProxyEdit(PortletSpecification spec) {
        List<PortalColumn> columns = portalTemplate.getPortalColumns();
        for (int col = 0; col < columns.size(); col++) {
            List<PortletSpecification> specs = columns.get(col).getPortletSpecifications();
            for (int row = 0; row < specs.size(); row++) {
                if (spec.getId() == specs.get(row).getId()) {
                    specs.set(row, spec);
                    return;
                }
            }
        }
    }
    
    public void onLayoutProxyDelete(LayoutProxyPortlet portlet) {
        List<PortalColumn> columns = portalTemplate.getPortalColumns();
        PortletSpecification spec = portlet.getSpecification();
        for (int col = 0; col < columns.size(); col++) {
            List<PortletSpecification> specs = columns.get(col).getPortletSpecifications();
            for (int row = 0; row < specs.size(); row++) {
                if (spec.getId() == specs.get(row).getId()) {
                    specs.remove(row);
                    portalContainer.remove(portlet, col);
                    updateButtons();
                    updateColumnMenus();
                    return;
                }
            }
        }
    }

    private void deleteColumn(int idx) {
        portalTemplate.getPortalColumns().remove(idx - 1);

        double w = 1.0d / (double)(portalTemplate.getPortalColumns().size());
        for (PortalColumn col : portalTemplate.getPortalColumns()) {
            col.setWidth(w);
        }

        List<PortalColumn> columns = portalTemplate.getPortalColumns();

        int colIdx = 0;
        portalContainer = new PortalContainer(columns.size(), false);
        for (PortalColumn column : columns) {
            portalContainer.setColumnWidth(colIdx, column.getWidth());

            List<PortletSpecification> specs = column.getPortletSpecifications();
            for (PortletSpecification spec : specs) {
                LayoutProxyPortlet p = LayoutProxyPortlet.create(spec, this);
                portalContainer.addLayoutProxy(p, colIdx);
            }

            colIdx++;
        }

        portalContainer.addListener(Events.Drop,
            new Listener<PortalContainerEvent>() {
                public void handleEvent(PortalContainerEvent be) {
                    movePortlet(be.startRow, be.startColumn, be.row, be.column);
                }
            }
        );

        cont.removeAll();
        cont.add(portalContainer);
        cont.layout();

        updateButtons();
        updateColumnMenus();
    }

    private void updateColumnMenus() {

        List<PortalColumn> columns = portalTemplate.getPortalColumns();
        int count = columns.size();
        for (int i = 0; i < 3; i++) {
            boolean visible = i < count;
            columnMenus.get(i).setVisible(visible);
            columnDeleteItems.get(i).setEnabled(count > 1);
        }
    }

    private void updatePreview() {
        previewContainer.removeAll();
        previewPortal = PortalUtils.buildPortal(portalPage, portalTemplate);
        previewContainer.add(previewPortal, new RowData(1, -1));

        DeferredCommand.addCommand(
            new Command() {
                public void execute() {
                    previewContainer.layout();
                }
            }
        );
    }

    private void doSave() {
        editListener.onChange(portalTemplate);
        close();
    }
    
    private void movePortlet(int fromRow, int fromCol, int toRow, int toCol) {

        List<PortalColumn> columns = portalTemplate.getPortalColumns();
        PortalColumn sourceColumn = columns.get(fromCol);
        PortalColumn targetColumn = columns.get(toCol);

        PortletSpecification spec = sourceColumn.getPortletSpecifications().remove(fromRow);
        targetColumn.getPortletSpecifications().add(toRow, spec);
    }

    private void updateButtons() {
        boolean enabled = portalTemplate.getPortalColumns().size() < 3;
        addColumnItem.setEnabled(enabled);
    }

    private void reset() {
        portalTemplate = portalPage.getPageTemplate().copy();

        int colIdx = 0;
        List<PortalColumn> columns = portalTemplate.getPortalColumns();
        portalContainer = new PortalContainer(columns.size(), false);
        for (PortalColumn column : columns) {
            portalContainer.setColumnWidth(colIdx, column.getWidth());

            List<PortletSpecification> specs = column.getPortletSpecifications();
            for (PortletSpecification spec : specs) {
                LayoutProxyPortlet p = LayoutProxyPortlet.create(spec, this);
                portalContainer.addLayoutProxy(p, colIdx);
            }

            colIdx++;
        }

        portalContainer.addListener(Events.Drop,
            new Listener<PortalContainerEvent>() {
                public void handleEvent(PortalContainerEvent be) {
                    movePortlet(be.startRow, be.startColumn, be.row, be.column);
                }
            }
        );

        cont.removeAll();
        cont.add(portalContainer);
        cont.layout();

        if (tabPanel.getSelectedItem() == previewModeItem) {
            updatePreview();
        }

        updateButtons();
        updateColumnMenus();
    }

    private void addColumn() {
        double w = 1.0d / (double)(portalTemplate.getPortalColumns().size() + 1);
        for (PortalColumn col : portalTemplate.getPortalColumns()) {
            col.setWidth(w);
        }

        PortalColumn col = new PortalColumn(w);
        portalTemplate.add(col);

        List<PortalColumn> columns = portalTemplate.getPortalColumns();

        int colIdx = 0;
        portalContainer = new PortalContainer(columns.size(), false);
        for (PortalColumn column : columns) {
            portalContainer.setColumnWidth(colIdx, column.getWidth());

            List<PortletSpecification> specs = column.getPortletSpecifications();
            for (PortletSpecification spec : specs) {
                LayoutProxyPortlet p = LayoutProxyPortlet.create(spec, this);
                portalContainer.addLayoutProxy(p, colIdx);
            }

            colIdx++;
        }

        portalContainer.addListener(Events.Drop,
            new Listener<PortalContainerEvent>() {
                public void handleEvent(PortalContainerEvent be) {
                    movePortlet(be.startRow, be.startColumn, be.row, be.column);
                }
            }
        );

        cont.removeAll();
        cont.add(portalContainer);
        cont.layout();

        updateButtons();
        updateColumnMenus();
    }
}










