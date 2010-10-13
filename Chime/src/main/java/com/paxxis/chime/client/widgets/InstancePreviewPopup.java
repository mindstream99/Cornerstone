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

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Point;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.paxxis.chime.client.DataInstanceResponseObject;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceRequest;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.DataInstanceRequest.Depth;

/**
 * 
 * @author Robert Englander
 *
 */
public class InstancePreviewPopup extends ChimePopup {

	public enum Location {
		Top,
		Right,
		Bottom, 
		Left,
		None
	}
	
	private DataInstance currentInstance = null;
	private Location popupLocation;
	private boolean centered = true;
	
	public InstancePreviewPopup(Location location, boolean centered) {
		super();
		init(location, centered);
	}

	protected void init(Location location, boolean centered) {
        setShadow(false);
        setAnimate(false);
        setBorders(true);
        setConstrainViewport(true);
        setStyleAttribute("border", "2px solid #003399");
        setStyleAttribute("background", "white");
        setWidth(175);
        setHeight(175);
        setLayout(new FitLayout());
        setAutoHide(false);

        this.centered = centered;
        popupLocation = location;
        setSlideLocation(location);
	}
	
	public void showInstance(Component parent, DataInstance inst) {
		if (inst == null) {
			hide();
		} else {
			if (currentInstance == null || (inst.getId() != currentInstance.getId())) {
				getInstance(parent, inst);
			}
		}
	}
	
	public void hide() {
		currentInstance = null;
		if (isRendered()) {
			super.hide();
		}

		removeAll();
	}
	
	private boolean supportsPreview(DataInstance instance) {
		//return instance.getTypes().get(0).getName().equals("Image");
		return true;
	}
	
	private void getInstance(final Component parent, DataInstance instance) {
		if (!supportsPreview(instance)) {
			hide();
			return;
		}
		
        final AsyncCallback callback = new AsyncCallback()
        {
            public void onFailure(Throwable arg0) {
            }

            public void onSuccess(Object obj)
            {
                DataInstanceResponseObject response = (DataInstanceResponseObject)obj;
                if (response.isResponse()) {
                    List<DataInstance> list = response.getResponse().getDataInstances();
                    if (list.size() == 1) {
                        DataInstance dataInstance = list.get(0);
                		boolean isImage = dataInstance.getShapes().get(0).getName().equals("Image");
                        if (!isImage && dataInstance.getImages().size() == 0) {
                        	hide();
                        } else {
                            render(parent, dataInstance);
                        }
                    } else {
                    	hide();
                    }
                } else {
                	hide();
                }
            }

        };

        DataInstanceRequest req = new DataInstanceRequest();
        req.setDepth(Depth.Deep);
        req.setIds(instance.getId());
        req.setUser(ServiceManager.getActiveUser());
        ServiceManager.getService().sendDataInstanceRequest(req, callback);
	}
	
	private void render(Component parent, DataInstance inst) {
		DataInstance image = null;
		if (inst.getShapes().get(0).getName().equals("Image")) {
			image = inst;
		} else {
			image = inst.getImages().get(0);
		}
		
        Shape type = image.getShapes().get(0);
        DataField field = type.getField("File ID");
        List<DataFieldValue> vals = image.getFieldValues(type, field);
        if (vals.size() == 1) {
            removeAll();
    		ImageContainer ic = new ImageContainer(this, vals.get(0).getValue().toString(), true, "white", false);
    		ic.setImageLoadListener(
    			new ImageContainer.ImageLoadListener() {
    				public void onSuccess() {
    				}
    				
    				public void onFailure() {
    					hide();
    				}
    			}
    		);
    		add(ic, new FitData(new Margins(0)));
    		if (currentInstance == null) {
    			Point pt = getTopLeft(parent);
        		showAt(pt.x, pt.y);
    		}
    		currentInstance = image;
    		layout(true);
        } else {
        	hide();
        }
	}
	
	private Point getTopLeft(Component comp) {
		int left = comp.getAbsoluteLeft();
		int right = left + comp.getOffsetWidth();
		int top = comp.getAbsoluteTop();
		int bottom = top + comp.getOffsetHeight();
		int offset = 0;

		if (centered) {
			switch (popupLocation) {
			case Top:
				offset = (comp.getOffsetWidth() / 2) - 88;
				break;
			case Left:
				offset = (comp.getOffsetHeight() / 2) - 88;
				break;
			case Bottom:
				offset = (comp.getOffsetWidth() / 2) - 88;
				break;
			case Right:
				offset = (comp.getOffsetHeight() / 2) - 88;
				break;
		}
		}
		
		Point result = null;
		switch (popupLocation) {
			case Top:
				offset = (comp.getOffsetWidth() / 2) - 88;
				result = new Point((left + offset), (top - 176));
				break;
			case Left:
				offset = (comp.getOffsetHeight() / 2) - 88;
				result = new Point((left - 176), (top + offset));
				break;
			case Bottom:
				offset = (comp.getOffsetWidth() / 2) - 88;
				result = new Point((left + offset), (bottom + 1));
				break;
			case Right:
				offset = (comp.getOffsetHeight() / 2) - 88;
				result = new Point((right + 1), (top + offset));
				break;
		}
		
		return result;
	}

}
