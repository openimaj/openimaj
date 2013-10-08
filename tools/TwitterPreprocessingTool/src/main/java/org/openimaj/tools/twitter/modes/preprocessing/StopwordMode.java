/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.tools.twitter.modes.preprocessing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.openimaj.io.FileUtils;
import org.openimaj.text.nlp.language.LanguageDetector.WeightedLocale;
import org.openimaj.twitter.USMFStatus;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class StopwordMode extends TwitterPreprocessingMode<List<String>> {

	private static final String STOPWORDS_KEY = "nostopwords";
	private static final String[][] STOPWORD_FILES = {
		new String[]{"en","/org/openimaj/text/stopwords/stopwords-list.txt"},
		new String[]{"en","/org/openimaj/text/stopwords/en_stopwords.txt"},
		new String[]{"en","/org/openimaj/text/stopwords/en_dokuwiki_stopwords.txt"},
		new String[]{"bg","/org/openimaj/text/stopwords/bg_dokuwiki_stopwords.txt"},
		new String[]{"da","/org/openimaj/text/stopwords/da_dokuwiki_stopwords.txt"},
		new String[]{"de","/org/openimaj/text/stopwords/de_dokuwiki_stopwords.txt"},
		new String[]{"el","/org/openimaj/text/stopwords/el_dokuwiki_stopwords.txt"},
		new String[]{"es","/org/openimaj/text/stopwords/es_dokuwiki_stopwords.txt"},
		new String[]{"fi","/org/openimaj/text/stopwords/fi_dokuwiki_stopwords.txt"},
		new String[]{"fr","/org/openimaj/text/stopwords/fr_dokuwiki_stopwords.txt"},
		new String[]{"it","/org/openimaj/text/stopwords/it_dokuwiki_stopwords.txt"},
		new String[]{"nl","/org/openimaj/text/stopwords/nl_dokuwiki_stopwords.txt"},
		new String[]{"pt","/org/openimaj/text/stopwords/pt_dokuwiki_stopwords.txt"},
		new String[]{"sv","/org/openimaj/text/stopwords/sv_dokuwiki_stopwords.txt"},
	};
	private LanguageDetectionMode langMode;
	private TokeniseMode tokMode;
	private HashMap<String, HashSet<String>> languageStopwords;

	/**
	 * @throws IOException
	 */
	public StopwordMode() throws IOException {
		langMode = new LanguageDetectionMode();
		tokMode = new TokeniseMode();
		languageStopwords = loadStopwords();
	}

	private HashMap<String, HashSet<String>> loadStopwords() {
		HashMap<String,HashSet<String>> retMap = new HashMap<String,HashSet<String>>();
		for (String[] swLangFile: STOPWORD_FILES) {
			try {
				HashSet<String> ret = new HashSet<String>();
				String[] swLines = FileUtils.readlines(StopwordMode.class.getResourceAsStream(swLangFile[1]),"UTF-8");
				for (String sw : swLines) {
					if(sw.startsWith("#")) continue;
					ret.add(sw.toLowerCase().trim());
				}
				retMap.put(swLangFile[0], ret);
			} catch (IOException e) {	}
		}
		return retMap;
	}

	@Override
	public List<String> process(USMFStatus twitterStatus) {
		List<String> nonstopwords = new ArrayList<String>();
		try {
			Map<String,Object> localeMap = TwitterPreprocessingMode.results(twitterStatus,langMode);
			WeightedLocale locale = WeightedLocale.fromMap(localeMap);
			String country = locale.language.toLowerCase();
			Map<String,List<String>> tokens = TwitterPreprocessingMode.results(twitterStatus,tokMode);

			if(!languageStopwords.containsKey(country)){
				// We don't know stopwords for this language, all the tokens become the non-stopwords!
				nonstopwords.addAll(tokens.get(TokeniseMode.TOKENS_ALL));
			}
			else{
				HashSet<String> protectedToks = new HashSet<String>();
				protectedToks.addAll(tokens.get(TokeniseMode.TOKENS_PROTECTED));
				HashSet<String> stopwords = languageStopwords.get(country);
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
