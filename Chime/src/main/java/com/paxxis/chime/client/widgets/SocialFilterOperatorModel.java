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

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.paxxis.chime.client.common.DataInstanceRequest.Operator;

/**
 *
 * @author Robert Englander
 */
public class SocialFilterOperatorModel extends BaseModelData
{
    private Operator _operator;

    public SocialFilterOperatorModel()
    {

    }

    public SocialFilterOperatorModel(Operator operator) {

        String txt = "";

        switch (operator) {
            case Reference:
                txt = "Written By";
                break;
            case NotReference:
                txt = "Not Written By";
                break;
            case ContainedIn:
                txt = "Written By User In";
                break;
            case NotContainedIn:
                txt = "Not Written By User In";
                break;
        }

        _operator = operator;
        setDescription(txt);
    }

    public String getDescription()
    {
        return get("description");
    }

    public void setDescription(String text)
    {
        set("description", text);
    }

    public String getOp()
    {
        return get("op");
    }

    public void setOp(String operator)
    {
        set("op", operator);
    }

    public Operator getOperator()
    {
        return _operator;
    }
}

