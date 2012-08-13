package org.openimaj.text.nlp.namedentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openimaj.feature.FeatureExtractor;

/**
 * A feature extractor which a list of TF-IDFs for a list of string
 * 
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class TfIdfFeatureExtractor implements FeatureExtractor<ArrayList<Double>, List<String>> {

	private final HashMap<String, ArrayList<Double>> docTfIdfVectors;
	private final HashMap<String, Double> termIDF;
	HashMap<String, TermCounter> counters;
	TermCounter global;

	/**
	 * @param tokenisedCorpus
	 *            a map of documents to lists of strings
	 */
	public TfIdfFeatureExtractor(Map<String, List<String>> tokenisedCorpus) {
		this.docTfIdfVectors = new HashMap<String, ArrayList<Double>>();
		this.termIDF = new HashMap<String, Double>();
		counters = new HashMap<String, TfIdfFeatureExtractor.TermCounter>();
		global = new TermCounter();

		// Count all the terms in each document and in the corpus.
		for (final String me : tokenisedCorpus.keySet()) {
			counters.put(me, new TermCounter());
			for (final String term : tokenisedCorpus.get(me)) {
				addNullToAllExceptMe(term, me);
			}
		}

		// Calculate the IDF for each term.
		for (final String term : global.termCount.keySet()) {
			termIDF.put(term, calculateIdfOf(term));
		}

		// Build the TF/IDF vectors for each document
		for (final String me : tokenisedCorpus.keySet()) {
			final ArrayList<Double> vector = new ArrayList<Double>();
			for (final String term : global.termCount.keySet()) {
				vector.add(calculateTfOf(term, me) * termIDF.get(term));
			}
			docTfIdfVectors.put(me, vector);
		}
	}

	private double calculateTfOf(String term, String docKey) {
		if (counters.get(docKey).termCount.get(term).count > 0) {
			final double count = counters.get(docKey).termCount.get(term).count;
			final double total = counters.get(docKey).getTotal();
			return count / total;
		}
		return 0;
	}

	private double calculateIdfOf(String term) {
		int docCount = 0;
		for (final String docKey : counters.keySet()) {
			if (counters.get(docKey).termCount.get(term).count > 0)
				docCount++;
		}
		return Math.log(counters.size() / (1 + docCount));
	}

	@Override
	public ArrayList<Double> extractFeature(List<String> object) {
		final RestrictedTermCounter tc = new RestrictedTermCounter(global.termCount.keySet());
		for (final String term : object) {
			tc.incrementTerm(term);
		}
		final ArrayList<Double> vector = new ArrayList<Double>();
		for (final String term : global.termCount.keySet()) {
			if (tc.termCount.get(term).count > 0) {
				final double count = tc.termCount.get(term).count;
				final double total = tc.getTotal();
				final double tf = count / total;
				vector.add(tf * termIDF.get(term));
			} else
				vector.add(0.0);
		}
		return null;
	}

	private void addNullToAllExceptMe(String term, String me) {
		for (final String key : counters.keySet()) {
			if (key.equals(me))
				counters.get(key).addIncrementTerm(term);
			else
				(counters.get(key)).addNullTerm(term);
		}
		global.addIncrementTerm(term);
	}

	/**
	 * Used to keep counts of particular terms
	 * 
	 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class TermCounter {
		/**
		 * Terms to counter instances
		 */
		public LinkedHashMap<String, Counter> termCount;
		private double total = 0;

		/**
		 * instantiate the {@link TermCounter#termCount}
		 */
		public TermCounter() {
			termCount = new LinkedHashMap<String, Counter>();
		}

		/**
		 * @return total count of terms
		 */
		public double getTotal() {
			return total;
		}

		/**
		 * @param term
		 *            increment a given term's counter
		 */
		public void addIncrementTerm(String term) {
			if (termCount.containsKey(term)) {
				termCount.get(term).increment();
			} else {
				final Counter c = new Counter();
				c.increment();
				termCount.put(term, c);
			}
			total++;
		}

		/**
		 * @param term
		 *            instantiate a term's counter to 0
		 */
		public void addNullTerm(String term) {
			if (!termCount.containsKey(term))
				termCount.put(term, new Counter());
		}
	}

	/**
	 * Count the terms withn a specified vocabulary
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class RestrictedTermCounter {
		/**
		 * the counted terms
		 */
		public LinkedHashMap<String, Counter> termCount;
		private double total = 0;

		/**
		 * @param vocabulary
		 *            the allowed vocabulary (a counter is instantiated for each
		 *            item in the vocabulary)
		 */
		public RestrictedTermCounter(Set<String> vocabulary) {
			termCount = new LinkedHashMap<String, Counter>();
			for (final String term : vocabulary) {
				termCount.put(term, new Counter());
			}
		}

		/**
		 * @return the total number of terms seen (including terms not in the
		 *         vocabulary)
		 */
		public double getTotal() {
			return total;
		}

		/**
		 * @param term
		 *            increment a terms
		 */
		public void incrementTerm(String term) {
			if (termCount.containsKey(term))
				termCount.get(term).increment();
			total++;
		}
	}

	/**
	 * A counter
	 * 
	 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class Counter {
		private int count;

		/**
		 * instantiate the count
		 */
		public Counter() {
			this.count = 0;
		}

		/**
		 * increment the count
		 */
		public void increment() {
			count++;
		}

		/**
		 * @return current count
		 */
		public int count() {
			return count;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final TermCounter t = new TermCounter();
		for (final String term : "what what do not get confused what".split(" ")) {
			t.addIncrementTerm(term);
		}
		for (final String term : t.termCount.keySet()) {
			System.out.println(t.termCount.get(term).count());
		}
	}

}
