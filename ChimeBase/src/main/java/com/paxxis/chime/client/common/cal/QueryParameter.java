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
    public enum Narrow {
        Rating,
        ReferenceField
    }

    private QueryParameter.Narrow narrowType;
    private ValueExpression.Operator operator;
    private IValue expression;
    private IValue expression2;

    public QueryParameter() {
    }

    public QueryParameter(QueryParameter.Narrow narrowType, ValueExpression.Operator operator, IValue expression) {
        this.narrowType = narrowType;
        this.operator = operator;
        this.expression = expression;
    }

    public QueryParameter(QueryParameter.Narrow narrowType,
            ValueExpression.Operator operator, IValue expression, IValue expression2) {
        this.narrowType = narrowType;
        this.operator = operator;
        this.expression = expression;
        this.expression2 = expression2;
    }

    public QueryParameter.Narrow getNarrowType() {
        return narrowType;
    }

    public ValueExpression.Operator getOperator() {
        return operator;
    }

    public IValue getExpression() {
        return expression;
    }

    public IValue getExpression2() {
        return expression2;
    }
}
