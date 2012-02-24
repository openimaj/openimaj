package org.openimaj.tools.twitter.modes.preprocessing;

import java.io.IOException;
import java.util.Map;

import org.openimaj.text.nlp.language.LanguageDetector;
import org.openimaj.twitter.TwitterStatus;

/**
 * A gateway class which loads and uses the #LanguageDetector
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class LanguageDetectionMode implements TwitterPreprocessingMode<Map<String,Object>> {
	
	private LanguageDetector detector;
	final static String LANGUAGES = "langid";

	/**
	 * Loads the language detector
	 * @throws IOException 
	 */
	public LanguageDetectionMode() throws IOException {
		detector = new LanguageDetector();
	}

	@Override
	public Map<String,Object> process(TwitterStatus twitterStatus) {
		Map<String,Object> language = null;
		try {
			language = detector.classify(twitterStatus.text).asMap();
			
		} catch (Exception e) {
		}
		twitterStatus.addAnalysis(LANGUAGES, language);
		return language;	
		
	}
}
