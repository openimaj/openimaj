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
package org.openimaj.text.nlp.sentiment.lexicon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.ml.clustering.DoubleCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.DoubleKMeans;
import org.openimaj.text.nlp.textpipe.annotations.POSAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.RawTextAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.SentenceAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.TokenAnnotation;
import org.openimaj.text.nlp.textpipe.annotators.MissingRequiredAnnotationException;
import org.openimaj.text.nlp.textpipe.annotators.OpenNLPPOSAnnotator;
import org.openimaj.text.nlp.textpipe.annotators.OpenNLPSentenceAnnotator;
import org.openimaj.text.nlp.textpipe.annotators.OpenNLPTokenAnnotator;
import org.openimaj.util.pair.IntDoublePair;

/**
 * An implementation of Hatzivassiloglou and McKeown's approach to a
 * semisupervised method of building a bipolar sentiment lexicon. This is a one
 * pass version, in that the corpus to build from is fixed.
 * 
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * 
 */
public class TotalLexBuilder {
	private HashMap<String, double[]> vectors;
	private HashMap<String, Integer> assignments;
	private List<String> posConfirmation;
	private List<String> negConfirmation;
	private OpenNLPTokenAnnotator tokA;
	private OpenNLPPOSAnnotator posA;
	private OpenNLPSentenceAnnotator sentA;
	private HashMap<String, List<HashMap<String, Counter>>> counts;
	private int AND = 0, BUT = 1;

	/**
	 * Constructor.
	 * 
	 * @param posConfirmation
	 *            = list of positive adjectives used to orient the
	 *            classification.
	 * @param negConfirmation
	 *            = list of negative adjectives used to orient the
	 *            classification.
	 */
	public TotalLexBuilder(List<String> posConfirmation,
			List<String> negConfirmation)
	{
		this.posConfirmation = posConfirmation;
		this.negConfirmation = negConfirmation;
		tokA = new OpenNLPTokenAnnotator();
		posA = new OpenNLPPOSAnnotator();
		sentA = new OpenNLPSentenceAnnotator();
		this.counts = new HashMap<String, List<HashMap<String, Counter>>>();
	}

	/**
	 * Builds a Scored Sentiment mapping of adjectives from the corpus.
	 * 
	 * @param corpus
	 * @return Scored Sentiment map of adjectives.
	 */
	public Map<String, Double> build(List<String> corpus) {
		// Find all the adjective conjunctions.
		for (final String doc : corpus) {
			getAdjectiveConjunctions(doc, " and ");
			getAdjectiveConjunctions(doc, " but ");
		}
		// Build the vectors for each adjective
		buildVectors();
		normaliseVectors();
		cluster();
		return null;
	}

	private void cluster() {
		final DoubleKMeans fkm = DoubleKMeans.createExact(counts.keySet().size(), 2);
		final double[][] data = new double[counts.keySet().size()][];
		int i = 0;
		for (final double[] ds : vectors.values()) {
			data[i] = ds;
			i++;
		}
		final DoubleCentroidsResult cluster = fkm.cluster(data);
		final HardAssigner<double[], double[], IntDoublePair> assigner = cluster.defaultHardAssigner();
		assignments = new HashMap<String, Integer>();
		for (final String adj : vectors.keySet()) {
			assignments.put(adj, assigner.assign(vectors.get(adj)));
		}
		for (final String adj : assignments.keySet()) {
			System.out.println(adj + " " + assignments.get(adj));
		}
	}

	private void normaliseVectors() {

	}

	private void buildVectors() {
		vectors = new HashMap<String, double[]>();
		for (final String adj : counts.keySet()) {
			vectors.put(adj, new double[counts.keySet().size() * 2]);
			final HashMap<String, Counter> andCount = counts.get(adj).get(AND);
			final HashMap<String, Counter> butCount = counts.get(adj).get(BUT);
			int i = 0;
			for (final String adjInc : counts.keySet()) {
				if (andCount.containsKey(adjInc)) {
					vectors.get(adj)[i] = andCount.get(adjInc).count;
				} else {
					vectors.get(adj)[i] = 0;
				}
				if (butCount.containsKey(adjInc)) {
					vectors.get(adj)[(i + counts.keySet().size())] = butCount
							.get(adjInc).count;
				} else {
					vectors.get(adj)[(i + counts.keySet().size())] = 0;
				}
				i++;
			}
		}
	}

	private void getAdjectiveConjunctions(String toSearch, String conjunction) {
		String leftToSearch = toSearch;
		if (leftToSearch.contains(conjunction)) {
			final RawTextAnnotation rta = new RawTextAnnotation(toSearch);
			try {
				sentA.annotate(rta);
				tokA.annotate(rta);
				posA.annotate(rta);
			} catch (final MissingRequiredAnnotationException e) {
				e.printStackTrace();
			}
			final List<SentenceAnnotation> sentences = rta
					.getAnnotationsFor(SentenceAnnotation.class);
			int searchedIndex = 0;
			int sentI = 0;
			while (leftToSearch.contains(conjunction)) {
				final int loc = leftToSearch.indexOf(conjunction) + 1 + searchedIndex;
				List<TokenAnnotation> tokens = null;
				int t = 0;
				searchloop: for (int s = sentI; s < sentences.size(); s++) {
					final SentenceAnnotation sentence = sentences.get(s);
					if (sentence.start < loc && sentence.stop > loc) {
						tokens = sentence
								.getAnnotationsFor(TokenAnnotation.class);
						for (t = 0; t < tokens.size(); t++) {
							if (tokens.get(t).start + sentence.start == loc) {
								break searchloop;
							}
						}
					}
					sentI++;
				}
				if (tokens != null) {
					final int c = (conjunction.trim().equals("and")) ? AND : BUT;
					checkForConjunctionGroup(tokens, t, c);
				}
				searchedIndex = loc + conjunction.length() - 1;
				leftToSearch = toSearch.substring(searchedIndex);
			}
		}
	}

	private void checkForConjunctionGroup(List<TokenAnnotation> tokens,
			int conjunctionIndex, int conjunction)
	{
		if (tokens.size() > conjunctionIndex + 1
				&& tokens.get(conjunctionIndex - 1)
						.getAnnotationsFor(POSAnnotation.class).get(0).pos
						.toString().contains("JJ")
				&& tokens.get(conjunctionIndex + 1)
						.getAnnotationsFor(POSAnnotation.class).get(0).pos
						.toString().contains("JJ"))
		{

			System.out.println(tokens.get(conjunctionIndex - 1)
					.getStringToken()
					+ " "
					+ tokens.get(conjunctionIndex).getStringToken()
					+ " "
					+ tokens.get(conjunctionIndex + 1).getStringToken());

			final List<String> adjectives = new ArrayList<String>();
			adjectives.add(tokens.get(conjunctionIndex - 1).getStringToken());
			adjectives.add(tokens.get(conjunctionIndex + 1).getStringToken());
			for (int i = 0; i < adjectives.size(); i++) {
				final String vecAdj = adjectives.get(i);
				if (!counts.keySet().contains(vecAdj)) {
					final ArrayList<HashMap<String, Counter>> cons = new ArrayList<HashMap<String, Counter>>();
					cons.add(new HashMap<String, Counter>());
					cons.add(new HashMap<String, Counter>());
					counts.put(vecAdj, cons);
				}
				final HashMap<String, Counter> conVector = counts.get(vecAdj).get(
						conjunction);
				for (int j = 0; j < adjectives.size(); j++) {
					final String incAdj = adjectives.get(j);
					if (!conVector.containsKey(incAdj)) {
						conVector.put(incAdj, new Counter());
					} else
						conVector.get(incAdj).inc();
				}
			}
		}
	}

	/**
	 * Easily incremented object for counting.
	 * 
	 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
	 * 
	 */
	public class Counter {
		public double count;

		public Counter() {
			count = 1.0;
		}

		public void inc() {
			count += 1;
		}
	}

	/**
	 * Quick tester for class
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		final ArrayList<String> pos = new ArrayList<String>();
		final ArrayList<String> neg = new ArrayList<String>();
		final ArrayList<String> corpus = new ArrayList<String>();
		pos.add("dandy");
		neg.add("horrible");

		corpus.add("Hello, this day is just fine and dandy, I wonder if it is going to turn horrible and sad?."
				+ " Hopefully not. "
				+ "Then again, if you are fine and warm inside, it would not make a difference. "
				+ "Unless a dandy but horrible wolf came along. "
				+ "Then we would be  be warm but sad inside. "
				+ "Our only option would be to offer the sad and horrible wolf the opportunity to be warm and dandy." +
				"warm and fine. dandy and warm. fine but horrible. dandy but sad. sad and horrible." +
				"fine and warm, fine and dandy, fine and warm, fine and dandy");

		// corpus.add("fine and warm");
		final TotalLexBuilder b = new TotalLexBuilder(pos, neg);
		b.build(corpus);
	}

}
