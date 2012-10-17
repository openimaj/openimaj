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

/*import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.openimaj.feature.FeatureExtractor;
import org.openimaj.math.model.GaussianVectorNaiveBayesModel;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.text.nlp.sentiment.model.SentimentAnnotator;
import org.openimaj.text.nlp.sentiment.model.TokenListSentimentAnnotator;
import org.openimaj.text.nlp.sentiment.type.BipolarSentiment;
import org.openimaj.text.nlp.sentiment.type.WeightedBipolarSentiment;
import org.openimaj.util.pair.IndependentPair;

*//**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 *//*
public class GaussianNaiveBayesBiopolarGeneralSentimentAnnotator<EXTRACTOR extends FeatureExtractor<?, List<String>>, CLONETYPE extends SentimentAnnotator<List<String>,EXTRACTOR,CLONETYPE>>
		extends
		TokenListSentimentAnnotator<EXTRACTOR, CLONETYPE> {

	private GaussianVectorNaiveBayesModel<WeightedBipolarSentiment> gaussianVectorModel;
	private ArrayList<String> vocabList;

	public GaussianNaiveBayesBiopolarGeneralSentimentAnnotator() {
		gaussianVectorModel = new GaussianVectorNaiveBayesModel<WeightedBipolarSentiment>();
	}

	@Override
	public GaussianNaiveBayesBiopolarGeneralSentimentAnnotator clone() {
		return new GaussianNaiveBayesBiopolarGeneralSentimentAnnotator();
	}

	public void estimate(
			List<? extends IndependentPair<List<String>, WeightedBipolarSentiment>> data) {
		List<IndependentPair<double[], WeightedBipolarSentiment>> procData = new ArrayList<IndependentPair<double[], WeightedBipolarSentiment>>();
		HashMap<String, Integer> vocab = new HashMap<String, Integer>();
		for (IndependentPair<List<String>, WeightedBipolarSentiment> independentPair : data) {
			for (String s : independentPair.firstObject()) {
				Integer current = vocab.get(s);
				if (current == null)
					current = 0;
				vocab.put(s, current + 1);

			}
		}
		this.vocabList = new ArrayList<String>();
		for (Entry<String, Integer> entry : vocab.entrySet()) {
			if (entry.getValue() > 50) {
				vocabList.add(entry.getKey());
			}
		}
		for (IndependentPair<List<String>, WeightedBipolarSentiment> independentPair : data) {
			procData.add(createDoubleArrSentiment(independentPair));
		}
		gaussianVectorModel.estimate(procData);
	}

	private IndependentPair<double[], WeightedBipolarSentiment> createDoubleArrSentiment(
			IndependentPair<List<String>, WeightedBipolarSentiment> independentPair) {
		double[] vect = createDoubleArr(independentPair.firstObject());
		IndependentPair<double[], WeightedBipolarSentiment> toAdd = IndependentPair
				.pair(vect, independentPair.secondObject());
		return toAdd;
	}

	private double[] createDoubleArr(List<String> strings) {
		double[] vect = new double[vocabList.size()];
		for (int i = 0; i < vect.length; i++) {
			vect[i] += 0.00001;

		}
		for (String s : strings) {
			int ind = vocabList.indexOf(s);
			if (ind >= 0)
				vect[ind] += 1;
		}
		return vect;
	}

	public boolean validate(
			IndependentPair<List<String>, WeightedBipolarSentiment> data) {
		WeightedBipolarSentiment pred = this.predict(data.firstObject());
		BipolarSentiment predBipolar = pred.bipolar();
		BipolarSentiment valiBipolar = data.secondObject().bipolar();
		return valiBipolar.equals(predBipolar);
	}

	public WeightedBipolarSentiment predict(List<String> data) {
		return gaussianVectorModel.predict(createDoubleArr(data));
	}

	public int numItemsToEstimate() {
		return 0;
	}

	public double calculateError(
			List<? extends IndependentPair<List<String>, WeightedBipolarSentiment>> data) {
		double total = data.size();
		double correct = 0;
		for (IndependentPair<List<String>, WeightedBipolarSentiment> independentPair : data) {
			correct += validate(independentPair) ? 1 : 0;
		}
		return 1 - (correct / total);
	}

	@Override
	public List<ScoredAnnotation<BipolarSentiment>> annotate(List<String> object) {
		// TODO Auto-generated method stub
		return null;
	}

}*/
