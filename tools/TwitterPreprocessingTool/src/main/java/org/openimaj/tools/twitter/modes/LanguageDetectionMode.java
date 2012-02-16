package org.openimaj.tools.twitter.modes;

import java.io.IOException;

import org.openimaj.text.nlp.language.LanguageDetector;
import org.openimaj.text.nlp.language.LanguageDetector.WeightedLocale;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;
import org.openimaj.twitter.TwitterStatus;

/**
 * A gateway class which loads and uses the #LanguageDetector
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class LanguageDetectionMode implements TwitterPreprocessingMode {
	
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
	public void process(TwitterStatus twitterStatus) {
		
		try {
			WeightedLocale language = detector.classify(twitterStatus.text);
			twitterStatus.addAnalysis(LANGUAGES, language.asMap());
		} catch (Exception e) {
			twitterStatus.addAnalysis(LANGUAGES, null);
		}	
		
	}

}
