package org.openimaj.text.nlp.patterns;

public class TimePatternProvider extends PatternProvider{
	String Timelike = "\\d+:\\d+h{0,1}"; // removes the h trailing the hour like in 18:00h
	@Override
	public String patternString() {
		return Timelike;
	}

}
