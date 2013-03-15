package org.openimaj.text.nlp.patterns;

/**
 * Some tweets end with ... and when the tweet ends with a URL there are some horrible issues
 * with URLs being detected as emoticons. Terrible.
 *
 * So we deal with that here!
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TruncatedURLPatternProvider extends PatternProvider {

	String TrunctedURL = "http[:]/+[\\w(?:.)]*(?:\\s)?[.]{3}";

	@Override
	public String patternString() {
		return TrunctedURL;
	}

}
