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


/**
 * @author Robert Englander
 */
public class StringVariable extends RuleVariable {
    private static final long serialVersionUID = 2L;
    private static MethodProvider<StringVariable> methodProvider = new MethodProvider<StringVariable>(StringVariable.class);
    static {
        methodProvider.initialize();
    }

    // the value
    private String value = null;
    private String parameterDefault = null;

    public StringVariable() {

    }

    public StringVariable(String name) {
        super(name);
    }

    public StringVariable(String name, String value) {
        super(name);
        this.value = value;
    }

    @Override
    protected MethodProvider<StringVariable> getMethodProvider() {
        return methodProvider;
    }

    public boolean isValueNull() {
        return null == value;
    }

    public String getType() {
        return "String";
    }

    public String getDefaultValue() {
        return (parameterDefault == null) ? "null" : parameterDefault;
    }

    public void resetValue() {
        if (this.getHasParameterDefault() && value == null) {
            value = parameterDefault;
        }
    }

    public void setParameterDefaultValue(String val) {
        parameterDefault = val;
        setHasParameterDefault(true);
    }

    @Override
    public boolean supportsMacroExpansion() {
        return true;
    }

    /**
     * determines if the passed string parameter is contained
     * within this string
     * @param params
     * @return
     */
    @CSLMethod
    public IValue contains(IValue param) {
        String sub = param.valueAsString();
        boolean contains = -1 != value.indexOf(sub);
        return new BooleanVariable(null, contains);
    }

    @CSLMethod
    public IValue length() {
        int size = value.length();
        return new IntegerVariable(null, size);
    }

    @CSLMethod
    public void makeUpperCase() {
        setValue(new StringVariable(null, value.toUpperCase()));
    }

    /**
     * subString is used for obtaining a segment of this string, hence it's usage is akin to the underlying
     * Java method function of substring(int beginIndex, int endIndex), with the first character being index 0
     * @param params  beginIndex - the beginning index, inclusive.
     *                endIndex - the ending index, exclusive.
     * @return    a new string that is a substring of this string. The substring begins at the specified beginIndex and extends to the character at index endIndex - 1. Thus the length of the substring is endIndex-beginIndex
     * Throws:
     *       IndexOutOfBoundsException - if the beginIndex is negative, or endIndex is larger than the length of this String object, or beginIndex is larger than endIndex.
     */
    @CSLMethod
    public IValue subString(IValue start, IValue end) {
        int beginIndex = start.valueAsInteger();
        int endIndex = end.valueAsInteger();
        String substring = value.substring(beginIndex, endIndex);
        return new StringVariable("",substring);
    }

    protected void setValue(IValue val) {
        if (val instanceof RuleVariable) {
            RuleVariable rv = (RuleVariable)val;
            setValue(rv);
        } else {
            value = val.valueAsString();
        }

        if (runtime != null) {
            runtime.variableChange(this);
        }
    }

    private void setValue(RuleVariable rv) {
        value = renderAsString(rv.valueAsString());
    }

    protected String renderAsString(String val) {
        String v = val;
        if (isMacro()) {
            v = runtime.performMacroExpansion(v);
        }

        return v;
    }

    public Object valueAsObject() {
        return value;
    }

    public String valueAsString() {
        return renderAsString(value);
    }

    public Double valueAsDouble() {
        if (isValueNull()) {
            return null;
        }

        double result = Double.valueOf(value).doubleValue();
        return result;
    }

    public Integer valueAsInteger() {
        if (isValueNull()) {
            return null;
        }

        // we need to truncate doubles...
        int result = valueAsDouble().intValue();
        return result;
    }

    public Boolean valueAsBoolean() {
        if (isValueNull()) {
            return null;
        }

        return value.equals("true");
    }

    @Override
    public ResultVariable valueAsResult() {
        ResultVariable res = new ResultVariable(null, valueAsBoolean());
        return res;
    }

    public IValue evaluate() {
        return new StringVariable(null, valueAsString());
    }

}
