package org.openimaj.text.nlp.patterns;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public abstract class PatternProvider {
	protected static String regex_or(String ... items )
	{
		String r = StringUtils.join(items, "|");
		r = '(' + r + ')';
		return r;
	}
	protected static String regex_or(PatternProvider ... patterns) {
		String[] allpat = new String[patterns.length];
		int i = 0;
		for (PatternProvider patternProvider : patterns) {
			allpat[i++] = patternProvider.patternString();
		}
		return regex_or(allpat);
	}
	
	public String pos_lookahead(String r){
		return "(?=" + r + ')';
	}
		
	public String neg_lookahead(String r) {
		return "(?!" + r + ')';
	}
	public String optional(String r){
		return String.format("(%s)?",r);
	}
	
	
	
	public abstract String patternString();
	public Pattern pattern() {
		return Pattern.compile(patternString());
	}
	public PatternProvider combine(PatternProvider other) {
		return new CombinedPatternProvider(this,other);
	}
}
