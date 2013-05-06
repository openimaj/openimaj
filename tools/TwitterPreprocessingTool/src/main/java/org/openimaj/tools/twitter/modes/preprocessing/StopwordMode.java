package org.openimaj.tools.twitter.modes.preprocessing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.openimaj.io.FileUtils;
import org.openimaj.text.nlp.language.LanguageDetector.WeightedLocale;
import org.openimaj.twitter.USMFStatus;
import org.tartarus.snowball.ext.EnglishStemmer;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class StopwordMode extends TwitterPreprocessingMode<List<String>> {

	private static final String STOPWORDS_KEY = "nostopwords";
	private static final String[] STOPWORD_FILES = {
		"/org/openimaj/text/stopwords/stopwords-list.txt",
		"/org/openimaj/text/stopwords/en_stopwords.txt",
	};
	private LanguageDetectionMode langMode;
	private TokeniseMode tokMode;
	private Set<String> stopwords;
	
	/**
	 * @throws IOException
	 */
	public StopwordMode() throws IOException {
		langMode = new LanguageDetectionMode();
		tokMode = new TokeniseMode();
		stopwords = loadStopwords();
	}

	private Set<String> loadStopwords() {
		HashSet<String> ret = new HashSet<String>();
		for (String swFile: STOPWORD_FILES) {
			try {
				String[] swLines = FileUtils.readlines(StopwordMode.class.getResourceAsStream(swFile));
				for (String sw : swLines) {
					ret.add(sw.toLowerCase().trim());
				}
			} catch (IOException e) {	}
		}
		return ret;
	}

	@Override
	public List<String> process(USMFStatus twitterStatus) {
		List<String> nonstopwords = new ArrayList<String>();
		try {
			Map<String,Object> localeMap = TwitterPreprocessingMode.results(twitterStatus,langMode);
			WeightedLocale locale = WeightedLocale.fromMap(localeMap);
			if(locale.getLocale().equals(Locale.ENGLISH)){
				Map<String,List<String>> tokens = TwitterPreprocessingMode.results(twitterStatus,tokMode);
				HashSet<String> protectedToks = new HashSet<String>();
				protectedToks.addAll(tokens.get(TokeniseMode.TOKENS_PROTECTED));
				for (String token : tokens.get(TokeniseMode.TOKENS_ALL)) {
					if(!protectedToks.contains(token)) {
						if(!stopwords.contains(token.toLowerCase()))
							nonstopwords.add(token);
					}
					else{
						nonstopwords.add(token);
					}
				}
			}
		} catch (Exception e) { }
		twitterStatus.addAnalysis(STOPWORDS_KEY, nonstopwords);
		return nonstopwords;	
	}

	@Override
	public String getAnalysisKey() {
		return STOPWORDS_KEY;
	}

}
