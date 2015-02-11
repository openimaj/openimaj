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
package org.openimaj.text.nlp.sentiment.model.classifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.ml.annotation.AbstractAnnotator;
import org.openimaj.ml.annotation.bayes.NaiveBayesAnnotator;

/**
 * {@link FeatureExtractor} that is suitable for {@link NaiveBayesAnnotator}.
 * Should be initialized with training corpus of the machine learning
 * {@link AbstractAnnotator} you are using.
 *
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 *
 */
public class GeneralSentimentFeatureExtractor implements
FeatureExtractor<DoubleFV, List<String>>
{

	private ArrayList<String> vocabList;
	private int wordOccuranceThresh = 50;

	/**
	 * Construct with the training set. This is required to build a vocabulary.
	 *
	 * @param domainVocabularyCorpus
	 *            list of tokenised corpus documents.
	 */
	public GeneralSentimentFeatureExtractor(
			List<List<String>> domainVocabularyCorpus)
	{
		initialize(domainVocabularyCorpus);
	}

	/**
	 * Blank constructor. Will require initialize to be called at a later stage.
	 */
	public GeneralSentimentFeatureExtractor() {

	}

	/**
	 * Allows a new vocabulary to be constructed from a new corpus.
	 *
	 * @param domainVocabularyCorpus
	 *            list of tokenised corpus documents.
	 */
	public void initialize(List<List<String>> domainVocabularyCorpus) {
		final HashMap<String, Integer> vocab = new HashMap<String, Integer>();
		for (final List<String> doc : domainVocabularyCorpus) {
			for (final String s : doc) {
				Integer current = vocab.get(s);
				if (current == null)
					current = 0;
				vocab.put(s, current + 1);

			}
		}
		this.vocabList = new ArrayList<String>();
		for (final Entry<String, Integer> entry : vocab.entrySet()) {
			if (entry.getValue() > wordOccuranceThresh) {
				vocabList.add(entry.getKey());
			}
		}
	}

	@Override
	public DoubleFV extractFeature(List<String> tokens) {
		final double[] vect = new double[vocabList.size()];
		for (int i = 0; i < vect.length; i++) {
			vect[i] += 0.00001;
		}
		for (final String s : tokens) {
			final int ind = vocabList.indexOf(s);
			if (ind >= 0)
				vect[ind] += 1;
		}
		final double[] vectNorm = new double[vocabList.size()];
		for (int i = 0; i < vect.length; i++) {
			vectNorm[i] = vect[i] / tokens.size();
		}
		return new DoubleFV(vect);
	}

}
