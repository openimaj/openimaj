package org.openimaj.text.nlp.patterns;

import java.util.regex.Pattern;

public abstract class PatternProvider {
	private Pattern compiledPattern = null;
	
	
	
	public abstract String patternString();
	public Pattern pattern() {
		if (compiledPattern == null)
			compiledPattern = Pattern.compile(patternString());
		return compiledPattern;
	}
	public PatternProvider combine(PatternProvider other) {
		return new CombinedPatternProvider(this,other);
	}
	
}
