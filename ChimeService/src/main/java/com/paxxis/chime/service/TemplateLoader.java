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

package com.paxxis.chime.service;

import com.paxxis.chime.data.PortalTemplateUtils;
import com.paxxis.chime.client.common.portal.PortalColumn;
import com.paxxis.chime.client.common.portal.PortalTemplate;
import com.paxxis.chime.client.common.portal.PortletSpecification;
import com.paxxis.chime.client.common.portal.PortletSpecification.PortletType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Robert Englander
 */
public class TemplateLoader {
    long nextSpecId = 1;
    long nextTemplateId = 1;

    public void setTemplateFiles(List<String> values) {
        for (String name : values) {
            parse(name);
        }
    }

    private void parse(String fileName) {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(fileName);
            Element root = doc.getDocumentElement();

            // step 1:  read all of the specifications
            HashMap<String, PortalTemplate> specMap = new HashMap<String, PortalTemplate>();
            NodeList specificationList = root.getElementsByTagName("specifications");

            int count = specificationList.getLength();
            if (count == 1) {
                Node node = specificationList.item(0);
                NodeList children = node.getChildNodes();
                count = children.getLength();
                for (int i = 0; i < count; i++) {
                    Node specificationNode = children.item(i);
                    if (specificationNode.getNodeName().equals("specification")) {
                        String templName = specificationNode.getAttributes().getNamedItem("name").getNodeValue();
                        PortalTemplate portalTemplate = new PortalTemplate();
                        portalTemplate.setId("" + nextTemplateId++);

                        NodeList colList = specificationNode.getChildNodes();
                        int colCount = colList.getLength();
                        for (int j = 0; j < colCount; j++) {
                            Node colNode = colList.item(j);
                            if (colNode.getNodeName().equals("column")) {
                                PortalColumn portalColumn = new PortalColumn();
                                String width = colNode.getAttributes().getNamedItem("width").getNodeValue();
                                portalTemplate.add(portalColumn);

                                portalColumn.setWidth(Double.parseDouble(width));

                                NodeList portletNodes = colNode.getChildNodes();
                                int portletCount = portletNodes.getLength();
                                for (int k = 0; k < portletCount; k++) {
                                    Node portletNode = portletNodes.item(k);
                                    if (portletNode.getNodeName().equals("portlet")) {
                                        String type = portletNode.getAttributes().getNamedItem("type").getNodeValue();
                                        PortletType ptype = PortletType.valueOf(type);
                                        PortletSpecification spec = new PortletSpecification(ptype, nextSpecId++);

                                        NamedNodeMap map = portletNode.getAttributes();
                                        int acnt = map.getLength();
                                        for (int a = 0; a < acnt; a++) {
                                            Node n = map.item(a);
                                            String aname = n.getNodeName();
                                            String aval = n.getNodeValue();
                                            spec.setProperty(aname, aval);
                                        }

                                        // excluded fields?
                                        ArrayList<String> exclusions = new ArrayList<String>();
                                        NodeList kids = portletNode.getChildNodes();
                                        int kidCnt = kids.getLength();
                                        for (int l = 0; l < kidCnt; l++) {
                                            Node kidNode = kids.item(l);
                                            if (kidNode.getNodeName().equals("exclusion")) {
                                                String field = kidNode.getAttributes().getNamedItem("name").getNodeValue();
                                                exclusions.add(field);
                                            }
                                        }

                                        spec.setProperty("exclusions", exclusions);
                                        
                                        portalColumn.add(spec);
                                        int xx = 1;
                                    }
                                }
                            }
                        }

                        specMap.put(templName, portalTemplate);
                    }
                }
            }

            // step 2: run through the specifications, looking for any that have a ref property, and replace the
            // name with the respective template
            List<PortalTemplate> tList = new ArrayList<PortalTemplate>(specMap.values());
            for (PortalTemplate t : tList) {
                List<PortalColumn> cols = t.getPortalColumns();
                for (PortalColumn col : cols) {
                    List<PortletSpecification> specs = col.getPortletSpecifications();
                    for (PortletSpecification spec : specs) {
                        Object obj = spec.getProperty("ref");
                        if (obj != null) {
                            String ref = obj.toString();
                            PortalTemplate template = specMap.get(ref);
                            spec.setProperty("ref", template);
                        }
                    }
                }
            }

            // step 3:
            NodeList mappingList = root.getElementsByTagName("mappings");
            count = mappingList.getLength();
            if (count == 1) {
                Node node = mappingList.item(0);
                NodeList children = node.getChildNodes();
                count = children.getLength();
                for (int i = 0; i < count; i++) {
                    Node mappingNode = children.item(i);
                    if (mappingNode.getNodeName().equals("mapping")) {
                        Node autoUpdateNode = mappingNode.getAttributes().getNamedItem("autoUpdate");
                        String specName = mappingNode.getAttributes().getNamedItem("specification").getNodeValue();
                        boolean autoUpdate = false;
                        if (autoUpdateNode != null) {
                            autoUpdate = autoUpdateNode.getNodeValue().equals("true");
                        }

                        PortalTemplate portalTemplate = specMap.get(specName).copy();
                        portalTemplate.setAutoUpdate(autoUpdate);

                        Node snode = mappingNode.getAttributes().getNamedItem("shape");
                        Node inode = mappingNode.getAttributes().getNamedItem("instance");
                        if (inode != null) {
                            String id = inode.getNodeValue();
                            PortalTemplateUtils.addLocalInstanceTemplate(id, portalTemplate);
                        } else if (snode != null) {
                            String shapeId = snode.getNodeValue();
                            PortalTemplateUtils.addLocalShapeTemplate(shapeId, portalTemplate);
                        } else {
                            Logger.getLogger(PortalTemplateUtils.class.getCanonicalName()).log(
                                    Level.WARNING, "A template mapping in {0} has no shape or instance attribute.", fileName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(PortalTemplateUtils.class.getCanonicalName()).warning(e.getLocalizedMessage());
        }
    }
}
