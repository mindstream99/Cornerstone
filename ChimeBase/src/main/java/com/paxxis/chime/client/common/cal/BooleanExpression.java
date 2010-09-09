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

import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class BooleanExpression extends IValue implements IBinaryExpression
{
    public static enum Operator
    {
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
    IValue _leftOperand = null;
    IValue _rightOperand = null;

    // the operator
    Operator _operator = Operator.INVALID;

    public BooleanExpression()
    {
    }

    // this is not an array
    public boolean isArray()
    {
        return false;
    }

    public boolean isTable()
    {
        return false;
    }

    public void findVariables(List<RuleVariable> deps)
    {
    }

    /**
     * Sets one of the operands for this expression.
     * @param left true if it's the left operand, false otherwise.
     * @param operand the operand
     */
    public void setOperand(boolean left, IValue operand)
    {
        if (left)
        {
            _leftOperand = operand;
        }
        else
        {
            _rightOperand = operand;
        }
    }

    /**
     * Returns the left operand of the expression.
     * @return the left operand
     */
    public IValue getLeftOperand()
    {
        return _leftOperand;
    }

    /**
     * Returns the right operand of the expression.
     * @return the right operand
     */
    public IValue getRightOperand()
    {
        return _rightOperand;
    }

    /**
     * Sets the operator for this expression.
     * @param operator the boolean operator.
     */
    public void setOperator(Operator operator)
    {
        // if we get an operator and already have a
        // complete expression, we turn ourself into
        // a new expression and make that new one our
        // own left side operand.
        if (_leftOperand != null &&
                _rightOperand != null && _operator != Operator.INVALID)
        {
            BooleanExpression e = new BooleanExpression();
            e.setOperand(true, _leftOperand);
            e.setOperator(_operator);
            e.setOperand(false, _rightOperand);
            _leftOperand = e;
            _rightOperand = null;
        }

        _operator = operator;
    }

    /**
     * returns the value of the expression as an Object
     * @return
     */
    public Object valueAsObject()
    {
        // since this is a boolean expression, we return
        // a Boolean value
        return new Boolean(valueAsBoolean());
    }

    /**
     * returns the value of the expression as a String.
     * @return
     */
    public String valueAsString()
    {
        return new BooleanVariable(null, valueAsBoolean()).valueAsString();
    }

    /**
     * returns the value of the expression as an integer.
     * @return
     */
    public int valueAsInteger()
    {
        return new BooleanVariable(null, valueAsBoolean()).valueAsInteger();
    }

    /**
     * returns the value of the expression as a double.
     * @return
     */
    public double valueAsDouble()
    {
        return new BooleanVariable(null, valueAsBoolean()).valueAsDouble();
    }

    /**
     * evaluates the expression without returning the result.
     */
    public IValue evaluate()
    {
        return new BooleanVariable(null, valueAsBoolean());
    }

    /**
     * returns the value of the expression as a boolean.
     * @return
     */
    public boolean valueAsBoolean()
    {
        boolean result = false;
        switch (_operator)
        {
            case AND:
                result = (_leftOperand.valueAsBoolean() &&
                            _rightOperand.valueAsBoolean());
                break;
            case OR:
                result = _leftOperand.valueAsBoolean();
                if (!result)
                {
                    result = _rightOperand.valueAsBoolean();
                }
                break;
            case BOOLEQUALS:
                result = (_leftOperand.valueAsBoolean() ==
                            _rightOperand.valueAsBoolean());
                break;
            case VALEQUALS:
                result = (_leftOperand.valueAsDouble() == _rightOperand.valueAsDouble());
                break;
            case NOTEQUALS:
                result = (_leftOperand.valueAsDouble() != _rightOperand.valueAsDouble());
                break;
            case GREATERTHAN:
                result = (_leftOperand.valueAsDouble() > _rightOperand.valueAsDouble());
                break;
            case LESSTHAN:
                result = (_leftOperand.valueAsDouble() < _rightOperand.valueAsDouble());
                break;
            case GREATERTHANEQ:
                result = (_leftOperand.valueAsDouble() >= _rightOperand.valueAsDouble());
                break;
            case LESSTHANEQ:
                result = (_leftOperand.valueAsDouble() <= _rightOperand.valueAsDouble());
                break;
            case STREQUALS:
                result = (_leftOperand.valueAsString().equals(_rightOperand.valueAsString()));
                break;
            case STRNOTEQUALS:
                result = !(_leftOperand.valueAsString().equals(_rightOperand.valueAsString()));
                break;
            case NOT:
                result = !(_leftOperand.valueAsBoolean());
                break;
        }

        return result;
    }
}
