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

import com.extjs.gxt.ui.client.core.El;
import com.google.gwt.user.client.ui.Frame;

/**
 *
 * @author Robert Englander
 */
public class ImageFrame extends Frame {

    public ImageFrame(String url) {
        setSize("100%", "100%");
        El.fly(getElement()).setStyleAttribute("border", "none");
        setUrl(url);
    }
}
