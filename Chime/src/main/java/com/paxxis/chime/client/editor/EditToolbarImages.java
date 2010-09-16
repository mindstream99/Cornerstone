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

package com.paxxis.chime.client.editor;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ImageBundle;

/**
 * 
 * @author Robert Englander
 *
 */
@SuppressWarnings("deprecation")
public interface EditToolbarImages extends ImageBundle {
	  @Resource("align-center.gif")
	  AbstractImagePrototype getAlignCenter();

	  @Resource("align-left.gif")
	  AbstractImagePrototype getAlignLeft();

	  @Resource("align-right.gif")
	  AbstractImagePrototype getAlignRight();

	  @Resource("background-color.gif")
	  AbstractImagePrototype getBackgroundColor();

	  @Resource("bold.gif")
	  AbstractImagePrototype getBold();

	  @Resource("bullet-list.gif")
	  AbstractImagePrototype getBulletList();

	  @Resource("decrease-size.gif")
	  AbstractImagePrototype getDecreaseSize();

	  @Resource("edit-mode.gif")
	  AbstractImagePrototype getSource();

	  @Resource("font-color.gif")
	  AbstractImagePrototype getFontColor();

	  @Resource("increase-size.gif")
	  AbstractImagePrototype getIncreaseSize();

	  @Resource("italic.gif")
	  AbstractImagePrototype getItalic();

	  @Resource("link.gif")
	  AbstractImagePrototype getLink();

	  @Resource("link_break.png")
	  AbstractImagePrototype getLinkBreak();

	  @Resource("number-list.gif")
	  AbstractImagePrototype getNumberList();

	  @Resource("underline.gif")
	  AbstractImagePrototype getUnderline();

}
