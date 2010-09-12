/*
 */

package com.paxxis.chime.service;

import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceHelper;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.EditDataInstanceRequest.Operation;
import com.paxxis.chime.client.common.EditPageTemplateRequest;
import com.paxxis.chime.client.common.Dashboard;
import com.paxxis.chime.client.common.RequestMessage;
import com.paxxis.chime.client.common.portal.PortalTemplate;
import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class DashboardHelper implements DataInstanceHelper {

    public void processAfterRead(DataInstance instance, Object obj) {
        Dashboard portalPage = (Dashboard)instance;

        // get the contents of the TemplateDefinition field, which contains
        // a JSON representation of the portal template
        Shape type = instance.getShapes().get(0);
        DataField field = type.getField("TemplateDefinition");
        List<DataFieldValue> values = instance.getFieldValues(type, field);
        if (values.size() == 1)
        {
            String jsonText = values.get(0).getName();

            PortalTemplateHelper helper = new PortalTemplateHelper();
            PortalTemplate template = helper.convert(jsonText);
            portalPage.setPageTemplate(template);
        }
    }

    public void processBeforeWrite(RequestMessage request) {
        if (request instanceof EditPageTemplateRequest) {
            EditPageTemplateRequest req = (EditPageTemplateRequest)request;

            PortalTemplateHelper helper = new PortalTemplateHelper();
            String str = helper.convert(req.getTemplate());

            DataField field = req.getShapes().get(0).getField("TemplateDefinition");
            List<DataFieldValue> values = req.getDataInstance().getFieldValues(req.getShapes().get(0), field);
            if (values.size() == 0) {
                DataFieldValue value = new DataFieldValue();
                value.setName(str);
                req.addFieldData(req.getShapes().get(0), field, value);
                req.setOperation(Operation.AddFieldData);
            } else {
                DataFieldValue value = values.get(0);
                value.setName(str);
                req.addFieldData(req.getShapes().get(0), field, value);
                req.setOperation(Operation.ModifyFieldData);
            }
        }
    }

}
