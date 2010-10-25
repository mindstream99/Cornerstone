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

package com.paxxis.chime.client.portal;

import com.extjs.gxt.ui.client.core.Template;
import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.paxxis.chime.client.common.portal.PortletSpecification;
import com.paxxis.chime.client.editor.RichTextEditorWindow;
import com.paxxis.chime.client.editor.RichTextEditorWindow.TextEditorListener;
import com.paxxis.chime.client.widgets.InterceptedHtml;

/**
 *
 * @author Robert Englander
 */
public class RichTextPortlet extends PortletContainer {

    public static class RichTextPortletProxy extends LayoutProxyPortlet {

        InterceptedHtml _html;

        public RichTextPortletProxy(PortletSpecification spec, LayoutProxyListener listener) {
            super(spec, listener);
        }

        @Override
        protected void init() {
            super.init();
            setHeading("Rich Text");

            _html = new InterceptedHtml(false);
            String text =  getSpecification().getProperty("content").toString();
            String txt = text.replaceAll("##QUOTE##", "\"");
            txt = txt.replaceAll("<ul", "<ul class='forceul'");
            txt = txt.replaceAll("<ol", "<ol class='forceol'");
            Params params = new Params();
            params.set("content", txt);
            String content = template.applyTemplate(params);
            _html.setHtml(content);

            getPropertiesContainer().add(_html);
            getPropertiesContainer().applyMask();

            // this prevents the url click from being processed.  basically we just eat it here
            // doesn't seem to lead to the desired behavior, which is why the mask is applied
            // to the properties container above
            /*
            _html.setLinkListener(
            	new InterceptedLinkListener() {
					public boolean onLink(String url) {
						return true;
					}
            	}
            );
            */
        }

        @Override
        protected void onEdit() {
            RichTextEditorWindow w = new RichTextEditorWindow(
                new TextEditorListener() {
                    public void onComplete(String content) {

                        String newText = content.replaceAll("\"", "##QUOTE##");
                        getSpecification().setProperty("content", newText);

                        String txt = content.replaceAll("<ul", "<ul class='forceul'");
                        txt = txt.replaceAll("<ol", "<ol class='forceol'");
                        Params params = new Params();
                        params.set("content", txt);
                        String newContent = template.applyTemplate(params);
                        _html.setHtml(newContent);

                        getListener().onLayoutProxyEdit(getSpecification());
                    }
                }, _html.getHtml()
            );

            w.show();
        }
    }

    private static final String NOLINKTEMPLATE = "<div id='portal'>{content}</div>";

    private static Template template;

    static
    {
        template = new Template(NOLINKTEMPLATE);
        template.compile();
    }

    private InterceptedHtml _html = null;

    public RichTextPortlet(PortletSpecification spec) {
        super(spec, HeaderType.None, true);
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    protected void init() {
    	super.init();
        getBody().setLayout(new RowLayout());

        _html = new InterceptedHtml();
        String text =  getSpecification().getProperty("content").toString();
        String txt = text.replaceAll("##QUOTE##", "\"");
        txt = txt.replaceAll("<ul", "<ul class='forceul'");
        txt = txt.replaceAll("<ol", "<ol class='forceol'");
        Params params = new Params();
        params.set("content", txt);
        String content = template.applyTemplate(params);
        _html.setHtml(content);
        getBody().add(_html, new RowData(1, -1));
    }

}
