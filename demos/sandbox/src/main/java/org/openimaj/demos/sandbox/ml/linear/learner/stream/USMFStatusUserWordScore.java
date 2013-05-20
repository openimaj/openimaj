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
import org.openimaj.util.stream.window.Aggregation;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <T>
 *
 */
public class USMFStatusUserWordScore<T extends Aggregation<List<USMFStatus>,Long>> implements Function<T,Aggregation<Map<String,Map<String,Double>>,Long>> {

	private TwitterPreprocessingMode<List<String>> mode;
	private Predicate<String> junkWords;

	/**
	 * @param mode the mode from which to grab words
	 */
	public USMFStatusUserWordScore(TwitterPreprocessingMode<List<String>> mode) {
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
	public Aggregation<Map<String,Map<String,Double>>,Long> apply(T in) {
		Map<String, Map<String, Double>> ret = new HashMap<String, Map<String,Double>>();
		Map<String,Double> userTotals = new HashMap<String, Double>();
		for (USMFStatus usmfStatus : in.getPayload()) {
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

		return new Aggregation<Map<String,Map<String,Double>>, Long>(ret, in.getMeta()) ;
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
