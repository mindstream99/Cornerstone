/* Copyright 2010 the original author or authors.
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
 * A 3-tuple that contains a result code, a boolean indicating success (true),
 * or failure (false), and a textual description of the result.
 * 
 * When the right side of an expression is a Result, then all three fields are
 * copied if the left side is also a Result.  If the left side is a Result and
 * the right side is not, then the right side result code is rendered.  If the
 * left side is a Result and the right side is not, then the right side expression
 * value is rendered as a boolean and used to set the value of the Result's result code.
 * The result code and description are left as null in this case.
 * 
 * @author Robert Englander
 *
 */
public class ResultVariable extends RuleVariable {
    private static final long serialVersionUID = 1L;

    private static enum Methods {
        isSuccess,
        getResultCode,
        getDescription;
        
	public static boolean contains(String name) {
	    boolean contains = false;
	    for (Methods option : Methods.values()) {
		if (option.toString().equals(name)) {
		    contains = true;
		    break;
		}
	    }
		
	    return contains;
	}
    }

    // the result (true for success, false for failure)
    private Boolean success = null;
    
    private Integer resultCode = new Integer(0);
    private String description = "";

    public ResultVariable() {
    }

    /**
     * Constructs the variable
     * @param name the name of the variable, or null if anonymous
     */
    public ResultVariable(String name) {
        super(name);
    }

    public boolean isNull() {
    	return null == success;
    }
    
    public String getType() {
        return "Result";
    }
    
    public void resetValue() {
    	if (!this.getHasParameterDefault()) {
            success = null;
            resultCode = new Integer(0);
            description = "";
    	}
    }
    
    public ResultVariable(String name, boolean value) {
        super(name);
        this.success = value;
    }

    public boolean methodHasReturn(String name) {
    	if (Methods.contains(name)) {
            switch (Methods.valueOf(name)) {
                case isSuccess:
                case getResultCode:
                case getDescription:
                    return true;
            }
    	}

        return super.methodHasReturn(name);
    }

    public int getMethodParameterCount(String name) {
    	if (Methods.contains(name)) {
            switch (Methods.valueOf(name)) {
                case isSuccess:
                case getResultCode:
                case getDescription:
                    return 0;
            }
    	}

        return super.getMethodParameterCount(name);
    }

    public IValue executeMethod(String name, List<IValue> params) {
    	if (Methods.contains(name)) {
            switch (Methods.valueOf(name)) {
                case isSuccess:
                    return isSuccess(params);
                case getResultCode:
                    return getResultCode(params);
                case getDescription:
                    return getDescription(params);
            }
    	}

        return super.executeMethod(name, params);
    }

    private IValue isSuccess(List<IValue> params) {
        return new BooleanVariable(null, success);
    }
    
    private IValue getResultCode(List<IValue> params) {
        return new IntegerVariable(null, resultCode);
    }
    
    private IValue getDescription(List<IValue> params) {
        return new StringVariable(null, description);
    }
    
    public void setResultCode(int code) {
	resultCode = code;
    }
    
    public void setDescription(String desc) {
	description = desc;
    }
    
    public void setValue(boolean val) {
        success = val;

        if (runtime != null) {
            runtime.variableChange(this);
        }
    }

    protected void setValue(IValue val) {
        if (val instanceof RuleVariable) {
            RuleVariable rv = (RuleVariable)val;
            setValue(rv);
        } else {
            setValue(val.valueAsResult());
        }
    }

    private void setValue(RuleVariable rv) {
    	if (rv instanceof ResultVariable) {
    	    ResultVariable dv = (ResultVariable)rv;
    	    success = dv.success;
    	    resultCode = dv.resultCode;
    	    description = dv.description;
    	} else {
    		Boolean sval = rv.valueAsBoolean();
    		if (sval == null) {
    			success = null;
    		} else {
    			success = sval;
    		}
    	}
    }

    /**
     * @return the value as an Object
     */
    public Object valueAsObject()
    {
    	if (isNull()) {
    		return null;
    	}

    	return new Boolean(valueAsBoolean());
    }

    /**
     * @return the value as a String
     */
    public String valueAsString()
    {
    	if (isNull()) {
    	    return null;
    	}

	StringBuilder buf = new StringBuilder();

        buf.append("Result: ")
           .append(success)
           .append(" Code: ").append(resultCode)
           .append(" Description: ").append(description);
        
        return buf.toString();
    }

    /**
     * @return the value as a double
     */
    public Double valueAsDouble()
    {
    	if (isNull()) {
    		return null;
    	}
    	
        if (success == true)
        {
            return 1.0;
        }
        else
        {
            return 0.0;
        }
    }

    /**
     * @return the value as an int
     */
    public Integer valueAsInteger()
    {
    	if (isNull()) {
    		return null;
    	}

    	if (success == true)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    /**
     * @return the value as a boolean
     */
    public Boolean valueAsBoolean()
    {
    	if (isNull()) {
    		return null;
    	}

    	return success;
    }

    @Override
    public ResultVariable valueAsResult() {
        return this;
    }

    /**
     * evaluates this variable without returning its value.
     */
    public IValue evaluate()
    {
        // this is a no-op
        return this;
    }

}
