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

import com.extjs.gxt.ui.client.event.ContainerEvent;

/**
 *
 * @author Robert Englander
 */
public class PortalContainerEvent extends ContainerEvent<PortalContainer, PortletContainer> {

  public PortalContainer portal;
  
  public PortletContainer portlet;
  
  public int startColumn;

  public int startRow; 

  public int column;

  public int row;

  public PortalContainerEvent(PortalContainer portal) {
    super(portal);
    this.portal = portal;
  }

  public PortalContainerEvent(PortalContainer portal, PortletContainer portlet, int startColumn, int startRow, int column, int row) {
    super(portal);
    this.portlet = portlet;
    this.startColumn = startColumn;
    this.startRow = startRow;
    this.column = column;
    this.row = row;
  }

  public PortalContainerEvent(PortalContainer portal, PortletContainer portlet) {
    super(portal, portlet);
    this.portal = portal;
    this.portlet = portlet;
  }

}
