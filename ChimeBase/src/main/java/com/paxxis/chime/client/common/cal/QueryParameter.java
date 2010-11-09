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

package com.paxxis.chime.client.common.cal;

import java.io.Serializable;

/**
 *
 * @author Robert Englander
 */
public class QueryParameter implements Serializable {

    private ValueExpression.Operator operator;
    private IValue shapeExpression = null;
    private IValue fieldExpression = null;
    private IValue subFieldExpression = null;
    private IValue valueExpression = null;

    public QueryParameter() {
    }

    public QueryParameter(IValue shapeExpression, IValue fieldExpression, IValue subFieldExpression, ValueExpression.Operator operator, IValue valueExpression) {
        this.operator = operator;
        this.shapeExpression = shapeExpression;
        this.fieldExpression = fieldExpression;
        this.subFieldExpression = subFieldExpression;
        this.valueExpression = valueExpression;
    }

    public QueryParameter(IValue fieldExpression, ValueExpression.Operator operator, IValue valueExpression) {
        this.operator = operator;
        this.fieldExpression = fieldExpression;
        this.valueExpression = valueExpression;
    }

    public ValueExpression.Operator getOperator() {
        return operator;
    }

    public IValue getShapeExpression() {
        return shapeExpression;
    }

    public IValue getFieldExpression() {
        return fieldExpression;
    }

    public IValue getSubFieldExpression() {
        return subFieldExpression;
    }

    public IValue getValueExpression() {
        return valueExpression;
    }
}
