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
 * @author Robert Englander
 */
public class ValueExpression extends IValue implements IBinaryExpression {
    private static final long serialVersionUID = 1L;

    public static enum Operator {
        INVALID,
        STRCAT,
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
        PLUS,
        MINUS,
        MULT,
        DIV,
        NOT,
        IS,
        ISNOT,
        ON,
        BEFORE,
        AFTER
    }
    
    private IValue leftOperand = null;
    private IValue rightOperand = null;

    private Operator operator = Operator.INVALID;

    public ValueExpression() {
    }

    public boolean isArray() {
        return false;
    }

    public boolean isTable() {
        return false;
    }

    public void findVariables(List<RuleVariable> deps) {
        if (leftOperand != null) {
            leftOperand.findVariables(deps);
        }
        
        if (rightOperand != null) {
            rightOperand.findVariables(deps);
        }
    }

    public void setOperand(boolean left, IValue exp) {
        if (left) {
            leftOperand = exp;
        } else {
            rightOperand = exp;
        }
    }

    public IValue getLeftOperand() {
        return leftOperand;
    }

    public IValue getRightOperand() {
        return rightOperand;
    }

    private boolean takesPrecedence(Operator op) {
        return (precedenceLevel(op) < precedenceLevel(operator));
    }
    
    private int precedenceLevel(Operator op) {
        switch (op) {
            case MULT:
            case DIV:
                return 0;
            case PLUS:
            case MINUS:
                return 1;
            case BOOLEQUALS:
            case VALEQUALS:
            case NOTEQUALS:
            case GREATERTHAN:
            case LESSTHAN:
            case GREATERTHANEQ:
            case LESSTHANEQ:
            case STREQUALS:
            case STRNOTEQUALS:
                return 2;
            case AND:
            case OR:
            case NOT:
                return 3;
            case STRCAT:
                return 4;
        }
        
        throw new RuntimeException("Invalid Operator");
    }
    
    public void setOperator(Operator op, IValue exp) {
        if (!operator.equals(Operator.INVALID)) {
            if (takesPrecedence(op)) {
                ValueExpression e = new ValueExpression();
                e.setOperand(true, rightOperand);
                e.setOperator(op, exp);
                rightOperand = e;
            } else {
                ValueExpression e = new ValueExpression();
                e.setOperand(true, leftOperand);
                e.setOperator(operator, rightOperand);
                leftOperand = e;
                operator = op;
                rightOperand = exp;
            }
        } else {
            operator = op;
            setOperand(false, exp);
        }
    }

    public Object valueAsObject() {
        IValue val = operate();
    	if (val == null) {
    	    return null;
    	}
        return val.valueAsObject();
    }

    public String valueAsString() {
        IValue val = operate();
    	if (val == null) {
    	    return null;
    	}
        return val.valueAsString();
    }

    public Integer valueAsInteger() {
        IValue val = operate();
    	if (val == null) {
    	    return null;
    	}
        return val.valueAsInteger();
    }

    public Boolean valueAsBoolean() {
        IValue val = operate();
    	if (val == null) {
    	    return null;
    	}
        return val.valueAsBoolean();
    }

    @Override
    public ResultVariable valueAsResult() {
        ResultVariable res = new ResultVariable(null, valueAsBoolean());
        return res;
    }

    public Double valueAsDouble() {
        IValue val = operate();
    	if (val == null) {
    	    return null;
    	}
        return val.valueAsDouble();
    }

    protected IValue operate() {
        IValue result = null;

        switch (operator) {
            case PLUS:
                result = new DoubleVariable(null,(leftOperand.valueAsDouble() +
                            rightOperand.valueAsDouble()));
                break;
            case MINUS:
                result = new DoubleVariable(null,(leftOperand.valueAsDouble() -
                            rightOperand.valueAsDouble()));
                break;
            case MULT:
                result = new DoubleVariable(null,(leftOperand.valueAsDouble() *
                            rightOperand.valueAsDouble()));
                break;
            case DIV:
                result = new DoubleVariable(null,(leftOperand.valueAsDouble() /
                            rightOperand.valueAsDouble()));
                break;
            case STRCAT:
                String left = leftOperand.valueAsString();
                String right = rightOperand.valueAsString();
                String str = left + right;
                result = new StringVariable(null, str);
                break;
            case AND:
                result = new BooleanVariable(null, (leftOperand.valueAsBoolean() &&
                            rightOperand.valueAsBoolean()));
                break;
            case OR:
                boolean res = leftOperand.valueAsBoolean();
                if (!res)
                {
                    res = rightOperand.valueAsBoolean();
                }
                result = new BooleanVariable(null, res);
                break;
            case BOOLEQUALS:
                result = new BooleanVariable(null, (leftOperand.valueAsBoolean() ==
                            rightOperand.valueAsBoolean()));
                break;
            case VALEQUALS:
                result = new BooleanVariable(null, (leftOperand.valueAsDouble() == rightOperand.valueAsDouble()));
                break;
            case NOTEQUALS:
                result = new BooleanVariable(null, (leftOperand.valueAsDouble() != rightOperand.valueAsDouble()));
                break;
            case GREATERTHAN:
                result = new BooleanVariable(null, (leftOperand.valueAsDouble() > rightOperand.valueAsDouble()));
                break;
            case LESSTHAN:
                result = new BooleanVariable(null, (leftOperand.valueAsDouble() < rightOperand.valueAsDouble()));
                break;
            case GREATERTHANEQ:
                result = new BooleanVariable(null, (leftOperand.valueAsDouble() >= rightOperand.valueAsDouble()));
                break;
            case LESSTHANEQ:
                result = new BooleanVariable(null, (leftOperand.valueAsDouble() <= rightOperand.valueAsDouble()));
                break;
            case STREQUALS:
                result = new BooleanVariable(null, (leftOperand.valueAsString().equals(rightOperand.valueAsString())));
                break;
            case STRNOTEQUALS:
                result = new BooleanVariable(null, !(leftOperand.valueAsString().equals(rightOperand.valueAsString())));
                break;
            case NOT:
                result = new BooleanVariable(null, !(leftOperand.valueAsBoolean()));
                break;
        }

        return result;
    }

    public IValue evaluate() {
        return operate();
    }

}
