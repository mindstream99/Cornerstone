package com.paxxis.cornerstone.scripting.parser;

import java.util.List;

import com.paxxis.cornerstone.scripting.RuleSet;

public interface RuleParser {
	public void initialize(String sourceCode);
	public void parseRuleSet(RuleSet ruleSet) throws Exception;
	public boolean hasParseErrors();
	public List<ParseException> getParseErrors();
}
