package org.openimaj.text.nlp.patterns;

import org.openimaj.text.util.RegexUtil;

/**
 * Regex for numbers with a decimal point or command separated. Greedy search, won't stop with 10,000 in the number 10,000,000.
 * 
 * I
 * @author ss
 *
 */
public class ComplicatedNumberPatternProvider extends PatternProvider{
	private static final String[] after_number = new String[]{"[^\\d,\\-a-z.]","$"};
	private static final String[] number_ends = new String[]{"\\.\\d+"};
//	String Number = "(?:\\b|[$])\\d+" + pos_lookahead(regex_or("[^,\\d]","$"));
	
	String NumNum = String.format("(?:\\b|[$])\\d+%s",RegexUtil.regex_or(number_ends))+ RegexUtil.pos_lookahead(RegexUtil.regex_or(after_number));
	String NumberWithCommas = "(?:\\b|[$])(?:\\d+,)+\\d{3}(?:.\\d+)?"+ RegexUtil.pos_lookahead(RegexUtil.regex_or(after_number));
	
	@Override
	public String patternString() {
		return RegexUtil.regex_or(NumNum,NumberWithCommas);
	}
}
