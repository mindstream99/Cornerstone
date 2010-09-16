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

import java.util.List;

import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.User;

/**
 *
 * @author Robert Englander
 */
public class FavoritePanel extends ChimeLayoutContainer {
    public interface FavoritePanelListener {
        public void onFavorite(boolean makeFavorite);
    }

    //private InterceptedHtml html;
    private IconButton favButton;
    private DataInstance dataInstance = null;
    private FavoritePanelListener listener;
    private boolean isFavorite;

    public FavoritePanel(FavoritePanelListener listener) {
        this.listener = listener;
    }

    protected void init() {
        setLayout(new RowLayout());
        setBorders(false);
        setStyleAttribute("backgroundColor", "transparent");

        favButton = new IconButton("favoritesAdd-icon");
        favButton.setSize(24, 24);
        LayoutContainer lc = new LayoutContainer();
        lc.setLayout(new CenterLayout());
        lc.add(favButton);
        lc.setHeight(36);
        add(lc, new RowData(1, -1));

        favButton.addSelectionListener(
            new SelectionListener<IconButtonEvent>() {
                @Override
                public void componentSelected(IconButtonEvent ce) {
                    if (dataInstance != null) {
                        FavoriteWindow w = new FavoriteWindow(dataInstance, isFavorite,
                            new FavoriteWindow.FavoriteChangeListener() {
                                public void onFavoriteChange(boolean makeFavorite) {
                                    listener.onFavorite(makeFavorite);
                                }
                            }
                        );
                        int x = favButton.getAbsoluteLeft();
                        int y = favButton.getAbsoluteTop() + favButton.getOffsetHeight() + 1;
                        w.showAt(x, y);
                    }
                }
            }
        );
    }

    public void setDataInstance(final DataInstance instance) {
    	Runnable r = new Runnable() {
    		public void run() {
    	        dataInstance = instance;
    	        isFavorite = false;

    	        if (ServiceManager.isLoggedIn()) {
    	            User user = ServiceManager.getActiveUser();
    	            List<DataInstance> pages = user.getFavorites();
    	            for (DataInstance page : pages) {
    	                if (page.getId().equals(instance.getId())) {
    	                    isFavorite = true;
    	                    break;
    	                }
    	            }
    	        }

    	        if (isFavorite) {
    	            favButton.changeStyle("favorite-icon");
    	        } else {
    	            favButton.changeStyle("favoritesAdd-icon");
    	        }

    	        favButton.setVisible(true);

    	        layout();
    		}
    	};
    	
    	if (isRendered()) {
    		r.run();
    	} else {
    		addPostRenderRunnable(r);
    	}
    }
}
