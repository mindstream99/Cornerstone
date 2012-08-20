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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.paxxis.cornerstone.scripting.parser.CSLRuntime;
import com.paxxis.cornerstone.scripting.parser.ParseException;

/**
 * 
 * @author Rob Englander
 *
 */
public class FileSystemScriptLoader implements ScriptLoader {

	private String sourceName = null;
	private boolean useSubDirectories = true;
	private String methodName = null;
	private RuleSet ruleSet = null;
	private ParserCreator parserCreator = null;
	private CSLRuntime cslRuntime = null;
	
	public static void main(String[] args) {
	    FileSystemScriptLoader loader = new FileSystemScriptLoader();
	    loader.setSourceName(args[0]);
	    loader.setUseSubDirectories(true);
	    loader.setMethodName(args[1]);
	    loader.setRuntime(new CSLRuntime());
	    loader.setParserCreator(new CSLParserCreator());
	    loader.initialize();
	    try {
		loader.ruleSet = loader.load();
	            Rule rule = loader.ruleSet.getRule(loader.methodName);
	            
	            if (rule == null) {
	            	System.err.println("No such script method: " + loader.methodName);
	            	System.exit(1);
	            }
	        
	            List<IValue> params = new ArrayList<IValue>();
	            boolean result = rule.process(params);
	    
	            IValue value = rule.getReturnValue();
	            String str = value.valueAsString();
	            System.out.println(str);
	    
	            System.exit(0);
	    } catch (Exception e) {
		e.printStackTrace();
		System.exit(1);
	    }
	    
	}

	public void setSourceName(String name) {
		sourceName = name;
	}
	
	public void setUseSubDirectories(boolean useSubDirectories) {
		this.useSubDirectories = useSubDirectories;
	}

	public void setMethodName(String name) {
		methodName = name;
	}

	public void setParserCreator(ParserCreator parserCreator) {
		this.parserCreator = parserCreator;
	}

	public void setRuntime(CSLRuntime runtime) {
		this.cslRuntime = runtime;
	}
	
	public void initialize() {
		if (sourceName == null) {
			throw new RuntimeException("SourceName property can't be null");
		}
		
		if (parserCreator == null) {
			throw new RuntimeException("ParserCreator property can't be null");
		}
		
		if (cslRuntime == null) {
			throw new RuntimeException("Runtime property can't be null");
		}
	}
	
	private void loadSource(File source, boolean recursive) throws Exception {
		try {
			if (source.isDirectory()) {
				if (recursive) {
		            String[] fileList = source.list();
		            for (String file : fileList) {
		            	File f = new File(source.getAbsolutePath() + File.separator + file);
		            	loadSource(f, useSubDirectories);
		            }
				}
			} else {
				StringBuilder buffer = new StringBuilder();
				
				FileReader fr = new FileReader(source);
				BufferedReader rdr = new BufferedReader(fr);
				String line;
				while (null != (line = rdr.readLine())) {
					buffer.append(line).append("\n");
				}
				
				rdr.close();
				
				parserCreator.process(buffer.toString(), ruleSet);
			}

		} catch (ParseException pe) {
			throw new Exception("Source: " + source.getCanonicalPath() + "\n" + pe.getMessage(), pe);
		}
	}
	
	public RuleSet load() throws Exception {
		try {
			ruleSet = new RuleSet(sourceName, "", cslRuntime);
			File source = new File(sourceName);
			loadSource(source, true);
        	ruleSet.resoveRuleReferences();
        	return ruleSet;

		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
