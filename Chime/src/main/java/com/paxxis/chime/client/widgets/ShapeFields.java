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
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.paxxis.chime.client.InstanceUpdateListener;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.Shape;

/**
 *
 * @author Robert Englander
 */
public class ShapeFields extends LayoutContainer
{
    private Shape _type = null;
    private InstanceUpdateListener _saveListener;

    public ShapeFields(InstanceUpdateListener saveListener)
    {
        init(saveListener);
    }

    private void init(InstanceUpdateListener saveListener)
    {
        _saveListener = saveListener;

        setLayout(new RowLayout());
        setStyleAttribute("backgroundColor", "white");
        setBorders(false);
    }

    public void setDataInstance(Shape type) {
        update(type, _type);
        _type = type;
    }

    private void update(Shape newInstance, Shape oldInstance) {

        // we want to keep the fields that were also in the old instance; remove those
        // that don't exist anymore; and add the new ones.

        boolean startOver = true; //oldInstance == null || newInstance.getId() != oldInstance.getId();
        if (startOver) {
            removeAll(); 

            boolean isFirst = true;
            boolean shade = false;
            List<DataField> fields = newInstance.getFields();
            for (DataField field : fields) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    add(new Html("<hr COLOR=\"#f1f1f1\"/>"), new RowData(1, -1, new Margins(2, 0, 2, 0)));
                }

                ShapeField f = new ShapeField(newInstance, field, _saveListener);
                if (shade) {
                    f.setStyleAttribute("backgroundColor", "#f1f1f1");
                }
                
                add(f, new RowData(1, -1, new Margins(2, 0, 2, 0)));
                shade = !shade;
            }
        } else {
            //for (Component comp : getItems()) {
            //    DataInstanceField f = (DataInstanceField)comp;
            //    f.updateDataInstance(newInstance);
            //}
        }


        layout();
    }
}
