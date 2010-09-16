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

import com.extjs.gxt.ui.client.core.Template;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.portal.PortletContainer;

/**
 * 
 * @author Robert Englander
 *
 */
public class GenericDataField extends LayoutContainer {
    private static final String CONTENTTEMPLATE = "<div id='type-field'><span id='type-text-content'>{content}</span></div>";

    private static Template _contentTemplate;

    static
    {
        _contentTemplate = new Template(CONTENTTEMPLATE);
        _contentTemplate.compile();
    }

    private Shape _type;
    private DataField _field;

    private LayoutContainer _contentContainer;
    private InterceptedHtml _content;

    private ToolButton actionsButton;
    private PortletContainer leftSide;

}
