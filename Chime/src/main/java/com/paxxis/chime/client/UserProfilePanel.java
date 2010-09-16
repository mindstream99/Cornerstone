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

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.google.gwt.user.client.Element;

/**
 *
 * @author Robert Englander
 */
public class UserProfilePanel extends ContentPanel {

    public UserProfilePanel() {
        setHeaderVisible(false);
    }

    @Override
    public void onRender(Element el, int p) {
        super.onRender(el, p);
        initialize();
    }

    private void initialize()
    {
        setBorders(false);

        setLayout(new BorderLayout());

    }
}
