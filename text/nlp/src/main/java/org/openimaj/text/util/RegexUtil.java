package org.openimaj.text.util;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openimaj.text.nlp.patterns.PatternProvider;

public class RegexUtil {
	public static String regex_or(String ... items )
	{
		String r = StringUtils.join(items, "|");
		r = '(' + r + ')';
		return r;
	}
	public static String regex_or(PatternProvider ... patterns) {
		String[] allpat = new String[patterns.length];
		int i = 0;
		for (PatternProvider patternProvider : patterns) {
			allpat[i++] = patternProvider.patternString();
		}
		return regex_or(allpat);
	}
	
	public static String regex_or(List<String> items) {
		String r = StringUtils.join(items, "|");
		r = '(' + r + ')';
		return r;
	}
	
	public static String regex_char_neg(List<String> puncs) {
		String r = StringUtils.join(puncs, "");
		r = '[' + r + ']';
		return r;
	}
	
	public static String pos_lookahead(String r){
		return "(?=" + r + ')';
	}
		
	public static String neg_lookahead(String r) {
		return "(?!" + r + ')';
	}
	public static String optional(String r){
		return String.format("(%s)?",r);
	}
}
