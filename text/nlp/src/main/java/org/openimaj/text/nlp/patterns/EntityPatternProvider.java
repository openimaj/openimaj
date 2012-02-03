package org.openimaj.text.nlp.patterns;

public class EntityPatternProvider extends PatternProvider {
	
	String Entity = "&(amp|lt|gt|quot);";
		
	@Override
	public String patternString() {
		return Entity;
	}
}
