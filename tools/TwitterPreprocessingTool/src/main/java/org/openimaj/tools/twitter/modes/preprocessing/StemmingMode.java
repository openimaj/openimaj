package org.openimaj.tools.twitter.modes.preprocessing;

import gov.sandia.cognition.text.term.filter.stem.PorterEnglishStemmingFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.openimaj.text.nlp.language.LanguageDetector.WeightedLocale;
import org.openimaj.twitter.TwitterStatus;
import org.tartarus.martin.Stemmer;
import org.terrier.terms.EnglishSnowballStemmer;

/**
 * A gateway class which loads and uses the #PorterEnglishStemmingFilter
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class StemmingMode implements TwitterPreprocessingMode<List<String>> {
	
	final static String STEMMED = "stemmed";
	private TwitterPreprocessingMode<Map<String,Object>> langMode;
	private TwitterPreprocessingMode<Map<String,List<String>>> tokMode;
	private EnglishSnowballStemmer stemmer;

	/**
	 * Loads the language detector
	 * @throws IOException 
	 */
	public StemmingMode() throws IOException {
		try {
			langMode = TwitterPreprocessingModeOption.LANG_ID.createMode();
			tokMode = TwitterPreprocessingModeOption.TOKENISE.createMode();
			stemmer = new EnglishSnowballStemmer(null);
		} catch (Exception e) {
			throw new IOException("Couldn't create required language detector and tokeniser",e);
		}
	}

	@Override
	public List<String> process(TwitterStatus twitterStatus) {
		List<String> stems = new ArrayList<String>();
		try {
			Map<String,Object> localeMap = TwitterPreprocessingModeOption.LANG_ID.results(twitterStatus,langMode);
			WeightedLocale locale = WeightedLocale.fromMap(localeMap);
			if(locale.getLocale().equals(Locale.ENGLISH)){
				Map<String,List<String>> tokens = TwitterPreprocessingModeOption.TOKENISE.results(twitterStatus,tokMode);
				HashSet<String> protectedToks = new HashSet<String>();
				protectedToks.addAll(tokens.get(TokeniseMode.TOKENS_PROTECTED));
				for (String token : tokens.get(TokeniseMode.TOKENS_ALL)) {
					if(! protectedToks.contains(token)) {
						stems.add(stemmer.stem(token));
					}
					else{
						stems.add(token);
					}
					
				}
			}
		} catch (Exception e) { }
		twitterStatus.addAnalysis(STEMMED, stems);
		return stems;	
		
	}

}
