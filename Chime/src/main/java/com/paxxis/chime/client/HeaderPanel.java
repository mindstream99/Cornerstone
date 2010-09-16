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

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.client.editor.CommunityCreatorWindow;
import com.paxxis.chime.client.editor.InstanceCreatorWindow;
import com.paxxis.chime.client.editor.ShapeCreatorWindow;
import com.paxxis.chime.client.editor.UserCreatorWindow;
import com.paxxis.chime.client.pages.PageManager;
import com.paxxis.chime.client.pages.PageManager.StaticPageType;

/**
 *
 * @author Robert Englander
 */
public class HeaderPanel extends LayoutContainer
{
    Button home;
    Button search;
    Button login;
    Button create;
    Button userProfile;
    SearchComboBox searchBox;
    LabelToolItem quickFindLabel;
    ToolBar bar;

    public HeaderPanel()
    {
    }
    
    @Override
    public void onRender(Element element, int p)
    {
        super.onRender(element, p);
        setStyleAttribute("background", "transparent url(resources/images/slate/panel/white-top-bottom.gif)");
        setStyleAttribute("border", "0pt none");
        setStyleAttribute("padding-left", "3px");
        ColumnLayout layout = new ColumnLayout();  
        setLayout(layout);

        ServiceManager.getService().getBrandingData(
        	new AsyncCallback() {

				@Override
				public void onFailure(Throwable caught) {
					finishRendering(new BrandingData());
				}

				@Override
				public void onSuccess(Object result) {
					finishRendering((BrandingData)result);
				}
        	}
        );
    }

    private void finishRendering(BrandingData branding) {
        StringBuffer sb = new StringBuffer();
        sb.append("<div id='endslice-header' class='endslice-header'><div id='endslice-header-toolbar'></div><div id='endslice-header-title'><img src='resources/images/chime/bell.png' width='16' height='16'/><b>&nbsp;&nbsp;CHIME</b>&nbsp;&nbsp;<i>" + 
        		branding.getHeading() + "</i></div></div>");
        
        HtmlContainer html = new HtmlContainer();
        html.setHtml(sb.toString());
        
        add(html, new ColumnData(0.3)); 

        bar = new ToolBar();
        bar.setAlignment(HorizontalAlignment.RIGHT);
       
        bar.setStyleAttribute("background", "transparent");
        bar.setBorders(false);

        // Home
        home = new Button();
        home.setText("My Home");
        home.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent evt) 
                {
                    PageManager.instance().open(StaticPageType.Navigator, true);
                }
            
            }
        );

        search = new Button();
        search.setText("Search");
        search.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent evt)
                {
                    PageManager.instance().open(StaticPageType.Search, true);
                }

            }
        );

        userProfile = new Button();
        userProfile.setText("My Page");
        userProfile.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent evt)
                {
                    PageManager.instance().openNavigator(true, ServiceManager.getActiveUser());
                    StateManager.instance().pushInactiveToken("detail:" + ServiceManager.getActiveUser().getId());
                }

            }
        );

        create = new Button();
        create.setText("Create");
        create.setMenu(new Menu());

        final ServiceManagerListener listener = new ServiceManagerAdapter()
        {
            public void onLoginResponse(LoginResponseObject resp) 
            {
                updateButtonState();
                updateActionMenu();
            }
            
            public void onLogout()
            {
                updateButtonState();
            }
        };
        
        ServiceManager.addListener(listener);
        
        login = new Button();
        login.setText("Log In");
        login.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent evt) 
                {
                    if (ServiceManager.getActiveUser() == null)
                    {
                        ServiceManager.login();
                    }
                    else
                    {
                        ServiceManager.logout();
                    }
                }
            
            }
        );

        DataInputListener inputListener = new DataInputListener() {

            public void onDataInstance(DataInstance instance) {
                PageManager.instance().openNavigator(true, instance);
                StateManager.instance().pushInactiveToken("detail:" + instance.getId());
            }

            public void onStringData(String text) {
                PageManager.instance().openSearch(text);
            }

        };

        quickFindLabel = new LabelToolItem("Quick Find:");
        bar.add(quickFindLabel);
        quickFindLabel.setStyleAttribute("color", "white");
        searchBox = new SearchComboBox(inputListener, false);
        searchBox.setReturnFullInstances(true);
        searchBox.setWidth(225);
        bar.add(searchBox);

        LabelToolItem label = new LabelToolItem("&nbsp;&nbsp;&nbsp;");
        bar.add(label);
        bar.add(home);
        bar.add(userProfile);
        bar.add(search);
        bar.add(create);
        bar.add(login);

        if (GXT.isIE)
        {
            LayoutContainer cont = new LayoutContainer();
            cont.setLayout(new FilledColumnLayout(HorizontalAlignment.RIGHT));

            LayoutContainer filler = new LayoutContainer();
            filler.setStyleAttribute("backgroundColor", "transparent");

            cont.add(filler, new FilledColumnLayoutData(-1));
            cont.add(bar, new FilledColumnLayoutData(-1));
            cont.setWidth(250);

            //add(cont, "#endslice-header-toolbar");
        }
        else
        {
            //add(bar, "#endslice-header-toolbar");
        	LayoutContainer lc = new LayoutContainer();
        	lc.setLayout(new RowLayout());
        	lc.add(bar, new RowData(1, -1, new Margins(5)));
            add(lc, new ColumnData(0.7)); 
        }

        updateActionMenu();
        updateButtonState();
        
        layout();
    }

    private void updateActionMenu() {

        Menu menu = new Menu();
        
        MenuItem item = new MenuItem("New Data Instance");
        item.addSelectionListener(
            new SelectionListener<MenuEvent>() {
                @Override
                public void componentSelected(MenuEvent evt) {
                    InstanceCreatorWindow editor = new InstanceCreatorWindow();
                    editor.show();
                }
            }
        );
        menu.add(item);

        MenuItem item2 = new MenuItem("New Shape");
        item2.addSelectionListener(
            new SelectionListener<MenuEvent>() {
                @Override
                public void componentSelected(MenuEvent evt) {
                    ShapeCreatorWindow editor = new ShapeCreatorWindow();
                    editor.show();
                }
            }
        );
        menu.add(item2);

        if (ServiceManager.isAdminLoggedIn()) {
            MenuItem item3 = new MenuItem("New User");
            item3.addSelectionListener(
                new SelectionListener<MenuEvent>() {
                    @Override
                    public void componentSelected(MenuEvent evt) {
                        UserCreatorWindow editor = new UserCreatorWindow();
                        editor.show();
                    }
                }
            );
            menu.add(item3);

            MenuItem item4 = new MenuItem("New Community");
            item4.addSelectionListener(
                new SelectionListener<MenuEvent>() {
                    @Override
                    public void componentSelected(MenuEvent evt) {
                        CommunityCreatorWindow editor = new CommunityCreatorWindow();
                        editor.show();
                    }
                }
            );
            menu.add(item4);
        }

        create.setMenu(menu);
    }

    private void updateButtonState() {
        User user = ServiceManager.getActiveUser();
        if (user != null)
        {
            login.setText("Log Out, " + user.getName());
            home.setVisible(true);
            search.setVisible(true);
            login.setVisible(true);
            create.setVisible(true);
            userProfile.setVisible(true);
            searchBox.setVisible(true);
            quickFindLabel.setVisible(true);
        }
        else
        {
            login.setText("Log In");
            login.setVisible(false);
            home.setVisible(false);
            search.setVisible(false);
            create.setVisible(false);
            userProfile.setVisible(false);
            searchBox.setVisible(false);
            quickFindLabel.setVisible(false);
        }
    }

    public int getPanelHeight()
    {
        return Constants.HEADERHEIGHT;
    }
}
