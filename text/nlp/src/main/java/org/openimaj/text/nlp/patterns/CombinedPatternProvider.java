package org.openimaj.text.nlp.patterns;

public class CombinedPatternProvider extends PatternProvider {

	private String pattern;

	public CombinedPatternProvider(PatternProvider one,PatternProvider two) {
		this.pattern = regex_or(one,two);
	}

	@Override
	public String patternString() {
		return this.pattern;
	}

}
