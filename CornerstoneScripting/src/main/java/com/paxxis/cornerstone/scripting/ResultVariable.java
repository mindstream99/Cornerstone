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

import java.util.ArrayList;
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
    private static final long serialVersionUID = 2L;
    private static MethodProvider<ResultVariable> methodProvider = new MethodProvider<ResultVariable>(ResultVariable.class);
    static {
        methodProvider.initialize();
    }

	// the result (true for success, false for failure)
	private Boolean success = null;

	private Integer resultCode = new Integer(0);
	private List<String> messages = new ArrayList<String>();
	
	// sub results that have been merged
	private List<ResultVariable> mergedResults = new ArrayList<ResultVariable>();

	public ResultVariable() {
	}

	/**
	 * Constructs the variable
	 * @param name the name of the variable, or null if anonymous
	 */
	public ResultVariable(String name) {
		super(name);
	}

    @Override
    protected MethodProvider<ResultVariable> getMethodProvider() {
        return methodProvider;
    }

    public boolean isValueNull() {
		return null == success;
	}

	public String getType() {
		return "Result";
	}

	public void resetValue() {
		if (!this.getHasParameterDefault()) {
			success = null;
			resultCode = new Integer(0);
			messages.clear();
			mergedResults.clear();
		}
	}

	public ResultVariable(String name, boolean value) {
		super(name);
		this.success = value;
	}

	public Integer getResultCode() {
	    return this.resultCode;
	}
	
	@CSLMethod
	public IValue isSuccess() {
		return new BooleanVariable(null, success);
	}

	@CSLMethod
	public IValue getResCode() {
		return new IntegerVariable(null, resultCode);
	}

	public List<String> getMessages() {
		return messages;
	}
	
	public List<ResultVariable> getMerged() {
		return mergedResults;
	}
	
	@CSLMethod
	public IValue getResultMessages() {
		Array msgs = new Array();
		List<IValue> list = new ArrayList<IValue>();
		for (String msg : messages) {
			StringVariable sv = new StringVariable(null, msg);
			list.add(sv);
		}

		msgs.initialize(list);
		return msgs;
	}

	@CSLMethod
	public IValue merge(IValue param) {
		if (!(param instanceof ResultVariable)) {
			throw new ScriptExecutionException(301, "merge parameter must be a Result");
		}

        ResultVariable rv = (ResultVariable)param;
		merge(rv);
		return new BooleanVariable(null, true);
	}

	public void merge(ResultVariable rv) {
        if (success == null || success) {
            success = rv.success;
        }

        // rule variables are re-used, so we copy the merged variable before copying
        ResultVariable result = new ResultVariable();
        result.setValue(rv);
        mergedResults.add(result);
	}
	
	public void setResultCode(int code) {
		resultCode = code;
	}

	public void addMessage(String message) {
		messages.add(message);
	}

	public void insertMessage(String message) {
		messages.add(0, message);
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
			messages.clear();
			messages.addAll(dv.messages);
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
		if (isValueNull()) {
			return null;
		}

		return new Boolean(valueAsBoolean());
	}

	/**
	 * @return the value as a String
	 */
	 public String valueAsString()
	{
		if (isValueNull()) {
			return null;
		}

		StringBuilder buf = new StringBuilder();

		buf.append("Result: ")
		.append(success)
		.append(" Code: ").append(resultCode)
		.append(" Description: ").append(messages);

		return buf.toString();
	}

	/**
	 * @return the value as a double
	 */
	 public Double valueAsDouble()
	 {
		 if (isValueNull()) {
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
		 if (isValueNull()) {
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
		 if (isValueNull()) {
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
