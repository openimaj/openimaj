package org.openimaj.text.nlp.patterns;

public class TwitterStuffPatternProvider extends PatternProvider{
	
	String TwitterR = "([@|#|$][A-Za-z0-9_]+)";

	@Override
	public String patternString() {
		return TwitterR;
	}
	
}
