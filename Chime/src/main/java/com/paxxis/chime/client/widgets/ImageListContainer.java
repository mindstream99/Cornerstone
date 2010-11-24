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
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.Element;
import com.paxxis.chime.client.InstanceUpdateListener;
import com.paxxis.chime.client.Paginator;
import com.paxxis.chime.client.PaginatorContainer;
import com.paxxis.chime.client.PagingListener;
import com.paxxis.chime.client.StateManager;
import com.paxxis.chime.client.Utils;
import com.paxxis.chime.client.InstanceUpdateListener.Type;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.editor.FieldDataEditor;
import com.paxxis.chime.client.editor.FieldEditListener;
import com.paxxis.chime.client.pages.PageManager;
import com.paxxis.cornerstone.base.Cursor;

/**
 *
 * @author Robert Englander
 */
public class ImageListContainer extends LayoutContainer implements ImageContainer.ImageClickListener {

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

    private List<DataInstance> imageList = EMPTYLIST;
    private HashMap<Integer, ImageContainer> imageIndexMap = new HashMap<Integer, ImageContainer>();
    private HashMap<Integer, DataInstance> instanceIndexMap = new HashMap<Integer, DataInstance>();

    private DataInstance dataInstance;
    private CardPanel mainContainer;
    private Paginator paginator;
    private ContentPanel noImagesPanel;
    private ContentPanel noImageFilePanel;
    private InterceptedHtml noImageFileHtml;
    private ToolButton actionsButton;
    private InstanceUpdateListener _saveListener;
    private FieldEditListener fieldListener;
    private int imageIndex = -1;

    private boolean pendingData = false;
    private DataInstance pendingInstance; 
    private int pendingIdx;

    public ImageListContainer(ToolButton actionsButton, InstanceUpdateListener listener) {
        this.actionsButton = actionsButton;
        _saveListener = listener;
    }

    public void onRender(Element parent, int index) {
    	super.onRender(parent, index);
        init();

        if (pendingData) {
        	pendingData = false;
        	setDataInstance(pendingInstance, pendingIdx);
        }
    }
    
    private void init() {
        setLayout(new RowLayout());

        fieldListener = new FieldEditListener() {
            public void onEdit(DataInstance instance, Shape type, DataField field) {
                sendEdit();
            }
        };

        mainContainer = new CardPanel();
        mainContainer.setHeight(EMPTYHEIGHT);

        noImagesPanel = new ContentPanel();
        noImagesPanel.setHeaderVisible(false);
        noImagesPanel.setLayout(new CenterLayout());
        InterceptedHtml html = new InterceptedHtml();
        html.setHtml("No Images");
        noImagesPanel.add(html);

        noImageFilePanel = new ContentPanel();
        noImageFilePanel.setHeaderVisible(false);
        noImageFilePanel.setLayout(new CenterLayout());
        noImageFileHtml = new InterceptedHtml();
        noImageFilePanel.add(noImageFileHtml);

        add(mainContainer, new RowData(1, -1));

        paginator = new Paginator(
            new PagingListener() {

                public void onNext() {
                    Cursor c = paginator.getCursor();
                    c.prepareNext();
                    showImage(c.getFirst());
                }

                public void onPrevious() {
                    Cursor c = paginator.getCursor();
                    c.preparePrevious();
                    showImage(c.getFirst());
                }

                public void onFirst() {
                    Cursor c = paginator.getCursor();
                    c.prepareFirst();
                    showImage(c.getFirst());
                }

                public void onLast() {
                    Cursor c = paginator.getCursor();
                    c.prepareLast();
                    showImage(c.getFirst());
                }

                public void onRefresh() {
                }

            }
        );

        add(new PaginatorContainer(paginator), new RowData(1, -1));
        setupEditMenu();
    }

    private void sendEdit() {
        if (_saveListener != null) {
            _saveListener.onUpdate(dataInstance, Type.ImageAdd);
        }
    }

    private void setupEditMenu() {
        actionsButton.addSelectionListener(
                new SelectionListener<IconButtonEvent>() {
            @Override
             public void componentSelected(IconButtonEvent ce) {
                FieldDataEditor w = new FieldDataEditor(dataInstance, true, fieldListener);
                w.show();
             }
        });

    }

    private void showImage(int idx) {
        if (idx <= imageList.size()) {
            ImageContainer image = imageIndexMap.get(idx);
            if (image == null) {
                getImage(idx);
            } else {
                placeImage(image, false, idx);
            }

            imageIndex = idx;
        }
    }

    private void getImage(final int idx) {
        DataInstance img = dataInstance.getImages().get(idx);

        // find the image type
        Shape imageType = null;
        for (Shape t : img.getShapes()) {
            if (t.getName().equals("Image")) {
                imageType = t;
                break;
            }
        }

        DataField field = imageType.getField("File ID");
        List<DataFieldValue> vals = img.getFieldValues(imageType, field);
        if (vals.size() == 1) {
            String id = vals.get(0).getValue().toString();
            ImageContainer image = new ImageContainer(ImageListContainer.this, id, true, "#e1e1e1", false,
                    ImageListContainer.this, idx);
            imageIndexMap.put(idx, image);
            instanceIndexMap.put(idx, img);
            placeImage(image, true, idx);
        } else {
            noImageFileHtml.setHtml("No File For " + Utils.toUrl(img.getId(), "This") + " Image");
            mainContainer.setActiveItem(noImageFilePanel);
            noImageFilePanel.layout(true);
            paginator.setCursor(new Cursor(idx, 1, 1, imageList.size(), false));
        }
    }

    private void placeImage(ImageContainer img, boolean add, int idx) {

        if (add) {
            mainContainer.add(img);
        }

        mainContainer.setActiveItem(img);
        paginator.setCursor(new Cursor(idx, 1, 1, imageList.size(), false));
    }

    public void setDataInstance(DataInstance instance, boolean refresh) {
        dataInstance = instance;
    }

    public void setDataInstance(DataInstance instance, int idx) {
    	if (!isRendered()) {
    		pendingData = true;
    		pendingInstance = instance;
    		pendingIdx = idx;
    		return;
    	}

    	dataInstance = instance;
        boolean isEmpty = true;

        mainContainer.removeAll();
        imageIndexMap.clear();
        instanceIndexMap.clear();

        imageList = dataInstance.getImages();
        if (imageList.size() > 0) {
            isEmpty = false;
        }

        mainContainer.add(noImageFilePanel);

        if (isEmpty) {
            mainContainer.setHeight(EMPTYHEIGHT);
            mainContainer.add(noImagesPanel);
            mainContainer.setActiveItem(noImagesPanel);
            paginator.setCursor(new Cursor());
            imageIndex = -1;
        } else {
            mainContainer.setHeight(HEIGHT);
            showImage(idx);
        }

        layout();
    }

    public int getImageIndex() {
        return imageIndex;
    }

    public void onImageClick(int key) {
        DataInstance instance = instanceIndexMap.get(key);
        if (instance != null) {
            StateManager.instance().pushInactiveToken("detail:" + instance.getId());
            PageManager.instance().openNavigator(true, instance);
        }
    }
}
