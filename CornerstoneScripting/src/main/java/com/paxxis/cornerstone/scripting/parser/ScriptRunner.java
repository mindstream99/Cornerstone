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
package com.paxxis.cornerstone.scripting.parser;

import java.util.ArrayList;
import java.util.List;

import com.paxxis.cornerstone.scripting.ContextProvider;
import com.paxxis.cornerstone.scripting.IValue;
import com.paxxis.cornerstone.scripting.Rule;
import com.paxxis.cornerstone.scripting.RuleSet;
import com.paxxis.cornerstone.scripting.extension.ExtensionHelper;

/**
 * 
 * @author Robert Englander
 *
 */
public class ScriptRunner {

    /**
     * @param args
     */
    public static void main(String[] args) {
	RuleReader reader = new RuleReader();
	String content = reader.getFileContents(args[0]);
	
	CSLRuleParser parser = CSLRuleParser.create(content);
        CSLRuntime runtime = new CSLRuntime();
        runtime.setContextProvider(
            new ContextProvider() {
		@Override
		public ExtensionHelper createExtensionHelper(String id) {
		    return null;
		}

		@Override
		public boolean allowsWhileLoops() {
		    return false;
		}

		@Override
		public boolean supportsMacroExpansion() {
		    // TODO Auto-generated method stub
		    return false;
		}

		@Override
		public String performMacroExpansion(String value) {
		    return value;
		}
        	
            }
        );
        
        RuleSet ruleSet = new RuleSet(args[0], parser.getSourceCode(), runtime);

        try {
            parser.parseRuleSet(ruleSet);
            Rule rule = ruleSet.getRule(args[1]);
            
            List<IValue> params = new ArrayList<IValue>();
            if (rule == null) {
        	    throw new Exception("No such script method: " + args[1]);
            }
            
            long start = System.currentTimeMillis();
            boolean result = rule.process(params);
            long end = System.currentTimeMillis();
            System.out.println("Completed: " + rule.getReturnValue().valueAsString());
            System.out.println("Elapsed time: " + (end - start) + " msecs");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    	
    }

}
