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

package com.paxxis.chime.client;

import com.paxxis.chime.client.common.DataInstanceRequest.Operator;
import com.extjs.gxt.ui.client.data.BaseTreeModel;
import java.io.Serializable;

/**
 *
 * @author Robert Englander
 */
public class SearchCriteriaOperatorModel extends BaseTreeModel implements Serializable
{
    private Operator _operator;

    public SearchCriteriaOperatorModel()
    {

    }

    public SearchCriteriaOperatorModel(Operator operator)
    {
        _operator = operator;

        switch (_operator)
        {
            case Contains:
                setDescription("Contains Word or Phrase");
                break;
            case Equals:
                setDescription("Equals");
                break;
            case LessThan:
                setDescription("Less Than");
                break;
            case LessThanOrEquals:
                setDescription("Less or Equal to");
                break;
            case GreaterThan:
                setDescription("Greater Than");
                break;
            case GreaterThanOrEquals:
                setDescription("Greater or Equal to");
                break;
            case Like:
                setDescription("Contains Word Like");
                break;
            case NotEquals:
                setDescription("Not Equal To");
                break;
            case Reference:
                setDescription("Is");
                break;
            case NotReference:
                setDescription("Is Not");
                break;
            case StartsWith:
                setDescription("Contains Word Starting With");
                break;
            case Past24Hours:
                setDescription("In Past 24 Hours");
                break;
            case Past3Days:
                setDescription("In Past 3 Days");
                break;
            case Past7Days:
                setDescription("In Past 7 Days");
                break;
            case Past30Days:
                setDescription("In Past 30 Days");
                break;
        }
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

