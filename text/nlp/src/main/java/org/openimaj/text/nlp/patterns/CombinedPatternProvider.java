package org.openimaj.text.nlp.patterns;

import org.openimaj.text.util.RegexUtil;

public class CombinedPatternProvider extends PatternProvider {

	private String pattern;

	public CombinedPatternProvider(PatternProvider one,PatternProvider two) {
		this.pattern = RegexUtil.regex_or(one,two);
	}

	@Override
	public String patternString() {
		return this.pattern;
	}

}
