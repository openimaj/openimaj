package org.openimaj.text.nlp.patterns;

import java.util.regex.Pattern;

public class PunctuationPatternProvider extends PatternProvider{
	
	String PunctChars = "['\\u201c\\u201d\".?!,:;]";
	String Punct = String.format("%s+", PunctChars);
	@Override
	public String patternString() {
		return Punct ;
	}
	
	public String getPunctuation(){
		return PunctChars;
	}

	@Override
	public PatternProvider combine(PatternProvider other) {
		// TODO Auto-generated method stub
		return null;
	}
}
