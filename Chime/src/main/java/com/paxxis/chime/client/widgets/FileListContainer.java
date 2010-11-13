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
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.core.Template;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.CardPanel;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.paxxis.chime.client.InstanceUpdateListener;
import com.paxxis.chime.client.StateManager;
import com.paxxis.chime.client.InstanceUpdateListener.Type;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.editor.FieldDataEditor;
import com.paxxis.chime.client.editor.FieldEditListener;
import com.paxxis.chime.client.pages.PageManager;

/**
 * 
 * @author Robert Englander
 *
 */
public class FileListContainer extends LayoutContainer { 

    private static final int HEIGHT = 175;
    private static final int EMPTYHEIGHT = 60;

    private static final String PAGINATORTEMPLATE = "<div id='endslice-paginator'><div id='{id}' style='float: left; padding-top: 2px; padding-right: 8px;'></div><div id='endslice-paginator-title'>&nbsp;&nbsp;</div></div>";
    private static Template _paginatorTemplate;

    static
    {
        _paginatorTemplate = new Template(PAGINATORTEMPLATE);
        _paginatorTemplate.compile();
    }


    private final List<DataInstance> EMPTYLIST = new ArrayList<DataInstance>();

    private List<DataInstance> fileList = EMPTYLIST;
    private HashMap<Integer, FilesContainer> filesIndexMap = new HashMap<Integer, FilesContainer>();
    private HashMap<Integer, DataInstance> instanceIndexMap = new HashMap<Integer, DataInstance>();

    private DataInstance dataInstance;
    private CardPanel mainContainer;
    //private Paginator paginator;
    private ContentPanel noFilesPanel;
    private ContentPanel dummyPanel;
    private ContentPanel noFileFilePanel;
    private InterceptedHtml noFileFileHtml;
    private ToolButton actionsButton;
    private InstanceUpdateListener _saveListener;
    private FieldEditListener fieldListener;
    private int fileIndex = -1;
    private FilesContainer filesContainer;

    public FileListContainer(ToolButton actionsButton, InstanceUpdateListener listener) {
        init(actionsButton, listener);
    }

    private void init(ToolButton actions, InstanceUpdateListener listener) {
        actionsButton = actions;
        setLayout(new RowLayout());
        _saveListener = listener;

        fieldListener = new FieldEditListener() {
            public void onEdit(DataInstance instance, Shape type, DataField field) {
                sendEdit();
            }
        };


        mainContainer = new CardPanel();
        mainContainer.setHeight(EMPTYHEIGHT);

        noFilesPanel = new ContentPanel();
        noFilesPanel.setHeaderVisible(false);
        noFilesPanel.setLayout(new CenterLayout());
        InterceptedHtml html = new InterceptedHtml();
        html.setHtml("No Files");
        noFilesPanel.add(html);

        dummyPanel = new ContentPanel();
        dummyPanel.setHeaderVisible(false);
        dummyPanel.setLayout(new CenterLayout());
        html = new InterceptedHtml();
        html.setHtml("Dummy");
        dummyPanel.add(html);

        noFileFilePanel = new ContentPanel();
        noFileFilePanel.setHeaderVisible(false);
        noFileFilePanel.setLayout(new CenterLayout());
        noFileFileHtml = new InterceptedHtml();
        noFileFilePanel.add(noFileFileHtml);

        add(mainContainer, new RowData(1, -1));

        HtmlContainer south = new HtmlContainer();

        /*
        paginator = new Paginator(
            new PagingListener() {

                public void onNext() {
                    Cursor c = paginator.getCursor();
                    c.prepareNext();
                    showFile(c.getFirst());
                }

                public void onPrevious() {
                    Cursor c = paginator.getCursor();
                    c.preparePrevious();
                    showFile(c.getFirst());
                }

                public void onFirst() {
                    Cursor c = paginator.getCursor();
                    c.prepareFirst();
                    showFile(c.getFirst());
                }

                public void onLast() {
                    Cursor c = paginator.getCursor();
                    c.prepareLast();
                    showFile(c.getFirst());
                }

                public void onRefresh() {
                }

            }
        );

        Params params = new Params();
        params.set("id", paginator.getUniqueId());

        south.setHtml(_paginatorTemplate.applyTemplate(params));
        if (GXT.isIE)
        {
            LayoutContainer cont = new LayoutContainer();
            cont.setLayout(new FilledColumnLayout(HorizontalAlignment.RIGHT));

            LayoutContainer filler = new LayoutContainer();
            filler.setStyleAttribute("backgroundColor", "transparent");

            cont.add(filler, new FilledColumnLayoutData(-1));
            cont.add(paginator, new FilledColumnLayoutData(-1));
            cont.setWidth(400);

            south.add(cont, "#" + paginator.getUniqueId());
        }
        else
        {
            south.add(paginator, "#" + paginator.getUniqueId());
        }

        paginator.setCursor(new Cursor());
        */
        //add(south, new RowData(1, -1));
        setupEditMenu();

        filesContainer = new FilesContainer();
        //mainContainer.add(dummyPanel);
    }

    private void sendEdit() {
        if (_saveListener != null) {
            _saveListener.onUpdate(dataInstance, Type.FileAdd);
        }
    }

    private void setupEditMenu() {
        actionsButton.addSelectionListener(
                new SelectionListener<IconButtonEvent>() {
            @Override
             public void componentSelected(IconButtonEvent ce) {
                FieldDataEditor w = new FieldDataEditor(dataInstance, false, fieldListener);
                w.show();
             }
        });

    }

    private void showFile(int idx) {
        mainContainer.add(filesContainer);
        mainContainer.setActiveItem(filesContainer);
        filesContainer.showFiles(dataInstance, idx);
    }

    public void setDataInstance(DataInstance instance, boolean refresh) {
        dataInstance = instance;
    }

    public void setDataInstance(DataInstance instance, int idx) {
        dataInstance = instance;
        boolean isEmpty = true;

        mainContainer.removeAll();
        filesIndexMap.clear();
        instanceIndexMap.clear();

        fileList = dataInstance.getFiles();
        if (fileList.size() > 0) {
            isEmpty = false;
        }

        mainContainer.add(noFileFilePanel);

        if (isEmpty) {
            mainContainer.setHeight(EMPTYHEIGHT);
            mainContainer.add(noFilesPanel);
            mainContainer.setActiveItem(noFilesPanel);
            fileIndex = -1;
        } else {
            mainContainer.setHeight(HEIGHT);
            showFile(idx);
        }

        layout();
    }

    public int getFileIndex() {
        return fileIndex;
    }

    public void onImageClick(int key) {
        DataInstance instance = instanceIndexMap.get(key);
        if (instance != null) {
            StateManager.instance().pushInactiveToken("detail:" + instance.getId());
            PageManager.instance().openNavigator(true, instance);
        }
    }
}
