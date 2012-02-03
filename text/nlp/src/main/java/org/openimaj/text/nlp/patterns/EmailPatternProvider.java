package org.openimaj.text.nlp.patterns;

public class EmailPatternProvider extends PatternProvider{
	
	String emailR = "\\b([\\w\\-]?)+([\\.\\w]?)+[\\w]+@([\\w\\-]+\\.)+[a-z]{2,4}";

	@Override
	public String patternString() {
		return emailR;
	}
	
}
