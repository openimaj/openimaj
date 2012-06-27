package org.openimaj.tools.twitter.modes.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.kohsuke.args4j.Option;
import org.openimaj.text.nlp.language.LanguageDetector.WeightedLocale;
import org.openimaj.tools.twitter.modes.preprocessing.LanguageDetectionMode;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;
import org.openimaj.twitter.USMFStatus;

public class LanguageFilter extends TwitterPreprocessingFilter {
	
	@Option(name="--accept-language", aliases="-l", required=false, usage="Using detected language, accept these languages", metaVar="STRING", multiValued=true)
	List<String> localeMatch = new ArrayList<String>();
	
	private LanguageDetectionMode langMode;

	@Override
	public boolean filter(USMFStatus twitterStatus) {
		try {
			Map<String,Object> localeMap = TwitterPreprocessingMode.results(twitterStatus,langMode);
			WeightedLocale locale = WeightedLocale.fromMap(localeMap);
			for (String lang : this.localeMatch) {
				if(locale.language.equals(lang)) return true;
			}
			
			return false;
		} catch (Exception e) {
			return false;
		}
	}
	
	@Override
	public void validate() {
		try {
			this.langMode = new LanguageDetectionMode();
		} catch (IOException e) {
		}
	}

}
