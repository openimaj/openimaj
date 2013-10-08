/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
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
package org.openimaj.demos.sandbox.ml.linear.learner.stream.twitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.util.filter.FilterUtils;
import org.openimaj.util.function.Function;
import org.openimaj.util.function.Predicate;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class USMFStatusBagOfWords implements Function<List<USMFStatus>,Map<String,Map<String,Double>>> {

	private TwitterPreprocessingMode<List<String>> mode;
	private Predicate<String> junkWords;
	private NameStrategy userStrat;

	/**
	 * @param mode the mode from which to grab words
	 */
	public USMFStatusBagOfWords(TwitterPreprocessingMode<List<String>> mode) {
		this.mode = mode;
		this.userStrat = new UserNameStrategy();
		initJunkWords();
	}

	/**
	 * @param mode the mode from which to grab words
	 * @param userNameStrategy
	 */
	public USMFStatusBagOfWords(TwitterPreprocessingMode<List<String>> mode, NameStrategy userNameStrategy) {
		this.mode = mode;
		this.userStrat = userNameStrategy;
		initJunkWords();
	}



	private void initJunkWords() {
		this.junkWords = new Predicate<String>() {

			@Override
			public boolean test(String object) {
				String lowerCase = object.toLowerCase();
				if( lowerCase.length()<=2 ||
					lowerCase.contains("http") ||
					lowerCase.startsWith("@") ||
					lowerCase.startsWith("#") ||
					lowerCase.startsWith("$")
				) {
					return false;
				}
				else {
					return true;
				}
			}
		};
	}

	@Override
	public Map<String,Map<String,Double>> apply(List<USMFStatus> in) {
		Map<String, Map<String, Double>> ret = new HashMap<String, Map<String,Double>>();
		Map<String,Double> userTotals = new HashMap<String, Double>();
		for (USMFStatus usmfStatus : in) {
			String userName = this.userStrat.createName(usmfStatus);
			Map<String, Double> userWordCounts = userWordCounts(ret, userTotals,userName);

			try {
				List<String> words = TwitterPreprocessingMode.results(usmfStatus, mode);
				words = FilterUtils.filter(words, junkWords);
				userTotals.put(userName, userTotals.get(userName) + words.size());
				for (String word : words) {
					Double currentWordCount = userWordCounts.get(word);
					if(currentWordCount == null) userWordCounts.put(word, 1d);
					else userWordCounts.put(word, currentWordCount + 1d);
				}

			} catch (Exception e) {

			}
		}
		for (Entry<String, Map<String, Double>> entry: ret.entrySet()) {
			Map<String, Double> userWords = entry.getValue();
			String userName = entry.getKey();
			for (String word : userWords.keySet()) {
				userWords.put(word, userWords.get(word) / userTotals.get(userName));
			}
		}

		return ret;
	}

	private Map<String, Double> userWordCounts(Map<String, Map<String, Double>> ret, Map<String,Double> totals, String userName) {
		Map<String, Double> wordCounts = ret.get(userName);
		if(wordCounts == null) {
			ret.put(userName, wordCounts = new HashMap<String, Double>());
			totals.put(userName, 0d);
		}
		return wordCounts;
	}

}
