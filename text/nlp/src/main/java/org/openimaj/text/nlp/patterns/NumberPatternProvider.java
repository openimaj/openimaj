package org.openimaj.text.nlp.patterns;

public class NumberPatternProvider extends PatternProvider{
	String Number = "^\\d+";
	String NumNum = "\\d+\\.\\d+";
	String NumberWithCommas = "(\\d+,)+?\\d{3}" + pos_lookahead(regex_or("[^,]","$"));
	
	@Override
	public String patternString() {
		return regex_or(Number,NumNum,NumberWithCommas);
	}
}
