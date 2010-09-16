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

package com.paxxis.chime.client.detailpopup;

import java.util.List;

import com.extjs.gxt.charts.client.Chart;
import com.extjs.gxt.charts.client.model.ChartModel;
import com.extjs.gxt.charts.client.model.axis.YAxis;
import com.extjs.gxt.charts.client.model.charts.StackedBarChart;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.core.Template;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Popup;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.paxxis.chime.client.DataInstanceModel;
import com.paxxis.chime.client.DataInstanceResponseObject;
import com.paxxis.chime.client.FilledColumnLayout;
import com.paxxis.chime.client.FilledColumnLayoutData;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceRequest;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.client.common.UserProfile;
import com.paxxis.chime.client.common.DataInstanceRequest.Depth;
import com.paxxis.chime.client.widgets.ImageContainer;
import com.paxxis.chime.client.widgets.InterceptedHtml;

/**
 *
 * @author Robert Englander
 */
public class DetailPopupManager {
    private static final String TEMPLATE = "<div id='rob'>"  +
        "<div class='rob-indicator'>{name}</div>" +
        "<div class='rob-rating'>{rating}</div>" + 
        "<div class='rob-msg'><br>{desc}</div></div>";

    private static Template _template;

    static
    {
        _template = new Template(TEMPLATE);
        _template.compile();
    }
    
    class PopupWindow extends Popup {
        private int x;
        private int y;
        public PopupWindow(int x, int y) {
            this.x = x;
            this.y = y;
            sinkEvents(Event.ONCLICK | Event.MOUSEEVENTS);
        }

        public void showNow() {
            showAt(x, y);
        }

        @Override
        public void onBrowserEvent(final Event evt) {
            if (evt.getTypeInt() == Event.ONMOUSEOVER) {
                pendingHide = false;
            } else if (evt.getTypeInt() == Event.ONMOUSEOUT) {
                doHide(600);
            }
        }
    }

    private static final DetailPopupManager INSTANCE = new DetailPopupManager();

    public static DetailPopupManager instance() {
        return INSTANCE;
    }

    private PopupWindow popup;
    private InterceptedHtml htmlPanel;
    private DataInstance dataInstance = null;
    private String detailToken = "";
    private boolean showing = false;
    private boolean pendingHide = false;
    private LayoutContainer imageLayoutContainer;
    private LayoutContainer contextDetailContainer;

    private DetailPopupManager() {
    }

    private void createPopup(int x, int y) {
        popup = new PopupWindow(x, y);
        htmlPanel = new InterceptedHtml(false);
        popup.setShadow(false);
        //popup.setAnimate(true);
        popup.setBorders(true);
        popup.setConstrainViewport(true);
        popup.setStyleAttribute("background", "#fbf0d2");
        popup.setStyleAttribute("border", "2px solid #003399");
        popup.setWidth(350);
        popup.setLayout(new FilledColumnLayout(HorizontalAlignment.LEFT));

        popup.addListener(Events.Hide,
            new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent evt) {
                    showing = false;
                }
            }
        );

        String columnPadding = "5px 5px 5px 5px";
        imageLayoutContainer = new LayoutContainer();
        imageLayoutContainer.setWidth(125);
        imageLayoutContainer.setLayout(new FlowLayout());

        imageLayoutContainer.setStyleAttribute("padding", columnPadding);
        popup.add(imageLayoutContainer, new FilledColumnLayoutData());

        LayoutContainer lc = new LayoutContainer();
        lc.setStyleAttribute("padding", columnPadding);
        lc.setLayout(new RowLayout());
        lc.add(htmlPanel, new RowData(1, -1));

        contextDetailContainer = new LayoutContainer();
        contextDetailContainer.setHeight(125);
        contextDetailContainer.setStyleAttribute("background", "transparent");
        contextDetailContainer.setLayout(new RowLayout());
        lc.add(contextDetailContainer, new RowData(1, -1));

        popup.add(lc, new FilledColumnLayoutData());
    }

    /**
     * Checks to see if there is currently a popup showing for the specified token.
     * If there is, cancel any pending hide.
     * 
     * @param token the token to check
     * @return true if we kept it showing, false otherwise
     */
    public boolean keepShowing(String token) {
        boolean result = false;
        if (detailToken.equals(token)) {
            if (showing) {
                pendingHide = false;
                result = true;
            }
        }

        return result;
    }

    public void hidePopup() {
        doHide(1000);
    }

    public void hideImmediately() {
        pendingHide = false;
        if (popup != null) {
            popup.hide();
            popup = null;
        }

        detailToken = "";
        showing = false;
    }

    private void doHide(int timeout) {
        Timer t = new Timer() {
            @Override
            public void run() {
                if (pendingHide) {
                    hideImmediately();
                }
            }
        };

        pendingHide = true;
        t.schedule(timeout);
    }

    public void show(String token, Element element) {
        int x = element.getAbsoluteLeft();
        int y = 1 + element.getAbsoluteTop() + element.getOffsetHeight();
    	show(token, x, y);
    }
    
    public void show(String token, int x, int y) {
        if (!token.equals(detailToken)) {
            if (showing) {
                hideImmediately();
            }
            
            detailToken = token;
            pendingHide = false;

            createPopup(x, y);
            showing = true;
            
            String[] parts = token.split(":");
            if (parts[0].equals("detail") || parts[0].equals("search"))
            {
                // part1 is the instance id
                String instanceId = parts[1];

                getInstance(InstanceId.create(instanceId));
            }
        }
    }

    private void renderInstance() {
        DataInstanceModel model = new DataInstanceModel(dataInstance, true, 300, false);
        Params params = new Params();
        params.set("rating", model.getRating());
        params.set("name", model.getName());
        params.set("desc", model.getDesc());

        String content = _template.applyTemplate(params);
        htmlPanel.setHtml(content);

        imageLayoutContainer.removeAll();
        DataInstance image = null;
        if (dataInstance.getShapes().get(0).getName().equals("Image")) {
            image = dataInstance;
        } else {
            List<DataInstance> images = dataInstance.getImages();
            if (images.size() > 0) {
                image = images.get(0);
            }
        }

        if (image != null) {
            imageLayoutContainer.setVisible(true);
            imageLayoutContainer.setWidth(125);
            Shape type = image.getShapes().get(0);
            if (type.getName().equals("Image")) {
                DataField field = type.getField("File ID");
                List<DataFieldValue> vals = image.getFieldValues(type, field);
                if (vals.size() > 0) {
                    String id = vals.get(0).getName();
                    ImageContainer imageContainer = new ImageContainer(imageLayoutContainer, String.valueOf(id), false, null, false);
                    imageLayoutContainer.add(imageContainer);
                    imageLayoutContainer.layout();
                }
            }
        } else {
            imageLayoutContainer.setSize(1, 1);
            imageLayoutContainer.setVisible(false);
        }

        renderContextDetail();
        
        popup.layout();
        popup.showNow();
    }

    private void renderContextDetail() {
    	contextDetailContainer.removeAll();

    	if (false) { //dataInstance instanceof User) {
    		Chart chart = getUserVotingChart();
    		contextDetailContainer.add(chart, new RowData(1, 1, new Margins(15, 0, 0, 0)));
        	contextDetailContainer.setVisible(true);
        } else {
        	contextDetailContainer.setVisible(false);
        }
    }
    
    private Chart getUserVotingChart() {
        String url = "./resources/chart/open-flash-chart.swf";        
        Chart chart = new Chart(url);  
        chart.setBorders(false);  
    	
        User user = (User)dataInstance;
        UserProfile profile = user.getProfile();
    	ChartModel cm = new ChartModel("",  
		"font-size: 10px; font-family: Verdana; color:#ffff00;");  
    	cm.setBackgroundColour("#fbf0d2");  
    	
    	long positiveVotesReceived = profile.getPositiveCommentVotesReceived() +
			profile.getPositiveReviewVotesReceived();
    	long negativeVotesReceived = profile.getNegativeCommentVotesReceived() +
			profile.getNegativeReviewVotesReceived();
	
    	long positiveVotesWritten = profile.getPositiveCommentVotesWritten() +
    		profile.getPositiveReviewVotesWritten();
    	long negativeVotesWritten = profile.getNegativeCommentVotesWritten() +
    		profile.getNegativeReviewVotesWritten();

    	StackedBarChart cfg = new StackedBarChart();
    	StackedBarChart.StackValue value = new StackedBarChart.StackValue(positiveVotesReceived, "#0000ff");
    	StackedBarChart.StackValue value2 = new StackedBarChart.StackValue(negativeVotesReceived, "#ff0000");
    	cfg.addStack(value, value2);

    	StackedBarChart.StackValue value3 = new StackedBarChart.StackValue(positiveVotesWritten, "#0000ff");
    	StackedBarChart.StackValue value4 = new StackedBarChart.StackValue(negativeVotesWritten, "#ff0000");
    	cfg.addStack(value3, value4);
    	cfg.setTooltip(null);

    	long max = positiveVotesReceived + negativeVotesReceived;
    	long temp = positiveVotesWritten + negativeVotesWritten;
    	if (temp > max) {
    		max = temp;
    	}
    	
    	YAxis ya = new YAxis();
		ya.setMax(temp);
		ya.setSteps(10);
		ya.setMin(0);
		ya.setGridColour("-1");  
		ya.setColour("#ffff00");

		cm.setYAxis(ya);  
    	
    	cm.addChartConfig(cfg);  
		
		chart.setChartModel(cm);
		
        return chart;
    }
    
    private void getInstance(final InstanceId instanceId)
    {
        final AsyncCallback callback = new AsyncCallback()
        {
            public void onFailure(Throwable arg0) {
                popup = null;
            }

            public void onSuccess(Object obj)
            {
                DataInstanceResponseObject response = (DataInstanceResponseObject)obj;
                if (response.isResponse()) {
                    List<DataInstance> list = response.getResponse().getDataInstances();
                    if (list.size() == 1) {
                        dataInstance = list.get(0);
                        renderInstance();
                    } else {
                        popup = null;
                    }
                } else {
                    popup = null;
                }
            }

        };

        DataInstanceRequest req = new DataInstanceRequest();
        req.setDepth(Depth.Deep);
        req.setIds(instanceId);
        req.setUser(ServiceManager.getActiveUser());
        ServiceManager.getService().sendDataInstanceRequest(req, callback);
    }
}
