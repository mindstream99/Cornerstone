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

import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;

/**
 * MessageBox event type.
 * 
 * <p />
 * Note: For a given event, only the fields which are appropriate will be filled
 * in. The appropriate fields for each event are documented by the event source.
 * 
 * @see MessageBox
 * 
 * @author Robert Englander
 */
public class ChimeMessageBoxEvent extends WindowEvent {

  private Dialog dialog;
  private ChimeMessageBox messageBox;
  private String value;

  public ChimeMessageBoxEvent(ChimeMessageBox messageBox, Dialog window, Button buttonClicked) {
    super(window, buttonClicked);
    this.messageBox = messageBox;
    this.dialog = window;
  }

  /**
   * Returns the source dialog.
   * 
   * @return the source dialog
   */
  public Dialog getDialog() {
    return dialog;
  }

  /**
   * The source message box.
   * 
   * @return the message box
   */
  public ChimeMessageBox getMessageBox() {
    return messageBox;
  }

  /**
   * Returns the value.
   * 
   * @return the value
   */
  public String getValue() {
    return value;
  }

  /**
   * Sets the source dialog.
   * 
   * @param dialog the source dialog
   */
  public void setDialog(Dialog dialog) {
    this.dialog = dialog;
  }

  /**
   * Sets the source message box.
   * 
   * @param messageBox the message box
   */
  public void setMessageBox(ChimeMessageBox messageBox) {
    this.messageBox = messageBox;
  }

  /**
   * Sets the field value. Only applies to prompt and multi-prompt message
   * boxes.
   * 
   * @param value the value
   */
  public void setValue(String value) {
    this.value = value;
  }

}
