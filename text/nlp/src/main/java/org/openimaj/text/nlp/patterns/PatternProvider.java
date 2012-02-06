package org.openimaj.text.nlp.patterns;

import java.util.List;
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
	
	protected static String regex_or(List<String> items) {
		String r = StringUtils.join(items, "|");
		r = '(' + r + ')';
		return r;
	}
	
	protected static String regex_char_neg(List<String> puncs) {
		String r = StringUtils.join(puncs, "");
		r = '[' + r + ']';
		return r;
	}
	
	protected static String pos_lookahead(String r){
		return "(?=" + r + ')';
	}
		
	protected static String neg_lookahead(String r) {
		return "(?!" + r + ')';
	}
	protected static String optional(String r){
		return String.format("(%s)?",r);
	}
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
