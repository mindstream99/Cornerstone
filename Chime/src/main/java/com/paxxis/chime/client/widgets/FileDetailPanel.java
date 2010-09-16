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

import com.extjs.gxt.ui.client.core.Template;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.google.gwt.core.client.GWT;
import com.paxxis.chime.client.Utils;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.Shape;

/**
 *
 * @author Robert Englander
 */
public class FileDetailPanel extends LayoutContainer {

    private static final String TEMPLATE = "<div id='review'>"  +
        "<span id='review-detail-msg'><b>MIME Type:</b>&nbsp;{mimeType}&nbsp;&nbsp;<b>File Size:</b>&nbsp;{size} Bytes&nbsp;&nbsp;{downloadLink}</span></div>";

    private static Template template;

    static
    {
        template = new Template(TEMPLATE);
        template.compile();
    }

    private DataInstance dataInstance;
    private InterceptedHtml html;

    public FileDetailPanel(DataInstance instance) {
        super();
        dataInstance = instance;
        init();
        update();
    }

    private void init() {
        LayoutContainer cont = new LayoutContainer();

        html = new InterceptedHtml();
        cont.add(html, new FlowData(0, 5, 0, 5));

        add(cont, new FlowData(new Margins(5)));
    }

    public void update()
    {
        Params params = new Params();
        params.set("mimeType", getMimeType());
        params.set("size", getFileSize());
        params.set("downloadLink", getDownloadLink());

        String content = template.applyTemplate(params);
        html.setHtml(content);
    }

    private String getMimeType() {
        Shape type = dataInstance.getShapes().get(0);
        DataField field = type.getField("MIME Type");
        List<DataFieldValue> values = dataInstance.getFieldValues(type, field);

        String result = "Unknown";
        if (values.size() > 0) {
            result = values.get(0).getName();
        }

        return result;
    }

    private String getFileSize() {
        Shape type = dataInstance.getShapes().get(0);
        DataField field = type.getField("Size");
        List<DataFieldValue> values = dataInstance.getFieldValues(type, field);

        String result = "Unknown";
        if (values.size() > 0) {
            result = values.get(0).getName();

            try {
                double sz = Double.valueOf(result);
                result = "" + (long)sz;
            } catch (Exception e) {
                result = e.getMessage();
            }
        }

        return result;
    }

    private String getDownloadLink() {
        Shape type = dataInstance.getShapes().get(0);
        DataField field = type.getField("File ID");
        List<DataFieldValue> values = dataInstance.getFieldValues(type, field);

        String result = "";
        if (values.size() > 0) {
            String fileName = dataInstance.getName();
            if (-1 == fileName.indexOf(".")) {
                List<DataFieldValue> vals = dataInstance.getFieldValues(type, type.getField("Extension"));
                if (vals.size() > 0) {
                    fileName += ("." + vals.get(0).getName());
                }
            }

            String id = values.get(0).getName();
            String link = GWT.getHostPageBaseURL().trim();
            if (link.endsWith("/")) {
                link = link.substring(0, link.length() - 1);
            }
            
            link += "/FileManager/" + fileName + "?id=" + id;
            result = Utils.toExternalUrl(link, "Download...");
        }

        return result;
    }
}
