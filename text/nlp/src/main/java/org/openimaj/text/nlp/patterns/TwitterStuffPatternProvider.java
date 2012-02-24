package org.openimaj.text.nlp.patterns;

import java.util.regex.Pattern;

import org.openimaj.text.util.RegexUtil;



/**
 * Borrowed heavily from https://github.com/twitter/twitter-text-java
 * @author ss
 *
 */
public class TwitterStuffPatternProvider extends PatternProvider{
	
	// These constants were lifted directly from the twitter regex class file mentioned above
	private static String LATIN_ACCENTS_CHARS = "\\u00c0-\\u00d6\\u00d8-\\u00f6\\u00f8-\\u00ff\\u015f";
	private static final String HASHTAG_ALPHA_CHARS = "a-zA-Z" + LATIN_ACCENTS_CHARS +
    "\\u0400-\\u04ff\\u0500-\\u0527" +  // Cyrillic
    "\\u2de0-\\u2dff\\ua640-\\ua69f" +  // Cyrillic Extended A/B
    "\\u1100-\\u11ff\\u3130-\\u3185\\uA960-\\uA97F\\uAC00-\\uD7AF\\uD7B0-\\uD7FF" + // Hangul (Korean)
    "\\p{InHiragana}\\p{InKatakana}" +  // Japanese Hiragana and Katakana
    "\\p{InCJKUnifiedIdeographs}" +     // Japanese Kanji / Chinese Han
    "\\u3005\\u303b" +                  // Kanji/Han iteration marks
    "\\uff21-\\uff3a\\uff41-\\uff5a" +  // full width Alphabet
    "\\uff66-\\uff9f" +                 // half width Katakana
    "\\uffa1-\\uffdc";                  // half width Hangul (Korean)
	private static final String HASHTAG_ALPHA_NUMERIC_CHARS = "0-9\\uff10-\\uff19_-" + HASHTAG_ALPHA_CHARS;
	private static final String HASHTAG_ALPHA = "[" + HASHTAG_ALPHA_CHARS +"]";
	private static final String HASHTAG_ALPHA_NUMERIC = "[" + HASHTAG_ALPHA_NUMERIC_CHARS +"]";
	private static String AT_SIGNS_CHARS = "@\uFF20";
	private  static final Pattern AT_SIGNS = Pattern.compile("[" + AT_SIGNS_CHARS + "]");
	
	
	String linkHashtag = "(?:#|\uFF03)(?:" + HASHTAG_ALPHA_NUMERIC + "*" + HASHTAG_ALPHA + HASHTAG_ALPHA_NUMERIC + "*)";
	String linkUsernames = "(?:" + AT_SIGNS + "+)([a-z0-9_]{1,20})(/[a-z][a-z0-9_\\-]{0,24})?(?=[^a-zA-Z0-9_])";
	String retweet = "(?:(\\b)RT:?(\\b))";
	
	@Override
	public String patternString() {
		return RegexUtil.regex_or(linkUsernames,linkHashtag,retweet);
	}
	
	@Override
	public Pattern pattern(){
		return Pattern.compile(patternString(), Pattern.CASE_INSENSITIVE);
	}
}
