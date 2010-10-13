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
 * @author Robert Englander
 */
public class ValueExpression extends IValue implements IBinaryExpression
{
    public static enum Operator
    {
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
    
    IValue _leftOperand = null;
    IValue _rightOperand = null;

    Operator _operator = Operator.INVALID;

    public ValueExpression()
    {
    }

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
        if (_leftOperand != null)
        {
            _leftOperand.findVariables(deps);
        }
        
        if (_rightOperand != null)
        {
            _rightOperand.findVariables(deps);
        }
    }

    public void setOperand(boolean left, IValue exp)
    {
        if (left)
        {
            _leftOperand = exp;
        }
        else
        {
            _rightOperand = exp;
        }
    }

    public IValue getLeftOperand()
    {
        return _leftOperand;
    }

    public IValue getRightOperand()
    {
        return _rightOperand;
    }

    private boolean takesPrecedence(Operator op)
    {
        return (precedenceLevel(op) < precedenceLevel(_operator));
    }
    
    private int precedenceLevel(Operator op)
    {
        switch (op)
        {
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
    
    public void setOperator(Operator operator, IValue exp)
    {
        if (_operator != Operator.INVALID)
        {
            if (takesPrecedence(operator))
            {
                ValueExpression e = new ValueExpression();
                e.setOperand(true, _rightOperand);
                e.setOperator(operator, exp);
                _rightOperand = e;
            }
            else
            {
                ValueExpression e = new ValueExpression();
                e.setOperand(true, _leftOperand);
                e.setOperator(_operator, _rightOperand);
                _leftOperand = e;
                _operator = operator;
                _rightOperand = exp;
            }
        }
        else
        {
            _operator = operator;
            setOperand(false, exp);
        }
    }

    public Object valueAsObject()
    {
        IValue val = operate();
        return val.valueAsObject();
    }

    public String valueAsString()
    {
        IValue val = operate();
        return val.valueAsString();
    }

    public int valueAsInteger()
    {
        IValue val = operate();
        return val.valueAsInteger();
    }

    public boolean valueAsBoolean()
    {
        IValue val = operate();
        return val.valueAsBoolean();
    }

    public double valueAsDouble()
    {
        IValue val = operate();
        return val.valueAsDouble();
    }

    protected IValue operate()
    {
        IValue result = null;

        switch (_operator)
        {
            case PLUS:
                result = new DoubleVariable(null,(_leftOperand.valueAsDouble() +
                            _rightOperand.valueAsDouble()));
                break;
            case MINUS:
                result = new DoubleVariable(null,(_leftOperand.valueAsDouble() -
                            _rightOperand.valueAsDouble()));
                break;
            case MULT:
                result = new DoubleVariable(null,(_leftOperand.valueAsDouble() *
                            _rightOperand.valueAsDouble()));
                break;
            case DIV:
                result = new DoubleVariable(null,(_leftOperand.valueAsDouble() /
                            _rightOperand.valueAsDouble()));
                break;
            case STRCAT:
                String left = _leftOperand.valueAsString();
                String right = _rightOperand.valueAsString();
                String str = left + right;
                result = new StringVariable(null, str);
                break;
            case AND:
                result = new BooleanVariable(null, (_leftOperand.valueAsBoolean() &&
                            _rightOperand.valueAsBoolean()));
                break;
            case OR:
                boolean res = _leftOperand.valueAsBoolean();
                if (!res)
                {
                    res = _rightOperand.valueAsBoolean();
                }
                result = new BooleanVariable(null, res);
                break;
            case BOOLEQUALS:
                result = new BooleanVariable(null, (_leftOperand.valueAsBoolean() ==
                            _rightOperand.valueAsBoolean()));
                break;
            case VALEQUALS:
                result = new BooleanVariable(null, (_leftOperand.valueAsDouble() == _rightOperand.valueAsDouble()));
                break;
            case NOTEQUALS:
                result = new BooleanVariable(null, (_leftOperand.valueAsDouble() != _rightOperand.valueAsDouble()));
                break;
            case GREATERTHAN:
                result = new BooleanVariable(null, (_leftOperand.valueAsDouble() > _rightOperand.valueAsDouble()));
                break;
            case LESSTHAN:
                result = new BooleanVariable(null, (_leftOperand.valueAsDouble() < _rightOperand.valueAsDouble()));
                break;
            case GREATERTHANEQ:
                result = new BooleanVariable(null, (_leftOperand.valueAsDouble() >= _rightOperand.valueAsDouble()));
                break;
            case LESSTHANEQ:
                result = new BooleanVariable(null, (_leftOperand.valueAsDouble() <= _rightOperand.valueAsDouble()));
                break;
            case STREQUALS:
                result = new BooleanVariable(null, (_leftOperand.valueAsString().equals(_rightOperand.valueAsString())));
                break;
            case STRNOTEQUALS:
                result = new BooleanVariable(null, !(_leftOperand.valueAsString().equals(_rightOperand.valueAsString())));
                break;
            case NOT:
                result = new BooleanVariable(null, !(_leftOperand.valueAsBoolean()));
                break;
        }

        return result;
    }

    public IValue evaluate()
    {
        return operate();
    }

}
