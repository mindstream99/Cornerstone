package com.paxxis.cornerstone.scripting.parser;

import com.paxxis.cornerstone.scripting.RuleSet;

public interface RuleParser {
	public void initialize(String sourceCode);
	public void parseRuleSet(RuleSet ruleSet) throws Exception;
}
