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

package com.paxxis.cornerstone.scripting;

import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class BooleanExpression extends IValue implements IBinaryExpression {
    private static final long serialVersionUID = 1L;

    public static enum Operator {
        INVALID,
        AND,
        OR,
        BOOLEQUALS,
        VALEQUALS,
        STREQUALS,
        NOTEQUALS,
        STRNOTEQUALS,
        GREATERTHAN,
        LESSTHAN,
        GREATERTHANEQ,
        LESSTHANEQ,
        NOT
    }
    
    // the left and right operands
    private IValue leftOperand = null;
    private IValue rightOperand = null;

    // the operator
    private Operator operator = Operator.INVALID;

    public BooleanExpression() {
    }

    // this is not an array
    public boolean isArray() {
        return false;
    }

    public boolean isTable() {
        return false;
    }

    public void findVariables(List<RuleVariable> deps) {
    }

    /**
     * Sets one of the operands for this expression.
     * @param left true if it's the left operand, false otherwise.
     * @param operand the operand
     */
    public void setOperand(boolean left, IValue operand) {
        if (left) {
            leftOperand = operand;
        } else {
            rightOperand = operand;
        }
    }

    /**
     * Returns the left operand of the expression.
     * @return the left operand
     */
    public IValue getLeftOperand() {
        return leftOperand;
    }

    /**
     * Returns the right operand of the expression.
     * @return the right operand
     */
    public IValue getRightOperand() {
        return rightOperand;
    }

    /**
     * Sets the operator for this expression.
     * @param op the boolean operator.
     */
    public void setOperator(Operator op) {
        // if we get an operator and already have a
        // complete expression, we turn ourself into
        // a new expression and make that new one our
        // own left side operand.
        if (leftOperand != null &&
                rightOperand != null && operator != Operator.INVALID) {
            BooleanExpression e = new BooleanExpression();
            e.setOperand(true, leftOperand);
            e.setOperator(operator);
            e.setOperand(false, rightOperand);
            leftOperand = e;
            rightOperand = null;
        }

        operator = op;
    }

    public Object valueAsObject() {
        // since this is a boolean expression, we return
        // a Boolean value
        return new Boolean(valueAsBoolean());
    }

    public String valueAsString() {
        return new BooleanVariable(null, valueAsBoolean()).valueAsString();
    }

    public Integer valueAsInteger() {
        return new BooleanVariable(null, valueAsBoolean()).valueAsInteger();
    }

    public Double valueAsDouble() {
        return new BooleanVariable(null, valueAsBoolean()).valueAsDouble();
    }

    @Override
    public ResultVariable valueAsResult() {
        ResultVariable res = new ResultVariable(null, valueAsBoolean());
        return res;
    }

    public IValue evaluate() {
        return new BooleanVariable(null, valueAsBoolean());
    }

    public Boolean valueAsBoolean() {
        boolean result = false;
        switch (operator) {
            case AND:
                result = (leftOperand.valueAsBoolean() &&
                            rightOperand.valueAsBoolean());
                break;
            case OR:
                result = leftOperand.valueAsBoolean();
                if (!result) {
                    result = rightOperand.valueAsBoolean();
                }
                break;
            case BOOLEQUALS:
                result = (leftOperand.valueAsBoolean() ==
                            rightOperand.valueAsBoolean());
                break;
            case VALEQUALS:
                result = (leftOperand.valueAsDouble() == rightOperand.valueAsDouble());
                break;
            case NOTEQUALS:
                result = (leftOperand.valueAsDouble() != rightOperand.valueAsDouble());
                break;
            case GREATERTHAN:
                result = (leftOperand.valueAsDouble() > rightOperand.valueAsDouble());
                break;
            case LESSTHAN:
                result = (leftOperand.valueAsDouble() < rightOperand.valueAsDouble());
                break;
            case GREATERTHANEQ:
                result = (leftOperand.valueAsDouble() >= rightOperand.valueAsDouble());
                break;
            case LESSTHANEQ:
                result = (leftOperand.valueAsDouble() <= rightOperand.valueAsDouble());
                break;
            case STREQUALS:
                result = (leftOperand.valueAsString().equals(rightOperand.valueAsString()));
                break;
            case STRNOTEQUALS:
                result = !(leftOperand.valueAsString().equals(rightOperand.valueAsString()));
                break;
            case NOT:
                result = !(leftOperand.valueAsBoolean());
                break;
        }

        return result;
    }
}
