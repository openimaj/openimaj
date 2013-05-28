package org.openimaj.demos.sandbox.ml.linear.learner.stream;

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

	/**
	 * @param mode the mode from which to grab words
	 */
	public USMFStatusBagOfWords(TwitterPreprocessingMode<List<String>> mode) {
		this.mode = mode;
		this.junkWords = new Predicate<String>() {

			@Override
			public boolean test(String object) {
				String lowerCase = object.toLowerCase();
				if(
					lowerCase.length()<=2 ||
					lowerCase.contains("http") ||
					lowerCase.startsWith("@") ||
					lowerCase.startsWith("#") ||
					lowerCase.startsWith("$")

				)return false;
				return true;
			}
		};
	}

	@Override
	public Map<String,Map<String,Double>> apply(List<USMFStatus> in) {
		Map<String, Map<String, Double>> ret = new HashMap<String, Map<String,Double>>();
		Map<String,Double> userTotals = new HashMap<String, Double>();
		for (USMFStatus usmfStatus : in) {
			String userName = usmfStatus.user.name;
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
