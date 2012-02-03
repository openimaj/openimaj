package org.openimaj.text.nlp.patterns;

public class EntityPatternProvider extends PatternProvider {
	
	String Entity = "&(amp|lt|gt|quot|#[0-9]+);";
		
	@Override
	public String patternString() {
		return Entity;
	}
}
