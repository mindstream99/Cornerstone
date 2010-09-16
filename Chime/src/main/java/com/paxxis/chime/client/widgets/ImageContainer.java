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

import com.extjs.gxt.ui.client.event.BoxComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ProgressBar;
import com.extjs.gxt.ui.client.widget.WidgetComponent;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.LoadListener;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author Robert Englander
 */
public class ImageContainer extends LayoutContainer {
    public interface ImageClickListener {
        public void onImageClick(int key);
    }
    
    public interface ImageLoadListener {
    	public void onSuccess();
    	public void onFailure();
    }

    class ImageWidget extends Image {
        private ImageClickListener listener = null;
        private int clickKey = -1;

        public ImageWidget(String url) {
            super(url);
            sinkEvents(Event.ONCLICK | Event.MOUSEEVENTS);
        }

        public void setupListener(ImageClickListener listener, int key) {
            this.listener = listener;
            clickKey = key;
            //setStyleAttribute("cursor", "pointer");
        }

        @Override
        public void onBrowserEvent(final Event evt) {
            if (listener != null) {
                if (evt.getTypeInt() == Event.ONCLICK) {
                    listener.onImageClick(clickKey);
                } else if (evt.getTypeInt() == Event.ONMOUSEOVER) {
                    setStyleAttribute("cursor", "pointer");
                } else if (evt.getTypeInt() == Event.ONMOUSEOUT) {
                    setStyleAttribute("cursor", "default");
                } else {
                    super.onBrowserEvent(evt);
                }
            } else {
                super.onBrowserEvent(evt);
            }
        }
    }

    private ProgressBar progressBar = null;
    private ImageWidget image;
    private WidgetComponent imageComponent;
    private int imageWidth;
    private int imageHeight;
    private float aspectRatio;
    private boolean fitImage;
    private boolean imageLoaded = false;
    private LayoutContainer parentContainer;
    private Html waiting = null;
    private ImageLoadListener imageLoadListener = null;
    
    public ImageContainer(LayoutContainer parent, String imageId, boolean fit, String fitBackground, boolean showProgressBar) {
        this(parent, imageId, fit, fitBackground, showProgressBar, null, -1);
    }

    public ImageContainer(LayoutContainer parent, String imageId, boolean fit, String fitBackground, boolean showProgressBar,
            ImageClickListener listener, int key) {

        parentContainer = parent;

        image = new ImageWidget(GWT.getHostPageBaseURL() + "FileManager?id=" + imageId);
        fitImage = fit;

        if (fit) {
            setLayout(new CenterLayout());
            setStyleAttribute("backgroundColor", fitBackground);
        } else {
            setLayout(new FlowLayout());
        }
        
        imageComponent = new WidgetComponent(image);
        imageComponent.setVisible(false);
        if (listener != null) {
            image.setupListener(listener, key);
        }


        if (showProgressBar) {
            progressBar = new ProgressBar();
            progressBar.updateText("Loading Image...");
            progressBar.setInterval(100);
            add(progressBar);
            progressBar.auto();
        } else {
           waiting = new Html();
           waiting.setHtml("<img src=\"resources/images/default/shared/large-loading.gif\" width=\"32\" height=\"32\"/>");
           add(waiting);
        }

        image.addLoadListener(
            new LoadListener() {

                public void onError(Widget sender) {
                	if (imageLoadListener != null) {
                		imageLoadListener.onFailure();
                	}
                }

                public void onLoad(Widget sender) {
                    if (progressBar != null) {
                        remove(progressBar);
                    } else if (waiting != null) {
                        remove(waiting);
                    }

                    imageHeight = image.getHeight();
                    imageWidth = image.getWidth();
                    aspectRatio = (float)imageHeight / (float)imageWidth;

                    sizeImage();
                    imageComponent.setVisible(true);
                    layout();
                    imageLoaded = true;
                	if (imageLoadListener != null) {
                		imageLoadListener.onSuccess();
                	}
                }

            }
        );

        parentContainer.addListener(Events.Resize,
            new Listener<BoxComponentEvent>() {
                public void handleEvent(BoxComponentEvent evt) {
                    if (imageLoaded) {
                        DeferredCommand.addCommand(
                            new Command() {
                                public void execute() {
                                    sizeImage();
                                    parentContainer.layout();
                                }
                            }
                        );
                    }
                }
            }
        );

        add(imageComponent);
    }

    public void setImageLoadListener(ImageLoadListener listener) {
    	imageLoadListener = listener;
    }
    
    private void sizeByWidth() {
        int w = getWidth();

        // if the image doesn't fit into the layout container,
        // we need to scale it down.
        if (imageWidth > w) {
            int h = (int)((float)w * aspectRatio);
            setHeight(h);
            imageComponent.setSize(String.valueOf(w), String.valueOf(h));
        } else {
            imageComponent.setSize(String.valueOf(imageWidth), String.valueOf(imageHeight));
        }
    }

    private void sizeToFit() {
        int w = getWidth();
        int h = getHeight();

        // if the image doesn't fit into the layout container,
        // we need to scale it down.
        if (imageWidth > w || imageHeight > h) {

            // first determine how much the image needs to be scaled down;
            double wFactor = (double)w / (double)imageWidth;
            double hFactor = (double)h / (double)imageHeight;

            double factor = wFactor;
            if (hFactor < factor) {
                factor = hFactor;
            }

            int iw = (int)(factor * imageWidth);
            int ih = (int)(factor * imageHeight);
            imageComponent.setSize(String.valueOf(iw), String.valueOf(ih));
        } else {
            imageComponent.setSize(String.valueOf(imageWidth), String.valueOf(imageHeight));
        }
    }

    private void sizeImage() {
        if (fitImage) {
            sizeToFit();
        } else {
            sizeByWidth();
        }
    }
}
